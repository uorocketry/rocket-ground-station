package uorocketry.basestation.connections;

public interface DataReceiver {
    void receivedData(DeviceConnection deviceConnection, byte[] data);
}
