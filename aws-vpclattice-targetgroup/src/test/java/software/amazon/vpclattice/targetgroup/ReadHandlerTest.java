package software.amazon.vpclattice.targetgroup;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.TargetGroupStatus;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static software.amazon.vpclattice.targetgroup.TagsTestUtils.MOCK_PROXY_CLIENT;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends BaseTest {
    @BeforeEach
    public void setup() {
        proxy = spy(new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(1000).toMillis()));
        client = mock(VpcLatticeClient.class);
        proxyClient = MOCK_PROXY_CLIENT(proxy, client);
        handler = new ReadHandler();
        mockProxyClient();
    }

    @Test
    public void handleRequest_allApiCallsSuccess_successEvent() {
        final var request = getRequest();

        mockSdkReturn(getTargetGroup());
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.LIST_TAGS_RESPONSE);
        mockSdkReturn(getListTargetsResponse(3));

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getArn()).isEqualTo(TARGET_GROUP_ARN);
        assertThat(response.getResourceModel().getId()).isEqualTo(TARGET_GROUP_ID);
        assertThat(response.getResourceModel().getName()).isEqualTo(TARGET_GROUP_NAME);
        assertThat(response.getResourceModel().getType()).isEqualTo(TYPE);
        assertThat(response.getResourceModel().getConfig()).isEqualTo(MODEL_TARGET_GROUP_CONFIG);
        assertThat(response.getResourceModel().getStatus()).isEqualTo(TargetGroupStatus.ACTIVE.toString());
        assertThat(response.getResourceModel().getCreatedAt()).isNotNull();
        assertThat(response.getResourceModel().getLastUpdatedAt()).isNotNull();
        assertThat(response.getResourceModel().getTags()).isEqualTo(TagsTestUtils.MODEL_TAGS);
        assertThat(response.getResourceModel().getTargets()).hasSize(3);
    }

    @ParameterizedTest
    @MethodSource("provideExceptions")
    public void handleRequest_getCallThrowsException_throwEquivalentHandlerErrorCode(
            final Class<? extends Throwable> exception,
            final HandlerErrorCode errorCode) {
        final var request = getRequest();

        mockSdkGetThrow(exception);

        final var response = handler.handleRequest(proxy, request, null, logger);

        AssertionsForClassTypes.assertThat(response).isNotNull();
        AssertionsForClassTypes.assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        AssertionsForClassTypes.assertThat(response.getErrorCode()).isEqualTo(errorCode);
    }

    private ResourceModel getModel() {
        return ResourceModel.builder()
                .arn(TARGET_GROUP_ARN)
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> getRequest() {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getModel())
                .awsAccountId(AWS_ACCOUNT_ID)
                .region(REGION)
                .build();
    }
}