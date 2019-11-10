package uorocketry.basestation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
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

public class Window extends JFrame {
	
	private static final long serialVersionUID = -5397816377154627951L;
	
	JTable dataTable;
	private JPanel dataTablePanel;
	private JScrollPane scrollPane;
	
	private JPanel sliderSection;
	private JPanel sliderButtons;
	private JPanel eastSliderButtons;
	
	JSlider slider;
	JButton latestButton;
	JButton pauseButton;
	
	private JPanel comPanel;
	JList<String> comSelector;
	
	Vector<String> comSelectorData = new Vector<String>();
	JLabel comConnectionSuccess;
	private JPanel sidePanel;
	private JPanel centerPanel;
	
	XChartPanel<XYChart> altitudeChartPanel;
	XYChart altitudeChart;
	
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
		dataTablePanel.setLayout(new BoxLayout(dataTablePanel, BoxLayout.X_AXIS));
		
		dataTable = new JTable(Main.DATA_LENGTH, 2);
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
		getContentPane().add(scrollPane, BorderLayout.WEST);
		scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.setViewportBorder(null);
		
		sliderSection = new JPanel();
		getContentPane().add(sliderSection, BorderLayout.SOUTH);
		sliderSection.setLayout(new BorderLayout(0, 0));
		
		slider = new JSlider();
		slider.setSnapToTicks(true);
		sliderSection.add(slider);
		slider.setPaintTicks(true);
		slider.setValue(0);
		
		sliderButtons = new JPanel();
		sliderSection.add(sliderButtons, BorderLayout.NORTH);
		sliderButtons.setLayout(new BorderLayout(0, 0));
		
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
		
		centerPanel = new JPanel();
		getContentPane().add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new GridLayout(1, 1, 0, 0));
		
		// Create Chart
		altitudeChart = new XYChartBuilder().title("Altitude vs Timestamp").xAxisTitle("Timestamp (ms)").yAxisTitle("Altitude (m)").build();

		// Customize Chart
		altitudeChart.getStyler().setLegendPosition(LegendPosition.InsideNE);
		altitudeChart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Area);

		// Series
		altitudeChart.addSeries("altitude", new double[] { 0 }, new double[] { 0 });
		
		altitudeChartPanel = new XChartPanel<>(altitudeChart);
		centerPanel.add(altitudeChartPanel);
		
		setVisible(true);
	}

}
