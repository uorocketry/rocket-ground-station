package uorocketry.basestation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uorocketry.basestation.connections.ComConnectionHolder;
import uorocketry.basestation.connections.DataReciever;
import uorocketry.basestation.control.StateButton;
import uorocketry.basestation.data.DataTableCellRenderer;
import uorocketry.basestation.panel.DataChart;

public class Window extends JFrame {
	
	private static final long serialVersionUID = -5397816377154627951L;
	private Main main;
	
	private JPanel dataTablePanel;
	ArrayList<JTable> dataTables = new ArrayList<>();
	
	private JPanel leftPanel;
	private JScrollPane scrollPane;
	JCheckBox googleEarthCheckBox;
	JCheckBox webViewCheckBox;
	JCheckBox simulationCheckBox;
	
	private JPanel chartDataPointsOptions;
	JCheckBox onlyShowLatestDataCheckBox;
	JPanel maxDataPoints;
	JButton setMaxDataPointsButton;
	JTextField maxDataPointsTextField;
	
	private JPanel dataTools;
	JButton restoreDeletedData;
	JCheckBox dataDeletionModeCheckBox;
	
	private JPanel sliderSection;
	private JPanel sliderButtons;
	private JPanel eastSliderButtons;
	private JPanel westSliderButtons;
	
	List<JSlider> maxSliders = new ArrayList<JSlider>(2);
	List<JSlider> minSliders = new ArrayList<JSlider>(2);
	public JTabbedPane sliderTabs;
	JButton clearDataButton;
	JButton refreshComSelectorButton;
	JButton hideComSelectorButton;
	JButton hideBarsButton;
	JButton pauseButton;
	JButton latestButton;
	JLabel savingToLabel;
	
	public JPanel sidePanel;
	public JPanel comPanelParent;
	
	public JPanel stateSendingPanel;
	public List<List<StateButton>> stateButtons = new ArrayList<>();
	
	public JPanel centerChartPanel;
	
	public ArrayList<DataChart> charts = new ArrayList<>();
	
	JButton addChartButton;
	private JPanel savingToPanel;
	private JPanel layoutTools;
	
	JButton saveLayout;
	JButton loadLayout;
	private JSplitPane splitPane;
	
	public Window(Main main) {
	    this.main = main;
	    
		// Set look and feel
		try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
		
		setSize(1200, 782);
		setTitle("Ground Station");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		splitPane = new JSplitPane();
		getContentPane().add(splitPane, BorderLayout.CENTER);
		
		leftPanel = new JPanel();
		leftPanel.setAlignmentY(Component.TOP_ALIGNMENT);
		leftPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		
		dataTablePanel = new JPanel();
		dataTablePanel.setAlignmentY(Component.TOP_ALIGNMENT);
		dataTablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		dataTablePanel.setLayout(new BoxLayout(dataTablePanel, BoxLayout.X_AXIS));
		leftPanel.add(dataTablePanel);
		
		for (int i = 0; i < Main.dataSourceCount; i++) {
			addJTable(i, main.config.getJSONArray("datasets").getJSONObject(i));
		}
		
		scrollPane = new JScrollPane(leftPanel);
		
		googleEarthCheckBox = new JCheckBox("Google Earth");
		leftPanel.add(googleEarthCheckBox);
		
		webViewCheckBox = new JCheckBox("Web View");
		leftPanel.add(webViewCheckBox);
		
		simulationCheckBox = new JCheckBox("Simulation");
		leftPanel.add(simulationCheckBox);
		
		chartDataPointsOptions = new JPanel();
		chartDataPointsOptions.setBorder(new TitledBorder(null, "Chart Data Points", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		leftPanel.add(chartDataPointsOptions);
		chartDataPointsOptions.setLayout(new BoxLayout(chartDataPointsOptions, BoxLayout.Y_AXIS));
		
		onlyShowLatestDataCheckBox = new JCheckBox("Only Show Latest Data");
		chartDataPointsOptions.add(onlyShowLatestDataCheckBox);
		
		maxDataPoints = new JPanel();
		maxDataPoints.setAlignmentY(Component.TOP_ALIGNMENT);
		maxDataPoints.setAlignmentX(Component.LEFT_ALIGNMENT);
		maxDataPoints.setLayout(new BoxLayout(maxDataPoints, BoxLayout.X_AXIS));
		chartDataPointsOptions.add(maxDataPoints);
		
		maxDataPointsTextField = new JTextField(6);
		maxDataPointsTextField.setMaximumSize(maxDataPointsTextField.getPreferredSize());
		maxDataPointsTextField.setText(Main.maxDataPointsDisplayed + "");
		maxDataPoints.add(maxDataPointsTextField);
		
		setMaxDataPointsButton = new JButton("Set");
		maxDataPoints.add(setMaxDataPointsButton);
		
		dataTools = new JPanel();
		dataTools.setBorder(new TitledBorder(null, "Data", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		leftPanel.add(dataTools);
		dataTools.setLayout(new BoxLayout(dataTools, BoxLayout.Y_AXIS));
		
		dataDeletionModeCheckBox = new JCheckBox("Data Deletion Mode");
		dataTools.add(dataDeletionModeCheckBox);
		
		restoreDeletedData = new JButton("Restore Deleted Data");
		dataTools.add(restoreDeletedData);
		
		layoutTools = new JPanel();
		layoutTools.setBorder(new TitledBorder(null, "Layout", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		leftPanel.add(layoutTools);
		layoutTools.setLayout(new BoxLayout(layoutTools, BoxLayout.Y_AXIS));
		
		saveLayout = new JButton("Save Layout");
		layoutTools.add(saveLayout);
		
		loadLayout = new JButton("Load Layout");
		layoutTools.add(loadLayout);
		
		savingToPanel = new JPanel();
		savingToPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		leftPanel.add(savingToPanel);
		savingToPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		
		savingToLabel = new JLabel("Saving to data/log.txt");
		savingToPanel.add(savingToLabel);
		
		splitPane.setLeftComponent(scrollPane);
		
		scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
		scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		scrollPane.setViewportBorder(null);
		
		sliderSection = new JPanel();
		getContentPane().add(sliderSection, BorderLayout.SOUTH);
		sliderSection.setLayout(new BorderLayout(0, 0));
		
		sliderTabs = new JTabbedPane(JTabbedPane.TOP);
		
		for (int i = 0; i < Main.dataSourceCount; i++) {
			addSlider(main.config.getJSONArray("datasets").getJSONObject(i));
		}
		
		sliderSection.add(sliderTabs, BorderLayout.SOUTH);
		
		sliderButtons = new JPanel();
		sliderSection.add(sliderButtons, BorderLayout.NORTH);
		sliderButtons.setLayout(new BorderLayout(0, 0));
		
		westSliderButtons = new JPanel();
		sliderButtons.add(westSliderButtons, BorderLayout.WEST);
		
		addChartButton = new JButton("Add Chart");
		westSliderButtons.add(addChartButton);
		
		eastSliderButtons = new JPanel();
		sliderButtons.add(eastSliderButtons, BorderLayout.EAST);
		
		clearDataButton = new JButton("Clear Data");
		eastSliderButtons.add(clearDataButton);
		
		refreshComSelectorButton = new JButton("Refresh Com Selector");
        eastSliderButtons.add(refreshComSelectorButton);
		
		hideComSelectorButton = new JButton("Hide Com Selector");
		eastSliderButtons.add(hideComSelectorButton);
		
		hideBarsButton = new JButton("Hide Sliders");
		eastSliderButtons.add(hideBarsButton);
		
		pauseButton = new JButton("Pause");
		eastSliderButtons.add(pauseButton);
		
		latestButton = new JButton("Detach From Latest");
		eastSliderButtons.add(latestButton);
		
		sidePanel = new JPanel();
		getContentPane().add(sidePanel, BorderLayout.EAST);
		sidePanel.setLayout(new BorderLayout());
		
		comPanelParent = new JPanel();
		sidePanel.add(comPanelParent, BorderLayout.SOUTH);
		
		try {
			JSONArray array = main.config.getJSONArray("stateEvents");
			
			if (array.length() > 0) {
				stateSendingPanel = new JPanel();
				sidePanel.add(stateSendingPanel, BorderLayout.NORTH);
				stateSendingPanel.setLayout(new BoxLayout(stateSendingPanel, BoxLayout.Y_AXIS));
			}
			
			List<StateButton> buttons = new ArrayList<StateButton>(array.length());
			stateButtons.add(buttons);

			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				StateButton stateButton = new StateButton(main.comConnectionHolder, object.getString("name"), (byte) object.getInt("data"), object.getJSONArray("successStates"), object.getJSONArray("availableStates"));
				
				stateSendingPanel.add(stateButton.getPanel());
				buttons.add(stateButton);
			}
			
			if (buttons.size() > 0) {
			    addComSelectorPanel("Button Box", buttons.stream().toArray(StateButton[]::new));
            }
		} catch (JSONException e) {
			// No states then
			if (stateSendingPanel != null) {
				stateSendingPanel.setVisible(false);
			}
		}
		
		for (int i = 0; i < Main.dataSourceCount; i++) {
            addComSelectorPanel(main.config.getJSONArray("datasets").getJSONObject(i), main);
        }
		comPanelParent.setLayout(new GridLayout(comPanelParent.getComponentCount(), 1, 0, 0));
		
		centerChartPanel = new JPanel();
		
		splitPane.setRightComponent(centerChartPanel);

		centerChartPanel.setLayout(null);

		setVisible(true);
		
	}
	
	public void addJTable(int tableIndex, JSONObject dataSet) {
		JTable dataTable = new JTable(Main.dataLength.get(tableIndex), 2);
		dataTable.setBorder(new LineBorder(new Color(0, 0, 0)));
		dataTable.setDefaultRenderer(Object.class, new DataTableCellRenderer());
		dataTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		dataTable.setAlignmentY(Component.TOP_ALIGNMENT);
		dataTable.setAlignmentX(Component.LEFT_ALIGNMENT);
		dataTable.setCellSelectionEnabled(true);
		
		// Make non editable
		dataTable.setDefaultEditor(Object.class, null);
		
		// Increase row height
		dataTable.setRowHeight(30);
		
		// Adjust width
		dataTable.getColumnModel().getColumn(0).setPreferredWidth(130);
		dataTable.getColumnModel().getColumn(1).setPreferredWidth(130);
		
		dataTable.setFont(new Font("Arial", Font.PLAIN, 15));
		
		// Make outer title
		JPanel borderPanel = new JPanel();
		borderPanel.setBorder(BorderFactory.createTitledBorder(dataSet.getString("name")));
		
		borderPanel.add(dataTable);
		
		dataTablePanel.add(borderPanel);
		dataTables.add(dataTable);
	}
	
	public void addSlider(JSONObject dataSet) {
		// Add sliders to tabbedPane
		JPanel sliders = new JPanel();
		sliderSection.add(sliders, BorderLayout.SOUTH);
		sliders.setLayout(new BorderLayout(0, 0));
		
		JSlider maxSlider = new JSlider();
		sliders.add(maxSlider, BorderLayout.NORTH);
		maxSlider.setPaintTicks(true);
		maxSlider.setValue(0);
		
		JSlider minSlider = new JSlider();
		sliders.add(minSlider, BorderLayout.SOUTH);
		minSlider.setValue(0);
		minSlider.setPaintTicks(true);
		
		maxSliders.add(maxSlider);
		minSliders.add(minSlider);
		
		sliderTabs.add(sliders, dataSet.getString("name"));
	}
	
	public void addComSelectorPanel(JSONObject dataSet, DataReciever... dataRecievers) {
	    addComSelectorPanel(dataSet.getString("name"), dataRecievers);
    }
	
	public void addComSelectorPanel(String name, DataReciever... dataRecievers) {
		JPanel comPanel = new JPanel();
		comPanel.setLayout(new BoxLayout(comPanel, BoxLayout.Y_AXIS));
		comPanel.setBorder(BorderFactory.createTitledBorder(name));
		
		JList<String> comSelector = new JList<String>();
		comSelector.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		comPanel.add(comSelector);
		
		JLabel comConnectionSuccessLabel = new JLabel();
		comConnectionSuccessLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		comConnectionSuccessLabel.setHorizontalAlignment(SwingConstants.CENTER);
		comConnectionSuccessLabel.setOpaque(true);
		comPanel.add(comConnectionSuccessLabel);
		
		comPanelParent.add(comPanel);
		
		main.comConnectionHolder.add(ComConnectionHolder.Type.TABLE, dataRecievers, comPanel,
		        comSelector, comConnectionSuccessLabel);
	}

}
