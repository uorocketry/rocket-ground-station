package uorocketry.basestation;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import com.fazecast.jSerialComm.SerialPort;

/**
 * DOES NOT support multiple data sources 
 * (Hardcoded, fixable)
 * 
 * @author Ajay
 *
 */
public class StateButton implements ActionListener {
	
	// Always zero for now
	private static final int TABLE_INDEX = 0;
	
	String name;
	/** What data to send */
	byte[] data;
	/** Which state means this button has been complete */
	int stateNumber;
	
	private JButton button;
	private JPanel border;
	
	private List<SerialPort> activeSerialPorts;
	
	public StateButton(List<SerialPort> activeSerialPorts, String name, String data, int stateNumber) {
		this.activeSerialPorts = activeSerialPorts;
		
		this.name = name;
		this.data = data.getBytes();
		this.stateNumber = stateNumber;
		
		button = new JButton(name);
		button.addActionListener(this);
		button.setFont(new Font("Arial", Font.PLAIN, 20));
		
		border = new JPanel();
		border.setBorder(BorderFactory.createTitledBorder(name));
		border.add(button);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == button) {
			SerialPort serialPort = activeSerialPorts.get(TABLE_INDEX);
			if (serialPort != null) {
				serialPort.writeBytes(data, data.length);
			}
		}
	}
	
	public JPanel getPanel() {
		return border;
	}

}
