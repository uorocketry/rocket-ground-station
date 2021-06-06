package uorocketry.basestation.data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uorocketry.basestation.connections.DataReceiver;
import uorocketry.basestation.connections.DeviceConnection;

public class DataProcessor {

	private List<List<DataHolder>> allData;
	private JSONObject config;

	/** Where to save the log file */
	public static final String LOG_FILE_SAVE_LOCATION = "data/";
	public static final String DEFAULT_LOG_FILE_NAME = "log";
	public static final String LOG_FILE_EXTENSION = ".txt";
	/** Will have a number appended to the end to not overwrite old logs */
	final ArrayList<String> currentLogFileName;
	final ArrayList<Queue<byte[]>> logQueues;

	public DataProcessor(JSONObject config, int dataSourceCount) {
		this.config = config;

		allData = new ArrayList<>(dataSourceCount);
		currentLogFileName = new ArrayList<>(dataSourceCount);
		logQueues = new ArrayList<>(dataSourceCount);

		for (int i = 0; i < dataSourceCount; i++) {
			allData.add(new ArrayList<>());
			logQueues.add(new ArrayDeque<>());
		}
	}

	/** Separator for the data */
	public static final String SEPARATOR = ",";
    
	public void receivedData(@NotNull DeviceConnection deviceConnection, byte[] data) {
		receivedData(deviceConnection.getTableIndex(), data);

		logData(deviceConnection, data);
	}

	public void receivedData(int tableIndex, byte[] data) {
		//TODO: Determine whether a normal packet or RSSI packet before converting to string
		String delimitedMessage = new String(data, StandardCharsets.UTF_8);

		allData.get(tableIndex).add(parseData(tableIndex, delimitedMessage));
	}

	private DataHolder parseData(int tableIndex, String data) {
		DataHolder dataHolder = new DataHolder(tableIndex, config.getJSONArray("datasets").getJSONObject(tableIndex));
		
		// Clear out the b' ' stuff added that is only meant for the radio to see
		data = data.replaceAll("b'|(?:\\\\r\\\\n|\\r\\n)'?", "");
		if (data.endsWith(",")) data = data.substring(0, data.length() - 1);

		// Semi-colon separated
		String[] splitData = data.split(SEPARATOR);
		if (splitData.length != dataHolder.data.length) {
			//invalid data
			System.err.println("Line with invalid data (Not the correct amount of data). It was "
					+ splitData.length + " vs " + dataHolder.data.length + ". " + data);
			
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
	 * Log the raw bytes received
	 *
	 * @param deviceConnection
	 * @param data
	 */
	private void logData(DeviceConnection deviceConnection, byte[] data) {
		if (!deviceConnection.isWriting()) {
			deviceConnection.setWriting(true);

			// Write to file
			try (OutputStream outputStream = new FileOutputStream(
					LOG_FILE_SAVE_LOCATION + currentLogFileName.get(deviceConnection.getTableIndex()))) {
				outputStream.write(data);

				Queue<byte[]> logQueue = logQueues.get(deviceConnection.getTableIndex());
				if (!logQueue.isEmpty()) {
					byte[][] queueItems = new byte[100][];
					synchronized (logQueues) {
						for (int i = 0; !logQueue.isEmpty() && i < queueItems.length; i++) {
							queueItems[i] = logQueue.remove();
						}
					}

					for (int i = 0; i < queueItems.length && queueItems[i] != null; i++) {
						outputStream.write(queueItems.length);
					}
				}


			} catch (IOException err) {
				err.printStackTrace();
			}

			deviceConnection.setWriting(false);
		} else {
			synchronized (logQueues) {
				logQueues.get(deviceConnection.getTableIndex()).add(data);
			}
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
			for (int j = 0 ; j < getDataSourceCount(); j++) {
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
		for (int i = 0 ; i < getDataSourceCount(); i++) {
			currentLogFileName.add(DEFAULT_LOG_FILE_NAME + "_" + dataSets.getJSONObject(i).getString("name").toLowerCase() + "_" + logIndex + LOG_FILE_EXTENSION);
		}
	}

	public String formattedSavingToLocations() {
		StringBuilder savingToText = new StringBuilder();

		// Add text for each file
		for (int i = 0 ; i < getDataSourceCount(); i++) {
			if (i != 0) savingToText.append(", ");

			savingToText.append(LOG_FILE_SAVE_LOCATION + currentLogFileName.get(i));
		}

		return savingToText.toString();
	}

	private int getDataSourceCount() {
		return allData.size();
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
