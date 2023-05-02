package software.amazon.vpclattice.service;

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
                .then((progress) -> this.createService(progress, proxy, request, proxyClient))
                .then((progress) -> this.waitServiceActive(progress, proxy, proxyClient))
                .then((progress) -> new ReadHandler().handleRequest(proxy, request.toBuilder().desiredResourceState(progress.getResourceModel()).build(), null, logger));
    }

    protected ProgressEvent<ResourceModel, CallbackContext> createService(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate(
                        "AWS::VpcLattice::Service::CreateService",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext()
                )
                .translateToServiceRequest((_model) -> Translator.createCreateServiceRequest(request))
                .makeServiceCall(((createServiceRequest, client) ->
                        client.injectCredentialsAndInvokeV2(createServiceRequest, client.client()::createService)))
                .handleError(ExceptionHandler::handleError)
                .done((createServiceResponse) -> {
                    final var model = progress.getResourceModel();

                    model.setArn(createServiceResponse.arn());

                    return ProgressEvent.defaultInProgressHandler(progress.getCallbackContext(), 0, model);
                });
    }

    private ProgressEvent<ResourceModel, CallbackContext> waitServiceActive(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate(
                        "AWS::VpcLattice::Service::WaitForServiceActive",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext()
                )
                .translateToServiceRequest(Function.identity())
                .makeServiceCall((model, _client) -> null)
                .stabilize((_request, _response, client, model, _context) ->
                        this.isServiceActive(client, model))
                .handleError(ExceptionHandler::handleError)
                .progress();

    }
}
