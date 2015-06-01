

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

import de.ibm.issw.requestmetrics.RMNode;
import de.ibm.issw.requestmetrics.RMRecord;
import de.ibm.issw.requestmetrics.RmProcessor;

@SuppressWarnings("serial")
public class Starter extends JPanel {
	private static boolean debug = false;

	private static Hashtable<String, JPanel> jTreesHashtable = new Hashtable<String, JPanel>();
	private static String selectedUseCase = "";

	// GUI elements
	private static JSplitPane splitPane = new JSplitPane(0);
	private static JInternalFrame listInternalFrame = new JInternalFrame("Use Cases List", true, false, true, true);
	private static JInternalFrame treeInternalFrame = new JInternalFrame("Selected Use Case Tree View", true, false, true, true);

	private static RmProcessor processor = new RmProcessor();
	
	private void getRecursive(SortableNode node, RMNode rmNode) {
		String rmRecId = rmNode.getData().getRmRecId();
		if (processor.getParentNodesMap().containsKey(rmRecId)) {
			List<RMNode> rmRecChildren = processor.getParentNodesMap().get(rmRecId);

			for (RMNode childRMRecNode : rmRecChildren) {
				SortableNode childElement = new SortableNode(childRMRecNode.getData().determineRMRecDesc());
				node.add(childElement);
				getRecursive(childElement, childRMRecNode);
			}
		}
	}

	public Starter(RMNode useCaseRootNode) {
		setLayout(new BorderLayout());
		RMRecord rootRMRec = useCaseRootNode.getData();
		String root_desc = rootRMRec.determineRMRecDesc();

		SortableNode root = new SortableNode(root_desc);

		getRecursive(root, useCaseRootNode);

		JTree tree = new JTree(root);
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}

		JScrollPane scrollpane = new JScrollPane();
		scrollpane.getViewport().add(tree);
		add("Center", scrollpane);

		if (debug) {
			String tabs = "   ";
			System.out.println("Tree built for : " + root_desc + " is:");
			for (int i = 0; i < tree.getRowCount(); i++) {
				int rowLevel = tree.getPathForRow(i).getPathCount();
				String pathString = tree.getPathForRow(i).getPathComponent(rowLevel - 1).toString();
				if (rowLevel > 1) {
					pathString = "|- " + pathString;
				}
				for (int j = 2; j < rowLevel; j++) {
					pathString = tabs + pathString;
				}
				System.out.println(pathString);
			}
		}
	}

	public Dimension getMinimumSize() {
		return new Dimension(100, 600);
	}

	public Dimension getPreferredSize() {
		return new Dimension(100, 600);
	}

	public static void main(String[] args) {
		boolean parametererror = true;
		String inputFileName = null;
		
		if (args.length > 0) {
			for (String param : args) {
				if (param.startsWith("-FILE=")) {
					inputFileName = param.substring(6);
					parametererror = false;
				} else if (param.startsWith("-TIMEBORDER=")) {
					try {
						processor.setElapsedTimeBorder(new Long(param.substring(12)));
					} catch (Exception e) {
						parametererror = true;
						break;
					}
				} else if (param.startsWith("-DEBUG=")) {
					if (param.equalsIgnoreCase("-DEBUG=on"))
						debug = true;
				}
			}
		}
		
		if(!parametererror) {
			// we can parse the file now
			processor.processInputFile(inputFileName);		
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Starter.createAndShowGUI();
				}
			});
		} else {
			System.err.println("");
			System.err.println("ERROR: Parameters must be entered in the following format -<parameter>=<value>!");
			System.err.println("");
			System.err.println("The following parameters are supported/needed:");
			System.err.println("|-----------------------------------------------------------------------------------------|");
			System.err.println("| Parameter  | Mandatory | Default | Description                                          |");
			System.err.println("|------------|-----------|---------|------------------------------------------------------|");
			System.err.println("| FILE       | Yes       | n.a.    | path to file with request metrics entries            |");
			System.err.println("|            |           |         | (must be grepped with following command              |");
			System.err.println("|            |           |         | 'grep \"PMRM0003I\" SystemOut*.log > RMrecords.txt')   |");
			System.err.println("| TIMEBORDER | No        | null	   | time in milliseconds for the requests that should be |");
			System.err.println("|            |           |         | analyzed (null = all requests are analyzed)          |");
			System.err.println("| DEBUG	     | No        | off     | print out debug information                          |");
			System.err.println("|-----------------------------------------------------------------------------------------|");
			System.exit(1);
		}
	}

	private static void createAndShowGUI() {
			Set<String> enumUseCases = processor.getUseCaseRootList().keySet();
			for (String useCaseId : enumUseCases) {
				RMNode rmRecRoot = processor.getUseCaseRootList().get(useCaseId);
				if(debug) System.out.println(">>> Build up node " + rmRecRoot.getData().toString());
				jTreesHashtable.put(useCaseId, new Starter(rmRecRoot));
			}

			ArrayList<String> casesList = new ArrayList<String>();
			for (String data : processor.getUseCaseRootList().keySet()) {
				casesList.add(data);
			}
			JList<String> jlist = new JList<String>(casesList.toArray(new String[0]));
			if (casesList.size() > 0) {
				jlist.setSelectedIndex(0);
				selectedUseCase = (String) jlist.getSelectedValue();
			}
			jlist.setSelectionMode(0);

			Object listener = new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent event) {
					JList<String> list = (JList<String>) event.getSource();

					Starter.selectedUseCase = list.getSelectedValue();
					JPanel jpanel = Starter.jTreesHashtable.get(Starter.selectedUseCase);
					Starter.treeInternalFrame.setVisible(false);
					Starter.treeInternalFrame.getContentPane().removeAll();
					Starter.treeInternalFrame.add(jpanel, "Center");
					Starter.treeInternalFrame.setVisible(true);
				}
			};
			jlist.addListSelectionListener((ListSelectionListener) listener);

			JFrame mainFrame = new JFrame("RM Records Log File Analysis Results");
			mainFrame.setDefaultCloseOperation(3);
			JScrollPane listScrollPane = new JScrollPane(jlist);
			listInternalFrame.add(listScrollPane, "Center");
			listInternalFrame.setVisible(true);
			splitPane.setDividerLocation(200);
			splitPane.setLeftComponent(listInternalFrame);
			JPanel jpanel = null;
			if (selectedUseCase.length() > 0)
				jpanel = jTreesHashtable.get(selectedUseCase);
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

	class SortableNode extends DefaultMutableTreeNode implements Comparable {
		public SortableNode(String name) {
			super(name);
		}

		public void insert(MutableTreeNode newChild, int childIndex) {
			super.insert(newChild, childIndex);
			Collections.sort(this.children);
		}

		public int compareTo(Object obj) {
			String str = "";
			String desc = (String) getUserObject();
			String obj_desc = (String) ((SortableNode) obj).getUserObject();

			StringTokenizer st = new StringTokenizer(desc, "|");
			if (st.hasMoreTokens()) {
				str = st.nextToken();
				str = st.nextToken();
			}
			long timestamp = convertToTimeInMillis(str.trim());
			st = new StringTokenizer(obj_desc, "|");
			if (st.hasMoreTokens()) {
				str = st.nextToken();
				str = st.nextToken();
			}
			long obj_timestamp = convertToTimeInMillis(str.trim());
			long diff = timestamp - obj_timestamp;
			return Integer.parseInt(new Long(diff).toString());
		}

		public long convertToTimeInMillis(String strDateTime) {
			Calendar cal = Calendar.getInstance();

			StringTokenizer st = new StringTokenizer(strDateTime, " ");
			String strDate = st.nextToken();

			String strTime = st.nextToken();

			st = new StringTokenizer(strDate, "/");
			String str = st.nextToken();

			cal.set(2, Integer.parseInt(str));
			str = st.nextToken();

			cal.set(5, Integer.parseInt(str));
			str = st.nextToken();

			cal.set(1, Integer.parseInt(str));

			st = new StringTokenizer(strTime, ":");
			str = st.nextToken();

			cal.set(11, Integer.parseInt(str));
			str = st.nextToken();

			cal.set(12, Integer.parseInt(str));
			str = st.nextToken();

			cal.set(13, Integer.parseInt(str));
			str = st.nextToken();

			cal.set(14, Integer.parseInt(str));
			return cal.getTimeInMillis();
		}
	}
}