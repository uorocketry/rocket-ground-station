package uorocketry.basestation.external;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;

import uorocketry.basestation.Main;
import uorocketry.basestation.config.Config;
import uorocketry.basestation.data.Data;
import uorocketry.basestation.data.DataHolder;
import uorocketry.basestation.data.DataPoint;
import uorocketry.basestation.data.DataPointHolder;

/**
 * DOES NOT support multiple data sources
 * 
 * @author Ajay
 *
 */
public class WebViewUpdater extends WebSocketServer {

	private final int TABLE_INDEX = 0;
	
	List<WebSocket> connections = new ArrayList<>();

	public WebViewUpdater() {
		super(new InetSocketAddress(Main.WEBVIEW_PORT));
		
		start();
	}

	/**
	 * Generates a json from the data currentDataIndex
	 */
	public JSONObject generateJSON(DataPointHolder dataPointHolder, List<Integer> minDataIndex, List<Integer> currentDataIndex, Config config) {
		JSONObject jsonObject = new JSONObject();
		
		int index = currentDataIndex.get(TABLE_INDEX);
		List<DataPoint> currentDataList = dataPointHolder.get(TABLE_INDEX);
		if (currentDataList == null) return null;
		
		DataHolder dataHolder = currentDataList.get(index).getReceivedData();
		if (dataHolder == null) return null;
		
		Data altitudeData = dataHolder.data[config.getDataSet(TABLE_INDEX).getIndex("altitude")];
		Data longitudeData = dataHolder.data[config.getDataSet(TABLE_INDEX).getIndex("longitude")];
		Data latitudeData = dataHolder.data[config.getDataSet(TABLE_INDEX).getIndex("latitude")];
		if (altitudeData == null || longitudeData == null || latitudeData == null) return null;

		jsonObject.put("latitude", latitudeData.getDecimalValue());
		jsonObject.put("longitude", longitudeData.getDecimalValue());
		jsonObject.put("altitude", altitudeData.getDecimalValue());

		return jsonObject;
	}
	
	/**
	 * Sends updated data over the websocket channel
	 * 
	 * @param dataPointHolder
	 * @param currentDataIndex
	 * @param config
	 */
	public void sendUpdate(DataPointHolder dataPointHolder, List<Integer> minDataIndex, List<Integer> currentDataIndex, Config config) {
		JSONObject jsonObject = generateJSON(dataPointHolder, minDataIndex, currentDataIndex, config);
		if (jsonObject == null) return;
		
		String fileContent = jsonObject.toString();
		
		for (WebSocket connection : connections) {
			connection.send(fileContent);
		}
 	}
	
	public void close() {
		try {
			super.stop();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		connections.clear();
	}
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) { 
		connections.add(conn);
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		connections.remove(conn);
	}

	@Override
	public void onMessage(WebSocket conn, String message) { }

	@Override
	public void onError(WebSocket conn, Exception ex) { }

	@Override
	public void onStart() { }
}
