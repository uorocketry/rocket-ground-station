package uorocketry.basestation.control;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;

import org.json.JSONArray;

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
	private static final int TABLE_INDEX = 0;
	
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
	
	public StateButton(DeviceConnectionHolder deviceConnectionHolder, String name, byte data, JSONArray successStates, JSONArray availableStates) {
		this.deviceConnectionHolder = deviceConnectionHolder;
		
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

			String[] valves = {
					"SV01",
					"SV02 (Unused)",
					"SBV01",
					"SBV02",
					"SBV03"
			};

			JCheckBox[] checkBoxes = new JCheckBox[valves.length];
			for (int i = 0; i < checkBoxes.length; i++) {
				checkBoxes[i] = new JCheckBox(valves[i]);
				servoControls.add(checkBoxes[i]);
			}

			JButton send = new JButton("Send");
			servoControls.add(send);
			send.addActionListener((e) -> {
				byte sendBit = 1;
				for (int i = 0; i < checkBoxes.length; i++) {
					if (checkBoxes[i].isSelected()) {
						sendBit += 1 << (i + 1);
					}
				}

				deviceConnectionHolder.get(TABLE_INDEX).writeBytes(new byte[] {sendBit});
			});

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
