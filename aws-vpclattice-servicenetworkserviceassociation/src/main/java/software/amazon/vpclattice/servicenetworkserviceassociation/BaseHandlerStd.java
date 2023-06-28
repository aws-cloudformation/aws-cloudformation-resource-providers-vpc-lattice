package software.amazon.vpclattice.servicenetworkserviceassociation;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ResourceNotFoundException;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;
import software.amazon.vpclattice.common.ClientBuilder;
import software.amazon.vpclattice.common.LatticeNotStabilizedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Optional;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    protected static final Constant BACKOFF_STRATEGY = Constant.of()
            .timeout(Duration.ofMinutes(120L))
            .delay(Duration.ofSeconds(5L))
            .build();

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

    protected final Boolean isServiceNetworkServiceAssociationActive(
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final ResourceModel model) {
        try {
            final var request = Translator.createGetServiceNetworkServiceAssociationRequest(model);

            final var serviceNetworkVpcAssociation = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::getServiceNetworkServiceAssociation);

            switch (serviceNetworkVpcAssociation.status()) {
                case ACTIVE:
                    return true;
                case CREATE_IN_PROGRESS:
                    return false;
                case CREATE_FAILED:
                default:
                    throw new LatticeNotStabilizedException(serviceNetworkVpcAssociation.failureMessage());
            }
        } catch (InternalServerException | ResourceNotFoundException |
                 ThrottlingException e) {
            return false;
        }
    }

    protected final Boolean isServiceNetworkServiceAssociationDeleted(
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final ResourceModel model) {
        try {
            final var request = Translator.createGetServiceNetworkServiceAssociationRequest(model);

            final var serviceNetworkVpcAssociation = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::getServiceNetworkServiceAssociation);

            switch (serviceNetworkVpcAssociation.status()) {
                case DELETE_FAILED:
                    throw new LatticeNotStabilizedException(serviceNetworkVpcAssociation.failureMessage());
                case DELETE_IN_PROGRESS:
                default:
                    return false;
            }
        } catch (ResourceNotFoundException e) {
            return true;
        } catch (InternalServerException | ThrottlingException e) {
            return false;
        }
    }
}
