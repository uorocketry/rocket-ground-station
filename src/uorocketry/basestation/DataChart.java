package uorocketry.basestation;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

public class DataChart {
	XYChart xyChart;
	
	XChartPanel<XYChart> chartPanel;
	/** The snap panel for this chart */
	SnapPanel snapPanel;
	
	Window window;
	
	// The active chart series on this chart
	String[] activeSeries = new String[0];
	
	int[] xTypes = {DataHandler.ALTITUDE};
	
	public DataChart(Window window, XYChart xyChart, XChartPanel<XYChart> chartPanel) {
		this.window = window;
		
		this.xyChart = xyChart;
		this.chartPanel = chartPanel;
		
		this.snapPanel = new SnapPanel(this);
		
		activeSeries = new String[xTypes.length];
		for (int i = 0; i < xTypes.length; i++) {
			activeSeries[i] = "series" + i;
		}
	}
	
	public DataChart(Window window, XYChart xyChart, XChartPanel<XYChart> chartPanel, int[] xType) {
		this(window, xyChart, chartPanel);

		this.xTypes = xType;
	}
}
