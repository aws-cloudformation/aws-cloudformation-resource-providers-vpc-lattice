package software.amazon.vpclattice.servicenetworkvpcassociation;

import com.google.common.collect.ImmutableSet;
import software.amazon.awssdk.services.vpclattice.model.CreateServiceNetworkVpcAssociationRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteServiceNetworkVpcAssociationRequest;
import software.amazon.awssdk.services.vpclattice.model.GetServiceNetworkVpcAssociationRequest;
import software.amazon.awssdk.services.vpclattice.model.GetServiceNetworkVpcAssociationResponse;
import software.amazon.awssdk.services.vpclattice.model.ListServiceNetworkVpcAssociationsRequest;
import software.amazon.awssdk.services.vpclattice.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceNetworkVpcAssociationSummary;
import software.amazon.awssdk.services.vpclattice.model.UpdateServiceNetworkVpcAssociationRequest;

import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.TagHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class Translator {
    private Translator() {
    }

    public static CreateServiceNetworkVpcAssociationRequest createCreateServiceNetworkVpcAssociationRequest(
            @Nonnull final ResourceHandlerRequest<ResourceModel> request) {
        final var model = request.getDesiredResourceState();

        if (model.getServiceNetworkIdentifier() == null) {
            throw new CfnInvalidRequestException("ServiceNetworkIdentifier cannot be empty");
        }

        if (model.getVpcIdentifier() == null) {
            throw new CfnInvalidRequestException("VpcIdentifier cannot be empty");
        }

        final var clientToken = request.getClientRequestToken();

        final var tags = TagHelper.mergeTags(request.getSystemTags(), request.getDesiredResourceTags());

        return CreateServiceNetworkVpcAssociationRequest.builder()
                .serviceNetworkIdentifier(model.getServiceNetworkIdentifier())
                .vpcIdentifier(model.getVpcIdentifier())
                .tags(tags)
                .securityGroupIds(model.getSecurityGroupIds())
                .clientToken(clientToken)
                .build();
    }

    public static GetServiceNetworkVpcAssociationRequest createGetServiceNetworkVpcAssociationRequest(
            @Nonnull final ResourceModel model) {
        return GetServiceNetworkVpcAssociationRequest.builder()
                .serviceNetworkVpcAssociationIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .build();
    }

    public static DeleteServiceNetworkVpcAssociationRequest createDeleteServiceNetworkVpcAssociationRequest(
            @Nonnull final ResourceModel model) {
        return DeleteServiceNetworkVpcAssociationRequest.builder()
                .serviceNetworkVpcAssociationIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .build();
    }

    public static ListServiceNetworkVpcAssociationsRequest createListServiceNetworkVpcAssociationsRequest(
            @Nonnull final ResourceModel model,
            @Nullable String nextToken) {
        if (model.getServiceNetworkIdentifier() == null && model.getVpcIdentifier() == null) {
            throw new CfnInvalidRequestException("Missing ServiceNetworkIdentifier and VpcIdentifier");
        }

        return ListServiceNetworkVpcAssociationsRequest.builder()
                .nextToken(nextToken)
                .serviceNetworkIdentifier(model.getServiceNetworkIdentifier())
                .vpcIdentifier(model.getVpcIdentifier())
                .build();
    }

    public static ResourceModel createResourceModel(
            @Nonnull final ServiceNetworkVpcAssociationSummary summary) {
        return ResourceModel.builder()
                .arn(summary.arn())
                .build();
    }

    public static UpdateServiceNetworkVpcAssociationRequest createUpdateServiceNetworkVpcAssociationRequest(@Nonnull ResourceModel model) {
        return UpdateServiceNetworkVpcAssociationRequest.builder()
                .serviceNetworkVpcAssociationIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .securityGroupIds(model.getSecurityGroupIds())
                .build();
    }

    public static ResourceModel createResourceModel(
            @Nonnull final GetServiceNetworkVpcAssociationResponse getServiceNetworkVpcAssociationResponse,
            @Nonnull final ListTagsForResourceResponse listTagsResponse) {
        return ResourceModel.builder()
                .id(getServiceNetworkVpcAssociationResponse.id())
                .arn(getServiceNetworkVpcAssociationResponse.arn())
                .createdAt(getServiceNetworkVpcAssociationResponse.createdAt().toString())
                .status(getServiceNetworkVpcAssociationResponse.statusAsString())
                .serviceNetworkId(getServiceNetworkVpcAssociationResponse.serviceNetworkId())
                .serviceNetworkArn(getServiceNetworkVpcAssociationResponse.serviceNetworkArn())
                .serviceNetworkName(getServiceNetworkVpcAssociationResponse.serviceNetworkName())
                .vpcId(getServiceNetworkVpcAssociationResponse.vpcId())
                .securityGroupIds(getServiceNetworkVpcAssociationResponse.securityGroupIds() == null ? null : ImmutableSet.copyOf(getServiceNetworkVpcAssociationResponse.securityGroupIds()))
                .tags(TagHelper.convertTagMapToModelTags(listTagsResponse.tags(), Tag::new))
                .build();
    }
}
