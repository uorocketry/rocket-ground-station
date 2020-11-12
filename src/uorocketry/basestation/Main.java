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
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.Styler.YAxisPosition;
import org.knowm.xchart.style.XYStyler;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;

public class Main implements ComponentListener, ChangeListener, ActionListener, MouseListener, ListSelectionListener, SerialPortMessageListener, SnapPanelListener {
	
	/** Constants */
	/** The location of the comma separated labels without the extension. */
	public static final String CONFIG_LOCATION = "data/config.json";
	/** How many data points are there. By default, it is the number of labels */
	public static List<Integer> dataLength = new ArrayList<>(2);
	/** Separator for the data */
	public static final String SEPARATOR = ";";
	/** Data file location for the simulation (new line separated for each event). This does not include the extension/ */
	public static final String SIM_DATA_LOCATION = "data/data";
	public static final String SIM_DATA_EXTENSION = ".txt";
	
	public static final Color LEGEND_BACKGROUND_COLOR = new Color(255, 255, 255, 100);
	
	/** Whether to update Google Earth file */
	public static boolean googleEarth = false;
	/** Where the updating Google Earth kml file is stored */
	public static final String GOOGLE_EARTH_DATA_LOCATION = "data/positions.kml";
	
	/** Where to save the log file */
	public static final String LOG_FILE_SAVE_LOCATION = "data/";
    public static final String DEFAULT_LOG_FILE_NAME = "log";
    public static final String LOG_FILE_EXTENSION = ".txt";
	/** Will have a number appended to the end to not overwrite old logs */
	ArrayList<String> currentLogFileName = new ArrayList<String>(2);
	
	/** Used to limit displayed data points to speed up rendering */
	public static int maxDataPointsDisplayed = 800;
	
	/** How many data sources to record data from. It is set when the config is loaded. */
	public static int dataSourceCount = 1;
	
	/** Is this running in simulation mode. Must be set at the beginning as it changes the setup. */
	public static boolean simulation = false;
	
	List<List<DataHandler>> allData = new ArrayList<>(2);
	
	List<String[]> labels = new ArrayList<>();
	JSONObject config = null; 
	
	/** Index of the current data point being looked at */
	ArrayList<Integer> currentDataIndexes = new ArrayList<>(2);
	/** Index of the minimum data point being looked at */
	ArrayList<Integer> minDataIndexes = new ArrayList<>(2);
	
	/** If {@link currentDataIndex} should be set to the latest message */
	boolean latest = true;
	/** If true, slider will temporarily stop growing */
	boolean paused = false;
	
	/** Used to only update the UI once at a time, even though it runs in its own thread */
	boolean updatingUI = false;
	
	/** If not in a simulation, the serial ports being listened to */
	List<SerialPort> activeSerialPorts = new ArrayList<SerialPort>(2);
	
	Window window;
	
	/** All the serial ports found */
	SerialPort[] allSerialPorts;
	List<Boolean> connectingToSerial = new ArrayList<Boolean>(2);
	
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
	
	/** If true, it will show the latest data instead of showing a subset of all data */
	boolean onlyShowLatestData = false;
	
	/** If true, clicking on data in a chart will hide it */
	boolean dataDeletionMode = false;
	
	/** What will be written to the log file */
	StringBuilder logFileStringBuilder = new StringBuilder();
	/** Is the log file being currently updated */
	ArrayList<Boolean> currentlyWriting = new ArrayList<Boolean>(2);
	
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
		allData = new ArrayList<>(dataSourceCount);
		currentDataIndexes = new ArrayList<>(dataSourceCount);
		minDataIndexes = new ArrayList<>(dataSourceCount);

		for (int i = 0; i < dataSourceCount; i++) {
			allData.add(new ArrayList<>());

			// Add data indexes
			currentDataIndexes.add(0);
			minDataIndexes.add(0);
			
			// Reset sliders
			window.maxSliders.get(i).setValue(0);
			window.minSliders.get(i).setValue(0);
		}
		
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
			activeSerialPorts.add(null);
			connectingToSerial.add(false);
			currentlyWriting.add(false);
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
		
		int logIndex = 0;
		
		JSONArray dataSets = config.getJSONArray("datasets");
		
		// Find a suitable filename
		for (int i = 0; i <= listOfLogFiles.length; i++) {
			boolean containsFile = false;
			for (int j = 0 ; j < dataSourceCount; j++) {
				if (usedFileNames.contains(DEFAULT_LOG_FILE_NAME + "_" + dataSets.getJSONObject(j).getString("name").toLowerCase() + "_" + logIndex + LOG_FILE_EXTENSION)) {
					containsFile = true;
					break;
				}
			}
			
			if (containsFile) {
				logIndex++;
			} else {
				break;
			}
		}
		
		// Set the names
		for (int i = 0 ; i < dataSourceCount; i++) {
			currentLogFileName.add(DEFAULT_LOG_FILE_NAME + "_" + dataSets.getJSONObject(i).getString("name").toLowerCase() + "_" + logIndex + LOG_FILE_EXTENSION);
		}
		
		window.savingToLabel.setText("Saving to " + formattedSavingToLocations());
	}
	
	public String formattedSavingToLocations() {
		StringBuilder savingToText = new StringBuilder();
		
		// Add text for each file
		for (int i = 0 ; i < dataSourceCount; i++) {
			if (i != 0) savingToText.append(", ");
			
			savingToText.append(LOG_FILE_SAVE_LOCATION + currentLogFileName.get(i));
		}
		
		return savingToText.toString();
	}
	
	public void initialisePort(int tableIndex, SerialPort serialPort) {
		if (serialPort.isOpen() || connectingToSerial.get(tableIndex)) return;
		
		if (activeSerialPorts.get(tableIndex) != null && activeSerialPorts.get(tableIndex).isOpen()) {
			// Switching ports, close the old one
			activeSerialPorts.get(tableIndex).closePort();
		}
		
		activeSerialPorts.set(tableIndex, serialPort);
		
		connectingToSerial.set(tableIndex, true);
		
		boolean open = serialPort.openPort();
		
		serialPort.setBaudRate(57600);
		
		if (open) {
			window.comConnectionSuccessLabels.get(tableIndex).setText("Connected");
			window.comConnectionSuccessLabels.get(tableIndex).setBackground(Color.green);
		} else {
			window.comConnectionSuccessLabels.get(tableIndex).setText("FAILED");
			window.comConnectionSuccessLabels.get(tableIndex).setBackground(Color.red);
		}
		
		// Setup listener
		serialPort.addDataListener(this);
		
		connectingToSerial.set(tableIndex, false);
	}
	
	public void setupUI() {
		addChart();
		
		// Add slider listeners
		for (int i = 0; i < dataSourceCount; i++) {
			window.maxSliders.get(i).addChangeListener(this);
			window.minSliders.get(i).addChangeListener(this);
		}
		
		// Buttons
		window.clearDataButton.addActionListener(this);
		window.hideComSelectorButton.addActionListener(this);
		window.hideBarsButton.addActionListener(this);
		window.pauseButton.addActionListener(this);
		window.latestButton.addActionListener(this);
		
		window.addChartButton.addActionListener(this);
		
		window.setMaxDataPointsButton.addActionListener(this);
		
		window.restoreDeletedData.addActionListener(this);
		
		window.saveLayout.addActionListener(this);
		window.loadLayout.addActionListener(this);
		
		// Checkboxes
		window.googleEarthCheckBox.addActionListener(this);
		window.simulationCheckBox.addActionListener(this);
		window.onlyShowLatestDataCheckBox.addActionListener(this);
		window.dataDeletionModeCheckBox.addActionListener(this);
		
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
		synchronized (window.charts) {
			selectedChart = window.charts.get(0);
			selectedChart.snapPanel.setSnapPanelListener(this);
			
			snapPanelSelected(selectedChart.snapPanel);
		}
	}
	
	public void setupGoogleEarth() {
		googleEarthUpdater = new GoogleEarthUpdater();
		
		// Setup updater file
//		googleEarthUpdater.createKMLUpdaterFile();
	}
	
	public void updateUI() {
		// If not ready yet
		if (allData.size() == 0 || updatingUI) return;
		
		updatingUI = true;
		
		// Update UI on another thread
		new Thread(this::updateUIInternal).start();
	}
	
	private void updateUIInternal() {
		try {
			// Update every table's data
			for (int i = 0; i < allData.size(); i++) {
				// If not ready yet
				if (allData.get(i).size() == 0) continue;
				
				// Don't change slider if paused
				if (!paused) {
					// Set max value of the sliders
					window.maxSliders.get(i).setMaximum(allData.get(i).size() - 1);
					window.minSliders.get(i).setMaximum(allData.get(i).size() - 1);
					
					// Move position to end
					if (latest) {
						window.maxSliders.get(i).setValue(allData.get(i).size() - 1);
					}
				}
				
				DataHandler currentDataHandler = allData.get(i).get(currentDataIndexes.get(i));
				
				if (currentDataHandler != null) {
					currentDataHandler.updateTableUIWithData(window.dataTables.get(i), labels.get(i));
				} else {
					setTableToError(i, window.dataTables.get(i));
				}
			}
			
			if (googleEarth) {
				googleEarthUpdater.updateKMLFile(allData, minDataIndexes, currentDataIndexes, config.getJSONArray("datasets"), false);
			}
			
			// Update every chart
			synchronized (window.charts) {
				for (DataChart chart : window.charts) {
					updateChart(chart);
				}
			}
		} catch (Exception e) {
			// Don't let an exception while updating break the program
			e.printStackTrace();
		}
		
		updatingUI = false;
	}
	
	public void setTableToError(int index, JTable table) {
		TableModel tableModel = table.getModel();
		
		// Set first item to "Error"
		tableModel.setValueAt("Parsing Error", 0, 0);
		tableModel.setValueAt(currentDataIndexes, 0, 1);
		
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
		for (int i = minDataIndexes.get(chart.yType.tableIndex); i <= currentDataIndexes.get(chart.yType.tableIndex); i++) {
			if (allData.get(chart.yType.tableIndex).size() == 0) continue;
			
			DataHandler data = allData.get(chart.yType.tableIndex).get(i);
			
			if (data != null) {
				altitudeDataX.add(data.data[chart.yType.index].getDecimalValue());
			}
		}
		
		// Add x axis
		for (int i = 0; i < chart.xTypes.length; i++) {
			// Used to limit the max number of data points displayed
			float targetRatio = (float) maxDataPointsDisplayed / (currentDataIndexes.get(chart.xTypes[i].tableIndex) - minDataIndexes.get(chart.xTypes[i].tableIndex));
			int dataPointsAdded = 0;
			
			int maxDataIndex = currentDataIndexes.get(chart.xTypes[i].tableIndex);
			int minDataIndex = onlyShowLatestData ? Math.max(maxDataIndex - maxDataPointsDisplayed, 0)
					: minDataIndexes.get(chart.xTypes[i].tableIndex);

			for (int j = minDataIndex; j <= maxDataIndex; j++) {
				if (allData.get(chart.yType.tableIndex).size() == 0) continue;

				DataHandler data = allData.get(chart.xTypes[i].tableIndex).get(j);
				
				if (data != null) {
					// Ensures that not too many data points are displayed
					// Always show data if only showing latest data (that is handled by changing the minSlider)
					boolean shouldShowDataPoint = onlyShowLatestData || ((float) dataPointsAdded / j <= targetRatio);
					
					if (!data.hiddenDataTypes.contains(data.types[chart.xTypes[i].index]) && shouldShowDataPoint ) {
						altitudeDataY.get(i).add(data.data[chart.xTypes[i].index].getDecimalValue());
						dataPointsAdded++;
					} else {
						// Hidden data
						altitudeDataY.get(i).add(null);
					}
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
			
			chart.xyChart.setYAxisGroupTitle(i, xTypeTitle);
			
			XYSeries series = null;
			
			if (chart.activeSeries.length > i) {
				series = chart.xyChart.updateXYSeries("series" + i, altitudeDataX, altitudeDataY.get(i), null);
			} else {
				series = chart.xyChart.addSeries("series" + i, altitudeDataX, altitudeDataY.get(i), null);
			}
			
			series.setLabel(xTypeTitle);
			series.setYAxisGroup(i);
			
			newActiveSeries[i] = "series" + i;
		}
		
		String yTypeTitle =  labels.get(chart.yType.tableIndex)[chart.yType.index];
		
		chart.xyChart.setTitle(title + " vs " + yTypeTitle);
		
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
		
		//TODO: Remove this hardcoded code to ignore small timestamp data
		try {
			List<DataHandler> currentTableDatas = allData.get(tableIndex);
			DataHandler lastDataPointDataHandler = null;
			// Find last non null data point
			for (int i = currentTableDatas.size() - 1; i >= 0; i--) {
				if (currentTableDatas.get(i) != null) {
					lastDataPointDataHandler = currentTableDatas.get(i);
					break;
				}
			}
			if (lastDataPointDataHandler != null && Integer.parseInt(splitData[1]) < lastDataPointDataHandler.data[1].getDecimalValue()) {
				// Treat as invalid data
				return null;
			}
		} catch (NumberFormatException e) {}
		
		JSONArray dataSets = config.getJSONArray("datasets");
		for (int i = 0; i < splitData.length; i++) {
			if (!dataHandler.set(i, splitData[i], dataSets.getJSONObject(tableIndex).getJSONObject("coordinateIndexes"))) {
				System.err.println("Failed to set data handler");

				// Parsing failed
				return null;
			}
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
			
			JOptionPane.showMessageDialog(window, "The config file was not found in " + fileName + 
					"\r\n\r\nIf you plan downloaded a release build, you might want to download the version with labels and sample data included.");
			
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
		if (e.getSource() instanceof JSlider && window.maxSliders.contains(e.getSource())) {
			JSlider maxSlider = (JSlider) e.getSource();
			int tableIndex = window.maxSliders.indexOf(maxSlider);
			
			currentDataIndexes.set(tableIndex, maxSlider.getValue());
			
			// Check if min is too high
			if (minDataIndexes.get(tableIndex) > currentDataIndexes.get(tableIndex)) {
				minDataIndexes.set(tableIndex, currentDataIndexes.get(tableIndex));
				window.minSliders.get(tableIndex).setValue(minDataIndexes.get(tableIndex));
			}
			
			
			updateUI();
		} else if (e.getSource() instanceof JSlider && window.minSliders.contains(e.getSource())) {
			JSlider minSlider = (JSlider) e.getSource();
			int tableIndex = window.minSliders.indexOf(minSlider);

			minDataIndexes.set(tableIndex, minSlider.getValue());
			
			// Check if min is too high
			if (minDataIndexes.get(tableIndex) > currentDataIndexes.get(tableIndex)) {
				minDataIndexes.set(tableIndex, currentDataIndexes.get(tableIndex));
				minSlider.setValue(minDataIndexes.get(tableIndex));
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
		int tableIndex = activeSerialPorts.indexOf(e.getSerialPort());
		
		String delimitedMessage = new String(e.getReceivedData(), StandardCharsets.UTF_8);
		
		allData.get(tableIndex).add(parseData(delimitedMessage, tableIndex));
		
		updateUI();
		
		// Add this message to the log file
		logFileStringBuilder.append(delimitedMessage);
		
		// Get string
		String logFileString = logFileStringBuilder.toString();
		
		if (!currentlyWriting.get(tableIndex)) {
			currentlyWriting.set(tableIndex, true);

			// Write to file
			try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(LOG_FILE_SAVE_LOCATION + currentLogFileName.get(tableIndex)), StandardCharsets.UTF_8))) {
			   writer.write(logFileString);
			} catch (IOException err) {
				err.printStackTrace();
			}
			
			currentlyWriting.set(tableIndex, false);
		}
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == window.clearDataButton) {
			if (JOptionPane.showConfirmDialog(window, 
					"Are you sure you would like to clear all the data?") == 0) {
				for (int i = 0; i < allData.size(); i++) {
					allData.get(i).clear();
				}
				
				updateUI();
			}
		} else if (e.getSource() == window.hideComSelectorButton) {
			window.sidePanel.setVisible(!window.sidePanel.isVisible());
			
			if (window.sidePanel.isVisible()) {
				window.hideComSelectorButton.setText("Hide Com Selector");
			} else {
				window.hideComSelectorButton.setText("Show Com Selector");
			}
		} else if (e.getSource() == window.hideBarsButton) {
			window.sliderTabs.setVisible(!window.sliderTabs.isVisible());
			
			if (window.sliderTabs.isVisible()) {
				window.hideBarsButton.setText("Hide Sliders");
			} else {
				window.hideBarsButton.setText("Show Sliders");
			}
		} else if (e.getSource() == window.pauseButton) {
			paused = !paused;
			
			if (paused) {
				window.pauseButton.setText("Resume");
			} else {
				window.pauseButton.setText("Pause");
			}
			
		} else if (e.getSource() == window.latestButton) {
			latest = !latest;
			
			if (latest) {
				window.latestButton.setText("Detach From Latest");
				
				for (int i = 0; i < window.maxSliders.size(); i++) {
					window.maxSliders.get(i).setValue(allData.get(0).size() - 1);	
				}
			} else {
				window.latestButton.setText("Latest");
			}
		} else if (e.getSource() == window.addChartButton) {
			addChart();
		} else if (e.getSource() == window.googleEarthCheckBox) {
			googleEarth = window.googleEarthCheckBox.isSelected();
			
			if (googleEarth) setupGoogleEarth();
		} else if (e.getSource() == window.simulationCheckBox && window.simulationCheckBox.isSelected() != simulation) {
			String warningMessage = "";
			if (window.simulationCheckBox.isSelected()) {
				warningMessage = "Are you sure you would like to enable simulation mode?\n\n"
						+ "The current data will be deleted from the UI. You can find it in " + formattedSavingToLocations();
			} else {
				warningMessage = "Are you sure you would like to disable simulation mode?";
			}
			
			if (JOptionPane.showConfirmDialog(window, warningMessage) == 0) {
				simulation = window.simulationCheckBox.isSelected();
				
				setupData();
			} else {
				window.simulationCheckBox.setSelected(simulation);
			}
		} else if (e.getSource() == window.onlyShowLatestDataCheckBox) {
			onlyShowLatestData = window.onlyShowLatestDataCheckBox.isSelected();
		} else if (e.getSource() == window.setMaxDataPointsButton) {
			try {
				int maxDataPoints = Integer.parseInt(window.maxDataPointsTextField.getText());
				maxDataPointsDisplayed = maxDataPoints;
			} catch (NumberFormatException err) {}
		} else if (e.getSource() == window.dataDeletionModeCheckBox) {
			dataDeletionMode = window.dataDeletionModeCheckBox.isSelected();
		} else if (e.getSource() == window.restoreDeletedData) {
			for (int tableIndex = 0; tableIndex < allData.size(); tableIndex++) {
				List<DataHandler> dataHandlers = allData.get(tableIndex);
				
				for (DataHandler dataHandler: dataHandlers) {
					// See if the hidden list needs to be cleared
					if (dataHandler != null && !dataHandler.hiddenDataTypes.isEmpty()) {
						dataHandler.hiddenDataTypes.clear();
					}
				}
			}
			
			updateUI();
		} else if (e.getSource() == window.saveLayout) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.addChoosableFileFilter(new LayoutFileFilter());
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			// Start the in current working directory
			fileChooser.setCurrentDirectory(new File("."));

			int result = fileChooser.showSaveDialog(window);
			
			if (result == JFileChooser.APPROVE_OPTION) {
				File saveFile = fileChooser.getSelectedFile();
				
				// Add extension
				if (!saveFile.getName().endsWith(".rlay")) {
					saveFile = new File(saveFile.getPath() + ".rlay");
				}
				
				// Prep file
				JSONObject saveObject = new JSONObject();
				
				JSONArray chartsArray = new JSONArray();
				saveObject.put("charts", chartsArray);
				
				for (DataChart chart: window.charts) {
					JSONObject chartData = new JSONObject();
					
					chartData.put("x", chart.snapPanel.relX);
					chartData.put("y", chart.snapPanel.relY);
					chartData.put("width", chart.snapPanel.relWidth);
					chartData.put("height", chart.snapPanel.relHeight);
					
					// Add xTypes
					JSONArray xTypeArray = new JSONArray();
					for (DataType dataType: chart.xTypes) {
						JSONObject xTypeData = new JSONObject();
						
						xTypeData.put("index", dataType.index);
						xTypeData.put("tableIndex", dataType.tableIndex);
						
						xTypeArray.put(xTypeData);
					}
					chartData.put("xTypes", xTypeArray);
					
					// Add yType
					JSONObject yTypeData = new JSONObject();
					yTypeData.put("index", chart.yType.index);
					yTypeData.put("tableIndex", chart.yType.tableIndex);
					chartData.put("yType", yTypeData);
					
					chartsArray.put(chartData);
				}
				
				// Save file
				try (PrintWriter out = new PrintWriter(saveFile)) {
				    out.println(saveObject.toString());
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
			}
			
		} else if (e.getSource() == window.loadLayout) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setAcceptAllFileFilterUsed(false);
			fileChooser.addChoosableFileFilter(new LayoutFileFilter());
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			
			// Start the in current working directory
			fileChooser.setCurrentDirectory(new File("."));

			int result = fileChooser.showOpenDialog(window);
			
			if (result == JFileChooser.APPROVE_OPTION) {
				File saveFile = fileChooser.getSelectedFile();
				
				// Load file
				JSONObject loadedLayout = null;
				try {
					loadedLayout = new JSONObject(new String(Files.readAllBytes(saveFile.toPath()), StandardCharsets.UTF_8));
				} catch (JSONException e1) {
					e1.printStackTrace();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				
				JSONArray chartsArray = loadedLayout.getJSONArray("charts");
				
				// Clear current charts
				for (DataChart dataChart: window.charts) {
					// Remove from the UI
					window.centerChartPanel.remove(dataChart.chartPanel);
				}
				
				// Finally, remove it from the list
				window.charts.clear();
				
				for (int i = 0; i < chartsArray.length(); i++) {
					JSONObject chartData = chartsArray.getJSONObject(i);
					
					addChart(true);
					
					DataChart chart = window.charts.get(i);
					
					// Get location
					chart.snapPanel.relX = chartData.getDouble("x");
					chart.snapPanel.relY = chartData.getDouble("y");
					chart.snapPanel.relWidth = chartData.getDouble("width");
					chart.snapPanel.relHeight = chartData.getDouble("height");
					
					chart.snapPanel.updateBounds(window.centerChartPanel.getWidth(), window.centerChartPanel.getHeight());
					
					// Get xTypes
					JSONArray xTypeArray = chartData.getJSONArray("xTypes");
					chart.xTypes = new DataType[xTypeArray.length()];
					for (int j = 0; j < chart.xTypes.length; j++) {
						JSONObject xTypeData = xTypeArray.getJSONObject(j);
						
						chart.xTypes[j] = new DataType(xTypeData.getInt("index"), xTypeData.getInt("tableIndex"));
					}
					
					// Add yType
					JSONObject yTypeData = chartData.getJSONObject("yType");
					chart.yType = new DataType(yTypeData.getInt("index"), yTypeData.getInt("tableIndex"));
				}
				
				updateUI();
			}
		}
	}
	
	public void addChart() {
		addChart(false);
	}
	
	/**
	 * @param silent Will not perform tasks such as updating the UI or selecting the chart
	 */
	public void addChart(boolean silent) {
		XYChart xyChart = new XYChartBuilder().title("Altitude vs Timestamp (s)").xAxisTitle("Timestamp (s)").yAxisTitle("Altitude (m)").build();

		// Customize Chart
		XYStyler firstChartStyler = xyChart.getStyler();
		
		firstChartStyler.setLegendPosition(LegendPosition.InsideNE);
		firstChartStyler.setLegendVisible(true);
		firstChartStyler.setLegendBackgroundColor(LEGEND_BACKGROUND_COLOR);
		firstChartStyler.setToolTipsEnabled(true);
		firstChartStyler.setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
		firstChartStyler.setYAxisGroupPosition(1, YAxisPosition.Right);

		// Series
		xyChart.addSeries("series0", new double[] { 0 }, new double[] { 0 });
		
		XChartPanel<XYChart> chartPanel = new XChartPanel<>(xyChart);
		window.centerChartPanel.add(chartPanel);
		
		DataChart dataChart = new DataChart(this, xyChart, chartPanel);
		
		// Set default size
		dataChart.snapPanel.setRelSize(600, 450);
		
		// Add these default charts to the list
		synchronized (window.charts) {
			window.charts.add(dataChart);
		}
		
		// Set to be selected
		window.centerChartPanel.setComponentZOrder(chartPanel, 0);
		dataChart.snapPanel.setSnapPanelListener(this);
		
		if (selectedChart != null) selectedChart.chartPanel.setBorder(null);
		
		if (!silent) {
			selectedChart = dataChart;
			
			snapPanelSelected(selectedChart.snapPanel);
			
			updateUI();
		}
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
					
					synchronized (window.charts) {
						// Set chart to be based on this row
						selectedChart.xTypes = formattedSelections;
					}
					
					dataTable.setColumnSelectionInterval(0, 0);
					
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
		
		synchronized (window.charts) {
			for (DataChart chart : window.charts) {
				chart.snapPanel.containerResized(currentChartContainerWidth, currentChartContainerHeight);
			}
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

class LayoutFileFilter extends javax.swing.filechooser.FileFilter {

	@Override
	public boolean accept(File pathname) {
		if (pathname.isDirectory()) {
			return true;
		} else {
			return pathname.getName().endsWith(".rlay");
		}
	}

	@Override
	public String getDescription() {
		return "Rocket Layout File (.rlay)";
	}
	
}
