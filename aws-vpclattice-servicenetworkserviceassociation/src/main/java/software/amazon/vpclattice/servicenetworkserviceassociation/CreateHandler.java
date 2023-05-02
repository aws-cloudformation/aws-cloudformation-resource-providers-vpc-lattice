package software.amazon.vpclattice.servicenetworkserviceassociation;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ExceptionHandler;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class CreateHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final CallbackContext callbackContext,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> this.createServiceNetworkServiceAssociation(progress, proxy, request, proxyClient))
                .then(((progress) -> this.waitServiceNetworkServiceAssociationActive(progress, proxy, proxyClient)))
                .then((progress) -> new ReadHandler().handleRequest(proxy, request.toBuilder().desiredResourceState(progress.getResourceModel()).build(), null, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> createServiceNetworkServiceAssociation(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate(
                        "AWS::VpcLattice::ServiceNetworkVpcAssociation::CreateServiceNetworkVpcAssociation",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext()
                )
                .translateToServiceRequest((_model) -> Translator.createCreateServiceNetworkServiceAssociationRequest(request))
                .makeServiceCall((createServiceNetworkServiceAssociationRequest, client) ->
                        client.injectCredentialsAndInvokeV2(createServiceNetworkServiceAssociationRequest, client.client()::createServiceNetworkServiceAssociation))
                .handleError(ExceptionHandler::handleError)
                .done((createServiceNetworkServiceAssociationResponse) -> {
                    final var model = progress.getResourceModel();

                    model.setArn(createServiceNetworkServiceAssociationResponse.arn());

                    return ProgressEvent.defaultInProgressHandler(progress.getCallbackContext(), 0, model);
                });
    }

    private ProgressEvent<ResourceModel, CallbackContext> waitServiceNetworkServiceAssociationActive(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate(
                        "AWS::VpcLattice::ServiceNetworkServiceAssociation::WaitForServiceNetworkServiceAssociationActive",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext()
                )
                .translateToServiceRequest(Function.identity())
                .backoffDelay(BACKOFF_STRATEGY)
                .makeServiceCall((model, _client) -> null)
                .stabilize((_request, _response, client, model, _context) ->
                        this.isServiceNetworkServiceAssociationActive(client, model))
                .handleError(ExceptionHandler::handleError)
                .progress();

    }
}
