package software.amazon.vpclattice.common;

import com.google.common.base.Strings;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public final class NameHelper {
    /**
     * Create a random name of format {@literal `<stack-name>-<LogicalResourceId>-<RandomHash>`}
     *
     * @param request         request
     * @param forbiddenPrefix prefix that names can't start with
     * @param maxLength       maximum length of names
     * @return a random name of format {@literal `<stack-name>-<LogicalResourceId>-<RandomHash>`}
     */
    public static String generateRandomName(
            @Nonnull ResourceHandlerRequest<?> request,
            @Nullable final String forbiddenPrefix,
            @Nonnull final Integer maxLength) {
        // This case only exists when run contract tests
        if (request.getStackId() == null || request.getLogicalResourceIdentifier() == null || request.getClientRequestToken() == null) {
            final var name = String.format("random-%s", UUID.randomUUID());

            return name.substring(0, Math.min(name.length(), maxLength));
        }

        var name = IdentifierUtils
                .generateResourceIdentifier(
                        request.getStackId(),
                        request.getLogicalResourceIdentifier(),
                        request.getClientRequestToken(),
                        maxLength)
                // since name doesn't allow UPPERCASE characters
                .toLowerCase();

        // To prevent creating names that start with identifiers
        while (!Strings.isNullOrEmpty(forbiddenPrefix) && name.startsWith(forbiddenPrefix)) {
            name = name.substring(forbiddenPrefix.length());
        }

        return name;
    }
}
