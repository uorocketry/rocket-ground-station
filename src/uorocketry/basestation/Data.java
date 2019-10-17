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
		COORDINATE
	}
	
	public Data(float data) {
		this.data = data;
		
		type = Types.NORMAL;
		
		format.setGroupingUsed(true);
		format.setGroupingSize(3);
	}
	
	public Data(float degrees, float minutes) {
		this.data = degrees;
		this.minutes = minutes;
		
		type = Types.COORDINATE;
	}
	
	public String getFormattedString() {
		switch (type) {
			case COORDINATE:
				return Math.round(data) + "° " + minutes + " '";
			default: 
				return format.format(data);
		}
	}
}
