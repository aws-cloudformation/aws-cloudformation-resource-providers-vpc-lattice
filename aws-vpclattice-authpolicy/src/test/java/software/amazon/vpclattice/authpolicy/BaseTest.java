package software.amazon.vpclattice.authpolicy;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.AccessDeniedException;
import software.amazon.awssdk.services.vpclattice.model.AuthPolicyState;
import software.amazon.awssdk.services.vpclattice.model.ConflictException;
import software.amazon.awssdk.services.vpclattice.model.DeleteAuthPolicyRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteAuthPolicyResponse;
import software.amazon.awssdk.services.vpclattice.model.GetAuthPolicyRequest;
import software.amazon.awssdk.services.vpclattice.model.GetAuthPolicyResponse;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.PutAuthPolicyRequest;
import software.amazon.awssdk.services.vpclattice.model.PutAuthPolicyResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.awssdk.services.vpclattice.model.ValidationException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.vpclattice.common.JsonConverter;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
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

    public static ProxyClient<VpcLatticeClient> MOCK_PROXY_CLIENT(
            final AmazonWebServicesClientProxy proxy,
            final VpcLatticeClient vpcLatticeClient) {
        return new ProxyClient<>() {
            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseT
            injectCredentialsAndInvokeV2(RequestT request, Function<RequestT, ResponseT> requestFunction) {
                return proxy.injectCredentialsAndInvokeV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse>
            CompletableFuture<ResponseT>
            injectCredentialsAndInvokeV2Async(RequestT request, Function<RequestT, CompletableFuture<ResponseT>> requestFunction) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse, IterableT extends SdkIterable<ResponseT>>
            IterableT
            injectCredentialsAndInvokeIterableV2(RequestT request, Function<RequestT, IterableT> requestFunction) {
                return proxy.injectCredentialsAndInvokeIterableV2(request, requestFunction);
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseInputStream<ResponseT>
            injectCredentialsAndInvokeV2InputStream(RequestT requestT, Function<RequestT, ResponseInputStream<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <RequestT extends AwsRequest, ResponseT extends AwsResponse> ResponseBytes<ResponseT>
            injectCredentialsAndInvokeV2Bytes(RequestT requestT, Function<RequestT, ResponseBytes<ResponseT>> function) {
                throw new UnsupportedOperationException();
            }

            @Override
            public VpcLatticeClient client() {
                return vpcLatticeClient;
            }
        };
    }

    protected static final String AWS_ACCOUNT_ID = "123456789012";

    protected static final String REGION = "us-west-2";

    protected static final String RESOURCE_IDENTIFIER = "svc-12345678901234567";

    protected static final Map<String, Object> POLICY = ImmutableMap.<String, Object>builder()
            .put("Key", "Value")
            .build();

    protected static final String POLICY_STRING = JsonConverter.toJSONString(POLICY);

    protected static final AuthPolicyState STATE = AuthPolicyState.ACTIVE;

    protected static GetAuthPolicyResponse getAuthPolicy() {
        return GetAuthPolicyResponse.builder()
                .state((String) null)
                .policy(null)
                .build();
    }

    protected static GetAuthPolicyResponse getAuthPolicy(
            Map<String, Object> policy) {
        return GetAuthPolicyResponse.builder()
                .state(STATE)
                .policy(JsonConverter.toJSONString(policy))
                .build();
    }

    protected void mockProxyClient() {
        doReturn(proxyClient).when(proxy).newProxy(any());
    }

    protected void mockSdkReturn(@Nonnull final GetAuthPolicyResponse response, @Nonnull final GetAuthPolicyResponse... responses) {
        var stubber = doReturn(response);

        for (final var r : responses) {
            stubber = stubber.doReturn(r);
        }

        stubber.when(proxyClient.client()).getAuthPolicy(any(GetAuthPolicyRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final PutAuthPolicyResponse response) {
        doReturn(response).when(proxyClient.client()).putAuthPolicy(any(PutAuthPolicyRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final DeleteAuthPolicyResponse response) {
        doReturn(response).when(proxyClient.client()).deleteAuthPolicy(any(DeleteAuthPolicyRequest.class));
    }

    protected void mockSdkGetThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).getAuthPolicy(any(GetAuthPolicyRequest.class));
    }

    protected void mockSdkPutThrow(@Nonnull final Throwable exception) {
        doThrow(exception).when(proxyClient.client()).putAuthPolicy(any(PutAuthPolicyRequest.class));
    }

    protected void mockSdkPutThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).putAuthPolicy(any(PutAuthPolicyRequest.class));
    }

    protected void mockSdkDeleteThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).deleteAuthPolicy(any(DeleteAuthPolicyRequest.class));
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
}
