package uorocketry.basestation.data;

import java.text.DecimalFormat;

public class Data {
	//the actual data point
	public float data;
	
	long dataLong;
	
	//used for special data types
	float minutes;
	
	Types type;
	
	DecimalFormat format = new DecimalFormat("#.######");
	
	enum Types {
		NORMAL,
		LONG,
		FORMATTED_COORDINATE
	}
	
	public Data(float data) {
		this.data = data;
		
		type = Types.NORMAL;
		
		format.setGroupingUsed(true);
		format.setGroupingSize(3);
	}
	
	/**
	 * Used for timestamps
	 * 
	 * @param data
	 */
	public Data(long data) {
		this.dataLong = data;
		
		type = Types.LONG;
		
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
			case LONG:
				return format.format(dataLong);
			default: 
				return format.format(data);
		}
	}
	
	public float getDecimalValue() {
		switch (type) {
			case FORMATTED_COORDINATE:
				return getDecimalCoordinate();
			case LONG:
				return dataLong;
			default:
				return data;
		}
	}
	
	public long getLongValue() {
		switch (type) {
			case LONG:
				return dataLong;
			default:
				return (long) getDecimalValue();
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
