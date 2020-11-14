package uorocketry.basestation;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * DOES NOT support multiple data sources
 * 
 * @author Ajay
 *
 */
public class WebViewUpdater {

	/**
	 * Generates a json file from the data currentDataIndex
	 * 
	 * @param main
	 */
	public JSONObject generateJSONFile(List<List<DataHandler>> allData, List<Integer> minDataIndex, List<Integer> currentDataIndex, JSONArray dataSets) {
		JSONObject coordinateIndexes = dataSets.getJSONObject(0).getJSONObject("coordinateIndexes");
		
		JSONObject jsonObject = new JSONObject();
		
		int index = currentDataIndex.get(0);
		List<DataHandler> currentDataList = allData.get(0);
		if (currentDataList == null) return null;
		
		DataHandler dataPoint = currentDataList.get(index);
		if (dataPoint == null) return null;
		
		Data altitudeData = dataPoint.data[coordinateIndexes.getInt("altitude")];
		Data longitudeData = dataPoint.data[coordinateIndexes.getInt("longitude")];
		Data latitudeData = dataPoint.data[coordinateIndexes.getInt("latitude")];
		if (altitudeData == null || longitudeData == null || latitudeData == null) return null;
		
		
		jsonObject.put("latitude", latitudeData.getDecimalValue());
		jsonObject.put("longitude", longitudeData.getDecimalValue());
		jsonObject.put("altitude", altitudeData.getDecimalValue());
		
		return jsonObject;
	}
	
	/**
	 * Updates the JSON file with the data up to currentDataIndex.
	 * 
	 * @param tableIndex
	 * @param allData
	 * @param currentDataIndex
	 * @param dataSets
	 * @param secondRun Is this a second run? This is true if it is being run from a task called by this function.
	 * 		  The task is run to force Google Earth to update the display.
	 */
	public void updateJSONFile(List<List<DataHandler>> allData, List<Integer> minDataIndex, List<Integer> currentDataIndex, JSONArray dataSets, boolean secondRun) {
		JSONObject jsonObject = generateJSONFile(allData, minDataIndex, currentDataIndex, dataSets);
		if (jsonObject == null) return;
		
		String fileContent = jsonObject.toString();
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(Main.WEB_VIEW_DATA_LOCATION), StandardCharsets.UTF_8))) {
		   writer.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
