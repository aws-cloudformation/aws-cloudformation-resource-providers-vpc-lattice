package software.amazon.vpclattice.authpolicy;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.PutAuthPolicyResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.JsonConverter;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends BaseTest {
    private static final Map<String, Object> UPDATED_POLICY = ImmutableMap.<String, Object>builder()
            .put("Key", "Value")
            .put("Key2", "Value2")
            .build();

    private static final String UPDATED_POLICY_STRING = JsonConverter.toJSONString(UPDATED_POLICY);

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
        final var previousModel = getModel(POLICY);

        final var model = getModel(UPDATED_POLICY);

        final var request = getRequest(previousModel, model).toBuilder()
                .desiredResourceTags(Map.of("foo", "bar"))
                .build();

        mockSdkReturn(PutAuthPolicyResponse.builder()
                .policy(UPDATED_POLICY_STRING)
                .state(STATE)
                .build());

        mockSdkReturn(getAuthPolicy(UPDATED_POLICY));

        final var response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getResourceIdentifier()).isEqualTo(RESOURCE_IDENTIFIER);
        assertThat(response.getResourceModel().getPolicy()).isEqualTo(UPDATED_POLICY);
    }

    @Test
    public void handleRequest_sameModel_successEvent() {
        final var request = getRequest(getModel(POLICY), getModel(POLICY));

        final var response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getResourceIdentifier()).isEqualTo(RESOURCE_IDENTIFIER);
    }

    @ParameterizedTest
    @MethodSource("provideExceptions")
    public void handleRequest_updateCallThrowsException_throwEquivalentHandlerErrorCode(
            final Class<? extends Throwable> exception,
            final HandlerErrorCode errorCode) {
        final var request = getRequest(getModel(POLICY), getModel(UPDATED_POLICY));

        mockSdkPutThrow(exception);
        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
    }

    private ResourceModel getModel(Map<String, Object> policy) {
        return ResourceModel.builder()
                .resourceIdentifier(RESOURCE_IDENTIFIER)
                .policy(policy)
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
