package uorocketry.basestation;

import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Component;
import java.awt.Font;

public class Window extends JFrame {
	
	private static final long serialVersionUID = -5397816377154627951L;
	JLabel dataLabel;
	
	public Window() {
		setSize(1000, 600);
		setTitle("Ground Station");
		setLocationRelativeTo(null);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		
		dataLabel = new JLabel("Data");
		dataLabel.setFont(new Font("Arial", Font.PLAIN, 21));
		
		dataLabel.setAlignmentY(Component.TOP_ALIGNMENT);
		dataLabel.setVerticalAlignment(SwingConstants.TOP);
		
		getContentPane().add(dataLabel);
		
		setVisible(true);
	}

}
