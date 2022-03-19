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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import games.stendhal.client.GameScreen;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.ScrolledViewport;
import games.stendhal.client.gui.imageviewer.ImageViewWindow;
import games.stendhal.client.gui.imageviewer.ItemListImageViewerEvent.HeaderRenderer;
import games.stendhal.client.gui.imageviewer.ViewPanel;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

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
					TableColumn col = table.getColumnModel().getColumn(1);
					col.setCellRenderer(new DefaultTableCellRenderer());

					DefaultTableCellRenderer r = new DefaultTableCellRenderer();
					r.setHorizontalAlignment(SwingConstants.CENTER);

					col = table.getColumnModel().getColumn(0);
					col.setCellRenderer(new SpriteCellRenderer());
					col = table.getColumnModel().getColumn(2);
					col.setCellRenderer(r);
					col = table.getColumnModel().getColumn(3);
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
					final String[] columnNames = { "#" ,"Nazwa przedmiotu", "Ile sztuk", "Zdobyto" };

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
					final String clazz = item[3];
					final String subclazz = item[4];

					boolean dropped = false;
					if (item[1].equals("true")) {
						dropped = true;
					}

					rval[0] = getItemImage(clazz, subclazz, dropped);
					rval[1] = name;
					rval[2] = "";
					rval[3] = "";

					if (dropped) {
						rval[2] = itemDropCount;
						rval[3] = "✔";
					}

					return rval;
				}

				private Sprite getItemImage(String clazz, String subclazz, boolean dropped) {
					String imagePath = "/data/sprites/items/" + clazz + "/" + subclazz + ".png";

					Sprite sprite;
					sprite = SpriteStore.get().getColoredSprite("/data/gui/bag.png", Color.LIGHT_GRAY);
					if (dropped) {
						sprite = SpriteStore.get().getSprite(imagePath);
					}

					if (sprite.getWidth() > sprite.getHeight()) {
						sprite = SpriteStore.get().getAnimatedSprite(sprite, 100);
					}

					return sprite;
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

				@SuppressWarnings("serial")
				class SpriteCellRenderer extends JComponent implements TableCellRenderer {
					private Sprite sprite;

					@Override
					public Component getTableCellRendererComponent(JTable table, Object color,
							boolean isSelected, boolean hasFocus, int row, int col) {
						Object obj = table.getValueAt(row, col);
						if (obj instanceof Sprite) {
							sprite = (Sprite) obj;
						} else {
							sprite = null;
						}
						return this;
					}

					@Override
					public Dimension getPreferredSize() {
						Dimension d = new Dimension();
						if (sprite != null) {
							d.width = sprite.getWidth() + 2 * PAD;
							d.height = sprite.getHeight() + 2 * PAD;
						}
						return d;
					}

					@Override
					protected void paintComponent(Graphics g) {
						if (sprite != null) {
							sprite.draw(g, (getWidth() - sprite.getWidth()) / 2, (getHeight() - sprite.getHeight()) / 2);
						}
					}
				}
			};

			new ImageViewWindow("Spis Przedmiotów", panel);
		} else {
			logger.warn("Could not create item log: Event does not have \"dropped_items\" attribute");
		}
	}
}