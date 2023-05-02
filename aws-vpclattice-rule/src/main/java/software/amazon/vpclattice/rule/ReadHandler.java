package software.amazon.vpclattice.rule;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.awssdk.services.vpclattice.model.GetRuleResponse;
import software.amazon.awssdk.services.vpclattice.model.RuleSummary;
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
import java.util.Objects;
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
                                "AWS::VpcLattice::Rule::GetRule",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                        .translateToServiceRequest(Function.identity())
                        .makeServiceCall((model, client) -> {
                            final var getRuleResponse = getRule(client, model);

                            final var listTagsResponse = TagHelper.listTags(model.getArn(), client);

                            return Translator.createResourceModel(getRuleResponse, listTagsResponse);
                        })
                        .handleError(ExceptionHandler::handleError)
                        .done(ProgressEvent::defaultSuccessHandler));
    }


    private GetRuleResponse getRule(
            @Nonnull final ProxyClient<VpcLatticeClient> client,
            @Nonnull final ResourceModel model) {
        if (model.getArn() == null && (model.getName() == null || model.getListenerIdentifier() == null || model.getServiceIdentifier() == null)) {
            throw new CfnInvalidRequestException("Missing identifiers");
        }

        // If both arn and id is null, then get id by calling list and filter by name
        if (model.getArn() == null && model.getId() == null) {
            final var arn = this.getArnByServiceIdentifierAndNameAndPort(client, model);

            model.setArn(arn);
        }


        return client.injectCredentialsAndInvokeV2(Translator.createGetRuleRequest(model), client.client()::getRule);
    }

    private String getArnByServiceIdentifierAndNameAndPort(
            @Nonnull final ProxyClient<VpcLatticeClient> client,
            @Nonnull final ResourceModel model) {
        final var listRequest = Translator.createListRulesRequest(model, null);

        final var listResponse =
                client.injectCredentialsAndInvokeV2(listRequest, client.client()::listRules);

        return Optional.ofNullable(listResponse.items())
                .orElse(List.of())
                .stream()
                .filter((item) -> Objects.equals(item.name(), model.getName()))
                .findFirst()
                .map(RuleSummary::arn)
                .orElseThrow(() -> new CfnNotFoundException(ResourceModel.TYPE_NAME, String.format("%s|%s|%s", model.getServiceIdentifier(), model.getListenerIdentifier(), model.getName())));
    }
}
