package software.amazon.vpclattice.accesslogsubscription;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ExceptionHandler;

import javax.annotation.Nonnull;

public class CreateHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final CallbackContext callbackContext,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> this.createAccessLogSubscription(progress, proxy, request, proxyClient))
                .then((progress) -> new ReadHandler().handleRequest(proxy, request.toBuilder().desiredResourceState(progress.getResourceModel()).build(), null, logger));
    }

    protected ProgressEvent<ResourceModel, CallbackContext> createAccessLogSubscription(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate(
                        "AWS::VpcLattice::AccessLogSubscription::CreateAccessLogSubscription",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext()
                )
                .translateToServiceRequest((_model) -> Translator.createCreateAccessLogSubscriptionRequest(request))
                .makeServiceCall(((createAccessLogSubscriptionRequest, client) ->
                        client.injectCredentialsAndInvokeV2(createAccessLogSubscriptionRequest, client.client()::createAccessLogSubscription)))
                .handleError(ExceptionHandler::handleError)
                .done((createAccessLogSubscriptionResponse) -> {
                    final var model = progress.getResourceModel();

                    model.setArn(createAccessLogSubscriptionResponse.arn());

                    return ProgressEvent.defaultInProgressHandler(progress.getCallbackContext(), 0, model);
                });
    }
}
