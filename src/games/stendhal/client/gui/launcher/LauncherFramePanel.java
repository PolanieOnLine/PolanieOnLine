/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.client.gui.launcher;

import java.awt.Graphics;
import java.awt.Graphics2D;

/** Panel with the shared, programmatically drawn launcher frame. */
@SuppressWarnings("serial")
public class LauncherFramePanel extends LauncherTransparentPanel {

	@Override
	protected void paintComponent(final Graphics graphics) {
		super.paintComponent(graphics);
		final Graphics2D g2 = (Graphics2D) graphics.create();
		LauncherTheme.paintFrame(g2, 0, 0, getWidth(), getHeight(), 8);
		g2.dispose();
	}
}
