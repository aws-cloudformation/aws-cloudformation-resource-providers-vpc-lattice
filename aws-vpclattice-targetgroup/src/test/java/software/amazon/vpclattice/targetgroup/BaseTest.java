package software.amazon.vpclattice.targetgroup;

import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.AccessDeniedException;
import software.amazon.awssdk.services.vpclattice.model.ConflictException;
import software.amazon.awssdk.services.vpclattice.model.CreateTargetGroupRequest;
import software.amazon.awssdk.services.vpclattice.model.CreateTargetGroupResponse;
import software.amazon.awssdk.services.vpclattice.model.DeleteTargetGroupRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteTargetGroupResponse;
import software.amazon.awssdk.services.vpclattice.model.DeregisterTargetsRequest;
import software.amazon.awssdk.services.vpclattice.model.DeregisterTargetsResponse;
import software.amazon.awssdk.services.vpclattice.model.GetTargetGroupRequest;
import software.amazon.awssdk.services.vpclattice.model.GetTargetGroupResponse;
import software.amazon.awssdk.services.vpclattice.model.InternalServerException;
import software.amazon.awssdk.services.vpclattice.model.ListTargetGroupsRequest;
import software.amazon.awssdk.services.vpclattice.model.ListTargetGroupsResponse;
import software.amazon.awssdk.services.vpclattice.model.ListTargetsRequest;
import software.amazon.awssdk.services.vpclattice.model.ListTargetsResponse;
import software.amazon.awssdk.services.vpclattice.model.RegisterTargetsRequest;
import software.amazon.awssdk.services.vpclattice.model.RegisterTargetsResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceQuotaExceededException;
import software.amazon.awssdk.services.vpclattice.model.TargetFailure;
import software.amazon.awssdk.services.vpclattice.model.TargetGroupStatus;
import software.amazon.awssdk.services.vpclattice.model.TargetSummary;
import software.amazon.awssdk.services.vpclattice.model.ThrottlingException;
import software.amazon.awssdk.services.vpclattice.model.UpdateTargetGroupRequest;
import software.amazon.awssdk.services.vpclattice.model.UpdateTargetGroupResponse;
import software.amazon.awssdk.services.vpclattice.model.ValidationException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Credentials;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.LoggerProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import javax.annotation.Nonnull;
import java.util.Date;
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

    protected static final LoggerProxy logger;
    protected static final Credentials MOCK_CREDENTIALS;

    static {
        MOCK_CREDENTIALS = new Credentials("accessKey", "secretKey", "token");
        logger = new LoggerProxy();
    }

    @Mock
    protected ProxyClient<VpcLatticeClient> proxyClient;

    protected BaseHandlerStd handler;
    public static final String CLIENT_TOKEN = "DUMMY_TOKEN";

    public static final String NEXT_TOKEN = "NEXT_TOKEN";

    public static final String FAILURE_CODE = "ERROR_CODE";

    public static final String FAILURE_MESSAGE = "FAILURE_MESSAGE";
    public static final String TARGET_GROUP_NAME = "target-group-name";

    public static final String TARGET_GROUP_ID = "tg-12345678901234567";

    public static final String AWS_ACCOUNT_ID = "123456789012";

    public static final String REGION = "us-west-2";

    public static final String TARGET_GROUP_ARN = String.format(
            "arn:aws:vpc-lattice:%s:%s:targetgroup/%s",
            REGION,
            AWS_ACCOUNT_ID,
            TARGET_GROUP_ID
    );

    public static final boolean ENABLED = true;
    public static final String PROTOCOL = "HTTPS";
    public static final String PROTOCOL_VERSION = "HTTP2";
    public static final int PORT = 20002;
    public static final String PATH = "path";
    public static final int HEALTH_CHECK_INTERVAL_SECONDS = 10;
    public static final int HEALTH_CHECK_TIMEOUT_SECONDS = 20;

    public static final int HEALTHY_THRESHOLD_COUNT = 5;

    public static final int UNHEALTHY_THRESHOLD_COUNT = 2;

    public static final String STATUS_CODE_MATCHER = "1-2";

    public static final String VPC_IDENTIFIER = "vpc-1";

    public static final String TYPE = "IP";


    public static final HealthCheckConfig MODEL_HEALTH_CHECK_CONFIG = HealthCheckConfig.builder()
            .enabled(ENABLED)
            .protocol(PROTOCOL)
            .port(PORT)
            .path(PATH)
            .healthCheckIntervalSeconds(HEALTH_CHECK_INTERVAL_SECONDS)
            .healthCheckTimeoutSeconds(HEALTH_CHECK_TIMEOUT_SECONDS)
            .healthyThresholdCount(HEALTHY_THRESHOLD_COUNT)
            .unhealthyThresholdCount(UNHEALTHY_THRESHOLD_COUNT)
            .matcher(Matcher.builder()
                    .httpCode(STATUS_CODE_MATCHER)
                    .build())
            .build();

    public static final TargetGroupConfig MODEL_TARGET_GROUP_CONFIG = TargetGroupConfig.builder()
            .port(PORT)
            .protocol(PROTOCOL)
            .protocolVersion(PROTOCOL_VERSION)
            .vpcIdentifier(VPC_IDENTIFIER)
            .healthCheck(MODEL_HEALTH_CHECK_CONFIG)
            .build();

    public static software.amazon.awssdk.services.vpclattice.model.HealthCheckConfig SDK_HEALTH_CHECK_CONFIG = getSdkHealthCheckConfig();

    public static software.amazon.awssdk.services.vpclattice.model.HealthCheckConfig getSdkHealthCheckConfig() {
        return software.amazon.awssdk.services.vpclattice.model.HealthCheckConfig.builder()
                .enabled(ENABLED)
                .protocol(PROTOCOL)
                .port(PORT)
                .path(PATH)
                .healthCheckIntervalSeconds(HEALTH_CHECK_INTERVAL_SECONDS)
                .healthCheckTimeoutSeconds(HEALTH_CHECK_TIMEOUT_SECONDS)
                .healthyThresholdCount(HEALTHY_THRESHOLD_COUNT)
                .unhealthyThresholdCount(UNHEALTHY_THRESHOLD_COUNT)
                .matcher(software.amazon.awssdk.services.vpclattice.model.Matcher.builder()
                        .httpCode(STATUS_CODE_MATCHER)
                        .build())
                .build();
    }

    public static software.amazon.awssdk.services.vpclattice.model.TargetGroupConfig getMercuryTargetGroupConfig() {
        return software.amazon.awssdk.services.vpclattice.model.TargetGroupConfig.builder()
                .port(PORT)
                .protocol(PROTOCOL)
                .protocolVersion(PROTOCOL_VERSION)
                .vpcIdentifier(VPC_IDENTIFIER)
                .healthCheck(SDK_HEALTH_CHECK_CONFIG)
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

    public static List<TargetSummary> getTargets(final int numberOfTargets) {
        return IntStream.range(0, numberOfTargets)
                .mapToObj((i) -> TargetSummary.builder()
                        .id("id-" + i)
                        .port(i)
                        .status("HEALTHY")
                        .build())
                .collect(Collectors.toList());
    }

    public static List<Target> getTargetsModel(final int numberOfTargets) {
        return IntStream.range(0, numberOfTargets)
                .mapToObj((i) -> Target.builder()
                        .id("id-" + i)
                        .port(i)
                        .build()
                )
                .collect(Collectors.toList());
    }

    public static ListTargetsResponse getListTargetsResponse(final int numberOfTargets) {
        return ListTargetsResponse.builder()
                .items(getTargets(numberOfTargets))
                .build();
    }

    public static List<software.amazon.awssdk.services.vpclattice.model.Target> getTargetSuccesses(
            final int numberOfTargets) {
        return IntStream.range(0, numberOfTargets)
                .mapToObj((i) -> software.amazon.awssdk.services.vpclattice.model.Target.builder()
                        .id("id-" + i)
                        .port(i)
                        .build())
                .collect(Collectors.toList());
    }

    public static List<TargetFailure> getTargetFailures(final int numberOfTargets) {
        return IntStream.range(0, numberOfTargets)
                .mapToObj((i) -> TargetFailure.builder()
                        .id("id-" + i)
                        .port(i)
                        .failureCode(FAILURE_CODE)
                        .failureMessage(FAILURE_MESSAGE)
                        .build())
                .collect(Collectors.toList());
    }

    public static GetTargetGroupResponse getTargetGroup() {
        return getTargetGroup(TargetGroupStatus.ACTIVE);
    }

    public static GetTargetGroupResponse getTargetGroup(TargetGroupStatus status) {
        return GetTargetGroupResponse.builder()
                .id(TARGET_GROUP_ID)
                .arn(TARGET_GROUP_ARN)
                .name(TARGET_GROUP_NAME)
                .config(getMercuryTargetGroupConfig())
                .type(TYPE)
                .status(status)
                .createdAt(new Date().toInstant())
                .lastUpdatedAt(new Date().toInstant())
                .failureMessage("")
                .build();
    }

    protected void mockProxyClient() {
        doReturn(proxyClient).when(proxy).newProxy(any());
    }

    protected void mockSdkReturn(@Nonnull final UpdateTargetGroupResponse response) {
        doReturn(response).when(proxyClient.client()).updateTargetGroup(any(UpdateTargetGroupRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final GetTargetGroupResponse response, @Nonnull final GetTargetGroupResponse... responses) {
        var stubber = doReturn(response);

        for (final var r : responses) {
            stubber = stubber.doReturn(r);
        }

        stubber.when(proxyClient.client()).getTargetGroup(any(GetTargetGroupRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final CreateTargetGroupResponse response) {
        doReturn(response).when(proxyClient.client()).createTargetGroup(any(CreateTargetGroupRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final DeleteTargetGroupResponse response) {
        doReturn(response).when(proxyClient.client()).deleteTargetGroup(any(DeleteTargetGroupRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final ListTargetGroupsResponse response) {
        doReturn(response).when(proxyClient.client()).listTargetGroups(any(ListTargetGroupsRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final ListTargetsResponse response, @Nonnull final ListTargetsResponse... responses) {
        var stubber = doReturn(response);

        for (final var r : responses) {
            stubber = stubber.doReturn(r);
        }

        stubber.when(proxyClient.client()).listTargets(any(ListTargetsRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final RegisterTargetsResponse response, @Nonnull final RegisterTargetsResponse... responses) {
        doReturn(response, (Object[]) responses).when(proxyClient.client()).registerTargets(any(RegisterTargetsRequest.class));
    }

    protected void mockSdkReturn(@Nonnull final DeregisterTargetsResponse response, @Nonnull final DeregisterTargetsResponse... responses) {
        doReturn(response, (Object[]) responses).when(proxyClient.client()).deregisterTargets(any(DeregisterTargetsRequest.class));
    }

    protected void mockSdkGetThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).getTargetGroup(any(GetTargetGroupRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).createTargetGroup(any(CreateTargetGroupRequest.class));
    }

    protected void mockSdkCreateThrow(@Nonnull final Throwable exception) {
        doThrow(exception).when(proxyClient.client()).createTargetGroup(any(CreateTargetGroupRequest.class));
    }

    protected void mockSdkUpdateThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).updateTargetGroup(any(UpdateTargetGroupRequest.class));
    }

    protected void mockSdkDeleteThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).deleteTargetGroup(any(DeleteTargetGroupRequest.class));
    }

    protected void mockSdkListThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).listTargetGroups(any(ListTargetGroupsRequest.class));
    }

    protected void mockSdkListTargetsThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).listTargets(any(ListTargetsRequest.class));
    }

    protected void mockSdkRegisterThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).registerTargets(any(RegisterTargetsRequest.class));
    }

    protected void mockSdkDegisterThrow(@Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).deregisterTargets(any(DeregisterTargetsRequest.class));
    }
}