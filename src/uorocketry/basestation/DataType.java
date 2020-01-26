package uorocketry.basestation;

public class DataType {
	int index;
	int tableIndex;
	
	public DataType(int index, int tableIndex) {
		this.index = index;
		this.tableIndex = tableIndex;
	}
	
	public boolean equals(int index, int tableIndex) {
		return this.index == index && this.tableIndex == tableIndex;
	}
}
