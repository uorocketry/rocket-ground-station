package uorocketry.basestation.data;

import java.text.DecimalFormat;

public class Data {
	//the actual data point
	private Float data;

	private Long dataLong;
	
	//used for special data types
	private float minutes;

	private Types type;

	private DecimalFormat format = new DecimalFormat("#.######");
	
	enum Types {
		NORMAL,
		LONG,
		FORMATTED_COORDINATE,
		PRESSURE
	}
	
	public Data(Float data) {
		this.data = data;
		
		type = Types.NORMAL;
		
		format.setGroupingUsed(true);
		format.setGroupingSize(3);
	}

	public Data(Float data, Types type) {
		this(data);

		this.type = type;
	}
	
	/**
	 * Used for timestamps
	 * 
	 * @param data
	 */
	public Data(Long data) {
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
				return dataLong != null ? format.format(dataLong) : "null";
			case PRESSURE:
				if (data != null) {
					String unitOne = format.format(data);
					String unitTwo = format.format(data * 6.895f);
					return unitOne + " PSI | " + unitTwo + " kPa";
				} else {
					return "null";
				}
			default:
				return data != null ? format.format(data) : "null";
		}
	}
	
	public Float getDecimalValue() {
		switch (type) {
			case FORMATTED_COORDINATE:
				return getDecimalCoordinate();
			case LONG:
				return dataLong != null ? dataLong.floatValue() : null;
			default:
				return data;
		}
	}
	
	public Long getLongValue() {
		switch (type) {
			case LONG:
				return dataLong;
			default:
			    Float value = getDecimalValue();
				return value != null ? value.longValue() : null;
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
