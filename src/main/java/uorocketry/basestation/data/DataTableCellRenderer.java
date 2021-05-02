package uorocketry.basestation.data;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.UIDefaults;
import javax.swing.table.DefaultTableCellRenderer;

public class DataTableCellRenderer extends DefaultTableCellRenderer  {
	private static final long serialVersionUID = -4019697511593716355L;

	public int coloredRow = -1;
	public Color color = new Color(0x80ffa2);
	public Color selectedBackgroundColor;
	public Color selectedForgroundColor;
	
	public DataTableCellRenderer() {
		super();
		
		UIDefaults defaults = javax.swing.UIManager.getDefaults();
		selectedBackgroundColor = defaults.getColor("List.selectionBackground");
		selectedForgroundColor = defaults.getColor("List.selectionForeground");
	}
	
	@Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        if (row == coloredRow) {
            c.setBackground(color);
            c.setForeground(Color.black);
        } else if (isSelected || table.isCellSelected(row, 0) || table.isCellSelected(row, 1)) {
            c.setBackground(selectedBackgroundColor);
            c.setForeground(selectedForgroundColor);
        } else {
            c.setBackground(Color.white);
            c.setForeground(Color.black);
        }

        return c;
    } 
}
