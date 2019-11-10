package uorocketry.basestation;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.knowm.xchart.XYChart;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

public class Main implements ComponentListener, ChangeListener, ActionListener, ListSelectionListener, SerialPortMessageListener {
	
	/** Constants */
	/** Is this running in simulation mode */
	public static final boolean SIMULATION = true;
	/** The location of the comma separated labels */
	public static final String LABELS_LOCATION = "data/labels.txt";
	/** How many data points are there */
	public static final int DATA_LENGTH = 15;
	/** Separator for the data */
	public static final String SEPARATOR = ";";
	/** Data file location for the simulation (new line separated for each event) */
	public static final String SIM_DATA_LOCATION = "data/data.txt";
	
	/** Whether to update Google Earth file */
	public static final boolean GOOGLE_EARTH = true;
	/** Where the updating Google Earth kml file is stored */
	public static final String GOOGLE_EARTH_DATA_LOCATION = "data/positions.kml";
	
	List<DataHandler> allData = new ArrayList<>();
	
	String[] labels = new String[DATA_LENGTH];
	
	/** Index of the current data point being looked at */
	int currentDataIndex = 0;
	
	/** If {@link currentDataIndex} should be set to the latest message */
	boolean latest = true;
	/** If true, slider will temporarily stop growing */
	boolean paused = false;
	
	/** If not in a simulation, the serial port being listened to */
	SerialPort activeSerialPort;
	
	Window window;
	
	/** All the serial ports found */
	SerialPort[] allSerialPorts;
	boolean connectingToSerial = false;
	
	/** Used for the map view */
	GoogleEarthUpdater googleEarthUpdater = new GoogleEarthUpdater();
	
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		// Create window
		window = new Window();
		
		window.addComponentListener(this);
		
		setupUI();
		
		// Load labels
		loadLabels(LABELS_LOCATION);
		
		// Load simulation data if necessary
		if (SIMULATION) {
			loadSimulationData();
		}
		
		// Setup com ports if not a simulation
		if (!SIMULATION) {
			setupSerialComs();
		}
		
		// Setup Google Earth map support
		if (GOOGLE_EARTH) {
			googleEarthUpdater = new GoogleEarthUpdater();
		}
		
		// Update UI once
		updateUI();
	}
	
	public void setupSerialComs() {
		allSerialPorts = SerialPort.getCommPorts();
		
		// Make array for the selector
		String[] comSelectorData = new String[allSerialPorts.length];
		
		for (int i = 0; i < allSerialPorts.length; i++) {
			comSelectorData[i]=  allSerialPorts[i].getDescriptivePortName();
		}

		window.comSelector.setListData(comSelectorData);
	}
	
	public void initialisePort(SerialPort serialPort) {
		if (serialPort.isOpen() || connectingToSerial) return;
		
		if (activeSerialPort != null && activeSerialPort.isOpen()) {
			activeSerialPort.closePort();
		}
		
		activeSerialPort = serialPort;
		
		connectingToSerial = true;
		
		boolean open = activeSerialPort.openPort();
		
		activeSerialPort.setBaudRate(57600);
		
		if (open) {
			window.comConnectionSuccess.setText("Connected");
			window.comConnectionSuccess.setBackground(Color.green);
		} else {
			window.comConnectionSuccess.setText("FAILED");
			window.comConnectionSuccess.setBackground(Color.red);
		}
		
		// Setup listener
		activeSerialPort.addDataListener(this);
		
		connectingToSerial = false;
	}
	
	public void setupUI() {
		// Add slider listener
		window.slider.addChangeListener(this);
		
		// Buttons
		window.pauseButton.addActionListener(this);
		window.latestButton.addActionListener(this);
		
		// Com selector
		window.comSelector.addListSelectionListener(this);
		
		// Setup listeners for table
		window.dataTable.getSelectionModel().addListSelectionListener(this);
	}
	
	public void updateUI() {
		// If not ready yet
		if (allData.size() == 0) return;
		
		// Don't change slider if paused
		if (!paused) {
			// Set max value of the slider
			window.slider.setMaximum(allData.size() - 1);
		}
		
		DataHandler currentDataHandler = allData.get(currentDataIndex);
		
		if (currentDataHandler != null) {
			currentDataHandler.updateTableUIWithData(window.dataTable, labels);
		} else {
			setTableToError(window.dataTable);
		}
		
		updateChart(window.mainChart);
		
		if (GOOGLE_EARTH) googleEarthUpdater.updateKMLFile(allData, currentDataIndex);
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
	 * Update the chart with data up to currentDataIndex, and then call window.repaint()
	 * 
	 * @param chart The chart to update
	 */
	public void updateChart(DataChart chart) {
		// Update altitude chart
		ArrayList<Float> altitudeDataX = new ArrayList<>();
		ArrayList<Float> altitudeDataY = new ArrayList<>();
		
		for (int i = 0; i <= currentDataIndex; i++) {
			DataHandler data = allData.get(i);
			
			if (data != null) {
				altitudeDataX.add(data.data[DataHandler.TIMESTAMP].getDecimalValue() / 1000);
				altitudeDataY.add(data.data[chart.xType].getDecimalValue());
			}
		}
		
		// Set Labels
		chart.xyChart.setTitle(labels[chart.xType] + " vs Timestamp");
		chart.xyChart.setYAxisTitle(labels[chart.xType]);
		
		chart.xyChart.updateXYSeries("chart1", altitudeDataX, altitudeDataY, null);
		window.repaint();
	}
	
	/** 
	 * Run once at the beginning of simulation mode
	 */
	public void loadSimulationData() {
		// Load simulation data
		loadSimulationData(SIM_DATA_LOCATION);
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
		    String line = null;

		    while ((line = br.readLine()) != null) {
		    	//this line contains all of the labels
		    	//this one is comma separated, not the same as the actual data
		    	labels = line.split(",");
		    }
		    
		    br.close();
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
			
			// Update the latest value
			latest = currentDataIndex == window.slider.getMaximum() - 1;
		}
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

	@Override
	public void serialEvent(SerialPortEvent e) {
		String delimitedMessage = new String(e.getReceivedData(), StandardCharsets.UTF_8);
		
		allData.add(parseData(delimitedMessage));
		
		updateUI();
		
		// Move position to end
		if (latest) {
			window.slider.setValue(allData.size() - 1);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == window.pauseButton) {
			paused = !paused;
			
			if (paused) {
				window.pauseButton.setText("Resume");
			} else {
				window.pauseButton.setText("Pause");
			}
			
		} else if (e.getSource() == window.latestButton) {
			window.slider.setValue(allData.size() - 1);
		}
	}

	/** For com selector JList */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() == window.comSelector) {
			// Set to loading
			window.comConnectionSuccess.setText("Loading...");
			window.comConnectionSuccess.setBackground(Color.yellow);
			
			// Find what port it was
			if (allSerialPorts != null) {
				for (int i = 0; i < allSerialPorts.length; i++) {
					if (allSerialPorts[i].getDescriptivePortName().equals(window.comSelector.getSelectedValue())) {
						// This is the one
						
						final SerialPort newSerialPort = allSerialPorts[i];
						
						// Do it in an other thread
						Thread thread = new Thread() {
							public void run() {
								initialisePort(newSerialPort);
							}
						};
						thread.start();
						
						break;
					}
				}
			}
		} else if (e.getSource() == window.dataTable.getSelectionModel()) {
			int selectedRow = window.dataTable.getSelectedRow();
			
			System.out.println(selectedRow);
			
			// Set chart to be based on this row
			window.mainChart.xType = selectedRow;
			updateUI();
		}
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		
	}

	@Override
	public void componentResized(ComponentEvent e) {

	}

	@Override
	public void componentShown(ComponentEvent e) {
		
	}
}
