package games.stendhal.client.gui.shop;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

class ShopTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private final List<ShopItem> items;
	private final String[] columnNames = { "Ikona", "Nazwa", "Cena" };

	ShopTableModel(List<ShopItem> initialItems) {
		items = new ArrayList<ShopItem>(initialItems);
	}

	public void setItems(List<ShopItem> newItems) {
		items.clear();
		if (newItems != null) {
			items.addAll(newItems);
		}
		fireTableDataChanged();
	}

	public ShopItem getItemAt(int rowIndex) {
		if ((rowIndex < 0) || (rowIndex >= items.size())) {
			return null;
		}
		return items.get(rowIndex);
	}

	@Override
	public int getRowCount() {
		return items.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return ShopItem.class;
		case 1:
			return String.class;
		case 2:
			return Integer.class;
		default:
			return Object.class;
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		ShopItem item = items.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return item;
		case 1:
			return item.getName();
		case 2:
			return Integer.valueOf(item.getPriceCopper());
		default:
			return null;
		}
	}

	boolean isEmpty() {
		return items.isEmpty();
	}
}
