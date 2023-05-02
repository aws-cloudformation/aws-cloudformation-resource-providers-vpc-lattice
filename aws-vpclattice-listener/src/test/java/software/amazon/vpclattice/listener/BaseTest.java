package software.amazon.vpclattice.listener;

import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.AccessDeniedException;
import software.amazon.awssdk.services.vpclattice.model.ConflictException;
import software.amazon.awssdk.services.vpclattice.model.CreateListenerRequest;
import software.amazon.awssdk.services.vpclattice.model.CreateListenerResponse;
import software.amazon.awssdk.services.vpclattice.model.DeleteListenerRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteListenerResponse;
import software.amazon.awssdk.services.vpclattice.model.GetListenerRequest;
import software.amazon.awssdk.services.vpclattice.model.GetListenerResponse;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ListListenersRequest;
import software.amazon.awssdk.services.vpclattice.model.ListListenersResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.awssdk.services.vpclattice.model.UpdateListenerRequest;
import software.amazon.awssdk.services.vpclattice.model.UpdateListenerResponse;
import software.amazon.awssdk.services.vpclattice.model.ValidationException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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

    public static final String AWS_ACCOUNT_ID = "123456789012";

    public static final String REGION = "us-west-2";

    public static final String SERVICE_ID = "svc-12345678901234567";

    public static final String SERVICE_ARN = String.format(
            "arn:aws:vpc-lattice:%s:%s:service/%s",
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_ID
    );

    public static final String LISTENER_ID = "listener-12345678901234567";

    public static final String LISTENER_NAME = "listener-name";

    public static final String LISTENER_ARN = String.format(
            "arn:aws:vpc-lattice:%s:%s:service/%s/listener/%s",
            REGION,
            AWS_ACCOUNT_ID,
            SERVICE_ID,
            LISTENER_ID
    );

    public static final Integer PORT = 8080;

    public static final String PROTOCOL = "HTTP";

    public static final String CLIENT_TOKEN = "DUMMY_TOKEN";

    public static final String NEXT_TOKEN = "NEXT_TOKEN";

    public static final DefaultAction MODEL_DEFAULT_RULE_ACTION = DefaultAction.builder()
            .forward(Forward.builder()
                    .targetGroups(getModelWeightedTargetGroups(2))
                    .build())
            .build();

    public static final DefaultAction MODEL_UPDATED_RULE_ACTION = DefaultAction.builder()
            .forward(Forward.builder()
                    .targetGroups(getModelWeightedTargetGroups(3))
                    .build())
            .build();

    public static final software.amazon.awssdk.services.vpclattice.model.RuleAction DEFAULT_RULE_ACTION =
            software.amazon.awssdk.services.vpclattice.model.RuleAction.builder()
                    .forward(software.amazon.awssdk.services.vpclattice.model.ForwardAction.builder()
                            .targetGroups(getMercuryWeightedTargetGroups(2))
                            .build())
                    .build();

    public static final software.amazon.awssdk.services.vpclattice.model.RuleAction UPDATED_RULE_ACTION =
            software.amazon.awssdk.services.vpclattice.model.RuleAction.builder()
                    .forward(software.amazon.awssdk.services.vpclattice.model.ForwardAction.builder()
                            .targetGroups(getMercuryWeightedTargetGroups(3))
                            .build())
                    .build();

    protected static GetListenerResponse getListener() {
        return GetListenerResponse.builder()
                .id(LISTENER_ID)
                .arn(LISTENER_ARN)
                .name(LISTENER_NAME)
                .serviceId(SERVICE_ID)
                .serviceArn(SERVICE_ARN)
                .defaultAction(DEFAULT_RULE_ACTION)
                .protocol(PROTOCOL)
                .port(PORT)
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

    public static List<WeightedTargetGroup> getModelWeightedTargetGroups(
            final int numberOfWeightedTargetGroups) {
        return IntStream.range(0, numberOfWeightedTargetGroups)
                .mapToObj((i) -> WeightedTargetGroup.builder()
                        .targetGroupIdentifier("tg-" + i)
                        .weight((int) (1. / numberOfWeightedTargetGroups * 100.))
                        .build())
                .collect(Collectors.toList());
    }

    public static List<software.amazon.awssdk.services.vpclattice.model.WeightedTargetGroup> getMercuryWeightedTargetGroups(
            final int numberOfWeightedTargetGroups) {
        return IntStream.range(0, numberOfWeightedTargetGroups)
                .mapToObj((i) -> software.amazon.awssdk.services.vpclattice.model.WeightedTargetGroup.builder()
                        .targetGroupIdentifier("tg-" + i)
                        .weight((int) (1. / numberOfWeightedTargetGroups * 100.))
                        .build())
                .collect(Collectors.toList());
    }

    protected void mockProxyClient() {
        doReturn(proxyClient).when(proxy).newProxy(any());
    }

    protected void mockSdkReturn(@Nonnull final UpdateListenerResponse response) {
        doReturn(response).when(proxyClient.client()).updateListener(any(UpdateListenerRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final GetListenerResponse response, @Nonnull final GetListenerResponse... responses) {
        var stubber = doReturn(response);

        for (final var r : responses) {
            stubber = stubber.doReturn(r);
        }

        stubber.when(proxyClient.client()).getListener(any(GetListenerRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final CreateListenerResponse response) {
        doReturn(response).when(proxyClient.client()).createListener(any(CreateListenerRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final DeleteListenerResponse response) {
        doReturn(response).when(proxyClient.client()).deleteListener(any(DeleteListenerRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final ListListenersResponse response) {
        doReturn(response).when(proxyClient.client()).listListeners(any(ListListenersRequest.class));
    }

    protected void mockSdkGetThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).getListener(any(GetListenerRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).createListener(any(CreateListenerRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Throwable exception) {
        doThrow(exception).when(proxyClient.client()).createListener(any(CreateListenerRequest.class));
    }

    protected void mockSdkUpdateThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).updateListener(any(UpdateListenerRequest.class));
    }

    protected void mockSdkDeleteThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).deleteListener(any(DeleteListenerRequest.class));
    }

    protected void mockSdkListThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).listListeners(any(ListListenersRequest.class));
    }
}
