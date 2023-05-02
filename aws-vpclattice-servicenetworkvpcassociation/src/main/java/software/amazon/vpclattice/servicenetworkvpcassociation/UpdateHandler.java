package software.amazon.vpclattice.servicenetworkvpcassociation;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ExceptionHandler;
import software.amazon.vpclattice.common.TagHelper;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

public class UpdateHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final CallbackContext callbackContext,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient, @Nonnull Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> this.updateServiceNetworkVpcAssociation(progress, proxy, request, proxyClient))
                .then((progress) ->
                        this.shouldUpdateServiceNetworkVpcAssociation(request) ? this.waitServiceNetworkVpcAssociationActive(progress, proxy, proxyClient) : progress)
                .then((progress) -> this.updateTags(progress, proxy, request, proxyClient))
                .then((progress) -> new ReadHandler().handleRequest(proxy, request, null, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateServiceNetworkVpcAssociation(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) {
        if (!this.shouldUpdateServiceNetworkVpcAssociation(request) || progress.getCallbackContext().hasCalledUpdate) {
            return progress;
        }

        return proxy.initiate(
                        "AWS::VpcLattice::ServiceNetworkVpcAssociation::UpdateServiceNetworkVpcAssociation",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext()
                )
                .translateToServiceRequest(Translator::createUpdateServiceNetworkVpcAssociationRequest)
                .backoffDelay(BACKOFF_STRATEGY)
                .makeServiceCall((updateServiceNetworkVpcAssociationRequest, client) ->
                        client.injectCredentialsAndInvokeV2(updateServiceNetworkVpcAssociationRequest, client.client()::updateServiceNetworkVpcAssociation))
                .handleError(ExceptionHandler::handleError)
                .done((updateServiceNetworkVpcAssociationResponse) -> {
                    final var model = progress.getResourceModel();

                    progress.getCallbackContext().setHasCalledUpdate(true);

                    return ProgressEvent.defaultInProgressHandler(progress.getCallbackContext(), 2, model);
                });
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateTags(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate("AWS::VpcLattice::ServiceNetworkVpcAssociation::UpdateTags", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest(Function.identity())
                .makeServiceCall(TagHelper.updateTagsServiceCall(
                        TagHelper.UpdateTagsServiceCall.<ResourceModel, Tag>builder()
                                .arnProvider(ResourceModel::getArn)
                                .tagConstructor(Tag::new)
                                .tagKeyProvider(Tag::getKey)
                                .tagValueProvider(Tag::getValue)
                                .request(request)
                                .build()))
                .handleError(ExceptionHandler::handleError)
                .progress();
    }

    private boolean shouldUpdateServiceNetworkVpcAssociation(
            @Nonnull final ResourceHandlerRequest<ResourceModel> request) {
        return this.shouldUpdateServiceNetworkVpcAssociation(request.getPreviousResourceState(), request.getDesiredResourceState());
    }

    private boolean shouldUpdateServiceNetworkVpcAssociation(
            @Nonnull final ResourceModel previousModel,
            @Nonnull final ResourceModel model) {
        return !Objects.equals(
                previousModel.getSecurityGroupIds(),
                model.getSecurityGroupIds()
        );
    }
}
