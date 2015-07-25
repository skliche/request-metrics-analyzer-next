
import javax.swing.SwingUtilities;

import de.ibm.issw.requestmetrics.RmProcessor;
import de.ibm.issw.requestmetrics.gui.RequestMetricsGui;

public class Starter {
	private static boolean debug = false;

	public static void main(String[] args) {
		boolean parametererror = false;
		final RmProcessor processor = new RmProcessor();
		
		if (args.length > 0) {
			for (String param : args) {
				if (param.startsWith("-TIMEBORDER=")) {
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
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					RequestMetricsGui.createAndShowGUI(processor);
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
			System.err.println("| TIMEBORDER | No        | null	   | time in milliseconds for the requests that should be |");
			System.err.println("|            |           |         | analyzed (null = all requests are analyzed)          |");
			System.err.println("| DEBUG	     | No        | off     | print out debug information                          |");
			System.err.println("|-----------------------------------------------------------------------------------------|");
			System.exit(1);
		}
	}
}