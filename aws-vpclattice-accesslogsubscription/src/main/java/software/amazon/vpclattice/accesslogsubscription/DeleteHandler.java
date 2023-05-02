package software.amazon.vpclattice.accesslogsubscription;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ExceptionHandler;

import javax.annotation.Nonnull;

public class DeleteHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final CallbackContext callbackContext,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> this.deleteAccessLogSubscription(progress, proxy, proxyClient));
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteAccessLogSubscription(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate(
                        "AWS::VpcLattice::AccessLogSubscription::DeleteAccessLogSubscription",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext()
                )
                .translateToServiceRequest(Translator::createDeleteAccessLogSubscriptionRequest)
                .makeServiceCall((deleteAccessLogSubscriptionRequest, client) ->
                        client.injectCredentialsAndInvokeV2(deleteAccessLogSubscriptionRequest, client.client()::deleteAccessLogSubscription))
                .handleError(ExceptionHandler::handleError)
                .done((_response) -> ProgressEvent.defaultSuccessHandler(null));
    }
}
