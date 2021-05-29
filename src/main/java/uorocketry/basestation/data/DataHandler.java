package uorocketry.basestation.data;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.json.JSONException;
import org.json.JSONObject;

import uorocketry.basestation.Main;

public class DataHandler {
	
    public static final DataType TIMESTAMP = new DataType(0, 0);
	public static final DataType ALTITUDE = new DataType(1, 0);
	public static final DataType LATITUDE = new DataType(2, 0);
	public static final DataType LONGITUDE = new DataType(3, 0);
	public static final DataType PITCH = new DataType(4, 0);
	public static final DataType ROLL = new DataType(5, 0);
	public static final DataType YAW = new DataType(6, 0);
	public static final DataType ACCELX = new DataType(7, 0);
	public static final DataType ACCELY = new DataType(8, 0);
	public static final DataType ACCELZ = new DataType(9, 0);
	public static final DataType VELOCITY = new DataType(10, 0);
	public static final DataType BRAKE_PERCENTAGE = new DataType(10, 0);
	public static final DataType ACTUAL_BRAKE_VALUE = new DataType(12, 0);
	public static final DataType GPS_FIX = new DataType(13, 0);
	public static final DataType GPS_FIX_QUALITY = new DataType(14, 0);
	
	public Data[] data;
	public DataType[] types;
	
	/** Which of this data should be hidden for any reason */
	public List<DataType> hiddenDataTypes = new LinkedList<DataType>();
	
	/**
	 * This chooses which table this data is displayed in
	 */
	public int tableIndex = 0;
	
	private JSONObject datasetConfig;
	
	public DataHandler(int tableIndex, JSONObject datasetConfig) {
		this.tableIndex = tableIndex;
		this.datasetConfig = datasetConfig;
		
		this.data = new Data[Main.dataLength.get(tableIndex)];
		
		types = new DataType[Main.dataLength.get(tableIndex)];
		for (int i = 0; i < types.length; i++) {
			types[i] = new DataType(i, tableIndex);
		}
	}
	
	public void updateTableUIWithData(JTable table, String[] labels) {
		TableModel tableModel = table.getModel();
		
		for (int i = 0; i < data.length; i++) {
			// Set label
			tableModel.setValueAt(labels[i], i, 0);
			
			String dataText = data[i].getFormattedString();
			if (hiddenDataTypes.contains(types[i])) dataText = "Hidden Data";
			
			if (datasetConfig.getInt("stateIndex") == i && data[i].getDecimalValue() != null) {
				dataText = datasetConfig.getJSONArray("states").getString(data[i].getDecimalValue().intValue());
			}
			
			// Set data
			tableModel.setValueAt(dataText, i, 1);
		}
	}
	
	public boolean set(int index, String currentData) {
		// Check for special cases first
		boolean isFormattedCoordinate = false;
		boolean isTimestamp = false;
		try {
			isTimestamp = datasetConfig.getInt("timestampIndex") == index;
			
			JSONObject coordinateIndexes = datasetConfig.getJSONObject("coordinateIndexes");
			isFormattedCoordinate = coordinateIndexes.has("formattedCoordinates") 
					&& coordinateIndexes.getBoolean("formattedCoordinates") 
					&& (coordinateIndexes.getInt("latitude") == index || coordinateIndexes.getInt("longitude") == index);
		} catch (JSONException e) {}
		
		if (isFormattedCoordinate) {
			// These need to be converted to decimal coordinates to be used
			
			float degrees = 0;
			float minutes = 0;
			
			int minutesIndex = currentData.indexOf(".") - 2;
			//otherwise, it is badly formatted and probably still zero
			if (minutesIndex >= 0) {
				minutes = Float.parseFloat(currentData.substring(minutesIndex, currentData.length()));
				degrees = Float.parseFloat(currentData.substring(0, minutesIndex));
			}
			
			data[index] = new Data(degrees, minutes);
		} else if (isTimestamp) {
			// Long case
			Long longData = -1L;
			
			try {
				longData = Long.parseLong(currentData);
			} catch (NumberFormatException e) {
				if (currentData.equals("ovf") || currentData.contentEquals("nan")) {
					// ovf means overflow
					longData = null;
				} else {
					System.err.println("Number conversion failed for '" + currentData + "', -1 being used instead");
					
					return false;
				}
			}
			
			data[index] = new Data(longData);
		} else {
			// Normal case
		    Float floatData = -1f;
			
			try {
				floatData = Float.parseFloat(currentData);
			} catch (NumberFormatException e) {
				if (currentData.equals("ovf") || currentData.contentEquals("nan")) {
					// ovf means overflow
					floatData = null;
				} else {
					System.err.println("Number conversion failed for '" + currentData + "', -1 being used instead");
					
					return false;
				}
			}
			
			data[index] = new Data(floatData);
		}
		
		return true;
	}
}
