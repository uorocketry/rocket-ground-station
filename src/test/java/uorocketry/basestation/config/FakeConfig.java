package uorocketry.basestation.config;

import org.json.JSONObject;

import java.util.List;

public class FakeConfig extends Config {

    public FakeConfig(List<DataSet> dataSet, JSONObject configObject) {
        this.dataSet = dataSet;
        this.configObject = configObject;
    }
}
