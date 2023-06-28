package software.amazon.vpclattice.servicenetworkvpcassociation;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ResourceNotFoundException;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;
import software.amazon.vpclattice.common.ClientBuilder;
import software.amazon.vpclattice.common.ExceptionHandler;
import software.amazon.vpclattice.common.LatticeNotStabilizedException;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Function;

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
            @Nonnull final Logger logger);

    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        return this.handleRequest(
                proxy,
                request,
                Optional.ofNullable(callbackContext).orElse(new CallbackContext()),
                proxy.newProxy(ClientBuilder::getClient),
                logger
        );
    }

    protected ProgressEvent<ResourceModel, CallbackContext> waitServiceNetworkVpcAssociationActive(
            @Nonnull final ProgressEvent<ResourceModel, CallbackContext> progress,
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) {
        return proxy.initiate(
                        "AWS::VpcLattice::ServiceNetworkVpcAssociation::WaitForServiceNetworkVpcAssociationActive",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext()
                )
                .translateToServiceRequest(Function.identity())
                .backoffDelay(BACKOFF_STRATEGY)
                .makeServiceCall((model, _client) -> null)
                .stabilize((_request, _response, client, model, _context) ->
                        this.isServiceNetworkVpcAssociationActive(client, model))
                .handleError(ExceptionHandler::handleError)
                .progress();
    }

    protected final Boolean isServiceNetworkVpcAssociationActive(
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final ResourceModel model) {
        try {
            final var request = Translator.createGetServiceNetworkVpcAssociationRequest(model);

            final var serviceNetworkVpcAssociation = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::getServiceNetworkVpcAssociation);

            switch (serviceNetworkVpcAssociation.status()) {
                case ACTIVE:
                    return true;
                case CREATE_IN_PROGRESS:
                case UPDATE_IN_PROGRESS:
                    return false;
                case CREATE_FAILED:
                case UPDATE_FAILED:
                default:
                    throw new LatticeNotStabilizedException(serviceNetworkVpcAssociation.failureMessage());
            }
        } catch (InternalServerException | ThrottlingException |
                 ResourceNotFoundException e) {
            return false;
        }
    }

    protected final Boolean isServiceNetworkVpcAssociationDeleted(
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final ResourceModel model) {
        try {
            final var request = Translator.createGetServiceNetworkVpcAssociationRequest(model);

            final var serviceNetworkVpcAssociation = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::getServiceNetworkVpcAssociation);

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
