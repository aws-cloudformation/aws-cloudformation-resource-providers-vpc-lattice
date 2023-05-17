package software.amazon.vpclattice.accesslogsubscription;

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
import software.amazon.awssdk.services.vpclattice.model.ConflictException;
import software.amazon.awssdk.services.vpclattice.model.CreateAccessLogSubscriptionResponse;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.awssdk.services.vpclattice.model.ValidationException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static software.amazon.vpclattice.accesslogsubscription.TagsTestUtils.MOCK_PROXY_CLIENT;


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

        mockSdkReturn(createAccessLogSubscription());

        final var response1 =
                handler.handleRequest(proxy, request, null, logger);

        mockSdkReturn(getAccessLogSubscription());
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.LIST_TAGS_RESPONSE);

        final var response2 =
                handler.handleRequest(proxy, request, response1.getCallbackContext(), logger);

        AssertionsForClassTypes.assertThat(response2).isNotNull();
        AssertionsForClassTypes.assertThat(response2.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        AssertionsForClassTypes.assertThat(response2.getResourceModel()).isNotNull();
        AssertionsForClassTypes.assertThat(response2.getResourceModel().getArn()).isEqualTo(ALS_ARN);
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

    @ParameterizedTest
    @MethodSource("provideExceptions")
    public void handleRequest_getCallThrowsException_throwEquivalentHandlerErrorCode(
            @Nonnull final Class<? extends Throwable> exception,
            @Nonnull final HandlerErrorCode cloudFormationErrorCode) {
        final var request = getRequest();

        mockSdkReturn(createAccessLogSubscription());

        final var response1 =
                handler.handleRequest(proxy, request, null, logger);

        mockSdkGetThrow(exception);

        final var response2 =
                handler.handleRequest(proxy, request, response1.getCallbackContext(), logger);

        assertThat(response2).isNotNull();
        assertThat(response2.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response2.getErrorCode()).isEqualTo(cloudFormationErrorCode);
        assertThat(response2.getResourceModel()).isNotNull();
        assertThat(response2.getResourceModel().getArn()).isEqualTo(ALS_ARN);
    }

    private ResourceModel getModel() {
        return ResourceModel.builder()
                .destinationArn(DESTINATION_ARN)
                .resourceIdentifier(RESOURCE_ARN)
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

    private static CreateAccessLogSubscriptionResponse createAccessLogSubscription() {
        return CreateAccessLogSubscriptionResponse.builder()
                .id(ALS_ID)
                .arn(ALS_ARN)
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
