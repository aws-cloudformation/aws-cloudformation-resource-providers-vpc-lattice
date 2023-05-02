package software.amazon.vpclattice.targetgroup;

import software.amazon.awssdk.services.vpclattice.model.CreateTargetGroupRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteTargetGroupRequest;
import software.amazon.awssdk.services.vpclattice.model.DeregisterTargetsRequest;
import software.amazon.awssdk.services.vpclattice.model.GetTargetGroupRequest;
import software.amazon.awssdk.services.vpclattice.model.GetTargetGroupResponse;
import software.amazon.awssdk.services.vpclattice.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.vpclattice.model.ListTargetGroupsRequest;
import software.amazon.awssdk.services.vpclattice.model.ListTargetsRequest;
import software.amazon.awssdk.services.vpclattice.model.ListTargetsResponse;
import software.amazon.awssdk.services.vpclattice.model.RegisterTargetsRequest;
import software.amazon.awssdk.services.vpclattice.model.TargetGroupSummary;
import software.amazon.awssdk.services.vpclattice.model.TargetSummary;
import software.amazon.awssdk.services.vpclattice.model.UpdateTargetGroupRequest;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.NameHelper;
import software.amazon.vpclattice.common.TagHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class Translator {
    public static final String TARGET_GROUP_ID_PREFIX = "tg-";

    public static final Integer TARGET_GROUP_NAME_MAX_LENGTH = 128;

    private Translator() {
    }

    public static ResourceModel createResourceModel(
            @Nonnull final GetTargetGroupResponse getTargetGroupResponse,
            @Nonnull final ListTagsForResourceResponse listTagsForResourceResponse,
            @Nonnull final ListTargetsResponse listTargetsResponse) {
        return ResourceModel.builder()
                .arn(getTargetGroupResponse.arn())
                .id(getTargetGroupResponse.id())
                .type(getTargetGroupResponse.typeAsString())
                .name(getTargetGroupResponse.name())
                .status(getTargetGroupResponse.statusAsString())
                .lastUpdatedAt(getTargetGroupResponse.lastUpdatedAt().toString())
                .createdAt(getTargetGroupResponse.createdAt().toString())
                .config(convertTargetGroupConfigFromSdk(getTargetGroupResponse.config()))
                .tags(TagHelper.convertTagMapToModelTags(listTagsForResourceResponse.tags(), Tag::new))
                .targets(Optional.ofNullable(listTargetsResponse.items())
                        .orElse(List.of())
                        .stream()
                        .map(Translator::convertTargetFromSdk)
                        .collect(Collectors.toList()))
                .build();
    }

    public static CreateTargetGroupRequest createCreateTargetGroupRequest(
            @Nonnull final ResourceHandlerRequest<ResourceModel> request) {
        final var model = request.getDesiredResourceState();

        final var tags = TagHelper.mergeTags(request.getSystemTags(), request.getDesiredResourceTags());

        final var clientToken = request.getClientRequestToken();

        return CreateTargetGroupRequest.builder()
                .clientToken(clientToken)
                .name(Optional.ofNullable(model.getName())
                        .orElse(NameHelper.generateRandomName(request, TARGET_GROUP_ID_PREFIX, TARGET_GROUP_NAME_MAX_LENGTH)))
                .type(model.getType())
                .config(convertTargetGroupConfigFromModel(model.getConfig()))
                .tags(tags)
                .build();
    }

    public static GetTargetGroupRequest createGetTargetGroupRequest(
            @Nonnull ResourceModel model) {
        return GetTargetGroupRequest.builder()
                .targetGroupIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .build();
    }

    public static ListTargetGroupsRequest createListTargetGroupsRequest(
            @Nullable String nextToken) {
        return ListTargetGroupsRequest.builder()
                .nextToken(nextToken)
                .build();
    }

    public static DeleteTargetGroupRequest createDeleteTargetGroupRequest(
            @Nonnull ResourceModel model) {
        return DeleteTargetGroupRequest.builder()
                .targetGroupIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .build();
    }

    public static UpdateTargetGroupRequest createUpdateTargetGroupRequest(
            @Nonnull ResourceModel model) {
        return UpdateTargetGroupRequest.builder()
                .targetGroupIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .healthCheck(convertHealthCheckConfigFromModel(
                        Optional.ofNullable(model.getConfig())
                                .map(software.amazon.vpclattice.targetgroup.TargetGroupConfig::getHealthCheck)
                                .orElse(null))
                )
                .build();
    }

    public static ListTargetsRequest createListTargetsRequest(
            @Nonnull final ResourceModel model) {
        return ListTargetsRequest.builder()
                .targetGroupIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .build();
    }

    public static RegisterTargetsRequest createRegisterTargetsRequest(
            @Nonnull final ResourceModel model) {
        return RegisterTargetsRequest.builder()
                .targetGroupIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .targets(Optional.ofNullable(model.getTargets())
                        .orElse(List.of())
                        .stream()
                        .map(Translator::convertTargetFromModel)
                        .collect(Collectors.toList()))
                .build();
    }

    public static DeregisterTargetsRequest createDeregisterTargetsRequest(
            @Nonnull final ResourceModel model) {
        return DeregisterTargetsRequest.builder()
                .targetGroupIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .targets(model.getTargets()
                        .stream()
                        .map(Translator::convertTargetFromModel)
                        .collect(Collectors.toList()))
                .build();
    }

    public static ResourceModel createResourceModel(final TargetGroupSummary summary) {
        return ResourceModel.builder()
                .arn(summary.arn())
                .build();
    }

    // Convert Sdk TargetGroupConfig to a CFN model TargetGroupConfig
    public static software.amazon.vpclattice.targetgroup.TargetGroupConfig convertTargetGroupConfigFromSdk(
            software.amazon.awssdk.services.vpclattice.model.TargetGroupConfig sdkTargetGroupConfig) {

        if (sdkTargetGroupConfig == null) {
            return null;
        }

        return software.amazon.vpclattice.targetgroup.TargetGroupConfig.builder()
                .port(sdkTargetGroupConfig.port())
                .protocol(sdkTargetGroupConfig.protocolAsString())
                .protocolVersion(sdkTargetGroupConfig.protocolVersionAsString())
                .vpcIdentifier(sdkTargetGroupConfig.vpcIdentifier())
                .ipAddressType(sdkTargetGroupConfig.ipAddressTypeAsString())
                .healthCheck(convertHealthCheckConfigFromSdk(sdkTargetGroupConfig.healthCheck()))
                .build();
    }

    // Converts the CFN model TargetGroupConfig to a Sdk TargetGroupConfig
    public static software.amazon.awssdk.services.vpclattice.model.TargetGroupConfig convertTargetGroupConfigFromModel(
            software.amazon.vpclattice.targetgroup.TargetGroupConfig modelConfig) {
        if (modelConfig == null) {
            return null;
        }

        return software.amazon.awssdk.services.vpclattice.model.TargetGroupConfig.builder()
                .port(modelConfig.getPort())
                .protocol(modelConfig.getProtocol())
                .protocolVersion(modelConfig.getProtocolVersion())
                .vpcIdentifier(modelConfig.getVpcIdentifier())
                .ipAddressType(modelConfig.getIpAddressType())
                .healthCheck(convertHealthCheckConfigFromModel(modelConfig.getHealthCheck()))
                .build();
    }

    public static Target convertTargetFromSdk(
            @Nullable TargetSummary target) {
        if (target == null) {
            return null;
        }

        return Target.builder()
                .id(target.id())
                .port(target.port())
                .build();
    }

    public static software.amazon.awssdk.services.vpclattice.model.Target convertTargetFromModel(
            @Nullable Target target) {
        if (target == null) {
            return null;
        }

        return software.amazon.awssdk.services.vpclattice.model.Target.builder()
                .id(target.getId())
                .port(target.getPort())
                .build();
    }

    // Convert sdk HealthCheckConfig to a CFN model HealthCheckConfig
    private static HealthCheckConfig convertHealthCheckConfigFromSdk(
            software.amazon.awssdk.services.vpclattice.model.HealthCheckConfig sdkHealthCheckConfig) {

        if (sdkHealthCheckConfig == null) {
            return null;
        }

        return HealthCheckConfig.builder()
                .enabled(sdkHealthCheckConfig.enabled())
                .protocol(sdkHealthCheckConfig.protocolAsString())
                .protocolVersion(sdkHealthCheckConfig.protocolVersionAsString())
                .port(sdkHealthCheckConfig.port())
                .path(sdkHealthCheckConfig.path())
                .healthCheckIntervalSeconds(sdkHealthCheckConfig.healthCheckIntervalSeconds())
                .healthCheckTimeoutSeconds(sdkHealthCheckConfig.healthCheckTimeoutSeconds())
                .healthyThresholdCount(sdkHealthCheckConfig.healthyThresholdCount())
                .unhealthyThresholdCount(sdkHealthCheckConfig.unhealthyThresholdCount())
                .matcher(convertMatcherFromSdk(sdkHealthCheckConfig.matcher()))
                .build();
    }

    // Converts the CFN model HealthCheckConfig to a Sdk HealthCheckConfig
    private static software.amazon.awssdk.services.vpclattice.model.HealthCheckConfig convertHealthCheckConfigFromModel(
            software.amazon.vpclattice.targetgroup.HealthCheckConfig modelHealthCheckConfig) {
        if (modelHealthCheckConfig == null) {
            return null;
        }

        return software.amazon.awssdk.services.vpclattice.model.HealthCheckConfig.builder()
                .enabled(modelHealthCheckConfig.getEnabled())
                .protocol(modelHealthCheckConfig.getProtocol())
                .protocolVersion(modelHealthCheckConfig.getProtocolVersion())
                .port(modelHealthCheckConfig.getPort())
                .path(modelHealthCheckConfig.getPath())
                .healthCheckIntervalSeconds(modelHealthCheckConfig.getHealthCheckIntervalSeconds())
                .healthCheckTimeoutSeconds(modelHealthCheckConfig.getHealthCheckTimeoutSeconds())
                .healthyThresholdCount(modelHealthCheckConfig.getHealthyThresholdCount())
                .unhealthyThresholdCount(modelHealthCheckConfig.getUnhealthyThresholdCount())
                .matcher(convertMatcherFromModel(modelHealthCheckConfig.getMatcher()))
                .build();
    }

    private static Matcher convertMatcherFromSdk(
            @Nullable software.amazon.awssdk.services.vpclattice.model.Matcher matcher) {
        if (matcher == null) {
            return null;
        }

        return Matcher.builder()
                .httpCode(matcher.httpCode())
                .build();
    }

    private static software.amazon.awssdk.services.vpclattice.model.Matcher convertMatcherFromModel(
            @Nullable Matcher matcher) {
        if (matcher == null) {
            return null;
        }

        return software.amazon.awssdk.services.vpclattice.model.Matcher.builder()
                .httpCode(matcher.getHttpCode())
                .build();
    }
}