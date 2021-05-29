package uorocketry.basestation.connections.method;

public interface ConnectionMethod {

    boolean open();

    void setConnectionMethodListener(ConnectionMethodListener listener);

    void writeBytes(byte[] data);

    void close();

    boolean isOpen();

    boolean isConnecting();
}
