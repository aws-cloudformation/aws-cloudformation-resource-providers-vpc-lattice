package software.amazon.vpclattice.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ArnHelperTest {
    public static final String PARTITION = "aws";

    public static final String REGION = "us-west-2";

    public static final String SERVICE_NAME = "vpc-lattice";

    public static final String AWS_ACCOUNT_ID = "123456789012";

    public static final ResourceHandlerRequest<?> REQUEST = ResourceHandlerRequest.builder()
            .awsPartition(PARTITION)
            .region(REGION)
            .awsAccountId(AWS_ACCOUNT_ID)
            .build();

    public static final String SERVICE_NETWORK_ID = "sn-12345678901234567";

    public static final String SERVICE_NETWORK_ARN = String.format(
            "arn:%s:%s:%s:%s:servicenetwork/%s",
            PARTITION,
            SERVICE_NAME,
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_NETWORK_ID
    );

    public static final String SERVICE_ID = "svc-12345678901234567";

    public static final String SERVICE_ARN = String.format(
            "arn:%s:%s:%s:%s:service/%s",
            PARTITION,
            SERVICE_NAME,
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_ID
    );

    public static final String TARGET_GROUP_ID = "tg-12345678901234567";

    public static final String TARGET_GROUP_ARN = String.format(
            "arn:%s:%s:%s:%s:targetgroup/%s",
            PARTITION,
            SERVICE_NAME,
            REGION,
            AWS_ACCOUNT_ID,
            TARGET_GROUP_ID
    );

    public static final String ACCESS_LOG_SUBSCRIPTION_ID = "als-12345678901234567";

    public static final String ACCESS_LOG_SUBSCRIPTION_ARN = String.format(
            "arn:%s:%s:%s:%s:accesslogsubscription/%s",
            PARTITION,
            SERVICE_NAME,
            REGION,
            AWS_ACCOUNT_ID,
            ACCESS_LOG_SUBSCRIPTION_ID
    );

    public static final String SERVICE_NETWORK_SERVICE_ASSOCIATION_ID = "snsa-12345678901234567";

    public static final String SERVICE_NETWORK_SERVICE_ASSOCIATION_ARN = String.format(
            "arn:%s:%s:%s:%s:servicenetworkserviceassociation/%s",
            PARTITION,
            SERVICE_NAME,
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_NETWORK_SERVICE_ASSOCIATION_ID
    );

    public static final String SERVICE_NETWORK_VPC_ASSOCIATION_ID = "snva-12345678901234567";

    public static final String SERVICE_NETWORK_VPC_ASSOCIATION_ARN = String.format(
            "arn:%s:%s:%s:%s:servicenetworkvpcassociation/%s",
            PARTITION,
            SERVICE_NAME,
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_NETWORK_VPC_ASSOCIATION_ID
    );

    public static final String LISTENER_ID = "listener-12345678901234567";

    public static final String LISTENER_ARN = String.format(
            "arn:%s:%s:%s:%s:service/%s/listener/%s",
            PARTITION,
            SERVICE_NAME,
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_ID,
            LISTENER_ID
    );

    public static final String RULE_ID = "rule-12345678901234567";

    public static final String RULE_ARN = String.format(
            "arn:%s:%s:%s:%s:service/%s/listener/%s/rule/%s",
            PARTITION,
            SERVICE_NAME,
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_ID,
            LISTENER_ID,
            RULE_ID
    );

    public static Stream<Arguments> provideArnGeneratorAndOriginalArn() {
        return Stream.of(
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) ArnHelper::getServiceNetworkArn, SERVICE_NETWORK_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) ArnHelper::getServiceArn, SERVICE_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) ArnHelper::getTargetGroupArn, TARGET_GROUP_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) ArnHelper::getAccessLogSubscriptionArn, ACCESS_LOG_SUBSCRIPTION_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) ArnHelper::getServiceNetworkServiceAssociationArn, SERVICE_NETWORK_SERVICE_ASSOCIATION_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) ArnHelper::getServiceNetworkVpcAssociationArn, SERVICE_NETWORK_VPC_ASSOCIATION_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) (request, arn) -> ArnHelper.getListenerArn(request, SERVICE_ID, arn), LISTENER_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) (request, arn) -> ArnHelper.getListenerArn(request, SERVICE_ARN, arn), LISTENER_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) (request, arn) -> ArnHelper.getRuleArn(request, SERVICE_ID, LISTENER_ID, arn), RULE_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) (request, arn) -> ArnHelper.getRuleArn(request, SERVICE_ID, LISTENER_ARN, arn), RULE_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) (request, arn) -> ArnHelper.getRuleArn(request, SERVICE_ARN, LISTENER_ID, arn), RULE_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) (request, arn) -> ArnHelper.getRuleArn(request, SERVICE_ARN, LISTENER_ARN, arn), RULE_ARN)
        );
    }

    public static Stream<Arguments> provideArnGeneratorAndResourceId() {
        return Stream.of(
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) ArnHelper::getServiceNetworkArn, SERVICE_NETWORK_ID, SERVICE_NETWORK_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) ArnHelper::getServiceArn, SERVICE_ID, SERVICE_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) ArnHelper::getTargetGroupArn, TARGET_GROUP_ID, TARGET_GROUP_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) ArnHelper::getAccessLogSubscriptionArn, ACCESS_LOG_SUBSCRIPTION_ID, ACCESS_LOG_SUBSCRIPTION_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) ArnHelper::getServiceNetworkServiceAssociationArn, SERVICE_NETWORK_SERVICE_ASSOCIATION_ID, SERVICE_NETWORK_SERVICE_ASSOCIATION_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) ArnHelper::getServiceNetworkVpcAssociationArn, SERVICE_NETWORK_VPC_ASSOCIATION_ID, SERVICE_NETWORK_VPC_ASSOCIATION_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) (request, arn) -> ArnHelper.getListenerArn(request, SERVICE_ID, arn), LISTENER_ID, LISTENER_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) (request, arn) -> ArnHelper.getListenerArn(request, SERVICE_ARN, arn), LISTENER_ID, LISTENER_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) (request, arn) -> ArnHelper.getRuleArn(request, SERVICE_ID, LISTENER_ID, arn), RULE_ID, RULE_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) (request, arn) -> ArnHelper.getRuleArn(request, SERVICE_ID, LISTENER_ARN, arn), RULE_ID, RULE_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) (request, arn) -> ArnHelper.getRuleArn(request, SERVICE_ARN, LISTENER_ID, arn), RULE_ID, RULE_ARN),
                Arguments.of((BiFunction<ResourceHandlerRequest<?>, String, String>) (request, arn) -> ArnHelper.getRuleArn(request, SERVICE_ARN, LISTENER_ARN, arn), RULE_ID, RULE_ARN)
        );
    }

    @ParameterizedTest
    @MethodSource("provideArnGeneratorAndOriginalArn")
    public void getResourceArn_ReturnOriginalArnWhenPassArn(
            @Nonnull final BiFunction<ResourceHandlerRequest<?>, String, String> arnGenerator,
            @Nonnull final String originalArn) {
        final var generatedArn = arnGenerator.apply(REQUEST, originalArn);

        assertThat(generatedArn).isEqualTo(originalArn);
    }

    @ParameterizedTest
    @MethodSource("provideArnGeneratorAndResourceId")
    public void getResourceArn_ReturnCorrectArnWhenPassId(
            @Nonnull final BiFunction<ResourceHandlerRequest<?>, String, String> arnGenerator,
            @Nonnull final String resourceId,
            @Nonnull final String expectedArn) {
        final var generatedArn = arnGenerator.apply(REQUEST, resourceId);

        assertThat(generatedArn).isEqualTo(expectedArn);
    }

    @Test
    public void getListenerArn_ThrowIllegalArgumentExceptionWhenServiceIdentifierIsNullAndListenerIdentifierIsNotArn() {
        assertThatThrownBy(() -> ArnHelper.getListenerArn(REQUEST, null, LISTENER_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void getRuleArn_ThrowIllegalArgumentExceptionWhenListenerIdentifierIsNullAndRuleIdentifierIsNotArn() {
        assertThatThrownBy(() -> ArnHelper.getRuleArn(REQUEST, null, null, RULE_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void getServiceIdFromListenerArn_ReturnCorrectServiceIdWhenPassValidListenerArn() {
        final var serviceId = ArnHelper.getServiceIdFromListenerArn(LISTENER_ARN);

        assertThat(serviceId).isEqualTo(SERVICE_ID);
    }

    @Test
    public void getListenerIdFromListenerArn_ReturnCorrectServiceIdWhenPassValidListenerArn() {
        final var listenerId = ArnHelper.getListenerIdFromListenerArn(LISTENER_ARN);

        assertThat(listenerId).isEqualTo(LISTENER_ID);
    }

    @Test
    public void getServiceIdFromRuleArn_ReturnCorrectServiceIdWhenPassValidRuleArn() {
        final var serviceId = ArnHelper.getServiceIdFromRuleArn(RULE_ARN);

        assertThat(serviceId).isEqualTo(SERVICE_ID);
    }

    @Test
    public void getListenerIdFromRuleArn_ReturnCorrectServiceIdWhenPassValidRuleArn() {
        final var listenerId = ArnHelper.getListenerIdFromRuleArn(RULE_ARN);

        assertThat(listenerId).isEqualTo(LISTENER_ID);
    }

    @Test
    public void getRuleIdFromRuleArn_ReturnCorrectRuleIdWhenPassValidRuleArn() {
        final var ruleId = ArnHelper.getRuleIdFromRuleArn(RULE_ARN);

        assertThat(ruleId).isEqualTo(RULE_ID);
    }
}
