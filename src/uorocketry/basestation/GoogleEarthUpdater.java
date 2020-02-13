package uorocketry.basestation;

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
import org.json.JSONObject;

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
	public String generateKMLFile(List<List<DataHandler>> allData, List<Integer> minDataIndex, List<Integer> currentDataIndex, JSONArray dataSets) {
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
			content.append("<Placemark>\r\n");
			content.append("<name>Rocket Path ");
			content.append(i);
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
				content.append("<name>Latest Position ");
				content.append(i);
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
	public void updateKMLFile(List<List<DataHandler>> allData, List<Integer> minDataIndex, List<Integer> currentDataIndex, JSONArray dataSets, boolean secondRun) {
		String fileContent = generateKMLFile(allData, minDataIndex, currentDataIndex, dataSets);
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(Main.GOOGLE_EARTH_DATA_LOCATION), StandardCharsets.UTF_8))) {
		   writer.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (!secondRun) {
			if (mapRefreshTaskTimer != null) {
				try {
					mapRefreshTaskTimer.cancel();
				} catch (IllegalStateException e) {
					// Ignore if it is already canceled
				}
			}
			
			// Start a new task
			mapRefreshTaskTimer = new TimerTask() {
				@Override
				public void run() {
					updateKMLFile(allData, minDataIndex, currentDataIndex, dataSets, true);
				}
			};
			try {
				mapRefreshTimer.schedule(mapRefreshTaskTimer, 1000);
			} catch (IllegalStateException e) {
				// Ignore as another has already started
			}
		}
	}
	
	public String getCoordinateString(DataHandler dataPoint, JSONObject coordinateIndexes) {
		if (dataPoint == null) return null;
		
		Data altitudeData = dataPoint.data[coordinateIndexes.getInt("altitude")];
		Data longitudeData = dataPoint.data[coordinateIndexes.getInt("longitude")];
		Data latitudeData = dataPoint.data[coordinateIndexes.getInt("latitude")];

		if (longitudeData.data != 0 && latitudeData.data != 0) {
			return "-" + longitudeData.getDecimalCoordinate() + "," + latitudeData.getDecimalCoordinate() + "," + altitudeData.data;
		}
		
		return null;
	}
}
