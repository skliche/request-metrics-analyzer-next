package de.ibm.issw.requestmetrics.gui.statistics;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

@SuppressWarnings("serial")
public class ChildNodeStatisticsTableCellRenderer extends DefaultTableCellRenderer {
	public ChildNodeStatisticsTableCellRenderer() {
		super();
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

		TableModel model = table.getModel();
		int modelRow = table.getRowSorter().convertRowIndexToModel(row);	// needed to update colors in first column when sorting table
		
        if(column == 0) {
        	//case: set background of first column to the corresponding chart color
	        Object chartColor = model.getValueAt(modelRow, 0);
	        setBackground((Color) chartColor);
	        setText("");
        } else {
        	setBackground(Color.white);
        	if(isSelected) setBackground(table.getSelectionBackground());
        }

		return this;
	}
}
