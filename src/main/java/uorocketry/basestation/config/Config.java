package uorocketry.basestation.config;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Config {

    /** The location of the comma separated labels without the extension. */
    public static final String CONFIG_LOCATION = "data/config.json";

    /** How many data points are there. By default, it is the number of labels */
    protected List<Integer> dataLength = new ArrayList<>(2);
    protected List<String[]> labels = new ArrayList<>();
    /** How many data sources to record data from. It is set when the config is loaded. */
    protected int dataSourceCount = 1;
    protected JSONObject configObject = null;

    /**
     * Run once at the beginning of simulation mode
     */
    public Config() {
        this(CONFIG_LOCATION);
    }

    public Config(String fileName) {
        String configString = null;
        try {
            configString = Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(null, "The config file was not found in " + fileName +
                    "\r\n\r\nIf you plan downloaded a release build, you might want to download the version with labels and sample data included.");

            return;
        }

        configObject = new JSONObject(configString);

        JSONArray datasetsJSONArray = configObject.getJSONArray("datasets");
        dataSourceCount = datasetsJSONArray.length();

        // Add all data
        for (int i = 0; i < datasetsJSONArray.length(); i++) {
            JSONObject currentDataset = datasetsJSONArray.getJSONObject(i);

            JSONArray labelsJsonArray = currentDataset.getJSONArray("labels");

            // Load labels
            String[] labelsArray = new String[labelsJsonArray.length()];

            for (int j = 0; j < labelsArray.length; j++) {
                labelsArray[j] = labelsJsonArray.getString(j);
            }

            labels.add(labelsArray);

            dataLength.add(labelsArray.length);
        }
    }

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
