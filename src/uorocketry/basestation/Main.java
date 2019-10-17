package uorocketry.basestation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

public class Main {
	
	List<DataHandler> allData = new ArrayList<>();
	
	ArrayList<String> labels = new ArrayList<>();
	
	DataHandler currentData;
	
	JFrame frame;
	
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		//Add test data
		
		//Load simulation data
		loadSimulationData("data/data.txt");
		
//		currentData = new DataHandler();
//		for (int i = 0; i < 15; i++) {
//			currentData.set(i, 10 + "");
//		}
		
		for (int i = 0; i < 15; i++) {
			labels.add("TESTING" + i);
		}
		
		Window window = new Window();
		
		window.dataLabel.setText(currentData.getFormattedData(labels));
		
	}
	
	public void loadSimulationData(String file) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		try {
			try {
			    String line = null;

			    while ((line = br.readLine()) != null) {
			        
			        //parse this line and add it as a data point
			        allData.add(parseData(line));
			    }
			} finally {
			    br.close();
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		//setup the current data variable
		currentData = allData.get(0);
	}
	
	public DataHandler parseData(String data) {
		DataHandler dataHandler = new DataHandler();
		
		//clear out the b' ' stuff added that is only meant for the radio to see
		data = data.replaceAll("b'|\\\\r\\\\n'", "");
		
		//semi-colon separated
		String[] splitData = data.split(";");
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
	
	public void loadLabels() {
		
	}
}
