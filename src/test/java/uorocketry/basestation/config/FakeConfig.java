package uorocketry.basestation.config;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FakeConfig extends Config {

    protected List<Integer> dataLength;
    protected List<String[]> labels;
    protected int dataSourceCount;
    protected JSONObject configObject;

    public FakeConfig(List<Integer> dataLength, List<String[]> labels, int dataSourceCount, JSONObject configObject) {
        this.dataLength = dataLength;
        this.labels = labels;
        this.dataSourceCount = dataSourceCount;
        this.configObject = configObject;
    }

    public Integer getDataLength(int index) {
        return dataLength.get(index);
    }

    public String[] getLabels(int index) {
        return labels.get(index);
    }

    public int getDataSourceCount() {
        return dataSourceCount;
    }

    public JSONObject getObject() {
        return configObject;
    }
}
