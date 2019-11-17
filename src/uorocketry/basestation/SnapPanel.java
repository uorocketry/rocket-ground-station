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
	
	JPanel panel;
	
	public SnapPanel(JPanel panel) {
		this.panel = panel;
		
		panel.addMouseListener(this);
		panel.addMouseMotionListener(this);
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		panel.getParent().setComponentZOrder(panel, 0);
		panel.getParent().repaint();
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
	public void mouseReleased(MouseEvent e) {
		lastMouseX = -1;
		lastMouseY = -1;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int currentX = e.getXOnScreen();
		int currentY = e.getYOnScreen();
		
		if (lastMouseX != -1 && lastMouseY != -1) {
			Rectangle currentBounds = panel.getBounds();
			
			panel.setLocation((int) currentBounds.getX() + (currentX - lastMouseX), (int) currentBounds.getY() + (currentY - lastMouseY));
		}
		
		lastMouseX = currentX;
		lastMouseY = currentY;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
	}
	
}
