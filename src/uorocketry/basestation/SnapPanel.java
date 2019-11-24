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
	Rectangle startBoundsRectangle = null;
	
	DataChart chart;
	JPanel panel;
	
	SnapPanelListener snapPanelListener;
	
	/** Used to keep the action consistent and make resizing not stop mid-resize if the mouse moved fast. */
	boolean resizing = false;
	
	/** Used to tell if the location should be moved along with the scaling */
	boolean resizeLeft = false;
	boolean resizeTop = false;
	
	/** Stores the bounds (x, y, width, height) in a relative way (less than 1) to be converted to the real screen bounds. */
	double relX;
	double relY;
	double relWidth;
	double relHeight;
	
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
		startBoundsRectangle = null;
		
		resizing = false;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		int currentX = e.getXOnScreen();
		int currentY = e.getYOnScreen();
		
		if (lastMouseX != -1 && lastMouseY != -1) {
			if (resizing) {
				// Used to make growth go in the opposite direction depending on where the scale is from
				int xChangeFactor = 1;
				int yChangeFactor = 1;
				
				// How much should the window be moved (depends on where the window is resized from)
				int xMovementFactor = 0;
				int yMovementFactor = 0;
				
				if (resizeLeft) {
					xChangeFactor = -1;
					xMovementFactor = 1;
				}
				if (resizeTop) {
					yChangeFactor = -1;
					yMovementFactor = 1;
				}
				
				// If it reaches a cap, it is like the mouse was in a different position
				int[] mousePosition = setRelSize((int) startBoundsRectangle.getWidth() + xChangeFactor * (currentX - lastMouseX), 
						(int) startBoundsRectangle.getHeight() + yChangeFactor * (currentY - lastMouseY));
			
				// Don't waste time moving it if it doesn't affect anything
				if (xMovementFactor != 0 || yMovementFactor != 0) {
					setRelPosition((int) startBoundsRectangle.getX() + xMovementFactor * (currentX - lastMouseX - mousePosition[0]), 
							(int) startBoundsRectangle.getY() + yMovementFactor * (currentY - lastMouseY - mousePosition[1]));
				}
			} else {
				// Move panel
				setRelPosition((int) startBoundsRectangle.getX() + (currentX - lastMouseX), (int) startBoundsRectangle.getY() + (currentY - lastMouseY));
			}
			
		} else {
			int currentXRelative = e.getX();
			int currentYRelative = e.getY();
			
			int cornerSize = 50;
			
			if ((currentXRelative < cornerSize || currentXRelative > panel.getWidth() - cornerSize) 
					&& (currentYRelative > panel.getHeight() - cornerSize || currentYRelative < cornerSize)) {
				
				resizing = true;
				resizeLeft = currentXRelative < cornerSize;
				resizeTop = currentYRelative < cornerSize;
			}
			
			lastMouseX = currentX;
			lastMouseY = currentY;
			
			startBoundsRectangle = panel.getBounds();
		}
		
		
	}
	
	public void setRelBounds(int absoluteX, int absoluteY, int absoluteWidth, int absoluteHeight) {
		setRelPosition(absoluteX, absoluteY);
		setRelSize(absoluteWidth, absoluteHeight);
	}
	
	public void setRelPosition(int absoluteX, int absoluteY) {
		Rectangle screenBounds = panel.getParent().getBounds();
		
		// Don't let it go off screen
		if (absoluteX < 0) {
			absoluteX = 0;
		} else if (absoluteX > screenBounds.getWidth() - panel.getWidth()) {
			absoluteX = (int) (screenBounds.getWidth() - panel.getWidth());
		}
		
		if (absoluteY < 0) {
			absoluteY = 0;
		} else if (absoluteY > screenBounds.getHeight() - panel.getHeight()) {
			absoluteY = (int) (screenBounds.getHeight() - panel.getHeight());
		}
		
		relX = absoluteX / screenBounds.getWidth();
		relY = absoluteY / screenBounds.getHeight();
		
		panel.setLocation(absoluteX, absoluteY);
	}

	/**
	 * Returns false if the minimum size has been hit.
	 * 
	 * @param absoluteWidth
	 * @param absoluteHeight
	 * @return
	 */
	public int[] setRelSize(int absoluteWidth, int absoluteHeight) {
		// If a cap is reached, treat it as if the mouse was in a different position
		int[] mousePosition = new int[]{0, 0};
		
		Rectangle screenBounds = panel.getParent().getBounds();
		
		// Don't get too small
		if (absoluteWidth < 100) {
			mousePosition[0] = 100 - absoluteWidth;
			
			absoluteWidth = 100;
		}
		if (absoluteHeight < 100) {
			mousePosition[1] = 100 - absoluteHeight;

			absoluteHeight = 100;
		}
		
		relWidth = absoluteWidth / screenBounds.getWidth();
		relHeight = absoluteHeight / screenBounds.getHeight();
		
		panel.setSize(absoluteWidth, absoluteHeight);
		
		return mousePosition;
	}
	
	/**
	 * Called whenever the parent is resized to change the layout to the new size.
	 * 
	 * @param xFactor The factor the x is stretched by (new/old)
	 * @param yFactor The factor the y is stretched by (new/old)
	 */
	public void containerResized(int newWidth, int newHeight) {
		panel.setBounds((int) (relX * newWidth), (int) (relY * newHeight), (int) (relWidth * newWidth), (int) (relHeight * newHeight));
	
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
