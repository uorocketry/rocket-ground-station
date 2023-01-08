package uorocketry.basestation.connections.method;

import themarpe.cobs.Cobs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class TcpConnectionMethod extends AbstractConnectionMethod implements Runnable {

    private String hostname;
    private int port;
    private boolean connecting;
    private boolean closing;

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private Thread thread;

    public TcpConnectionMethod(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    @Override
    public boolean open() {
        connecting = true;

        try {
            socket = new Socket(hostname, port);

            in = socket.getInputStream();
            out = socket.getOutputStream();

            thread = new Thread(this);
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();

            connecting = false;
            return false;
        }

        connecting = false;
        return true;
    }

    @Override
    public void run() {
        // Read data from serial port
        while (isOpen()) {
            try {
                ArrayList<Byte> receivedData = new ArrayList<>(512);
                int input;
                while (isOpen() && (input = in.read()) != -1) {
                    receivedData.add((byte) input);

                    if (receivedData.size() >= DELIMITER.length) {
                        boolean foundDelimeter = true;
                        for (int i = 0; i < DELIMITER.length; i++) {
                            if (receivedData.get(receivedData.size() - 1 - i) != DELIMITER[DELIMITER.length - 1 - i]) {
                                foundDelimeter = false;
                                break;
                            }
                        }

                        if (foundDelimeter) {
                            break;
                        }
                    }

                }

                if (receivedData.size() > 0) {
                    byte[] result = new byte[receivedData.size()];
                    for (int i = 0; i < receivedData.size(); i++) {
                        result[i] = receivedData.get(i);
                    }

                    byte[] decoded = new byte[receivedData.size()];
                    Cobs.DecodeResult decodeResult = Cobs.decode(decoded, result);

                    if (decodeResult.status == Cobs.DecodeStatus.OK) {
                        listener.receivedData(result);
                    } else {
                        System.err.println("Failed to decode using COBS due to " + decodeResult.status.toString() + ": " + Arrays.toString(decoded));
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean writeBytes(byte[] data) {
        try {
            out.write(data);
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    @Override
    public boolean close() {
        try {
            closing = true;
            in.close();
            out.close();

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }

        return true;
    }

    @Override
    public boolean isOpen() {
        return !closing && socket != null && socket.isConnected();
    }

    @Override
    public boolean isConnecting() {
        return connecting;
    }

}
