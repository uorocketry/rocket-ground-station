package uorocketry.basestation.data;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uorocketry.basestation.config.Config;
import uorocketry.basestation.config.DataSet;
import uorocketry.basestation.config.FakeConfig;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataProcessorParseRSSIDataUnitTest {

    Config config;

    @BeforeEach
    public void setup() {
        String[] labels = new String[] {"Timestamp (ns)", "Value 1", "Value 2", "Value 3", "Value 4", "Value 5", "Value 6", "Value 7", "Value 8", "Value 9"};
        String[] states = new String[] {"First State", "Second State"};
        Map<String, Integer> indexes = new HashMap<>();
        indexes.put("timestamp", 0);

        DataSet dataSet = new DataSet("Processor Testing Set", "#AC1C3A", labels, states, indexes, ",");
        config = new FakeConfig(Collections.singletonList(dataSet), null);
    }

    @Test
    public void parseData_plain() {
        String data = "L/R RSSI: 50/0  L/R noise: 66/0 pkts: 0  txe=0 rxe=0 stx=0 srx=0 ecc=0/0 temp=21 dco=0";
        assertAndParseData(setupParseDataConfig(), data);
    }

    @Test
    public void parseData_newline() {
        String data = "L/R RSSI: 50/0  L/R noise: 66/0 pkts: 0  txe=0 rxe=0 stx=0 srx=0 ecc=0/0 temp=21 dco=0\r\n";
        assertAndParseData(setupParseDataConfig(), data);
    }

    private DataProcessor setupParseDataConfig() {
        return new DataProcessor(config, null);
    }

    private void assertAndParseData(DataProcessor testObject, String data) {
        DataHolder result = testObject.parseRSSI(0, data);
        assertData(result);

        assertAndReceiveData(testObject, data);
    }

    private void assertAndReceiveData(DataProcessor testObject, String data) {
        testObject.receivedData(0, data.getBytes(StandardCharsets.UTF_8));

        List<DataPoint> dataPoints = testObject.getDataPointHolder().get(0);
        assertData(dataPoints.get(dataPoints.size() - 1).getConnectionInfoData());
    }

    private void assertData(DataHolder result) {
        assertEquals(50, result.data[0].getDecimalValue());
        assertEquals(0, result.data[1].getDecimalValue());
        assertEquals(66, result.data[2].getDecimalValue());
        assertEquals(0, result.data[3].getDecimalValue());
        assertEquals(0, result.data[4].getDecimalValue());
        assertEquals(21, result.data[5].getDecimalValue());
        assertEquals(0, result.data[6].getDecimalValue());
        assertEquals("0", result.data[6].getFormattedString());
        assertEquals(0, result.data[7].getDecimalValue());
        assertEquals(0, result.data[8].getDecimalValue());
        assertEquals(0, result.data[9].getDecimalValue());
        assertEquals(0, result.data[10].getDecimalValue());
        assertEquals(0, result.data[11].getDecimalValue());
        assertEquals(0, result.data[12].getDecimalValue());
    }
}
