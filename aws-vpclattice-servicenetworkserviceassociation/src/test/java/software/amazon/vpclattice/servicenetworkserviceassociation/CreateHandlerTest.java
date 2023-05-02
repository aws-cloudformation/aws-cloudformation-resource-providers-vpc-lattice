package software.amazon.vpclattice.servicenetworkserviceassociation;

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
import software.amazon.awssdk.services.vpclattice.model.CreateServiceNetworkServiceAssociationResponse;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ServiceNetworkServiceAssociationStatus;
import software.amazon.awssdk.services.vpclattice.model.ServiceQuotaExceededException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static software.amazon.vpclattice.servicenetworkserviceassociation.TagsTestUtils.MOCK_PROXY_CLIENT;

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

        mockSdkReturn(createServiceNetworkServiceAssociation(ServiceNetworkServiceAssociationStatus.CREATE_IN_PROGRESS));
        mockSdkReturn(getServiceNetworkServiceAssociation(ServiceNetworkServiceAssociationStatus.CREATE_IN_PROGRESS), getServiceNetworkServiceAssociation());
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.LIST_TAGS_RESPONSE);

        final var response = handler.handleRequest(proxy, request, null, logger);

        AssertionsForClassTypes.assertThat(response).isNotNull();
        AssertionsForClassTypes.assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        AssertionsForClassTypes.assertThat(response.getResourceModel()).isNotNull();
        AssertionsForClassTypes.assertThat(response.getResourceModel().getArn()).isEqualTo(SERVICE_NETWORK_SERVICE_ASSOCIATION_ARN);
    }

    @Test
    public void handleRequest_pollingStatusCreateFailed_throwNotStabilizedException() {
        final var request = getRequest();

        mockSdkReturn(createServiceNetworkServiceAssociation(ServiceNetworkServiceAssociationStatus.CREATE_IN_PROGRESS));
        mockSdkReturn(getServiceNetworkServiceAssociation(ServiceNetworkServiceAssociationStatus.CREATE_FAILED));

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getArn()).isEqualTo(SERVICE_NETWORK_SERVICE_ASSOCIATION_ARN);
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
                .serviceNetworkIdentifier(SERVICE_NETWORK_ID)
                .serviceIdentifier(SERVICE_ID)
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

    private static CreateServiceNetworkServiceAssociationResponse createServiceNetworkServiceAssociation(ServiceNetworkServiceAssociationStatus status) {
        return CreateServiceNetworkServiceAssociationResponse.builder()
                .id(SERVICE_NETWORK_SERVICE_ASSOCIATION_ID)
                .arn(SERVICE_NETWORK_SERVICE_ASSOCIATION_ARN)
                .status(status)
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
                Arguments.of(ConflictException.builder().message("already exists").build(), HandlerErrorCode.AlreadyExists)
        );
    }
}
