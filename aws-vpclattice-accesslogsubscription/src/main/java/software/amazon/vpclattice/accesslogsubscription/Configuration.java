package software.amazon.vpclattice.accesslogsubscription;

import java.util.Map;
import java.util.stream.Collectors;

class Configuration extends BaseConfiguration {
    public Configuration() {
        super("aws-vpclattice-accesslogsubscription.json");
    }


    /**
     * Providers should implement this method if their resource has a 'Tags' property to define resource-level tags
     *
     * @return
     */
    @Override
    public Map<String, String> resourceDefinedTags(final ResourceModel resourceModel) {
        if (resourceModel.getTags() == null) {
            return null;
        } else {
            return resourceModel.getTags()
                    .stream()
                    .collect(Collectors.toMap(Tag::getKey, Tag::getValue));
        }
    }
}
