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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.json.JSONArray;
import org.json.JSONObject;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.XYStyler;
import org.knowm.xchart.style.Styler.LegendPosition;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

public class Main implements ComponentListener, ChangeListener, ActionListener, MouseListener, ListSelectionListener, SerialPortMessageListener, SnapPanelListener {
	
	/** Constants */
	/** The location of the comma separated labels without the extension. */
	public static final String CONFIG_LOCATION = "data/config.json";
	/** How many data points are there. By default, it is the number of labels */
	public static List<Integer> dataLength = new ArrayList<>();
	/** Separator for the data */
	public static final String SEPARATOR = ";";
	/** Data file location for the simulation (new line separated for each event). This does not include the extension/ */
	public static final String SIM_DATA_LOCATION = "data/data";
	public static final String SIM_DATA_EXTENSION = ".txt";
	
	/** Whether to update Google Earth file */
	public static boolean googleEarth = false;
	/** Where the updating Google Earth kml file is stored */
	public static final String GOOGLE_EARTH_DATA_LOCATION = "data/positions.kml";
	
	/** Where to save the log file */
	public static final String LOG_FILE_SAVE_LOCATION = "data/";
	public static final String DEFAULT_LOG_FILE_NAME = "log.txt";
	/** Will have a number appended to the end to not overwrite old logs */
	String currentLogFileName = DEFAULT_LOG_FILE_NAME;
	
	/** How many data sources to record data from. It is set when the config is loaded. */
	public static int dataSourceCount = 1;
	
	/** Is this running in simulation mode. Must be set at the beginning as it changes the setup. */
	public static boolean simulation = false;
	
	List<List<DataHandler>> allData = new ArrayList<>();
	
	List<String[]> labels = new ArrayList<>();
	JSONObject config = null; 
	
	/** Index of the current data point being looked at */
	ArrayList<Integer> currentDataIndex = new ArrayList<>();
	/** Index of the minimum data point being looked at */
	ArrayList<Integer> minDataIndex = new ArrayList<>();
	
	/** If {@link currentDataIndex} should be set to the latest message */
	boolean latest = true;
	/** If true, slider will temporarily stop growing */
	boolean paused = false;
	
	/** If not in a simulation, the serial port being listened to */
	List<SerialPort> activeSerialPort = new ArrayList<SerialPort>();
	
	Window window;
	
	/** All the serial ports found */
	SerialPort[] allSerialPorts;
	List<Boolean> connectingToSerial = new ArrayList<Boolean>();
	
	/** Used for the map view */
	GoogleEarthUpdater googleEarthUpdater;
	
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
			}
		}
		
		new Main();
	}
	
	public Main() {
		// Load labels
		loadConfig();
		
		// Create window
		window = new Window(this);
		
		window.addComponentListener(this);
		
		setupUI();
		
		// Different setups depending on if simulation or not
		setupData();
		
		// Setup Google Earth map support
		if (googleEarth) {
			setupGoogleEarth();
		}
		
		// Update UI once
		updateUI();
	}
	
	public void setupData() {
		allData = new ArrayList<>();
		for (int i = 0; i < dataSourceCount; i++) {
			allData.add(new ArrayList<>());

			// Add data indexes
			currentDataIndex.add(0);
			minDataIndex.add(0);
		}
		
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
			comSelectorData[i] = allSerialPorts[i].getDescriptivePortName();
		}

		for (JList<String> comSelector: window.comSelectors) {
			comSelector.setListData(comSelectorData);
		}
		
		// Create required lists
		for (int i = 0; i < dataSourceCount; i++) {
			activeSerialPort.add(null);
		}
		for (int i = 0; i < dataSourceCount; i++) {
			connectingToSerial.add(false);
		}
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
	
	public void initialisePort(int tableIndex, SerialPort serialPort) {
		if (serialPort.isOpen() || connectingToSerial.get(tableIndex)) return;
		
		if (activeSerialPort.get(tableIndex) != null && activeSerialPort.get(tableIndex).isOpen()) {
			// Switching ports, close the old one
			activeSerialPort.get(tableIndex).closePort();
		}
		
		activeSerialPort.set(tableIndex, serialPort);
		
		connectingToSerial.set(tableIndex, true);
		
		boolean open = activeSerialPort.get(tableIndex).openPort();
		
		activeSerialPort.get(tableIndex).setBaudRate(57600);
		
		if (open) {
			window.comConnectionSuccessLabels.get(tableIndex).setText("Connected");
			window.comConnectionSuccessLabels.get(tableIndex).setBackground(Color.green);
		} else {
			window.comConnectionSuccessLabels.get(tableIndex).setText("FAILED");
			window.comConnectionSuccessLabels.get(tableIndex).setBackground(Color.red);
		}
		
		// Setup listener
		activeSerialPort.get(tableIndex).addDataListener(this);
		
		connectingToSerial.set(tableIndex, false);
	}
	
	public void setupUI() {
		addChart();
		
		// Add slider listeners
		window.maxSlider.addChangeListener(this);
		window.minSlider.addChangeListener(this);
		
		// Buttons
		window.pauseButton.addActionListener(this);
		window.latestButton.addActionListener(this);
		
		window.addChartButton.addActionListener(this);
		
		// Checkboxes
		window.googleEarthCheckBox.addActionListener(this);
		window.simulationCheckBox.addActionListener(this);
		
		// Set simulation checkbox to be default
		window.simulationCheckBox.setSelected(simulation);
		
		// Com selector
		for (JList<String> comSelector: window.comSelectors) {
			comSelector.addListSelectionListener(this);
		}
		
		// Setup listeners for table
		for (JTable dataTable : window.dataTables) {
			dataTable.getSelectionModel().addListSelectionListener(this);
			dataTable.addMouseListener(this);
		}
		
		
		// Setup Snap Panel system
		selectedChart = window.charts.get(0);
		selectedChart.snapPanel.setSnapPanelListener(this);

		snapPanelSelected(selectedChart.snapPanel);
	}
	
	public void setupGoogleEarth() {
		googleEarthUpdater = new GoogleEarthUpdater();
		
		// Setup updater file
//		googleEarthUpdater.createKMLUpdaterFile();
	}
	
	public void updateUI() {
		// If not ready yet
		if (allData.size() == 0) return;
		
		// Update every table's data
		for (int i = 0; i < allData.size(); i++) {
			// If not ready yet
			if (allData.get(i).size() == 0) continue;
			
			// Don't change slider if paused
			// Only do this once
			if (!paused && i == 0) {
				// Set max value of the sliders
				window.maxSlider.setMaximum(allData.get(i).size() - 1);
				window.minSlider.setMaximum(allData.get(i).size() - 1);
			}
			
			DataHandler currentDataHandler = allData.get(i).get(currentDataIndex.get(i));
			
			if (currentDataHandler != null) {
				currentDataHandler.updateTableUIWithData(window.dataTables.get(i), labels.get(i));
			} else {
				setTableToError(i, window.dataTables.get(i));
			}
		}
		
		// Only record google earth data for the first one for now 
		// There is no way to change the filename yet
		if (googleEarth) {
			googleEarthUpdater.updateKMLFile(allData, minDataIndex, currentDataIndex, config.getJSONArray("datasets"), false);
		}
		
		// Update every chart
		for (DataChart chart : window.charts) {
			updateChart(chart);
		}
	}
	
	public void setTableToError(int index, JTable table) {
		TableModel tableModel = table.getModel();
		
		// Set first item to "Error"
		tableModel.setValueAt("Parsing Error", 0, 0);
		tableModel.setValueAt(currentDataIndex, 0, 1);
		
		for (int i = 1; i < dataLength.get(index); i++) {
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
		
		// Add y axis
		for (int i = minDataIndex.get(chart.yType.tableIndex); i <= currentDataIndex.get(chart.yType.tableIndex); i++) {
			if (allData.get(chart.yType.tableIndex).size() == 0) continue;
			
			DataHandler data = allData.get(chart.yType.tableIndex).get(i);
			
			if (data != null) {
				altitudeDataX.add(data.data[chart.yType.index].getDecimalValue() / 1000);
			}
		}
		
		// Add x axis
		for (int i = 0; i < chart.xTypes.length; i++) {
			for (int j = minDataIndex.get(chart.xTypes[i].tableIndex); j <= currentDataIndex.get(chart.xTypes[i].tableIndex); j++) {
				if (allData.get(chart.yType.tableIndex).size() == 0) continue;

				DataHandler data = allData.get(chart.xTypes[i].tableIndex).get(j);
				
				if (data != null) {
					altitudeDataY.get(i).add(data.data[chart.xTypes[i].index].getDecimalValue());
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
			String xTypeTitle = labels.get(chart.xTypes[i].tableIndex)[chart.xTypes[i].index];
			
			if (title.length() != 0) title.append(", ");
			title.append(xTypeTitle);
			
			chart.xyChart.setYAxisTitle(xTypeTitle);
			
			XYSeries series = null;
			
			if (chart.activeSeries.length > i) {
				series = chart.xyChart.updateXYSeries("series" + i, altitudeDataX, altitudeDataY.get(i), null);
			} else {
				series = chart.xyChart.addSeries("series" + i, altitudeDataX, altitudeDataY.get(i), null);
			}
			
			series.setLabel(xTypeTitle);
			
			newActiveSeries[i] = "series" + i;
		}
		
		String yTypeTitle =  labels.get(chart.yType.tableIndex)[chart.yType.index];
		
		chart.xyChart.setTitle(title + " vs " + yTypeTitle);
		
		if (chart.xTypes.length > 1) {
			chart.xyChart.setYAxisTitle("Value");
		}
		
		chart.xyChart.setXAxisTitle(yTypeTitle);
		
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
		for (int i = 0; i < dataSourceCount; i++) {
			loadSimulationData(i, SIM_DATA_LOCATION + i + SIM_DATA_EXTENSION);
		}
	}
	
	public void loadSimulationData(int index, String fileName) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		ArrayList<DataHandler> dataHandlers = new ArrayList<DataHandler>();
		
		try {
			try {
			    String line = null;

			    while ((line = br.readLine()) != null) {
			        // Parse this line and add it as a data point
			    	dataHandlers.add(parseData(line, index));
			    }
			} finally {
			    br.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		allData.set(index, dataHandlers);
	}
	
	public DataHandler parseData(String data, int tableIndex) {
		DataHandler dataHandler = new DataHandler(tableIndex);
		
		// Clear out the b' ' stuff added that is only meant for the radio to see
		data = data.replaceAll("b'|\\\\r\\\\n'", "");
		
		// Semi-colon separated
		String[] splitData = data.split(SEPARATOR);
		if (splitData.length != dataHandler.data.length) {
			//invalid data
			System.err.println("Line with invalid data (Not the correct amount of data)");
			
			return null;
		}
		
		JSONArray dataSets = config.getJSONArray("datasets");
		for (int i = 0; i < splitData.length; i++) {
			dataHandler.set(i, splitData[i], dataSets.getJSONObject(tableIndex).getJSONObject("coordinateIndexes"));
		}
		
		return dataHandler;
	}
	
	/** 
	 * Run once at the beginning of simulation mode
	 */
	public void loadConfig() {
		loadConfig(CONFIG_LOCATION);
	}
	
	public void loadConfig(String fileName) {
		String configString = null;
		try {
			configString = new String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			
			return;
		}
		
		config = new JSONObject(configString);
		
		JSONArray datasetsJSONArray = config.getJSONArray("datasets");
		dataSourceCount = datasetsJSONArray.length();
		
		// Add all data
		for (int i = 0; i < datasetsJSONArray.length(); i++) {
			JSONObject currentDataset = datasetsJSONArray.getJSONObject(i);
			
			JSONArray labelsJsonArray = currentDataset.getJSONArray("labels");
			
			// Load labels
			String[] labelsArray = new String[labelsJsonArray.length()];
			
			for (int j = 0; j < labelsArray.length; j++) {
				labelsArray[j] = labelsJsonArray.getString(j);
			}
			
			labels.add(labelsArray);
			
			dataLength.add(labelsArray.length);
		}
	}
	
	/**
	 * Triggered every time the slider changes
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == window.maxSlider) {
			// For now, just use a fraction of the slider value
			for (int i = 0; i < currentDataIndex.size(); i++) {
				int value = window.maxSlider.getValue();
				if (i != 0) value = (int) ((double) value / allData.get(0).size() * allData.get(i).size());
				
				currentDataIndex.set(i, value);
				
				// Check if min is too high
				if (minDataIndex.get(i) > currentDataIndex.get(i)) {
					minDataIndex.set(i, currentDataIndex.get(i));
					window.minSlider.setValue(minDataIndex.get(i));
				}
			}
			
			updateUI();
			
			// Update the latest value
			latest = currentDataIndex.get(0) == window.maxSlider.getMaximum() - 1;
		} else if (e.getSource() == window.minSlider) {
			// For now, just use a fraction of the slider value
			for (int i = 0; i < currentDataIndex.size(); i++) {
				int value = window.minSlider.getValue();
				if (i != 0) value = (int) ((double) value / allData.get(0).size() * allData.get(i).size());
				
				minDataIndex.set(i, value);
				
				// Check if min is too high
				if (minDataIndex.get(i) > currentDataIndex.get(i)) {
					minDataIndex.set(i, currentDataIndex.get(i));
					window.minSlider.setValue(minDataIndex.get(i));
				}
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
		int tableIndex = activeSerialPort.indexOf(e.getSerialPort());
		
		String delimitedMessage = new String(e.getReceivedData(), StandardCharsets.UTF_8);
		
		allData.get(tableIndex).add(parseData(delimitedMessage, tableIndex));
		
		updateUI();
		
		// Move position to end
		if (latest) {
			window.maxSlider.setValue(allData.size() - 1);
		}
		
		// Add this message to the log file
		logFileStringBuilder.append(delimitedMessage);
		
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
			
			if (googleEarth) setupGoogleEarth();
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
		}
	}
	
	public void addChart() {
		XYChart xyChart = new XYChartBuilder().title("Altitude vs Timestamp (s)").xAxisTitle("Timestamp (s)").yAxisTitle("Altitude (m)").build();

		// Customize Chart
		XYStyler firstChartStyler = xyChart.getStyler();
		
		firstChartStyler.setLegendPosition(LegendPosition.InsideNE);
		firstChartStyler.setLegendVisible(true);
		firstChartStyler.setToolTipsEnabled(true);
		firstChartStyler.setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);

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
		
		if (selectedChart != null) selectedChart.chartPanel.setBorder(null);
		selectedChart = dataChart;
		
		snapPanelSelected(selectedChart.snapPanel);
		
		updateUI();
	}

	/** For com selector JList */
	@Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getSource() instanceof JList && window.comSelectors.contains(e.getSource())) {
			@SuppressWarnings("unchecked")
			JList<String> comSelector = (JList<String>) e.getSource();
			
			int tableIndex = window.comSelectors.indexOf(comSelector);
			
			// Set to loading
			window.comConnectionSuccessLabels.get(tableIndex).setText("Loading...");
			window.comConnectionSuccessLabels.get(tableIndex).setBackground(Color.yellow);
			
			// Find what port it was
			if (allSerialPorts != null) {
				for (int i = 0; i < allSerialPorts.length; i++) {
					// Check if this is the selected com selector
					if (allSerialPorts[i].getDescriptivePortName().equals(comSelector.getSelectedValue())) {
						final SerialPort newSerialPort = allSerialPorts[i];
						
						// Do it in an other thread
						Thread thread = new Thread() {
							public void run() {
								initialisePort(tableIndex, newSerialPort);
							}
						};
						thread.start();
						
						break;
					}
				}
			}
		} else if(e.getSource() instanceof ListSelectionModel && !ignoreSelections) {
			for (int i = 0; i < window.dataTables.size(); i++) {
				JTable dataTable = window.dataTables.get(i);
				
				if (e.getSource() == dataTable.getSelectionModel()) {
					int[] selections = dataTable.getSelectedRows();
					DataType[] formattedSelections = new DataType[selections.length];
					
					moveSelectionsToNewTable(i, true);
					
					for (int j = 0; j < formattedSelections.length; j++) {
						formattedSelections[j] = new DataType(selections[j], window.dataTables.indexOf(dataTable));
					}
					
					// Set chart to be based on this row
					selectedChart.xTypes = formattedSelections;
					
					updateUI();
					
					break;
				}
			}
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		for (int i = 0; i < window.dataTables.size(); i++) {
			JTable dataTable = window.dataTables.get(i);
			
			if (e.getSource() == dataTable && e.getButton() == MouseEvent.BUTTON3) {
				// Left clicking the dataTable
				int row = dataTable.rowAtPoint(e.getPoint());

				ignoreSelections = true;
				
				moveSelectionsToNewTable(i, false);
				
				selectedChart.yType = new DataType(row, i);
				
				((DataTableCellRenderer) dataTable.getDefaultRenderer(Object.class)).coloredRow = row;
				dataTable.repaint();
				
				updateUI();
				
				ignoreSelections = false;
			}
		}
	}
	
	public void moveSelectionsToNewTable(int newTableIndex, boolean changingX) {
		boolean movingXType = false;
		
		// Clear previous selections
		for (int j = 0; j < selectedChart.xTypes.length; j++) {
			if (selectedChart.xTypes[j].tableIndex != newTableIndex) {
				int currentTableIndex = selectedChart.xTypes[j].tableIndex;
				
				// Clear that table's selection
				window.dataTables.get(currentTableIndex).clearSelection();
				window.dataTables.get(currentTableIndex).repaint();
				
				movingXType = true;
			}
		}
		
		if (movingXType && !changingX) {
			selectedChart.xTypes = new DataType[1];
			selectedChart.xTypes[0] = new DataType(1, newTableIndex);
			
			window.dataTables.get(newTableIndex).setRowSelectionInterval(1, 1);
			window.dataTables.get(newTableIndex).setColumnSelectionInterval(0, 0);
			window.dataTables.get(newTableIndex).repaint();
		}
		
		// Move yType selection if needed
		if (selectedChart.yType.tableIndex != newTableIndex) {
			// Deselect the old one
			JTable oldDataTable = window.dataTables.get(selectedChart.yType.tableIndex);
			((DataTableCellRenderer) oldDataTable.getDefaultRenderer(Object.class)).coloredRow = -1;
			oldDataTable.repaint();
			
			// Select this default selection
			JTable dataTable = window.dataTables.get(newTableIndex);
			((DataTableCellRenderer) dataTable.getDefaultRenderer(Object.class)).coloredRow = 0;
			dataTable.repaint();
			
			selectedChart.yType = new DataType(0, newTableIndex);
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
			
			for (int i = 0; i < window.dataTables.size(); i++) {
				JTable dataTable = window.dataTables.get(i);
				
				dataTable.clearSelection();
				for (int j = 0; j < selectedChart.xTypes.length; j++) {
					if (selectedChart.xTypes[j].tableIndex == i) {
						dataTable.addRowSelectionInterval(selectedChart.xTypes[j].index, selectedChart.xTypes[j].index);
					}
				}
				
				// Update yType
				if (selectedChart.yType.tableIndex == i) {
					((DataTableCellRenderer) dataTable.getDefaultRenderer(Object.class)).coloredRow = selectedChart.yType.index;
				}
				
				window.repaint();
				
				dataTable.setColumnSelectionInterval(0, 0);
			}
			
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
