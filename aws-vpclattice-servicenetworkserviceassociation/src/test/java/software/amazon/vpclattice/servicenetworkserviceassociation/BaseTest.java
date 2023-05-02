package software.amazon.vpclattice.servicenetworkserviceassociation;

import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.AccessDeniedException;
import software.amazon.awssdk.services.vpclattice.model.ConflictException;
import software.amazon.awssdk.services.vpclattice.model.CreateServiceNetworkServiceAssociationRequest;
import software.amazon.awssdk.services.vpclattice.model.CreateServiceNetworkServiceAssociationResponse;
import software.amazon.awssdk.services.vpclattice.model.DeleteServiceNetworkServiceAssociationRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteServiceNetworkServiceAssociationResponse;
import software.amazon.awssdk.services.vpclattice.model.GetServiceNetworkServiceAssociationRequest;
import software.amazon.awssdk.services.vpclattice.model.GetServiceNetworkServiceAssociationResponse;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ListServiceNetworkServiceAssociationsRequest;
import software.amazon.awssdk.services.vpclattice.model.ListServiceNetworkServiceAssociationsResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceNetworkServiceAssociationStatus;
import software.amazon.awssdk.services.vpclattice.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
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

public class BaseTest {
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

    public static final String AWS_ACCOUNT_ID = "123456789012";

    public static final String REGION = "us-west-2";

    public static final String NEXT_TOKEN = "NEXT_TOKEN";

    public static final String SERVICE_NETWORK_SERVICE_ASSOCIATION_ID = "snsa-12345678901234567";

    public static final String SERVICE_NETWORK_SERVICE_ASSOCIATION_ARN = String.format(
            "arn:aws:vpc-lattice:%s:%s:servicenetworkserviceassociation/%s",
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_NETWORK_SERVICE_ASSOCIATION_ID
    );

    public static final String SERVICE_NETWORK_ID = "sn-12345678901234567";

    public static final String SERVICE_NETWORK_ARN = String.format(
            "arn:aws:vpc-lattice:%s:%s:servicenetwork/%s",
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_NETWORK_ID
    );

    public static final String SERVICE_NETWORK_NAME = "service-network-name";

    public static final String SERVICE_ID = "svc-12345678901234567";

    public static final String SERVICE_ARN = String.format(
            "arn:aws:vpc-lattice:%s:%s:service/%s",
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_ID
    );

    public static final String SERVICE_NAME = "service-name";

    public static final String DOMAIN_NAME = "domain-name";

    public static final String HOSTED_ZONE_ID = "hosted-zone-id";

    public static final DnsEntry DNS_ENTRY = DnsEntry.builder()
            .domainName(DOMAIN_NAME)
            .hostedZoneId(HOSTED_ZONE_ID)
            .build();

    public static final software.amazon.awssdk.services.vpclattice.model.DnsEntry SDk_DNS =
            software.amazon.awssdk.services.vpclattice.model.DnsEntry.builder()
                    .domainName(DOMAIN_NAME)
                    .hostedZoneId(HOSTED_ZONE_ID)
                    .build();

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

    public static GetServiceNetworkServiceAssociationResponse getServiceNetworkServiceAssociation() {
        return getServiceNetworkServiceAssociation(ServiceNetworkServiceAssociationStatus.ACTIVE);
    }

    public static GetServiceNetworkServiceAssociationResponse getServiceNetworkServiceAssociation(
            @Nonnull final ServiceNetworkServiceAssociationStatus status) {
        return GetServiceNetworkServiceAssociationResponse.builder()
                .id(SERVICE_NETWORK_SERVICE_ASSOCIATION_ID)
                .arn(SERVICE_NETWORK_SERVICE_ASSOCIATION_ARN)
                .dnsEntry(SDk_DNS)
                .status(status)
                .createdAt(new Date().toInstant())
                .serviceNetworkId(SERVICE_NETWORK_ID)
                .serviceNetworkArn(SERVICE_NETWORK_ARN)
                .serviceNetworkName(SERVICE_NETWORK_NAME)
                .serviceId(SERVICE_ID)
                .serviceArn(SERVICE_ARN)
                .serviceName(SERVICE_NAME)
                .failureMessage("")
                .build();
    }


    protected void mockProxyClient() {
        doReturn(proxyClient).when(proxy).newProxy(any());
    }

    protected void mockSdkReturn(@Nonnull final GetServiceNetworkServiceAssociationResponse response, @Nonnull final GetServiceNetworkServiceAssociationResponse... responses) {
        var stubber = doReturn(response);

        for (final var r : responses) {
            stubber = stubber.doReturn(r);
        }

        stubber.when(proxyClient.client()).getServiceNetworkServiceAssociation(any(GetServiceNetworkServiceAssociationRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final CreateServiceNetworkServiceAssociationResponse response) {
        doReturn(response).when(proxyClient.client()).createServiceNetworkServiceAssociation(any(CreateServiceNetworkServiceAssociationRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final DeleteServiceNetworkServiceAssociationResponse response) {
        doReturn(response).when(proxyClient.client()).deleteServiceNetworkServiceAssociation(any(DeleteServiceNetworkServiceAssociationRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final ListServiceNetworkServiceAssociationsResponse response) {
        doReturn(response).when(proxyClient.client()).listServiceNetworkServiceAssociations(any(ListServiceNetworkServiceAssociationsRequest.class));
    }

    protected void mockSdkGetThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).getServiceNetworkServiceAssociation(any(GetServiceNetworkServiceAssociationRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).createServiceNetworkServiceAssociation(any(CreateServiceNetworkServiceAssociationRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Throwable exception) {
        doThrow(exception).when(proxyClient.client()).createServiceNetworkServiceAssociation(any(CreateServiceNetworkServiceAssociationRequest.class));
    }

    protected void mockSdkDeleteThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).deleteServiceNetworkServiceAssociation(any(DeleteServiceNetworkServiceAssociationRequest.class));
    }

    protected void mockSdkListThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).listServiceNetworkServiceAssociations(any(ListServiceNetworkServiceAssociationsRequest.class));
    }
}
