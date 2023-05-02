package software.amazon.vpclattice.accesslogsubscription;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.GetAccessLogSubscriptionResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.vpclattice.common.ExceptionHandler;
import software.amazon.vpclattice.common.TagHelper;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ReadHandler extends BaseHandlerStd {
    @Override
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            @Nonnull final AmazonWebServicesClientProxy proxy,
            @Nonnull final ResourceHandlerRequest<ResourceModel> request,
            @Nonnull final CallbackContext callbackContext,
            @Nonnull final ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull final Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> proxy.initiate(
                                "AWS::VpcLattice::AccessLogSubscription::GetAccessLogSubscription",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                        .translateToServiceRequest(Function.identity())
                        .makeServiceCall((model, client) -> {
                            final var getAccessLogSubscriptionResponse = getAccessLogSubscription(client, model);

                            final var listTagsResponse = TagHelper.listTags(model.getArn(), client);

                            return Translator.createResourceModel(getAccessLogSubscriptionResponse, listTagsResponse);
                        })
                        .handleError(ExceptionHandler::handleError)
                        .done(ProgressEvent::defaultSuccessHandler));
    }

    private GetAccessLogSubscriptionResponse getAccessLogSubscription(
            @Nonnull final ProxyClient<VpcLatticeClient> client,
            @Nonnull final ResourceModel model) {
        if (model.getArn() == null && model.getId() == null && model.getResourceIdentifier() == null) {
            throw new CfnInvalidRequestException("Missing identifiers");
        }

        // If both arn and id is null, then get id by calling list and filter by name
        if (model.getArn() == null && model.getId() == null) {
            final var arn = this.getArnByResourceIdentifier(client, model);

            model.setArn(arn);
        }


        return client.injectCredentialsAndInvokeV2(Translator.createGetAccessLogSubscriptionRequest(model), client.client()::getAccessLogSubscription);
    }

    private String getArnByResourceIdentifier(
            @Nonnull final ProxyClient<VpcLatticeClient> client,
            @Nonnull final ResourceModel model) {
        String nextToken = null;

        final var resourceIdentifier = model.getResourceIdentifier();

        do {
            final var listRequest = Translator.createListAccessLogSubscriptionsRequest(model, nextToken);

            final var listResponse =
                    client.injectCredentialsAndInvokeV2(listRequest, client.client()::listAccessLogSubscriptions);

            final var items = Optional.ofNullable(listResponse.items())
                    .orElse(List.of());

            for (final var accessLogSubscription : items) {
                if (accessLogSubscription.resourceId().equals(resourceIdentifier) || accessLogSubscription.resourceArn().equals(resourceIdentifier)) {
                    return accessLogSubscription.arn();
                }
            }

            nextToken = listRequest.nextToken();
        } while (nextToken != null);

        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, resourceIdentifier);
    }
}
