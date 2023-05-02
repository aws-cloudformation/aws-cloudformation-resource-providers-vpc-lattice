package software.amazon.vpclattice.common;

import javax.annotation.Nonnull;

/**
 * Wrapper around `CfnNotStabilizedException` to avoid default handleError handler
 * to get full resource model back
 */
public final class LatticeNotStabilizedException extends RuntimeException {
    public LatticeNotStabilizedException(@Nonnull final String message) {
        super(message);
    }
}
