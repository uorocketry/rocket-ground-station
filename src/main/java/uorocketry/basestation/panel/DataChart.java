package uorocketry.basestation.panel;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import uorocketry.basestation.Main;
import uorocketry.basestation.config.Config;
import uorocketry.basestation.data.DataHolder;
import uorocketry.basestation.data.DataType;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class DataChart implements Chart {
    private XYChart xyChart;

	private XChartPanel<XYChart> chartPanel;
	/** The snap panel for this chart */
	private SnapPanel snapPanel;

	private Main main;
	private Config config;
	
	// The active chart series on this chart
	private String[] activeSeries = new String[0];

	private DataType[] xTypes = {DataHolder.ALTITUDE};
	private DataType yType = DataHolder.TIMESTAMP;
	
	public DataChart(Main main, XYChart xyChart, XChartPanel<XYChart> chartPanel) {
		this.main = main;
		this.config = main.config;
		
		this.xyChart = xyChart;
		this.chartPanel = chartPanel;
		
		this.snapPanel = new SnapPanel(main, this);
		
		activeSeries = new String[xTypes.length];
		for (int i = 0; i < xTypes.length; i++) {
			activeSeries[i] = "series" + i;
		}
	}
	
	public DataChart(Main main, XYChart xyChart, XChartPanel<XYChart> chartPanel, DataType[] xTypes) {
		this(main, xyChart, chartPanel);

		this.xTypes = xTypes;
	}

	@Override
	public void update(List<List<DataHolder>> dataHolders, int minDataIndex, int maxDataIndex, boolean onlyShowLatestData, int maxDataPointsDisplayed) {
		// Update altitude chart
		ArrayList<Float> altitudeDataX = new ArrayList<>();
		ArrayList<ArrayList<Float>> altitudeDataY = new ArrayList<ArrayList<Float>>();

		// Add all array lists
		for (int i = 0; i < xTypes.length; i++) {
			altitudeDataY.add(new ArrayList<Float>());
		}
		
		if (onlyShowLatestData) minDataIndex = Math.max(maxDataIndex - maxDataPointsDisplayed, minDataIndex);

		// Add y axis
		{

			for (int i = minDataIndex; i <= maxDataIndex; i++) {
				if (dataHolders.get(yType.tableIndex).size() == 0) continue;

				DataHolder data = dataHolders.get(yType.tableIndex).get(i);
				DataHolder other = dataHolders.get(xTypes[0].tableIndex).get(i);

				if (data != null && (other == null || !other.hiddenDataTypes.contains(other.types[xTypes[0].index]))) {
					altitudeDataX.add(data.data[yType.index].getDecimalValue());
				}
			}
		}


		// Add x axis
		for (int i = 0; i < xTypes.length; i++) {
			if (xTypes[i].tableIndex != yType.tableIndex) continue;
			
			// Used to limit the max number of data points displayed
			float targetRatio = (float) maxDataPointsDisplayed / (maxDataIndex - minDataIndex);
			int dataPointsAdded = 0;

			for (int j = minDataIndex; j <= maxDataIndex; j++) {
				if (dataHolders.get(yType.tableIndex).size() == 0) continue;

				DataHolder data = dataHolders.get(xTypes[i].tableIndex).get(j);

				if (data != null) {
					// Ensures that not too many data points are displayed
					// Always show data if only showing latest data (that is handled by changing the minSlider)
					boolean shouldShowDataPoint = onlyShowLatestData || ((float) dataPointsAdded / j <= targetRatio);

					if (!data.hiddenDataTypes.contains(data.types[xTypes[i].index]) && shouldShowDataPoint ) {
						altitudeDataY.get(i).add(data.data[xTypes[i].index].getDecimalValue());

						dataPointsAdded++;
					} else if (!shouldShowDataPoint) {
						// Hidden data
						altitudeDataY.get(i).add(null);
					}
				}
			}
		}

		if (altitudeDataX.size() == 0) {
			// Add default data
			altitudeDataX.add(0f);

			for (int j = 0; j < xTypes.length; j++) {
				altitudeDataY.get(j).add(0f);
			};
		}

		String[] newActiveSeries = new String[xTypes.length];
		StringBuilder title = new StringBuilder();

		// Set Labels
		for (int i = 0; i < xTypes.length; i++) {
			String xTypeTitle = config.getLabels(xTypes[i].tableIndex)[xTypes[i].index];

			if (title.length() != 0) title.append(", ");
			title.append(xTypeTitle);

			xyChart.setYAxisGroupTitle(i, xTypeTitle);

			XYSeries series = null;

			if (activeSeries.length > i) {
				series = xyChart.updateXYSeries("series" + i, altitudeDataX, altitudeDataY.get(i), null);
			} else {
				series = xyChart.addSeries("series" + i, altitudeDataX, altitudeDataY.get(i), null);
			}

			series.setLabel(xTypeTitle);
			series.setYAxisGroup(i);

			newActiveSeries[i] = "series" + i;
		}

		String yTypeTitle = config.getLabels(yType.tableIndex)[yType.index];

		xyChart.setTitle(title + " vs " + yTypeTitle);

		xyChart.setXAxisTitle(yTypeTitle);

		// Remove extra series
		for (int i = xTypes.length; i < activeSeries.length; i++) {
			xyChart.removeSeries("series" + i);
		}

		activeSeries = newActiveSeries;
	}

	@Override
	public boolean mouseDragged(MouseEvent e) {
		if (main.dataDeletionMode) {
			double xMousePos = chartPanel.getChart().getChartXFromCoordinate(e.getX());

			// Start value - Last value is the total chart size in chart coordinates
			double chartSizeX = Math.abs(chartPanel.getChart().getChartXFromCoordinate(0) -
					chartPanel.getChart().getChartXFromCoordinate(chartPanel.getChart().getWidth()));

			// Find all data points near the click
			for (int xTypeIndex = 0; xTypeIndex < xTypes.length; xTypeIndex++) {
				DataType currentType = xTypes[xTypeIndex];
				List<DataHolder> dataHolders = main.dataProcessor.getAllReceivedData().get(currentType.tableIndex);

				// Y axis depends on the which data is being checked
				double yMousePos = chartPanel.getChart().getChartYFromCoordinate(e.getY(), xTypeIndex);
				double chartSizeY = Math.abs(chartPanel.getChart().getChartYFromCoordinate(0, xTypeIndex) -
						chartPanel.getChart().getChartYFromCoordinate(chartPanel.getChart().getHeight(), xTypeIndex));

				for (DataHolder dataHolder: dataHolders) {
					// See if click is anywhere near this point
					if (dataHolder != null && Math.abs(dataHolder.data[yType.index].getDecimalValue() - xMousePos) <= chartSizeX / main.maxDataPointsDisplayed
							&& Math.abs(dataHolder.data[currentType.index].getDecimalValue() - yMousePos) <= chartSizeY / 100
							&& !dataHolder.hiddenDataTypes.contains(currentType)) {

						// Hide this point
						dataHolder.hiddenDataTypes.add(new DataType(currentType.index, currentType.tableIndex));
					}
				}
			}

			main.updateUI();

			// Don't allow window moving
			return true;
		}

		return false;
	}

	@Override
	public JPanel getPanel() {
		return chartPanel;
	}

	@Override
	public SnapPanel getSpanPanel() {
		return snapPanel;
	}

	@Override
	public DataType[] getXTypes() {
		return xTypes;
	}

	@Override
	public void setXTypes(DataType[] xTypes) {
		this.xTypes = xTypes;
	}

	@Override
	public DataType getYType() {
		return yType;
	}

	@Override
	public void setYType(DataType yTypes) {
		this.yType = yTypes;
	}
}
