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
import uorocketry.basestation.data.Data;
import uorocketry.basestation.data.DataHandler;

/**
 * DOES NOT support multiple data sources
 * 
 * @author Ajay
 *
 */
public class WebViewUpdater extends WebSocketServer {
	
	List<WebSocket> connections = new ArrayList<>();
	
	public WebViewUpdater() {
		super(new InetSocketAddress(Main.WEBVIEW_PORT));
		
		start();
	}

	/**
	 * Generates a json from the data currentDataIndex
	 * 
	 * @param main
	 */
	public JSONObject generateJSON(List<List<DataHandler>> allData, List<Integer> minDataIndex, List<Integer> currentDataIndex, JSONArray dataSets) {
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
	 * Sends updated data over the websocket channel
	 * 
	 * @param tableIndex
	 * @param allData
	 * @param currentDataIndex
	 * @param dataSets
	 */
	public void sendUpdate(List<List<DataHandler>> allData, List<Integer> minDataIndex, List<Integer> currentDataIndex, JSONArray dataSets) {
		JSONObject jsonObject = generateJSON(allData, minDataIndex, currentDataIndex, dataSets);
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
