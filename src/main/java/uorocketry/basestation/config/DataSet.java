package uorocketry.basestation.config;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;

import lombok.Builder;

@Builder
@lombok.Data
public class DataSet {
    @lombok.Getter(onMethod_ = { @JsonProperty("name") })
    @lombok.Setter(onMethod_ = { @JsonProperty("name") })
    private String name;
    @lombok.Getter(onMethod_ = { @JsonProperty("color") })
    @lombok.Setter(onMethod_ = { @JsonProperty("color") })
    private String color;
    @lombok.Getter(onMethod_ = { @JsonProperty("labels") })
    @lombok.Setter(onMethod_ = { @JsonProperty("labels") })
    private String[] labels;
    @lombok.Getter(onMethod_ = { @JsonProperty("states") })
    @lombok.Setter(onMethod_ = { @JsonProperty("states") })
    private String[] states;
    @lombok.Getter(onMethod_ = { @JsonProperty("indexes") })
    @lombok.Setter(onMethod_ = { @JsonProperty("indexes") })
    @Builder.Default
    private Map<String, Integer> indexes = new HashMap<>();
    @lombok.Getter(onMethod_ = { @JsonProperty("separator") })
    @lombok.Setter(onMethod_ = { @JsonProperty("separator") })
    private String separator;
}