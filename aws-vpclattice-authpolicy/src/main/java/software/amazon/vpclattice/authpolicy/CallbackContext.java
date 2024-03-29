package software.amazon.vpclattice.authpolicy;

import lombok.NoArgsConstructor;
import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CallbackContext extends StdCallbackContext {
}
