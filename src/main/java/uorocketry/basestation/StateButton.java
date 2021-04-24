package uorocketry.basestation;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.json.JSONArray;

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
	
	private static final Color AVAILABLE_COLOR = new Color(0, 33, 115);
	private static final Color SUCCESS_COLOR = new Color(3, 176, 0);
	private static final Color INACTIVE_COLOR = new Color(79, 79, 79);
	
	String name;
	/** What data to send */
	byte[] data;
	/** Which states can this action be completed from */
	int[] availableStates;
	/** Which states means this button has been complete */
	int[] successStates;
	
	private JButton button;
	private JPanel borderPanel;
	private Border defaultButtonBorder;
	
	private List<SerialPort> activeSerialPorts;
	
	public StateButton(List<SerialPort> activeSerialPorts, String name, String data, JSONArray successStates, JSONArray availableStates) {
		this.activeSerialPorts = activeSerialPorts;
		
		this.name = name;
		this.data = new byte[1];
		this.data[0] = Byte.parseByte(data);
		this.successStates = Helper.toIntArray(successStates);
		this.availableStates = Helper.toIntArray(availableStates);
		
		button = new JButton(name);
		button.addActionListener(this);
		button.setFont(new Font("Arial", Font.PLAIN, 20));
		defaultButtonBorder = button.getBorder();
		
		borderPanel = new JPanel();
		borderPanel.setBorder(BorderFactory.createTitledBorder(name));
		borderPanel.add(button);
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
	
	public void stateChanged(int newState) {
		if (Helper.arrayIncludes(availableStates, newState)) {
			button.setForeground(AVAILABLE_COLOR);
		} else if (Helper.arrayIncludes(successStates, newState)) {
			button.setForeground(SUCCESS_COLOR);

		} else {
			button.setForeground(INACTIVE_COLOR);

		}
	}
	
	public JPanel getPanel() {
		return borderPanel;
	}

}
