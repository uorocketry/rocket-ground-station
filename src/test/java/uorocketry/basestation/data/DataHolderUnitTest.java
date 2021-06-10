package uorocketry.basestation.data;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import uorocketry.basestation.Main;
import uorocketry.basestation.config.Config;
import uorocketry.basestation.config.FakeConfig;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataHolderUnitTest {

    @Test
    public void updateTableUI() {
        JSONObject datasetConfig = new JSONObject();
        datasetConfig.put("timestampIndex", 0);
        datasetConfig.put("stateIndex", 1);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put("First State");
        jsonArray.put("Second State");
        datasetConfig.put("states", jsonArray);

        String[] labels = new String[] {"Timestamp (ns)", "State Value", "Hidden Value", "Overflow", "NaN", "Decmial", "Bigger Decimal"};

        Main.config = new FakeConfig(Collections.singletonList(labels.length), null, labels.length, null);

        DataHolder dataHolder = new DataHolder(0, datasetConfig);
        dataHolder.hiddenDataTypes.add(new DataType(2, 0));

        dataHolder.set(0, "102020399293"); // has to be long (timestamp)
        dataHolder.set(1, "1");
        dataHolder.set(2, "2");
        dataHolder.set(3, "ovf");
        dataHolder.set(4, "nan");
        dataHolder.set(5, "5.2");
        dataHolder.set(6, "2321.34");

        JTable table = new JTable(labels.length, 2);
        dataHolder.updateTableUIWithData(table, labels);

        TableModel tableModel = table.getModel();

        for (int i = 0; i < labels.length; i++) {
            assertEquals(labels[i], tableModel.getValueAt(i, 0));
        }

        assertEquals("102,020,399,293", tableModel.getValueAt(0, 1));
        assertEquals("Second State", tableModel.getValueAt(1, 1));
        assertEquals("Hidden Data", tableModel.getValueAt(2, 1));
        assertEquals("null", tableModel.getValueAt(3, 1));
        assertEquals("null", tableModel.getValueAt(4, 1));
        assertEquals("5.2", tableModel.getValueAt(5, 1));
        assertEquals("2,321.340088", tableModel.getValueAt(6, 1));
    }
}
