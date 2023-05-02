package software.amazon.vpclattice.targetgroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.AccessDeniedException;
import software.amazon.awssdk.services.vpclattice.model.ConflictException;
import software.amazon.awssdk.services.vpclattice.model.CreateTargetGroupResponse;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.RegisterTargetsResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.vpclattice.model.TargetGroupStatus;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.awssdk.services.vpclattice.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static software.amazon.vpclattice.targetgroup.TagsTestUtils.MOCK_PROXY_CLIENT;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends BaseTest {
    @BeforeEach
    public void setup() {
        proxy = spy(new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(1000).toMillis()));
        client = mock(VpcLatticeClient.class);
        proxyClient = MOCK_PROXY_CLIENT(proxy, client);
        handler = new CreateHandler();
        mockProxyClient();
    }

    @Test
    public void handleRequest_allApiCallsSuccess_successEvent() {
        final var request = getRequest(getResourceModelWithTargets());

        mockSdkReturn(createTargetGroup(TargetGroupStatus.CREATE_IN_PROGRESS));
        mockSdkReturn(getTargetGroup(TargetGroupStatus.CREATE_IN_PROGRESS), getTargetGroup(), getTargetGroup());
        mockSdkReturn(RegisterTargetsResponse.builder().successful(getTargetSuccesses(3)).build());
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.LIST_TAGS_RESPONSE);
        mockSdkReturn(getListTargetsResponse(3));

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getArn()).isEqualTo(TARGET_GROUP_ARN);
    }

    @Test
    public void handleRequest_pollingStatusCreateFailed_throwNotStabilizedException() {
        final var request = getRequest();

        mockSdkReturn(createTargetGroup(TargetGroupStatus.CREATE_IN_PROGRESS));
        mockSdkReturn(getTargetGroup(TargetGroupStatus.CREATE_FAILED));

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getArn()).isEqualTo(TARGET_GROUP_ARN);
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
    }

    @ParameterizedTest
    @MethodSource("provideCreateExceptions")
    public void handleRequest_createCallThrowsException_throwEquivalentHandlerErrorCode(
            final Exception exception,
            final HandlerErrorCode errorCode) {
        final var request = getRequest();

        mockSdkCreateThrow(exception);
        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
    }

    @Test
    public void handleRequest_registerTargetsUnsuccessful_throwNotStabilizedException() {
        final var request = getRequest(getResourceModelWithTargets());

        mockSdkReturn(createTargetGroup(TargetGroupStatus.CREATE_IN_PROGRESS));
        mockSdkReturn(getTargetGroup(TargetGroupStatus.CREATE_IN_PROGRESS), getTargetGroup(), getTargetGroup());
        mockSdkReturn(RegisterTargetsResponse.builder().unsuccessful(getTargetFailures(3)).build());

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getArn()).isEqualTo(TARGET_GROUP_ARN);
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
    }

    private static ResourceModel getResourceModel() {
        return ResourceModel.builder()
                .name(TARGET_GROUP_NAME)
                .type(TYPE)
                .config(MODEL_TARGET_GROUP_CONFIG)
                .targets(List.of())
                .build();
    }

    private static ResourceModel getResourceModelWithTargets() {
        return ResourceModel.builder()
                .name(TARGET_GROUP_NAME)
                .type(TYPE)
                .config(MODEL_TARGET_GROUP_CONFIG)
                .targets(getTargetsModel(3))
                .build();
    }

    private static ResourceHandlerRequest<ResourceModel> getRequest() {
        return getRequest(getResourceModel());
    }

    private static ResourceHandlerRequest<ResourceModel> getRequest(
            @Nonnull final ResourceModel model) {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .awsAccountId(AWS_ACCOUNT_ID)
                .region(REGION)
                .build();
    }

    private static CreateTargetGroupResponse createTargetGroup(TargetGroupStatus status) {
        return CreateTargetGroupResponse.builder()
                .id(TARGET_GROUP_ID)
                .arn(TARGET_GROUP_ARN)
                .status(status)
                .type(TYPE)
                .name(TARGET_GROUP_NAME)
                .config(getMercuryTargetGroupConfig())
                .build();
    }

    public static Stream<Arguments> provideCreateExceptions() {
        return Stream.of(
                Arguments.of(ValidationException.builder().build(), HandlerErrorCode.InvalidRequest),
                Arguments.of(ConflictException.builder().message("").build(), HandlerErrorCode.InvalidRequest),
                Arguments.of(AccessDeniedException.builder().build(), HandlerErrorCode.AccessDenied),
                Arguments.of(InternalServerException.builder().build(), HandlerErrorCode.ServiceInternalError),
                Arguments.of(ServiceQuotaExceededException.builder().build(), HandlerErrorCode.ServiceLimitExceeded),
                Arguments.of(ThrottlingException.builder().build(), HandlerErrorCode.Throttling),
                Arguments.of(new IllegalStateException(""), HandlerErrorCode.InternalFailure),
                Arguments.of(ConflictException.builder().message("Resource of type TargetGroup with name targetgroup-name already exists in 123456").build(), HandlerErrorCode.AlreadyExists)
        );
    }
}