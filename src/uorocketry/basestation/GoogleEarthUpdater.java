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
			
			DataHandler currentData = allData.get(i); 
			if (currentData != null && currentData.data[DataHandler.LONGITUDE].data != 0 && currentData.data[DataHandler.LATITUDE].data != 0) {
				content.append("-" + currentData.data[DataHandler.LONGITUDE].getDecimalCoordinate() + "," + currentData.data[DataHandler.LATITUDE].getDecimalCoordinate() + "," + currentData.data[DataHandler.ALTITUDE].data + "\r\n ");
			}
		}
		
		content.append("</coordinates></LineString>\r\n");
		content.append("</Placemark>\r\n");
		
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
			mapRefreshTaskTimer.cancel();
		}
		
		// Start a new task
		mapRefreshTaskTimer = new TimerTask() {
			@Override
			public void run() {
				updateKMLFile(allData, currentDataIndex);
			}
		};
		mapRefreshTimer.schedule(mapRefreshTaskTimer, 50);
	}
}
