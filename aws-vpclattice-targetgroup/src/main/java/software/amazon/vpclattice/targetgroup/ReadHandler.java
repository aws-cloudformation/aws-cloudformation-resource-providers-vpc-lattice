package software.amazon.vpclattice.targetgroup;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.GetTargetGroupResponse;
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
            @Nonnull AmazonWebServicesClientProxy proxy,
            @Nonnull ResourceHandlerRequest<ResourceModel> request,
            @Nonnull CallbackContext callbackContext,
            @Nonnull ProxyClient<VpcLatticeClient> proxyClient,
            @Nonnull Logger logger) {
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then((progress) -> proxy.initiate(
                                "AWS::VpcLattice::TargetGroup::GetTargetGroup",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                        .translateToServiceRequest(Function.identity())
                        .makeServiceCall((model, client) -> {
                            final var getTargetGroupResponse =
                                    getTargetGroup(client, model);

                            final var listTagsResponse = TagHelper.listTags(model.getArn(), client);

                            final var listTargetsResponse =
                                    client.injectCredentialsAndInvokeV2(Translator.createListTargetsRequest(model), client.client()::listTargets);

                            return Translator.createResourceModel(getTargetGroupResponse, listTagsResponse, listTargetsResponse);
                        })
                        .handleError(ExceptionHandler::handleError)
                        .done(ProgressEvent::defaultSuccessHandler));
    }

    private GetTargetGroupResponse getTargetGroup(
            @Nonnull final ProxyClient<VpcLatticeClient> client,
            @Nonnull final ResourceModel model) {
        if (model.getArn() == null && model.getId() == null && model.getName() == null) {
            throw new CfnInvalidRequestException("Missing identifiers");
        }

        // If both arn and id is null, then get id by calling list and filter by name
        if (model.getArn() == null && model.getId() == null) {
            final var arn = this.getArnByName(client, model.getName());

            model.setArn(arn);
        }


        return client.injectCredentialsAndInvokeV2(Translator.createGetTargetGroupRequest(model), client.client()::getTargetGroup);
    }

    private String getArnByName(
            @Nonnull final ProxyClient<VpcLatticeClient> client,
            @Nonnull final String name) {
        String nextToken = null;

        do {
            final var listRequest = Translator.createListTargetGroupsRequest(nextToken);

            final var listResponse =
                    client.injectCredentialsAndInvokeV2(listRequest, client.client()::listTargetGroups);

            final var items = Optional.ofNullable(listResponse.items())
                    .orElse(List.of());

            for (final var targetGroup : items) {
                if (targetGroup.name().equals(name)) {
                    return targetGroup.arn();
                }
            }

            nextToken = listRequest.nextToken();
        } while (nextToken != null);

        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, name);
    }
}
