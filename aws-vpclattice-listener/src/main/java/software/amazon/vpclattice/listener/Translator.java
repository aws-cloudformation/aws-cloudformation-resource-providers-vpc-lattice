package software.amazon.vpclattice.listener;

import software.amazon.awssdk.services.vpclattice.model.CreateListenerRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteListenerRequest;
import software.amazon.awssdk.services.vpclattice.model.FixedResponseAction;
import software.amazon.awssdk.services.vpclattice.model.ForwardAction;
import software.amazon.awssdk.services.vpclattice.model.GetListenerRequest;
import software.amazon.awssdk.services.vpclattice.model.GetListenerResponse;
import software.amazon.awssdk.services.vpclattice.model.ListListenersRequest;
import software.amazon.awssdk.services.vpclattice.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.vpclattice.model.ListenerSummary;
import software.amazon.awssdk.services.vpclattice.model.UpdateListenerRequest;
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
    private static final String LISTENER_ID_PREFIX = "listener-";

    private static final Integer LISTENER_NAME_MAX_LENGTH = 63;

    private Translator() {
        // prevent instantiation
    }

    public static ResourceModel createResourceModel(
            @Nonnull final GetListenerResponse getListenerResponse,
            @Nonnull final ListTagsForResourceResponse listTagsForResourceResponse) {
        return ResourceModel.builder()
                .id(getListenerResponse.id())
                .arn(getListenerResponse.arn())
                .name(getListenerResponse.name())
                .defaultAction(Translator.convertRuleActionFromSdk(getListenerResponse.defaultAction()))
                .serviceId(getListenerResponse.serviceId())
                .serviceArn(getListenerResponse.serviceArn())
                .port(getListenerResponse.port())
                .protocol(getListenerResponse.protocolAsString())
                .tags(TagHelper.convertTagMapToModelTags(listTagsForResourceResponse.tags(), Tag::new))
                .build();
    }

    public static CreateListenerRequest createCreateListenerRequest(
            @Nonnull final ResourceHandlerRequest<ResourceModel> request) {
        final var model = request.getDesiredResourceState();

        if (model.getServiceIdentifier() == null) {
            throw new CfnInvalidRequestException("ServiceIdentifier cannot be empty");
        }

        final var clientToken = request.getClientRequestToken();

        final var tags = TagHelper.mergeTags(
                request.getDesiredResourceTags(),
                request.getSystemTags()
        );

        return CreateListenerRequest.builder()
                .serviceIdentifier(model.getServiceIdentifier())
                .name(Optional.ofNullable(model.getName())
                        .orElse(NameHelper.generateRandomName(request, LISTENER_ID_PREFIX, LISTENER_NAME_MAX_LENGTH)))
                .protocol(model.getProtocol())
                .port(model.getPort())
                .defaultAction(convertRuleActionFromModel(model.getDefaultAction()))
                .tags(tags)
                .clientToken(clientToken)
                .build();
    }

    public static GetListenerRequest createGetListenerRequest(
            @Nonnull final ResourceModel model) {
        populateServiceIdAndListenerId(model);

        return GetListenerRequest.builder()
                .serviceIdentifier(model.getServiceId())
                .listenerIdentifier(model.getId())
                .build();
    }

    public static UpdateListenerRequest createUpdateListenerRequest(
            @Nonnull final ResourceModel model) {
        populateServiceIdAndListenerId(model);

        return UpdateListenerRequest.builder()
                .serviceIdentifier(model.getServiceId())
                .listenerIdentifier(model.getId())
                .defaultAction(convertRuleActionFromModel(model.getDefaultAction()))
                .build();
    }

    public static DeleteListenerRequest createDeleteListenerRequest(
            @Nonnull final ResourceModel model) {
        populateServiceIdAndListenerId(model);

        return DeleteListenerRequest.builder()
                .serviceIdentifier(model.getServiceId())
                .listenerIdentifier(model.getId())
                .build();
    }

    public static ListListenersRequest createListListenersRequest(
            @Nonnull final ResourceModel model,
            @Nullable final String nextToken) {
//        if (model.getServiceIdentifier() == null) {
//            throw new CfnInvalidRequestException("Missing ServiceIdentifier");
//        }

        final var serviceIdentifier = Optional
                .ofNullable(model.getArn())
                .map(ArnHelper::getServiceIdFromListenerArn)
                .orElse(Optional.ofNullable(model.getServiceIdentifier())
                        .orElse(Optional.ofNullable(model.getServiceArn())
                                .orElse(model.getServiceId())));

        return ListListenersRequest.builder()
                .serviceIdentifier(serviceIdentifier)
//                .serviceIdentifier(model.getServiceIdentifier())
                .nextToken(nextToken)
                .build();
    }

    public static software.amazon.awssdk.services.vpclattice.model.RuleAction convertRuleActionFromModel(
            @Nullable final DefaultAction ruleAction) {
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

    private static software.amazon.awssdk.services.vpclattice.model.FixedResponseAction convertFixedResponseActionFromModel(
            @Nullable final FixedResponse fixedResponseAction) {
        if (fixedResponseAction == null) {
            return null;
        }

        return software.amazon.awssdk.services.vpclattice.model.FixedResponseAction.builder()
                .statusCode(fixedResponseAction.getStatusCode())
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

    public static DefaultAction convertRuleActionFromSdk(
            @Nullable final software.amazon.awssdk.services.vpclattice.model.RuleAction ruleAction) {
        if (ruleAction == null) {
            return null;
        }

        return DefaultAction.builder()
                .forward(convertForwardActionFromSdk(ruleAction.forward()))
                .fixedResponse(convertFixedResponseFromSdk(ruleAction.fixedResponse()))
                .build();
    }

    public static Forward convertForwardActionFromSdk(
            @Nullable ForwardAction forwardAction) {
        if (forwardAction == null) {
            return null;
        }

        return Forward.builder()
                .targetGroups(Optional.ofNullable(forwardAction.targetGroups())
                        .orElse(List.of())
                        .stream()
                        .map((weightedTargetGroup -> WeightedTargetGroup.builder()
                                .targetGroupIdentifier(weightedTargetGroup.targetGroupIdentifier())
                                .weight(weightedTargetGroup.weight())
                                .build()))
                        .collect(Collectors.toList()))
                .build();
    }

    public static FixedResponse convertFixedResponseFromSdk(
            @Nullable FixedResponseAction fixedResponseAction) {
        if (fixedResponseAction == null) {
            return null;
        }

        return FixedResponse.builder()
                .statusCode(fixedResponseAction.statusCode())
                .build();
    }

    public static ResourceModel createResourceModel(
            @Nonnull final ListenerSummary summary) {
        return ResourceModel.builder()
                .arn(summary.arn())
                .build();
    }

    private static void populateServiceIdAndListenerId(
            @Nonnull final ResourceModel model) {
        if (model.getPrimaryIdentifier() == null) {
            throw new CfnInvalidRequestException("Required Arn can't be empty");
        }

        try {
            model.setId(ArnHelper.getListenerIdFromListenerArn(model.getArn()));
            model.setServiceId(ArnHelper.getServiceIdFromListenerArn(model.getArn()));
        } catch (IllegalArgumentException e) {
            throw new CfnInvalidRequestException(e);
        }
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
