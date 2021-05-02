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

public class ComConnectionHolder implements Iterable<ComConnection> {
    /** Connection data and related UI */
    private Map<Type, List<ComConnection>> connections = new HashMap<>();
    
    /** All the serial ports found */
    private SerialPort[] allSerialPorts;

    public enum Type {
        TABLE,
        BUTTON_BOX
    }
    
    public ComConnection add(Type type, DataReciever dataReciever, JPanel panel, JList<String> selectorList, JLabel successLabel) {
        ComConnection comConnection = new ComConnection(this, dataReciever, panel, selectorList, successLabel);
        getList(type).add(comConnection);

        return comConnection;
    }
    
    public ComConnection get(int tableIndex) {
        return getList(Type.TABLE).get(tableIndex);
    }
    
    private List<ComConnection> getList(Type type) {
        List<ComConnection> result = connections.get(type);
        if (result != null) {
            return result;
        } else {
            result = new ArrayList<>(1);
            connections.put(type, result);
            
            return result;
        }
    }
    
    @Override
    public Iterator<ComConnection> iterator() {
        return new ArrayList<List<ComConnection>>(connections.values()).parallelStream().flatMap(List::stream).iterator();
    }
    
    public SerialPort[] getAllSerialPorts() {
       return allSerialPorts;
    }
    
    public void setAllSerialPorts(SerialPort[] allSerialPorts) {
        this.allSerialPorts = allSerialPorts;
    }
    
}
