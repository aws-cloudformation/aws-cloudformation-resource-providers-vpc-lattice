package software.amazon.vpclattice.resourcepolicy;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.GetResourcePolicyResponse;
import software.amazon.awssdk.services.vpclattice.model.ResourceNotFoundException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ClientBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final CallbackContext callbackContext,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final Logger logger
    );

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nullable final CallbackContext callbackContext,
            @Nonnull final Logger logger) {
        return this.handleRequest(
                proxy,
                request,
                Optional.ofNullable(callbackContext).orElse(new CallbackContext()),
                proxy.newProxy(ClientBuilder::getClient),
                logger
        );
    }

    protected boolean isResourcePolicyExisted(
            @Nonnull final ResourceModel model,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) throws CfnGeneralServiceException {
        try {
            final var request = Translator.createGetResourcePolicyRequest(model);

            final var response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::getResourcePolicy);

            return !isResourcePolicyEmpty(response);
        } catch (ResourceNotFoundException e) {
            return false;
        } catch (Exception e) {
            throw new CfnGeneralServiceException(e);
        }
    }

    protected boolean isResourcePolicyEmpty(
            @Nonnull final GetResourcePolicyResponse response) {
        return response.policy() == null || response.policy().isEmpty();
    }
}
