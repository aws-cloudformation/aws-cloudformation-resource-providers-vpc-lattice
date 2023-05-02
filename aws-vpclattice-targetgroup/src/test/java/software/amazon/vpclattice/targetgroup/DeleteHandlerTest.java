package software.amazon.vpclattice.targetgroup;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.DeleteTargetGroupResponse;
import software.amazon.awssdk.services.vpclattice.model.DeregisterTargetsResponse;
import software.amazon.awssdk.services.vpclattice.model.ListTargetsResponse;
import software.amazon.awssdk.services.vpclattice.model.ResourceNotFoundException;
import software.amazon.awssdk.services.vpclattice.model.TargetGroupStatus;
import software.amazon.awssdk.services.vpclattice.model.TargetStatus;
import software.amazon.awssdk.services.vpclattice.model.TargetSummary;
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
public class DeleteHandlerTest extends BaseTest {
    @BeforeEach
    public void setup() {
        proxy = spy(new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(1000).toMillis()));
        client = mock(VpcLatticeClient.class);
        proxyClient = MOCK_PROXY_CLIENT(proxy, client);
        handler = new DeleteHandler();
        mockProxyClient();
    }

    @Test
    public void handleRequest_allApiCallsSuccess_successEvent() {
        final var request = getRequest();

        mockSdkReturn(getListTargetsResponse(3), getListTargetsResponse(0));
        mockSdkReturn(DeregisterTargetsResponse.builder()
                .successful(getTargetSuccesses(3))
                .build());
        mockSdkReturn(DeleteTargetGroupResponse.builder().build());
        mockSdkGetThrow(ResourceNotFoundException.class);

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        assertThat(response.getResourceModel()).isNull();
    }

    @Test
    public void handleRequest_deleteStatusDeleteStatusFailed_throwNotStabilizedException() {
        final var request = getRequest();

        mockSdkReturn(getListTargetsResponse(0));
        mockSdkReturn(DeleteTargetGroupResponse.builder().build());
        mockSdkReturn(getTargetGroup(TargetGroupStatus.DELETE_FAILED));

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
        assertThat(response.getMessage()).isNotNull();
    }

    @Test
    public void handleRequest_deregisterTargetsUnsuccessful_throwGeneralServiceException() {
        final var request = getRequest();

        mockSdkReturn(getListTargetsResponse(3));
        mockSdkReturn(DeregisterTargetsResponse.builder()
                .unsuccessful(getTargetFailures(1))
                .build());

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
        assertThat(response.getMessage()).isNotNull();
    }

    @Test
    public void handleRequest_listTargetsDraining_waitFor5Minutes() {
        final var request = getRequest();

        mockSdkReturn(ListTargetsResponse.builder()
                .items(TargetSummary.builder().status(TargetStatus.DRAINING).build())
                .build());
//        mockSdkReturn(DeregisterTargetsResponse.builder()
//                .successful(getTargetSuccesses(3))
//                .build());

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(5 * 60);
    }

    @ParameterizedTest
    @MethodSource("provideExceptions")
    public void handleRequest_deleteCallThrowsException_throwEquivalentHandlerErrorCode(
            final Class<? extends Throwable> exception,
            final HandlerErrorCode errorCode) {
        final var request = getRequest();

        mockSdkReturn(getListTargetsResponse(0));
        mockSdkDeleteThrow(exception);

        final var response = handler.handleRequest(proxy, request, null, logger);

        AssertionsForClassTypes.assertThat(response).isNotNull();
        AssertionsForClassTypes.assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        AssertionsForClassTypes.assertThat(response.getErrorCode()).isEqualTo(errorCode);
    }

    private static ResourceModel getModel() {
        return ResourceModel.builder().arn(TARGET_GROUP_ARN).build();
    }

    private static ResourceHandlerRequest<ResourceModel> getRequest() {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(getModel())
                .awsAccountId(AWS_ACCOUNT_ID)
                .region(REGION)
                .build();
    }
}