package de.ibm.issw.requestmetrics.gui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import de.ibm.issw.requestmetrics.RMNode;
import de.ibm.issw.requestmetrics.RmProcessor;

public class RequestMetricsGui extends JPanel{
	// GUI elements
	private static JSplitPane splitPane = new JSplitPane(0);
	private static JInternalFrame listInternalFrame = new JInternalFrame("Use Cases List", true, false, true, true);
	private static JInternalFrame treeInternalFrame = new JInternalFrame("Selected Use Case Tree View", true, false, true, true);
	
	public Dimension getMinimumSize() {
		return new Dimension(100, 600);
	}

	public Dimension getPreferredSize() {
		return new Dimension(100, 600);
	}
	
	public static void createAndShowGUI(final RmProcessor processor) {
		// populate use case list
		JList<String> jlist = new JList<String>(processor.getUseCases().toArray(new String[0])); 
		jlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// add selection listener to select the use cases
		jlist.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				JList<String> list = (JList<String>) event.getSource();

				String useCase = list.getSelectedValue();
				RMNode rmRecRoot = processor.getUseCaseRootList().get(useCase);
				JPanel jpanel = new UsecasePanel(rmRecRoot, processor);
				RequestMetricsGui.treeInternalFrame.setVisible(false);
				RequestMetricsGui.treeInternalFrame.getContentPane().removeAll();
				RequestMetricsGui.treeInternalFrame.add(jpanel, "Center");
				RequestMetricsGui.treeInternalFrame.setVisible(true);
			}
		});

		JFrame mainFrame = new JFrame("RM Records Log File Analysis Results");
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JScrollPane listScrollPane = new JScrollPane(jlist);
		listInternalFrame.add(listScrollPane, "Center");
		listInternalFrame.setVisible(true);
		
		splitPane.setDividerLocation(200);
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
		mainFrame.setSize(800, 800);
		mainFrame.setVisible(true);
	}
}
