package uorocketry.basestation;

public class Data {
	//the actual data point
	float data;
	
	//used for special data types
	float minutes;
	
	Types types;
	
	enum Types {
		NORMAL,
		COORDINATE
	}
	
	public Data(float data) {
		this.data = data;
		
		types = Types.NORMAL;
	}
	
	public Data(float degrees, float minutes) {
		this.data = degrees;
		this.minutes = minutes;
	}
	
	public String getFormattedString() {
		return data + "";
	}
}
