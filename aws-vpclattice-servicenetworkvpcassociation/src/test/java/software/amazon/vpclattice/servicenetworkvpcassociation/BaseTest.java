package software.amazon.vpclattice.servicenetworkvpcassociation;

import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.AccessDeniedException;
import software.amazon.awssdk.services.vpclattice.model.ConflictException;
import software.amazon.awssdk.services.vpclattice.model.CreateServiceNetworkVpcAssociationRequest;
import software.amazon.awssdk.services.vpclattice.model.CreateServiceNetworkVpcAssociationResponse;
import software.amazon.awssdk.services.vpclattice.model.DeleteServiceNetworkVpcAssociationRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteServiceNetworkVpcAssociationResponse;
import software.amazon.awssdk.services.vpclattice.model.GetServiceNetworkVpcAssociationRequest;
import software.amazon.awssdk.services.vpclattice.model.GetServiceNetworkVpcAssociationResponse;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ListServiceNetworkVpcAssociationsRequest;
import software.amazon.awssdk.services.vpclattice.model.ListServiceNetworkVpcAssociationsResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceNetworkVpcAssociationStatus;
import software.amazon.awssdk.services.vpclattice.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.awssdk.services.vpclattice.model.UpdateServiceNetworkVpcAssociationRequest;
import software.amazon.awssdk.services.vpclattice.model.UpdateServiceNetworkVpcAssociationResponse;
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

    public static final String SERVICE_NETWORK_VPC_ASSOCIATION_ID = "snva-12345678901234567";

    public static final String SERVICE_NETWORK_VPC_ASSOCIATION_ARN = String.format(
            "arn:aws:vpc-lattice:%s:%s:servicenetworkvpcassociation/%s",
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_NETWORK_VPC_ASSOCIATION_ID
    );

    public static final String SERVICE_NETWORK_ID = "sn-12345678901234567";

    public static final String SERVICE_NETWORK_ARN = String.format(
            "arn:aws:vpc-lattice:%s:%s:servicenetwork/%s",
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_NETWORK_ID
    );

    public static final String SERVICE_NETWORK_NAME = "service-network-name";

    public static final String VPC_ID = "vpc-id";

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

    public static GetServiceNetworkVpcAssociationResponse getServiceNetworkVpcAssociation() {
        return getServiceNetworkVpcAssociation(ServiceNetworkVpcAssociationStatus.ACTIVE);
    }

    public static GetServiceNetworkVpcAssociationResponse getServiceNetworkVpcAssociation(
            @Nonnull final ServiceNetworkVpcAssociationStatus status) {
        return GetServiceNetworkVpcAssociationResponse.builder()
                .id(SERVICE_NETWORK_VPC_ASSOCIATION_ID)
                .arn(SERVICE_NETWORK_VPC_ASSOCIATION_ARN)
                .status(status)
                .createdAt(new Date().toInstant())
                .vpcId(VPC_ID)
                .serviceNetworkId(SERVICE_NETWORK_ID)
                .serviceNetworkArn(SERVICE_NETWORK_ARN)
                .serviceNetworkName(SERVICE_NETWORK_NAME)
                .failureMessage("")
                .build();
    }

    protected void mockProxyClient() {
        doReturn(proxyClient).when(proxy).newProxy(any());
    }

    protected void mockSdkReturn(@Nonnull final UpdateServiceNetworkVpcAssociationResponse response) {
        doReturn(response).when(proxyClient.client()).updateServiceNetworkVpcAssociation(any(UpdateServiceNetworkVpcAssociationRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final GetServiceNetworkVpcAssociationResponse response, @Nonnull final GetServiceNetworkVpcAssociationResponse... responses) {
        var stubber = doReturn(response);

        for (final var r : responses) {
            stubber = stubber.doReturn(r);
        }

        stubber.when(proxyClient.client()).getServiceNetworkVpcAssociation(any(GetServiceNetworkVpcAssociationRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final CreateServiceNetworkVpcAssociationResponse response) {
        doReturn(response).when(proxyClient.client()).createServiceNetworkVpcAssociation(any(CreateServiceNetworkVpcAssociationRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final DeleteServiceNetworkVpcAssociationResponse response) {
        doReturn(response).when(proxyClient.client()).deleteServiceNetworkVpcAssociation(any(DeleteServiceNetworkVpcAssociationRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final ListServiceNetworkVpcAssociationsResponse response) {
        doReturn(response).when(proxyClient.client()).listServiceNetworkVpcAssociations(any(ListServiceNetworkVpcAssociationsRequest.class));
    }

    protected void mockSdkGetThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).getServiceNetworkVpcAssociation(any(GetServiceNetworkVpcAssociationRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).createServiceNetworkVpcAssociation(any(CreateServiceNetworkVpcAssociationRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Throwable exception) {
        doThrow(exception).when(proxyClient.client()).createServiceNetworkVpcAssociation(any(CreateServiceNetworkVpcAssociationRequest.class));
    }

    protected void mockSdkUpdateThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).updateServiceNetworkVpcAssociation(any(UpdateServiceNetworkVpcAssociationRequest.class));
    }

    protected void mockSdkDeleteThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).deleteServiceNetworkVpcAssociation(any(DeleteServiceNetworkVpcAssociationRequest.class));
    }

    protected void mockSdkListThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).listServiceNetworkVpcAssociations(any(ListServiceNetworkVpcAssociationsRequest.class));
    }
}
