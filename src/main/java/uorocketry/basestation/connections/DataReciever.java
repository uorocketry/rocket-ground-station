package uorocketry.basestation.connections;

public interface DataReciever {
    void recievedData(Connection connection, byte[] data);
}
