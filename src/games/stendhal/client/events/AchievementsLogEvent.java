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
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import games.stendhal.client.GameScreen;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.ScrolledViewport;
import games.stendhal.client.gui.imageviewer.ImageViewWindow;
import games.stendhal.client.gui.imageviewer.ItemListImageViewerEvent.HeaderRenderer;
import games.stendhal.client.gui.imageviewer.ViewPanel;

/**
 * @author KarajuSs
 */
class AchievementsLogEvent extends Event<RPEntity> {
	private static final int PAD = 5;

	/**
	 * executes the event
	 */
	@Override
	public void execute() {
		new ImageViewWindow("Dziennik osiągnięć", createViewPanel());
	}

	private ViewPanel createViewPanel() {
		return new ViewPanel() {
			@Override
			public void prepareView(Dimension maxSize) {
				Dimension screenSize = GameScreen.get().getSize();
				final int maxPreferredWidth = screenSize.width - 80;

				final JTable table = createTable();

				table.setEnabled(false);
				table.setFillsViewportHeight(true);
				table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
				TableColumn col = table.getColumnModel().getColumn(0);
				col.setCellRenderer(new DefaultTableCellRenderer());

				DefaultTableCellRenderer r = new DefaultTableCellRenderer();
				r.setHorizontalAlignment(SwingConstants.CENTER);

				col = table.getColumnModel().getColumn(1);
				col.setCellRenderer(new SpriteCellRenderer());

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
						Math.min(screenSize.height - 100, table.getPreferredSize().height
								+ hr.getPreferredSize().height + 4 * PAD)));
				viewPort.getComponent().setBackground(table.getBackground());
				add(viewPort.getComponent(), BorderLayout.CENTER);

				setVisible(true);
			}

			private JTable createTable() {
				final String[] columnNames = { "Nazwa", "Podgląd", "Cena" };

				return null;
			}

			private void adjustColumnWidths(JTable table) {
				//nothing
			}
			private void adjustRowHeights(JTable table) {
				//nothing
			}
		};
	}

	private static class SpriteCellRenderer extends JComponent implements TableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
