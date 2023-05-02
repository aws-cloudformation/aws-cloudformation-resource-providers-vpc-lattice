package software.amazon.vpclattice.targetgroup;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ResourceNotFoundException;
import software.amazon.awssdk.services.vpclattice.model.TargetFailure;
import software.amazon.awssdk.services.vpclattice.model.TargetGroupType;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ClientBuilder;
import software.amazon.vpclattice.common.LatticeNotStabilizedException;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    protected Logger logger;

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
        this.logger = logger;

        return this.handleRequest(
                proxy,
                request,
                Optional.ofNullable(callbackContext).orElse(CallbackContext.builder().build()),
                proxy.newProxy(ClientBuilder::getClient),
                logger
        );
    }

    protected Boolean isTargetGroupActive(
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final ResourceModel model) {
        try {
            final var request = Translator.createGetTargetGroupRequest(model);

            final var targetGroup = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::getTargetGroup);

            switch (targetGroup.status()) {
                case ACTIVE:
                    return true;
                case CREATE_IN_PROGRESS:
                    return false;
                case CREATE_FAILED:
                default:
                    throw new LatticeNotStabilizedException(targetGroup.failureMessage());
            }

        } catch (InternalServerException | ThrottlingException e) {
            return false;
        }
    }

    protected Boolean isTargetGroupDeleted(
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final ResourceModel model) {
        try {
            final var request = Translator.createGetTargetGroupRequest(model);

            final var targetGroup = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::getTargetGroup);

            switch (targetGroup.status()) {
                case DELETE_IN_PROGRESS:
                    return false;
                case DELETE_FAILED:
                default:
                    throw new LatticeNotStabilizedException(targetGroup.failureMessage());
            }
        } catch (ResourceNotFoundException e) {
            return true;
        } catch (InternalServerException | ThrottlingException e) {
            return false;
        }
    }

    protected final void validateTargetGroupCanRegisterTargets(
            @Nonnull final ResourceModel model) {
        if (TargetGroupType.LAMBDA.toString().equals(model.getType())
                || TargetGroupType.ALB.toString().equals(model.getType())) {
            if (model.getTargets() == null || model.getTargets().size() <= 1) {
                return;
            }

            throw new CfnInvalidRequestException(
                    String.format("TargetGroup of type %s can't more than 1 Target registered", model.getType()));
        }
    }

    protected final String getFailedRegisterTargetErrorMessage(
            @Nonnull final List<TargetFailure> targetFailures) {
        return String.format("Failed to register target %s",
                targetFailures.stream()
                        .findFirst()
                        .map((targetFailure) -> targetFailure.port() != null ?
                                String.format("%s:%s with message: %s", targetFailure.id(), targetFailure.port(), targetFailure.failureMessage())
                                : String.format("%s with message: %s", targetFailure.id(), targetFailure.failureMessage()))
                        .orElseThrow(CfnInternalFailureException::new)
        );
    }

    protected final String getFailedDeregisterTargetsErrorMessage(
            @Nonnull final List<TargetFailure> targetFailures) {
        return String.format("Failed to deregister target %s",
                targetFailures.stream()
                        .findFirst()
                        .map((targetFailure) -> targetFailure.port() != null ?
                                String.format("%s:%s with message: %s", targetFailure.id(), targetFailure.port(), targetFailure.failureMessage())
                                : String.format("%s with message: %s", targetFailure.id(), targetFailure.failureMessage()))
                        .orElseThrow(CfnInternalFailureException::new)
        );
    }
}