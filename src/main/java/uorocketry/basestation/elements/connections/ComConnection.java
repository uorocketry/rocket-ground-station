package uorocketry.basestation.elements.connections;

import java.awt.Color;
import java.nio.charset.StandardCharsets;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

public class ComConnection implements ListSelectionListener, SerialPortMessageListener {
    private ComConnectionHolder comConnectionHolder;
    private DataReciever dataReciever;

    private SerialPort serialPort;
    private int tableIndex;
    private boolean connecting;
    
    private JPanel panel;
    private JList<String> selectorList;
    private JLabel successLabel;
    
    public ComConnection(ComConnectionHolder comConnectionHolder, DataReciever dataReciever, JPanel panel, JList<String> selectorList, JLabel successLabel) {
        this.comConnectionHolder = comConnectionHolder;
        this.dataReciever = dataReciever;

        this.panel = panel;
        this.selectorList = selectorList;
        this.successLabel = successLabel;
        
        selectorList.addListSelectionListener(this);
    }
    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == selectorList) {
            // Set to loading
            successLabel.setText("Loading...");
            successLabel.setBackground(Color.yellow);
            
            // Find what port it was
            if (comConnectionHolder.getAllSerialPorts() != null) {
                for (int i = 0; i < comConnectionHolder.getAllSerialPorts().length; i++) {
                    // Check if this is the selected com selector
                    if (comConnectionHolder.getAllSerialPorts()[i].getDescriptivePortName().equals(selectorList.getSelectedValue())) {
                        final SerialPort newSerialPort = comConnectionHolder.getAllSerialPorts()[i];
                        
                        new Thread(() -> this.initialisePort(newSerialPort)).start();
                        break;
                    }
                }
            }
        }
    }
    
    public void initialisePort(SerialPort newSerialPort) {
        if (newSerialPort.isOpen() || connecting) return;
        
        if (serialPort != null && serialPort.isOpen()) {
            // Switching ports, close the old one
            serialPort.closePort();
        }
        
        connecting = true;
        serialPort = newSerialPort;
        
        boolean open = serialPort.openPort();
        
        serialPort.setBaudRate(57600);
        
        if (open) {
            successLabel.setText("Connected");
            successLabel.setBackground(Color.green);
        } else {
            successLabel.setText("FAILED");
            successLabel.setBackground(Color.red);
        }
        
        // Setup listener
        serialPort.addDataListener(this);
        
        connecting = false;
    }
    
    

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
    }
    
    public boolean isConnecting() {
        return connecting;
    }

    public void setConnecting(boolean connecting) {
        this.connecting = connecting;
    }

    public JPanel getPanel() {
        return panel;
    }

    public JList<String> getSelectorList() {
        return selectorList;
    }

    public JLabel getSuccessLabel() {
        return successLabel;
    }

    @Override
    public void serialEvent(SerialPortEvent e) {
        dataReciever.recievedData(this, e.getReceivedData());
    }
    
    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
    }
    
    @Override
    public byte[] getMessageDelimiter() {
        return "\n".getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public boolean delimiterIndicatesEndOfMessage() {
        return true; 
    }

}

