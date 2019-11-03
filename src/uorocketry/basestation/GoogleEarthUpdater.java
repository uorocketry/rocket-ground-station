package uorocketry.basestation;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.spi.CharsetProvider;
import java.util.List;

public class GoogleEarthUpdater {
	
	/**
	 * Generates a kml file from the data currentDataIndex
	 * 
	 * @param main
	 */
	public static String generateKMLFile(List<DataHandler> allData, int currentDataIndex) {
		StringBuilder content = new StringBuilder();
		
		content.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" + 
				"<kml xmlns=\"http://www.opengis.net/kml/2.2\"><Document>");
		
		for (int i = 0; i <= currentDataIndex; i++) {
			
			DataHandler currentData = allData.get(i); 
			if (currentData != null && currentData.data[DataHandler.LONGITUDE].data != 0 && currentData.data[DataHandler.LATITUDE].data != 0) {
				content.append("<Placemark>\r\n");
				content.append("<name>" + currentData.data[DataHandler.TIMESTAMP].data + "ms</name>\r\n");
				
				content.append("<Point><coordinates>-" + currentData.data[DataHandler.LONGITUDE].getDecimalCoordinate() + "," + currentData.data[DataHandler.LATITUDE].getDecimalCoordinate() + "," + currentData.data[DataHandler.ALTITUDE].data + "</coordinates></Point>\r\n ");
				
				content.append("</Placemark>\r\n");
			}
		}
		
		content.append("</Document></kml>");
		
		return content.toString();
	}
	
	public static void updateKMLFile(List<DataHandler> allData, int currentDataIndex) {
		String fileContent = generateKMLFile(allData, currentDataIndex);
		
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Main.GOOGLE_EARTH_DATA_LOCATION), StandardCharsets.UTF_8))) {
		   writer.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
