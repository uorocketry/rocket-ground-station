package uorocketry.basestation.config;

import org.json.JSONObject;

public interface Config {
    Integer getDataLength(int index);

    String[] getLabel(int index);

    int getDataSourceCount();

    JSONObject getObject();
}
