package uorocketry.basestation.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import uorocketry.basestation.connections.DataReceiver;
import uorocketry.basestation.connections.DeviceConnection;

public class DataProcessor implements DataReceiver {

	private List<List<DataHolder>> allData = new ArrayList<>(2);
	private JSONObject config;

	public DataProcessor(JSONObject config, int dataSourceCount) {
		this.config = config;

		allData = new ArrayList<>(dataSourceCount);
		for (int i = 0; i < dataSourceCount; i++) {
			allData.add(new ArrayList<>());
		}
	}

	/** Separator for the data */
	public static final String SEPARATOR = ",";
    
	@Override
	public void receivedData(DeviceConnection deviceConnection, byte[] data) {
        
	}

	public DataHolder parseData(String data, int tableIndex) {
		DataHolder dataHolder = new DataHolder(tableIndex, config.getJSONArray("datasets").getJSONObject(tableIndex));
		
		// Clear out the b' ' stuff added that is only meant for the radio to see
		data = data.replaceAll("b'|\\\\r\\\\n'", "");
		if (data.endsWith(",")) data = data.substring(0, data.length() - 1);
		
		// Semi-colon separated
		String[] splitData = data.split(SEPARATOR);
		if (splitData.length != dataHolder.data.length) {
			//invalid data
			System.err.println("Line with invalid data (Not the correct amount of data). It was " + splitData.length);
			
			return null;
		}
		
		// TODO: Potentially remove and test this
		// Ensure that the timestamp has not gone back in time
		try {
			DataHolder lastDataPointDataHolder = findLastValidDataPoint(allData.get(tableIndex));
			
			int timestampIndex = config.getJSONArray("datasets").getJSONObject(tableIndex).getInt("timestampIndex");
			if (lastDataPointDataHolder != null) {
			    Float value = lastDataPointDataHolder.data[timestampIndex].getDecimalValue();
			    if (value != null && Float.parseFloat(splitData[timestampIndex]) < value) {
					System.err.println("Timestamp just went backwards");

					// Treat as invalid data
			        return null;
			    }
			}
		} catch (NumberFormatException | JSONException e) {}
		
		for (int i = 0; i < splitData.length; i++) {
			if (!dataHolder.set(i, splitData[i])) {
				System.err.println("Failed to set data handler");

				// Parsing failed
				return null;
			}
		}

		return dataHolder;
	}

	/**
	 * Find last non null data point
	 * 
	 * @param currentTableData
	 * @return DataHolder if found, null otherwise
	 */
	private DataHolder findLastValidDataPoint(List<DataHolder> currentTableData) {
		for (int i = currentTableData.size() - 1; i >= 0; i--) {
			if (currentTableData.get(i) != null) {
				return currentTableData.get(i);
			}
		}
		
		return null;
	}

    public List<List<DataHolder>> getAllData() {
		return allData;
	}


}
