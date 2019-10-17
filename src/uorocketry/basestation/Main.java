package uorocketry.basestation;

import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main implements AdjustmentListener {
	
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
	
	List<DataHandler> allData = new ArrayList<>();
	
	String[] labels = new String[DATA_LENGTH];
	
	// Index of the current data point being looked at
	int currentDataIndex = 0;
	
	Window window;
	
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		// Create window
		window = new Window();
		
		// Add scroll bar listener
		window.scrollBar.addAdjustmentListener(this);
		
		// Load simulation data if necessary
		if (SIMULATION) loadSimulationData();
		
		// Update UI once
		updateUI();
	}
	
	public void updateUI() {
		DataHandler currentDataHandler = allData.get(currentDataIndex);
		
		if (currentDataHandler != null) {
			currentDataHandler.updateTableUIWithData(window.dataTable, labels);
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
	 * Triggered every time the scroll bar changes
	 */
	@Override
	public void adjustmentValueChanged(AdjustmentEvent e) {
		if (e.getSource() == window.scrollBar) {
			float scrollBarMaxValue = 100 - window.scrollBar.getVisibleAmount();
			
			currentDataIndex = Math.round((allData.size() - 1) * (window.scrollBar.getValue() / scrollBarMaxValue));
			
			updateUI();
		}
	}
}
