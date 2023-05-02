package software.amazon.vpclattice.servicenetworkvpcassociation;

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
                .then((progress) -> this.createServiceNetworkVpcAssociation(progress, proxy, request, proxyClient))
                .then((progress) -> this.waitServiceNetworkVpcAssociationActive(progress, proxy, proxyClient))
                .then((progress) -> new ReadHandler().handleRequest(proxy, request.toBuilder().desiredResourceState(progress.getResourceModel()).build(), null, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> createServiceNetworkVpcAssociation(
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
                .translateToServiceRequest((_model) -> Translator.createCreateServiceNetworkVpcAssociationRequest(request))
                .makeServiceCall((createServiceNetworkVpcAssociationRequest, client) ->
                        client.injectCredentialsAndInvokeV2(createServiceNetworkVpcAssociationRequest, client.client()::createServiceNetworkVpcAssociation))
                .handleError(ExceptionHandler::handleError)
                .done((createServiceNetworkVpcAssociationResponse) -> {
                    final var model = progress.getResourceModel();

                    model.setArn(createServiceNetworkVpcAssociationResponse.arn());

                    return ProgressEvent.defaultInProgressHandler(progress.getCallbackContext(), 0, model);
                });
    }
}
