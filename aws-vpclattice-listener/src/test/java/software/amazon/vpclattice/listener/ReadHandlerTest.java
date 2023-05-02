package software.amazon.vpclattice.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nonnull;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static software.amazon.vpclattice.listener.TagsTestUtils.MOCK_PROXY_CLIENT;

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

        mockSdkReturn(getListener());
        TagsTestUtils.mockSdkReturn(proxyClient, TagsTestUtils.LIST_TAGS_RESPONSE);

        final var response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        final var model = response.getResourceModel();

        assertThat(model.getId()).isEqualTo(LISTENER_ID);
        assertThat(model.getArn()).isEqualTo(LISTENER_ARN);

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
                .arn(LISTENER_ARN)
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
