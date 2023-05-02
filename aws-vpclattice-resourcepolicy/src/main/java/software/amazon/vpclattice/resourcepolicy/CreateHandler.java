package software.amazon.vpclattice.resourcepolicy;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
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
                .then((progress) -> proxy.initiate("AWS::VpcLattice::ResourcePolicy::CheckExist", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Function.identity())
                        .makeServiceCall(this::isResourcePolicyExisted)
                        .handleError(ExceptionHandler::handleError)
                        .done((isResourcePolicyExisted) -> {
                            if (isResourcePolicyExisted) {
                                throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, progress.getResourceModel().getResourceArn());
                            }

                            return progress;
                        }))
                .then((progress) -> proxy.initiate("AWS::VpcLattice::ResourcePolicy::PutResourcePolicy", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::createPutResourcePolicyRequest)
                        .makeServiceCall((putResourcePolicyRequest, client) ->
                                client.injectCredentialsAndInvokeV2(putResourcePolicyRequest, client.client()::putResourcePolicy))
                        .handleError(ExceptionHandler::handleError)
                        .progress())
                .then((progress) -> new ReadHandler().handleRequest(proxy, request, null, logger));
    }
}
