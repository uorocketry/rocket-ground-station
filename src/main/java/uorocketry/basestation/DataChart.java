package uorocketry.basestation;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

public class DataChart {
	XYChart xyChart;
	
	XChartPanel<XYChart> chartPanel;
	/** The snap panel for this chart */
	SnapPanel snapPanel;
	
	Main main;
	
	// The active chart series on this chart
	String[] activeSeries = new String[0];
	
	DataType[] xTypes = {DataHandler.ALTITUDE};
	DataType yType = DataHandler.TIMESTAMP;
	
	public DataChart(Main main, XYChart xyChart, XChartPanel<XYChart> chartPanel) {
		this.main = main;
		
		this.xyChart = xyChart;
		this.chartPanel = chartPanel;
		
		this.snapPanel = new SnapPanel(this);
		
		activeSeries = new String[xTypes.length];
		for (int i = 0; i < xTypes.length; i++) {
			activeSeries[i] = "series" + i;
		}
	}
	
	public DataChart(Main main, XYChart xyChart, XChartPanel<XYChart> chartPanel, DataType[] xTypes) {
		this(main, xyChart, chartPanel);

		this.xTypes = xTypes;
	}
}
