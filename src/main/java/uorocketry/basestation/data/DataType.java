package uorocketry.basestation.data;

public class DataType {
	public int index;
	public int tableIndex;
	
	public DataType(int index, int tableIndex) {
		this.index = index;
		this.tableIndex = tableIndex;
	}
	
	@Override
	public boolean equals(Object object) {
		if (object != null && object.getClass() == getClass()) {
			DataType other = (DataType) object;
			
			return equals(other.index, other.tableIndex);
		}
		
		return false;
	}
	
	public boolean equals(int index, int tableIndex) {
		return this.index == index && this.tableIndex == tableIndex;
	}
}
