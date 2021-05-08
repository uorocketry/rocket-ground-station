package uorocketry.basestation.panel;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

import uorocketry.basestation.Main;
import uorocketry.basestation.data.DataHandler;
import uorocketry.basestation.data.DataType;

public class DataChart {
    public XYChart xyChart;
	
	public XChartPanel<XYChart> chartPanel;
	/** The snap panel for this chart */
	public SnapPanel snapPanel;
	
	public Main main;
	
	// The active chart series on this chart
	public String[] activeSeries = new String[0];
	
	public DataType[] xTypes = {DataHandler.ALTITUDE};
	public DataType yType = DataHandler.TIMESTAMP;
	
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
