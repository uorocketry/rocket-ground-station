package uorocketry.basestation.config;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class Config {

    protected List<DataSet> dataSet = new ArrayList<>(2);

    protected JSONObject configObject = null;

    public Integer getDataLength(int index) {
        return getDataSet(index).getLabels().length;
    }

    public String[] getLabels(int index) {
        return getDataSet(index).getLabels();
    }

    public int getDataSourceCount() {
        return dataSet.size();
    }

    public DataSet getDataSet(int index) {
        return dataSet.get(index);
    }

    public JSONObject getObject() {
        return configObject;
    }
}
