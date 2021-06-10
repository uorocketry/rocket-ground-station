package uorocketry.basestation.config;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class Config {

    /** How many data points are there. By default, it is the number of labels */
    protected List<Integer> dataLength = new ArrayList<>(2);
    protected List<String[]> labels = new ArrayList<>();
    /** How many data sources to record data from. It is set when the config is loaded. */
    protected int dataSourceCount = 1;
    protected JSONObject configObject = null;

    public Integer getDataLength(int index) {
        return dataLength.get(index);
    }

    public String[] getLabel(int index) {
        return labels.get(index);
    }

    public int getDataSourceCount() {
        return dataSourceCount;
    }

    public JSONObject getObject() {
        return configObject;
    }
}
