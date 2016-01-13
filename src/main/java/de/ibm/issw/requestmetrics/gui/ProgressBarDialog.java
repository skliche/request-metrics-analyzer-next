package de.ibm.issw.requestmetrics.gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

import de.ibm.issw.requestmetrics.engine.events.PercentageIncreasedEvent;

@SuppressWarnings("serial")
public class ProgressBarDialog extends JDialog {
	private JProgressBar progressBarAllFiles = new JProgressBar(0, 100);
	private JProgressBar progressBarCurrentFile = new JProgressBar(0, 100);
	private JLabel currentFileLabel = new JLabel();
	private JLabel allFilesLabel = new JLabel();
	final private static int CHARS_TO_SHOW = 45;

	public ProgressBarDialog() {
		Border pbAllFilesBorder = BorderFactory.createTitledBorder("Progress of processing all files");
		progressBarAllFiles.setStringPainted(true);
		progressBarAllFiles.setBorder(pbAllFilesBorder);

		Border pbCurrentFileBorder = BorderFactory.createTitledBorder("Progress of processing current File");
		progressBarCurrentFile.setStringPainted(true);
		progressBarCurrentFile.setBorder(pbCurrentFileBorder);

		this.setTitle("Parsing selected Files...");
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		this.add(Box.createVerticalStrut(40));
		this.add(new JLabel("Currently processed file: "));
		this.add(currentFileLabel);
		this.add(Box.createVerticalStrut(10));
		this.add(progressBarCurrentFile);
		this.add(Box.createVerticalStrut(40));
		this.add(new JLabel("Processed Files: "));
		this.add(allFilesLabel);
		this.add(Box.createVerticalStrut(10));
		this.add(progressBarAllFiles);
		this.add(Box.createVerticalStrut(10));

		this.setSize(300, 300);
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	/**
	 * 
	 * @param event - LineProcessingEvent that created when 
	 */
	public void update(PercentageIncreasedEvent event) {
		progressBarCurrentFile.setValue(event.getPercentCurrentFileProcessed());
		String fileName = event.getFileName();
		if (fileName == null || fileName.length() < CHARS_TO_SHOW) {
		    currentFileLabel.setText(fileName);
		} else {
			currentFileLabel.setText("...".concat(fileName.substring(fileName.length() - CHARS_TO_SHOW)));
		}
		progressBarAllFiles.setValue(event.getPercentAllFilesProcessed());
		allFilesLabel.setText(event.getFilesProcessed() + " / " + event.getTotalFiles());
	}
}