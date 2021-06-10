package uorocketry.basestation.config;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileConfig extends Config {

    /** The location of the comma separated labels without the extension. */
    public static final String CONFIG_LOCATION = "data/config.json";

    /**
     * Run once at the beginning of simulation mode
     */
    public FileConfig() {
        this(CONFIG_LOCATION);
    }

    public FileConfig(String fileName) {
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
}
