package software.amazon.vpclattice.service;

import software.amazon.awssdk.services.vpclattice.model.CreateServiceRequest;
import software.amazon.awssdk.services.vpclattice.model.DeleteServiceRequest;
import software.amazon.awssdk.services.vpclattice.model.GetServiceRequest;
import software.amazon.awssdk.services.vpclattice.model.GetServiceResponse;
import software.amazon.awssdk.services.vpclattice.model.ListServicesRequest;
import software.amazon.awssdk.services.vpclattice.model.ListTagsForResourceResponse;
import software.amazon.awssdk.services.vpclattice.model.ServiceSummary;
import software.amazon.awssdk.services.vpclattice.model.UpdateServiceRequest;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.NameHelper;
import software.amazon.vpclattice.common.TagHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Optional;

public final class Translator {
    private static final String SERVICE_ID_PREFIX = "svc-";

    private static final Integer SERVICE_NAME_MAX_LENGTH = 40;

    private Translator() {
    }

    public static ResourceModel createResourceModel(
            @Nonnull final GetServiceResponse getServiceResponse,
            @Nonnull final ListTagsForResourceResponse listTagsResponse) {
        return ResourceModel.builder()
                .id(getServiceResponse.id())
                .arn(getServiceResponse.arn())
                .name(getServiceResponse.name())
                .certificateArn(getServiceResponse.certificateArn())
                .customDomainName(getServiceResponse.customDomainName())
                .dnsEntry(Translator.convertDnsEntryFromSdk(getServiceResponse.dnsEntry()))
                .createdAt(Optional.ofNullable(getServiceResponse.createdAt()).map(Instant::toString).orElse(null))
                .lastUpdatedAt(Optional.ofNullable(getServiceResponse.lastUpdatedAt()).map(Instant::toString).orElse(null))
                .status(getServiceResponse.statusAsString())
                .authType(getServiceResponse.authTypeAsString())
                .tags(TagHelper.convertTagMapToModelTags(listTagsResponse.tags(), Tag::new))
                .build();
    }

    public static CreateServiceRequest createCreateServiceRequest(
            @Nonnull final ResourceHandlerRequest<ResourceModel> handlerRequest) {
        final var model = handlerRequest.getDesiredResourceState();

        final var clientToken = handlerRequest.getClientRequestToken();

        final var tags = TagHelper.mergeTags(
                handlerRequest.getDesiredResourceTags(),
                handlerRequest.getSystemTags()
        );

        return CreateServiceRequest.builder()
                .name(Optional.ofNullable(model.getName())
                        .orElse(NameHelper.generateRandomName(handlerRequest, SERVICE_ID_PREFIX, SERVICE_NAME_MAX_LENGTH)))
                .authType(model.getAuthType())
                .certificateArn(model.getCertificateArn())
                .customDomainName(model.getCustomDomainName())
                .tags(tags)
                .clientToken(clientToken)
                .build();
    }

    public static GetServiceRequest createGetServiceRequest(
            @Nonnull final ResourceModel model) {
        return GetServiceRequest.builder()
                .serviceIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .build();
    }

    public static UpdateServiceRequest createUpdateServiceRequest(
            @Nonnull final ResourceModel model) {
        return UpdateServiceRequest.builder()
                .serviceIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .authType(model.getAuthType())
                .certificateArn(model.getCertificateArn())
                .build();
    }

    public static DeleteServiceRequest createDeleteServiceRequest(
            @Nonnull final ResourceModel model) {
        return DeleteServiceRequest.builder()
                .serviceIdentifier(Optional.ofNullable(model.getArn()).orElse(model.getId()))
                .build();
    }

    public static ListServicesRequest createListServicesRequest(
            @Nullable final String nextToken) {
        return ListServicesRequest.builder()
                .nextToken(nextToken)
                .build();
    }

    public static ResourceModel createResourceModel(
            @Nonnull final ServiceSummary summary) {
        return ResourceModel.builder()
                .arn(summary.arn())
                .build();
    }

    public static DnsEntry convertDnsEntryFromSdk(
            @Nullable final software.amazon.awssdk.services.vpclattice.model.DnsEntry dnsEntry) {
        if (dnsEntry == null) {
            return null;
        }

        return DnsEntry.builder()
                .domainName(dnsEntry.domainName())
                .hostedZoneId(dnsEntry.hostedZoneId())
                .build();
    }
}
