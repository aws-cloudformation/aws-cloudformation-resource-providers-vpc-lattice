package software.amazon.vpclattice.targetgroup;

import software.amazon.awssdk.services.vpclattice.model.ListTargetsResponse;
import software.amazon.awssdk.services.vpclattice.model.TargetStatus;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ExceptionHandler;
import software.amazon.vpclattice.common.LatticeNotStabilizedException;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DeleteHandler extends BaseHandlerStd {
    private static final Integer TARGETS_DRAINING_TIME_IN_SECONDS = 5 * 60;

    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull AmazonWebServicesClientProxy proxy,
            @Nonnull ResourceHandlerRequest<ResourceModel> request,
            @Nonnull CallbackContext callbackContext,
            @Nonnull ProxyClient<software.amazon.awssdk.services.vpclattice.VpcLatticeClient> proxyClient,
            @Nonnull Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> this.listTargets(progress, proxy, proxyClient))
                .then((progress) -> this.deregisterTargets(progress, proxy, proxyClient))
                .then((progress) -> this.listTargets(progress, proxy, proxyClient))
                .then((progress) -> this.deleteTargetGroup(progress, proxy, proxyClient));
    }

    protected ProgressEvent<ResourceModel, CallbackContext> listTargets(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull AmazonWebServicesClientProxy proxy,
            @Nonnull ProxyClient<software.amazon.awssdk.services.vpclattice.VpcLatticeClient> proxyClient) {
        return proxy.initiate("AWS::VpcLattice::TargetGroup::ListTargets", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Translator::createListTargetsRequest)
                .makeServiceCall((listTargetsRequest, client) ->
                        client.injectCredentialsAndInvokeV2(listTargetsRequest, client.client()::listTargets))
                .handleError(ExceptionHandler::handleError)
                .done((response) -> {
                    if (response.items() == null || response.items().isEmpty()) {
                        progress.getResourceModel().setTargets(List.of());

                        return progress;
                    }

                    if (this.areAllTargetsInDrainingStatus(response)) {
                        // Wait for status draining
                        return ProgressEvent.defaultInProgressHandler(progress.getCallbackContext(), TARGETS_DRAINING_TIME_IN_SECONDS, progress.getResourceModel());
                    }

                    progress.getResourceModel().setTargets(response.items().stream().map(Translator::convertTargetFromSdk).collect(Collectors.toList()));

                    return progress;
                });
    }

    protected ProgressEvent<ResourceModel, CallbackContext> deregisterTargets(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull AmazonWebServicesClientProxy proxy,
            @Nonnull ProxyClient<software.amazon.awssdk.services.vpclattice.VpcLatticeClient> proxyClient) {
        return proxy.initiate("AWS::VpcLattice::TargetGroup::DeregisterTargets", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Translator::createDeregisterTargetsRequest)
                .makeServiceCall((deregisterTargetsRequest, client) -> {
                    if (!deregisterTargetsRequest.hasTargets() || deregisterTargetsRequest.targets().isEmpty()) {
                        return null;
                    }

                    final var response = client.injectCredentialsAndInvokeV2(deregisterTargetsRequest, client.client()::deregisterTargets);

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

    protected ProgressEvent<ResourceModel, CallbackContext> deleteTargetGroup(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull AmazonWebServicesClientProxy proxy,
            @Nonnull ProxyClient<software.amazon.awssdk.services.vpclattice.VpcLatticeClient> proxyClient) {
        return proxy.initiate("AWS::VpcLattice::TargetGroup::DeleteTargetGroup", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Translator::createDeleteTargetGroupRequest)
                .makeServiceCall((deleteTargetGroupRequest, client) ->
                        client.injectCredentialsAndInvokeV2(deleteTargetGroupRequest, client.client()::deleteTargetGroup))
                .stabilize(((deleteTargetGroupRequest, response, client, model, _callbackContext) -> this.isTargetGroupDeleted(client, model)))
                .handleError(ExceptionHandler::handleError)
                .done((response) -> ProgressEvent.defaultSuccessHandler(null));
    }

    private boolean areAllTargetsInDrainingStatus(
            @Nonnull final ListTargetsResponse response) {
        return response.items().stream().allMatch((target) -> TargetStatus.DRAINING == target.status());
    }
}