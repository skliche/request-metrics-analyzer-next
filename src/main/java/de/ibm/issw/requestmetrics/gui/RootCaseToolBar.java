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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import de.ibm.issw.requestmetrics.engine.filter.RootCaseFilter;

@SuppressWarnings("serial")
public class RootCaseToolBar extends JToolBar{
	private final JComboBox comboBox = new JComboBox();
	private final JFormattedTextField elapsedTimeFilterField = new JFormattedTextField(NumberFormat.getIntegerInstance());
	private final JFormattedTextField detailFilterField = new JFormattedTextField();
	
	public RootCaseToolBar() {
		
		setFloatable(false);
		setLayout(new FlowLayout());
		
		comboBox.setEnabled(false);
		elapsedTimeFilterField.setColumns(5);
		elapsedTimeFilterField.setEnabled(false);
		elapsedTimeFilterField.addKeyListener(elapsedTimeFilterInput);
		detailFilterField.setColumns(5);
		detailFilterField.setEnabled(false);
		detailFilterField.getDocument().addDocumentListener(detailFieldListener);
		
		this.add(comboBox);
		this.add(new JLabel("Show Elapsed Time >"));
		this.add(elapsedTimeFilterField);
		this.add(new JLabel());
		this.add(detailFilterField);
	
	}
	
	private KeyAdapter elapsedTimeFilterInput = new KeyAdapter() {
		public void keyReleased(KeyEvent evt) {
			if (evt != null) {
				try {
					elapsedTimeFilterField.commitEdit();
					RootCaseFilter.filterElapsedTime(elapsedTimeFilterField.getValue());
				} catch (ParseException e) {
					
				}
			}
		}
	};
	
	private DocumentListener detailFieldListener = new DocumentListener() {
		
		@Override
		public void removeUpdate(DocumentEvent e) {
		}
		
		@Override
		public void insertUpdate(DocumentEvent e) {
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
		}
	};
		
	
	/*elapsedTimeFilterField.addKeyListener(new KeyAdapter() {
		public void keyReleased(KeyEvent evt) {
			if (evt != null) {
				TableRowSorter<UsecaseTableModel> sorter = new TableRowSorter<UsecaseTableModel>(rootCaseModel);
				table.setRowSorter(sorter);
				try {
					elapsedTimeFilterField.commitEdit();
					final Long userInput = (Long) elapsedTimeFilterField.getValue();
					
					RowFilter<UsecaseTableModel, Integer> filterElapsedTime = new RowFilter<UsecaseTableModel, Integer>(){
						
						@Override
						public boolean include(javax.swing.RowFilter.Entry<? extends UsecaseTableModel, ? extends Integer> entry) {
							Long elapsedTime = (Long) rootCaseModel.getValueAt(entry.getIdentifier(), 2);
							if (elapsedTime >= userInput || elapsedTimeFilterField.getValue().equals(null)) {
								return true;
							} else {
								return false;
							}
						}
					};
					sorter.setRowFilter(filterElapsedTime);
				} catch (ParseException e) {
					sorter.setRowFilter(null);
				}
				
			}
		}
	});*/
	
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
