package uorocketry.basestation;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

public class DataChart {
	XYChart xyChart;
	
	XChartPanel<XYChart> chartPanel;
	/** The snap panel for this chart */
	SnapPanel snapPanel;
	
	Window window;
	
	int xType = DataHandler.ALTITUDE;
	
	public DataChart(Window window, XYChart xyChart, XChartPanel<XYChart> chartPanel) {
		this.window = window;
		
		this.xyChart = xyChart;
		this.chartPanel = chartPanel;
		
		this.snapPanel = new SnapPanel(this);
	}
	
	public DataChart(Window window, XYChart xyChart, XChartPanel<XYChart> chartPanel, int xType) {
		this(window, xyChart, chartPanel);

		this.xType = xType;
	}
}
