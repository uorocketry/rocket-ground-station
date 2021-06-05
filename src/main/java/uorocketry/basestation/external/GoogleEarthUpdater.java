package uorocketry.basestation.external;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uorocketry.basestation.Main;
import uorocketry.basestation.data.Data;
import uorocketry.basestation.data.DataHolder;

public class GoogleEarthUpdater {
	
	/**
	 * Used to update the kml file again after a small amount of time to force Google Earth to refresh the kml file.
	 */
	Timer mapRefreshTimer;
	TimerTask mapRefreshTaskTimer;
	
	public GoogleEarthUpdater() {
		mapRefreshTimer = new Timer();
	}
	
	/**
	 * Generates a kml file from the data currentDataIndex
	 * 
	 * @param main
	 */
	public String generateKMLFile(List<List<DataHolder>> allData, List<Integer> minDataIndex, List<Integer> currentDataIndex, JSONArray dataSets) {
		StringBuilder content = new StringBuilder();
		
		content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
				"<kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document>");
		
		// Add style
		content.append("<Style id='blackLine'>\r\n" + 
				"	<LineStyle>\r\n" + 
				"      <color>FF000000</color>\r\n" + 
				"      <width>5</width>\r\n" + 
				"    </LineStyle>" +
				"    </Style>");
		
		for (int i = 0; i < allData.size(); i++) {
			// Add style
			content.append("<Style id='blackLine'>\r\n"); 
			content.append("<LineStyle>\r\n");
			content.append("<color>" + dataSets.getJSONObject(i).getString("color"));
			content.append("</color>\r\n");
			content.append("<width>5</width>\r\n");
			content.append("</LineStyle>");
			content.append("</Style>");
			
			content.append("<Placemark>\r\n");
			content.append("<name>Path of " + dataSets.getJSONObject(i).getString("name"));
			content.append("</name>\r\n");
			content.append("<styleUrl>#blackLine</styleUrl>");
			content.append("<LineString><altitudeMode>absolute</altitudeMode><coordinates>\r\n");
			
			for (int j = minDataIndex.get(i); j <= currentDataIndex.get(i); j++) {
				String currentString = getCoordinateString(allData.get(i).get(j), dataSets.getJSONObject(i).getJSONObject("coordinateIndexes"));
				
				if (currentString != null) {
					content.append(currentString + "\r\n");
				}
				
			}
			
			content.append("</coordinates></LineString>\r\n");
			content.append("</Placemark>\r\n");
			
			//Add the latest coordinate as a placemark
			String latestDataString = getCoordinateString(allData.get(i).get(currentDataIndex.get(i)), dataSets.getJSONObject(i).getJSONObject("coordinateIndexes"));
			if (latestDataString != null) {
				content.append("<Placemark>\r\n");
				content.append("<name>Latest Position of " + dataSets.getJSONObject(i).getString("name"));
				content.append("</name>\r\n");
				content.append("<Point>\r\n<altitudeMode>absolute</altitudeMode>\r\n<coordinates>");
				
				content.append(latestDataString);
				
				content.append("</coordinates>\r\n</Point>\r\n</Placemark>\r\n");
			}
		}
		
		content.append("</Document></kml>");
		
		return content.toString();
	}
	
	/**
	 * Updates the KML file with the data up to currentDataIndex.
	 * 
	 * @param tableIndex
	 * @param allData
	 * @param currentDataIndex
	 * @param dataSets
	 * @param secondRun Is this a second run? This is true if it is being run from a task called by this function.
	 * 		  The task is run to force Google Earth to update the display.
	 */
	public void updateKMLFile(List<List<DataHolder>> allData, List<Integer> minDataIndex, List<Integer> currentDataIndex, JSONArray dataSets, boolean secondRun) {
		if (!secondRun) {
			if (mapRefreshTaskTimer != null) {
				// No need to update again that recently
				return;
			}

			// Start a new task
			mapRefreshTaskTimer = new TimerTask() {
				@Override
				public void run() {
					updateKMLFile(allData, minDataIndex, currentDataIndex, dataSets, true);
					
					mapRefreshTaskTimer = null;
				}
			};
			try {
				mapRefreshTimer.schedule(mapRefreshTaskTimer, 1000);
			} catch (IllegalStateException e) {
				// Ignore as another has already started
			}
		}
		
		String fileContent = generateKMLFile(allData, minDataIndex, currentDataIndex, dataSets);
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(Main.GOOGLE_EARTH_DATA_LOCATION), StandardCharsets.UTF_8))) {
		   writer.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getCoordinateString(DataHolder dataPoint, JSONObject coordinateIndexes) {
		if (dataPoint == null) return null;
		
		Data altitudeData = dataPoint.data[coordinateIndexes.getInt("altitude")];
		Data longitudeData = dataPoint.data[coordinateIndexes.getInt("longitude")];
		Data latitudeData = dataPoint.data[coordinateIndexes.getInt("latitude")];
		
		String prefixString = "";
		try {
			if (coordinateIndexes.getBoolean("formattedCoordinates")) {
				// Assume west
				prefixString = "-";
			}
		} catch (JSONException e) {}
		
		if (longitudeData.data != 0 && latitudeData.data != 0 && longitudeData.getDecimalValue() != null && latitudeData.getDecimalValue() != null) {
			return prefixString + longitudeData.getDecimalValue() + "," + latitudeData.getDecimalValue() + "," + altitudeData.data;
		}
		
		return null;
	}
}
