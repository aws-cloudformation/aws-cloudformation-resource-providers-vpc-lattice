package software.amazon.vpclattice.authpolicy;

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
                .then((progress) -> proxy.initiate("AWS::VpcLattice::AuthPolicy::CheckExist", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Function.identity())
                        .makeServiceCall(this::isAuthPolicyExisted)
                        .handleError(ExceptionHandler::handleError)
                        .done((isAuthPolicyExisted) -> {
                            if (!isAuthPolicyExisted) {
                                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, progress.getResourceModel().getResourceIdentifier());
                            }

                            return progress;
                        }))
                .then((progress) -> proxy.initiate("AWS::VpcLattice::AuthPolicy::DeleteAuthPolicy", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::createDeleteAuthPolicyRequest)
                        .makeServiceCall((deleteAuthPolicyRequest, client) ->
                                client.injectCredentialsAndInvokeV2(deleteAuthPolicyRequest, client.client()::deleteAuthPolicy))
                        .handleError(ExceptionHandler::handleError)
                        .done((deleteAuthPolicyResponse) -> ProgressEvent.defaultSuccessHandler(null)));
    }
}
