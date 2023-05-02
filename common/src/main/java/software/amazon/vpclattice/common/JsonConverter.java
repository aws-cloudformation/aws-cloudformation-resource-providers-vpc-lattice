package software.amazon.vpclattice.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import software.amazon.cloudformation.exceptions.CfnInternalFailureException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public final class JsonConverter {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonConverter() {
    }

    public static String toJSONString(
            @Nonnull final Map<String, Object> object) throws CfnInternalFailureException {
        try {
            MAPPER.disable(SerializationFeature.INDENT_OUTPUT);

            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new CfnInternalFailureException();
        }
    }

    public static Map<String, Object> toJSONMap(
            @Nullable final String jsonString) throws CfnInternalFailureException {
        if (jsonString == null) {
            return null;
        }

        MAPPER.disable(SerializationFeature.INDENT_OUTPUT);

        try {
            return MAPPER.readValue(jsonString, new TypeReference<Map<String, Object>>() {
            });
        } catch (JsonProcessingException e) {
            throw new CfnInternalFailureException();
        }
    }
}
