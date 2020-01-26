package uorocketry.basestation;

import javax.swing.JTable;
import javax.swing.table.TableModel;

public class DataHandler {
	
	static final DataType TIMESTAMP = new DataType(0, 0);
	static final DataType ALTITUDE = new DataType(1, 0);
	static final DataType LATITUDE = new DataType(2, 0);
	static final DataType LONGITUDE = new DataType(3, 0);
	static final DataType PITCH = new DataType(4, 0);
	static final DataType ROLL = new DataType(5, 0);
	static final DataType YAW = new DataType(6, 0);
	static final DataType ACCELX = new DataType(7, 0);
	static final DataType ACCELY = new DataType(8, 0);
	static final DataType ACCELZ = new DataType(9, 0);
	static final DataType VELOCITY = new DataType(10, 0);
	static final DataType BRAKE_PERCENTAGE = new DataType(10, 0);
	static final DataType ACTUAL_BRAKE_VALUE = new DataType(12, 0);
	static final DataType GPS_FIX = new DataType(13, 0);
	static final DataType GPS_FIX_QUALITY = new DataType(14, 0);
	
	Data[] data;
	
	/**
	 * This chooses which table this data is displayed in
	 */
	int tableIndex = 0;
	
	public DataHandler(int tableIndex) {
		this.tableIndex = tableIndex;
		
		this.data = new Data[Main.dataLength.get(tableIndex)];
	}
	
	public void updateTableUIWithData(JTable table, String[] labels) {
		TableModel tableModel = table.getModel();
		
		for (int i = 0; i < data.length; i++) {
			// Set label
			tableModel.setValueAt(labels[i], i, 0);
			
			// Set data
			tableModel.setValueAt(data[i].getFormattedString(), i, 1);
		}
	}
	
	public void set(int index, String currentData) {
		// Check for special cases first
		if (LATITUDE.equals(index, tableIndex) || LONGITUDE.equals(index, tableIndex)) {
			float degrees = 0;
			float minutes = 0;
			
			int minutesIndex = currentData.indexOf(".") - 2;
			//otherwise, it is badly formatted and probably still zero
			if (minutesIndex >= 0) {
				minutes = Float.parseFloat(currentData.substring(minutesIndex, currentData.length()));
				degrees = Float.parseFloat(currentData.substring(0, minutesIndex));
			}
			
			data[index] = new Data(degrees, minutes);
		} else {
			
			// Normal case
			float floatData = -1;
			
			try {
				floatData = Float.parseFloat(currentData);
			} catch (NumberFormatException e) {
				if (currentData.equals("ovf")) {
					// ovf means overflow
					floatData = Float.MAX_VALUE;
				} else {
					System.err.println("Number conversion failed for '" + currentData + "', -1 being used instead");
				}
			}
			
			data[index] = new Data(floatData);
		}
		
	}
}
