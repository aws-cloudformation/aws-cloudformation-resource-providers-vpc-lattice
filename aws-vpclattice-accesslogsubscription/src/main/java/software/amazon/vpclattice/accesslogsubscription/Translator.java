package software.amazon.vpclattice.accesslogsubscription;

import software.amazon.awssdk.services.vpclattice.model.AccessLogSubscriptionSummary;
import software.amazon.awssdk.services.vpclattice.model.CreateAccessLogSubscriptionRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteAccessLogSubscriptionRequest;
import software.amazon.awssdk.services.vpclattice.model.GetAccessLogSubscriptionRequest;
import software.amazon.awssdk.services.vpclattice.model.GetAccessLogSubscriptionResponse;
import software.amazon.awssdk.services.vpclattice.model.ListAccessLogSubscriptionsRequest;
import software.amazon.awssdk.services.vpclattice.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.vpclattice.model.UpdateAccessLogSubscriptionRequest;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.TagHelper;

import javax.annotation.Nonnull;
import java.util.Optional;

public class Translator {
    private Translator() {
        //prevent instantiation
    }

    public static ResourceModel createResourceModel(
            @Nonnull final GetAccessLogSubscriptionResponse getAccessLogSubscriptionResponse,
            @Nonnull final ListTagsForResourceResponse listTagsResponse) {
        return ResourceModel.builder()
                .arn(getAccessLogSubscriptionResponse.arn())
                .id(getAccessLogSubscriptionResponse.id())
                .destinationArn(getAccessLogSubscriptionResponse.destinationArn())
                .resourceArn(getAccessLogSubscriptionResponse.resourceArn())
                .resourceId(getAccessLogSubscriptionResponse.resourceId())
                .tags(TagHelper.convertTagMapToModelTags(listTagsResponse.tags(), Tag::new))
                .build();
    }

    public static CreateAccessLogSubscriptionRequest createCreateAccessLogSubscriptionRequest(
            @Nonnull final ResourceHandlerRequest<ResourceModel> request) {
        final var model = request.getDesiredResourceState();

        if (model.getResourceIdentifier() == null) {
            throw new CfnInvalidRequestException("ResourceIdentifier cannot be empty");
        }

        final var tags = TagHelper.mergeTags(
                request.getDesiredResourceTags(),
                request.getSystemTags()
        );

        final var clientToken = request.getClientRequestToken();

        return CreateAccessLogSubscriptionRequest.builder()
                .resourceIdentifier(model.getResourceIdentifier())
                .destinationArn(model.getDestinationArn())
                .tags(tags)
                .clientToken(clientToken)
                .build();
    }

    static GetAccessLogSubscriptionRequest createGetAccessLogSubscriptionRequest(
            @Nonnull final ResourceModel model) {
        return GetAccessLogSubscriptionRequest.builder()
                .accessLogSubscriptionIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .build();
    }

    static DeleteAccessLogSubscriptionRequest createDeleteAccessLogSubscriptionRequest(
            @Nonnull final ResourceModel model) {
        return DeleteAccessLogSubscriptionRequest.builder()
                .accessLogSubscriptionIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .build();
    }

    static UpdateAccessLogSubscriptionRequest createUpdateAccessLogSubscriptionRequest(
            @Nonnull final ResourceModel model) {
        return UpdateAccessLogSubscriptionRequest.builder()
                .accessLogSubscriptionIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .destinationArn(model.getDestinationArn())
                .build();
    }

    static ListAccessLogSubscriptionsRequest createListAccessLogSubscriptionsRequest(final ResourceModel model, final String nextToken) {
        if (model.getResourceIdentifier() == null) {
            throw new CfnInvalidRequestException("Missing resourceIdentifier");
        }

        return ListAccessLogSubscriptionsRequest.builder()
                .resourceIdentifier(model.getResourceIdentifier())
                .nextToken(nextToken)
                .build();
    }

    static ResourceModel createResourceModel(final AccessLogSubscriptionSummary summary) {
        return ResourceModel.builder()
                .arn(summary.arn())
                .build();
    }
}