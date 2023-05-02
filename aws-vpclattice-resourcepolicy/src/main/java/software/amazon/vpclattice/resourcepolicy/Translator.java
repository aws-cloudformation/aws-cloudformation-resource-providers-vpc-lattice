package software.amazon.vpclattice.resourcepolicy;

import software.amazon.awssdk.services.vpclattice.model.DeleteResourcePolicyRequest;
import software.amazon.awssdk.services.vpclattice.model.GetResourcePolicyRequest;
import software.amazon.awssdk.services.vpclattice.model.GetResourcePolicyResponse;
import software.amazon.awssdk.services.vpclattice.model.PutResourcePolicyRequest;
import software.amazon.vpclattice.common.JsonConverter;

import javax.annotation.Nonnull;

public class Translator {
    private Translator() {
    }

    public static ResourceModel createResourceModel(
            @Nonnull final String resourceArn,
            @Nonnull final GetResourcePolicyResponse getResourcePolicyResponse) {
        return ResourceModel.builder()
                .resourceArn(resourceArn)
                .policy(JsonConverter.toJSONMap(getResourcePolicyResponse.policy()))
                .build();
    }

    public static GetResourcePolicyRequest createGetResourcePolicyRequest(
            @Nonnull final ResourceModel model) {
        return GetResourcePolicyRequest.builder()
                .resourceArn(model.getResourceArn())
                .build();
    }

    public static PutResourcePolicyRequest createPutResourcePolicyRequest(
            @Nonnull final ResourceModel model) {
        return PutResourcePolicyRequest.builder()
                .resourceArn(model.getResourceArn())
                .policy(JsonConverter.toJSONString(model.getPolicy()))
                .build();
    }

    public static DeleteResourcePolicyRequest createDeleteResourcePolicyRequest(
            @Nonnull final ResourceModel model) {
        return DeleteResourcePolicyRequest.builder()
                .resourceArn(model.getResourceArn())
                .build();
    }
}
