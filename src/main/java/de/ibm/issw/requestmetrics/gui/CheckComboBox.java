package de.ibm.issw.requestmetrics.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.EventListener;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JSeparator;
import javax.swing.ListCellRenderer;

@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
public class CheckComboBox extends JComboBox {
	private List<CheckBoxObject> checkBoxesList;
	private Map<Object, Boolean> mapCheckBoxObjectsSelected;
	private List<CheckComboBoxSelectionChangedListener> changedListeners = new Vector();

	private Object nullObject = new Object();

	public CheckComboBox(final Set objects) {
		resetCheckBoxes(objects, false);
	}

	public void addSelectionChangedListener(CheckComboBoxSelectionChangedListener listener) {
		if (listener == null) {
			return;
		}
		changedListeners.add(listener);
	}

	public void resetCheckBoxes(final Set objects, boolean selected) {
		mapCheckBoxObjectsSelected = new LinkedHashMap();
		for (Object object : objects) {
			mapCheckBoxObjectsSelected.put(object, selected);
		}

		reset();
	}

	public Object[] getSelectedItems() {
		Set selectedItemsSet = new TreeSet(); // alphabetically
		for (Map.Entry<Object, Boolean> entry : mapCheckBoxObjectsSelected.entrySet()) {
			Object object = entry.getKey();
			Boolean selected = entry.getValue();

			if (selected) {
				selectedItemsSet.add(object);
			}
		}

		if (selectedItemsSet.isEmpty())
			return null;

		return selectedItemsSet.toArray(new Object[selectedItemsSet.size()]);
	}

	public void reset() {
		this.removeAllItems();

		initCheckBoxes();

		this.addItem(new String());
		for (JCheckBox checkBox : checkBoxesList) {
			this.addItem(checkBox);
		}

		setRenderer(new CheckBoxRenderer(checkBoxesList));
		addActionListener(this);
	}

	private void initCheckBoxes() {
		checkBoxesList = new Vector<CheckBoxObject>();

		boolean selectedNone = false;

		CheckBoxObject checkBox;
		for (Map.Entry<Object, Boolean> entry : mapCheckBoxObjectsSelected.entrySet()) {
			Object object = entry.getKey();
			Boolean selected = entry.getValue();

			if (selected)
				selectedNone = false;
				
			checkBox = new CheckBoxObject(object);
			checkBox.setSelected(selected);
			checkBoxesList.add(checkBox);
		}

		checkBox = new CheckBoxObject("Show all types");
		checkBox.setSelected(selectedNone);
		checkBoxesList.add(checkBox);
	}

	private void checkBoxSelectionChanged(int index) {
		int n = checkBoxesList.size();
		if (index < 0 || index >= n)
			return;

		if (index < n - 1) {
			CheckBoxObject checkBox = checkBoxesList.get(index);
			if (checkBox.getObject() == nullObject) {
				return;
			}

			if (checkBox.isSelected()) {
				checkBox.setSelected(false);
				mapCheckBoxObjectsSelected.put(checkBox.getObject(), false);

				checkBoxesList.get(n - 1).setSelected(getSelectedItems() == null); // select
																		// none
			} else {
				checkBox.setSelected(true);
				mapCheckBoxObjectsSelected.put(checkBox.getObject(), true);

				checkBoxesList.get(n - 1).setSelected(false); // select none
			}
		} else if (index == n - 1){ 
			for (Object object : mapCheckBoxObjectsSelected.keySet()) {
				mapCheckBoxObjectsSelected.put(object, false);
			}

			for (int i = 0; i < n - 1; i++) {
				checkBoxesList.get(i).setSelected(false);
			}
			checkBoxesList.get(n - 1).setSelected(true);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int selection = getSelectedIndex();

		if (selection == 0) {
			getUI().setPopupVisible(this, true);
		} else if (selection > 0) {
			checkBoxSelectionChanged(selection - 1);
			for (CheckComboBoxSelectionChangedListener l : changedListeners) {
				l.selectionChanged();
			}
		}

		this.setSelectedIndex(-1); // clear selection
	}

	// checkbox renderer for combobox
	class CheckBoxRenderer implements ListCellRenderer {
		private final DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		private JSeparator separator;
		private final List<CheckBoxObject> checkBoxes;

		public CheckBoxRenderer(final List<CheckBoxObject> checkBoxes) {
			this.checkBoxes = checkBoxes;
			separator = new JSeparator(JSeparator.HORIZONTAL);
		}

		// @Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			if (index > 0 && index <= checkBoxes.size()) {
				CheckBoxObject checkBox = checkBoxes.get(index - 1);
				if (checkBox.getObject() == nullObject) {
					return separator;
				}

				checkBox.setBackground(isSelected ? Color.blue : Color.white);
				checkBox.setForeground(isSelected ? Color.white : Color.black);

				return checkBox;
			}

			String string;
			Object[] objects = getSelectedItems();
			Vector<String> strings = new Vector();
			if (objects == null) {
				string = "All types";
			} else {
				for (Object object : objects) {
					strings.add(object.toString());
				}
				string = strings.toString();
			}
			return defaultRenderer.getListCellRendererComponent(list, string, index, isSelected, cellHasFocus);
		}
	}

	class CheckBoxObject extends JCheckBox {
		private final Object object;

		public CheckBoxObject(final Object obj) {
			super(obj.toString());
			this.object = obj;
		}

		public Object getObject() {
			return object;
		}
	}
}

interface CheckComboBoxSelectionChangedListener extends EventListener {
	public void selectionChanged();
}
