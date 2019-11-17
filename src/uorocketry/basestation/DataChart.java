package uorocketry.basestation;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

public class DataChart {
	XYChart xyChart;
	
	XChartPanel<XYChart> chartPanel;
	/** The snap panel for this chart */
	SnapPanel snapPanel;
	
	int xType = DataHandler.ALTITUDE;
	
	public DataChart(XYChart xyChart, XChartPanel<XYChart> chartPanel) {
		this.xyChart = xyChart;
		this.chartPanel = chartPanel;
		
		this.snapPanel = new SnapPanel(this);
	}
	
	public DataChart(XYChart xyChart, XChartPanel<XYChart> chartPanel, int xType) {
		this(xyChart, chartPanel);

		this.xType = xType;
	}
}
