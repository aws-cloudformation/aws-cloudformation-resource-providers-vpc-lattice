package software.amazon.vpclattice.servicenetworkvpcassociation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.DeleteServiceNetworkVpcAssociationResponse;
import software.amazon.awssdk.services.vpclattice.model.ResourceNotFoundException;
import software.amazon.awssdk.services.vpclattice.model.ServiceNetworkVpcAssociationStatus;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static software.amazon.vpclattice.servicenetworkvpcassociation.TagsTestUtils.MOCK_PROXY_CLIENT;

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

        mockSdkReturn(DeleteServiceNetworkVpcAssociationResponse.builder().build());
        mockSdkGetThrow(ResourceNotFoundException.class);

        final var response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        assertThat(response.getResourceModel()).isNull();
    }

    @Test
    public void handleRequest_deleteStatusDeleteStatusFailed_throwNotStabilizedException() {
        final var request = getRequest();
        mockSdkReturn(DeleteServiceNetworkVpcAssociationResponse.builder().build());
        mockSdkReturn(getServiceNetworkVpcAssociation(ServiceNetworkVpcAssociationStatus.DELETE_FAILED));

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotStabilized);
        assertThat(response.getMessage()).isNotNull();
    }

    @ParameterizedTest
    @MethodSource("provideExceptions")
    public void handleRequest_deleteCallThrowsException_throwEquivalentHandlerErrorCode(
            final Class<? extends Throwable> exception,
            final HandlerErrorCode errorCode) {
        final var request = getRequest();

        mockSdkDeleteThrow(exception);

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
    }

    private ResourceModel getModel() {
        return ResourceModel.builder()
                .arn(SERVICE_NETWORK_VPC_ASSOCIATION_ARN)
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> getRequest() {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .awsAccountId(AWS_ACCOUNT_ID)
                .region(REGION)
                .desiredResourceState(getModel())
                .build();
    }
}
