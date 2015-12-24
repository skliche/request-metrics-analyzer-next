package de.ibm.issw.requestmetrics.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JToolBar;

import org.freixas.jcalendar.DateEvent;
import org.freixas.jcalendar.DateListener;
import org.freixas.jcalendar.JCalendarCombo;

import de.ibm.issw.requestmetrics.engine.filter.RootCaseFilter;
import de.ibm.issw.requestmetrics.gui.comparator.LogTimeStampComparator;
import de.ibm.issw.requestmetrics.model.RmRootCase;

@SuppressWarnings("serial")
public class RootCaseToolBar extends JToolBar{
	private final JFormattedTextField elapsedTimeFilterField = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JFormattedTextField detailFilterField = new JFormattedTextField();
	private JButton clearFiltersButton = new JButton("CLEAR");
	private final CheckComboBox comboBox;
	private RootCaseFilter rootCaseFilter;
	private JCalendarCombo startDatePicker =  new JCalendarCombo(JCalendarCombo.DISPLAY_DATE | JCalendarCombo.DISPLAY_TIME, false);
	private JCalendarCombo endDatePicker =  new JCalendarCombo(JCalendarCombo.DISPLAY_DATE | JCalendarCombo.DISPLAY_TIME, false);
	
	public static final SimpleDateFormat sdf = new SimpleDateFormat("y/MM/dd HH:mm:ss");
	
	private static Date startLogTimeStamp;
	private static Date endLogTimeStamp;
	private Set<String> types;
	private RequestMetricsGui gui;
	
	public RootCaseToolBar(RequestMetricsGui gui) {
		this.gui = gui;
		setFloatable(false);
		setLayout(new FlowLayout(FlowLayout.LEFT));
		
		elapsedTimeFilterField.setColumns(5);
		elapsedTimeFilterField.setEnabled(false);
		elapsedTimeFilterField.setFocusLostBehavior(JFormattedTextField.PERSIST);
		elapsedTimeFilterField.addKeyListener(elapsedTimeFilterFieldListener);
		
		detailFilterField.setColumns(20);
		detailFilterField.setEnabled(false);
		detailFilterField.setFocusLostBehavior(JFormattedTextField.PERSIST);
		detailFilterField.addKeyListener(detailFieldListener);
		
		clearFiltersButton.setEnabled(false);
		clearFiltersButton.addActionListener(clearFiltersListener);
		
		comboBox = new CheckComboBox(new TreeSet<String>());
		comboBox.setEnabled(false);
		comboBox.setPreferredSize(new Dimension(200, 20));
		comboBox.setMinimumSize(comboBox.getPreferredSize());
		comboBox.setMaximumSize(comboBox.getPreferredSize());
		comboBox.addSelectionChangedListener(checkBoxListener);
		
		startDatePicker.setDateFormat(sdf);
		startDatePicker.setDate(null);
		startDatePicker.setEditable(false);
		startDatePicker.setEnabled(false);
		startDatePicker.setPreferredSize(new Dimension(150, 20));
		startDatePicker.setMinimumSize(startDatePicker.getPreferredSize());
		startDatePicker.setMaximumSize(startDatePicker.getPreferredSize());
		startDatePicker.setToolTipText("Filter for entries which occured after the chosen date");
		startDatePicker.addDateListener(startDateListener);
		
		endDatePicker.setDateFormat(sdf);
		endDatePicker.setDate(null);
		endDatePicker.setEditable(false);
		endDatePicker.setEnabled(false);
		endDatePicker.setPreferredSize(new Dimension(150, 20));
		endDatePicker.setMinimumSize(startDatePicker.getPreferredSize());
		endDatePicker.setMaximumSize(startDatePicker.getPreferredSize());
		endDatePicker.setToolTipText("Filter for entries which occured before the chosen date");
		endDatePicker.addDateListener(endDateListener);
		
		this.add(new JLabel("Timestamp >"));
		this.add(startDatePicker);
		this.add(new JLabel("Timestamp <"));
		this.add(endDatePicker);
		this.add(new JLabel("Elapsed Time >"));
		this.add(elapsedTimeFilterField);
		this.add(new JLabel("Types:"));
		this.add(comboBox);
		this.add(new JLabel("Details:"));
		this.add(detailFilterField);
		this.add(clearFiltersButton);
	}
	
	private DateListener startDateListener = new DateListener() {
		@Override
		public void dateChanged(DateEvent evt) {
			if(evt.getSelectedDate() != null) rootCaseFilter.filterStartDate(evt.getSelectedDate().getTime());
		}
	};
	
	private DateListener endDateListener = new DateListener() {
		@Override
		public void dateChanged(DateEvent evt) {
			if(evt.getSelectedDate() != null) rootCaseFilter.filterEndDate(evt.getSelectedDate().getTime());
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
			clearFilters();
		}
	};
	
	private CheckComboBoxSelectionChangedListener checkBoxListener = new CheckComboBoxSelectionChangedListener() {
		@Override
		public void selectionChanged() {
			rootCaseFilter.filterType(comboBox);
		}
	};
	
	private void clearFilters() {
		rootCaseFilter.clearFilters();
		comboBox.resetCheckBoxes(types, false);
		elapsedTimeFilterField.setValue(null);
		detailFilterField.setText("");
		startDatePicker.setDate(startLogTimeStamp);
		endDatePicker.setDate(endLogTimeStamp);
	}
		
	/**
	 * enables the filters and generates a new instance of the filter class
	 * @param rootCaseTable the table that the filters are applied on
	 */
	public void enableFilters(JTable rootCaseTable, Set<String> types) {
		elapsedTimeFilterField.setEnabled(true);
		detailFilterField.setEnabled(true);
		comboBox.setEnabled(true);
		clearFiltersButton.setEnabled(true);
		startDatePicker.setEnabled(true);
		endDatePicker.setEnabled(true);
		rootCaseFilter = new RootCaseFilter(rootCaseTable, gui);

		// set start- and end-date after rootCaseFilter is initialized
		if(rootCaseTable != null && rootCaseTable.getModel() != null) {
			List<RmRootCase> rootCases = ((RootCaseTableModel)rootCaseTable.getModel()).getUseCases();
			Collections.sort(rootCases, new LogTimeStampComparator());
			// substract and add one millisecond to include the first and the last root case in the filter
			startLogTimeStamp = new Date(rootCases.get(0).getRmNode().getData().getLogTimeStamp().getTime()-1);
			endLogTimeStamp = new Date(rootCases.get(rootCases.size()-1).getRmNode().getData().getLogTimeStamp().getTime()+1);
		}
		// set types for combobox
		this.types = types;
		clearFilters();
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
		startDatePicker.setDate(null);
		endDatePicker.setEnabled(false);
		endDatePicker.setDate(null);
		if (rootCaseFilter != null)
			rootCaseFilter.clearFilters();
	}
}
