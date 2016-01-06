package de.ibm.issw.requestmetrics.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
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

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ibm.issw.requestmetrics.engine.RmProcessor;
import de.ibm.issw.requestmetrics.engine.events.LogParsingTypeEvent;
import de.ibm.issw.requestmetrics.engine.events.NonUniqueRequestIdEvent;
import de.ibm.issw.requestmetrics.engine.events.ParsingAllFilesHasFinishedEvent;
import de.ibm.issw.requestmetrics.engine.events.PercentageIncreasedEvent;
import de.ibm.issw.requestmetrics.engine.events.UnsupportedFileEvent;
import de.ibm.issw.requestmetrics.gui.comparator.ElapsedTimeComparator;
import de.ibm.issw.requestmetrics.model.RMNode;
import de.ibm.issw.requestmetrics.model.RmRootCase;
import de.ibm.issw.requestmetrics.util.FileTypeFilter;

@SuppressWarnings("serial")
public class RequestMetricsGui implements Observer {
	// Logging and utilities
	public static final Logger LOG = LoggerFactory.getLogger(RequestMetricsGui.class);
	private static final SimpleDateFormat sdf = new SimpleDateFormat("y/MM/dd HH:mm:ss.S");
	
	// Variables holding business state
	private RmProcessor processor;
	private List<NonUniqueRequestIdEvent> nonUniqueReqIds = new ArrayList<NonUniqueRequestIdEvent>();
	private StringBuffer invalidFiles = new StringBuffer();
	private RMNode currentSelectedRootNode;

	// GUI elements
	private final JPanel transactionDrilldownScrollFrame = new JPanel(new BorderLayout());
	private final JPanel rootCaseScrollFrame = new JPanel(new BorderLayout());
	
	private RootCaseToolBar rootCaseToolBar = new RootCaseToolBar(this);
	private JTable rootCaseTable;
	private ProgressBarDialog fileProcessingDialog;
	private TransactionDrilldownPanel transactionDrilldownPanel;
	private TransactionDrilldownToolBar transactionDrilldownToolBar = new TransactionDrilldownToolBar();
	private JFrame mainFrame = new JFrame("RM Records Log File Analysis Results");
	
	public Dimension getMinimumSize() {
		return new Dimension(100, 800);
	}

	public Dimension getPreferredSize() {
		return new Dimension(100, 800);
	}
	
	public JFrame getMainFrame() {
		return mainFrame;
	}
	
	public TransactionDrilldownToolBar getTransactionDrilldownToolBar() {
		return transactionDrilldownToolBar;
	}
	
	public void createAndShowGUI(final RmProcessor processor) {
		this.processor = processor;
		
		// register the GUI as observer for the events of the processor
		processor.addObserver(this);
		
		buildRootCaseTable();
		rootCaseScrollFrame.add(rootCaseToolBar, BorderLayout.NORTH);
		rootCaseScrollFrame.add(new JScrollPane(rootCaseTable), BorderLayout.CENTER);
		setTitleRootCaseFrame("Transactions");
		
		setTitleTransactionDrilldownFrame("Transaction Drilldown");
		transactionDrilldownScrollFrame.add(transactionDrilldownToolBar, BorderLayout.NORTH);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rootCaseScrollFrame, transactionDrilldownScrollFrame);
		splitPane.setResizeWeight(0.3);
		
		final JMenuBar menuBar = buildMenubar(mainFrame, processor);
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);

		mainFrame.getContentPane().add(menuBar, BorderLayout.NORTH);
		mainFrame.getContentPane().add(splitPane, BorderLayout.CENTER);

		mainFrame.setVisible(true);
	}


	private JMenuBar buildMenubar(final JFrame mainFrame, final RmProcessor processor) {
		JMenuBar menu = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		final JFileChooser fc = new JFileChooser();
		FileFilter logFilter = new FileTypeFilter(".log", "Log Files");
		FileFilter zipFilter = new FileTypeFilter(".zip", "ZIP Files");
		fc.addChoosableFileFilter(logFilter);
		fc.addChoosableFileFilter(zipFilter);
		fc.setMultiSelectionEnabled(true);
		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		
		JMenuItem fileLoadScenarioItem = new JMenuItem("Load Scenario");
		fileLoadScenarioItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int action = fc.showOpenDialog(mainFrame);
				
				final File[] files = fc.getSelectedFiles();
				if(action != JFileChooser.APPROVE_OPTION || files.length == 0) return;
				
				processor.reset();
				invalidFiles = new StringBuffer();
				resetGui();

				//create a new dialog containing 2 progress bars
				fileProcessingDialog = new ProgressBarDialog();

				new Thread(new Runnable() {
					public void run() {
						processor.processInputFiles(files);
						setTitleRootCaseFrame(processor.getRootCases().size() + " Transactions");
						
						// remove the old model
						List<RmRootCase> rootCases = processor.getRootCases();
						if(rootCases != null && !rootCases.isEmpty()) {
							final RootCaseTableModel rootCaseModel = new RootCaseTableModel(rootCases);
							rootCaseTable.setModel(rootCaseModel);
							// the width is currently hard coded and could be gathered from data in future
							rootCaseTable.getColumnModel().getColumn(RootCaseTableModel.FILE_COLUMN_INDEX).setMinWidth(215); 
							rootCaseTable.getColumnModel().getColumn(RootCaseTableModel.FILE_COLUMN_INDEX).setMaxWidth(515); 
							rootCaseTable.getColumnModel().getColumn(RootCaseTableModel.TIMESTAMP_COLUMN_INDEX).setMinWidth(170); 
							rootCaseTable.getColumnModel().getColumn(RootCaseTableModel.TIMESTAMP_COLUMN_INDEX).setMaxWidth(200); 
							rootCaseTable.getColumnModel().getColumn(RootCaseTableModel.ELAPSEDTIME_COLUMN_INDEX).setMinWidth(100); 
							rootCaseTable.getColumnModel().getColumn(RootCaseTableModel.ELAPSEDTIME_COLUMN_INDEX).setMaxWidth(100); 
							rootCaseTable.getColumnModel().getColumn(RootCaseTableModel.TYPE_COLUMN_INDEX).setMinWidth(140); 
							rootCaseTable.getColumnModel().getColumn(RootCaseTableModel.TYPE_COLUMN_INDEX).setMaxWidth(140); 
							rootCaseTable.getColumnModel().getColumn(RootCaseTableModel.REQUESTID_COLUMN_INDEX).setMinWidth(85); 
							rootCaseTable.getColumnModel().getColumn(RootCaseTableModel.REQUESTID_COLUMN_INDEX).setMaxWidth(85); 
							
							// we write our own cell renderer for rendering the date values
							TableCellRenderer tableCellRenderer = new DefaultTableCellRenderer() {
								public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
									if( value instanceof Date) {
										value = sdf.format(value);
									}
									return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
								}
							};
							rootCaseTable.getColumnModel().getColumn(1).setCellRenderer(tableCellRenderer);
							rootCaseToolBar.enableFilters(rootCaseTable, processor.getRootCaseTypes());

							// initially sort root cases by elapsed time descending
							Collections.sort(rootCases, new ElapsedTimeComparator());
							Collections.reverse(rootCases);
						}
					}
				}).start();

			}
		});
		
		fileMenu.add(fileLoadScenarioItem);
		menu.add(fileMenu);
		return menu;
	}
	
	public void setTitleRootCaseFrame(String title) {
		TitledBorder border = BorderFactory.createTitledBorder(title);
		border.setTitleJustification(TitledBorder.CENTER);
		rootCaseScrollFrame.setBorder(border);
	}
	
	public void setTitleTransactionDrilldownFrame(String title) {
		TitledBorder border = BorderFactory.createTitledBorder(title);
		border.setTitleJustification(TitledBorder.CENTER);
		transactionDrilldownScrollFrame.setBorder(border);
	}

	private void buildRootCaseTable() {
		rootCaseTable = new JTable();
		rootCaseTable.setFillsViewportHeight(true);
		rootCaseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rootCaseTable.setAutoCreateRowSorter(true);
		
		// reference to this window 
		final RequestMetricsGui rootWindow = this;
		
		// add selection listener to select the use cases
		rootCaseTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				// check if we are in an event sequence and only process the last one
				if(!event.getValueIsAdjusting() && !rootCaseTable.getSelectionModel().isSelectionEmpty()) {
					int row = rootCaseTable.getSelectedRow();
					if(row != -1) { //if no row is selected row = -1 (and we do nothing)
						final RmRootCase currentSelectedRootCase = processor.getRootCases().get(rootCaseTable.convertRowIndexToModel(row));
						LOG.debug("user selected use case " + currentSelectedRootCase.getRmNode().toString());
						
						currentSelectedRootNode = currentSelectedRootCase.getRmNode();
						currentSelectedRootNode.calculateExecutionTime();
						resetGui();
						transactionDrilldownPanel = new TransactionDrilldownPanel(rootWindow, currentSelectedRootNode, processor);
						transactionDrilldownScrollFrame.add(transactionDrilldownPanel, "Center");
						setTitleTransactionDrilldownFrame("Transaction Drilldown for #" + currentSelectedRootCase.getRmNode().getData().getCurrentCmp().getReqid() + " " + currentSelectedRootCase.getRmNode().getData().getDetailCmp());
						
						transactionDrilldownToolBar.enableSelectionButtons(transactionDrilldownPanel);
						
						transactionDrilldownScrollFrame.setVisible(true);
						repaintGui();
					}
				}
			} 
		});
	}
	
	private void resetGui() {
		transactionDrilldownScrollFrame.setVisible(false);
		setTitleTransactionDrilldownFrame("Transaction Drilldown");
		
		if(transactionDrilldownPanel != null) transactionDrilldownScrollFrame.remove(transactionDrilldownPanel);
		transactionDrilldownScrollFrame.setVisible(true);
		transactionDrilldownToolBar.disableSelectionButtons();
		transactionDrilldownToolBar.disableStatisticsButton();
	}
	
	private void repaintGui() {
		transactionDrilldownScrollFrame.repaint();
		rootCaseScrollFrame.repaint();
		if(transactionDrilldownPanel != null) transactionDrilldownPanel.repaint();
	}
	
	@Override
	public void update(Observable o, Object event) {
		if (event instanceof PercentageIncreasedEvent) {
			//update progress bar
			PercentageIncreasedEvent concreteEvent = (PercentageIncreasedEvent) event;
			fileProcessingDialog.update(concreteEvent);
		} 
		else if(event instanceof ParsingAllFilesHasFinishedEvent) {
			LOG.debug("Parsing of all files finished. The following were processed: " + ((ParsingAllFilesHasFinishedEvent)event).getFiles().toString());
			/*notify user when parsing of all Files has finished, show which files could not be parsed, reset
			 *the internal window frames and dispose the progress bar dialog
			 */
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
			String typeInfo = String.format("The loaded file '%s' was of type '%s'", concreteEvent.getFileName(),concreteEvent.getType());
			LOG.info(typeInfo);
		} 
		else if (event instanceof NonUniqueRequestIdEvent) {
			//TODO: notify User that there are NonUnique Req IDs (if performance is okay), but do not display which ones
			NonUniqueRequestIdEvent concreteEvent = (NonUniqueRequestIdEvent) event;
			nonUniqueReqIds.add(concreteEvent);
			LOG.info("There are" + nonUniqueReqIds.size() + "non unique request IDs");
		} else {
			LOG.info("unhandled event of type " + event.getClass());
		}
	}
}
