/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.stats;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import games.stendhal.client.entity.StatusID;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.sprite.DataLoader;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

class StatusIconPanel extends JComponent {
    /** Status bar icons */
    private static final String iconFolder = "data/sprites/status/panel/";

	private static final ImageIcon eatingIcon = new ImageIcon(DataLoader.getResource("data/sprites/ideas/eat.png"));
	private static final ImageIcon chokingIcon = new ImageIcon(DataLoader.getResource("data/sprites/ideas/choking.png"));

	private final static Sprite awaySprite, grumpySprite;
	static {
		final SpriteStore store = SpriteStore.get();
		awaySprite = store.getSprite("data/sprites/ideas/away.png");
		grumpySprite = store.getSprite("data/sprites/ideas/grumpy.png");
	}

	final JLabel eating, choking;
	final AnimatedIcon away, grumpy;
    private final Map<StatusID, JLabel> statusIDMap;

	protected StatusIconPanel() {
		setLayout(new SBoxLayout(SBoxLayout.HORIZONTAL));
		setOpaque(false);

		eating = new JLabel(eatingIcon);
		eating.setToolTipText("Status: Jedzenie posiłku");
		eating.getAccessibleContext().setAccessibleDescription("Status: Jedzenie posiłku");
		add(eating);
		eating.setVisible(false);

		choking = new JLabel(chokingIcon);
		choking.setToolTipText("Status: Duszenie się");
		choking.getAccessibleContext().setAccessibleDescription("Status: Duszenie się");
		add(choking);
		choking.setVisible(false);

		away = new AnimatedIcon(awaySprite, 1000);
		add(away);
		away.setVisible(false);

		grumpy = new AnimatedIcon(grumpySprite, 500);
		add(grumpy);
		grumpy.setVisible(false);

		/** Initialize map */
		statusIDMap = new EnumMap<StatusID, JLabel>(StatusID.class);
		addStatusIndicator(StatusID.CONFUSE, "confuse", "Status: Oszołomienie");
		addStatusIndicator(StatusID.POISON, "poison", "Status: Zatrucie");
		addStatusIndicator(StatusID.BLEED, "bleed", "Status: Krwawienie");
		addStatusIndicator(StatusID.SHOCK, "shock", "Status: Porażenie");
		addStatusIndicator(StatusID.ZOMBIE, "zombie", "Status: Przemiana w zombie");
		addStatusIndicator(StatusID.HEAVY, "heavy", "Status: Przeciążenie");
		updatePanelTooltip();
	}

	/**
	 * Create and add a status indicator label.
	 *
	 * @param identifier string identifier used to look up for the label icon
	 * @return the created label
	 */
	private void addStatusIndicator(StatusID id, String identifier, String tooltip) {
		JLabel label = createStatusIndicator(identifier, tooltip);
		statusIDMap.put(id, label);
	}

	private JLabel createStatusIndicator(String identifier, String tooltip) {
		Icon icon = new ImageIcon(DataLoader.getResource(iconFolder + identifier + ".png"));
		JLabel label = new JLabel(icon);
		label.setVisible(false);
		if ((tooltip != null) && !tooltip.isEmpty()) {
			label.setToolTipText(tooltip);
			label.getAccessibleContext().setAccessibleDescription(tooltip);
		}
		add(label);

		return label;
	}

	private void updatePanelTooltip() {
		List<String> active = new ArrayList<String>();
		if (eating.isVisible()) {
			addNormalizedTooltip(active, eating.getToolTipText());
		}
		if (choking.isVisible()) {
			addNormalizedTooltip(active, choking.getToolTipText());
		}
		for (Map.Entry<StatusID, JLabel> entry : statusIDMap.entrySet()) {
			JLabel status = entry.getValue();
			if (status.isVisible()) {
				addNormalizedTooltip(active, status.getToolTipText());
			}
		}
		if (away.isVisible()) {
			addNormalizedTooltip(active, away.getToolTipText());
		}
		if (grumpy.isVisible()) {
			addNormalizedTooltip(active, grumpy.getToolTipText());
		}
		if (active.isEmpty()) {
			setToolTipText(null);
			getAccessibleContext().setAccessibleDescription(null);
			return;
		}
		StringBuilder html = new StringBuilder("<html><b>Aktywne statusy:</b><br>");
		StringBuilder plain = new StringBuilder("Aktywne statusy: ");
		for (int i = 0; i < active.size(); i++) {
			if (i > 0) {
				html.append("<br>");
				plain.append("; ");
			}
			String entryText = active.get(i);
			html.append(entryText.replace("\n", "<br>"));
			plain.append(entryText.replace('\n', ' '));
		}
		html.append("</html>");
		setToolTipText(html.toString());
		getAccessibleContext().setAccessibleDescription(plain.toString());
	}

	private static void addNormalizedTooltip(List<String> target, String tooltip) {
		String normalized = normalizeTooltip(tooltip);
		if ((normalized != null) && !normalized.isEmpty()) {
			target.add(normalized);
		}
	}

	private static String normalizeTooltip(String tooltip) {
		if ((tooltip == null) || tooltip.isEmpty()) {
			return null;
		}
		String normalized = tooltip;
		normalized = normalized.replaceAll("(?is)<br\\s*/?>", "\n");
		normalized = normalized.replaceAll("(?is)<[^>]+>", "");
		normalized = normalized.replace("&nbsp;", " ");
		return normalized.trim();
	}

	/**
	 * Display or hide eating icon
	 *
	 * @param isEating
	 */
	protected void setEating(boolean isEating) {
		if (eating.isVisible() != isEating) {
			// A hack to prevent eating and choking icons appearing
			// at the same time
			if (isEating) {
				if (!choking.isVisible()) {
					eating.setVisible(true);
				}
			} else {
				eating.setVisible(false);
			}
		}
		updatePanelTooltip();
	}

	/**
	 * Display or hide choking icon
	 *
	 * @param isChoking
	 */
	protected void setChoking(boolean isChoking) {
		if (choking.isVisible() != isChoking) {
			choking.setVisible(isChoking);
		}
		// A hack to prevent eating and choking icons appearing
		// at the same time
		if (isChoking) {
			eating.setVisible(false);
		}
		updatePanelTooltip();
	}

	/**
	 * Display or hide a status icon.
	 *
	 * @param ID
	 *	The ID value of the status
	 * @param visible
	 *	Show the icon
	 */
	void setStatus(final StatusID ID, final boolean visible) {
		final JLabel status = statusIDMap.get(ID);
		if (status.isVisible() != visible) {
			status.setVisible(visible);
		}
		updatePanelTooltip();
	}

	/**
	 * Hide all status icons. This is called when the user entity is deleted.
	 */
	void resetStatuses() {
		for (JLabel status : statusIDMap.values()) {
			if (status.isVisible()) {
				status.setVisible(false);
			}
		}
		updatePanelTooltip();
	}

	/**
	 * Set the away status message. null value will hide the icon.
	 *
	 * @param message
	 */
	void setAway(String message) {
		boolean isAway = message != null;
		if (isAway) {
			String tooltip = "<html>Jesteś oddalony z wiadomością:<br><b>" + message;
			away.setToolTipText(tooltip);
			away.getAccessibleContext().setAccessibleDescription(tooltip);
		} else {
			away.setToolTipText(null);
			away.getAccessibleContext().setAccessibleDescription(null);
		}
		if (away.isVisible() != isAway) {
			away.setVisible(isAway);
		}
		updatePanelTooltip();
	}

	/**
	 * Set the grumpy status message. null value will hide the icon.
	 *
	 * @param message
	 */
	void setGrumpy(String message) {
		boolean isGrumpy = message != null;
		if (isGrumpy) {
			String tooltip = "<html>Jesteś niedostępny z wiadomością:<br><b>" + message;
			grumpy.setToolTipText(tooltip);
			grumpy.getAccessibleContext().setAccessibleDescription(tooltip);
		} else {
			grumpy.setToolTipText(null);
			grumpy.getAccessibleContext().setAccessibleDescription(null);
		}
		if (grumpy.isVisible() != isGrumpy) {
			grumpy.setVisible(isGrumpy);
		}
		updatePanelTooltip();
	}
}
