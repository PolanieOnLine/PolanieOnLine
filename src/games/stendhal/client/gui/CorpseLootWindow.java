/* Copyright (C) 2026 PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import java.awt.Dimension;

/** Original compact slot window with single-line corpse names. */
public final class CorpseLootWindow extends SlotWindow {

	private static final long serialVersionUID = 1L;

	public CorpseLootWindow(final int columns, final int rows) {
		super("corpse", columns, rows);
		setMinimizable(false);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		size.width = Math.max(108, size.width);
		return size;
	}

	/** Plain labels abbreviate overflowing names with "..."; hover shows all. */
	public void setLootTitle(final String title) {
		titleLabel.putClientProperty(UiTranslation.PRESERVE_TEXT, true);
		titleLabel.putClientProperty("html.disable", true);
		titleLabel.setText(title);
		titleLabel.setToolTipText(title);
		getTitlebar().setToolTipText(title);
		// Keep the compact reference width; larger grids retain their own width.
		setSize(getPreferredSize());
		revalidate();
		repaint();
	}
}
