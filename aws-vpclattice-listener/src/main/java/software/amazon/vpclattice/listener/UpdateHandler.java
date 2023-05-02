package software.amazon.vpclattice.listener;

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
            @Nonnull final ProxyClient<software.amazon.awssdk.services.vpclattice.VpcLatticeClient> proxyClient,
            @Nonnull final Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> this.updateListener(progress, proxy, request, proxyClient))
                .then((progress) -> this.updateTags(progress, proxy, request, proxyClient))
                .then((progress) -> new ReadHandler().handleRequest(proxy, request, null, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateListener(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final ProxyClient<software.amazon.awssdk.services.vpclattice.VpcLatticeClient> proxyClient) {
        if (!this.shouldUpdateListener(request.getPreviousResourceState(), request.getDesiredResourceState())) {
            return progress;
        }

        return proxy.initiate(
                        "AWS::VpcLattice::Listener::UpdateListener",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext()
                )
                .translateToServiceRequest(Translator::createUpdateListenerRequest)
                .makeServiceCall((updateListenerRequest, client) ->
                        client.injectCredentialsAndInvokeV2(updateListenerRequest, client.client()::updateListener))
                .handleError(ExceptionHandler::handleError)
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateTags(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate("AWS::VpcLattice::Listener::UpdateTags", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
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

    private boolean shouldUpdateListener(
            @Nonnull final ResourceModel previousModel,
            @Nonnull final ResourceModel model) {
        return !Objects.equals(previousModel.getDefaultAction(), model.getDefaultAction());
    }
}
