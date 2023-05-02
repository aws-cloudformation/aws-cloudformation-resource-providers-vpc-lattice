package software.amazon.vpclattice.resourcepolicy;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ExceptionHandler;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class DeleteHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(@Nonnull AmazonWebServicesClientProxy proxy, @Nonnull ResourceHandlerRequest<ResourceModel> request, @Nonnull CallbackContext callbackContext, @Nonnull ProxyClient<VpcLatticeClient> proxyClient, @Nonnull Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> proxy.initiate("AWS::VpcLattice::ResourcePolicy::CheckExist", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Function.identity())
                        .makeServiceCall(this::isResourcePolicyExisted)
                        .handleError(ExceptionHandler::handleError)
                        .done((isResourcePolicyExisted) -> {
                            if (!isResourcePolicyExisted) {
                                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, progress.getResourceModel().getResourceArn());
                            }

                            return progress;
                        }))
                .then((progress) -> proxy.initiate("AWS::VpcLattice::ResourcePolicy::DeleteResourcePolicy", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::createDeleteResourcePolicyRequest)
                        .makeServiceCall((deleteResourcePolicyRequest, client) ->
                                client.injectCredentialsAndInvokeV2(deleteResourcePolicyRequest, client.client()::deleteResourcePolicy))
                        .handleError(ExceptionHandler::handleError)
                        .done((deleteResourcePolicyResponse) -> ProgressEvent.defaultSuccessHandler(null)));
    }
}
