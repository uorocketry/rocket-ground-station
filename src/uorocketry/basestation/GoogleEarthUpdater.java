package uorocketry.basestation;

import java.awt.MultipleGradientPaint.CycleMethod;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
	public String generateKMLFile(List<DataHandler> allData, int currentDataIndex) {
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
		
		content.append("<Placemark>\r\n");
		content.append("<name>Rocket Path</name>\r\n");
		content.append("<styleUrl>#blackLine</styleUrl>");
		content.append("<LineString><altitudeMode>absolute</altitudeMode><coordinates>\r\n");
		
		for (int i = 0; i <= currentDataIndex; i++) {
			String currentString = getCoordinateString(allData.get(i));
			
			if (currentString != null) {
				content.append(currentString + "\r\n");
			}
			
		}
		
		content.append("</coordinates></LineString>\r\n");
		content.append("</Placemark>\r\n");
		
		//Add the latest coordinate as a placemark
		String latestDataString = getCoordinateString(allData.get(currentDataIndex));
		if (latestDataString != null) {
			content.append("<Placemark>\r\n");
			content.append("<name>Latest Position</name>\r\n");
			content.append("<Point>\r\n<altitudeMode>absolute</altitudeMode>\r\n<coordinates>");
			
			content.append(latestDataString);
			
			content.append("</coordinates>\r\n</Point>\r\n</Placemark>\r\n");
		}
		
		
		content.append("</Document></kml>");
		
		return content.toString();
	}
	
	public void updateKMLFile(List<DataHandler> allData, int currentDataIndex) {
		String fileContent = generateKMLFile(allData, currentDataIndex);
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.GOOGLE_EARTH_DATA_LOCATION), StandardCharsets.UTF_8))) {
		   writer.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
				updateKMLFile(allData, currentDataIndex);
			}
		};
		try {
			mapRefreshTimer.schedule(mapRefreshTaskTimer, 50);
		} catch (IllegalStateException e) {
			// Ignore as another has already started
		}
	}
	
	public String getCoordinateString(DataHandler dataPoint) {
		if (dataPoint != null && dataPoint.data[DataHandler.LONGITUDE].data != 0 && dataPoint.data[DataHandler.LATITUDE].data != 0) {
			return "-" + dataPoint.data[DataHandler.LONGITUDE].getDecimalCoordinate() + "," + dataPoint.data[DataHandler.LATITUDE].getDecimalCoordinate() + "," + dataPoint.data[DataHandler.ALTITUDE].data;
		}
		
		return null;
	}
}
