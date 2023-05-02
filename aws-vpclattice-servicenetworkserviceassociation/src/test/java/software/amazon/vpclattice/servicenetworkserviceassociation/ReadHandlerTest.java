package software.amazon.vpclattice.servicenetworkserviceassociation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.ServiceNetworkServiceAssociationStatus;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nonnull;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static software.amazon.vpclattice.servicenetworkserviceassociation.TagsTestUtils.MOCK_PROXY_CLIENT;

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

        mockSdkReturn(getServiceNetworkServiceAssociation());
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.LIST_TAGS_RESPONSE);

        final var response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        final var model = response.getResourceModel();

        assertThat(model.getServiceNetworkIdentifier()).isNull();

        assertThat(model.getId()).isEqualTo(SERVICE_NETWORK_SERVICE_ASSOCIATION_ID);
        assertThat(model.getArn()).isEqualTo(SERVICE_NETWORK_SERVICE_ASSOCIATION_ARN);
        assertThat(model.getCreatedAt()).isNotNull();
        assertThat(model.getStatus())
                .isEqualTo(ServiceNetworkServiceAssociationStatus.ACTIVE.toString());

        assertThat(model.getServiceNetworkId()).isEqualTo(SERVICE_NETWORK_ID);
        assertThat(model.getServiceNetworkArn()).isEqualTo(SERVICE_NETWORK_ARN);
        assertThat(model.getServiceNetworkName()).isEqualTo(SERVICE_NETWORK_NAME);

        assertThat(model.getServiceId()).isEqualTo(SERVICE_ID);
        assertThat(model.getServiceArn()).isEqualTo(SERVICE_ARN);
        assertThat(model.getServiceName()).isEqualTo(SERVICE_NAME);

        assertThat(model.getDnsEntry()).isEqualTo(DNS_ENTRY);

        assertThat(model.getTags()).isEqualTo(TagsTestUtils.MODEL_TAGS);
    }

    @ParameterizedTest
    @MethodSource("provideExceptions")
    public void handleRequest_getCallThrowsException_throwEquivalentHandlerErrorCode(
            @Nonnull final Class<? extends Throwable> exception,
            @Nonnull final HandlerErrorCode errorCode) {
        final var request = getRequest();

        mockSdkGetThrow(exception);

        final var response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
    }

    private ResourceModel getModel() {
        return ResourceModel.builder()
                .arn(SERVICE_NETWORK_SERVICE_ASSOCIATION_ARN)
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
