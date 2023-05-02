package software.amazon.vpclattice.servicenetwork;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.ListServiceNetworksResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceNetworkSummary;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static software.amazon.vpclattice.servicenetwork.TagsTestUtils.MOCK_PROXY_CLIENT;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest extends BaseTest {
    @BeforeEach
    public void setup() {
        proxy = spy(new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(1000).toMillis()));
        client = mock(VpcLatticeClient.class);
        proxyClient = MOCK_PROXY_CLIENT(proxy, client);
        handler = new ListHandler();
        mockProxyClient();
    }

    @Test
    public void handleRequest_allApiCallsSuccess_successEvent() {
        final var request = getRequest();

        mockSdkReturn(ListServiceNetworksResponse.builder()
                .items(getSummaries(3))
                .build());

        final var response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);

        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isEqualTo(toResourceModels(getSummaries(3)));
    }

    @ParameterizedTest
    @MethodSource("provideExceptions")
    public void handleRequest_listCallThrowsException_throwEquivalentHandlerErrorCode(
            @Nonnull final Class<? extends Throwable> exception,
            @Nonnull final HandlerErrorCode errorCode) {
        final var request = getRequest();

        mockSdkListThrow(exception);

        final var response =
                handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(errorCode);
    }

    private ResourceModel getModel() {
        return ResourceModel.builder()
                .build();
    }

    private ResourceHandlerRequest<ResourceModel> getRequest() {
        return ResourceHandlerRequest.<ResourceModel>builder()
                .awsAccountId(AWS_ACCOUNT_ID)
                .region(REGION)
                .desiredResourceState(getModel())
                .build();
    }

    private List<ServiceNetworkSummary> getSummaries(final int numSummaries) {
        return IntStream
                .range(0, numSummaries)
                .mapToObj(
                        (i) -> ServiceNetworkSummary.builder()
                                .arn("service/svc-" + i)
                                .build()
                )
                .collect(Collectors.toList());
    }

    private List<ResourceModel> toResourceModels(@Nonnull final List<ServiceNetworkSummary> summaries) {
        return summaries
                .stream()
                .map((summary) -> ResourceModel.builder()
                        .arn(summary.arn())
                        .build())
                .collect(Collectors.toList());
    }
}
