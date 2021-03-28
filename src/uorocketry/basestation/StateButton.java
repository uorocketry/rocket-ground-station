package uorocketry.basestation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;

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
	
	JButton button;
	
	private List<SerialPort> activeSerialPorts;
	
	public StateButton(List<SerialPort> activeSerialPorts, String name, String data, int stateNumber) {
		this.activeSerialPorts = activeSerialPorts;
		
		this.name = name;
		this.data = data.getBytes();
		this.stateNumber = stateNumber;
		
		button = new JButton(name);
		button.addActionListener(this);
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

}
