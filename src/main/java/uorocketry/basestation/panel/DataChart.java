package uorocketry.basestation.panel;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import uorocketry.basestation.Main;
import uorocketry.basestation.config.Config;
import uorocketry.basestation.data.DataHolder;
import uorocketry.basestation.data.DataPoint;
import uorocketry.basestation.data.DataPointHolder;
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
	
	public DataChart(Main main, Config config, XYChart xyChart, XChartPanel<XYChart> chartPanel) {
		this.main = main;
		this.config = config;
		
		this.xyChart = xyChart;
		this.chartPanel = chartPanel;
		
		this.snapPanel = new SnapPanel(main, this);
		
		activeSeries = new String[xTypes.length];
		for (int i = 0; i < xTypes.length; i++) {
			activeSeries[i] = "series" + i;
		}
	}
	
	public DataChart(Main main, Config config, XYChart xyChart, XChartPanel<XYChart> chartPanel, DataType[] xTypes) {
		this(main, config, xyChart, chartPanel);

		this.xTypes = xTypes;
	}

	@Override
	public void update(DataPointHolder dataPointHolder, int minDataPointIndex, int maxDataPointIndex, boolean onlyShowLatestData, int maxDataPointsDisplayed) {
		if ((minDataPointIndex <= 0 && maxDataPointIndex <= 0) || toDataHolder(dataPointHolder).size() <= 0) return;

		// Update altitude chart
		ArrayList<Float> altitudeDataX = new ArrayList<>();
		ArrayList<ArrayList<Float>> altitudeDataY = new ArrayList<ArrayList<Float>>();

		// Add all array lists
		for (int i = 0; i < xTypes.length; i++) {
			altitudeDataY.add(new ArrayList<>());
		}
		
		if (onlyShowLatestData) minDataPointIndex = Math.max(maxDataPointIndex - maxDataPointsDisplayed, minDataPointIndex);

		// Add y axis
		{
			int minDataIndex = toDataHolderIndex(dataPointHolder, yType.tableIndex, minDataPointIndex);
			int maxDataIndex = toDataHolderIndex(dataPointHolder, yType.tableIndex, maxDataPointIndex);
			if (minDataIndex <= 0 && maxDataIndex <= 0) return; // Not Ready

			for (int i = minDataIndex; i <= maxDataIndex; i++) {
				if (dataPointHolder.get(yType.tableIndex).size() == 0) continue;

				DataHolder data = toDataHolder(dataPointHolder).get(yType.tableIndex).get(i);
				DataHolder other = toDataHolder(dataPointHolder).get(xTypes[0].tableIndex).get(i);

				if (data != null && (other == null || !other.hiddenDataTypes.contains(other.types[xTypes[0].index]))) {
					altitudeDataX.add(data.data[yType.index].getDecimalValue());
				}
			}
		}


		// Add x axis
		for (int i = 0; i < xTypes.length; i++) {
			if (xTypes[i].tableIndex != yType.tableIndex) continue;

			int minDataIndex = toDataHolderIndex(dataPointHolder, xTypes[i].tableIndex, minDataPointIndex);
			int maxDataIndex = toDataHolderIndex(dataPointHolder, xTypes[i].tableIndex, maxDataPointIndex);
			if (minDataIndex <= 0 && maxDataIndex <= 0) return; // Not Ready

			// Used to limit the max number of data points displayed
			float targetRatio = (float) maxDataPointsDisplayed / (maxDataIndex - minDataIndex);
			int dataPointsAdded = 0;

			for (int j = minDataIndex; j <= maxDataIndex; j++) {
				if (dataPointHolder.get(yType.tableIndex).size() == 0) continue;

				DataHolder data = toDataHolder(dataPointHolder).get(xTypes[i].tableIndex).get(j);

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

	/**
	 * Convert an index for the general dataPoints array into an index for the
	 * specific array dataHolder array
	 */
	public int toDataHolderIndex(DataPointHolder dataPointHolder, int tableIndex, int index) {
		//TODO: Choose between receivedDataIndex and connectionInfoDataIndex depending on chart type
		return dataPointHolder.toReceivedDataIndex(tableIndex, index);
	}

	public List<List<DataHolder>> toDataHolder(DataPointHolder dataPointHolder) {
		//TODO: Choose between receivedData and connectionInfoData depending on chart type
		return dataPointHolder.getAllReceivedData();
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
				List<DataHolder> dataHolders = toDataHolder(main.dataProcessor.getDataPointHolder()).get(currentType.tableIndex);

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
