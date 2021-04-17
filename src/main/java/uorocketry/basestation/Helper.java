package uorocketry.basestation;

import org.json.JSONArray;

public class Helper {
	
	/**
	 * 
	 * @param array
	 * @return
	 */
	public static int[] toIntArray(JSONArray array) {
		int[] result = new int[array.length()];
		
		for (int i = 0; i < array.length(); i++) {
			result[i] = array.getInt(i);
		}
		
		return result;
	}
	
	public static boolean arrayIncludes(int[] array, int value) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == value) return true;
		}
		
		return false;
	}
}
