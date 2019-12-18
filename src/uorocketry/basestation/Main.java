package uorocketry.basestation;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

public class Main implements ComponentListener, ChangeListener, ActionListener, MouseListener, ListSelectionListener, SerialPortMessageListener, SnapPanelListener {
	
	/** Constants */
	/** The location of the comma separated labels */
	public static final String LABELS_LOCATION = "data/labels.txt";
	/** How many data points are there */
	public static int dataLength = 15;
	/** Separator for the data */
	public static final String SEPARATOR = ";";
	/** Data file location for the simulation (new line separated for each event) */
	public static final String SIM_DATA_LOCATION = "data/data.txt";
	
	/** Whether to update Google Earth file */
	public static boolean googleEarth = false;
	/** Where the updating Google Earth kml file is stored */
	public static final String GOOGLE_EARTH_DATA_LOCATION = "data/positions.kml";
	
	/** Where to save the log file */
	public static final String LOG_FILE_SAVE_LOCATION = "data/";
	public static final String DEFAULT_LOG_FILE_NAME = "log.txt";
	/** Will have a number appended to the end to not overwrite old logs */
	String currentLogFileName = DEFAULT_LOG_FILE_NAME;
	
	/** Is this running in simulation mode. Must be set at the beginning as it changes the setup. */
	public static boolean simulation = false;
	
	List<DataHandler> allData = new ArrayList<>();
	
	String[] labels = new String[dataLength];
	
	/** Index of the current data point being looked at */
	int currentDataIndex = 0;
	/** Index of the minimum data point being looked at */
	int minDataIndex = 0;
	
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
	
	/** The chart last clicked */
	DataChart selectedChart;
	Border selectionBorder = BorderFactory.createLineBorder(Color.blue);
	
	/** The width and height of the chart container to resize elements inside on resize. */
	int chartContainerWidth = -1;
	int chartContainerHeight = -1;
	
	/** Set to true when automatically selecting or deselcting from the data table */
	boolean ignoreSelections = false;
	
	/** What will be written to the log file */
	StringBuilder logFileStringBuilder = new StringBuilder();
	/** Is the log file being currently updated */
	boolean currentlyWriting;
	
	public static void main(String[] args) {
		// Find different possible commands
		for (int i = 0; i + 1 < args.length; i++) {
			switch(args[i]) {
			case "--sim":
				simulation = Boolean.parseBoolean(args[i + 1]);
				
				break;
			case "--dataLength":
				try {
					dataLength = Integer.parseInt(args[i + 1]);
				} catch (NumberFormatException e) {
					System.err.println("Failed to interpret " + args[i] + " " + args[i + 1]);
				}
				
				break;
			}
		}
		
		new Main();
	}
	
	public Main() {
		// Create window
		window = new Window();
		
		window.addComponentListener(this);
		
		setupUI();
		
		// Load labels
		loadLabels(LABELS_LOCATION);
		
		// Different setups depending on if simulation or not
		setupData();
		
		// Setup Google Earth map support
		if (googleEarth) {
			googleEarthUpdater = new GoogleEarthUpdater();
		}
		
		// Update UI once
		updateUI();
	}
	
	public void setupData() {
		allData = new ArrayList<DataHandler>();
		minDataIndex = 0;
		currentDataIndex = 0;
		
		// Reset sliders
		window.maxSlider.setValue(0);
		window.minSlider.setValue(0);
		
		// Load simulation data if necessary
		if (simulation) {
			loadSimulationData();
			
			window.savingToLabel.setText("");
		}
		
		// Setup com ports if not a simulation
		if (!simulation) {
			setupSerialComs();
			
			setupLogFileName();
		}
		
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
	
	public void setupLogFileName() {
		// Figure out file name for logging
		File folder = new File(LOG_FILE_SAVE_LOCATION);
		File[] listOfLogFiles = folder.listFiles();
		Set<String> usedFileNames = new HashSet<String>();
		
		for (File file: listOfLogFiles) {
			if (file.isFile() && file.getName().contains(DEFAULT_LOG_FILE_NAME)) {
				usedFileNames.add(file.getName());
			}
		}
		
		// Find a suitable filename
		int logIndex = 0;
		while (usedFileNames.contains(logIndex + "_" + DEFAULT_LOG_FILE_NAME)) {
			logIndex++;
		}
		
		// Set the name
		currentLogFileName = logIndex + DEFAULT_LOG_FILE_NAME;
		
		window.savingToLabel.setText("Saving to " + LOG_FILE_SAVE_LOCATION + currentLogFileName);
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
		// Add slider listeners
		window.maxSlider.addChangeListener(this);
		window.minSlider.addChangeListener(this);
		
		// Buttons
		window.pauseButton.addActionListener(this);
		window.latestButton.addActionListener(this);
		
		window.addChartButton.addActionListener(this);
		
		window.dataLengthButton.addActionListener(this);
		window.dataLengthTextBox.setText(dataLength + "");
		
		// Checkboxes
		window.googleEarthCheckBox.addActionListener(this);
		window.simulationCheckBox.addActionListener(this);
		
		// Set simulation checkbox to be default
		window.simulationCheckBox.setSelected(simulation);
		
		// Com selector
		window.comSelector.addListSelectionListener(this);
		
		// Setup listeners for table
		window.dataTable.getSelectionModel().addListSelectionListener(this);
		window.dataTable.addMouseListener(this);
		
		// Setup Snap Panel system
		selectedChart = window.charts.get(0);
		selectedChart.snapPanel.setSnapPanelListener(this);

		snapPanelSelected(selectedChart.snapPanel);
	}
	
	public void updateUI() {
		// If not ready yet
		if (allData.size() == 0) return;
		
		// Don't change slider if paused
		if (!paused) {
			// Set max value of the sliders
			window.maxSlider.setMaximum(allData.size() - 1);
			window.minSlider.setMaximum(allData.size() - 1);
		}
		
		DataHandler currentDataHandler = allData.get(currentDataIndex);
		
		if (currentDataHandler != null) {
			currentDataHandler.updateTableUIWithData(window.dataTable, labels);
		} else {
			setTableToError(window.dataTable);
		}
		
		for (DataChart chart : window.charts) {
			updateChart(chart);
		}
		
		if (googleEarth) googleEarthUpdater.updateKMLFile(allData, currentDataIndex);
	}
	
	public void setTableToError(JTable table) {
		TableModel tableModel = table.getModel();
		
		// Set first item to "Error"
		tableModel.setValueAt("Parsing Error", 0, 0);
		tableModel.setValueAt(currentDataIndex, 0, 1);
		
		for (int i = 1; i < dataLength; i++) {
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
		ArrayList<ArrayList<Float>> altitudeDataY = new ArrayList<ArrayList<Float>>();
		
		// Add all array lists
		for (int i = 0; i < chart.xTypes.length; i++) {
			altitudeDataY.add(new ArrayList<Float>());
		}
		
		for (int i = minDataIndex; i <= currentDataIndex; i++) {
			DataHandler data = allData.get(i);
			
			if (data != null) {
				altitudeDataX.add(data.data[chart.yType].getDecimalValue() / 1000);
				
				for (int j = 0; j < chart.xTypes.length; j++) {
					altitudeDataY.get(j).add(data.data[chart.xTypes[j]].getDecimalValue());
				}
			}
		}
		
		if (altitudeDataX.size() == 0) {
			// Add default data
			altitudeDataX.add(0f);
			
			for (int j = 0; j < chart.xTypes.length; j++) {
				altitudeDataY.get(j).add(0f);
			};
		}
		
		String[] newActiveSeries = new String[chart.xTypes.length];
		StringBuilder title = new StringBuilder();
		
		// Set Labels
		for (int i = 0; i < chart.xTypes.length; i++) {
			if (title.length() != 0) title.append(", ");
			title.append(labels[chart.xTypes[i]]);
			
			chart.xyChart.setYAxisTitle(labels[chart.xTypes[i]]);
			
			if (chart.activeSeries.length > i) {
				chart.xyChart.updateXYSeries("series" + i, altitudeDataX, altitudeDataY.get(i), null);

			} else {
				chart.xyChart.addSeries("series" + i, altitudeDataX, altitudeDataY.get(i), null);
			}
			
			newActiveSeries[i] = "series" + i;
		}
		
		chart.xyChart.setTitle(title + " vs " + labels[chart.yType]);
		
		if (chart.xTypes.length > 1) {
			chart.xyChart.setYAxisTitle("Value");
		}
		
		chart.xyChart.setXAxisTitle(labels[chart.yType]);
		
		// Remove extra series
		for (int i = chart.xTypes.length; i < chart.activeSeries.length; i++) {
			chart.xyChart.removeSeries("series" + i);
		}
		
		chart.activeSeries = newActiveSeries;
		
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
		if (e.getSource() == window.maxSlider) {
			currentDataIndex = window.maxSlider.getValue();
			
			// Check if min is too high
			if (minDataIndex > currentDataIndex) {
				minDataIndex = currentDataIndex;
				window.minSlider.setValue(minDataIndex);
			}
			
			updateUI();
			
			// Update the latest value
			latest = currentDataIndex == window.maxSlider.getMaximum() - 1;
		} else if (e.getSource() == window.minSlider) {
			minDataIndex = window.minSlider.getValue();
			
			// Check if min is too high
			if (minDataIndex > currentDataIndex) {
				minDataIndex = currentDataIndex;
				window.minSlider.setValue(minDataIndex);
			}
			
			updateUI();
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
			window.maxSlider.setValue(allData.size() - 1);
		}
		
		// Add this message to the log file
		logFileStringBuilder.append(delimitedMessage);
		logFileStringBuilder.append("\n");
		
		// Get string
		String logFileString = logFileStringBuilder.toString();
		
		if (!currentlyWriting) {
			currentlyWriting = true;

			// Write to file
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(LOG_FILE_SAVE_LOCATION + currentLogFileName), StandardCharsets.UTF_8))) {
			   writer.write(logFileString);
			} catch (IOException err) {
				err.printStackTrace();
			}
			
			currentlyWriting = false;
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
			window.maxSlider.setValue(allData.size() - 1);
		} else if (e.getSource() == window.addChartButton) {
			addChart();
		} else if (e.getSource() == window.googleEarthCheckBox) {
			googleEarth = window.googleEarthCheckBox.isSelected();
		} else if (e.getSource() == window.simulationCheckBox && window.simulationCheckBox.isSelected() != simulation) {
			String warningMessage = "";
			if (window.simulationCheckBox.isSelected()) {
				warningMessage = "Are you sure you would like to enable simulation mode?\n\n"
						+ "The current data will be deleted from the UI. You can find it in " + LOG_FILE_SAVE_LOCATION + currentLogFileName;
			} else {
				warningMessage = "Are you sure you would like to disable simulation mode?";
			}
			
			if (JOptionPane.showConfirmDialog(window, warningMessage) == 0) {
				simulation = window.simulationCheckBox.isSelected();
				
				setupData();
			} else {
				window.simulationCheckBox.setSelected(simulation);
			}
		} else if (e.getSource() == window.dataLengthButton) {
			String warningMessage = "";
			if (!simulation) {
				warningMessage = "Are you sure you would like to change the data length?\n\n"
						+ "The current data will be deleted from the UI. You can find it in " + LOG_FILE_SAVE_LOCATION + currentLogFileName;
			} else {
				warningMessage = "Are you sure you would like to change the data length? The data will be reloaded.";
			}
			
			if (JOptionPane.showConfirmDialog(window, warningMessage) == 0) {
				// Set data length
				try {
					dataLength = Integer.parseInt(window.dataLengthTextBox.getText());
				} catch (NumberFormatException error) {
					JOptionPane.showMessageDialog(window, "'" + window.dataLengthTextBox.getText() + "' is not a number");
				}
				
				// Load labels
				loadLabels(LABELS_LOCATION);
				
				// Different setups depending on if simulation or not
				setupData();
				
				updateUI();
			} 
		}
	}
	
	public void addChart() {
		XYChart xyChart = new XYChartBuilder().title("Altitude vs Timestamp (s)").xAxisTitle("Timestamp (s)").yAxisTitle("Altitude (m)").build();

		// Customize Chart
		xyChart.getStyler().setLegendPosition(LegendPosition.InsideNE);
		xyChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);

		// Series
		xyChart.addSeries("series0", new double[] { 0 }, new double[] { 0 });
		
		XChartPanel<XYChart> chartPanel = new XChartPanel<>(xyChart);
		window.centerChartPanel.add(chartPanel);
		
		DataChart dataChart = new DataChart(window, xyChart, chartPanel);
		
		// Set default size
		dataChart.snapPanel.setRelSize(600, 450);
		
		// Add these default charts to the list
		window.charts.add(dataChart);
		
		// Set to be selected
		window.centerChartPanel.setComponentZOrder(chartPanel, 0);
		dataChart.snapPanel.setSnapPanelListener(this);
		
		selectedChart.chartPanel.setBorder(null);
		selectedChart = dataChart;
		
		snapPanelSelected(selectedChart.snapPanel);
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
			if (ignoreSelections) return;
			
			// Set chart to be based on this row
			selectedChart.xTypes = window.dataTable.getSelectedRows();
			updateUI();
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getSource() == window.dataTable && e.getButton() == MouseEvent.BUTTON3) {
			// Left clicking the dataTable
			int row = window.dataTable.rowAtPoint(e.getPoint());
			
			selectedChart.yType = row;
			
			((DataTableCellRenderer) window.dataTable.getDefaultRenderer(Object.class)).coloredRow = row;
			window.dataTable.repaint();
			
			updateUI();
		}
	}
	
	/**
	 * Called when a new snap window is highlighted
	 */
	@Override
	public void snapPanelSelected(SnapPanel snapPanel) {
		if (snapPanel.chart != null) {
			// Remove border on old object
			selectedChart.chartPanel.setBorder(null);

			selectedChart = snapPanel.chart;
			
			// Add border
			selectedChart.chartPanel.setBorder(selectionBorder);
			
			// Add selections
			ignoreSelections = true;
			
			window.dataTable.clearSelection();
			for (int i = 0; i < selectedChart.xTypes.length; i++) {
				window.dataTable.addRowSelectionInterval(selectedChart.xTypes[i], selectedChart.xTypes[i]);
			}
			
			// Update yType
			((DataTableCellRenderer) window.dataTable.getDefaultRenderer(Object.class)).coloredRow = selectedChart.yType;
			window.repaint();
			
			window.dataTable.setColumnSelectionInterval(0, 0);
			ignoreSelections = false;
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
		// Chart Container resize management
		int currentChartContainerWidth = window.centerChartPanel.getWidth();
		int currentChartContainerHeight = window.centerChartPanel.getHeight();
		
		for (DataChart chart : window.charts) {
			chart.snapPanel.containerResized(currentChartContainerWidth, currentChartContainerHeight);
		}
		
		chartContainerWidth = currentChartContainerWidth;
		chartContainerHeight = currentChartContainerHeight;
	}

	@Override
	public void componentShown(ComponentEvent e) {
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		
	}
}
