package de.ibm.issw.requestmetrics.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

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
	private final JButton highestExecTimeButton = new JButton("Highest Execution Time");
	private final JButton mostDirectChildrenTimeButton = new JButton("Most Direct Children");
	private final JButton calcStatisticsButton = new JButton("Open Statistics");
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("y/MM/dd HH:mm:ss:S");
	
	private RmProcessor processor;
	private static JTable table;
	private ProgressBarDialog fileProcessingDialog;
	private UsecasePanel transactionDrilldownPanel;
	private RMNode currentSelectedRootNode;
	private final JFormattedTextField elapsedTimeFilterField = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JFormattedTextField detailFilterField = new JFormattedTextField();
	private final JCheckBox filterTypeEJB = new JCheckBox("EJB");
	private final JCheckBox filterTypeServletFilter = new JCheckBox("Servlet Filter");
	
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

		JToolBar rootCaseToolBar = new JToolBar();
		rootCaseToolBar.setLayout(new FlowLayout());
		listInternalFrame.add(rootCaseToolBar, "North");
		listInternalFrame.getContentPane().add(listScrollPane, "Center");
		listInternalFrame.setVisible(true);
						
		JToolBar drillDownToolBar = new JToolBar();
		treeInternalFrame.getContentPane().add(drillDownToolBar, "North");
		treeInternalFrame.setVisible(true);

		final JLabel elapsedTimeFilterLabel = new JLabel("Show Elapsed Time > ");
		elapsedTimeFilterField.setColumns(5);
		
		final JLabel detailFilterLabel = new JLabel("Search in Details: ");
		detailFilterField.setColumns(7);
		
		
		rootCaseToolBar.setPreferredSize(new Dimension(listInternalFrame.getWidth(), 35));
		rootCaseToolBar.setFloatable(false);
		rootCaseToolBar.add(filterTypeEJB);
		rootCaseToolBar.add(filterTypeServletFilter);
		rootCaseToolBar.add(elapsedTimeFilterLabel);
		rootCaseToolBar.add(elapsedTimeFilterField);
		rootCaseToolBar.add(detailFilterLabel);
		rootCaseToolBar.add(detailFilterField);
		
		drillDownToolBar.setPreferredSize(new Dimension(treeInternalFrame.getWidth(), 35));
		drillDownToolBar.setFloatable(false);
		highestExecTimeButton.setToolTipText("Jump to the subtransaction with the highest execution time of all subtransactions.");
		highestExecTimeButton.setEnabled(false);
		highestExecTimeButton.addActionListener(selectHighestExecTimeNode());
		mostDirectChildrenTimeButton.setToolTipText("Jump to the subtransaction with the highest number of direct children.");
		mostDirectChildrenTimeButton.setEnabled(false);
		mostDirectChildrenTimeButton.addActionListener(selectMostDirectChildrenNode());
		calcStatisticsButton.setToolTipText("Calculate statistics for selected node and open the result in a new dialog window.");
		calcStatisticsButton.setEnabled(false);
		calcStatisticsButton.addActionListener(openStatistics());
		drillDownToolBar.add(highestExecTimeButton);
		drillDownToolBar.add(mostDirectChildrenTimeButton);
		drillDownToolBar.add(calcStatisticsButton);
		
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
							
							//TODO: currently, only one filter can be applied
							
							ItemListener checkBoxListener = new ItemListener(){

								@Override
								public void itemStateChanged(ItemEvent e) {
									Object source = e.getItemSelectable();
									TableRowSorter<UsecaseTableModel> sorter = new TableRowSorter<UsecaseTableModel>(rootCaseModel);
									table.setRowSorter(sorter);
									
									if (e.getStateChange() == ItemEvent.SELECTED) {
										if (source == filterTypeEJB) {
											sorter.setRowFilter(RowFilter.regexFilter("EJB"));
										} else if (source == filterTypeServletFilter) {
											sorter.setRowFilter(RowFilter.regexFilter("Servlet Filter"));
										}
										
									} else if (e.getStateChange() == ItemEvent.DESELECTED) {
										table.setRowSorter(null);
									}
								}
								
							};
							filterTypeEJB.addItemListener(checkBoxListener);
							filterTypeServletFilter.addItemListener(checkBoxListener);
							
							
							elapsedTimeFilterField.addKeyListener(new KeyAdapter() {
								public void keyReleased(KeyEvent evt) {
									if (evt != null) {
										try {
											elapsedTimeFilterField.commitEdit();
										} catch (ParseException e) {
											e.printStackTrace();
										}
										//TODO: if the field contains no value (e.g. when it is deleted), userInput should be treated as 0, so no entry is filtered
										final Long userInput = (Long) elapsedTimeFilterField.getValue();
										
										RowFilter<UsecaseTableModel, Integer> filterElapsedTime = new RowFilter<UsecaseTableModel, Integer>(){
											
											@Override
											public boolean include(javax.swing.RowFilter.Entry<? extends UsecaseTableModel, ? extends Integer> entry) {
												Long elapsedTime = (Long) rootCaseModel.getValueAt(entry.getIdentifier(), 2);
												if (elapsedTime >= userInput) {
													return true;
												} else {
													return false;
												}
											}
										};
										TableRowSorter<UsecaseTableModel> sorter = new TableRowSorter<UsecaseTableModel>(rootCaseModel);
										table.setRowSorter(sorter);
										sorter.setRowFilter(filterElapsedTime);
									}
								}
							});
							
							detailFilterField.addKeyListener(new KeyAdapter() {
								public void keyReleased(KeyEvent evt) {
									if (evt != null) {
										try {
											detailFilterField.commitEdit();
										} catch (ParseException e) {
											e.printStackTrace();
										}
										TableRowSorter<UsecaseTableModel> sorter = new TableRowSorter<UsecaseTableModel>(rootCaseModel);
										table.setRowSorter(sorter);
										sorter.setRowFilter(RowFilter.regexFilter(detailFilterField.getText()));
									}
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
						
						highestExecTimeButton.setEnabled(true);
						mostDirectChildrenTimeButton.setEnabled(true);
						
						treeInternalFrame.setVisible(true);
						repaintGui();
					}
				}
			} 
		});
		
		return businessTransactionTable;
	}
	
	private ActionListener selectHighestExecTimeNode() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentSelectedRootNode != null)
					transactionDrilldownPanel.selectTreeNode(transactionDrilldownPanel.getHighestExecTimeNode());
			}
		};
	}

	private ActionListener selectMostDirectChildrenNode() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentSelectedRootNode != null)
					transactionDrilldownPanel.selectTreeNode(transactionDrilldownPanel.getMostDirectChildrenNode());
			}
		};
	}

	private ActionListener openStatistics() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (transactionDrilldownPanel != null && transactionDrilldownPanel.getSelectedTreeNode() != null)
					transactionDrilldownPanel.calculateAndOpenStatisticsDialog(transactionDrilldownPanel.getSelectedTreeNode());;
			}
		};
	}
	
	private void resetGui() {
		treeInternalFrame.setVisible(false);
		treeInternalFrame.setTitle("Transaction Drilldown");
		if(transactionDrilldownPanel != null) treeInternalFrame.getContentPane().remove(transactionDrilldownPanel);
		treeInternalFrame.setVisible(true);
		highestExecTimeButton.setEnabled(false);
		mostDirectChildrenTimeButton.setEnabled(false);
		calcStatisticsButton.setEnabled(false);
	}
	
	private void repaintGui() {
		treeInternalFrame.repaint();
		listInternalFrame.repaint();
		if(transactionDrilldownPanel != null) transactionDrilldownPanel.repaint();
	}
	
	public JButton getCalcStatisticsButton() {
		return calcStatisticsButton;
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
