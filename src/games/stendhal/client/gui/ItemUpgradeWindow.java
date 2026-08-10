/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Item;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.constants.Actions;
import games.stendhal.common.constants.ItemRarity;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;

/** Desktop Item Upgrades 2.0 window. Values always come from the server. */
public final class ItemUpgradeWindow extends InternalManagedWindow {
	private static final long serialVersionUID = 1L;
	private static final Color ACCENT = new Color(255, 190, 64);
	private static final Color AVAILABLE = new Color(92, 190, 105);
	private static final Color UNAVAILABLE = new Color(225, 90, 75);
	private static final Color PANEL_BORDER = new Color(130, 106, 75);
	private static final Sprite SLOT_BACKGROUND =
			SpriteStore.get().getSprite("data/gui/slot.png");
	private static ItemUpgradeWindow instance;

	private final ItemIcon icon = new ItemIcon();
	private final JLabel itemName = centered("Przeciągnij tutaj przedmiot");
	private final JLabel level = centered("Przedmiot pozostanie w ekwipunku.");
	private final JPanel stats = new JPanel();
	private final JLabel chance = centered("");
	private final JLabel fee = centered("");
	private final JPanel materials = new JPanel();
	private final JLabel status = centered("Rozmawiaj z kowalem, aby odświeżyć okno.");
	private final JButton upgrade = new JButton("Ulepsz");
	private final JButton refresh = new JButton("Odśwież");

	private int npcId;
	private String selectedPath;
	private String requestToken;
	private final Set<String> candidatePaths = new HashSet<String>();

	private ItemUpgradeWindow() {
		super("item-upgrade", "Ulepszanie przedmiotu");
		setCloseable(true);
		setMinimizable(false);
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
					if (!event.has("phase") || !"open".equals(event.get("phase"))) {
						return;
					}
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
		content.setPreferredSize(new Dimension(380, 400));

		final JPanel top = new JPanel(new BorderLayout(6, 6));
		top.add(sectionTitle("Przedmiot do ulepszenia"), BorderLayout.NORTH);

		final JPanel identity = new JPanel(new BorderLayout(8, 2));
		identity.setBorder(cardBorder());
		icon.setBorder(BorderFactory.createLineBorder(PANEL_BORDER));
		identity.add(icon, BorderLayout.WEST);
		final JPanel names = new JPanel(new GridLayout(0, 1));
		names.add(itemName);
		names.add(level);
		identity.add(names, BorderLayout.CENTER);
		top.add(identity, BorderLayout.CENTER);
		content.add(top, BorderLayout.NORTH);

		final JPanel center = new JPanel(new GridBagLayout());
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.insets = new Insets(0, 0, 3, 0);

		constraints.gridy = 0;
		center.add(sectionTitle("Rezultat następnego ulepszenia"), constraints);
		stats.setLayout(new GridLayout(0, 1, 2, 2));
		stats.setBorder(cardBorder());
		constraints.gridy++;
		constraints.insets = new Insets(0, 0, 8, 0);
		center.add(stats, constraints);

		constraints.gridy++;
		constraints.insets = new Insets(0, 0, 3, 0);
		center.add(sectionTitle("Koszt i szansa"), constraints);
		final JPanel requirements = new JPanel(new GridLayout(0, 1, 2, 2));
		requirements.setBorder(cardBorder());
		requirements.add(chance);
		requirements.add(fee);
		constraints.gridy++;
		constraints.insets = new Insets(0, 0, 8, 0);
		center.add(requirements, constraints);

		constraints.gridy++;
		constraints.insets = new Insets(0, 0, 3, 0);
		center.add(sectionTitle("Materiały — posiadasz / wymagane"), constraints);
		materials.setLayout(new GridLayout(0, 1, 2, 2));
		materials.setBorder(cardBorder());
		constraints.gridy++;
		constraints.insets = new Insets(0, 0, 0, 0);
		center.add(materials, constraints);

		constraints.gridy++;
		constraints.weighty = 1.0;
		constraints.fill = GridBagConstraints.BOTH;
		center.add(new JPanel(), constraints);
		content.add(center, BorderLayout.CENTER);

		final JPanel bottom = new JPanel(new BorderLayout(4, 4));
		status.setVerticalAlignment(SwingConstants.TOP);
		status.setBorder(cardBorder());
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
		if (event.has("npc_id")) {
			npcId = event.getInt("npc_id");
		}
		final String state = event.has("status") ? event.get("status") : "";
		final boolean preservePreview = !event.has("name")
				&& isInteractionStatus(state) && selectedPath != null;
		if (event.has("selected_path")) {
			selectedPath = event.get("selected_path");
		} else if (!preservePreview) {
			selectedPath = null;
		}
		requestToken = event.has("request_token") ? event.get("request_token") : null;

		if (event.has("candidate_paths")) {
			candidatePaths.clear();
			candidatePaths.addAll(list(event, "candidate_paths"));
		} else if (!preservePreview) {
			candidatePaths.clear();
		}

		if (event.has("name")) {
			itemName.setText(colorName(event.get("name"),
					event.has("rarity_id") ? event.get("rarity_id") : "common"));
			level.setText("Poziom ulepszenia: +" + event.getInt("upgrade_level")
					+ " / +" + event.getInt("max_upgrade_level"));
			icon.setItem(event.get("class"), event.get("subclass"));
		} else if (!preservePreview) {
			itemName.setText("NO_UPGRADEABLE_ITEMS".equals(state)
					? "Brak przedmiotu do ulepszenia"
					: "Przeciągnij tutaj przedmiot");
			level.setText("Przedmiot pozostanie w ekwipunku.");
			icon.setItem(null, null);
		}
		if (event.has("name") || !preservePreview) {
			updateStats(event);
			chance.setText(event.has("success_percent")
					? htmlValue("Szansa powodzenia", event.getInt("success_percent") + "%")
					: htmlValue("Szansa powodzenia", "—"));
			fee.setText(event.has("fee_text")
					? htmlValue("Koszt", event.get("fee_text"))
					: htmlValue("Koszt", "—"));
			updateMaterials(event);
		}

		final boolean canUpgrade = event.has("can_upgrade")
				&& event.getInt("can_upgrade") == 1 && requestToken != null;
		upgrade.setEnabled(canUpgrade);
		refresh.setEnabled(selectedPath != null && !isInteractionStatus(state));
		final String message = event.has("message") ? event.get("message") : "";
		status.setText(message.length() > 0 ? message : statusText(state));
		status.setForeground("SUCCESS".equals(state) || "READY".equals(state)
				? AVAILABLE : "SELECT_ITEM".equals(state) ? ACCENT : UNAVAILABLE);
		upgrade.setToolTipText(canUpgrade ? "Wykonaj próbę ulepszenia" : statusText(state));
		revalidate();
		repaint();
	}

	private void updateStats(final RPEvent event) {
		stats.removeAll();
		final List<String> names = list(event, "stat_names");
		final List<String> current = list(event, "current_stat_values");
		final List<String> upgradedValues = list(event, "upgraded_stat_values");
		final Map<String, String> currentByName = valuesByName(names, current);
		final Map<String, String> upgradedByName = valuesByName(names, upgradedValues);
		if (currentByName.containsKey("damage_min")
				&& currentByName.containsKey("damage_max")) {
			stats.add(statRow("Obrażenia",
					currentByName.get("damage_min") + "–" + currentByName.get("damage_max"),
					upgradedByName.get("damage_min") + "–" + upgradedByName.get("damage_max")));
		}
		for (int index = 0; index < names.size(); index++) {
			final String name = names.get(index);
			if (("damage_min".equals(name) || "damage_max".equals(name))
					&& currentByName.containsKey("damage_min")
					&& currentByName.containsKey("damage_max")) {
				continue;
			}
			if (index < current.size() && index < upgradedValues.size()) {
				stats.add(statRow(statLabel(name), current.get(index),
						upgradedValues.get(index)));
			}
		}
		if (stats.getComponentCount() == 0) {
			stats.add(centered("Po wybraniu przedmiotu zobaczysz zmianę statystyk."));
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
			row.setHorizontalAlignment(SwingConstants.CENTER);
			row.setForeground(have >= need ? AVAILABLE : UNAVAILABLE);
			materials.add(row);
		}
		if (names.isEmpty()) {
			materials.add(centered("Wymagania pojawią się po wybraniu przedmiotu."));
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

	private static String encodePath(final List<String> path) {
		return String.join("/", path);
	}

	private static List<String> list(final RPEvent event, final String name) {
		return event.has(name) ? event.getList(name) : new ArrayList<String>();
	}

	private static JLabel centered(final String text) {
		return new JLabel(text, SwingConstants.CENTER);
	}

	private static JLabel sectionTitle(final String text) {
		final JLabel label = new JLabel(text);
		label.setForeground(ACCENT);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		return label;
	}

	private static javax.swing.border.Border cardBorder() {
		return BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(PANEL_BORDER),
				BorderFactory.createEmptyBorder(5, 6, 5, 6));
	}

	private static JLabel statRow(final String name, final String current,
			final String upgraded) {
		return centered("<html><b>" + name + ":</b>&nbsp; " + current
				+ " &nbsp;→&nbsp; <font color='#5cbe69'><b>" + upgraded
				+ "</b></font></html>");
	}

	private static String htmlValue(final String label, final String value) {
		return "<html><b>" + label + ":</b>&nbsp; " + value + "</html>";
	}

	private static Map<String, String> valuesByName(final List<String> names,
			final List<String> values) {
		final Map<String, String> result = new LinkedHashMap<String, String>();
		for (int index = 0; index < names.size() && index < values.size(); index++) {
			result.put(names.get(index), values.get(index));
		}
		return result;
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
		if ("SELECT_ITEM".equals(state)) return "Przeciągnij przedmiot z ekwipunku do slotu.";
		if ("NO_UPGRADEABLE_ITEMS".equals(state)) return "Nie masz przedmiotu, który można ulepszyć.";
		if ("READY".equals(state)) return "Wymagania są spełnione.";
		if ("NOT_ENOUGH_MONEY".equals(state)) return "Brakuje pieniędzy.";
		if ("MISSING_RESOURCES".equals(state)) return "Brakuje materiałów.";
		if ("MAX_LEVEL".equals(state)) return "Osiągnięto maksymalny poziom.";
		if ("STALE_PREVIEW".equals(state)) return "Podgląd wygasł — odświeżono stan.";
		if ("NPC_BUSY".equals(state)) return "Kowal rozmawia teraz z innym graczem.";
		if ("NPC_NOT_ATTENDING".equals(state)) return "Najpierw rozpocznij rozmowę z kowalem.";
		if ("NPC_TOO_FAR".equals(state)) return "Musisz pozostać w zasięgu rozmowy z kowalem.";
		if ("FAILURE".equals(state)) return "Próba ulepszenia nie powiodła się.";
		return "Nie można teraz ulepszyć przedmiotu.";
	}

	private static boolean isInteractionStatus(final String state) {
		return "NPC_BUSY".equals(state) || "NPC_NOT_ATTENDING".equals(state)
				|| "NPC_TOO_FAR".equals(state);
	}

	private static String colorName(final String name, final String rarityId) {
		final ItemRarity rarity = ItemRarity.fromIdOrCommon(rarityId);
		return "<html><b><font color='" + rarity.getColorHex() + "'>"
				+ name + "</font></b></html>";
	}

	private final class ItemIcon extends JComponent implements DropTarget {
		private static final long serialVersionUID = 1L;
		private Sprite sprite;

		private ItemIcon() {
			setPreferredSize(new Dimension(56, 56));
			setMinimumSize(new Dimension(56, 56));
			setToolTipText("Przeciągnij tutaj przedmiot z ekwipunku.");
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
			SLOT_BACKGROUND.draw(graphics,
					(getWidth() - SLOT_BACKGROUND.getWidth()) / 2,
					(getHeight() - SLOT_BACKGROUND.getHeight()) / 2);
			if (sprite != null) {
				sprite.draw(graphics, (getWidth() - sprite.getWidth()) / 2,
						(getHeight() - sprite.getHeight()) / 2);
			}
		}

		@Override
		public boolean canAccept(final IEntity entity) {
			return entity instanceof Item
					&& candidatePaths.contains(encodePath(entity.getPath()));
		}

		@Override
		public void dropEntity(final IEntity entity, final int amount,
				final Point point) {
			if (!canAccept(entity)) {
				return;
			}
			selectedPath = encodePath(entity.getPath());
			requestToken = null;
			upgrade.setEnabled(false);
			refresh.setEnabled(false);
			status.setForeground(ACCENT);
			status.setText("Pobieranie podglądu z serwera…");
			send("preview", selectedPath, null);
		}
	}
}
