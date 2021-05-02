package uorocketry.basestation.connections;

public interface DataReciever {
    void recievedData(ComConnection connection, byte[] data);
}
