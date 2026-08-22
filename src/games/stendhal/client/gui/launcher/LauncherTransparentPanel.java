/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.client.gui.launcher;

import java.awt.LayoutManager;

import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicPanelUI;

/**
 * Transparent panel isolated from the textured Look & Feel used by the game.
 */
@SuppressWarnings("serial")
public class LauncherTransparentPanel extends JPanel {

	public LauncherTransparentPanel() {
		super();
		setOpaque(false);
	}

	public LauncherTransparentPanel(final LayoutManager layout) {
		super(layout);
		setOpaque(false);
	}

	@Override
	public void updateUI() {
		setUI(new BasicPanelUI());
	}
}
