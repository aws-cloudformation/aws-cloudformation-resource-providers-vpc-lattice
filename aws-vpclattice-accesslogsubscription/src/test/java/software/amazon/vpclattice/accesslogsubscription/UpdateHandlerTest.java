package software.amazon.vpclattice.accesslogsubscription;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.UpdateAccessLogSubscriptionResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static software.amazon.vpclattice.accesslogsubscription.TagsTestUtils.MOCK_PROXY_CLIENT;

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
        final var previousModel = getModel(DESTINATION_ARN);

        final var model = getModel(ANOTHER_DESTINATION_ARN);

        final var request = getRequest(previousModel, model).toBuilder()
                .desiredResourceTags(Map.of("foo", "bar"))
                .build();

        mockSdkReturn(UpdateAccessLogSubscriptionResponse.builder()
                .arn(ALS_ARN)
                .destinationArn(DESTINATION_ARN + 1)
                .build());

        mockSdkReturn(getAccessLogSubscription());
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.LIST_TAGS_RESPONSE);
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.TAG_RESOURCE_RESPONSE);
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.UNTAG_RESOURCE_RESPONSE);

        final var response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getArn()).isEqualTo(ALS_ARN);
    }

    @Test
    public void handleRequest_sameModel_successEvent() {
        final var request = getRequest(getModel(DESTINATION_ARN), getModel(DESTINATION_ARN));
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.EMPTY_LIST_TAGS_RESPONSE);
        mockSdkReturn(getAccessLogSubscription());

        final var response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getArn()).isEqualTo(ALS_ARN);
    }

    @ParameterizedTest
    @MethodSource("provideExceptions")
    public void handleRequest_updateCallThrowsException_throwEquivalentHandlerErrorCode(
            final Class<? extends Throwable> exception,
            final HandlerErrorCode errorCode) {
        final var request = getRequest(getModel(DESTINATION_ARN), getModel(ANOTHER_DESTINATION_ARN));

        mockSdkUpdateThrow(exception);
        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
    }

    private ResourceModel getModel(String destinationArn) {
        return ResourceModel.builder()
                .arn(ALS_ARN)
                .destinationArn(destinationArn)
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
