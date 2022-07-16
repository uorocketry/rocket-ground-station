package uorocketry.basestation.config;

import java.io.IOException;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

@lombok.Data
public class Config {
    @lombok.Getter(onMethod_ = { @JsonProperty("designedFor") })
    @lombok.Setter(onMethod_ = { @JsonProperty("designedFor") })
    private String designedFor;
    @lombok.Getter(onMethod_ = { @JsonProperty("datasets") })
    @lombok.Setter(onMethod_ = { @JsonProperty("datasets") })
    private DataSet[] datasets;
    @lombok.Getter(onMethod_ = { @JsonProperty("stateEvents") })
    @lombok.Setter(onMethod_ = { @JsonProperty("stateEvents") })
    private StateEvent[] stateEvents;

    // Serialize/deserialize helpers
    public static Config fromJsonString(String json) throws IOException {
        return getObjectReader().readValue(json);
    }

    public static String toJsonString(Config obj) throws JsonProcessingException {
        return getObjectWriter().writeValueAsString(obj);
    }

    private static ObjectReader reader;
    private static ObjectWriter writer;

    private static void instantiateMapper() {
        ObjectMapper mapper = new ObjectMapper();
        reader = mapper.readerFor(Config.class);
        writer = mapper.writerFor(Config.class);
    }

    private static ObjectReader getObjectReader() {
        if (reader == null)
            instantiateMapper();
        return reader;
    }

    private static ObjectWriter getObjectWriter() {
        if (writer == null)
            instantiateMapper();
        return writer;
    }
}