/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.events;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import games.stendhal.client.GameScreen;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.ScrolledViewport;
import games.stendhal.client.gui.imageviewer.ImageViewWindow;
import games.stendhal.client.gui.imageviewer.ItemListImageViewerEvent.HeaderRenderer;
import games.stendhal.client.gui.imageviewer.ViewPanel;

public class ItemLogEvent extends Event<RPEntity> {
	// logger instance
	private static Logger logger = Logger.getLogger(ItemLogEvent.class);

	@Override
	public void execute() {
		if (event.has("dropped_items")) {
			// much of this is taken from games.stendhal.client.gui.imageviewer.ItemListImageViewerEvent
			final ViewPanel panel = new ViewPanel() {
				private static final int PAD = 5;

				@Override
				public void prepareView(final Dimension maxSize) {
					Dimension screenSize = GameScreen.get().getSize();
					int maxPreferredWidth = screenSize.width - 180;
					JLabel header = new JLabel();
					header.setBorder(BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD));
					add(header, BorderLayout.NORTH);

					final JTable table = createTable();
					// Prevents selection
					table.setEnabled(false);
					table.setFillsViewportHeight(true);
					table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
					TableColumn col = table.getColumnModel().getColumn(0);
					col.setCellRenderer(new DefaultTableCellRenderer());

					DefaultTableCellRenderer r = new DefaultTableCellRenderer();
					r.setHorizontalAlignment(SwingConstants.CENTER);

					col = table.getColumnModel().getColumn(1);
					col.setCellRenderer(r);
					col = table.getColumnModel().getColumn(2);
					col.setCellRenderer(r);

					HeaderRenderer hr = new HeaderRenderer();
					Enumeration<TableColumn> cols = table.getColumnModel().getColumns();
					while (cols.hasMoreElements()) {
						TableColumn c = cols.nextElement();
						c.setHeaderRenderer(hr);
					}

					adjustColumnWidths(table);
					adjustRowHeights(table);

					ScrolledViewport viewPort = new ScrolledViewport(table);
					/*
					 * maxPreferredWidth is incorrect, but java does not seem to support
					 * max-width property for div's, so all the cells report the same
					 * preferred width anyway.
					 */
					viewPort.getComponent().setPreferredSize(new Dimension(maxPreferredWidth,
							Math.min(screenSize.height - 200, table.getPreferredSize().height
									+ hr.getPreferredSize().height + 4 * PAD)));
					viewPort.getComponent().setBackground(table.getBackground());
					add(viewPort.getComponent(), BorderLayout.CENTER);

					setVisible(true);
				}

				private JTable createTable() {
					final String[] columnNames = { "Nazwa przedmiotu", "Ile sztuk", "Zdobyto" };

					final List<String> items = Arrays.asList(event.get("dropped_items").split(";"));

					final Object[][] data = new Object[items.size()][];
					int i = 0;
					for (final String e: items) {
						data[i] = createDataRow(e.split(","));
						i++;
					}

					return new JTable(data, columnNames);
				}

				private Object[] createDataRow(final String[] item) {
					final Object[] rval = new Object[4];

					final String name = item[0];
					final String itemDropCount = item[2];

					rval[0] = name;
					rval[1] = "";
					rval[2] = "";

					if (Integer.parseInt(itemDropCount) > 0) {
						rval[1] = itemDropCount;
					}
					if (item[1].equals("true")) {
						rval[2] = "✔";
					}

					return rval;
				}

				/**
				 * Adjust the column widths of a table based on the table contents.
				 *
				 * @param table adjusted table
				 */
				private void adjustColumnWidths(JTable table) {
					final TableColumnModel model = table.getColumnModel();
					for (int column = 0; column < table.getColumnCount(); column++) {
						TableColumn tc = model.getColumn(column);
						int width = tc.getWidth();
						for (int row = 0; row < table.getRowCount(); row++) {
							Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
							width = Math.max(width, comp.getPreferredSize().width);
						}

						tc.setPreferredWidth(width);
					}
				}

				/**
				 * Adjust the row heights of a table based on the table contents.
				 *
				 * @param table adjusted table
				 */
				private void adjustRowHeights(JTable table) {
					for (int row = 0; row < table.getRowCount(); row++) {
						int rowHeight = table.getRowHeight();

						for (int column = 0; column < table.getColumnCount(); column++) {
							Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
							rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
						}

						table.setRowHeight(row, rowHeight);
					}
				}
			};

			new ImageViewWindow("Spis Przedmiotów", panel);
		} else {
			logger.warn("Could not create item log: Event does not have \"dropped_items\" attribute");
		}
	}
}