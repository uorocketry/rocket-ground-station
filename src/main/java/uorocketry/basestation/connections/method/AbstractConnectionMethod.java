package uorocketry.basestation.connections.method;
import java.util.Timer;
import java.util.TimerTask;

import java.nio.charset.StandardCharsets;

public abstract class AbstractConnectionMethod implements ConnectionMethod {

    protected final byte[] DELIMITER = "\n".getBytes(StandardCharsets.UTF_8);

    private static int PING_PERIOD = 5000;

    private static byte PING_BYTE = 2;

    protected ConnectionMethodListener listener;

    private class Ping extends TimerTask
    {
        private AbstractConnectionMethod parent;

        private byte[] pingData = {PING_BYTE};

        public void run()
        {
            writeBytes(pingData);
            System.out.println("Ping");
        }
    }

    protected Timer pingTimer;
    protected Ping ping;


    public AbstractConnectionMethod() {
        pingTimer = new Timer();
        ping = new Ping();

        pingTimer.schedule(ping, 2000, PING_PERIOD);
    }



    @Override
    public void setConnectionMethodListener(ConnectionMethodListener listener) {
        this.listener = listener;
    }
}
