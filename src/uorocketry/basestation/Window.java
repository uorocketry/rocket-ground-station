package uorocketry.basestation;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JSlider;

public class Window extends JFrame {
	
	private static final long serialVersionUID = -5397816377154627951L;
	
	JTable dataTable;
	private JPanel dataTablePanel;
	private JScrollPane scrollPane;
	
	JSlider slider;
	
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
		getContentPane().setLayout(new BorderLayout(0, 0));
		
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
		
		slider = new JSlider();
		slider.setPaintTicks(true);
		slider.setValue(0);
		getContentPane().add(slider, BorderLayout.SOUTH);
		
		setVisible(true);
	}

}
