package uorocketry.basestation.connections;

import org.jetbrains.annotations.NotNull;

public interface DataReceiver {
    void receivedData(@NotNull DeviceConnection deviceConnection, byte[] data);
}
