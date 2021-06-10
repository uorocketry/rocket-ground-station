package uorocketry.basestation.config;

import org.json.JSONObject;

import java.util.List;

public class FakeConfig extends Config {

    public FakeConfig(List<Integer> dataLength, List<String[]> labels, int dataSourceCount, JSONObject configObject) {
        this.dataLength = dataLength;
        this.labels = labels;
        this.dataSourceCount = dataSourceCount;
        this.configObject = configObject;
    }
}
