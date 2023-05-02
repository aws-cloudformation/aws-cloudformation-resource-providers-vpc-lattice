package software.amazon.vpclattice.servicenetworkvpcassociation;

import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.ListTagsForResourceRequest;
import software.amazon.awssdk.services.vpclattice.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.vpclattice.model.TagResourceRequest;
import software.amazon.awssdk.services.vpclattice.model.TagResourceResponse;
import software.amazon.awssdk.services.vpclattice.model.UntagResourceRequest;
import software.amazon.awssdk.services.vpclattice.model.UntagResourceResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProxyClient;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

public class TagsTestUtils {
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

    public static final Map<String, String> TAGS = ImmutableMap.<String, String>builder()
            .put("another-key", "another-stack-level-value")
            .put("key", "resource-level-value")
            .put("other-key", "other-value")
            .build();

    protected static final Set<Tag> MODEL_TAGS = TAGS.entrySet()
            .stream()
            .map((entry) -> Tag.builder()
                    .key(entry.getKey())
                    .value(entry.getValue())
                    .build())
            .collect(Collectors.toSet());

    public static final ListTagsForResourceResponse LIST_TAGS_RESPONSE = ListTagsForResourceResponse.builder()
            .tags(TAGS)
            .build();

    public static final ListTagsForResourceResponse EMPTY_LIST_TAGS_RESPONSE = ListTagsForResourceResponse.builder()
            .tags(Map.of())
            .build();

    public static final TagResourceResponse TAG_RESOURCE_RESPONSE = TagResourceResponse.builder()
            .build();

    public static final UntagResourceResponse UNTAG_RESOURCE_RESPONSE = UntagResourceResponse.builder()
            .build();

    public static void mockSdkReturn(@Nonnull final ProxyClient<VpcLatticeClient> proxyClient, @Nonnull final ListTagsForResourceResponse response, final ListTagsForResourceResponse... responses) {
        var stubber = doReturn(response);

        for (final var r : responses) {
            stubber = stubber.doReturn(r);
        }

        stubber.when(proxyClient.client()).listTagsForResource(any(ListTagsForResourceRequest.class));
    }

    public static void mockSdkReturn(@Nonnull final ProxyClient<VpcLatticeClient> proxyClient, @Nonnull final TagResourceResponse response) {
        doReturn(response).when(proxyClient.client()).tagResource(any(TagResourceRequest.class));
    }

    public static void mockSdkReturn(@Nonnull final ProxyClient<VpcLatticeClient> proxyClient, @Nonnull final UntagResourceResponse response) {
        doReturn(response).when(proxyClient.client()).untagResource(any(UntagResourceRequest.class));
    }

    public static void mockSdkListTagsThrow(@Nonnull final ProxyClient<VpcLatticeClient> proxyClient, @Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).listTagsForResource(any(ListTagsForResourceRequest.class));
    }

    public static void mockSdkTagThrow(@Nonnull final ProxyClient<VpcLatticeClient> proxyClient, @Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).tagResource(any(TagResourceRequest.class));
    }

    public static void mockSdkUntagThrow(@Nonnull final ProxyClient<VpcLatticeClient> proxyClient, @Nonnull final Class<? extends Throwable> exception) {
        doThrow(exception).when(proxyClient.client()).untagResource(any(UntagResourceRequest.class));
    }

}
