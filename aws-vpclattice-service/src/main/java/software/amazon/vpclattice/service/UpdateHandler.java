package software.amazon.vpclattice.service;

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
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> this.updateService(progress, proxy, request, proxyClient))
                .then((progress) -> this.updateTags(progress, proxy, request, proxyClient))
                .then((progress) -> new ReadHandler().handleRequest(proxy, request, null, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateService(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) {
        if (!this.shouldUpdateService(request.getPreviousResourceState(), request.getDesiredResourceState())) {
            return progress;
        }

        return proxy.initiate(
                        "AWS::VpcLattice::Service::UpdateService",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext()
                )
                .translateToServiceRequest(Translator::createUpdateServiceRequest)
                .makeServiceCall((updateServiceRequest, client) ->
                        client.injectCredentialsAndInvokeV2(updateServiceRequest, client.client()::updateService))
                .handleError(ExceptionHandler::handleError)
                .progress();
    }

    private ProgressEvent<ResourceModel, CallbackContext> updateTags(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate("AWS::VpcLattice::Service::UpdateTags", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
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

    private boolean shouldUpdateService(
            @Nonnull final ResourceModel previousModel,
            @Nonnull final ResourceModel model) {
        return !Objects.equals(previousModel.getAuthType(), model.getAuthType())
                || !Objects.equals(previousModel.getCertificateArn(), model.getCertificateArn());
    }
}
