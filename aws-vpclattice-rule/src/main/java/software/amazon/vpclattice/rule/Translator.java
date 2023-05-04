package software.amazon.vpclattice.rule;

import software.amazon.awssdk.services.vpclattice.model.CreateRuleRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteRuleRequest;
import software.amazon.awssdk.services.vpclattice.model.GetRuleRequest;
import software.amazon.awssdk.services.vpclattice.model.GetRuleResponse;
import software.amazon.awssdk.services.vpclattice.model.ListRulesRequest;
import software.amazon.awssdk.services.vpclattice.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.vpclattice.model.RuleSummary;
import software.amazon.awssdk.services.vpclattice.model.UpdateRuleRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ArnHelper;
import software.amazon.vpclattice.common.NameHelper;
import software.amazon.vpclattice.common.TagHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Translator {
    private static final String RULE_ID_PREFIX = "rule-";

    private static final Integer RULE_NAME_MAX_LENGTH = 63;

    private Translator() {
        // prevent instantiation
    }

    public static ResourceModel createResourceModel(
            @Nonnull final GetRuleResponse getRuleResponse,
            @Nonnull final ListTagsForResourceResponse listTagsForResourceResponse) {
        return ResourceModel.builder()
                .id(getRuleResponse.id())
                .arn(getRuleResponse.arn())
                .name(getRuleResponse.name())
                .priority(getRuleResponse.priority())
                .action(Translator.convertRuleActionFromSdk(getRuleResponse.action()))
                .match(Translator.convertRuleMatchFromSdk(getRuleResponse.match()))
                .tags(TagHelper.convertTagMapToModelTags(listTagsForResourceResponse.tags(), Tag::new))
                .build();
    }

    public static CreateRuleRequest createCreateRuleRequest(
            @Nonnull final ResourceHandlerRequest<ResourceModel> request) {
        final var model = request.getDesiredResourceState();

        if (model.getServiceIdentifier() == null) {
            throw new CfnInvalidRequestException("ServiceIdentifier cannot be empty");
        }

        if (model.getListenerIdentifier() == null) {
            throw new CfnInvalidRequestException("ListenerIdentifier cannot be empty");
        }

        final var stackLevelTags = request.getDesiredResourceTags();

        final var clientToken = request.getClientRequestToken();

        final var tags = TagHelper.mergeTags(
                stackLevelTags,
                TagHelper.convertModelTagsToTagMap(model.getTags(), Tag::getKey, Tag::getValue)
        );

        return CreateRuleRequest.builder()
                .serviceIdentifier(model.getServiceIdentifier())
                .listenerIdentifier(model.getListenerIdentifier())
                .name(Optional.ofNullable(model.getName())
                        .orElse(NameHelper.generateRandomName(request, RULE_ID_PREFIX, RULE_NAME_MAX_LENGTH)))
                .match(convertRuleMatchFromModel(model.getMatch()))
                .priority(model.getPriority())
                .action(convertRuleActionFromModel(model.getAction()))
                .tags(tags)
                .clientToken(clientToken)
                .build();
    }

    public static GetRuleRequest createGetRuleRequest(
            @Nonnull final ResourceModel model) {
        if (model.getArn() == null) {
            throw new CfnInvalidRequestException("Required Arn can't be empty");
        }

        return GetRuleRequest.builder()
                .serviceIdentifier(ArnHelper.getServiceIdFromRuleArn(model.getArn()))
                .listenerIdentifier(ArnHelper.getListenerIdFromRuleArn(model.getArn()))
                .ruleIdentifier(model.getArn())
                .build();
    }

    public static UpdateRuleRequest createUpdateRuleRequest(
            @Nonnull final ResourceModel model) {
        if (model.getArn() == null) {
            throw new CfnInvalidRequestException("Required Arn can't be empty");
        }

        return UpdateRuleRequest.builder()
                .serviceIdentifier(ArnHelper.getServiceIdFromRuleArn(model.getArn()))
                .listenerIdentifier(ArnHelper.getListenerIdFromRuleArn(model.getArn()))
                .ruleIdentifier(model.getArn())
                .match(convertRuleMatchFromModel(model.getMatch()))
                .priority(model.getPriority())
                .action(convertRuleActionFromModel(model.getAction()))
                .build();
    }

    public static DeleteRuleRequest createDeleteRuleRequest(
            @Nonnull final ResourceModel model) {
        if (model.getArn() == null) {
            throw new CfnInvalidRequestException("Required Arn can't be empty");
        }

        return DeleteRuleRequest.builder()
                .serviceIdentifier(ArnHelper.getServiceIdFromRuleArn(model.getArn()))
                .listenerIdentifier(ArnHelper.getListenerIdFromRuleArn(model.getArn()))
                .ruleIdentifier(model.getArn())
                .build();
    }

    public static ListRulesRequest createListRulesRequest(
            @Nonnull final ResourceModel model,
            @Nullable final String nextToken) {
        if (model.getServiceIdentifier() == null) {
            throw new CfnInvalidRequestException("Missing ServiceIdentifier");
        }

        if (model.getListenerIdentifier() == null) {
            throw new CfnInvalidRequestException("Missing ListenerIdentifier");
        }

        return ListRulesRequest.builder()
                .serviceIdentifier(model.getServiceIdentifier())
                .listenerIdentifier(model.getListenerIdentifier())
                .nextToken(nextToken)
                .build();
    }

    public static software.amazon.awssdk.services.vpclattice.model.RuleMatch convertRuleMatchFromModel(
            @Nullable final Match ruleMatch) {
        if (ruleMatch == null) {
            return null;
        }

        return software.amazon.awssdk.services.vpclattice.model.RuleMatch.builder()
                .httpMatch(convertHTTPMatchFromModel(ruleMatch.getHttpMatch()))
                .build();
    }

    public static Match convertRuleMatchFromSdk(
            @Nullable final software.amazon.awssdk.services.vpclattice.model.RuleMatch ruleMatch) {
        if (ruleMatch == null) {
            return null;
        }

        return Match.builder()
                .httpMatch(convertHTTPMatchFromSdk(ruleMatch.httpMatch()))
                .build();
    }

    private static software.amazon.awssdk.services.vpclattice.model.HttpMatch convertHTTPMatchFromModel(
            @Nullable final HttpMatch httpMatch) {
        if (httpMatch == null) {
            return null;
        }

        return software.amazon.awssdk.services.vpclattice.model.HttpMatch.builder()
                .headerMatches(Optional
                        .ofNullable(httpMatch.getHeaderMatches())
                        .map(List::stream)
                        .map((stream) -> stream.map(Translator::convertHeaderMatchFromModel))
                        .map((stream) -> stream.collect(Collectors.toList()))
                        .orElse(null))
                .method(httpMatch.getMethod())
                .pathMatch(convertPathMatchFromModel(httpMatch.getPathMatch()))
                .build();
    }

    private static HttpMatch convertHTTPMatchFromSdk(
            @Nullable final software.amazon.awssdk.services.vpclattice.model.HttpMatch httpMatch) {
        if (httpMatch == null) {
            return null;
        }

        return HttpMatch.builder()
                .headerMatches(Optional.ofNullable(httpMatch.headerMatches())
                        .orElse(List.of())
                        .stream()
                        .map(Translator::convertHeaderMatchFromSdk)
                        .collect(Collectors.toList()))
                .pathMatch(convertPathMatchFromSdk(httpMatch.pathMatch()))
                .method(httpMatch.method())
                .build();
    }

    private static software.amazon.awssdk.services.vpclattice.model.HeaderMatch convertHeaderMatchFromModel(
            @Nullable final HeaderMatch headerMatch) {
        if (headerMatch == null) {
            return null;
        }

        return software.amazon.awssdk.services.vpclattice.model.HeaderMatch.builder()
                .match(convertHeaderMatchTypeFromModel(headerMatch.getMatch()))
                .name(headerMatch.getName())
                .caseSensitive(headerMatch.getCaseSensitive())
                .build();
    }

    private static HeaderMatch convertHeaderMatchFromSdk(
            @Nullable final software.amazon.awssdk.services.vpclattice.model.HeaderMatch headerMatch) {
        if (headerMatch == null) {
            return null;
        }

        return HeaderMatch.builder()
                .name(headerMatch.name())
                .match(convertHeaderMatchTypeFromSdk(headerMatch.match()))
                .caseSensitive(headerMatch.caseSensitive())
                .build();
    }

    private static software.amazon.awssdk.services.vpclattice.model.HeaderMatchType convertHeaderMatchTypeFromModel(
            @Nullable final HeaderMatchType headerMatchType) throws CfnInvalidRequestException {
        if (headerMatchType == null) {
            return null;
        }

        if (!areLessThanOneValueNonNull(headerMatchType.getContains(),
                headerMatchType.getExact(),
                headerMatchType.getPrefix())) {
            throw new CfnInvalidRequestException("Invalid number of parameters set. Can only set one of the following keys: Contains, Exact, Prefix");
        }

        return software.amazon.awssdk.services.vpclattice.model.HeaderMatchType.builder()
                .contains(headerMatchType.getContains())
                .exact(headerMatchType.getExact())
                .prefix(headerMatchType.getPrefix())
                .build();
    }

    private static HeaderMatchType convertHeaderMatchTypeFromSdk(
            @Nullable final software.amazon.awssdk.services.vpclattice.model.HeaderMatchType headerMatchType) {
        if (headerMatchType == null) {
            return null;
        }

        return HeaderMatchType.builder()
                .contains(headerMatchType.contains())
                .exact(headerMatchType.exact())
                .prefix(headerMatchType.prefix())
                .build();
    }

    private static software.amazon.awssdk.services.vpclattice.model.PathMatch convertPathMatchFromModel(
            @Nullable final PathMatch pathMatch) {
        if (pathMatch == null) {
            return null;
        }

        return software.amazon.awssdk.services.vpclattice.model.PathMatch.builder()
                .caseSensitive(pathMatch.getCaseSensitive())
                .match(convertPathMatchTypeFromModel(pathMatch.getMatch()))
                .build();
    }

    private static PathMatch convertPathMatchFromSdk(
            @Nullable final software.amazon.awssdk.services.vpclattice.model.PathMatch pathMatch) {
        if (pathMatch == null) {
            return null;
        }

        return PathMatch.builder()
                .match(convertPathMatchTypeFromSdk(pathMatch.match()))
                .caseSensitive(pathMatch.caseSensitive())
                .build();
    }

    private static software.amazon.awssdk.services.vpclattice.model.PathMatchType convertPathMatchTypeFromModel(
            @Nullable final PathMatchType pathMatchType) throws CfnInvalidRequestException {
        if (pathMatchType == null) {
            return null;
        }

        if (!areLessThanOneValueNonNull(pathMatchType.getExact(), pathMatchType.getPrefix())) {
            throw new CfnInvalidRequestException("Invalid number of parameters set. Can only set one of the following keys: Exact, Prefix");
        }

        return software.amazon.awssdk.services.vpclattice.model.PathMatchType.builder()
                .exact(pathMatchType.getExact())
                .prefix(pathMatchType.getPrefix())
                .build();
    }

    private static PathMatchType convertPathMatchTypeFromSdk(
            @Nullable final software.amazon.awssdk.services.vpclattice.model.PathMatchType pathMatchType) {
        if (pathMatchType == null) {
            return null;
        }

        return PathMatchType.builder()
                .exact(pathMatchType.exact())
                .prefix(pathMatchType.prefix())
                .build();
    }

    public static software.amazon.awssdk.services.vpclattice.model.RuleAction convertRuleActionFromModel(
            @Nullable final Action ruleAction) {
        if (ruleAction == null) {
            return null;
        }

        if (!areLessThanOneValueNonNull(ruleAction.getForward(), ruleAction.getFixedResponse())) {
            throw new CfnInvalidRequestException("Invalid number of parameters set. Can only set one of the following keys: Forward, FixedResponse");
        }

        return software.amazon.awssdk.services.vpclattice.model.RuleAction.builder()
                .forward(convertForwardActionFromModel(ruleAction.getForward()))
                .fixedResponse(convertFixedResponseActionFromModel(ruleAction.getFixedResponse()))
                .build();
    }

    public static Action convertRuleActionFromSdk(
            @Nullable final software.amazon.awssdk.services.vpclattice.model.RuleAction ruleAction) {
        if (ruleAction == null) {
            return null;
        }

        return Action.builder()
                .forward(convertForwardActionFromSdk(ruleAction.forward()))
                .fixedResponse(convertFixedResponseActionFromSdk(ruleAction.fixedResponse()))
                .build();
    }

    private static software.amazon.awssdk.services.vpclattice.model.ForwardAction convertForwardActionFromModel(
            @Nullable final Forward forwardAction) {
        if (forwardAction == null) {
            return null;
        }

        return software.amazon.awssdk.services.vpclattice.model.ForwardAction.builder()
                .targetGroups(Optional
                        .ofNullable(forwardAction.getTargetGroups())
                        .orElse(List.of())
                        .stream()
                        .map(Translator::convertWeightedTargetGroupFromModel)
                        .collect(Collectors.toList()))
                .build();
    }

    private static Forward convertForwardActionFromSdk(
            @Nullable final software.amazon.awssdk.services.vpclattice.model.ForwardAction forwardAction) {
        if (forwardAction == null) {
            return null;
        }

        return Forward.builder()
                .targetGroups(Optional.ofNullable(forwardAction.targetGroups())
                        .orElse(List.of())
                        .stream()
                        .map(Translator::convertWeightedTargetGroupFromSdk)
                        .collect(Collectors.toList()))
                .build();
    }

    private static software.amazon.awssdk.services.vpclattice.model.FixedResponseAction convertFixedResponseActionFromModel(
            @Nullable final FixedResponse fixedResponseAction) {
        if (fixedResponseAction == null) {
            return null;
        }

        return software.amazon.awssdk.services.vpclattice.model.FixedResponseAction.builder()
                .statusCode(fixedResponseAction.getStatusCode())
                .build();
    }

    private static FixedResponse convertFixedResponseActionFromSdk(
            @Nullable software.amazon.awssdk.services.vpclattice.model.FixedResponseAction fixedResponseAction) {
        if (fixedResponseAction == null) {
            return null;
        }

        return FixedResponse.builder()
                .statusCode(fixedResponseAction.statusCode())
                .build();
    }

    private static software.amazon.awssdk.services.vpclattice.model.WeightedTargetGroup convertWeightedTargetGroupFromModel(
            @Nullable final WeightedTargetGroup weightedTargetGroup) {
        if (weightedTargetGroup == null) {
            return null;
        }

        return software.amazon.awssdk.services.vpclattice.model.WeightedTargetGroup.builder()
                .targetGroupIdentifier(weightedTargetGroup.getTargetGroupIdentifier())
                .weight(weightedTargetGroup.getWeight())
                .build();
    }

    private static WeightedTargetGroup convertWeightedTargetGroupFromSdk(
            @Nullable final software.amazon.awssdk.services.vpclattice.model.WeightedTargetGroup weightedTargetGroup) {
        if (weightedTargetGroup == null) {
            return null;
        }

        return WeightedTargetGroup.builder()
                .targetGroupIdentifier(weightedTargetGroup.targetGroupIdentifier())
                .weight(weightedTargetGroup.weight())
                .build();
    }

    public static ResourceModel createResourceModel(
            @Nonnull final RuleSummary summary) {
        return ResourceModel.builder()
                .arn(summary.arn())
                .build();
    }

    private static boolean areLessThanOneValueNonNull(
            @Nullable Object value1, @Nullable Object value2, Object... values) {
        final var nonNullCount = Stream
                .concat(Stream.of(value1, value2), Arrays.stream(values))
                .filter(Objects::nonNull)
                .count();

        return nonNullCount <= 1L;
    }
}
