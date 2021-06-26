package uorocketry.basestation.data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;

import uorocketry.basestation.config.Config;
import uorocketry.basestation.config.DataSet;
import uorocketry.basestation.connections.DeviceConnection;
import uorocketry.basestation.panel.Chart;
import uorocketry.basestation.panel.TableHolder;

import javax.swing.*;
import javax.swing.table.TableModel;

public class DataProcessor {

	/** Separator for the data */
	public static final String SEPARATOR = ",";

	private DataPointHolder dataPointHolder;
	private final Config mainConfig;
	private final List<DataSet> rssiDataSets;

	/** Where to save the log file */
	private static final String LOG_FILE_SAVE_LOCATION = "data/";
	private static final String DEFAULT_LOG_FILE_NAME = "log";
	private static final String LOG_FILE_EXTENSION = ".txt";
	/** Will have a number appended to the end to not overwrite old logs */
	private final ArrayList<String> currentLogFileName;
	private final ArrayList<Queue<byte[]>> logQueues;

	private final ArrayList<TableHolder> dataTables;

	public DataProcessor(Config mainConfig, ArrayList<TableHolder> dataTables) {
		this.mainConfig = mainConfig;
		this.dataTables = dataTables;

		dataPointHolder = new DataPointHolder(mainConfig.getDataSourceCount());
		currentLogFileName = new ArrayList<>(mainConfig.getDataSourceCount());
		logQueues = new ArrayList<>(mainConfig.getDataSourceCount());
		rssiDataSets = new ArrayList<>(mainConfig.getDataSourceCount());

		for (int i = 0; i < mainConfig.getDataSourceCount(); i++) {
			logQueues.add(new ArrayDeque<>());

			rssiDataSets.add(new DataSet(mainConfig.getDataSet(i).getName() + " RSSI",
					mainConfig.getDataSet(i).getColor(), RssiProcessor.labels, null, null, SEPARATOR));
		}
	}

	public void receivedData(@NotNull DeviceConnection deviceConnection, byte[] data) {
		receivedData(deviceConnection.getTableIndex(), data);

		logData(deviceConnection, data);
	}

	public void receivedData(int tableIndex, byte[] data) {
		String delimitedMessage = new String(data, StandardCharsets.UTF_8);

		if (RssiProcessor.isValid(delimitedMessage)) {
			DataHolder dataHolder = parseRSSI(tableIndex, delimitedMessage);
			List<DataHolder> connectionInfoData = dataPointHolder.getAllConnectionInfoData().get(tableIndex);
			connectionInfoData.add(dataHolder);

			List<DataHolder> receivedData = dataPointHolder.getAllReceivedData().get(tableIndex);
			dataPointHolder.get(tableIndex).add(new DataPoint(receivedData, connectionInfoData,
					receivedData.size() - 1,
					connectionInfoData.size() - 1));
		} else {
			DataHolder dataHolder = parseData(tableIndex, delimitedMessage);
			List<DataHolder> receivedData = dataPointHolder.getAllReceivedData().get(tableIndex);
			receivedData.add(dataHolder);

			List<DataHolder> connectionInfoData = dataPointHolder.getAllConnectionInfoData().get(tableIndex);
			dataPointHolder.get(tableIndex).add(new DataPoint(receivedData, connectionInfoData,
					receivedData.size() - 1,
					connectionInfoData.size() - 1));
		}
	}

	protected DataHolder parseData(int tableIndex, String data) {
		DataHolder dataHolder = new DataHolder(tableIndex, mainConfig.getDataSet(tableIndex));
		
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
		
		// Ensure that the timestamp has not gone back in time
		Integer timestampIndex = mainConfig.getDataSet(tableIndex).getIndex("timestamp");
		if (timestampIndex != null) {
			DataHolder lastDataPointDataHolder = findLastValidDataPoint(dataPointHolder.getAllReceivedData().get(tableIndex));
			if (lastDataPointDataHolder != null) {
				Float value = lastDataPointDataHolder.data[timestampIndex].getDecimalValue();
				try {
					if (value != null && Float.parseFloat(splitData[timestampIndex]) < value) {
						System.err.println("Timestamp just went backwards. Original was " + value);

						// Treat as invalid data
						return null;
					}
				} catch (NumberFormatException e) {}

			}
		}

		for (int i = 0; i < splitData.length; i++) {
			if (!dataHolder.set(i, splitData[i])) {
				System.err.println("Failed to set data handler");

				// Parsing failed
				return null;
			}
		}

		return dataHolder;
	}

	protected DataHolder parseRSSI(int tableIndex, String data) {
		DataHolder dataHolder = new DataHolder(tableIndex, rssiDataSets.get(tableIndex));

		if (RssiProcessor.setDataHolder(dataHolder, data)) {
			return dataHolder;
		} else {
			System.err.println("RSSI Line with invalid data. " + data);
			return null;
		}

	}

	/**
	 * Sets tables up for the given index
	 *
	 * @param showMaxIndex If true, it will make sure to always show the latest index,
	 *                     for use when connection info and received data drift apart
	 * @return The received DataHolder
	 */
	public DataPoint setTableTo(int tableIndex, int index, boolean showMaxIndex) {
		DataPoint dataPoint = dataPointHolder.get(tableIndex).get(index);
		if (dataPoint == null) return null;

		DataHolder currentDataHolder = dataPoint.getReceivedData();
		JTable receivedDataTable = dataTables.get(tableIndex).getReceivedDataTable();
		updateTable(index, currentDataHolder, receivedDataTable, mainConfig.getDataSet(tableIndex));

		DataHolder connectionInfoHolder = dataPoint.getConnectionInfoData();
		JTable connectionInfoTable = dataTables.get(tableIndex).getConnectionInfoTable();
		updateTable(index, connectionInfoHolder, connectionInfoTable, rssiDataSets.get(tableIndex));

		return dataPoint;
	}

	private void updateTable(int index, DataHolder currentDataHolder, JTable dataTable, DataSet dataSet) {
		if (currentDataHolder != null) {
			currentDataHolder.updateTableUIWithData(dataTable, dataSet.getLabels());
		} else {
			setTableToError(index, dataTable);
		}
	}

	private void setTableToError(int index, JTable table) {
		TableModel tableModel = table.getModel();

		// Set first item to "Error"
		tableModel.setValueAt("Parsing Error", 0, 0);
		tableModel.setValueAt(index, 0, 1);

		for (int i = 1; i < tableModel.getRowCount(); i++) {
			// Set label
			tableModel.setValueAt("", i, 0);

			// Set data
			tableModel.setValueAt("", i, 1);
		}
	}

	public void updateChart(Chart chart, int minDataIndex, int maxDataIndex, boolean onlyShowLatestData, int maxDataPointsDisplayed) {
		chart.update(dataPointHolder, minDataIndex, maxDataIndex, onlyShowLatestData, maxDataPointsDisplayed);
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
					LOG_FILE_SAVE_LOCATION + currentLogFileName.get(deviceConnection.getTableIndex()), true)) {
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

		JSONArray dataSets = mainConfig.getObject().getJSONArray("datasets");

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
		return dataPointHolder.size();
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

	@Deprecated
    public List<List<DataHolder>> getAllReceivedData() {
		return dataPointHolder.getAllReceivedData();
	}

	public DataPointHolder getDataPointHolder() {
		return dataPointHolder;
	}

}
