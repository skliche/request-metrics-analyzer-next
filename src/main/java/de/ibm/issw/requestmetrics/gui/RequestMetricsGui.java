package de.ibm.issw.requestmetrics.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import de.ibm.issw.requestmetrics.engine.RmProcessor;
import de.ibm.issw.requestmetrics.model.RMNode;
import de.ibm.issw.requestmetrics.model.RmRootCase;

@SuppressWarnings("serial")
public class RequestMetricsGui extends JPanel{
	private static final Logger LOG = Logger.getLogger(RequestMetricsGui.class.getName());
	// GUI elements
	private static final JInternalFrame treeInternalFrame = new JInternalFrame("Selected Use Case Tree View", true, false, true, true);
	private static final SimpleDateFormat sdf = new SimpleDateFormat("y/MM/dd HH:mm:ss:S");
	
	private static RmProcessor processor;
	private static JTable table;
	
	public Dimension getMinimumSize() {
		return new Dimension(100, 800);
	}

	public Dimension getPreferredSize() {
		return new Dimension(100, 800);
	}
	
	public static void createAndShowGUI(final RmProcessor processor) {
		RequestMetricsGui.processor = processor;
		
		table = buildRootCaseTable();
		JScrollPane listScrollPane = new JScrollPane(table);

		

		JInternalFrame listInternalFrame = new JInternalFrame("Use Cases List", true, false, true, true);
		listInternalFrame.add(listScrollPane, "Center");
		listInternalFrame.setVisible(true);
		
		JPanel jpanel = new JPanel();
		treeInternalFrame.add(jpanel, "Center");
		treeInternalFrame.setVisible(true);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(250);
		splitPane.setLeftComponent(listInternalFrame);
		splitPane.setRightComponent(treeInternalFrame);
		
		JFrame mainFrame = new JFrame("RM Records Log File Analysis Results");
		final JMenuBar menuBar = buildMenubar(mainFrame);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setSize(900, 800);

		mainFrame.add(menuBar, BorderLayout.NORTH);
		mainFrame.add(splitPane, BorderLayout.CENTER);

		mainFrame.setVisible(true);
	}

	private static JMenuBar buildMenubar(JFrame mainFrame) {
		JMenuBar menu = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		final FileDialog fd = new FileDialog(mainFrame, "Load Scenario File", FileDialog.LOAD);
		
		JMenuItem fileLoadScenarioItem = new JMenuItem("Load Scenario");
		fileLoadScenarioItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fd.setVisible(true);
				processor.reset();
				try {
					processor.processInputFile(fd.getDirectory() + fd.getFile());
				} catch (Exception e1) {
					return;
				} 
				// remove the old model
				table.setModel(new UsecaseTableModel(processor.getRootCases()));
				
				// the width is currently hard coded and could be gathered from data in future
				table.getColumnModel().getColumn(0).setMinWidth(215); 
				table.getColumnModel().getColumn(0).setMaxWidth(515); 
				table.getColumnModel().getColumn(1).setMaxWidth(85); 
				table.getColumnModel().getColumn(2).setMaxWidth(85); 
				table.getColumnModel().getColumn(3).setMaxWidth(85); 
				
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
		
				
			}
		});
		
		fileMenu.add(fileLoadScenarioItem);
		menu.add(fileMenu);
		return menu;
	}

	private static JTable buildRootCaseTable() {
		final JTable table = new JTable();
		table.setFillsViewportHeight(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(true);
		
		// add selection listener to select the use cases
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				
				// check if we are in an event sequence and only process the last one
				if(!event.getValueIsAdjusting() && !table.getSelectionModel().isSelectionEmpty()) {
					int row = table.getSelectedRow();
					if(row != -1) { //if no row is selected row = -1 (and we do nothing)
						RmRootCase useCase = processor.getRootCases().get(table.convertRowIndexToModel(row));
						
						LOG.info("user selected use case " + useCase.getRmNode().toString());
						
						RMNode rmRecRoot = useCase.getRmNode();
						JPanel jpanel = new UsecasePanel(rmRecRoot, processor);
						treeInternalFrame.setVisible(false);
						treeInternalFrame.getContentPane().removeAll();
						treeInternalFrame.add(jpanel, "Center");
						treeInternalFrame.setVisible(true);
					}
				}
			} 
		});
		
		return table;
	}
}
