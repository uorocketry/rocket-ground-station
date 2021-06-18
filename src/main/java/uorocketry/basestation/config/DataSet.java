package uorocketry.basestation.config;

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
    private Map<String, Integer> indexes;

    private String separator;

    public DataSet(JSONObject dataSet) {
        if (dataSet.has("name")) name = dataSet.getString("name");
        if (dataSet.has("color")) color = dataSet.getString("color");
        if (dataSet.has("labels")) labels = dataSet.getJSONArray("labels").toList().toArray(String[]::new);
        if (dataSet.has("states")) states = dataSet.getJSONArray("states").toList().toArray(String[]::new);

        if (dataSet.has("name")) {
            JSONObject indexesJson = dataSet.getJSONObject("indexes");
            indexes = new HashMap<>();
            for (Iterator<String> it = indexesJson.keys(); it.hasNext(); ) {
                String key = it.next();
                try {
                    indexes.put(key, indexesJson.getInt(key));
                } catch (JSONException e) {}
            }
        }

        if (dataSet.has("separator")) separator = dataSet.getString("separator");
    }

    public DataSet(String name, String color, String[] labels, String[] states,
                   Map<String, Integer> indexes, String separator) {
        this.name = name;
        this.color = color;
        this.labels = labels;
        this.states = states;
        this.indexes = indexes;
        this.separator = separator;
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

    public Map<String, Integer> getIndexes() {
        return indexes;
    }

    public boolean hasIndex(String key) {
        return indexes != null && indexes.containsKey(key);
    }

    public boolean indexEquals(String key, int index) {
        if (indexes == null) return false;

        Integer result = indexes.get(key);
        return result != null && result == index;
    }

    public Integer getIndex(String key) {
        return indexes != null ? indexes.get(key) : null;
    }

    public String getSeparator() {
        return separator;
    }
}
