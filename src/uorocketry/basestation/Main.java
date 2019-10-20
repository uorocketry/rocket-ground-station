package uorocketry.basestation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

public class Main implements ChangeListener, SerialPortMessageListener {
	
	/** Constants */
	/** Is this running in simulation mode */
	public static final boolean SIMULATION = false;
	/** The location of the comma separated labels */
	public static final String LABELS_LOCATION = "data/labels.txt";
	/** How many data points are there */
	public static final int DATA_LENGTH = 15;
	/** Separator for the data */
	public static final String SEPARATOR = ";";
	/** Data file location for the simulation (new line separated for each event) */
	public static final String SIM_DATA_LOCATION = "data/data.txt";
	
	List<DataHandler> allData = new ArrayList<>();
	
	String[] labels = new String[DATA_LENGTH];
	
	/** Index of the current data point being looked at */
	int currentDataIndex = 0;
	
	/** If {@link currentDataIndex} should be set to the latest message */
	boolean latest = true;
	
	/** If not in a simulation, the serial port being listened to */
	SerialPort activeSerialPort;
	
	Window window;
	
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		// Create window
		window = new Window();
		
		// Add slider listener
		window.slider.addChangeListener(this);
		
		// Load simulation data if necessary
		if (SIMULATION) {
			loadSimulationData();
		}
		
		//Setup com ports if not a simulation
		if (!SIMULATION) {
			setupSerialComs();
		}
		
		// Update UI once
		updateUI();
	}
	
	public void setupSerialComs() {
		SerialPort[] ports = SerialPort.getCommPorts();
		
		// Grab just the first port for now
		if (ports.length > 0) {
			activeSerialPort = ports[0];
			
			// Setup listener
			activeSerialPort.addDataListener(this);
		}
	}
	
	public void updateUI() {
		// If not ready yet
		if (allData.size() == 0) return;
		
		//set max value of the slider
		window.slider.setMaximum(allData.size() - 1);
		
		DataHandler currentDataHandler = allData.get(currentDataIndex);
		
		if (currentDataHandler != null) {
			currentDataHandler.updateTableUIWithData(window.dataTable, labels);
		} else {
			setTableToError(window.dataTable);
		}
	}
	
	public void setTableToError(JTable table) {
		TableModel tableModel = table.getModel();
		
		// Set first item to "Error"
		tableModel.setValueAt("Parsing Error", 0, 0);
		tableModel.setValueAt(currentDataIndex, 0, 1);
		
		for (int i = 1; i < DATA_LENGTH; i++) {
			// Set label
			tableModel.setValueAt("", i, 0);
			
			// Set data
			tableModel.setValueAt("", i, 1);
		}
	}
	
	/** 
	 * Run once at the beginning of simulation mode
	 */
	public void loadSimulationData() {
		// Load simulation data
		loadSimulationData(SIM_DATA_LOCATION);
		
		// Load labels
		loadLabels(LABELS_LOCATION);
	}
	
	public void loadSimulationData(String fileName) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			try {
			    String line = null;

			    while ((line = br.readLine()) != null) {
			        // Parse this line and add it as a data point
			        allData.add(parseData(line));
			    }
			} finally {
			    br.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public DataHandler parseData(String data) {
		DataHandler dataHandler = new DataHandler();
		
		// Clear out the b' ' stuff added that is only meant for the radio to see
		data = data.replaceAll("b'|\\\\r\\\\n'", "");
		
		// Semi-colon separated
		String[] splitData = data.split(SEPARATOR);
		if (splitData.length != dataHandler.data.length) {
			//invalid data
			System.err.println("Line with invalid data (Not the correct amount of data)");
			
			return null;
		}
		
		
		for (int i = 0; i < splitData.length; i++) {
			dataHandler.set(i, splitData[i]);
		}
		
		return dataHandler;
	}
	
	public void loadLabels(String fileName) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			try {
			    String line = null;

			    while ((line = br.readLine()) != null) {
			    	//this line contains all of the labels
			    	//this one is comma separated, not the same as the actual data
			    	labels = line.split(",");
			    }
			} finally {
			    br.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Triggered every time the slider changes
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == window.slider) {
			currentDataIndex = window.slider.getValue();
			
			updateUI();
		}
	}

	@Override
	public int getListeningEvents() {
		return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
	}
	
	@Override
   public byte[] getMessageDelimiter() {
		return "\n".getBytes(StandardCharsets.UTF_8);
	}

   @Override
   public boolean delimiterIndicatesEndOfMessage() {
	   return true; 
   }

	@Override
	public void serialEvent(SerialPortEvent e) {
		String delimitedMessage = new String(e.getReceivedData(), StandardCharsets.UTF_8);
		
		allData.add(parseData(delimitedMessage));
		
		updateUI();
	}
}
