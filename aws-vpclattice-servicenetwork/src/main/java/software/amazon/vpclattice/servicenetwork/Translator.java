package software.amazon.vpclattice.servicenetwork;

import software.amazon.awssdk.services.vpclattice.model.CreateServiceNetworkRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteServiceNetworkRequest;
import software.amazon.awssdk.services.vpclattice.model.GetServiceNetworkRequest;
import software.amazon.awssdk.services.vpclattice.model.GetServiceNetworkResponse;
import software.amazon.awssdk.services.vpclattice.model.ListServiceNetworksRequest;
import software.amazon.awssdk.services.vpclattice.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceNetworkSummary;
import software.amazon.awssdk.services.vpclattice.model.UpdateServiceNetworkRequest;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.NameHelper;
import software.amazon.vpclattice.common.TagHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Optional;

public class Translator {
    private static final Integer SERVICE_NETWORK_NAM_MAX_LENGTH = 63;

    private static final String FORBIDDEN_PREFIX = "servicenetwork-";

    private Translator() {
        //prevent instantiation
    }

    public static ResourceModel createResourceModel(
            @Nonnull final GetServiceNetworkResponse getServiceNetworkResponse,
            @Nonnull final ListTagsForResourceResponse listTagsResponse) {
        return ResourceModel.builder()
                .id(getServiceNetworkResponse.id())
                .arn(getServiceNetworkResponse.arn())
                .name(getServiceNetworkResponse.name())
                .createdAt(Optional.ofNullable(getServiceNetworkResponse.createdAt()).map(Instant::toString).orElse(null))
                .lastUpdatedAt(Optional.ofNullable(getServiceNetworkResponse.lastUpdatedAt()).map(Instant::toString).orElse(null))
                .authType(getServiceNetworkResponse.authTypeAsString())
                .tags(TagHelper.convertTagMapToModelTags(listTagsResponse.tags(), Tag::new))
                .build();
    }

    public static CreateServiceNetworkRequest createCreateServiceNetworkRequest(
            @Nonnull final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        final var model = handlerRequest.getDesiredResourceState();

        final var clientToken = handlerRequest.getClientRequestToken();

        final var tags = TagHelper.mergeTags(
                handlerRequest.getDesiredResourceTags(),
                handlerRequest.getSystemTags()
        );

        return CreateServiceNetworkRequest.builder()
                .name(Optional.ofNullable(model.getName())
                        .orElse(NameHelper.generateRandomName(handlerRequest, FORBIDDEN_PREFIX, SERVICE_NETWORK_NAM_MAX_LENGTH)))
                .authType(model.getAuthType())
                .tags(tags)
                .clientToken(clientToken)
                .build();
    }

    public static GetServiceNetworkRequest createGetServiceNetworkRequest(
            @Nonnull final ResourceModel model) {
        return GetServiceNetworkRequest.builder()
                .serviceNetworkIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .build();
    }

    public static UpdateServiceNetworkRequest createUpdateServiceNetworkRequest(
            @Nonnull final ResourceModel model) {
        return UpdateServiceNetworkRequest.builder()
                .serviceNetworkIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .authType(model.getAuthType())
                .build();
    }

    public static DeleteServiceNetworkRequest createDeleteServiceNetworkRequest(
            @Nonnull final ResourceModel model) {
        return DeleteServiceNetworkRequest.builder()
                .serviceNetworkIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .build();
    }

    public static ListServiceNetworksRequest createListServiceNetworksRequest(
            @Nullable final String nextToken) {
        return ListServiceNetworksRequest.builder()
                .nextToken(nextToken)
                .build();
    }

    public static ResourceModel createResourceModel(
            @Nonnull final ServiceNetworkSummary summary) {
        return ResourceModel.builder()
                .arn(summary.arn())
                .build();
    }
}
