package de.ibm.issw.requestmetrics.gui;

import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.ParseException;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import de.ibm.issw.requestmetrics.engine.filter.RootCaseFilter;

@SuppressWarnings("serial")
public class RootCaseToolBar extends JToolBar{
	private final JComboBox comboBox = new JComboBox();
	private final JFormattedTextField elapsedTimeFilterField = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JFormattedTextField detailFilterField = new JFormattedTextField();
	private RootCaseFilter rootCaseFilter;
	
	public RootCaseToolBar() {
		
		setFloatable(false);
		setLayout(new FlowLayout());
		
		comboBox.setEnabled(false);
		elapsedTimeFilterField.setColumns(5);
		elapsedTimeFilterField.setEnabled(false);
		elapsedTimeFilterField.setFocusLostBehavior(JFormattedTextField.REVERT);
		elapsedTimeFilterField.addKeyListener(elapsedTimeFilterFieldListener);
		detailFilterField.setColumns(5);
		detailFilterField.setEnabled(false);
		detailFilterField.addKeyListener(detailFieldListener);
		
		this.add(comboBox);
		this.add(new JLabel("Show Elapsed Time > "));
		this.add(elapsedTimeFilterField);
		this.add(new JLabel("Filter Details: "));
		this.add(detailFilterField);
		rootCaseFilter = new RootCaseFilter();
	}
	
	private KeyAdapter elapsedTimeFilterFieldListener = new KeyAdapter() {
		public void keyReleased(KeyEvent evt) {
			if (evt != null) {
				try {
					elapsedTimeFilterField.commitEdit();
					rootCaseFilter.filterElapsedTime(elapsedTimeFilterField.getValue());
				} catch (ParseException e) {
					rootCaseFilter.filterElapsedTime(null);
					elapsedTimeFilterField.setFocusLostBehavior(JFormattedTextField.PERSIST);
				}
			}
		}
	};
	
	private KeyAdapter detailFieldListener = new KeyAdapter() {
		
		@Override
		public void keyReleased(KeyEvent evt) {
			if (evt != null) {
				try {
					detailFilterField.commitEdit();
					rootCaseFilter.filterDetails(detailFilterField.getText());
				} catch (ParseException e) {
					
				}
			}
		}
	};
		
	public void setFiltersEnabled(boolean enabled) {
		if (enabled == true) {
			comboBox.setEnabled(true);
			elapsedTimeFilterField.setEnabled(true);
			detailFilterField.setEnabled(true);
		} else {
			comboBox.setEnabled(false);
			elapsedTimeFilterField.setEnabled(false);
			detailFilterField.setEnabled(false);
		}
	}

}
