package software.amazon.vpclattice.servicenetworkvpcassociation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.ServiceNetworkVpcAssociationStatus;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static software.amazon.vpclattice.servicenetworkvpcassociation.TagsTestUtils.MOCK_PROXY_CLIENT;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends BaseTest {
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
        final var previousModel = getModel(List.of("sg-1"));

        final var model = getModel(List.of("sg-2"));

        final var request = getRequest(previousModel, model).toBuilder()
                .desiredResourceTags(Map.of("foo", "bar"))
                .build();

        mockSdkReturn(getServiceNetworkVpcAssociation(ServiceNetworkVpcAssociationStatus.UPDATE_IN_PROGRESS), getServiceNetworkVpcAssociation(), getServiceNetworkVpcAssociation());
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.LIST_TAGS_RESPONSE);
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.TAG_RESOURCE_RESPONSE);
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.UNTAG_RESOURCE_RESPONSE);

        final var response =
                handler.handleRequest(proxy, request, CallbackContext.builder().hasCalledUpdate(true).build(), logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getArn()).isEqualTo(SERVICE_NETWORK_VPC_ASSOCIATION_ARN);
    }

    @Test
    public void handleRequest_sameModel_successEvent() {
        final var request = getRequest(getModel(List.of()), getModel(List.of()));
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.EMPTY_LIST_TAGS_RESPONSE);
        mockSdkReturn(getServiceNetworkVpcAssociation());

        final var response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getArn()).isEqualTo(SERVICE_NETWORK_VPC_ASSOCIATION_ARN);
    }

    @ParameterizedTest
    @MethodSource("provideExceptions")
    public void handleRequest_updateCallThrowsException_throwEquivalentHandlerErrorCode(
            final Class<? extends Throwable> exception,
            final HandlerErrorCode errorCode) {
        final var request = getRequest(getModel(List.of("sg-1")), getModel(List.of("sg-2")));

        mockSdkUpdateThrow(exception);
        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
    }

    private ResourceModel getModel(Collection<String> securityGroupIds) {
        return ResourceModel.builder()
                .arn(SERVICE_NETWORK_VPC_ASSOCIATION_ARN)
                .securityGroupIds(new HashSet<>(securityGroupIds))
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> getRequest(
            @Nonnull final ResourceModel previousModel,
            @Nonnull final ResourceModel model) {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .awsAccountId(AWS_ACCOUNT_ID)
                .region(REGION)
                .previousResourceState(previousModel)
                .desiredResourceState(model)
                .build();
    }
}
