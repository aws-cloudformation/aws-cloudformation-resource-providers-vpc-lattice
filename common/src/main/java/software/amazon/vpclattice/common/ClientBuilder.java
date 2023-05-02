package software.amazon.vpclattice.common;

import software.amazon.awssdk.services.vpclattice.VpcLatticeClient;
import software.amazon.cloudformation.LambdaWrapper;

final public class ClientBuilder {
    private ClientBuilder() {
        //prevent instantiation
    }

    public static VpcLatticeClient getClient() {
        return VpcLatticeClient.builder()
                .httpClient(LambdaWrapper.HTTP_CLIENT)
                .build();
    }
}