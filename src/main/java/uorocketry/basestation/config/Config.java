package uorocketry.basestation.config;

import com.fasterxml.jackson.annotation.*;

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
}