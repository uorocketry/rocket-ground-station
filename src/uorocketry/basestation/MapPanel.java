package uorocketry.basestation;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class MapPanel extends JPanel {
	
	private static final long serialVersionUID = -6133681739811652607L;
	
	BufferedImage mapImage;
	
	public MapPanel() {
		
		// Load map image
		try {
			mapImage = ImageIO.read(getClass().getResourceAsStream("/maps/map.jpg"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setPreferredSize(new Dimension(200, 200));
	}
	
	@Override
	public void paint(Graphics g) {
		g.drawImage(mapImage, 0, 0, getWidth(), getHeight(), this);
	}
}
