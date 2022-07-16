package uorocketry.basestation.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataPointHolder implements Iterable<List<DataPoint>> {
    private final List<List<DataHolder>> allReceivedData;
    private final List<List<DataHolder>> allConnectionInfoData;
    private final List<List<DataPoint>> dataPoints;

    public DataPointHolder(int dataSourceCount) {
        allReceivedData = new ArrayList<>(dataSourceCount);
        allConnectionInfoData = new ArrayList<>(dataSourceCount);
        dataPoints = new ArrayList<>(dataSourceCount);

        for (int i = 0; i < dataSourceCount; i++) {
            allReceivedData.add(new ArrayList<>());
            allConnectionInfoData.add(new ArrayList<>());
            dataPoints.add(new ArrayList<>());
        }
    }

    public List<DataPoint> get(int i) {
        return dataPoints.get(i);
    }

    public int size() {
        return dataPoints.size();
    }

    public List<List<DataHolder>> getAllReceivedData() {
        return allReceivedData;
    }

    public List<List<DataHolder>> getAllConnectionInfoData() {
        return allConnectionInfoData;
    }

    @Override
    public Iterator<List<DataPoint>> iterator() {
        return dataPoints.iterator();
    }

    /**
     * Convert an index for the general dataPoints array into an index for the
     * specific array allConnectionInfoData
     */
    public int toReceivedDataIndex(int tableIndex, int dataPointIndex) {
        return toReceivedDataIndex(dataPoints.get(tableIndex), dataPointIndex);
    }

    /**
     * Convert an index for the general dataPoints array into an index for the
     * specific array allReceivedData
     */
    public int toReceivedDataIndex(List<DataPoint> dataPoints, int dataPointIndex) {
        return dataPoints.get(dataPointIndex).getReceivedDataIndex();
    }

    /**
     * Convert an index for the general dataPoints array into an index for the
     * specific array allConnectionInfoData
     * 
     * @return
     */
    public int toConnectionInfoDataIndex(int tableIndex, int dataPointIndex) {
        return toConnectionInfoDataIndex(dataPoints.get(tableIndex), dataPointIndex);
    }

    /**
     * Convert an index for the general dataPoints array into an index for the
     * specific array allConnectionInfoData
     */
    public int toConnectionInfoDataIndex(List<DataPoint> dataPoints, int dataPointIndex) {
        return dataPoints.get(dataPointIndex).getConnectionInfoIndex();
    }
}
