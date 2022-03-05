package uorocketry.basestation.connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fazecast.jSerialComm.SerialPort;

public class DeviceConnectionHolder implements Iterable<DeviceConnection> {
    /** Connection data and related UI */
    private Map<Type, List<DeviceConnection>> connections = new HashMap<>();
    
    /** All the serial ports found */
    private SerialPort[] allSerialPorts;

    public enum Type {
        TABLE,
        BUTTON_BOX
    }
    
    public DeviceConnection add(Type type, DataReceiver[] dataReceivers, String name) {
        List<DeviceConnection> list = getList(type);
        DeviceConnection deviceConnection = new DeviceConnection(this, name, list.size());
        deviceConnection.setDataReceivers(dataReceivers);
        list.add(deviceConnection);

        return deviceConnection;
    }
    
    public DeviceConnection get(int tableIndex) {
        return getList(Type.TABLE).get(tableIndex);
    }
    
    private List<DeviceConnection> getList(Type type) {
        List<DeviceConnection> result = connections.get(type);
        if (result != null) {
            return result;
        } else {
            result = new ArrayList<>(1);
            connections.put(type, result);
            
            return result;
        }
    }
    
    @Override
    public Iterator<DeviceConnection> iterator() {
        return new ArrayList<List<DeviceConnection>>(connections.values()).parallelStream().flatMap(List::stream).iterator();
    }
    
    public SerialPort[] getAllSerialPorts() {
       return allSerialPorts;
    }
    
    public void setAllSerialPorts(SerialPort[] allSerialPorts) {
        this.allSerialPorts = allSerialPorts;
    }
    
}
