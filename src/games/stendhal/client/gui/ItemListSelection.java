package games.stendhal.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/** Shared translucent selection used by merchant and item service catalogues. */
final class ItemListSelection {
	private ItemListSelection() { }

	static void paint(final Graphics graphics, final int width, final int height,
			final boolean selected, final boolean hovered) {
		if (selected || hovered) {
			graphics.setColor(new Color(255, 255, 255, selected ? 31 : 15));
			graphics.fillRect(0, 0, width, height);
		}
	}

	static class Renderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		private boolean selected;

		@Override
		public Component getListCellRendererComponent(final JList<?> list, final Object value,
				final int index, final boolean selected, final boolean focus) {
			super.getListCellRendererComponent(list, value, index, selected, focus);
			this.selected = selected;
			setOpaque(false);
			return this;
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			ItemListSelection.paint(graphics, getWidth(), getHeight(), selected, false);
			super.paintComponent(graphics);
		}
	}
}
