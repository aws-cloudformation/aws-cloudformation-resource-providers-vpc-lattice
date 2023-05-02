package software.amazon.vpclattice.service;

import lombok.Builder;
import lombok.NoArgsConstructor;
import software.amazon.cloudformation.proxy.StdCallbackContext;

@Builder
@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class CallbackContext extends StdCallbackContext {
}
