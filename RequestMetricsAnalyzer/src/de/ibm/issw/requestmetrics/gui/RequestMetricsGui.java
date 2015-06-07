package de.ibm.issw.requestmetrics.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import de.ibm.issw.requestmetrics.RMNode;
import de.ibm.issw.requestmetrics.RmProcessor;
import de.ibm.issw.requestmetrics.RmRootCase;

@SuppressWarnings("serial")
public class RequestMetricsGui extends JPanel{
	// GUI elements
	private static final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private static final JInternalFrame listInternalFrame = new JInternalFrame("Use Cases List", true, false, true, true);
	private static final JInternalFrame treeInternalFrame = new JInternalFrame("Selected Use Case Tree View", true, false, true, true);
	private static final SimpleDateFormat sdf = new SimpleDateFormat("y/MM/dd HH:mm:ss:S");
	
	public Dimension getMinimumSize() {
		return new Dimension(100, 800);
	}

	public Dimension getPreferredSize() {
		return new Dimension(100, 800);
	}
	
	public static void createAndShowGUI(final RmProcessor processor) {
		final JTable table = new JTable(new UsecaseTableModel(processor.getUseCases()));
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(true);
		// the width is currently hard coded and could be gathered from data in future
		table.getColumnModel().getColumn(0).setMinWidth(215); 
		table.getColumnModel().getColumn(0).setMaxWidth(515); 
		table.getColumnModel().getColumn(1).setMaxWidth(85); 
		table.getColumnModel().getColumn(2).setMaxWidth(85); 
		table.getColumnModel().getColumn(3).setMaxWidth(85); 
		
		// add selection listener to select the use cases
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				RmRootCase useCase = processor.getUseCases().get(table.convertRowIndexToModel(table.getSelectedRow()));
				RMNode rmRecRoot = processor.getUseCaseRootList().get(useCase.getRmNode().getRmRecId());
				JPanel jpanel = new UsecasePanel(rmRecRoot, processor);
				RequestMetricsGui.treeInternalFrame.setVisible(false);
				RequestMetricsGui.treeInternalFrame.getContentPane().removeAll();
				RequestMetricsGui.treeInternalFrame.add(jpanel, "Center");
				RequestMetricsGui.treeInternalFrame.setVisible(true);
			} 
		});
		
		// we write our own cell renderer for rendering the date values
		TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
		    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		        if( value instanceof Date) {
		            value = sdf.format(value);
		        }
		        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		    }
		};
		table.getColumnModel().getColumn(0).setCellRenderer(tableCellRenderer);

		JFrame mainFrame = new JFrame("RM Records Log File Analysis Results");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JScrollPane listScrollPane = new JScrollPane(table);
		listInternalFrame.add(listScrollPane, "Center");
		listInternalFrame.setVisible(true);
		
		splitPane.setDividerLocation(250);
		splitPane.setLeftComponent(listInternalFrame);
		
		JPanel jpanel = new JPanel();
		treeInternalFrame.add(jpanel, "Center");
		treeInternalFrame.setVisible(true);
		splitPane.setRightComponent(treeInternalFrame);
		
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		mainFrame.add(splitPane, "Center");
		mainFrame.setSize(900, 800);
		mainFrame.setVisible(true);
	}
}
