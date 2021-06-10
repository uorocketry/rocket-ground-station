package uorocketry.basestation.data;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uorocketry.basestation.Main;
import uorocketry.basestation.config.Config;
import uorocketry.basestation.config.FakeConfig;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataProcessorUnitTest {

    JSONObject configObject;

    @BeforeEach
    public void setup() {
        configObject = new JSONObject();
        JSONArray datasets = new JSONArray();
        JSONObject datasetConfig = new JSONObject();
        datasetConfig.put("timestampIndex", 0);
        datasets.put(datasetConfig);
        configObject.put("datasets", datasets);
    }

    @Test
    public void parseData_plain() {
        String data = "102020399293,2,182.12,192,12.41,2,1,331,12,1";
        assertAndParseData(setupParseDataConfig(), data);
    }

    @Test
    public void parseData_newline() {
        String data = "102020399293,2,182.12,192,12.41,2,1,331,12,1\r\n";
        assertAndParseData(setupParseDataConfig(), data);
    }

    @Test
    public void parseData_plainNewline() {
        String data = "102020399293,2,182.12,192,12.41,2,1,331,12,1\\r\\n";
        assertAndParseData(setupParseDataConfig(), data);
    }

    @Test
    public void parseData_plainNewlineAndNewline() {
        String data = "102020399293,2,182.12,192,12.41,2,1,331,12,1\\r\\n\r\n";
        assertAndParseData(setupParseDataConfig(), data);
    }

    @Test
    public void parseData_binarySyntax() {
        String data = "b'102020399293,2,182.12,192,12.41,2,1,331,12,1\\r\\n'";
        assertAndParseData(setupParseDataConfig(), data);
    }

    private DataProcessor setupParseDataConfig() {
        int dataLength = 10;
        Config config = new FakeConfig(Collections.singletonList(dataLength), null, dataLength, configObject);
        Main.config = config;

        return new DataProcessor(config, null);
    }

    private void assertAndParseData(DataProcessor testObject, String data) {
        DataHolder result = testObject.parseData(0, data);

        assertEquals(102020399293L, result.data[0].getLongValue());
        assertEquals(2, result.data[1].getDecimalValue());
        assertEquals(182.12f, result.data[2].getDecimalValue());
        assertEquals(192, result.data[3].getDecimalValue());
        assertEquals(12.41f, result.data[4].getDecimalValue());
        assertEquals(2, result.data[5].getDecimalValue());
        assertEquals(1, result.data[6].getDecimalValue());
        assertEquals(331, result.data[7].getDecimalValue());
        assertEquals(12, result.data[8].getDecimalValue());
        assertEquals(1, result.data[9].getDecimalValue());
    }
}
