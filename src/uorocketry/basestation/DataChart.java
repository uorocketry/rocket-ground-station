package uorocketry.basestation;

import org.knowm.xchart.XYChart;

public class DataChart {
	XYChart xyChart;
	
	int xType = DataHandler.ALTITUDE;
	
	public DataChart(XYChart xyChart) {
		this.xyChart = xyChart;
	}
	
	public DataChart(XYChart xyChart, int xType) {
		this(xyChart);

		this.xType = xType;
	}
}
