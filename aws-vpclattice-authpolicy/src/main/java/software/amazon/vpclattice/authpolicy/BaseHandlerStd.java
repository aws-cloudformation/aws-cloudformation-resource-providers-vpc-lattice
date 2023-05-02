package software.amazon.vpclattice.authpolicy;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.GetAuthPolicyResponse;
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
    protected Logger logger;

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final CallbackContext callbackContext,
            @Nonnull final ProxyClient<software.amazon.awssdk.services.vpclattice.VpcLatticeClient> proxyClient,
            @Nonnull final Logger logger
    );

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nullable final CallbackContext callbackContext,
            @Nonnull final Logger logger) {
        this.logger = logger;

        return this.handleRequest(
                proxy,
                request,
                Optional.ofNullable(callbackContext).orElse(new CallbackContext()),
                proxy.newProxy(ClientBuilder::getClient),
                logger
        );
    }

    protected boolean isAuthPolicyExisted(
            @Nonnull final ResourceModel model,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) throws CfnGeneralServiceException {
        try {
            final var request = Translator.createGetAuthPolicyRequest(model);

            final var response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::getAuthPolicy);

            return !isAuthPolicyEmpty(response);
        } catch (ResourceNotFoundException e) {
            return false;
        } catch (Exception e) {
            throw new CfnGeneralServiceException(e);
        }
    }

    protected boolean isAuthPolicyEmpty(
            @Nonnull final GetAuthPolicyResponse response) {
        return response.state() == null && response.policy() == null;
    }
}
