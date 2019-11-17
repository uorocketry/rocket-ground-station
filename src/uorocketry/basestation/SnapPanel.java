package uorocketry.basestation;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

/**
 * Makes JPanel have the ability to snap and move in an absolute layout
 */
public class SnapPanel implements MouseListener, MouseMotionListener {
	
	int lastMouseX = -1;
	int lastMouseY = -1;
	
	DataChart chart;
	JPanel panel;
	
	SnapPanelListener snapPanelListener;
	
	/** Used to keep the action consistent and make resizing not stop mid-resize if the mouse moved fast. */
	boolean resizing = false;
	
	public SnapPanel(DataChart chart) {
		this.chart = chart;
		this.panel = chart.chartPanel;
		
		panel.addMouseListener(this);
		panel.addMouseMotionListener(this);
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		panel.getParent().setComponentZOrder(panel, 0);
		panel.getParent().repaint();
		
		snapPanelListener.snapPanelSelected(this);
	}
	
	public void setSnapPanelListener(SnapPanelListener snapPanelListener) {
		this.snapPanelListener = snapPanelListener;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		lastMouseX = -1;
		lastMouseY = -1;
		
		resizing = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int currentX = e.getXOnScreen();
		int currentY = e.getYOnScreen();
		
		if (lastMouseX != -1 && lastMouseY != -1) {
			Rectangle currentBounds = panel.getBounds();
			
			if (resizing) {
				panel.setSize((int) currentBounds.getWidth() + (currentX - lastMouseX), (int) currentBounds.getHeight() + (currentY - lastMouseY));
			} else {
				// Move panel
				panel.setLocation((int) currentBounds.getX() + (currentX - lastMouseX), (int) currentBounds.getY() + (currentY - lastMouseY));
			}
			
		} else {
			int currentXRelative = e.getX();
			int currentYRelative = e.getY();
			
			if (currentXRelative > panel.getWidth() - 20 && currentYRelative > panel.getHeight() - 20) {
				resizing = true;
			}
		}
		
		lastMouseX = currentX;
		lastMouseY = currentY;
	}
	
	/**
	 * Called whenever the parent is resized to change the layout to the new size.
	 * 
	 * @param xFactor The factor the x is stretched by (new/old)
	 * @param yFactor The factor the y is stretched by (new/old)
	 */
	public void containerResized(double xFactor, double yFactor) {
		Rectangle currentBounds = panel.getBounds();
		
		panel.setBounds((int) (currentBounds.getX() * xFactor), (int) (currentBounds.getY() * yFactor), (int) (currentBounds.getWidth() * xFactor), (int) (currentBounds.getHeight() * yFactor));
	
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
	}
	
}
