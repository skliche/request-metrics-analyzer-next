package de.ibm.issw.requestmetrics.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import de.ibm.issw.requestmetrics.engine.RmProcessor;
import de.ibm.issw.requestmetrics.engine.events.LogParsingTypeEvent;
import de.ibm.issw.requestmetrics.engine.events.NonUniqueRequestIdEvent;
import de.ibm.issw.requestmetrics.engine.events.ParsingAllFilesHasFinishedEvent;
import de.ibm.issw.requestmetrics.engine.events.ParsingFileHasFinishedEvent;
import de.ibm.issw.requestmetrics.engine.events.PercentageIncreasedEvent;
import de.ibm.issw.requestmetrics.engine.events.UnsupportedFileEvent;
import de.ibm.issw.requestmetrics.gui.comparator.ElapsedTimeComparator;
import de.ibm.issw.requestmetrics.model.RMNode;
import de.ibm.issw.requestmetrics.model.RmRootCase;

@SuppressWarnings("serial")
public class RequestMetricsGui extends JDialog implements Observer {
	public RequestMetricsGui() {
		
		}
	private static final Logger LOG = Logger.getLogger(RequestMetricsGui.class.getName());
	private List<NonUniqueRequestIdEvent> nonUniqueReqIds = new ArrayList<NonUniqueRequestIdEvent>();
	private StringBuffer invalidFiles = new StringBuffer();
	// GUI elements
	private static final JInternalFrame treeInternalFrame = new JInternalFrame("Transaction Drilldown", true, false, true, true);
	private static final JInternalFrame listInternalFrame = new JInternalFrame("Business Transactions", true, false, true, true);
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("y/MM/dd HH:mm:ss:S");
	
	private RmProcessor processor;
	private static JTable table;
	private ProgressBarDialog fileProcessingDialog;
	private UsecasePanel transactionDrilldownPanel;
	private RMNode currentSelectedRootNode;
	private TransactionDrilldownToolBar transactionDrilldownToolBar = new TransactionDrilldownToolBar();
	private RootCaseToolBar rootCaseToolBar = new RootCaseToolBar();
	
	public Dimension getMinimumSize() {
		return new Dimension(100, 800);
	}

	public Dimension getPreferredSize() {
		return new Dimension(100, 800);
	}
	
	public void createAndShowGUI(final RmProcessor processor) {
		this.processor = processor;
		
		// register the GUI as observer for the events of the processor
		processor.addObserver(this);
		
		table = buildRootCaseTable();
		JScrollPane listScrollPane = new JScrollPane(table);

		listInternalFrame.add(rootCaseToolBar, "North");
		listInternalFrame.getContentPane().add(listScrollPane, "Center");
		listInternalFrame.setVisible(true);
						
		treeInternalFrame.getContentPane().add(transactionDrilldownToolBar, "North");
		treeInternalFrame.setVisible(true);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setDividerLocation(250);
		splitPane.setLeftComponent(listInternalFrame);
		splitPane.setRightComponent(treeInternalFrame);
		
		JFrame mainFrame = new JFrame("RM Records Log File Analysis Results");
		final JMenuBar menuBar = buildMenubar(mainFrame, processor);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		mainFrame.getContentPane().add(menuBar, BorderLayout.NORTH);
		mainFrame.getContentPane().add(splitPane, BorderLayout.CENTER);

		mainFrame.setVisible(true);
	}


	private JMenuBar buildMenubar(JFrame mainFrame, final RmProcessor processor) {
		JMenuBar menu = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		final FileDialog fd = new FileDialog(mainFrame, "Load Scenario File", FileDialog.LOAD);
		fd.setMultipleMode(true);
		
		JMenuItem fileLoadScenarioItem = new JMenuItem("Load Scenario");
		fileLoadScenarioItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fd.setVisible(true);
				processor.reset();
				invalidFiles = new StringBuffer();
				final File[] files = fd.getFiles();
				if(files.length == 0) return;
				
				resetGui();

				//create a new dialog containing 2 progress bars
				fileProcessingDialog = new ProgressBarDialog();

				new Thread(new Runnable() {
					public void run() {
						processor.processInputFiles(files);
						listInternalFrame.setTitle(processor.getRootCases().size() + " Business Transactions");
						rootCaseToolBar.setFiltersEnabled(true);
						
						// remove the old model
						List<RmRootCase> rootCases = processor.getRootCases();
						if(rootCases != null && !rootCases.isEmpty()) {
							final UsecaseTableModel rootCaseModel = new UsecaseTableModel(rootCases);
							table.setModel(rootCaseModel);
							// the width is currently hard coded and could be gathered from data in future
							table.getColumnModel().getColumn(0).setMinWidth(215); 
							table.getColumnModel().getColumn(0).setMaxWidth(515); 
							table.getColumnModel().getColumn(1).setMinWidth(160); 
							table.getColumnModel().getColumn(1).setMaxWidth(160); 
							table.getColumnModel().getColumn(2).setMinWidth(100); 
							table.getColumnModel().getColumn(2).setMaxWidth(100); 
							table.getColumnModel().getColumn(3).setMinWidth(140); 
							table.getColumnModel().getColumn(3).setMaxWidth(140); 
							table.getColumnModel().getColumn(4).setMinWidth(85); 
							table.getColumnModel().getColumn(4).setMaxWidth(85); 
							
							// initially sort root cases by elapsed time descending
							Collections.sort(rootCases, new ElapsedTimeComparator());
							Collections.reverse(rootCases);
							
							table.setRowSorter(null);
							
							//TODO: currently, only one filter can be applied
							//Solution: use a list of rowFilters and set them as rowsorter for the table
							//Problem: can't be defined in an enclosing scope (inner method)
							List<RowFilter<UsecaseTableModel, Object>> filters = new ArrayList<RowFilter<UsecaseTableModel, Object>>();
							RowFilter<UsecaseTableModel, Object> compoundFilter = null;
							RowFilter<UsecaseTableModel, Object> eJBFilter;
							//filters.add(eJBFilter);
							
							
							// we write our own cell renderer for rendering the date values
							TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
								public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
									if( value instanceof Date) {
										value = sdf.format(value);
									}
									return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
								}
							};
							table.getColumnModel().getColumn(1).setCellRenderer(tableCellRenderer);
						}
					}
				}).start();

			}
		});
		
		fileMenu.add(fileLoadScenarioItem);
		menu.add(fileMenu);
		return menu;
	}

	private JTable buildRootCaseTable() {
		final JTable businessTransactionTable = new JTable();
		businessTransactionTable.setFillsViewportHeight(true);
		businessTransactionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		businessTransactionTable.setAutoCreateRowSorter(true);
		
		// reference to this window 
		final RequestMetricsGui rootWindow = this;
		
		// add selection listener to select the use cases
		businessTransactionTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				// check if we are in an event sequence and only process the last one
				if(!event.getValueIsAdjusting() && !businessTransactionTable.getSelectionModel().isSelectionEmpty()) {
					int row = businessTransactionTable.getSelectedRow();
					if(row != -1) { //if no row is selected row = -1 (and we do nothing)
						final RmRootCase currentSelectedRootCase = processor.getRootCases().get(businessTransactionTable.convertRowIndexToModel(row));
						LOG.fine("user selected use case " + currentSelectedRootCase.getRmNode().toString());
						
						currentSelectedRootNode = currentSelectedRootCase.getRmNode();
						currentSelectedRootNode.calculateExecutionTime();
						resetGui();
						transactionDrilldownPanel = new UsecasePanel(rootWindow, currentSelectedRootNode, processor);
						treeInternalFrame.getContentPane().add(transactionDrilldownPanel, "Center");
						treeInternalFrame.setTitle("Transaction Drilldown for #" + currentSelectedRootCase.getRmNode().getData().getCurrentCmp().getReqid() + " " + currentSelectedRootCase.getRmNode().getData().getDetailCmp());
						
						transactionDrilldownToolBar.setSelectionButtonsEnabled(true);
						
						treeInternalFrame.setVisible(true);
						repaintGui();
					}
				}
			} 
		});
		
		return businessTransactionTable;
	}
	
	private void resetGui() {
		treeInternalFrame.setVisible(false);
		treeInternalFrame.setTitle("Transaction Drilldown");
		if(transactionDrilldownPanel != null) treeInternalFrame.getContentPane().remove(transactionDrilldownPanel);
		treeInternalFrame.setVisible(true);
		rootCaseToolBar.setFiltersEnabled(false);
		transactionDrilldownToolBar.setSelectionButtonsEnabled(false);
		transactionDrilldownToolBar.setStatisticsButtonEnabled(false);
	}
	
	private void repaintGui() {
		treeInternalFrame.repaint();
		listInternalFrame.repaint();
		if(transactionDrilldownPanel != null) transactionDrilldownPanel.repaint();
	}
	
	public static JTable getTable() {
		return table;
	}

	public static void setTable(JTable table) {
		RequestMetricsGui.table = table;
	}
	
	public TransactionDrilldownToolBar getTransactionDrilldownToolBar() {
		return transactionDrilldownToolBar;
	}

	@Override
	public void update(Observable o, Object event) {
		if(event instanceof ParsingFileHasFinishedEvent) {
			//create a log entry if parsing of a file has finished
			ParsingFileHasFinishedEvent concreteEvent = (ParsingFileHasFinishedEvent) event;
			LOG.info("parsing of file " + concreteEvent.getFileName() + " has finished.");
		} 
		else if (event instanceof PercentageIncreasedEvent) {
			//update progress bar
			PercentageIncreasedEvent concreteEvent = (PercentageIncreasedEvent) event;
			fileProcessingDialog.update(concreteEvent);
		} 
		else if(event instanceof ParsingAllFilesHasFinishedEvent) {
			/*notify user when parsing of all Files has finished, show which files could not be parsed, reset
			 *the internal window frames and dispose the progress bar dialog
			 */
			LOG.info("parsing of all files has finished.");
			if (invalidFiles.length() > 0)
				JOptionPane.showMessageDialog(null, "The following files are invalid and could not be parsed:" + invalidFiles);
			fileProcessingDialog.dispose();
			repaintGui();
		} 
		else if (event instanceof UnsupportedFileEvent) {
			//notify user if any of the loaded files could not be processed
			UnsupportedFileEvent concreteEvent = (UnsupportedFileEvent) event;
			invalidFiles.append("\n - " + concreteEvent.getFileName());
		} 
		else if (event instanceof LogParsingTypeEvent) {
			//create a log info about the type of a loaded file
			LogParsingTypeEvent concreteEvent = (LogParsingTypeEvent) event;
			String typeInfo = String.format("The loaded file '%s' was of type '%s'\n", concreteEvent.getFileName(),concreteEvent.getType());
			LOG.info(typeInfo);
		} 
		else if (event instanceof NonUniqueRequestIdEvent) {
			//TODO: notify User that there are NonUnique Req IDs (if performance is okay), but do not display which ones
			NonUniqueRequestIdEvent concreteEvent = (NonUniqueRequestIdEvent) event;
			nonUniqueReqIds.add(concreteEvent);
			LOG.info("There are" + nonUniqueReqIds.size() + "non unique request IDs");
		} 
	}
}
