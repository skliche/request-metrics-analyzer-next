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
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.freixas.jcalendar.DateEvent;
import org.freixas.jcalendar.DateListener;
import org.freixas.jcalendar.JCalendarCombo;

import de.ibm.issw.requestmetrics.engine.filter.RootCaseFilter;

@SuppressWarnings("serial")
public class RootCaseToolBar extends JToolBar{
	private final JFormattedTextField elapsedTimeFilterField = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JFormattedTextField detailFilterField = new JFormattedTextField();
	private JButton clearFiltersButton = new JButton("CLEAR");
	private final CheckComboBox comboBox;
	private RootCaseFilter rootCaseFilter;
	private Set<String> checkBoxes;
	private JCalendarCombo startDatePicker =  new JCalendarCombo(JCalendarCombo.DISPLAY_DATE | JCalendarCombo.DISPLAY_TIME, false);
	private JCalendarCombo endDatePicker =  new JCalendarCombo(JCalendarCombo.DISPLAY_DATE | JCalendarCombo.DISPLAY_TIME, false);
	
	private final String EJB = "EJB";
	private final String SERVLET_FILTER = "Servlet Filter";
	private final String WEB_SERVICES = "Web Services";
	private final String JNDI = "JNDI";
	private final String JMS = "JMS";
	private final String ASYNC_BEANS = "AsyncBeans";
	
	public RootCaseToolBar() {
		
		setFloatable(false);
		setLayout(new FlowLayout());
		
		elapsedTimeFilterField.setColumns(5);
		elapsedTimeFilterField.setEnabled(false);
		elapsedTimeFilterField.setFocusLostBehavior(JFormattedTextField.PERSIST);
		elapsedTimeFilterField.addKeyListener(elapsedTimeFilterFieldListener);
		
		detailFilterField.setColumns(12);
		detailFilterField.setEnabled(false);
		detailFilterField.setFocusLostBehavior(JFormattedTextField.PERSIST);
		detailFilterField.addKeyListener(detailFieldListener);
		
		clearFiltersButton.setEnabled(false);
		clearFiltersButton.addActionListener(clearFiltersListener);
		
		checkBoxes = new HashSet<String>();
		checkBoxes.add(EJB);
		checkBoxes.add(SERVLET_FILTER);
		checkBoxes.add(WEB_SERVICES);
		checkBoxes.add(JNDI);
		checkBoxes.add(JMS);
		checkBoxes.add(ASYNC_BEANS);
		
		comboBox = new CheckComboBox(checkBoxes);
		comboBox.setEnabled(false);
		comboBox.addSelectionChangedListener(checkBoxListener);
		
		startDatePicker.setEditable(false);
		startDatePicker.setEnabled(false);
		startDatePicker.setToolTipText("Filter for entries which occured after the chosen date");
		startDatePicker.addDateListener(startDateListener);
		
		endDatePicker.setEditable(false);
		endDatePicker.setEnabled(false);
		endDatePicker.setToolTipText("Filter for entries which occured before the chosen date");
		endDatePicker.addDateListener(endDateListener);
		
		this.add(comboBox);
		this.add(new JLabel("Start Date:"));
		this.add(startDatePicker);
		this.add(new JLabel("End Date: "));
		this.add(endDatePicker);
		this.add(new JLabel("Show Elapsed Time > "));
		this.add(elapsedTimeFilterField);
		this.add(new JLabel("Filter Details: "));
		this.add(detailFilterField);
		this.add(clearFiltersButton);
		
	}
	
	private DateListener startDateListener = new DateListener() {

		@Override
		public void dateChanged(DateEvent evt) {
			rootCaseFilter.filterStartDate(evt.getSelectedDate().getTime());
		}
	};
	
	private DateListener endDateListener = new DateListener() {

		@Override
		public void dateChanged(DateEvent evt) {
			rootCaseFilter.filterEndDate(evt.getSelectedDate().getTime());
		}
	};
	
	private KeyAdapter elapsedTimeFilterFieldListener = new KeyAdapter() {
		public void keyReleased(KeyEvent evt) {
			if (evt != null) {
				try {
					elapsedTimeFilterField.commitEdit();
					rootCaseFilter.filterElapsedTime(elapsedTimeFilterField.getValue());
				} catch (ParseException e) {
					rootCaseFilter.filterElapsedTime(null);
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
					rootCaseFilter.filterDetails(null);
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
		
	/**
	 * enables the filters and generates a new instance of the filter class
	 * @param rootCaseTable the table that the filters are applied on
	 */
	public void enableFilters(JTable rootCaseTable) {
		elapsedTimeFilterField.setEnabled(true);
		detailFilterField.setEnabled(true);
		comboBox.setEnabled(true);
		clearFiltersButton.setEnabled(true);
		startDatePicker.setEnabled(true);
		endDatePicker.setEnabled(true);
		rootCaseFilter = new RootCaseFilter(rootCaseTable);
	}
	
	/**
	 * disables all input fields for the filters and resets the filters
	 */
	public void disableFilters() {
		elapsedTimeFilterField.setEnabled(false);
		detailFilterField.setEnabled(false);
		comboBox.setEnabled(false);
		clearFiltersButton.setEnabled(false);
		startDatePicker.setEnabled(false);
		endDatePicker.setEnabled(false);
		if (rootCaseFilter != null)
			rootCaseFilter.clearFilters();
	}
}
