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
import java.util.regex.Pattern;

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
	/** Pattern matching HTML break tags. */
	private static final Pattern BREAK_PATTERN = Pattern.compile("(?is)<br\\s*/?>");

	/** Pattern matching any HTML tag. */
	private static final Pattern TAG_PATTERN = Pattern.compile("(?is)<[^>]+>");

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
	private final List<String> activeTooltips;
	private String currentTooltipHtml;
	private String currentTooltipPlain;
	private String awayMessage;
	private String grumpyMessage;

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

				activeTooltips = new ArrayList<String>();

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
				activeTooltips.clear();
				if (eating.isVisible()) {
						addNormalizedTooltip(activeTooltips, eating.getToolTipText());
				}
				if (choking.isVisible()) {
						addNormalizedTooltip(activeTooltips, choking.getToolTipText());
				}
				for (Map.Entry<StatusID, JLabel> entry : statusIDMap.entrySet()) {
						JLabel status = entry.getValue();
						if (status.isVisible()) {
								addNormalizedTooltip(activeTooltips, status.getToolTipText());
						}
				}
				if (away.isVisible()) {
						addNormalizedTooltip(activeTooltips, away.getToolTipText());
				}
				if (grumpy.isVisible()) {
						addNormalizedTooltip(activeTooltips, grumpy.getToolTipText());
				}
				if (activeTooltips.isEmpty()) {
						if (currentTooltipHtml != null) {
								currentTooltipHtml = null;
								setToolTipText(null);
						}
						if ((getAccessibleContext() != null) && (currentTooltipPlain != null)) {
								currentTooltipPlain = null;
								getAccessibleContext().setAccessibleDescription(null);
						}
						return;
				}
				StringBuilder html = new StringBuilder("<html><b>Aktywne statusy:</b><br>");
				StringBuilder plain = new StringBuilder("Aktywne statusy: ");
				for (int i = 0; i < activeTooltips.size(); i++) {
						if (i > 0) {
								html.append("<br>");
								plain.append("; ");
						}
						String entryText = activeTooltips.get(i);
						html.append(entryText.replace("\n", "<br>"));
						plain.append(entryText.replace('\n', ' '));
				}
				html.append("</html>");
				String newHtml = html.toString();
				if (!newHtml.equals(currentTooltipHtml)) {
						currentTooltipHtml = newHtml;
						setToolTipText(newHtml);
				}
				if (getAccessibleContext() != null) {
						String newPlain = plain.toString();
						if (!newPlain.equals(currentTooltipPlain)) {
								currentTooltipPlain = newPlain;
								getAccessibleContext().setAccessibleDescription(newPlain);
						}
				}
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
				normalized = BREAK_PATTERN.matcher(normalized).replaceAll("\n");
				normalized = TAG_PATTERN.matcher(normalized).replaceAll("");
				normalized = normalized.replace("&nbsp;", " ");
				return normalized.trim();
		}

	/**
	 * Display or hide eating icon
	 *
	 * @param isEating
	 */
		protected void setEating(boolean isEating) {
				boolean changed = false;
				if (isEating) {
						if (choking.isVisible()) {
								if (eating.isVisible()) {
										eating.setVisible(false);
										changed = true;
								}
						} else if (!eating.isVisible()) {
								eating.setVisible(true);
								changed = true;
						}
				} else if (eating.isVisible()) {
						eating.setVisible(false);
						changed = true;
				}
				if (changed) {
						updatePanelTooltip();
				}
		}

	/**
	 * Display or hide choking icon
	 *
	 * @param isChoking
	 */
		protected void setChoking(boolean isChoking) {
				boolean changed = false;
				if (choking.isVisible() != isChoking) {
						choking.setVisible(isChoking);
						changed = true;
				}
				// A hack to prevent eating and choking icons appearing
				// at the same time
				if (isChoking && eating.isVisible()) {
						eating.setVisible(false);
						changed = true;
				}
				if (changed) {
						updatePanelTooltip();
				}
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
						updatePanelTooltip();
				}
		}

	/**
	 * Hide all status icons. This is called when the user entity is deleted.
	 */
		void resetStatuses() {
				boolean changed = false;
				for (JLabel status : statusIDMap.values()) {
						if (status.isVisible()) {
								status.setVisible(false);
								changed = true;
						}
				}
				if (changed) {
						updatePanelTooltip();
				}
		}

	/**
	 * Set the away status message. null value will hide the icon.
	 *
	 * @param message
	 */
		void setAway(String message) {
				boolean isAway = message != null;
				boolean changed = false;
				if (isAway) {
						if ((awayMessage == null) || !message.equals(awayMessage)) {
								awayMessage = message;
								String tooltip = "<html>Jesteś oddalony z wiadomością:<br><b>" + message;
								away.setToolTipText(tooltip);
								away.getAccessibleContext().setAccessibleDescription(tooltip);
								changed = true;
						}
				} else if (awayMessage != null) {
						awayMessage = null;
						away.setToolTipText(null);
						away.getAccessibleContext().setAccessibleDescription(null);
						changed = true;
				}
				if (away.isVisible() != isAway) {
						away.setVisible(isAway);
						changed = true;
				}
				if (changed) {
						updatePanelTooltip();
				}
		}

	/**
	 * Set the grumpy status message. null value will hide the icon.
	 *
	 * @param message
	 */
		void setGrumpy(String message) {
				boolean isGrumpy = message != null;
				boolean changed = false;
				if (isGrumpy) {
						if ((grumpyMessage == null) || !message.equals(grumpyMessage)) {
								grumpyMessage = message;
								String tooltip = "<html>Jesteś niedostępny z wiadomością:<br><b>" + message;
								grumpy.setToolTipText(tooltip);
								grumpy.getAccessibleContext().setAccessibleDescription(tooltip);
								changed = true;
						}
				} else if (grumpyMessage != null) {
						grumpyMessage = null;
						grumpy.setToolTipText(null);
						grumpy.getAccessibleContext().setAccessibleDescription(null);
						changed = true;
				}
				if (grumpy.isVisible() != isGrumpy) {
						grumpy.setVisible(isGrumpy);
						changed = true;
				}
				if (changed) {
						updatePanelTooltip();
				}
		}
}
