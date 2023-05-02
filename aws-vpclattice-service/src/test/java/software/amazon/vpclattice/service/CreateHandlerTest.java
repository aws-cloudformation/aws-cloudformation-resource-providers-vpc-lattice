package software.amazon.vpclattice.service;

import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.AccessDeniedException;
import software.amazon.awssdk.services.vpclattice.model.AuthType;
import software.amazon.awssdk.services.vpclattice.model.ConflictException;
import software.amazon.awssdk.services.vpclattice.model.CreateServiceResponse;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.vpclattice.model.ServiceStatus;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.awssdk.services.vpclattice.model.ValidationException;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static software.amazon.vpclattice.service.TagsTestUtils.MOCK_PROXY_CLIENT;

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
        final var request = getRequest();

        mockSdkReturn(createService(ServiceStatus.CREATE_IN_PROGRESS));
        mockSdkReturn(getService(ServiceStatus.CREATE_IN_PROGRESS), getService());
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.LIST_TAGS_RESPONSE);

        final var response = handler.handleRequest(proxy, request, null, logger);

        AssertionsForClassTypes.assertThat(response).isNotNull();
        AssertionsForClassTypes.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        AssertionsForClassTypes.assertThat(response.getResourceModel()).isNotNull();
        AssertionsForClassTypes.assertThat(response.getResourceModel().getArn()).isEqualTo(SERVICE_ARN);
    }

    @Test
    public void handleRequest_pollingStatusCreateFailed_throwNotStabilizedException() {
        final var request = getRequest();

        mockSdkReturn(createService(ServiceStatus.CREATE_IN_PROGRESS));
        mockSdkReturn(getService(ServiceStatus.CREATE_FAILED));

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getArn()).isEqualTo(SERVICE_ARN);
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
    }

    @ParameterizedTest
    @MethodSource("provideCreateExceptions")
    public void handleRequest_createCallThrowsException_throwEquivalentHandlerErrorCode(
            @Nonnull final Throwable exception,
            @Nonnull final HandlerErrorCode cloudFormationErrorCode) {
        final var request = getRequest();

        mockSdkCreateThrow(exception);

        final var response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(cloudFormationErrorCode);
    }

    private ResourceModel getModel() {
        return ResourceModel.builder()
                .name(SERVICE_NAME)
                .authType(AuthType.NONE.name())
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> getRequest() {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .awsAccountId(AWS_ACCOUNT_ID)
                .region(REGION)
                .desiredResourceTags(TagsTestUtils.TAGS)
                .desiredResourceState(getModel())
                .build();
    }

    private static CreateServiceResponse createService(ServiceStatus status) {
        return CreateServiceResponse.builder()
                .id(SERVICE_ID)
                .arn(SERVICE_ARN)
                .status(status.name())
                .build();
    }

    public static Stream<Arguments> provideCreateExceptions() {
        return Stream.of(
                Arguments.of(ValidationException.builder().build(), HandlerErrorCode.InvalidRequest),
                Arguments.of(software.amazon.awssdk.services.vpclattice.model.ConflictException.builder().message("").build(), HandlerErrorCode.InvalidRequest),
                Arguments.of(AccessDeniedException.builder().build(), HandlerErrorCode.AccessDenied),
                Arguments.of(InternalServerException.builder().build(), HandlerErrorCode.ServiceInternalError),
                Arguments.of(ServiceQuotaExceededException.builder().build(), HandlerErrorCode.ServiceLimitExceeded),
                Arguments.of(ThrottlingException.builder().build(), HandlerErrorCode.Throttling),
                Arguments.of(new IllegalStateException(""), HandlerErrorCode.InternalFailure),
                Arguments.of(ConflictException.builder().message("already exists").build(), HandlerErrorCode.AlreadyExists)
        );
    }
}
