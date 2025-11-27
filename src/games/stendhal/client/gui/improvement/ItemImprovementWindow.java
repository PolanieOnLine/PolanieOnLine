/*******************************************************************************
*                      (C) Copyright 2024 - PolanieOnLine                     *
*******************************************************************************/
/*******************************************************************************
*                                                                             *
*   This program is free software; you can redistribute it and/or modify      *
*   it under the terms of the GNU General Public License as published by      *
*   the Free Software Foundation; either version 2 of the License, or         *
*   (at your option) any later version.                                       *
*                                                                             *
*******************************************************************************/
package games.stendhal.client.gui.improvement;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import games.stendhal.client.gui.InternalManagedWindow;
import games.stendhal.client.gui.imageviewer.ItemListImageViewerEvent.HeaderRenderer;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.sprite.Sprite;

/**
* Internal window showing improvable items with icon rendering matching item logs.
*/
public class ItemImprovementWindow extends InternalManagedWindow {
	private static final long serialVersionUID = 1L;
	private static final int PAD = 5;

	private final String npcName;
	private final ImprovementTableModel model;
	private final JTable table;

	public ItemImprovementWindow(final String npcName) {
		super("improvement", "Ulepszanie - " + npcName);
		this.npcName = npcName;

		setMinimizable(true);
		setCloseable(true);
		setHideOnClose(true);

		model = new ImprovementTableModel();
		table = new JTable(model);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

		final TableColumnModel columns = table.getColumnModel();
		columns.getColumn(0).setCellRenderer(new SpriteCellRenderer());
		final ImproveButtonCell buttonCell = new ImproveButtonCell();
		columns.getColumn(4).setCellRenderer(buttonCell);
		columns.getColumn(4).setCellEditor(buttonCell);

		final HeaderRenderer headerRenderer = new HeaderRenderer();
		for (final Enumeration<TableColumn> e = columns.getColumns(); e.hasMoreElements();) {
			final TableColumn col = e.nextElement();
			col.setHeaderRenderer(headerRenderer);
		}

	adjustColumnWidths();

	final JScrollPane scrollPane = new JScrollPane(table);
	scrollPane.setPreferredSize(new Dimension(650, 320));

	final JPanel content = new JPanel(new BorderLayout());
	content.setOpaque(false);
	content.add(scrollPane, BorderLayout.CENTER);
	setContent(content);
}

public boolean isForNpc(final String target) {
	return npcName.equals(target);
}

public void updateItems(final List<ItemImprovementEntry> items) {
	model.setRows(items);
	adjustRowHeights();
	adjustColumnWidths();
}

private void adjustColumnWidths() {
	final TableColumnModel model = table.getColumnModel();
	for (int column = 0; column < model.getColumnCount(); column++) {
		int width = model.getColumn(column).getMinWidth();
		for (int row = 0; row < table.getRowCount(); row++) {
			final Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
			width = Math.max(width, comp.getPreferredSize().width + PAD);
		}
	model.getColumn(column).setPreferredWidth(width);
}
}

private void adjustRowHeights() {
	for (int row = 0; row < table.getRowCount(); row++) {
		int rowHeight = table.getRowHeight();
		for (int column = 0; column < table.getColumnCount(); column++) {
			final Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
			rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
		}
	table.setRowHeight(row, rowHeight);
}
}

private void openConfirm(final ItemImprovementEntry entry) {
	new ItemImproveConfirmDialog(j2DClient.get().getMainFrame(), npcName, entry).setVisible(true);
}

private class ImprovementTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private final String[] columns = { "Ikona", "Przedmiot", "Materiały", "Koszt", "Akcja" };
	private List<ItemImprovementEntry> rows = new ArrayList<ItemImprovementEntry>();

	public void setRows(final List<ItemImprovementEntry> rows) {
		this.rows = new ArrayList<ItemImprovementEntry>(rows);
		fireTableDataChanged();
	}

@Override
public int getRowCount() {
	return rows.size();
}

@Override
public int getColumnCount() {
	return columns.length;
}

@Override
public Object getValueAt(final int rowIndex, final int columnIndex) {
	final ItemImprovementEntry entry = rows.get(rowIndex);
	switch (columnIndex) {
		case 0:
		return entry.getIcon();
		case 1:
		return entry.getName() + " (" + entry.getImprove() + "/" + entry.getMaxImprove() + ")";
		case 2:
		return entry.getRequirements();
		case 3:
		return entry.getCost();
		case 4:
		return entry;
		default:
		return "";
	}
}

@Override
public String getColumnName(final int column) {
	return columns[column];
}

@Override
public Class<?> getColumnClass(final int columnIndex) {
	if (columnIndex == 0) {
		return Sprite.class;
	}
if (columnIndex == 3) {
	return Integer.class;
}
if (columnIndex == 4) {
	return ItemImprovementEntry.class;
}
return String.class;
}

@Override
public boolean isCellEditable(final int rowIndex, final int columnIndex) {
	return columnIndex == 4;
}

public ItemImprovementEntry getRow(final int row) {
	return rows.get(row);
}
}

private class SpriteCellRenderer extends JComponent implements TableCellRenderer {
	private static final long serialVersionUID = 1L;
	private Sprite sprite;

	@Override
	public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
	final boolean hasFocus, final int row, final int column) {
		if (value instanceof Sprite) {
			sprite = (Sprite) value;
		} else {
		sprite = null;
	}
return this;
}

@Override
public Dimension getPreferredSize() {
	if (sprite == null) {
		return new Dimension(24, 24);
	}
return new Dimension(sprite.getWidth() + 2 * PAD, sprite.getHeight() + 2 * PAD);
}

@Override
protected void paintComponent(final Graphics g) {
	super.paintComponent(g);
	if (sprite != null) {
		sprite.draw(g, (getWidth() - sprite.getWidth()) / 2, (getHeight() - sprite.getHeight()) / 2);
	}
}
}

private class ImproveButtonCell extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener {
	private static final long serialVersionUID = 1L;
	private final JButton button = new JButton("Ulepsz");
	private ItemImprovementEntry entry;

	ImproveButtonCell() {
		button.addActionListener(this);
		button.setFocusable(false);
	}

@Override
public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
final boolean hasFocus, final int row, final int column) {
	return prepareButton(value);
}

@Override
public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected,
final int row, final int column) {
	return prepareButton(value);
}

private Component prepareButton(final Object value) {
	if (value instanceof ItemImprovementEntry) {
		entry = (ItemImprovementEntry) value;
		button.setEnabled(entry.getImprove() < entry.getMaxImprove());
	} else {
	entry = null;
	button.setEnabled(false);
}
return button;
}

@Override
public Object getCellEditorValue() {
	return entry;
}

@Override
public void actionPerformed(final ActionEvent e) {
	fireEditingStopped();
	if (entry != null) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				openConfirm(entry);
			}
	});
}
}
}
}
