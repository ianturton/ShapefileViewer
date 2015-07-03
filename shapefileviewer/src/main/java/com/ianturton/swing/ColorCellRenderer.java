package com.ianturton.swing;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class ColorCellRenderer extends DefaultTableCellRenderer {

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
	    boolean isSelected, boolean hasFocus, int row, int column) {
	Component ret = super.getTableCellRendererComponent(table, value,
		isSelected, hasFocus, row, column);
	if (value instanceof Color) {
	    Color color = (Color) value;
	    ret.setBackground(color);
	    if (ret instanceof JLabel) {
		JLabel jL = (JLabel) ret;
		jL.setOpaque(true);
		jL.setText("");
	    }
	}
	return ret;
    }

    /**
     * Serialisation
     */
    private static final long serialVersionUID = -6795841195282838087L;

}
