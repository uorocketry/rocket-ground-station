package uorocketry.basestation.connections;

import com.fazecast.jSerialComm.SerialPort;
import uorocketry.basestation.connections.method.ConnectionMethod;
import uorocketry.basestation.connections.method.ConnectionMethodListener;
import uorocketry.basestation.connections.method.SerialConnectionMethod;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.nio.charset.StandardCharsets;

public class DeviceConnection implements ListSelectionListener, MouseListener, ConnectionMethodListener {
    private DeviceConnectionHolder deviceConnectionHolder;
    private DataReceiver[] dataReceivers;

    private ConnectionMethod connectionMethod;
    private int tableIndex;
    private boolean writing;
    
    private JPanel panel;
    private JList<String> selectorList;
    private JLabel successLabel;
    boolean ignoreNextValueChange;

    private final byte[] DELIMITER = "\n".getBytes(StandardCharsets.UTF_8);

    public DeviceConnection(DeviceConnectionHolder deviceConnectionHolder, String name) {
        this.deviceConnectionHolder = deviceConnectionHolder;

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
        if (e.getValueIsAdjusting()) return;
        if (ignoreNextValueChange) {
            ignoreNextValueChange = false;
            return;
        }

        if (e.getSource() == selectorList) {
            // Find what port it was
            if (deviceConnectionHolder.getAllSerialPorts() != null) {
                if (selectorList.getSelectedIndex() != -1) {
                    for (int i = 0; i < deviceConnectionHolder.getAllSerialPorts().length; i++) {
                        // Check if this is the selected com selector
                        String name = deviceConnectionHolder.getAllSerialPorts()[i].getDescriptivePortName();
                        if (name.equals(selectorList.getSelectedValue())) {
                            final SerialPort newSerialPort = deviceConnectionHolder.getAllSerialPorts()[i];
                            
                            if (!isSerialOpen()
                                    || !newSerialPort.getDescriptivePortName().equals(getSerialConnection().getDescriptivePortName())) {
                                // Set to loading
                                successLabel.setText("Loading...");
                                successLabel.setBackground(Color.yellow);

                                new Thread(() -> this.initSerialPort(newSerialPort)).start();
                            }
                            
                            break;
                        }
                    }
                } else if (isSerialOpen()) {
                    for (int i = 0; i < deviceConnectionHolder.getAllSerialPorts().length; i++) {
                        // Check if this is the selected com selector
                        String name = deviceConnectionHolder.getAllSerialPorts()[i].getDescriptivePortName();
                        if (name.equals(getSerialConnection().getDescriptivePortName())) {
                            selectorList.setSelectedIndex(i);
                            
                            break;
                        }
                    }
                }
            }
        }
    }

    private boolean isConnectionOpen() {
        return connectionMethod != null && connectionMethod.isOpen();
    }

    private boolean isSerialConnection() {
        return connectionMethod != null && connectionMethod instanceof SerialConnectionMethod;
    }

    private boolean isSerialOpen() {
        return isConnectionOpen() && isSerialConnection();
    }

    private SerialConnectionMethod getSerialConnection() {
        if (isSerialOpen()) {
            return (SerialConnectionMethod) connectionMethod;
        }

        return null;
    }
    
    public void initSerialPort(SerialPort newSerialPort) {
        if (newSerialPort.isOpen() || (isSerialConnection() && connectionMethod.isConnecting())) return;

        if (isSerialOpen()) {
            // Switching ports, close the old one
            connectionMethod.close();
        }

        connectionMethod = new SerialConnectionMethod(newSerialPort);
        connectionMethod.setConnectionMethodListener(this);

        boolean isOpen = connectionMethod.open();
        if (isOpen) {
            successLabel.setText("Connected");
            successLabel.setBackground(Color.green);
        } else {
            successLabel.setText("FAILED");
            successLabel.setBackground(Color.red);
        }
    }

    @Override
    public void receivedData(byte[] data) {
        if (dataReceivers != null) {
            for (DataReceiver dataReceiver : dataReceivers) {
                dataReceiver.receivedData(this, data);
            }
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            ignoreNextValueChange = true;
            selectorList.clearSelection();
            if (connectionMethod != null) connectionMethod.close();
            
            successLabel.setText("Disconnected");
            successLabel.setBackground(null);
        }
    }

    public void writeBytes(byte[] data) {
        if (isConnectionOpen()) {
            connectionMethod.writeBytes(data);
        }
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
    
    public void setDataReceivers(DataReceiver... dataReceivers) {
        this.dataReceivers = dataReceivers;
    }

    public int getTableIndex() {
        return tableIndex;
    }

    public void setTableIndex(int tableIndex) {
        this.tableIndex = tableIndex;
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
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }
}

