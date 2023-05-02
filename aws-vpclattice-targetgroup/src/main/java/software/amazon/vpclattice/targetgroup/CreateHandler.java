package software.amazon.vpclattice.targetgroup;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ExceptionHandler;
import software.amazon.vpclattice.common.LatticeNotStabilizedException;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class CreateHandler extends BaseHandlerStd {
    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final CallbackContext callbackContext,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> this.createTargetGroup(progress, proxy, request, proxyClient))
                .then((progress) -> this.waitForTargetGroupActive(progress, proxy, proxyClient))
                .then((progress) -> this.registerTargets(progress, proxy, proxyClient))
                .then((progress) -> new ReadHandler().handleRequest(proxy, request.toBuilder().desiredResourceState(progress.getResourceModel()).build(), null, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> createTargetGroup(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate("AWS::VpcLattice::TargetGroup::CreateTargetGroup", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest((model) -> Translator.createCreateTargetGroupRequest(request))
                .makeServiceCall((targetGroupRequest, client) ->
                        client.injectCredentialsAndInvokeV2(targetGroupRequest, client.client()::createTargetGroup))
                .handleError(ExceptionHandler::handleError)
                .done((createResponse) -> {
                    final var model = progress.getResourceModel();

                    model.setArn(createResponse.arn());

                    return ProgressEvent.defaultInProgressHandler(progress.getCallbackContext(), 0, model);
                });
    }

    private ProgressEvent<ResourceModel, CallbackContext> waitForTargetGroupActive(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate("AWS::VpcLattice::TargetGroup::WaitTargetGroupActive", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Function.identity())
                .makeServiceCall((_request, client) -> null)
                .stabilize((_request, _response, client, model, _context) -> this.isTargetGroupActive(proxyClient, model))
                .handleError(ExceptionHandler::handleError)
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> registerTargets(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) {
        final var resourceModel = progress.getResourceModel();

        this.validateTargetGroupCanRegisterTargets(resourceModel);

        return proxy.initiate("AWS::VpcLattice::TargetGroup::RegisterTargets", proxyClient, resourceModel, progress.getCallbackContext())
                .translateToServiceRequest(Translator::createRegisterTargetsRequest)
                .makeServiceCall((targetsRequest, client) -> {
                    if (targetsRequest.targets() == null || targetsRequest.targets().isEmpty()) {
                        return null;
                    }

                    final var response = client.injectCredentialsAndInvokeV2(targetsRequest, client.client()::registerTargets);

                    if (response.unsuccessful() != null && !response.unsuccessful().isEmpty()) {
                        final var errorMessage = this.getFailedRegisterTargetErrorMessage(response.unsuccessful());

                        throw new LatticeNotStabilizedException(errorMessage);
                    }

                    return response;
                })
                .handleError(ExceptionHandler::handleError)
                .progress();
    }
}
