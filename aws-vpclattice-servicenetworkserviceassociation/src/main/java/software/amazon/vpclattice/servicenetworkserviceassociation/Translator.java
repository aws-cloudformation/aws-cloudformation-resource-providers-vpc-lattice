package software.amazon.vpclattice.servicenetworkserviceassociation;

import software.amazon.awssdk.services.vpclattice.model.CreateServiceNetworkServiceAssociationRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteServiceNetworkServiceAssociationRequest;
import software.amazon.awssdk.services.vpclattice.model.GetServiceNetworkServiceAssociationRequest;
import software.amazon.awssdk.services.vpclattice.model.GetServiceNetworkServiceAssociationResponse;
import software.amazon.awssdk.services.vpclattice.model.ListServiceNetworkServiceAssociationsRequest;
import software.amazon.awssdk.services.vpclattice.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceNetworkServiceAssociationSummary;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.TagHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public final class Translator {
    private Translator() {
    }

    public static ResourceModel createResourceModel(
            @Nonnull final GetServiceNetworkServiceAssociationResponse getServiceNetworkServiceAssociationResponse,
            @Nonnull final ListTagsForResourceResponse listTagsResponse) {
        return ResourceModel.builder()
                .id(getServiceNetworkServiceAssociationResponse.id())
                .arn(getServiceNetworkServiceAssociationResponse.arn())
                .status(getServiceNetworkServiceAssociationResponse.statusAsString())
                .createdAt(getServiceNetworkServiceAssociationResponse.createdAt().toString())
                .dnsEntry(Translator.convertDnsFromSdk(getServiceNetworkServiceAssociationResponse.dnsEntry()))
                .serviceNetworkId(getServiceNetworkServiceAssociationResponse.serviceNetworkId())
                .serviceNetworkName(getServiceNetworkServiceAssociationResponse.serviceNetworkName())
                .serviceNetworkArn(getServiceNetworkServiceAssociationResponse.serviceNetworkArn())
                .serviceId(getServiceNetworkServiceAssociationResponse.serviceId())
                .serviceName(getServiceNetworkServiceAssociationResponse.serviceName())
                .serviceArn(getServiceNetworkServiceAssociationResponse.serviceArn())
                .tags(TagHelper.convertTagMapToModelTags(listTagsResponse.tags(), Tag::new))
                .build();
    }

    public static CreateServiceNetworkServiceAssociationRequest createCreateServiceNetworkServiceAssociationRequest(
            @Nonnull ResourceHandlerRequest<ResourceModel> request) {
        final var model = request.getDesiredResourceState();

        if (model.getServiceIdentifier() == null) {
            throw new CfnInvalidRequestException("ServiceIdentifier cannot be empty");
        }

        if (model.getServiceNetworkIdentifier() == null) {
            throw new CfnInvalidRequestException("ServiceNetworkIdentifier cannot be empty");
        }

        final var clientToken = request.getClientRequestToken();

        final var tags = TagHelper.mergeTags(
                request.getDesiredResourceTags(),
                request.getSystemTags()
        );

        return CreateServiceNetworkServiceAssociationRequest.builder()
                .serviceNetworkIdentifier(model.getServiceNetworkIdentifier())
                .serviceIdentifier(model.getServiceIdentifier())
                .tags(tags)
                .clientToken(clientToken)
                .build();
    }

    public static GetServiceNetworkServiceAssociationRequest createGetServiceNetworkServiceAssociationRequest(
            @Nonnull ResourceModel model) {
        return GetServiceNetworkServiceAssociationRequest.builder()
                .serviceNetworkServiceAssociationIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .build();
    }

    public static DeleteServiceNetworkServiceAssociationRequest createDeleteServiceNetworkServiceAssociationRequest(
            @Nonnull ResourceModel model) {
        return DeleteServiceNetworkServiceAssociationRequest.builder()
                .serviceNetworkServiceAssociationIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .build();
    }

    public static ListServiceNetworkServiceAssociationsRequest createListServiceNetworkServiceAssociationsRequest(
            @Nonnull ResourceModel model,
            @Nullable String nextToken) {
        if (model.getServiceNetworkIdentifier() == null && model.getServiceIdentifier() == null) {
            throw new CfnInvalidRequestException("Missing ServiceNetworkIdentifier and ServiceIdentifier");
        }

        return ListServiceNetworkServiceAssociationsRequest.builder()
                .nextToken(nextToken)
                .serviceNetworkIdentifier(model.getServiceNetworkIdentifier())
                .serviceIdentifier(model.getServiceIdentifier())
                .build();
    }

    public static ResourceModel createResourceModel(
            @Nonnull final ServiceNetworkServiceAssociationSummary summary) {
        return ResourceModel.builder()
                .arn(summary.arn())
                .build();
    }

    public static DnsEntry convertDnsFromSdk(
            @Nullable software.amazon.awssdk.services.vpclattice.model.DnsEntry dnsEntry) {
        return Optional
                .ofNullable(dnsEntry)
                .map((dns) -> DnsEntry.builder()
                        .domainName(dns.domainName())
                        .hostedZoneId(dns.hostedZoneId())
                        .build())
                .orElse(null);
    }
}
