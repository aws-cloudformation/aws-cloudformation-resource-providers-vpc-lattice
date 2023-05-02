package software.amazon.vpclattice.targetgroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.RegisterTargetsResponse;
import software.amazon.awssdk.services.vpclattice.model.TargetGroupStatus;
import software.amazon.awssdk.services.vpclattice.model.UpdateTargetGroupResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static software.amazon.vpclattice.targetgroup.TagsTestUtils.MOCK_PROXY_CLIENT;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends BaseTest {
    public static final int NEW_PORT = 10001;

    @BeforeEach
    public void setup() {
        proxy = spy(new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(1000).toMillis()));
        client = mock(VpcLatticeClient.class);
        proxyClient = MOCK_PROXY_CLIENT(proxy, client);
        handler = new UpdateHandler();
        mockProxyClient();
    }

    @Test
    public void handleRequest_allApiCallsSuccess_successEvent() {
        final var request = getRequest(getPreviousModel(), getDesiredModel())
                .toBuilder()
                .desiredResourceTags(Map.of("key", "bar"))
                .build();

        mockSdkReturn(UpdateTargetGroupResponse.builder().build());
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.LIST_TAGS_RESPONSE);
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.TAG_RESOURCE_RESPONSE);
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.UNTAG_RESOURCE_RESPONSE);
        mockSdkReturn(RegisterTargetsResponse.builder().build());
        mockSdkReturn(getListTargetsResponse(5));
        mockSdkReturn(getTargetGroup());

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getArn()).isEqualTo(TARGET_GROUP_ARN);
    }

    @Test
    public void handleRequest_sameModel_successEvent() {
        final var request = getRequest(getPreviousModel(), getPreviousModel());

        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.EMPTY_LIST_TAGS_RESPONSE);
        mockSdkReturn(getListTargetsResponse(5));
        mockSdkReturn(getTargetGroup());

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getArn()).isEqualTo(TARGET_GROUP_ARN);
    }

    @ParameterizedTest
    @MethodSource("provideExceptions")
    public void handleRequest_updateCallThrowsException_throwEquivalentHandlerErrorCode(
            final Class<? extends Throwable> exception,
            final HandlerErrorCode errorCode) {
        final var request = getRequest(getPreviousModel(), getDesiredModel());

        mockSdkUpdateThrow(exception);
        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
    }

    private static ResourceModel getPreviousModel() {
        return ResourceModel.builder()
                .id(TARGET_GROUP_ID)
                .arn(TARGET_GROUP_ARN)
                .createdAt(new Date().toString())
                .name(TARGET_GROUP_NAME)
                .type(TYPE)
                .config(MODEL_TARGET_GROUP_CONFIG)
                .createdAt(new Date().toString())
                .lastUpdatedAt(new Date().toString())
                .status(TargetGroupStatus.ACTIVE.toString())
                .targets(getTargetsModel(2))
                .build();
    }

    private static ResourceModel getDesiredModel() {
        var healthCheckConfig = HealthCheckConfig.builder()
                .enabled(ENABLED)
                .protocol(PROTOCOL)
                .port(NEW_PORT)
                .path(PATH)
                .healthCheckIntervalSeconds(HEALTH_CHECK_INTERVAL_SECONDS)
                .healthCheckTimeoutSeconds(HEALTH_CHECK_TIMEOUT_SECONDS)
                .build();

        var config = TargetGroupConfig.builder()
                .port(PORT)
                .protocol(PROTOCOL)
                .protocolVersion(PROTOCOL_VERSION)
                .vpcIdentifier(VPC_IDENTIFIER)
                .healthCheck(healthCheckConfig)
                .build();

        var model = getPreviousModel();
        model.setConfig(config);

        model.setTargets(getTargetsModel(5));

        return model;
    }

    private static ResourceHandlerRequest<ResourceModel> getRequest(ResourceModel previous, ResourceModel desired) {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(previous)
                .desiredResourceState(desired)
                .awsAccountId(AWS_ACCOUNT_ID)
                .region(REGION)
                .build();
    }
}
