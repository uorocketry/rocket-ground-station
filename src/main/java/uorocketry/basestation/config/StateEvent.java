package uorocketry.basestation.config;

import com.fasterxml.jackson.annotation.*;

@lombok.Data
public class StateEvent {
    @lombok.Getter(onMethod_ = { @JsonProperty("name") })
    @lombok.Setter(onMethod_ = { @JsonProperty("name") })
    private String name;

    @lombok.Getter(onMethod_ = { @JsonProperty("data") })
    @lombok.Setter(onMethod_ = { @JsonProperty("data") })
    private Long data;

    @lombok.Getter(onMethod_ = { @JsonProperty("availableStates") })
    @lombok.Setter(onMethod_ = { @JsonProperty("availableStates") })
    private long[] availableStates;

    @lombok.Getter(onMethod_ = { @JsonProperty("successStates") })
    @lombok.Setter(onMethod_ = { @JsonProperty("successStates") })
    private long[] successStates;
}
