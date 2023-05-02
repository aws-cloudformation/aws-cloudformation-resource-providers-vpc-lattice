package software.amazon.vpclattice.targetgroup;

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
            @Nonnull AmazonWebServicesClientProxy proxy,
            @Nonnull ResourceHandlerRequest<ResourceModel> request,
            @Nonnull CallbackContext callbackContext,
            @Nonnull ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) ->
                        proxy.initiate("AWS::VpcLattice::ListTargetGroups", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest((model) -> Translator.createListTargetGroupsRequest(request.getNextToken()))
                                .makeServiceCall((listTargetGroupsRequest, client) ->
                                        client.injectCredentialsAndInvokeV2(listTargetGroupsRequest, client.client()::listTargetGroups))
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