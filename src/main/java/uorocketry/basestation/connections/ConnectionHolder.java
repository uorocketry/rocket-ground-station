package uorocketry.basestation.connections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import com.fazecast.jSerialComm.SerialPort;

public class ConnectionHolder implements Iterable<Connection> {
    /** Connection data and related UI */
    private Map<Type, List<Connection>> connections = new HashMap<>();
    
    /** All the serial ports found */
    private SerialPort[] allSerialPorts;

    public enum Type {
        TABLE,
        BUTTON_BOX
    }
    
    public Connection add(Type type, DataReciever[] dataRecievers, String name) {
        Connection connection = new Connection(this, name);
        connection.setDataRecievers(dataRecievers);
        getList(type).add(connection);

        return connection;
    }
    
    public Connection get(int tableIndex) {
        return getList(Type.TABLE).get(tableIndex);
    }
    
    private List<Connection> getList(Type type) {
        List<Connection> result = connections.get(type);
        if (result != null) {
            return result;
        } else {
            result = new ArrayList<>(1);
            connections.put(type, result);
            
            return result;
        }
    }
    
    @Override
    public Iterator<Connection> iterator() {
        return new ArrayList<List<Connection>>(connections.values()).parallelStream().flatMap(List::stream).iterator();
    }
    
    public SerialPort[] getAllSerialPorts() {
       return allSerialPorts;
    }
    
    public void setAllSerialPorts(SerialPort[] allSerialPorts) {
        this.allSerialPorts = allSerialPorts;
    }
    
}
