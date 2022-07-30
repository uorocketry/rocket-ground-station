package uorocketry.basestation.config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DataSet {
    private String name;
    private String color;

    private String[] labels;
    private String[] states;
    private Map<String, String> indexes;

    private String separator;

    public DataSet(JSONObject dataSet) {
        if (dataSet.has("name")) name = dataSet.getString("name");
        if (dataSet.has("color")) color = dataSet.getString("color");
        if (dataSet.has("labels")) {
            labels = jsonStringArrayToArray(dataSet.getJSONArray("labels"));
        }
        if (dataSet.has("states")) {
            states = jsonStringArrayToArray(dataSet.getJSONArray("states"));
        }

        if (dataSet.has("name")) {
            JSONObject indexesJson = dataSet.getJSONObject("indexes");
            indexes = new HashMap<>();
            for (Iterator<String> it = indexesJson.keys(); it.hasNext(); ) {
                String key = it.next();
                try {
                    indexes.put(key, indexesJson.getString(key));
                } catch (JSONException e) {}
            }
        }

        if (dataSet.has("separator")) separator = dataSet.getString("separator");
    }

    public DataSet(String name, String color, String[] labels, String[] states,
                   Map<String, String> indexes, String separator) {
        this.name = name;
        this.color = color;
        this.labels = labels;
        this.states = states;
        this.indexes = indexes;
        this.separator = separator;
    }

    private String[] jsonStringArrayToArray(JSONArray jsonArray) {
        String[] result = new String[jsonArray.length()];
        for (int i = 0; i < result.length; i++) {
            result[i] = jsonArray.getString(i);
        }

        return result;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public String[] getLabels() {
        return labels;
    }

    public String[] getStates() {
        return states;
    }

    public String getState(int index) {
        return states != null ? states[index] : String.valueOf(index);
    }

    public Map<String, String> getIndexes() {
        return indexes;
    }

    public boolean hasIndex(String key) {
        return indexes != null && indexes.containsKey(key);
    }

    public boolean indexIsType(String key, int index) {
        if (indexes == null) return false;

        String result = indexes.get(index + "");
        return key.equals(result);
    }

    public Integer getIndex(String key) {
        if (indexes != null) {
            for (Map.Entry<String, String> entry : indexes.entrySet()) {
                if (entry.getValue().equals(key)) {
                    try {
                        return Integer.parseInt(entry.getKey());
                    } catch (NumberFormatException ignored) {}
                }
            }

            return null;
        } else {
            return null;
        }
    }

    public String getSeparator() {
        return separator;
    }
}
