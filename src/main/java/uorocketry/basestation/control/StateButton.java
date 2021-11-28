package uorocketry.basestation.control;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;

import org.json.JSONArray;

import uorocketry.basestation.config.DataSet;
import uorocketry.basestation.connections.DeviceConnection;
import uorocketry.basestation.connections.DeviceConnectionHolder;
import uorocketry.basestation.connections.DataReceiver;
import uorocketry.basestation.helper.Helper;

/**
 * DOES NOT support multiple data sources 
 * (Hardcoded, fixable)
 * 
 * @author Ajay
 *
 */
public class StateButton implements ActionListener, DataReceiver {

	enum State {
		INACTIVE,
		AVAILABLE,
		SUCCESS
	};

	// Always zero for now
	public static final int TABLE_INDEX = 0;
	
	private static final Color AVAILABLE_COLOR = new Color(0, 33, 115);
	private static final Color SUCCESS_COLOR = new Color(3, 176, 0);
	private static final Color INACTIVE_COLOR = new Color(79, 79, 79);
    private static final Color CLICKED_COLOR = new Color(166, 178, 255);
	
	String name;
	/** What data to send */
	byte[] data;
	/** Which states can this action be completed from */
	int[] availableStates;
	/** Which states means this button has been complete */
	int[] successStates;

	private State state;
	
	private JButton button;
	private JPanel borderPanel;
    private Timer timer = new Timer();

    // Only true if this is a servo control button
	private JPanel servoControls;

	private DeviceConnectionHolder deviceConnectionHolder;
	private DataSet dataSet;
	
	public StateButton(DeviceConnectionHolder deviceConnectionHolder, DataSet dataSet, String name, byte data, JSONArray successStates, JSONArray availableStates) {
		this.deviceConnectionHolder = deviceConnectionHolder;
		this.dataSet = dataSet;
		
		this.name = name;
		this.data = new byte[] { data };
		this.successStates = Helper.toIntArray(successStates);
		this.availableStates = Helper.toIntArray(availableStates);
		
		button = new JButton(name);
		button.addActionListener(this);
		button.setFont(new Font("Arial", Font.PLAIN, 20));
		button.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		borderPanel = new JPanel();
		borderPanel.setLayout(new BoxLayout(borderPanel, BoxLayout.Y_AXIS));
		borderPanel.setBorder(BorderFactory.createTitledBorder(name));
		borderPanel.add(button);

		if (name.equals("Servo Control")) {
			servoControls = new JPanel();
			servoControls.setLayout(new BoxLayout(servoControls, BoxLayout.Y_AXIS));

			JPanel valveControls = new JPanel();
			servoControls.add(valveControls);

			String[] valves = {
					"VENT",
					"SV02 (Unused)",
					"MAIN",
					"PINHOLE",
					"FILL"
			};

			JCheckBox[] checkBoxes = new JCheckBox[valves.length];
			for (int i = 0; i < checkBoxes.length; i++) {
				checkBoxes[i] = new JCheckBox(valves[i]);
				valveControls.add(checkBoxes[i]);
			}

			JButton send = new JButton("Send");
			valveControls.add(send);
			send.addActionListener((e) -> {
				byte sendBit = 1;
				for (int i = 0; i < checkBoxes.length; i++) {
					if (checkBoxes[i].isSelected()) {
						sendBit += 1 << (i + 1);
					}
				}

				deviceConnectionHolder.get(TABLE_INDEX).writeBytes(new byte[] {sendBit});
			});

			// Go back to state
			JPanel exitServoControlContainer = new JPanel();
			servoControls.add(exitServoControlContainer);
			JLabel returnStateStatus = new JLabel();
			JTextField returnStateTextField = new JTextField(1);
			returnStateTextField.setMaximumSize(returnStateTextField.getPreferredSize());
			returnStateTextField.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent e) {

				}
				@Override
				public void keyPressed(KeyEvent e) {}
				@Override
				public void keyReleased(KeyEvent e) {
					try {
						byte parsedState = Byte.parseByte(returnStateTextField.getText());
						returnStateStatus.setText(dataSet.getState(parsedState));
					} catch (NumberFormatException ex) {}
				}
			});

			JButton returnStateButton = new JButton("Return To State");
			returnStateButton.addActionListener((e) -> {
				try {
					byte parsedState = Byte.parseByte(returnStateTextField.getText());
					byte sendBit = (byte) (parsedState << 1);

					deviceConnectionHolder.get(TABLE_INDEX).writeBytes(new byte[] {sendBit});
					returnStateStatus.setText(dataSet.getState(parsedState));
				} catch (NumberFormatException ex) {
					JOptionPane.showMessageDialog(null, "Not a valid state");
				}
			});

			exitServoControlContainer.add(returnStateStatus);
			exitServoControlContainer.add(returnStateTextField);
			exitServoControlContainer.add(returnStateButton);

			servoControls.setVisible(false);
			borderPanel.add(servoControls);
		}
	}
	
	public void sendAction() {
		deviceConnectionHolder.get(TABLE_INDEX).writeBytes(data);
    }
	
	public void stateChanged(int newState) {
        if (Helper.arrayIncludes(availableStates, newState)) {
            button.setForeground(AVAILABLE_COLOR);
			state = State.AVAILABLE;
        } else if (Helper.arrayIncludes(successStates, newState)) {
            button.setForeground(SUCCESS_COLOR);
            state = State.SUCCESS;
        } else {
            button.setForeground(INACTIVE_COLOR);
            state = State.INACTIVE;
        }

        if (servoControls != null) {
			servoControls.setVisible(state == State.SUCCESS);
		}
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == button) {
		    sendAction();
		}
	}
	
	@Override
    public void receivedData(DeviceConnection deviceConnection, byte[] data) {
	    if (deviceConnectionHolder.get(TABLE_INDEX).bytesEqualWithoutDelimiter(this.data, data)) {
	        sendAction();
	        
	        borderPanel.setBackground(CLICKED_COLOR);
	        timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    borderPanel.setBackground(null);
                }
            }, 750);
	    }
    }
	
	public JPanel getPanel() {
		return borderPanel;
	}

}
