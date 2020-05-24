package uorocketry.basestation;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.swing.JPanel;

/**
 * Makes JPanel have the ability to snap and move in an absolute layout
 */
public class SnapPanel implements MouseListener, MouseMotionListener {
	
	int lastMouseX = -1;
	int lastMouseY = -1;
	Rectangle startBoundsRectangle = null;
	
	// Used to check for double clicks
	long lastClickTime = 0;
	
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
		
		// Check to see if a double click occurred
		if (System.nanoTime() - lastClickTime < 1000000000 / 3) {
			Point panelLocation =  panel.getParent().getLocationOnScreen();
			int currentMouseX = e.getXOnScreen() - panelLocation.x;
			int currentMouseY = e.getYOnScreen() - panelLocation.y;
			
			snapToMaxSize(currentMouseX, currentMouseY);
		}
		
		if (e.getButton() == MouseEvent.BUTTON2) {
			// Close this
			chart.main.window.charts.remove(chart);
			chart.main.window.centerChartPanel.remove(panel);
		}
		
		lastClickTime = System.nanoTime();
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
	
	/**
	 * Will snap window to the largest possible window starting at mouseX and mouseY
	 * 
	 * @param mouseX
	 * @param mouseY
	 */
	public void snapToMaxSize(int mouseX, int mouseY) {
		// Snap
		Dimension panelSize = panel.getParent().getSize();
		
		DataChart closestLeftChart = findClosestChart(mouseX, mouseY, 0, 0);
		DataChart closestRightChart = findClosestChart(mouseX, mouseY, 1, 0);
		
		DataChart closestTopChart = findClosestChart(mouseX, mouseY, 0, 1);
		DataChart closestBottomChart = findClosestChart(mouseX, mouseY, 1, 1);
		
		int x = 0;
		if (closestLeftChart != null) x = (int) (closestLeftChart.chartPanel.getBounds().getX() + closestLeftChart.chartPanel.getBounds().getWidth());
		
		int width = (int) panelSize.getWidth() - x;
		if (closestRightChart != null) width = (int) (panelSize.getWidth() - x - (panelSize.getWidth() - closestRightChart.chartPanel.getBounds().getX()));
		
		int y = 0;
		if (closestTopChart != null) y = (int) (closestTopChart.chartPanel.getBounds().getY() + closestTopChart.chartPanel.getBounds().getHeight());
		
		int height = (int) panelSize.getHeight() - y;
		if (closestBottomChart != null) height = (int) (panelSize.getHeight() - y - (panelSize.getHeight() - closestBottomChart.chartPanel.getBounds().getY()));
		
		setRelBounds(x, y, width, height);
	}
	
	/**
	 * @param x
	 * @param y
	 * @param direction 0 searches left, 1 searches right
	 * @param coordinate 0 for x, 1 for y
	 * @return
	 */
	public DataChart findClosestChart(int x, int y, int direction, int coordinate) {
		DataChart closestChart = null;
		
		// This is the variable that will be compared against
		int pos = x;
		// The other coordinate
		int otherPos = y;
		if (coordinate == 1) {
			pos = y;
			otherPos = x;
		}
		
		for (DataChart chart : chart.main.window.charts) {
			if (chart == this.chart) continue;
			
			int currentChartPos = (int) chart.chartPanel.getBounds().getX();
			int currentChartOtherPos = (int) chart.chartPanel.getBounds().getY();
			int currentChartSize = (int) chart.chartPanel.getBounds().getWidth();
			int currentChartOtherSize = (int) chart.chartPanel.getBounds().getHeight();
			int closestChartPos = 0;
			int closestChartSize = 0;
			// To prevent null pointer exceptions
			if (closestChart != null) {
				closestChartPos = (int) closestChart.chartPanel.getBounds().getX();
				closestChartSize = (int) closestChart.chartPanel.getBounds().getWidth();
			}
			
			// For Y coordinate
			if (coordinate == 1) {
				currentChartPos = (int) chart.chartPanel.getBounds().getY();
				currentChartOtherPos = (int) chart.chartPanel.getBounds().getX();
				currentChartSize = (int) chart.chartPanel.getBounds().getHeight();
				currentChartOtherSize = (int) chart.chartPanel.getBounds().getWidth();
				
				if (closestChart != null) {
					closestChartPos = (int) closestChart.chartPanel.getBounds().getY();
					closestChartSize = (int) closestChart.chartPanel.getBounds().getHeight();
				}
			}
			
			if (currentChartOtherPos < otherPos && currentChartOtherPos + currentChartOtherSize > otherPos) {
				// Check for direction == 0
				boolean check = currentChartPos < pos && 
						(closestChart == null || closestChartPos < currentChartPos);
				
				if (direction == 1) {
					check = currentChartPos > pos && 
							(closestChart == null || closestChartPos > currentChartPos);
				}
				
				if (check) {
					closestChart = chart;
				}
			}
		}
		
		return closestChart;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (chart.main.dataDeletionMode) {
			double xMousePos = chart.chartPanel.getChart().getChartXFromCoordinate(e.getX());
			
			// Start value - Last value is the total chart size in chart coordinates
			double chartSizeX = Math.abs(chart.chartPanel.getChart().getChartXFromCoordinate(0) - 
					chart.chartPanel.getChart().getChartXFromCoordinate(chart.chartPanel.getChart().getWidth()));
			
			// Find all data points near the click
			for (int xTypeIndex = 0; xTypeIndex < chart.xTypes.length; xTypeIndex++) {
				DataType currentType = chart.xTypes[xTypeIndex];
				List<DataHandler> dataHandlers = chart.main.allData.get(currentType.tableIndex);
				
				// Y axis depends on the which data is being checked
				double yMousePos = chart.chartPanel.getChart().getChartYFromCoordinate(e.getY(), xTypeIndex);
				double chartSizeY = Math.abs(chart.chartPanel.getChart().getChartYFromCoordinate(0, xTypeIndex) - 
						chart.chartPanel.getChart().getChartYFromCoordinate(chart.chartPanel.getChart().getHeight(), xTypeIndex));
				
				for (DataHandler dataHandler: dataHandlers) {
					// See if click is anywhere near this point
					if (dataHandler != null && Math.abs(dataHandler.data[chart.yType.index].data - xMousePos) < chartSizeX / 100
						&& Math.abs(dataHandler.data[currentType.index].data - yMousePos) < chartSizeY / 100
						&& !dataHandler.hiddenDataTypes.contains(currentType)) {
						
						// Hide this point
						dataHandler.hiddenDataTypes.add(new DataType(currentType.index, currentType.tableIndex));
					}
				}
			}
			
			chart.main.updateUI();
			
			// Don't allow window moving
			return;
		}
		
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
				
				setRelBoundsChecked((int) startBoundsRectangle.getX() + xMovementFactor * (currentX - lastMouseX), 
						(int) startBoundsRectangle.getY() + yMovementFactor * (currentY - lastMouseY), 
						(int) startBoundsRectangle.getWidth() + xChangeFactor * (currentX - lastMouseX), 
						(int) startBoundsRectangle.getHeight() + yChangeFactor * (currentY - lastMouseY), 
						xMovementFactor, yMovementFactor);
			} else {
				// Move panel
				setRelPosition((int) startBoundsRectangle.getX() + (currentX - lastMouseX), 
						(int) startBoundsRectangle.getY() + (currentY - lastMouseY), true);
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
	
	/**
	 * Defaults checked to false.
	 * 
	 * @param absoluteX
	 * @param absoluteY
	 * @param absoluteWidth
	 * @param absoluteHeight
	 */
	public void setRelBounds(int absoluteX, int absoluteY, int absoluteWidth, int absoluteHeight) {
		setRelBounds(absoluteX, absoluteY, absoluteWidth, absoluteHeight, false);
	}
	
	/**
	 * @param absoluteX
	 * @param absoluteY
	 * @param absoluteWidth
	 * @param absoluteHeight
	 * @param checked If true, the window will not go offscreen
	 */
	public void setRelBounds(int absoluteX, int absoluteY, int absoluteWidth, int absoluteHeight, boolean checked) {
		setRelPosition(absoluteX, absoluteY, checked);
		setRelSize(absoluteWidth, absoluteHeight);
	}
	
	public void setRelPosition(int absoluteX, int absoluteY) {
		setRelPosition(absoluteX, absoluteY, false);
	}
	
	/**
	 * Sets the rel position based on an absolute position.
	 * 
	 * @param absoluteX
	 * @param absoluteY
	 * @param checked If true, the window will not go offscreen
	 */
	public void setRelPosition(int absoluteX, int absoluteY, boolean checked) {
		Rectangle screenBounds = panel.getParent().getBounds();
		
		if (checked) {
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
		}
		
		relX = absoluteX / screenBounds.getWidth();
		relY = absoluteY / screenBounds.getHeight();
		
		panel.setLocation(absoluteX, absoluteY);
	}
	
	/**
	 * Sets the rel size based on an absolute size
	 * Returns the adjustments to the mouse position needed if the minimum size has been hit.
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
	 * Sets the size and position of the window checking with the window size.
	 * Will not extend past the window size.
	 * 
	 * @param absoluteX
	 * @param absoluteY
	 * @param absoluteWidth
	 * @param absoluteHeight
	 * @param xMovementFactor 0 if the x movement should not be adjusted
	 * @param yMovementFactor 0 if the y movement should not be adjusted
	 */
	public void setRelBoundsChecked(int absoluteX, int absoluteY, int absoluteWidth, int absoluteHeight, int xMovementFactor, int yMovementFactor) {
		// If a cap is reached, modify the position
		int positionAdjustmentX = 0;
		int positionAdjustmentY = 0;
		
		Rectangle screenBounds = panel.getParent().getBounds();
		
		// Don't get too small
		if (absoluteWidth < 100) {
			positionAdjustmentX = 100 - absoluteWidth;
			
			absoluteWidth = 100;
		}
		if (absoluteHeight < 100) {
			positionAdjustmentY = 100 - absoluteHeight;

			absoluteHeight = 100;
		}
		
		// Don't let it go off screen
		if (absoluteX < 0) {
			absoluteWidth += absoluteX;
		} else if (relX * screenBounds.getWidth() > screenBounds.getWidth() - absoluteWidth) {
			absoluteWidth = (int) (screenBounds.getWidth() - relX * screenBounds.getWidth());
		}
		if (absoluteY < 0) {
			absoluteHeight += absoluteY; 
		} else if (relY * screenBounds.getHeight() > screenBounds.getHeight() - absoluteHeight) {
			absoluteHeight = (int) (screenBounds.getHeight() - relY * screenBounds.getHeight());
		}
		
		relWidth = absoluteWidth / screenBounds.getWidth();
		relHeight = absoluteHeight / screenBounds.getHeight();
		
		panel.setSize(absoluteWidth, absoluteHeight);
		
		setRelPosition(absoluteX - xMovementFactor * positionAdjustmentX, 
				absoluteY - yMovementFactor * positionAdjustmentY, true);
	}
	
	/**
	 * Updates the bounds based on the rel bounds and the screen size given.
	 * 
	 * @param width
	 * @param height
	 */
	public void updateBounds(int width, int height) {
		panel.setBounds((int) (relX * width), (int) (relY * height), (int) (relWidth * width), (int) (relHeight * height));
	}
	
	/**
	 * Called whenever the parent is resized to change the layout to the new size.
	 * 
	 * @param xFactor The factor the x is stretched by (new/old)
	 * @param yFactor The factor the y is stretched by (new/old)
	 */
	public void containerResized(int newWidth, int newHeight) {
		updateBounds(newWidth, newHeight);
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
