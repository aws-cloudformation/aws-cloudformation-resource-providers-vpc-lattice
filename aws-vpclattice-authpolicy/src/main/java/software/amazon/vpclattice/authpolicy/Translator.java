package software.amazon.vpclattice.authpolicy;

import software.amazon.awssdk.services.vpclattice.model.DeleteAuthPolicyRequest;
import software.amazon.awssdk.services.vpclattice.model.GetAuthPolicyRequest;
import software.amazon.awssdk.services.vpclattice.model.GetAuthPolicyResponse;
import software.amazon.awssdk.services.vpclattice.model.PutAuthPolicyRequest;
import software.amazon.vpclattice.common.JsonConverter;

import javax.annotation.Nonnull;

public class Translator {
    private Translator() {
    }

    public static ResourceModel createResourceModel(
            @Nonnull final String resourceIdentifier,
            @Nonnull final GetAuthPolicyResponse getAuthPolicyResponse) {
        return ResourceModel.builder()
                .resourceIdentifier(resourceIdentifier)
                .state(getAuthPolicyResponse.stateAsString())
                .policy(JsonConverter.toJSONMap(getAuthPolicyResponse.policy()))
                .build();
    }

    public static GetAuthPolicyRequest createGetAuthPolicyRequest(
            @Nonnull final ResourceModel model) {
        return GetAuthPolicyRequest.builder()
                .resourceIdentifier(model.getResourceIdentifier())
                .build();
    }

    public static PutAuthPolicyRequest createPutAuthPolicyRequest(
            @Nonnull final ResourceModel model) {
        return PutAuthPolicyRequest.builder()
                .resourceIdentifier(model.getResourceIdentifier())
                .policy(JsonConverter.toJSONString(model.getPolicy()))
                .build();
    }

    public static DeleteAuthPolicyRequest createDeleteAuthPolicyRequest(
            @Nonnull final ResourceModel model) {
        return DeleteAuthPolicyRequest.builder()
                .resourceIdentifier(model.getResourceIdentifier())
                .build();
    }
}
