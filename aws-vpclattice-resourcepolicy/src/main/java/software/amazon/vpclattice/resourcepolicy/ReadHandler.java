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

public class ReadHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(@Nonnull AmazonWebServicesClientProxy proxy, @Nonnull ResourceHandlerRequest<ResourceModel> request, @Nonnull CallbackContext callbackContext, @Nonnull ProxyClient<VpcLatticeClient> proxyClient, @Nonnull Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> proxy.initiate("AWS::VpcLattice::ResourcePolicy::GetResourcePolicy", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::createGetResourcePolicyRequest)
                        .makeServiceCall((getResourcePolicyRequest, client) ->
                                client.injectCredentialsAndInvokeV2(getResourcePolicyRequest, client.client()::getResourcePolicy))
                        .handleError(ExceptionHandler::handleError)
                        .done((getResourcePolicyResponse) -> {
                            if (this.isResourcePolicyEmpty(getResourcePolicyResponse)) {
                                throw new CfnNotFoundException(ResourceModel.TYPE_NAME, progress.getResourceModel().getResourceArn());
                            }

                            return ProgressEvent.defaultSuccessHandler(Translator.createResourceModel(progress.getResourceModel().getResourceArn(), getResourcePolicyResponse));
                        }));
    }
}
