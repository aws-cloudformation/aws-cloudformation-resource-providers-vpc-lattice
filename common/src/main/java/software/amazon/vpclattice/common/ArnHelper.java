package software.amazon.vpclattice.common;

import com.amazonaws.arn.Arn;
import com.google.common.base.Splitter;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ArnHelper {
    private static final String SERVICE_NETWORK_ARN_FORMAT = "arn:%s:vpc-lattice:%s:%s:servicenetwork/%s";

    private static final String SERVICE_ARN_FORMAT = "arn:%s:vpc-lattice:%s:%s:service/%s";

    private static final String TARGET_GROUP_ARN_FORMAT = "arn:%s:vpc-lattice:%s:%s:targetgroup/%s";

    private static final String ACCESS_LOG_SUBSCRIPTION_ARN_FORMAT = "arn:%s:vpc-lattice:%s:%s:accesslogsubscription/%s";

    private static final String SERVICE_NETWORK_SERVICE_ASSOCIATION_ARN_FORMAT = "arn:%s:vpc-lattice:%s:%s:servicenetworkserviceassociation/%s";

    private static final String SERVICE_NETWORK_VPC_ASSOCIATION_ARN_FORMAT = "arn:%s:vpc-lattice:%s:%s:servicenetworkvpcassociation/%s";

    private ArnHelper() {
    }

    public static String getServiceNetworkArn(
            @Nonnull final ResourceHandlerRequest<?> request,
            @Nonnull final String serviceNetworkIdentifier) {
        try {
            return Arn.fromString(serviceNetworkIdentifier).toString();
        } catch (Exception e) {
            return String.format(
                    SERVICE_NETWORK_ARN_FORMAT,
                    request.getAwsPartition(),
                    request.getRegion(),
                    request.getAwsAccountId(),
                    serviceNetworkIdentifier);
        }
    }

    public static String getServiceArn(
            @Nonnull final ResourceHandlerRequest<?> request,
            @Nonnull final String serviceIdentifier) {
        try {
            return Arn.fromString(serviceIdentifier).toString();
        } catch (Exception e) {
            return String.format(
                    SERVICE_ARN_FORMAT,
                    request.getAwsPartition(),
                    request.getRegion(),
                    request.getAwsAccountId(),
                    serviceIdentifier);
        }
    }

    public static String getTargetGroupArn(
            @Nonnull final ResourceHandlerRequest<?> request,
            @Nonnull final String targetGroupIdentifier) {
        try {
            return Arn.fromString(targetGroupIdentifier).toString();
        } catch (Exception e) {
            return String.format(
                    TARGET_GROUP_ARN_FORMAT,
                    request.getAwsPartition(),
                    request.getRegion(),
                    request.getAwsAccountId(),
                    targetGroupIdentifier);
        }
    }

    public static String getAccessLogSubscriptionArn(
            @Nonnull final ResourceHandlerRequest<?> request,
            @Nonnull final String accessLogSubscriptionIdentifier) {
        try {
            return Arn.fromString(accessLogSubscriptionIdentifier).toString();
        } catch (Exception e) {
            return String.format(
                    ACCESS_LOG_SUBSCRIPTION_ARN_FORMAT,
                    request.getAwsPartition(),
                    request.getRegion(),
                    request.getAwsAccountId(),
                    accessLogSubscriptionIdentifier);
        }
    }

    public static String getServiceNetworkServiceAssociationArn(
            @Nonnull final ResourceHandlerRequest<?> request,
            @Nonnull final String serviceNetworkServiceAssociationIdentifier) {
        try {
            return Arn.fromString(serviceNetworkServiceAssociationIdentifier).toString();
        } catch (Exception e) {
            return String.format(
                    SERVICE_NETWORK_SERVICE_ASSOCIATION_ARN_FORMAT,
                    request.getAwsPartition(),
                    request.getRegion(),
                    request.getAwsAccountId(),
                    serviceNetworkServiceAssociationIdentifier);
        }
    }

    public static String getServiceNetworkVpcAssociationArn(
            @Nonnull final ResourceHandlerRequest<?> request,
            @Nonnull final String serviceNetworkVpcAssociationIdentifier) {
        try {
            return Arn.fromString(serviceNetworkVpcAssociationIdentifier).toString();
        } catch (Exception e) {
            return String.format(
                    SERVICE_NETWORK_VPC_ASSOCIATION_ARN_FORMAT,
                    request.getAwsPartition(),
                    request.getRegion(),
                    request.getAwsAccountId(),
                    serviceNetworkVpcAssociationIdentifier);
        }
    }

    public static String getListenerArn(
            @Nonnull final ResourceHandlerRequest<?> request,
            @Nullable final String serviceIdentifier,
            @Nonnull final String listenerIdentifier) {
        try {
            return Arn.fromString(listenerIdentifier).toString();
        } catch (Exception e) {
            if (serviceIdentifier == null) {
                throw new IllegalArgumentException("ServiceIdentifier can't be null when listenerIdentifier is not a listener arn " + listenerIdentifier);
            }

            final var serviceArn = getServiceArn(request, serviceIdentifier);

            return String.format("%s/listener/%s", serviceArn, listenerIdentifier);
        }
    }

    public static String getRuleArn(
            @Nonnull final ResourceHandlerRequest<?> request,
            @Nullable final String serviceIdentifier,
            @Nullable final String listenerIdentifier,
            @Nonnull final String ruleIdentifier) {
        try {
            return Arn.fromString(ruleIdentifier).toString();
        } catch (Exception e) {
            if (listenerIdentifier == null) {
                throw new IllegalArgumentException("ListenerIdentifier can't be null when ruleIdentifier is not a rule arn " + ruleIdentifier);
            }

            final var listenerArn = getListenerArn(request,
                    serviceIdentifier,
                    listenerIdentifier);

            return String.format("%s/rule/%s", listenerArn, ruleIdentifier);
        }
    }

    public static String getServiceIdFromServiceArn(@Nonnull final String serviceArn) {
        final var arn = Arn.fromString(serviceArn);

        return arn.getResource().getResource();
    }

    public static String getServiceIdFromListenerArn(@Nonnull final String listenerArn)
            throws IllegalArgumentException {
        final var arn = Arn.fromString(listenerArn);

        final var resource = arn.getResourceAsString();

        // ["service", serviceId, "listener", listenerId]
        final var parts = Splitter.on('/')
                .omitEmptyStrings()
                .splitToList(resource);

        if (parts.size() != 4) {
            throw new IllegalArgumentException(listenerArn + " is not a valid listener arn");
        }

        return parts.get(1);
    }

    public static String getListenerIdFromListenerArn(@Nonnull final String listenerArn) {
        final var arn = Arn.fromString(listenerArn);

        final var resource = arn.getResourceAsString();

        // ["service", serviceId, "listener", listenerId]
        final var parts = Splitter.on('/')
                .omitEmptyStrings()
                .splitToList(resource);

        if (parts.size() != 4) {
            throw new IllegalArgumentException(listenerArn + " is not a valid listener arn");
        }

        return parts.get(3);
    }

    public static String getServiceIdFromRuleArn(@Nonnull final String ruleArn) {
        final var arn = Arn.fromString(ruleArn);

        final var resource = arn.getResourceAsString();

        // ["service", serviceId, "listener", listenerId, "rule", ruleId]
        final var parts = Splitter.on('/')
                .omitEmptyStrings()
                .splitToList(resource);

        if (parts.size() != 6) {
            throw new IllegalArgumentException(ruleArn + " is not a valid rule arn");
        }

        return parts.get(1);
    }

    public static String getListenerIdFromRuleArn(@Nonnull final String ruleArn) {
        final var arn = Arn.fromString(ruleArn);

        final var resource = arn.getResourceAsString();

        // ["service", serviceId, "listener", listenerId, "rule", ruleId]
        final var parts = Splitter.on('/')
                .omitEmptyStrings()
                .splitToList(resource);

        if (parts.size() != 6) {
            throw new IllegalArgumentException(ruleArn + " is not a valid rule arn");
        }

        return parts.get(3);
    }

    public static String getRuleIdFromRuleArn(@Nonnull final String ruleArn) {
        final var arn = Arn.fromString(ruleArn);

        final var resource = arn.getResourceAsString();

        // ["service", serviceId, "listener", listenerId, "rule", ruleId]
        final var parts = Splitter.on('/')
                .omitEmptyStrings()
                .splitToList(resource);

        if (parts.size() != 6) {
            throw new IllegalArgumentException(ruleArn + " is not a valid rule arn");
        }

        return parts.get(5);
    }
}
