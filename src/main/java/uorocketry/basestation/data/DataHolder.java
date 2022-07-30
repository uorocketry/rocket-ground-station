package uorocketry.basestation.data;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.TableModel;

import uorocketry.basestation.config.DataSet;

public class DataHolder {
	
	public Data[] data;
	public DataType[] types;

	public static final DataType TIMESTAMP = new DataType(0, 0);
	public static final DataType ALTITUDE = new DataType(1, 0);
	
	/** Which of this data should be hidden for any reason */
	public List<DataType> hiddenDataTypes = new LinkedList<DataType>();
	
	/**
	 * This chooses which table this data is displayed in
	 */
	public int tableIndex = 0;

	private DataSet dataSet;

	
	public DataHolder(int tableIndex, DataSet dataSet) {
		this.tableIndex = tableIndex;
		this.dataSet = dataSet;
		
		this.data = new Data[dataSet.getLabels().length];
		
		types = new DataType[dataSet.getLabels().length];
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

			if (dataSet.indexIsType("state", i) && data[i].getDecimalValue() != null) {
				dataText = dataSet.getState(data[i].getDecimalValue().intValue());
			}
			
			// Set data
			tableModel.setValueAt(dataText, i, 1);
		}
	}
	
	public boolean set(int index, String currentData) {
		// Check for special cases first
		boolean isFormattedCoordinate =  (dataSet.indexIsType("latitude", index) && dataSet.indexIsType("latitudeFormatted", index))
											|| (dataSet.indexIsType("longitude", index) && dataSet.indexIsType("longitudeFormatted", index));
		boolean isTimestamp = dataSet.indexIsType("timestamp", index);
		boolean isPressure = dataSet.indexIsType("pressure", index);

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
				longData = Long.parseLong(currentData.trim());
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
				floatData = Float.parseFloat(currentData.trim());
			} catch (NumberFormatException e) {
				if (currentData.equals("ovf") || currentData.contentEquals("nan")) {
					// ovf means overflow
					floatData = null;
				} else {
					System.err.println("Number conversion failed for '" + currentData + "', -1 being used instead");
					
					return false;
				}
			}
			
			data[index] = new Data(floatData, isPressure ? Data.Types.PRESSURE : Data.Types.NORMAL);
		}
		
		return true;
	}
}
