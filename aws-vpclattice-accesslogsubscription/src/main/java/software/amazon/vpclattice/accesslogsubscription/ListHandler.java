package software.amazon.vpclattice.accesslogsubscription;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ExceptionHandler;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;

public class ListHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final CallbackContext callbackContext,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> proxy.initiate(
                                "AWS::VpcLattice::AccessLogSubscription::ListAccessLogSubscriptions",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                        .translateToServiceRequest((model) -> Translator.createListAccessLogSubscriptionsRequest(model, request.getNextToken()))
                        .makeServiceCall((listAccessLogSubscriptionsRequest, client) ->
                                client.injectCredentialsAndInvokeV2(listAccessLogSubscriptionsRequest, client.client()::listAccessLogSubscriptions))
                        .handleError(ExceptionHandler::handleError)
                        .done((response) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                .status(OperationStatus.SUCCESS)
                                .resourceModels(response.items()
                                        .stream()
                                        .map(Translator::createResourceModel)
                                        .collect(Collectors.toList()))
                                .nextToken(response.nextToken())
                                .build()));
    }
}