/***************************************************************************
 *                      (C) Copyright 2020 - Stendhal                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.achievementlog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import games.stendhal.client.sprite.Sprite;

public class AchievementLogRenderers {
	private static AchievementLogRenderers instance;
	private static final int PAD = AchievementLog.PAD;

	public static synchronized AchievementLogRenderers get() {
		if (instance == null) {
			instance = new AchievementLogRenderers();
		}
		return instance;
	}

	@SuppressWarnings("serial")
	public static class HeaderRenderer extends JPanel implements TableCellRenderer {
		private final JLabel header = new JLabel();

		/**
		 * Create a new HeaderRenderer.
		 */
		public HeaderRenderer() {
			add(header);
			header.setBorder(null);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			header.setText(value.toString());
			return this;
		}
	}

	@SuppressWarnings("serial")
	public class DescriptionCellRenderer extends DefaultTableCellRenderer {
		private final Border border = BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD);

		@Override
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			super.getTableCellRendererComponent(table, value, isSelected,
					hasFocus, row, column);
			setBorder(border);
			return this;
		}
	}

	@SuppressWarnings("serial")
	public class SpriteCellRenderer extends JComponent implements TableCellRenderer {
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
}
