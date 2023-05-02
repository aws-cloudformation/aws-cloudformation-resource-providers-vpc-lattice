package software.amazon.vpclattice.accesslogsubscription;

import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.AccessDeniedException;
import software.amazon.awssdk.services.vpclattice.model.ConflictException;
import software.amazon.awssdk.services.vpclattice.model.CreateAccessLogSubscriptionRequest;
import software.amazon.awssdk.services.vpclattice.model.CreateAccessLogSubscriptionResponse;
import software.amazon.awssdk.services.vpclattice.model.DeleteAccessLogSubscriptionRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteAccessLogSubscriptionResponse;
import software.amazon.awssdk.services.vpclattice.model.GetAccessLogSubscriptionRequest;
import software.amazon.awssdk.services.vpclattice.model.GetAccessLogSubscriptionResponse;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ListAccessLogSubscriptionsRequest;
import software.amazon.awssdk.services.vpclattice.model.ListAccessLogSubscriptionsResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.awssdk.services.vpclattice.model.UpdateAccessLogSubscriptionRequest;
import software.amazon.awssdk.services.vpclattice.model.UpdateAccessLogSubscriptionResponse;
import software.amazon.awssdk.services.vpclattice.model.ValidationException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import javax.annotation.Nonnull;
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

    protected static final String REGION = "us-west-2";

    protected static final String NEXT_TOKEN = "next-token";

    protected static final String AWS_ACCOUNT_ID = "123456789012";

    protected static final String DESTINATION_ARN = "arn:aws:s3:::bucket-name";

    protected static final String ANOTHER_DESTINATION_ARN = "arn:aws:s3:::bucket-name-2";

    protected static final String RESOURCE_ID = "svc-12345678901234567";

    protected static final String RESOURCE_ARN = String.format("arn:aws:vpc-lattice:%s:%s:service/%s", REGION, AWS_ACCOUNT_ID, RESOURCE_ID);

    protected static final String ALS_ID = "als-12345678901234567";

    protected static final String ALS_ARN = String.format("arn:aws:vpc-lattice:%s:%s:accesslogsubscription/%s", REGION, AWS_ACCOUNT_ID, ALS_ID);

    protected static final String CLIENT_TOKEN = "token";

    protected GetAccessLogSubscriptionResponse getAccessLogSubscription() {
        return GetAccessLogSubscriptionResponse.builder()
                .id(ALS_ID)
                .arn(ALS_ARN)
                .resourceArn(RESOURCE_ARN)
                .resourceId(RESOURCE_ID)
                .destinationArn(DESTINATION_ARN)
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

    protected void mockSdkReturn(@Nonnull final UpdateAccessLogSubscriptionResponse response) {
        doReturn(response).when(proxyClient.client()).updateAccessLogSubscription(any(UpdateAccessLogSubscriptionRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final GetAccessLogSubscriptionResponse response, @Nonnull final GetAccessLogSubscriptionResponse... responses) {
        var stubber = doReturn(response);

        for (final var r : responses) {
            stubber = stubber.doReturn(r);
        }

        stubber.when(proxyClient.client()).getAccessLogSubscription(any(GetAccessLogSubscriptionRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final CreateAccessLogSubscriptionResponse response) {
        doReturn(response).when(proxyClient.client()).createAccessLogSubscription(any(CreateAccessLogSubscriptionRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final DeleteAccessLogSubscriptionResponse response) {
        doReturn(response).when(proxyClient.client()).deleteAccessLogSubscription(any(DeleteAccessLogSubscriptionRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final ListAccessLogSubscriptionsResponse response) {
        doReturn(response).when(proxyClient.client()).listAccessLogSubscriptions(any(ListAccessLogSubscriptionsRequest.class));
    }

    protected void mockSdkGetThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).getAccessLogSubscription(any(GetAccessLogSubscriptionRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).createAccessLogSubscription(any(CreateAccessLogSubscriptionRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Throwable exception) {
        doThrow(exception).when(proxyClient.client()).createAccessLogSubscription(any(CreateAccessLogSubscriptionRequest.class));
    }

    protected void mockSdkUpdateThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).updateAccessLogSubscription(any(UpdateAccessLogSubscriptionRequest.class));
    }

    protected void mockSdkDeleteThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).deleteAccessLogSubscription(any(DeleteAccessLogSubscriptionRequest.class));
    }

    protected void mockSdkListThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).listAccessLogSubscriptions(any(ListAccessLogSubscriptionsRequest.class));
    }
}
