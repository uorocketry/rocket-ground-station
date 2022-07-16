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

import uorocketry.basestation.Main;
import uorocketry.basestation.config.Config;
import uorocketry.basestation.config.DataSet;
import uorocketry.basestation.data.Data;
import uorocketry.basestation.data.DataHolder;
import uorocketry.basestation.data.DataPointHolder;

public class GoogleEarthUpdater {

	/**
	 * Used to update the kml file again after a small amount of time to force
	 * Google Earth to refresh the kml file.
	 */
	Timer mapRefreshTimer;
	TimerTask mapRefreshTaskTimer;

	public GoogleEarthUpdater() {
		mapRefreshTimer = new Timer();
	}

	/**
	 * Generates a kml file from the data currentDataIndex
	 */
	public String generateKMLFile(DataPointHolder dataPointHolder, List<Integer> minDataIndex,
			List<Integer> currentDataIndex, Config config) {
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

		for (int i = 0; i < dataPointHolder.size(); i++) {
			int minIndex = dataPointHolder.toReceivedDataIndex(i, minDataIndex.get(i));
			int maxIndex = dataPointHolder.toReceivedDataIndex(i, currentDataIndex.get(i));

			// Add style
			content.append("<Style id='blackLine'>\r\n");
			content.append("<LineStyle>\r\n");
			content.append("<color>" + config.getDatasets()[i].getColor());
			content.append("</color>\r\n");
			content.append("<width>5</width>\r\n");
			content.append("</LineStyle>");
			content.append("</Style>");

			content.append("<Placemark>\r\n");
			content.append("<name>Path of " + config.getDatasets()[i].getName());
			content.append("</name>\r\n");
			content.append("<styleUrl>#blackLine</styleUrl>");
			content.append("<LineString><altitudeMode>absolute</altitudeMode><coordinates>\r\n");

			for (int j = minIndex; j <= maxIndex; j++) {
				String currentString = getCoordinateString(dataPointHolder.getAllReceivedData().get(i).get(j),
						config.getDatasets()[i]);

				if (currentString != null) {
					content.append(currentString + "\r\n");
				}

			}

			content.append("</coordinates></LineString>\r\n");
			content.append("</Placemark>\r\n");

			// Add the latest coordinate as a placemark
			String latestDataString = getCoordinateString(dataPointHolder.getAllReceivedData().get(i).get(maxIndex),
					config.getDatasets()[i]);
			if (latestDataString != null) {
				content.append("<Placemark>\r\n");
				content.append("<name>Latest Position of " + config.getDatasets()[i].getName());
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
	 * @param dataPointHolder
	 * @param minDataIndex
	 * @param currentDataIndex
	 * @param config
	 * @param secondRun        Is this a second run? This is true if it is being run
	 *                         from a task called by this function.
	 *                         The task is run to force Google Earth to update the
	 *                         display.
	 */
	public void updateKMLFile(DataPointHolder dataPointHolder, List<Integer> minDataIndex,
			List<Integer> currentDataIndex, Config config, boolean secondRun) {
		if (!secondRun) {
			if (mapRefreshTaskTimer != null) {
				// No need to update again that recently
				return;
			}

			// Start a new task
			mapRefreshTaskTimer = new TimerTask() {
				@Override
				public void run() {
					updateKMLFile(dataPointHolder, minDataIndex, currentDataIndex, config, true);

					mapRefreshTaskTimer = null;
				}
			};
			try {
				mapRefreshTimer.schedule(mapRefreshTaskTimer, 1000);
			} catch (IllegalStateException e) {
				// Ignore as another has already started
			}
		}

		String fileContent = generateKMLFile(dataPointHolder, minDataIndex, currentDataIndex, config);

		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(Main.GOOGLE_EARTH_DATA_LOCATION), StandardCharsets.UTF_8))) {
			writer.write(fileContent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getCoordinateString(DataHolder dataPoint, DataSet dataSet) {
		if (dataPoint == null)
			return null;

		Data altitudeData = dataPoint.data[dataSet.getIndexes().get("altitude")];
		Data longitudeData = dataPoint.data[dataSet.getIndexes().get("longitude")];
		Data latitudeData = dataPoint.data[dataSet.getIndexes().get("latitude")];

		if (longitudeData.getDecimalValue() != null && latitudeData.getDecimalValue() != null
				&& altitudeData.getDecimalValue() != null
				&& longitudeData.getDecimalValue() != 0 && latitudeData.getDecimalValue() != 0) {
			return longitudeData.getDecimalValue() + "," + latitudeData.getDecimalValue() + ","
					+ altitudeData.getDecimalValue();
		}

		return null;
	}
}
