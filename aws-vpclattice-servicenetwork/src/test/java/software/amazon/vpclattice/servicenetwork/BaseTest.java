package software.amazon.vpclattice.servicenetwork;

import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.AccessDeniedException;
import software.amazon.awssdk.services.vpclattice.model.AuthType;
import software.amazon.awssdk.services.vpclattice.model.ConflictException;
import software.amazon.awssdk.services.vpclattice.model.CreateServiceNetworkRequest;
import software.amazon.awssdk.services.vpclattice.model.CreateServiceNetworkResponse;
import software.amazon.awssdk.services.vpclattice.model.DeleteServiceNetworkRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteServiceNetworkResponse;
import software.amazon.awssdk.services.vpclattice.model.GetServiceNetworkRequest;
import software.amazon.awssdk.services.vpclattice.model.GetServiceNetworkResponse;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ListServiceNetworksRequest;
import software.amazon.awssdk.services.vpclattice.model.ListServiceNetworksResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.awssdk.services.vpclattice.model.UpdateServiceNetworkRequest;
import software.amazon.awssdk.services.vpclattice.model.UpdateServiceNetworkResponse;
import software.amazon.awssdk.services.vpclattice.model.ValidationException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import javax.annotation.Nonnull;
import java.util.Date;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

public abstract class BaseTest {
    protected AmazonWebServicesClientProxy proxy;

    @Mock
    protected VpcLatticeClient client;

    @Mock
    protected ProxyClient<VpcLatticeClient> proxyClient;

    protected BaseHandlerStd handler;

    protected static final LoggerProxy logger;

    protected static final Credentials MOCK_CREDENTIALS;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
    }

    protected static final String AWS_ACCOUNT_ID = "123456789012";

    protected static final String REGION = "us-west-2";

    protected static final String SERVICE_NETWORK_NAME = "service-network-name";

    protected static final String SERVICE_NETWORK_ID = "sn-12345678901234567";

    protected static final String SERVICE_NETWORK_ARN = String.format("arn:aws:vpc-lattice:%s:%s:servicenetwork/%s", REGION, AWS_ACCOUNT_ID, SERVICE_NETWORK_ID);

    protected static final Date SERVICE_NETWORK_CREATED_AT = new Date();

    protected static final Date SERVICE_NETWORK_LAST_UPDATED = new Date();

    protected static final String CLIENT_TOKEN = "token";

    protected static final String NEXT_TOKEN = "next-token";

    protected static final String STACK_ID = String.format("arn:aws:cloudformation:%s:%s:stack/%s/%s",
            REGION,
            AWS_ACCOUNT_ID,
            "stack-name",
            "f449b250-b969-11e0-a185-5081d0136786"
    );

    protected GetServiceNetworkResponse getServiceNetwork() {
        return GetServiceNetworkResponse.builder()
                .id(SERVICE_NETWORK_ID)
                .arn(SERVICE_NETWORK_ARN)
                .name(SERVICE_NETWORK_NAME)
                .authType(AuthType.NONE)
                .createdAt(SERVICE_NETWORK_CREATED_AT.toInstant())
                .lastUpdatedAt(SERVICE_NETWORK_LAST_UPDATED.toInstant())
                .build();
    }

    public static Stream<Arguments> provideExceptions() {
        return Stream.of(
                Arguments.of(ValidationException.class, HandlerErrorCode.InvalidRequest),
                Arguments.of(ConflictException.class, HandlerErrorCode.InvalidRequest),
                Arguments.of(AccessDeniedException.class, HandlerErrorCode.AccessDenied),
                Arguments.of(InternalServerException.class, HandlerErrorCode.ServiceInternalError),
                Arguments.of(ServiceQuotaExceededException.class, HandlerErrorCode.ServiceLimitExceeded),
                Arguments.of(ThrottlingException.class, HandlerErrorCode.Throttling),
                Arguments.of(IllegalStateException.class, HandlerErrorCode.InternalFailure)
        );
    }

    protected void mockProxyClient() {
        doReturn(proxyClient).when(proxy).newProxy(any());
    }

    protected void mockSdkReturn(@Nonnull final UpdateServiceNetworkResponse response) {
        doReturn(response).when(proxyClient.client()).updateServiceNetwork(any(UpdateServiceNetworkRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final GetServiceNetworkResponse response, @Nonnull final GetServiceNetworkResponse... responses) {
        var stubber = doReturn(response);

        for (final var r : responses) {
            stubber = stubber.doReturn(r);
        }

        stubber.when(proxyClient.client()).getServiceNetwork(any(GetServiceNetworkRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final CreateServiceNetworkResponse response) {
        doReturn(response).when(proxyClient.client()).createServiceNetwork(any(CreateServiceNetworkRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final DeleteServiceNetworkResponse response) {
        doReturn(response).when(proxyClient.client()).deleteServiceNetwork(any(DeleteServiceNetworkRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final ListServiceNetworksResponse response) {
        doReturn(response).when(proxyClient.client()).listServiceNetworks(any(ListServiceNetworksRequest.class));
    }

    protected void mockSdkGetThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).getServiceNetwork(any(GetServiceNetworkRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).createServiceNetwork(any(CreateServiceNetworkRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Throwable exception) {
        doThrow(exception).when(proxyClient.client()).createServiceNetwork(any(CreateServiceNetworkRequest.class));
    }

    protected void mockSdkUpdateThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).updateServiceNetwork(any(UpdateServiceNetworkRequest.class));
    }

    protected void mockSdkDeleteThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).deleteServiceNetwork(any(DeleteServiceNetworkRequest.class));
    }

    protected void mockSdkListThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).listServiceNetworks(any(ListServiceNetworksRequest.class));
    }
}
