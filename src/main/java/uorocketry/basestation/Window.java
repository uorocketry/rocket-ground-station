package uorocketry.basestation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uorocketry.basestation.config.Config;
import uorocketry.basestation.config.DataSet;
import uorocketry.basestation.connections.DataReceiver;
import uorocketry.basestation.connections.DeviceConnection;
import uorocketry.basestation.connections.DeviceConnectionHolder;
import uorocketry.basestation.control.StateButton;
import uorocketry.basestation.data.DataTableCellRenderer;
import uorocketry.basestation.data.RssiProcessor;
import uorocketry.basestation.panel.Chart;
import uorocketry.basestation.panel.TableHolder;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Window extends JFrame {
	
	private static final long serialVersionUID = -5397816377154627951L;
	private Main main;
	private Config config;

	private JPanel dataTablePanel;
	ArrayList<TableHolder> dataTables = new ArrayList<>();

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
	private JScrollPane sideScrollPane;
	public JPanel comPanelParent;
	
	public JPanel stateSendingPanel;
	public List<List<StateButton>> stateButtons = new ArrayList<>();
	
	public JPanel centerChartPanel;
	
	public final ArrayList<Chart> charts = new ArrayList<>();
	
	JButton addChartButton;
	private JPanel savingToPanel;
	private JPanel layoutTools;
	
	JButton saveLayout;
	JButton loadLayout;
	private JSplitPane splitPane;
	
	public Window(Main main, Config config) {
	    this.main = main;
	    this.config = config;
	    
		// Set look and feel
		try {
			String lookAndFeel = UIManager.getSystemLookAndFeelClassName();
			Stream<UIManager.LookAndFeelInfo> allFeels = Arrays.stream(UIManager.getInstalledLookAndFeels());

			// Force GTK theme if available
			String gtkLook = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
			if (allFeels.anyMatch(look -> gtkLook.equals(look.getClassName()))) {
				lookAndFeel = gtkLook;
			}

            UIManager.setLookAndFeel(lookAndFeel);
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

		System.setProperty("awt.useSystemAAFontSettings","off");
		
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
		
		for (int i = 0; i < config.getDataSourceCount(); i++) {
			generateTelemetryPanel(i, config.getDataSet(i));
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
		onlyShowLatestDataCheckBox.setSelected(true);
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
		
		for (int i = 0; i < config.getDataSourceCount(); i++) {
			addSlider(config.getObject().getJSONArray("datasets").getJSONObject(i));
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
		sidePanel.setLayout(new BorderLayout());

		sideScrollPane = new JScrollPane(sidePanel);
		getContentPane().add(sideScrollPane, BorderLayout.EAST);

		comPanelParent = new JPanel();
		sidePanel.add(comPanelParent, BorderLayout.SOUTH);
		
		try {
			JSONArray array = config.getObject().getJSONArray("stateEvents");
			
			if (array.length() > 0) {
				stateSendingPanel = new JPanel();
				sidePanel.add(stateSendingPanel, BorderLayout.NORTH);
				stateSendingPanel.setLayout(new BoxLayout(stateSendingPanel, BoxLayout.Y_AXIS));
			}
			
			List<StateButton> buttons = new ArrayList<StateButton>(array.length());
			stateButtons.add(buttons);

			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				StateButton stateButton = new StateButton(main.deviceConnectionHolder, config.getDataSet(StateButton.TABLE_INDEX), object.getString("name"), (byte) object.getInt("data"), object.getJSONArray("successStates"), object.getJSONArray("availableStates"));
				
				stateSendingPanel.add(stateButton.getPanel());
				buttons.add(stateButton);
			}
			
			if (buttons.size() > 0) {
			    addComSelectorPanel(DeviceConnectionHolder.Type.BUTTON_BOX, "Button Box", buttons.stream().toArray(StateButton[]::new));
            }
		} catch (JSONException e) {
			// No states then
			if (stateSendingPanel != null) {
				stateSendingPanel.setVisible(false);
			}
		}
		
		for (int i = 0; i < config.getDataSourceCount(); i++) {
            addComSelectorPanel(config.getObject().getJSONArray("datasets").getJSONObject(i), main);
        }
		comPanelParent.setLayout(new GridLayout(comPanelParent.getComponentCount(), 1, 0, 0));
		
		centerChartPanel = new JPanel();
		
		splitPane.setRightComponent(centerChartPanel);

		centerChartPanel.setLayout(null);

		setVisible(true);
		
	}


	public void generateTelemetryPanel(int tableIndex, DataSet dataSet) {
		JPanel borderPanel = new JPanel();
		borderPanel.setBorder(BorderFactory.createTitledBorder(dataSet.getName()));
		borderPanel.setLayout(new BoxLayout(borderPanel, BoxLayout.Y_AXIS));

		JTable receivedDataTable = createTable(config.getDataLength(tableIndex), 2, 30, 130);
		JTable connectionInfoTable = createTable(RssiProcessor.labels.length, 2, 30, 130);
		borderPanel.add(receivedDataTable);
		borderPanel.add(connectionInfoTable);

		dataTables.add(new TableHolder(receivedDataTable, connectionInfoTable));

		dataTablePanel.add(borderPanel);
	}

	public JTable createTable(int rows, int columns, int height, int width) {
		JTable table = new JTable(rows, columns);
		table.setBorder(new LineBorder(new Color(0, 0, 0)));
		table.setDefaultRenderer(Object.class, new DataTableCellRenderer());
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setAlignmentY(Component.TOP_ALIGNMENT);
		table.setAlignmentX(Component.LEFT_ALIGNMENT);
		table.setCellSelectionEnabled(true);
		
		// Make non editable
		table.setDefaultEditor(Object.class, null);
		
		// Increase row height
		table.setRowHeight(height);
		
		// Adjust width
		table.getColumnModel().getColumn(0).setPreferredWidth(width);
		table.getColumnModel().getColumn(1).setPreferredWidth(width);
		
		table.setFont(new Font("Arial", Font.PLAIN, 15));

		return table;
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
	
	public void addComSelectorPanel(JSONObject dataSet, DataReceiver... dataReceivers) {
	    addComSelectorPanel(DeviceConnectionHolder.Type.TABLE, dataSet.getString("name"), dataReceivers);
    }
	
	public void addComSelectorPanel(DeviceConnectionHolder.Type type, String name, DataReceiver... dataReceivers) {
		DeviceConnection deviceConnection = main.deviceConnectionHolder.add(type, dataReceivers, name);

		comPanelParent.add(deviceConnection.getPanel());
	}

}
