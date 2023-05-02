package software.amazon.vpclattice.service;

import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.AccessDeniedException;
import software.amazon.awssdk.services.vpclattice.model.AuthType;
import software.amazon.awssdk.services.vpclattice.model.ConflictException;
import software.amazon.awssdk.services.vpclattice.model.CreateServiceRequest;
import software.amazon.awssdk.services.vpclattice.model.CreateServiceResponse;
import software.amazon.awssdk.services.vpclattice.model.DeleteServiceRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteServiceResponse;
import software.amazon.awssdk.services.vpclattice.model.GetServiceRequest;
import software.amazon.awssdk.services.vpclattice.model.GetServiceResponse;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ListServicesRequest;
import software.amazon.awssdk.services.vpclattice.model.ListServicesResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.vpclattice.model.ServiceStatus;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.awssdk.services.vpclattice.model.UpdateServiceRequest;
import software.amazon.awssdk.services.vpclattice.model.UpdateServiceResponse;
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

    public static final String SERVICE_NAME = "service-name";

    public static final String SERVICE_ID = "svc-12345678901234567";

    public static final String AWS_ACCOUNT_ID = "123456789012";

    public static final String REGION = "us-west-2";

    public static final String SERVICE_ARN = String.format(
            "arn:aws:vpc-lattice:%s:%s:service/%s",
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_ID
    );

    public static final String CLIENT_TOKEN = "DUMMY_TOKEN";

    public static final String DOMAIN_NAME = "dns-name";

    public static final String HOSTED_ZONE_ID = "hosted-zone-id";

    public static final DnsEntry DNS_ENTRY = DnsEntry.builder()
            .domainName(DOMAIN_NAME)
            .hostedZoneId(HOSTED_ZONE_ID)
            .build();

    public static final String NEXT_TOKEN = "NEXT_TOKEN";


    protected static final String STACK_ID = String.format("arn:aws:cloudformation:%s:%s:stack/%s/%s",
            REGION,
            AWS_ACCOUNT_ID,
            "stack-name",
            "f449b250-b969-11e0-a185-5081d0136786"
    );

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

    public static GetServiceResponse getService() {
        return getService(ServiceStatus.ACTIVE);
    }

    public static GetServiceResponse getService(
            @Nonnull final ServiceStatus status) {
        return GetServiceResponse.builder()
                .id(SERVICE_ID)
                .arn(SERVICE_ARN)
                .name(SERVICE_NAME)
                .authType(AuthType.NONE.name())
                .dnsEntry(software.amazon.awssdk.services.vpclattice.model.DnsEntry.builder()
                        .domainName(DOMAIN_NAME)
                        .hostedZoneId(HOSTED_ZONE_ID)
                        .build())
                .createdAt(new Date().toInstant())
                .lastUpdatedAt(new Date().toInstant())
                .status(status)
                .failureMessage("")
                .build();
    }

    protected void mockProxyClient() {
        doReturn(proxyClient).when(proxy).newProxy(any());
    }

    protected void mockSdkReturn(@Nonnull final UpdateServiceResponse response) {
        doReturn(response).when(proxyClient.client()).updateService(any(UpdateServiceRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final GetServiceResponse response, @Nonnull final GetServiceResponse... responses) {
        var stubber = doReturn(response);

        for (final var r : responses) {
            stubber = stubber.doReturn(r);
        }

        stubber.when(proxyClient.client()).getService(any(GetServiceRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final CreateServiceResponse response) {
        doReturn(response).when(proxyClient.client()).createService(any(CreateServiceRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final DeleteServiceResponse response) {
        doReturn(response).when(proxyClient.client()).deleteService(any(DeleteServiceRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final ListServicesResponse response) {
        doReturn(response).when(proxyClient.client()).listServices(any(ListServicesRequest.class));
    }

    protected void mockSdkGetThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).getService(any(GetServiceRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).createService(any(CreateServiceRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Throwable exception) {
        doThrow(exception).when(proxyClient.client()).createService(any(CreateServiceRequest.class));
    }

    protected void mockSdkUpdateThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).updateService(any(UpdateServiceRequest.class));
    }

    protected void mockSdkDeleteThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).deleteService(any(DeleteServiceRequest.class));
    }

    protected void mockSdkListThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).listServices(any(ListServicesRequest.class));
    }
}
