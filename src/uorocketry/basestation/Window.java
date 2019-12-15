package uorocketry.basestation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import javax.swing.JCheckBox;

public class Window extends JFrame {
	
	private static final long serialVersionUID = -5397816377154627951L;
	
	JTable dataTable;
	private JPanel dataTablePanel;
	private JScrollPane scrollPane;
	JCheckBox googleEarthCheckBox;
	JCheckBox simulationCheckBox;
	
	private JPanel sliderSection;
	private JPanel sliderButtons;
	private JPanel eastSliderButtons;
	private JPanel westSliderButtons;
	
	JSlider maxSlider;
	JSlider minSlider;
	JButton latestButton;
	JButton pauseButton;
	
	private JPanel comPanel;
	JList<String> comSelector;
	
	Vector<String> comSelectorData = new Vector<String>();
	JLabel comConnectionSuccess;
	private JPanel sidePanel;
	
	JPanel centerChartPanel;
	
	ArrayList<DataChart> charts = new ArrayList<>();
	
	JButton addChartButton;
	
	public Window() {
		// Set look and feel
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
		
		setSize(1000, 600);
		setTitle("Ground Station");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		dataTablePanel = new JPanel();
		dataTablePanel.setAlignmentY(Component.TOP_ALIGNMENT);
		dataTablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		dataTablePanel.setLayout(new BoxLayout(dataTablePanel, BoxLayout.Y_AXIS));
		
		dataTable = new JTable(Main.dataLength, 2);
		dataTable.setAlignmentY(Component.TOP_ALIGNMENT);
		dataTable.setAlignmentX(Component.LEFT_ALIGNMENT);
		dataTable.setCellSelectionEnabled(true);
		
		// Make non editable
		dataTable.setDefaultEditor(Object.class, null);
		
		// Increase row height
		dataTable.setRowHeight(30);
		
		// Adjust width
		dataTable.getColumnModel().getColumn(0).setPreferredWidth(200);
		dataTable.getColumnModel().getColumn(1).setPreferredWidth(150);
		
		dataTable.setFont(new Font("Arial", Font.PLAIN, 21));
		dataTablePanel.add(dataTable);
		
		scrollPane = new JScrollPane(dataTablePanel);
		
		googleEarthCheckBox = new JCheckBox("Google Earth");
		dataTablePanel.add(googleEarthCheckBox);
		
		simulationCheckBox = new JCheckBox("Simulation");
		dataTablePanel.add(simulationCheckBox);
		getContentPane().add(scrollPane, BorderLayout.WEST);
		scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.setViewportBorder(null);
		
		sliderSection = new JPanel();
		getContentPane().add(sliderSection, BorderLayout.SOUTH);
		sliderSection.setLayout(new BorderLayout(0, 0));
		
		maxSlider = new JSlider();
//		slider.setSnapToTicks(true);
		sliderSection.add(maxSlider);
		maxSlider.setPaintTicks(true);
		maxSlider.setValue(0);
		
		minSlider = new JSlider();
		minSlider.setValue(0);
		minSlider.setPaintTicks(true);
		sliderSection.add(minSlider, BorderLayout.SOUTH);
		
		sliderButtons = new JPanel();
		sliderSection.add(sliderButtons, BorderLayout.NORTH);
		sliderButtons.setLayout(new BorderLayout(0, 0));
		
		westSliderButtons = new JPanel();
		sliderButtons.add(westSliderButtons, BorderLayout.WEST);
		
		addChartButton = new JButton("Add Chart");
		westSliderButtons.add(addChartButton);
		
		eastSliderButtons = new JPanel();
		sliderButtons.add(eastSliderButtons, BorderLayout.EAST);
		
		pauseButton = new JButton("Pause");
		eastSliderButtons.add(pauseButton);
		
		latestButton = new JButton("Latest");
		eastSliderButtons.add(latestButton);
		
		sidePanel = new JPanel();
		getContentPane().add(sidePanel, BorderLayout.EAST);
		sidePanel.setLayout(new GridLayout(2, 1, 0, 0));
		
		comPanel = new JPanel();
		sidePanel.add(comPanel);
		comPanel.setLayout(new BoxLayout(comPanel, BoxLayout.Y_AXIS));
		
		comSelector = new JList<String>();
		comSelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		comPanel.add(comSelector);
		
		comConnectionSuccess = new JLabel();
		comConnectionSuccess.setAlignmentX(Component.CENTER_ALIGNMENT);
		comConnectionSuccess.setHorizontalAlignment(SwingConstants.CENTER);
		comConnectionSuccess.setOpaque(true);
		comPanel.add(comConnectionSuccess);
		
		centerChartPanel = new JPanel();
		getContentPane().add(centerChartPanel, BorderLayout.CENTER);
		
		// Create Chart
		XYChart firstChart = new XYChartBuilder().title("Altitude vs Timestamp (s)").xAxisTitle("Timestamp (s)").yAxisTitle("Altitude (m)").build();
		
		// Customize Chart
		firstChart.getStyler().setLegendPosition(LegendPosition.InsideNE);
		firstChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);

		// Series
		firstChart.addSeries("series0", new double[] { 0 }, new double[] { 0 });
		centerChartPanel.setLayout(null);
		
		XChartPanel<XYChart> chart1Panel = new XChartPanel<>(firstChart);
		centerChartPanel.add(chart1Panel);
		
		// Create the data chart container
		DataChart dataChart = new DataChart(this, firstChart, chart1Panel);
		
		// Add these default charts to the list
		charts.add(dataChart);
		
		setVisible(true);
		
		// Set default chart size
		dataChart.snapPanel.setRelSize(600, 450);
	}

}
