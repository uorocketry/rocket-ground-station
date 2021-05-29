package uorocketry.basestation.connections.method;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

import java.nio.charset.StandardCharsets;

public class SerialConnectionMethod implements ConnectionMethod, SerialPortMessageListener {

    private final byte[] DELIMITER = "\n".getBytes(StandardCharsets.UTF_8);

    private SerialPort serialPort;
    private boolean connecting;

    private ConnectionMethodListener listener;

    public SerialConnectionMethod(SerialPort newSerialPort) {
        serialPort = newSerialPort;
    }

    @Override
    public boolean open() {
        connecting = true;

        boolean isOpen = serialPort.openPort();
        serialPort.setBaudRate(57600);
        serialPort.addDataListener(this);

        connecting = false;

        return isOpen;
    }

    @Override
    public void setConnectionMethodListener(ConnectionMethodListener listener) {
        this.listener = listener;
    }

    @Override
    public void writeBytes(byte[] data) {
        if (isOpen()) {
            serialPort.writeBytes(data, data.length);
        }
    }

    @Override
    public void close() {
        if (isOpen()) {
            serialPort.closePort();
            serialPort.removeDataListener();
        }
    }

    @Override
    public boolean isOpen() {
        return serialPort != null && serialPort.isOpen();
    }

    @Override
    public boolean isConnecting() {
        return connecting;
    }

    public String getDescriptivePortName() {
        return serialPort != null ? serialPort.getDescriptivePortName() : null;
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }

    @Override
    public byte[] getMessageDelimiter() {
        return DELIMITER;
    }

    @Override
    public boolean delimiterIndicatesEndOfMessage() {
        return true;
    }

    @Override
    public void serialEvent(SerialPortEvent event) {
        if (listener != null) {
            listener.receivedData(event.getReceivedData());
        }
    }
}
