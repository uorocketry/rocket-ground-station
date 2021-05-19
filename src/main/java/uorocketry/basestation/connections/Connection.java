package uorocketry.basestation.connections;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.charset.StandardCharsets;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

public class Connection implements ListSelectionListener, MouseListener, SerialPortMessageListener {
    private ConnectionHolder connectionHolder;
    private DataReciever[] dataRecievers;

    private SerialPort serialPort;
    private int tableIndex;
    private boolean connecting;
    private boolean writing;
    
    private JPanel panel;
    private JList<String> selectorList;
    private JLabel successLabel;
    boolean ignoreNextValueChange;
    
    private final byte[] DELIMITER = "\n".getBytes(StandardCharsets.UTF_8);
    
    public Connection(ConnectionHolder connectionHolder, String name) {
        this.connectionHolder = connectionHolder;

        createUI(name);
    }

    private void createUI(String name) {
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(name));

        selectorList = new JList<>();
        selectorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectorList.addListSelectionListener(this);
        selectorList.addMouseListener(this);
        panel.add(selectorList);

        successLabel = new JLabel();
        successLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        successLabel.setHorizontalAlignment(SwingConstants.CENTER);
        successLabel.setOpaque(true);
        panel.add(successLabel);
    }
    
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (ignoreNextValueChange) {
            ignoreNextValueChange = false;
            return;
        }
        
        if (e.getSource() == selectorList) {
            // Find what port it was
            if (connectionHolder.getAllSerialPorts() != null) {
                if (selectorList.getSelectedIndex() != -1) {
                    for (int i = 0; i < connectionHolder.getAllSerialPorts().length; i++) {
                        // Check if this is the selected com selector
                        String name = connectionHolder.getAllSerialPorts()[i].getDescriptivePortName();
                        if (name.equals(selectorList.getSelectedValue())) {
                            final SerialPort newSerialPort = connectionHolder.getAllSerialPorts()[i];
                            
                            if (serialPort == null || !serialPort.isOpen() 
                                    || !newSerialPort.getDescriptivePortName().equals(serialPort.getDescriptivePortName())) {
                                // Set to loading
                                successLabel.setText("Loading...");
                                successLabel.setBackground(Color.yellow);
                                
                                new Thread(() -> this.initialisePort(newSerialPort, name)).start();
                            }
                            
                            break;
                        }
                    }
                } else {
                    for (int i = 0; i < connectionHolder.getAllSerialPorts().length; i++) {
                        // Check if this is the selected com selector
                        String name = connectionHolder.getAllSerialPorts()[i].getDescriptivePortName();
                        if (name.equals(serialPort.getDescriptivePortName())) {
                            selectorList.setSelectedIndex(i);
                            
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public void initialisePort(SerialPort newSerialPort, String name) {
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
    
    @Override
    public void serialEvent(SerialPortEvent e) {
        if (dataRecievers != null) {
            for (DataReciever dataReciever: dataRecievers) {
                dataReciever.recievedData(this, e.getReceivedData());
            }
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            ignoreNextValueChange = true;
            selectorList.clearSelection();
            serialPort.closePort();
            
            successLabel.setText("Disconnected");
            successLabel.setBackground(null);
        }
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
    
    public boolean bytesEqualWithoutDelimiter(byte[] a, byte[] b) {
        if (a.length == 0 || b.length == 0 || 
                a.length != b.length - DELIMITER.length) {
            return false;
        }
            
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        
        for (int i = 0; i < DELIMITER.length; i++) {
            if (DELIMITER[i] != b[a.length + i]) return false;
        }
        
        return true;
    }
    
    public void setDataRecievers(DataReciever... dataRecievers) {
        this.dataRecievers = dataRecievers;
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
    
    public boolean isWriting() {
        return writing;
    }

    public void setWriting(boolean writing) {
        this.writing = writing;
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
    public void mouseEntered(MouseEvent e) {
        
    }

    @Override
    public void mouseExited(MouseEvent e) {
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        
    }

}

