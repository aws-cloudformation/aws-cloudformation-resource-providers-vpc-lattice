package software.amazon.vpclattice.servicenetworkserviceassociation;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ExceptionHandler;
import software.amazon.vpclattice.common.TagHelper;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class UpdateHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final CallbackContext callbackContext,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> this.updateTags(progress, proxy, request, proxyClient))
                .then((progress) -> new ReadHandler().handleRequest(proxy, request, null, logger));
    }

    protected ProgressEvent<ResourceModel, CallbackContext> updateTags(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate("AWS::VpcLattice::ServiceNetworkServiceAssociation::UpdateTags", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
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
}
