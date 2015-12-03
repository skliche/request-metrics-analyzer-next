package de.ibm.issw.requestmetrics.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JToolBar;

import de.ibm.issw.requestmetrics.engine.filter.RootCaseFilter;

@SuppressWarnings("serial")
public class RootCaseToolBar extends JToolBar{
	private final JFormattedTextField elapsedTimeFilterField = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JFormattedTextField detailFilterField = new JFormattedTextField();
	private JButton clearFiltersButton = new JButton("CLEAR");
	private final CheckComboBox comboBox;
	private RootCaseFilter rootCaseFilter;
	Set<String> checkBoxes;
	
	public RootCaseToolBar() {
		
		setFloatable(false);
		setLayout(new FlowLayout());
		
		elapsedTimeFilterField.setColumns(5);
		elapsedTimeFilterField.setEnabled(false);
		elapsedTimeFilterField.setFocusLostBehavior(JFormattedTextField.REVERT);
		elapsedTimeFilterField.addKeyListener(elapsedTimeFilterFieldListener);
		detailFilterField.setColumns(12);
		detailFilterField.setEnabled(false);
		detailFilterField.addKeyListener(detailFieldListener);
		clearFiltersButton.setEnabled(false);
		clearFiltersButton.addActionListener(clearFiltersListener);
		
		checkBoxes = new HashSet<String>();
		checkBoxes.add("EJB");
		checkBoxes.add("Servlet Filter");
		checkBoxes.add("Web Services");
		checkBoxes.add("JNDI");
		checkBoxes.add("JMS");
		checkBoxes.add("AsynchBeans");
		
		comboBox = new CheckComboBox(checkBoxes);
		comboBox.setEnabled(false);
		comboBox.addSelectionChangedListener(checkBoxListener);
		
		this.add(comboBox);
		this.add(new JLabel("Show Elapsed Time > "));
		this.add(elapsedTimeFilterField);
		this.add(new JLabel("Filter Details: "));
		this.add(detailFilterField);
		this.add(clearFiltersButton);
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
	
	private ActionListener clearFiltersListener = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			rootCaseFilter.clearFilters();
			comboBox.resetCheckBoxes(checkBoxes, false);
			elapsedTimeFilterField.setValue(null);
			detailFilterField.setText("");
		}
	};
	
	private CheckComboBoxSelectionChangedListener checkBoxListener = new CheckComboBoxSelectionChangedListener() {
		
		@Override
		public void selectionChanged() {
			rootCaseFilter.filterType(comboBox);
		}
	};
		
	public void setFiltersEnabled(boolean enabled) {
		if (enabled == true) {
			elapsedTimeFilterField.setEnabled(true);
			detailFilterField.setEnabled(true);
			comboBox.setEnabled(true);
			clearFiltersButton.setEnabled(true);
		} 
		else {
			elapsedTimeFilterField.setEnabled(false);
			detailFilterField.setEnabled(false);
			comboBox.setEnabled(false);
			clearFiltersButton.setEnabled(false);
		}
	}

}
