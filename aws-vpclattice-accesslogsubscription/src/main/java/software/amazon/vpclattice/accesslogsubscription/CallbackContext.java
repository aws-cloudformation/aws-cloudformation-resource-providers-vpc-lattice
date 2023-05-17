package software.amazon.vpclattice.accesslogsubscription;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CallbackContext extends StdCallbackContext {
    public boolean hasCalledCreate;

    public boolean hasCalledUpdate;
}
