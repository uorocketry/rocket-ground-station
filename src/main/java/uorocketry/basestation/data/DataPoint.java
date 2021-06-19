package uorocketry.basestation.data;

import java.util.List;

public class DataPoint {
    private int receivedDataIndex;
    private DataHolder receivedData;

    private int connectionInfoIndex;
    private DataHolder connectionInfoData;

    public DataPoint(int receivedDataIndex, DataHolder receivedData, int connectionInfoIndex, DataHolder connectionInfoData) {
        this.receivedDataIndex = receivedDataIndex;
        this.receivedData = receivedData;

        this.connectionInfoIndex = connectionInfoIndex;
        this.connectionInfoData = connectionInfoData;
    }

    public DataPoint(List<DataHolder> receivedDataHolders, List<DataHolder> connectionInfoDataHolders,
                     int receivedDataIndex, int connectionInfoIndex) {
        this(receivedDataIndex,
                receivedDataHolders.size() >= receivedDataIndex - 1 ? receivedDataHolders.get(receivedDataIndex) : null,
                connectionInfoIndex,
                connectionInfoDataHolders.size() <= connectionInfoIndex - 1 ? connectionInfoDataHolders.get(connectionInfoIndex): null);
    }

    public void clearHiddenTypes() {
        clearHiddenTypes(receivedData);
        clearHiddenTypes(connectionInfoData);
    }

    private void clearHiddenTypes(DataHolder dataHolder) {
        if (dataHolder != null && !dataHolder.hiddenDataTypes.isEmpty()) {
            dataHolder.hiddenDataTypes.clear();
        }
    }

    public int getReceivedDataIndex() {
        return receivedDataIndex;
    }

    public DataHolder getReceivedData() {
        return receivedData;
    }

    public int getConnectionInfoIndex() {
        return connectionInfoIndex;
    }

    public DataHolder getConnectionInfoData() {
        return connectionInfoData;
    }
}
