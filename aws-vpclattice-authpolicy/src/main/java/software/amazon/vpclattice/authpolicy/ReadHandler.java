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

public class ReadHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(@Nonnull AmazonWebServicesClientProxy proxy, @Nonnull ResourceHandlerRequest<ResourceModel> request, @Nonnull CallbackContext callbackContext, @Nonnull ProxyClient<VpcLatticeClient> proxyClient, @Nonnull Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> proxy.initiate("AWS::VpcLattice::AuthPolicy::GetAuthPolicy", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::createGetAuthPolicyRequest)
                        .makeServiceCall((getAuthPolicyRequest, client) ->
                                client.injectCredentialsAndInvokeV2(getAuthPolicyRequest, client.client()::getAuthPolicy))
                        .handleError(ExceptionHandler::handleError)
                        .done((getAuthPolicyResponse) -> {
                            if (this.isAuthPolicyEmpty(getAuthPolicyResponse)) {
                                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, progress.getResourceModel().getResourceIdentifier());
                            }

                            return ProgressEvent.defaultSuccessHandler(Translator.createResourceModel(progress.getResourceModel().getResourceIdentifier(), getAuthPolicyResponse));
                        }));
    }
}
