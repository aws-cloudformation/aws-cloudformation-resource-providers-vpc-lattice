package software.amazon.vpclattice.targetgroup;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ExceptionHandler;
import software.amazon.vpclattice.common.LatticeNotStabilizedException;
import software.amazon.vpclattice.common.TagHelper;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull AmazonWebServicesClientProxy proxy,
            @Nonnull ResourceHandlerRequest<ResourceModel> request,
            @Nonnull CallbackContext callbackContext,
            @Nonnull ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> this.updateTargetGroup(progress, request, proxy, proxyClient))
                .then((progress) -> this.updateTags(progress, request, proxy, proxyClient))
                .then((progress) -> this.deregisterTargets(progress, request, proxy, proxyClient))
                .then((progress) -> this.registerTargets(progress, request, proxy, proxyClient))
                .then((progress) -> new ReadHandler().handleRequest(proxy, request, null, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateTargetGroup(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull ProxyClient<VpcLatticeClient> proxyClient) {
        this.validateTargetGroupCanRegisterTargets(progress.getResourceModel());

        if (!shouldUpdateTargetGroup(request.getPreviousResourceState(), request.getDesiredResourceState())) {
            return progress;
        }

        return proxy.initiate("AWS::VpcLattice::TargetGroup::UpdateTargetGroup", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Translator::createUpdateTargetGroupRequest)
                .makeServiceCall(((updateTargetGroupRequest, client) -> client.injectCredentialsAndInvokeV2(updateTargetGroupRequest, client.client()::updateTargetGroup)))
                .handleError(ExceptionHandler::handleError)
                .done((updateTargetGroupResponse) -> {
                    final var model = progress.getResourceModel();

                    model.setConfig(Translator.convertTargetGroupConfigFromSdk(updateTargetGroupResponse.config()));

                    return progress;
                });
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateTags(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate("AWS::VpcLattice::TargetGroup::UpdateTags", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Function.identity())
                .makeServiceCall(TagHelper.updateTagsServiceCall(
                        TagHelper.UpdateTagsServiceCall.<ResourceModel, Tag>builder()
                                .arnProvider(ResourceModel::getArn)
                                .tagConstructor(Tag::new)
                                .tagKeyProvider(Tag::getKey)
                                .tagValueProvider(Tag::getValue)
                                .request(request)
                                .build())
                )
                .handleError(ExceptionHandler::handleError)
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> registerTargets(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate("AWS::VpcLattice::TargetGroup::RegisterTargets", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Function.identity())
                .makeServiceCall((model, client) -> {
                    final var previousModel = request.getPreviousResourceState();

                    final var targets = this.getTargetsSetFromModel(model);

                    final var previousTargets = this.getTargetsSetFromModel(previousModel);

                    final var targetsToRegister = Sets.difference(targets, previousTargets);

                    if (targetsToRegister.isEmpty()) {
                        return null;
                    }

                    final var registerRequest = Translator.createRegisterTargetsRequest(ResourceModel.builder()
                            .arn(model.getArn())
                            .targets(new ArrayList<>(targetsToRegister))
                            .build());

                    final var response = client.injectCredentialsAndInvokeV2(registerRequest, client.client()::registerTargets);

                    if (response.unsuccessful() != null && !response.unsuccessful().isEmpty()) {
                        final var errorMessage = this.getFailedRegisterTargetErrorMessage(response.unsuccessful());

                        throw new LatticeNotStabilizedException(errorMessage);
                    }

                    return response;
                })
                .handleError(ExceptionHandler::handleError)
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> deregisterTargets(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate("AWS::VpcLattice::TargetGroup::DeregisterTargets", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Function.identity())
                .makeServiceCall((model, client) -> {
                    final var previousModel = request.getPreviousResourceState();

                    final var targets = getTargetsSetFromModel(model);

                    final var previousTargets = getTargetsSetFromModel(previousModel);

                    final var targetsToDeregister = Sets.difference(previousTargets, targets);

                    if (targetsToDeregister.isEmpty()) {
                        return null;
                    }

                    final var deregisterRequest = Translator.createDeregisterTargetsRequest(ResourceModel.builder()
                            .arn(model.getArn())
                            .targets(new ArrayList<>(targetsToDeregister))
                            .build());

                    final var response = client.injectCredentialsAndInvokeV2(deregisterRequest, client.client()::deregisterTargets);

                    final var unsuccessful = Optional
                            .ofNullable(response.unsuccessful())
                            .orElse(List.of())
                            .stream()
                            .filter((targetFailure) -> !"TargetNotFound".equalsIgnoreCase(targetFailure.failureCode()))
                            .collect(Collectors.toList());

                    if (!unsuccessful.isEmpty()) {
                        final var errorMessage = this.getFailedDeregisterTargetsErrorMessage(unsuccessful);

                        throw new LatticeNotStabilizedException(errorMessage);
                    }

                    return response;
                })
                .handleError(ExceptionHandler::handleError)
                .progress();
    }


    private ImmutableSet<Target> getTargetsSetFromModel(
            @Nonnull final ResourceModel model) {
        return Optional.ofNullable(model.getTargets())
                .map(ImmutableSet::copyOf)
                .orElse(ImmutableSet.of());
    }

    private boolean shouldUpdateTargetGroup(
            @Nonnull final ResourceModel previousModel,
            @Nonnull final ResourceModel model) {
        return !Objects.equals(
                Optional.ofNullable(previousModel.getConfig())
                        .map(TargetGroupConfig::getHealthCheck),
                Optional.ofNullable(model.getConfig())
                        .map(TargetGroupConfig::getHealthCheck)
        );
    }
}
