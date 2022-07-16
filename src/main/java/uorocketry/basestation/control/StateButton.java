package uorocketry.basestation.control;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import uorocketry.basestation.config.DataSet;
import uorocketry.basestation.config.StateEvent;
import uorocketry.basestation.connections.DataReceiver;
import uorocketry.basestation.connections.DeviceConnection;
import uorocketry.basestation.connections.DeviceConnectionHolder;

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
	long[] availableStates;
	/** Which states means this button has been complete */
	long[] successStates;

	private State state;

	private JButton button;
	private JPanel borderPanel;
	private Timer timer = new Timer();

	// Only true if this is a servo control button
	private JPanel servoControls;

	private DeviceConnectionHolder deviceConnectionHolder;
	private DataSet dataSet;

	public StateButton(DeviceConnectionHolder deviceConnectionHolder, DataSet dataSet, StateEvent stateEvent) {
		this.deviceConnectionHolder = deviceConnectionHolder;
		this.dataSet = dataSet;

		this.name = stateEvent.getName();
		// convert stateevent.getdata() to byte

		this.data = new byte[] { stateEvent.getData().byteValue() };
		this.successStates = stateEvent.getSuccessStates();
		this.availableStates = stateEvent.getAvailableStates();

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

			String[] valves = {
					"VENT",
					"IGNITER",
					"MAIN",
					"PINHOLE",
					"FILL"
			};

			JCheckBox[] checkBoxes = new JCheckBox[valves.length];
			for (int i = 0; i < checkBoxes.length; i++) {
				checkBoxes[i] = new JCheckBox(valves[i] + " (C)");
				checkBoxes[i].setAlignmentX(Component.CENTER_ALIGNMENT);
				servoControls.add(checkBoxes[i]);

				final int index = i;
				checkBoxes[i].addActionListener((l) -> checkBoxes[index]
						.setText(valves[index] + (checkBoxes[index].isSelected() ? " (O)" : " (C)")));
			}

			JButton send = new JButton("Send");
			send.setAlignmentX(Component.CENTER_ALIGNMENT);
			servoControls.add(send);
			send.addActionListener((e) -> {
				byte sendBit = 1;
				for (int i = 0; i < checkBoxes.length; i++) {
					if (checkBoxes[i].isSelected()) {
						sendBit += 1 << (i + 1);
					}
				}

				deviceConnectionHolder.get(TABLE_INDEX).writeBytes(new byte[] { sendBit });
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
				public void keyPressed(KeyEvent e) {
				}

				@Override
				public void keyReleased(KeyEvent e) {
					try {
						byte parsedState = Byte.parseByte(returnStateTextField.getText());
						returnStateStatus.setText(dataSet.getStates()[parsedState]);
					} catch (NumberFormatException ex) {
					}
				}
			});

			JButton returnStateButton = new JButton("Return To State");
			returnStateButton.addActionListener((e) -> {
				try {
					byte parsedState = Byte.parseByte(returnStateTextField.getText());
					byte sendBit = (byte) (parsedState << 1);

					deviceConnectionHolder.get(TABLE_INDEX).writeBytes(new byte[] { sendBit });
					returnStateStatus.setText(dataSet.getStates()[parsedState]);
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

	public void stateChanged(long newState) {
		// New state in availible states
		var inAvailible = false;
		for (long state : availableStates) {
			if (state == newState) {
				inAvailible = true;
				break;
			}
		}
		var inSuccess = false;
		for (long state : successStates) {
			if (state == newState) {
				inSuccess = true;
				break;
			}
		}

		if (inAvailible) {
			button.setForeground(AVAILABLE_COLOR);
			state = State.AVAILABLE;
		} else if (inSuccess) {
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
