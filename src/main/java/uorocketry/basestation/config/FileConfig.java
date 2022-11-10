package uorocketry.basestation.config;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileConfig extends Config {

    public FileConfig(String fileName) {
        String configString = null;
        try {
            configString = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(null, "The config file was not found in " + fileName +
                    "\r\n\r\nIf you plan downloaded a release build, you might want to download the version with labels and sample data included.");

            return;
        }

        configObject = new JSONObject(configString);

        JSONArray datasetsJSONArray = configObject.getJSONArray("datasets");
        dataSet = new ArrayList<>(datasetsJSONArray.length());
        for (int i = 0; i < datasetsJSONArray.length(); i++) {
            dataSet.add(new DataSet(datasetsJSONArray.getJSONObject(i)));
        }
    }
}
