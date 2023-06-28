package software.amazon.vpclattice.service;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ResourceNotFoundException;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ClientBuilder;
import software.amazon.vpclattice.common.LatticeNotStabilizedException;

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

    protected final Boolean isServiceActive(
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final ResourceModel model) {
        try {
            final var request = Translator.createGetServiceRequest(model);

            final var service = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::getService);

            switch (service.status()) {
                case ACTIVE:
                    return true;
                case CREATE_IN_PROGRESS:
                    return false;
                case CREATE_FAILED:
                default:
                    throw new LatticeNotStabilizedException(service.failureMessage());
            }
        } catch (InternalServerException | ThrottlingException |
                 ResourceNotFoundException e) {
            return false;
        }
    }

    protected final Boolean isServiceDeleted(
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final ResourceModel model) {
        try {
            final var request = Translator.createGetServiceRequest(model);

            final var service = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::getService);

            switch (service.status()) {
                case DELETE_IN_PROGRESS:
                    return false;
                case DELETE_FAILED:
                default:
                    throw new LatticeNotStabilizedException(service.failureMessage());
            }
        } catch (ResourceNotFoundException e) {
            return true;
        } catch (InternalServerException | ThrottlingException e) {
            return false;
        }
    }
}
