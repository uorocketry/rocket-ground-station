package uorocketry.basestation.elements.connections;

public interface DataReciever {
    void recievedData(ComConnection connection, byte[] data);
}
