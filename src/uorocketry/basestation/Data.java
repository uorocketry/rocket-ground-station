package uorocketry.basestation;

import java.text.DecimalFormat;

public class Data {
	//the actual data point
	float data;
	
	//used for special data types
	float minutes;
	
	Types type;
	
	DecimalFormat format = new DecimalFormat("#.######");
	
	enum Types {
		NORMAL,
		FORMATTED_COORDINATE
	}
	
	public Data(float data) {
		this.data = data;
		
		type = Types.NORMAL;
		
		format.setGroupingUsed(true);
		format.setGroupingSize(3);
	}
	
	/**
	 * Used for formatted coordinates.
	 * Not used anymore since the SBG returns a decimal coordinate.
	 * 
	 * @param degrees
	 * @param minutes
	 */
	public Data(float degrees, float minutes) {
		this.data = degrees;
		this.minutes = minutes;
		
		type = Types.FORMATTED_COORDINATE;
	}
	
	public String getFormattedString() {
		switch (type) {
			case FORMATTED_COORDINATE:
				return Math.round(data) + "° " + minutes + " '";
			default: 
				return format.format(data);
		}
	}
	
	public float getDecimalValue() {
		switch (type) {
			case FORMATTED_COORDINATE:
				return getDecimalCoordinate();
			default:
				return data;
		}
	}
	
	/**
	 * Only used by type COORDINATE
	 * @return
	 */
	private float getDecimalCoordinate() {
		return data + minutes/60;
	}
}
