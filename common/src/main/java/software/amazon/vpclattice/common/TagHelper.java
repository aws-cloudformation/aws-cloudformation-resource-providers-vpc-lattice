package software.amazon.vpclattice.common;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import lombok.Builder;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.AccessDeniedException;
import software.amazon.awssdk.services.vpclattice.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.vpclattice.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.vpclattice.model.TagResourceRequest;
import software.amazon.awssdk.services.vpclattice.model.TagResourceResponse;
import software.amazon.awssdk.services.vpclattice.model.UntagResourceRequest;
import software.amazon.awssdk.services.vpclattice.model.UntagResourceResponse;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TagHelper {
    private TagHelper() {
    }

    @Builder
    public static class UpdateTagsServiceCall<ModelT, TagT> {
        public final Function<ModelT, String> arnProvider;

        public final BiFunction<String, String, TagT> tagConstructor;

        public final Function<TagT, String> tagKeyProvider;

        public final Function<TagT, String> tagValueProvider;

        public final ResourceHandlerRequest<? extends ModelT> request;
    }

    public static <ModelT, TagT> BiFunction<ModelT, ProxyClient<VpcLatticeClient>, Void>
    updateTagsServiceCall(@Nonnull final UpdateTagsServiceCall<ModelT, TagT> updateTagsServiceCallBuilder) {
        final var arnProvider = updateTagsServiceCallBuilder.arnProvider;

        final var tagConstructor = updateTagsServiceCallBuilder.tagConstructor;

        final var request = updateTagsServiceCallBuilder.request;

        final var tagKeyProvider = updateTagsServiceCallBuilder.tagKeyProvider;

        final var tagValueProvider = updateTagsServiceCallBuilder.tagValueProvider;

        return (model, proxyClient) -> {
            final var listTagsResponse = TagHelper.listTags(arnProvider.apply(model), proxyClient);

            final var previousTags = TagHelper.convertTagMapToModelTags(listTagsResponse.tags(), tagConstructor);

            final var currentTags = TagHelper.convertTagMapToModelTags(
                    TagHelper.mergeTags(request.getDesiredResourceTags(), request.getSystemTags()),
                    tagConstructor);

            final var tagsToRemove = TagHelper.convertModelTagsToTagMap(
                    Sets.difference(previousTags, currentTags),
                    tagKeyProvider,
                    tagValueProvider
            );

            final var tagsToPut = TagHelper.convertModelTagsToTagMap(
                    Sets.difference(currentTags, previousTags),
                    tagKeyProvider,
                    tagValueProvider
            );

            TagHelper.updateTags(proxyClient, arnProvider.apply(model), tagsToRemove, tagsToPut);

            return null;
        };
    }

    public static ListTagsForResourceResponse listTags(
            @Nonnull final String arn,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient) {
        try {
            final var request = ListTagsForResourceRequest.builder()
                    .resourceArn(arn)
                    .build();

            return proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::listTagsForResource);
        } catch (AccessDeniedException e) {
            return ListTagsForResourceResponse.builder()
                    .tags(Map.of())
                    .build();
        }
    }

    public static void updateTags(
            @Nonnull final ProxyClient<software.amazon.awssdk.services.vpclattice.VpcLatticeClient> proxyClient,
            @Nonnull final String resourceArn,
            @Nullable final Map<String, String> tagsToRemove,
            @Nullable final Map<String, String> tagsToPut) {
        if (tagsToRemove != null && !tagsToRemove.isEmpty()) {
            untagResource(proxyClient, resourceArn, tagsToRemove.keySet());
        }

        if (tagsToPut != null && !tagsToPut.isEmpty()) {
            tagResource(proxyClient, resourceArn, tagsToPut);
        }
    }

    public static <Tag> Map<String, String> convertModelTagsToTagMap(
            @Nullable final Set<Tag> modelTags,
            @Nonnull final Function<Tag, String> getTagKeyFunc,
            @Nonnull final Function<Tag, String> getTagValueFunc) {
        if (modelTags == null) {
            return Map.of();
        }

        return modelTags
                .stream()
                .collect(Collectors.toMap(
                        getTagKeyFunc,
                        getTagValueFunc,
                        (oldValue, newValue) -> newValue));
    }

    public static <Tag> Set<Tag> convertTagMapToModelTags(
            @Nullable final Map<String, String> vpcLatticeTag,
            @Nonnull final BiFunction<String, String, Tag> tagConstructor) {
        if (vpcLatticeTag == null) {
            return ImmutableSet.of();
        }

        return vpcLatticeTag.entrySet()
                .stream()
                .map((entry) -> tagConstructor.apply(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    public static Map<String, String> mergeTags(
            @Nullable final Map<String, String> stackAndResourceLevelTags,
            @Nullable final Map<String, String> systemTags) {
        final var mergedMap = new HashMap<String, String>();

        if (stackAndResourceLevelTags != null) {
            mergedMap.putAll(stackAndResourceLevelTags);
        }

        if (systemTags != null) {
            mergedMap.putAll(systemTags);
        }

        return mergedMap;
    }

    public static TagResourceResponse tagResource(
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final String resourceArn,
            @Nonnull final Map<String, String> tags) {
        final var request = TagResourceRequest.builder()
                .resourceArn(resourceArn)
                .tags(tags)
                .build();

        return proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::tagResource);
    }

    public static UntagResourceResponse untagResource(
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final String resourceArn,
            @Nonnull final Collection<String> tags) {
        final var request = UntagResourceRequest.builder()
                .resourceArn(resourceArn)
                .tagKeys(tags)
                .build();

        return proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::untagResource);
    }
}
