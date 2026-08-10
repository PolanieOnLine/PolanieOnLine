/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.constants.Actions;
import games.stendhal.common.constants.ItemRarity;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;

/** Desktop Item Upgrades 2.0 window. Values always come from the server. */
public final class ItemUpgradeWindow extends InternalManagedWindow {
	private static final long serialVersionUID = 1L;
	private static ItemUpgradeWindow instance;

	private final JComboBox<Candidate> candidates = new JComboBox<Candidate>();
	private final ItemIcon icon = new ItemIcon();
	private final JLabel itemName = centered("Wybierz przedmiot");
	private final JLabel level = centered("");
	private final JPanel stats = new JPanel();
	private final JLabel chance = centered("");
	private final JLabel fee = centered("");
	private final JPanel materials = new JPanel();
	private final JLabel status = centered("Rozmawiaj z kowalem, aby odświeżyć okno.");
	private final JButton upgrade = new JButton("ULEPSZ");
	private final JButton refresh = new JButton("Odśwież");

	private boolean applyingEvent;
	private int npcId;
	private String selectedPath;
	private String requestToken;

	private ItemUpgradeWindow() {
		super("item-upgrade", "ULEPSZANIE PRZEDMIOTU");
		setCloseable(true);
		setContent(buildContent());
		addCloseListener(new CloseListener() {
			@Override
			public void windowClosed(final InternalWindow window) {
				instance = null;
			}
		});
	}

	public static void show(final RPEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (instance == null) {
					instance = new ItemUpgradeWindow();
					j2DClient.get().addWindow(instance);
				}
				instance.apply(event);
				instance.setVisible(true);
				instance.raise();
			}
		});
	}

	private JComponent buildContent() {
		final JPanel content = new JPanel(new BorderLayout(8, 8));
		content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		content.setPreferredSize(new Dimension(360, 430));

		final JPanel top = new JPanel(new BorderLayout(6, 6));
		top.add(new JLabel("Przedmiot do ulepszenia:"), BorderLayout.NORTH);
		candidates.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				if (!applyingEvent && candidates.getSelectedItem() != null) {
					send("preview", ((Candidate) candidates.getSelectedItem()).path, null);
				}
			}
		});
		top.add(candidates, BorderLayout.CENTER);

		final JPanel identity = new JPanel(new BorderLayout(8, 2));
		icon.setBorder(BorderFactory.createLineBorder(new Color(130, 106, 75)));
		identity.add(icon, BorderLayout.WEST);
		final JPanel names = new JPanel(new GridLayout(0, 1));
		names.add(itemName);
		names.add(level);
		identity.add(names, BorderLayout.CENTER);
		top.add(identity, BorderLayout.SOUTH);
		content.add(top, BorderLayout.NORTH);

		final JPanel center = new JPanel(new BorderLayout(6, 6));
		stats.setLayout(new GridLayout(0, 1, 2, 2));
		stats.setBorder(BorderFactory.createTitledBorder("Statystyki po ulepszeniu"));
		center.add(stats, BorderLayout.NORTH);
		final JPanel requirements = new JPanel(new BorderLayout(3, 3));
		requirements.add(chance, BorderLayout.NORTH);
		requirements.add(fee, BorderLayout.CENTER);
		materials.setLayout(new GridLayout(0, 1, 2, 2));
		materials.setBorder(BorderFactory.createTitledBorder("Materiały (posiadasz / wymagane)"));
		requirements.add(materials, BorderLayout.SOUTH);
		center.add(requirements, BorderLayout.CENTER);
		content.add(center, BorderLayout.CENTER);

		final JPanel bottom = new JPanel(new BorderLayout(4, 4));
		status.setVerticalAlignment(SwingConstants.TOP);
		bottom.add(status, BorderLayout.NORTH);
		final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				send("preview", selectedPath, null);
			}
		});
		upgrade.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				upgrade.setEnabled(false);
				send("upgrade", selectedPath, requestToken);
			}
		});
		buttons.add(refresh);
		buttons.add(upgrade);
		bottom.add(buttons, BorderLayout.SOUTH);
		content.add(bottom, BorderLayout.SOUTH);
		return content;
	}

	private void apply(final RPEvent event) {
		applyingEvent = true;
		npcId = event.has("npc_id") ? event.getInt("npc_id") : 0;
		selectedPath = event.has("selected_path") ? event.get("selected_path") : null;
		requestToken = event.has("request_token") ? event.get("request_token") : null;

		candidates.removeAllItems();
		final List<String> paths = list(event, "candidate_paths");
		final List<String> names = list(event, "candidate_names");
		for (int index = 0; index < paths.size(); index++) {
			final Candidate candidate = new Candidate(paths.get(index),
					index < names.size() ? names.get(index) : paths.get(index));
			candidates.addItem(candidate);
			if (candidate.path.equals(selectedPath)) {
				candidates.setSelectedItem(candidate);
			}
		}
		candidates.setEnabled(candidates.getItemCount() > 0);

		if (event.has("name")) {
			itemName.setText(colorName(event.get("name"),
					event.has("rarity_id") ? event.get("rarity_id") : "common"));
			level.setText("Poziom ulepszenia: +" + event.getInt("upgrade_level")
					+ " / +" + event.getInt("max_upgrade_level"));
			icon.setItem(event.get("class"), event.get("subclass"));
		} else {
			itemName.setText("Brak przedmiotu do ulepszenia");
			level.setText("");
			icon.setItem(null, null);
		}
		updateStats(event);
		chance.setText(event.has("success_percent")
				? "Szansa powodzenia: " + event.getInt("success_percent") + "%" : "");
		fee.setText(event.has("fee_text") ? "Koszt: " + event.get("fee_text") : "");
		updateMaterials(event);

		final boolean canUpgrade = event.has("can_upgrade")
				&& event.getInt("can_upgrade") == 1 && requestToken != null;
		upgrade.setEnabled(canUpgrade);
		refresh.setEnabled(selectedPath != null);
		final String message = event.has("message") ? event.get("message") : "";
		final String state = event.has("status") ? event.get("status") : "";
		status.setText(message.length() > 0 ? message : statusText(state));
		status.setForeground("SUCCESS".equals(state) ? new Color(40, 150, 55)
				: "READY".equals(state) ? Color.DARK_GRAY : new Color(175, 55, 45));
		upgrade.setToolTipText(canUpgrade ? "Wykonaj próbę ulepszenia" : statusText(state));
		applyingEvent = false;
		revalidate();
		repaint();
	}

	private void updateStats(final RPEvent event) {
		stats.removeAll();
		final List<String> names = list(event, "stat_names");
		final List<String> current = list(event, "current_stat_values");
		final List<String> upgradedValues = list(event, "upgraded_stat_values");
		for (int index = 0; index < names.size(); index++) {
			if (index < current.size() && index < upgradedValues.size()) {
				stats.add(new JLabel(statLabel(names.get(index)) + ": "
						+ current.get(index) + "  →  " + upgradedValues.get(index)));
			}
		}
	}

	private void updateMaterials(final RPEvent event) {
		materials.removeAll();
		final List<String> names = list(event, "material_names");
		final List<String> required = list(event, "material_values");
		final List<String> owned = list(event, "owned_material_values");
		for (int index = 0; index < names.size(); index++) {
			final int have = index < owned.size() ? integer(owned.get(index)) : 0;
			final int need = index < required.size() ? integer(required.get(index)) : 0;
			final JLabel row = new JLabel((have >= need ? "✓ " : "✗ ")
					+ names.get(index) + ": " + have + " / " + need);
			row.setForeground(have >= need ? new Color(35, 140, 50) : new Color(180, 50, 40));
			materials.add(row);
		}
	}

	private void send(final String command, final String path, final String token) {
		if (path == null || npcId == 0) {
			return;
		}
		final RPAction action = new RPAction();
		action.put("type", Actions.ITEM_UPGRADE);
		action.put("command", command);
		action.put("npc_id", npcId);
		action.put("target_path", decodePath(path));
		if (token != null) {
			action.put("request_token", token);
		}
		StendhalClient.get().send(action);
	}

	private static List<String> decodePath(final String path) {
		final List<String> result = new ArrayList<String>();
		for (final String part : path.split("/")) {
			result.add(part);
		}
		return result;
	}

	private static List<String> list(final RPEvent event, final String name) {
		return event.has(name) ? event.getList(name) : new ArrayList<String>();
	}

	private static JLabel centered(final String text) {
		return new JLabel(text, SwingConstants.CENTER);
	}

	private static int integer(final String value) {
		try {
			return Integer.parseInt(value);
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	private static String statLabel(final String stat) {
		if ("atk".equals(stat)) return "ATK";
		if ("ratk".equals(stat)) return "RATK";
		if ("def".equals(stat)) return "DEF";
		if ("damage_min".equals(stat)) return "Obrażenia min.";
		if ("damage_max".equals(stat)) return "Obrażenia max.";
		if ("range".equals(stat)) return "Zasięg";
		if ("rate".equals(stat)) return "Czas ataku";
		return stat;
	}

	private static String statusText(final String state) {
		if ("READY".equals(state)) return "Wymagania są spełnione.";
		if ("NOT_ENOUGH_MONEY".equals(state)) return "Brakuje pieniędzy.";
		if ("MISSING_RESOURCES".equals(state)) return "Brakuje materiałów.";
		if ("MAX_LEVEL".equals(state)) return "Osiągnięto maksymalny poziom.";
		if ("STALE_PREVIEW".equals(state)) return "Podgląd wygasł — odświeżono stan.";
		if ("NPC_TOO_FAR".equals(state)) return "Musisz pozostać przy kowalu.";
		if ("FAILURE".equals(state)) return "Próba ulepszenia nie powiodła się.";
		return "Nie można teraz ulepszyć przedmiotu.";
	}

	private static String colorName(final String name, final String rarityId) {
		final ItemRarity rarity = ItemRarity.fromIdOrCommon(rarityId);
		return "<html><b><font color='" + rarity.getColorHex() + "'>"
				+ name + "</font></b></html>";
	}

	private static final class Candidate {
		private final String path;
		private final String label;

		private Candidate(final String path, final String label) {
			this.path = path;
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}

	private static final class ItemIcon extends JComponent {
		private static final long serialVersionUID = 1L;
		private Sprite sprite;

		private ItemIcon() {
			setPreferredSize(new Dimension(48, 48));
		}

		private void setItem(final String itemClass, final String subclass) {
			if (itemClass == null || subclass == null) {
				sprite = null;
			} else {
				sprite = SpriteStore.get().getSprite("data/sprites/items/"
						+ itemClass + "/" + subclass + ".png");
				if (sprite.getWidth() > sprite.getHeight()) {
					sprite = SpriteStore.get().getAnimatedSprite(sprite, 100);
				}
			}
			repaint();
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			super.paintComponent(graphics);
			if (sprite != null) {
				sprite.draw(graphics, (getWidth() - sprite.getWidth()) / 2,
						(getHeight() - sprite.getHeight()) / 2);
			}
		}
	}
}
