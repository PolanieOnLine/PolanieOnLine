/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;
import javax.swing.Timer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.ClientLanguage;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Item;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.client.sprite.TextSprite;
import games.stendhal.common.constants.Actions;
import games.stendhal.common.constants.ItemRarity;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/** Desktop Item Upgrades 2.0 window. Values always come from the server. */
public final class ItemUpgradeWindow extends InternalManagedWindow {
	private static final long serialVersionUID = 1L;
	private static final Color ACCENT = new Color(255, 190, 64);
	private static final Color AVAILABLE = new Color(92, 190, 105);
	private static final Color UNAVAILABLE = new Color(225, 90, 75);
	private static final int CONTENT_WIDTH = 420;
	private static final int MATERIAL_CARD_WIDTH = 112;
	private static final int MATERIAL_ROW_GAP = 6;
	private static final Sprite SLOT_BACKGROUND =
			SpriteStore.get().getSprite("data/gui/slot.png");
	private static volatile ItemUpgradeWindow instance;
    private static volatile boolean openingGlyphWindow;

	private final ItemIcon icon = new ItemIcon();
	private final JLabel itemName = centered(selectionPrompt());
	private final JLabel level = centered(selectionInfo());
	private final JPanel stats = plainPanel(new GridLayout(0, 1, 0, 4));
	private final JLabel chance = centered("");
	private final JLabel fee = centered("");
	private final JPanel materials = plainPanel(new BorderLayout());
	private final JLabel status = centered("Rozmawiaj z kowalem, aby odświeżyć okno.");
	private final JButton upgrade = new JButton("Ulepsz");
	private final JButton refresh = new JButton("Odśwież");

	private int npcId;
	private boolean glyphMode;
    private boolean creatingGlyph;
    private String selectedRecipe;
    private final JPanel recipeCatalog = plainPanel(new GridBagLayout());
    private final RecipeCategory creationCategory = new RecipeCategory("Tworzenie glifów", true);
    private final RecipeCategory upgradeCategory = new RecipeCategory("Ulepszanie", false);
    private boolean updatingRecipes;
    private JPanel identitySection, statsSection, costSection;
	private String selectedPath;
	private String requestToken;
	private boolean selectionCleared;
	private final Set<String> upgradeCandidatePaths = new HashSet<String>();
    private final Map<String, Sprite> itemSprites = new LinkedHashMap<String, Sprite>();
    private String inventorySignature;
    private boolean catalogReceived;
    private final Timer inventoryRefresh = new Timer(200, event -> {
        if (glyphMode && npcId != 0) send(selectedRecipe == null ? "refresh" : "preview", null, null);
    });
    private final Timer animationTimer = new Timer(100, event -> {
        if (isShowing()) { icon.repaint(); materials.repaint(); creationCategory.rows.repaint(); upgradeCategory.rows.repaint(); }
    });

	private ItemUpgradeWindow() {
		super("item-upgrade", "Ulepszanie przedmiotu");
		setCloseable(true);
		setMinimizable(false);
		setContent(buildWindowContent());
        inventoryRefresh.setRepeats(false);
		addCloseListener(new CloseListener() {
			@Override
			public void windowClosed(final InternalWindow window) {
				send("close", null, null);
                inventoryRefresh.stop(); animationTimer.stop();
				instance = null;
			}
		});
	}

	public static void show(final RPEvent event) {
        if (event.has("phase") && "open".equals(event.get("phase")) && event.has("service") && "glyph".equals(event.get("service"))) openingGlyphWindow = true;
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
                openingGlyphWindow = false;
				instance.setVisible(true);
				instance.raise();
			}
		});
	}

    @Override public void addNotify() { super.addNotify(); animationTimer.start(); }
    @Override public void removeNotify() { animationTimer.stop(); inventoryRefresh.stop(); super.removeNotify(); }

    /** Read merged inventory contents on the game loop, then update the window on the EDT. */
    public static void onInventoryChanged(RPObject player) {
        if (player == null || (!openingGlyphWindow && (instance == null || !instance.glyphMode))) return;
        StringBuilder value = new StringBuilder(); appendInventory(value, player);
        String signature = value.toString();
        SwingUtilities.invokeLater(() -> {
            if (instance == null || !instance.glyphMode) return;
            ItemUpgradeWindow window = instance;
            boolean changed = window.inventorySignature != null && !signature.equals(window.inventorySignature);
            window.inventorySignature = signature;
            if (changed && !window.catalogReceived) {
                window.requestToken = null; window.upgrade.setEnabled(false); window.inventoryRefresh.restart();
            }
            window.catalogReceived = false;
        });
    }
    private static void appendInventory(StringBuilder value, RPObject owner) {
        for (RPSlot slot : owner.slots()) {
            value.append('[').append(slot.getName()).append(':');
            for (RPObject item : slot) {
                for (String key : new String[] {"id", "name", "class", "subclass", "quantity", "glyph_level", "bound", "itemdata"}) {
                    if (item.has(key)) { String text = item.get(key); value.append(key).append('=').append(text.length()).append(':').append(text); }
                }
                appendInventory(value, item); value.append(';');
            }
            value.append(']');
        }
    }

	/** A smith's preview cannot remain open after leaving the map. */
	public static void closeOnZoneChange() {
		SwingUtilities.invokeLater(() -> {
			if (instance != null) {
				instance.close();
			}
		});
	}

	/**
	 * Check whether an inventory item can currently be selected from its
	 * context menu.
	 *
	 * @return {@code true} when the upgrade window is open
	 */
	public static boolean isOpenForItemSelection() {
		return instance != null && instance.isVisible();
	}

	/** Check whether the server listed this exact item as an upgrade candidate. */
	public static boolean canSelectItemForUpgrade(final Item item) {
		return isOpenForItemSelection() && !instance.glyphMode && item != null
				&& instance.upgradeCandidatePaths.contains(
						encodePath(item.getPath()));
	}

	/** Select an inventory item in the already open upgrade window. */
	public static void selectItemForUpgrade(final Item item) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (canSelectItemForUpgrade(item)) {
					instance.selectItem(item);
					instance.raise();
				}
			}
		});
	}

	private JComponent buildWindowContent() {
        JPanel root = plainPanel(new BorderLayout(8, 0));
        root.add(buildContent(), BorderLayout.CENTER);
        recipeCatalog.setPreferredSize(new Dimension(236, 1));
        final Style catalogStyle = StyleUtil.getStyle();
        recipeCatalog.setBorder(BorderFactory.createCompoundBorder(
                catalogStyle != null ? catalogStyle.getBorderDown() : BorderFactory.createEtchedBorder(),
                BorderFactory.createEmptyBorder(8, 4, 8, 4)));
        recipeCatalog.setVisible(false); layoutRecipeCategories();
        root.add(recipeCatalog, BorderLayout.WEST); return root;
    }

    private void layoutRecipeCategories() {
        recipeCatalog.removeAll();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0; constraints.weightx = 1; constraints.fill = GridBagConstraints.BOTH;
        constraints.gridy = 0; constraints.weighty = creationCategory.expanded ? 1 : 0;
        recipeCatalog.add(creationCategory.panel, constraints);
        constraints.gridy = 1; constraints.weighty = upgradeCategory.expanded ? 1 : 0;
        recipeCatalog.add(upgradeCategory.panel, constraints);
        if (!creationCategory.expanded && !upgradeCategory.expanded) {
            constraints.gridy = 2; constraints.weighty = 1;
            recipeCatalog.add(plainPanel(new BorderLayout()), constraints);
        }
        recipeCatalog.revalidate(); recipeCatalog.repaint();
    }

    private JComponent buildContent() {
		final JPanel content = new LayoutPanel(new BorderLayout(0, 8)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Dimension getPreferredSize() {
				final Dimension size = super.getPreferredSize();
				return new Dimension(Math.max(CONTENT_WIDTH, size.width), size.height);
			}
		};
		content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		final JPanel identity = plainPanel(new BorderLayout(10, 2));
		identity.add(icon, BorderLayout.WEST);
		final JPanel names = plainPanel(new GridLayout(0, 1, 0, 4));
		itemName.setHorizontalAlignment(SwingConstants.LEFT);
		level.setHorizontalAlignment(SwingConstants.LEFT);
		names.add(itemName);
		names.add(level);
		identity.add(names, BorderLayout.CENTER);
		final MousePopupAdapter selectionPopup = new MousePopupAdapter() {
			@Override
			protected void showPopup(final MouseEvent event) {
				showSelectionPopup(event);
			}
		};
		identity.addMouseListener(selectionPopup);
		icon.addMouseListener(selectionPopup);
		itemName.addMouseListener(selectionPopup);
		level.addMouseListener(selectionPopup);
		content.add(identitySection = sectionPanel("Przedmiot do ulepszenia", identity), BorderLayout.NORTH);

		final JPanel center = plainPanel(new GridBagLayout());
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.weightx = 1.0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.anchor = GridBagConstraints.NORTH;
		constraints.insets = new Insets(0, 0, 3, 0);

		final JPanel requirements = plainPanel(new GridLayout(0, 1, 0, 4));
		requirements.add(chance);
		requirements.add(fee);

		constraints.gridy = 0;
		constraints.insets = new Insets(0, 0, 8, 0);
		center.add(statsSection = sectionPanel("Po ulepszeniu", stats),
				constraints);
		constraints.gridy++;
		center.add(costSection = sectionPanel("Koszt i szansa", requirements), constraints);
		constraints.gridy++;
		constraints.insets = new Insets(0, 0, 0, 0);
		center.add(sectionPanel("Materiały (posiadasz / potrzebne)", materials),
				constraints);

		content.add(center, BorderLayout.CENTER);

		final JPanel bottom = plainPanel(new BorderLayout(0, 6));
		bottom.add(new JSeparator(), BorderLayout.NORTH);
		status.setVerticalAlignment(SwingConstants.TOP);
		status.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		bottom.add(status, BorderLayout.CENTER);
		final JPanel buttons = plainPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		refresh.setFocusable(false);
		upgrade.setFocusable(false);
		refresh.setMargin(new Insets(4, 12, 4, 12));
		upgrade.setMargin(new Insets(4, 12, 4, 12));
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				send(selectedPath == null && selectedRecipe == null ? "refresh" : "preview",
						selectedPath, null);
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

	private void showSelectionPopup(final MouseEvent event) {
		if (selectedPath == null) {
			return;
		}
		final JPopupMenu menu = new JPopupMenu();
		final JMenuItem clear = new JMenuItem("Wyczyść");
		clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent actionEvent) {
				clearSelection();
			}
		});
		menu.add(clear);
		menu.show(event.getComponent(), event.getX(), event.getY());
	}

	private void clearSelection() {
		send("clear", null, null);
		selectionCleared = true;
		selectedPath = null;
        selectedRecipe = null; creationCategory.rows.clearSelection(); upgradeCategory.rows.clearSelection();
		requestToken = null;
		itemName.setText(selectionPrompt());
		level.setText(selectionInfo());
		icon.setItem(null, null);
		icon.setToolTipText(selectionHint());
		stats.removeAll();
		stats.add(centered(
				previewHint()));
		chance.setText(glyphMode ? "Gwarantowane powodzenie." : htmlValue("Szansa powodzenia", "brak danych"));
		fee.setText(htmlValue("Koszt", "brak danych"));
		showEmptyMaterials();
		status.setForeground(ACCENT);
		status.setText(wrappedText(selectionHint(),
				CONTENT_WIDTH - 40));
		refresh.setEnabled(npcId != 0);
		upgrade.setEnabled(false);
		upgrade.setToolTipText("Najpierw wybierz przedmiot.");
		setSize(getPreferredSize());
		revalidate();
		repaint();
	}

	private void apply(final RPEvent event) {
		final String phase = event.has("phase") ? event.get("phase") : "";
        final boolean responseGlyph = event.has("service") && "glyph".equals(event.get("service"));
        if (!"open".equals(phase) && (responseGlyph != glyphMode
                || (event.has("npc_id") && npcId != 0 && event.getInt("npc_id") != npcId))) return;
        final String responseRecipe = event.has("selected_recipe") ? event.get("selected_recipe") : null;
        final String responsePath = event.has("selected_path") ? event.get("selected_path") : null;
        if (!"open".equals(phase)) {
            if (selectionCleared && !"refresh".equals(phase)) return;
            if (responseRecipe != null && selectedRecipe != null && !selectedRecipe.equals(responseRecipe)) return;
            if ("refresh".equals(phase) && (selectedPath != null || (glyphMode && selectedRecipe != null))) return;
            if (!(glyphMode && responseRecipe != null) && responsePath != null
                    && selectedPath != null && !selectedPath.equals(responsePath)) return;
        }
        if ("open".equals(phase)) {
            glyphMode = responseGlyph;
            refresh.setVisible(!glyphMode); inventoryRefresh.stop(); inventorySignature = null; catalogReceived = false;
            setTitle(glyphMode ? "Stół runiczny" : "Ulepszanie przedmiotu");
            recipeCatalog.setVisible(glyphMode); selectedRecipe = null;
        }
        final String stateForRecipe = event.has("status") ? event.get("status") : "";
        if (responseRecipe != null) selectedRecipe = responseRecipe;
        else if (!isInteractionStatus(stateForRecipe)) selectedRecipe = null;
        creatingGlyph = glyphMode && event.has("operation") && "create".equals(event.get("operation"));
        setSectionTitle(identitySection, glyphMode ? (creatingGlyph ? "Tworzony glif" : "Wybrany glif") : "Przedmiot do ulepszenia");
        setSectionTitle(statsSection, creatingGlyph ? "Właściwości glifu" : "Po ulepszeniu");
        setSectionTitle(costSection, glyphMode ? "Koszt" : "Koszt i szansa");
        if (glyphMode && event.has("recipe_ids")) { updateRecipes(event); catalogReceived = true; }
        upgrade.setText(glyphMode ? (creatingGlyph ? "Utwórz glif" : "Ulepsz glif") : "Ulepsz");
		if ("open".equals(phase)) {
			selectionCleared = false;
		}
		if (event.has("npc_id")) {
			npcId = event.getInt("npc_id");
		}
		upgradeCandidatePaths.clear();
		upgradeCandidatePaths.addAll(list(event, "candidate_paths"));
		final String state = event.has("status") ? event.get("status") : "";
		final boolean preservePreview = !event.has("name")
				&& isInteractionStatus(state) && (selectedPath != null || selectedRecipe != null);
		if (event.has("selected_path")) {
			selectedPath = event.get("selected_path");
		} else if (!preservePreview) {
			selectedPath = null;
		}
		requestToken = event.has("request_token") ? event.get("request_token") : null;

		if (event.has("name")) {
			itemName.setText(glyphMode ? "<html><b>" + escapeHtml(event.get("name")) + "</b></html>"
                    : colorName(event.get("name"), event.has("rarity_id") ? event.get("rarity_id") : "common"));
			level.setText(glyphMode ? (creatingGlyph ? "Poziom glifu: I" : "Poziom glifu: " + roman(event.getInt("upgrade_level")) + " -> " + roman(event.getInt("next_upgrade_level"))
                    + (event.has("source_location") ? " (" + event.get("source_location") + ")" : ""))
                    : "Poziom ulepszenia: +" + event.getInt("upgrade_level") + " / +" + event.getInt("max_upgrade_level"));
			icon.setItem(event.get("class"), event.get("subclass"));
			icon.setToolTipText("Prawy przycisk myszy: wyczyść wybór.");
		} else if (!preservePreview) {
			itemName.setText("NO_UPGRADEABLE_ITEMS".equals(state)
					? (glyphMode ? "Brak glifu do rozwinięcia" : "Brak przedmiotu do ulepszenia")
					: selectionPrompt());
			level.setText(selectionInfo());
			icon.setItem(null, null);
			icon.setToolTipText(selectionHint());
		}
		if (event.has("name") || !preservePreview) {
			updateStats(event);
			chance.setText(glyphMode ? "Gwarantowane powodzenie." : event.has("success_percent")
					? htmlValue("Szansa powodzenia", event.getInt("success_percent") + "%")
					: htmlValue("Szansa powodzenia", "brak danych"));
			fee.setText(creatingGlyph ? "Koszt: wymagane składniki" : event.has("fee_text")
					? htmlValue("Koszt", event.get("fee_text"))
					: htmlValue("Koszt", "brak danych"));
			updateMaterials(event);
		}

		final boolean canUpgrade = event.has("can_upgrade")
				&& event.getInt("can_upgrade") == 1 && requestToken != null;
		upgrade.setEnabled(canUpgrade);
		refresh.setEnabled(npcId != 0);
        if (glyphMode && "STALE_PREVIEW".equals(state)) inventoryRefresh.restart();
		final String message = event.has("message") ? event.get("message") : "";
		status.setText(wrappedText(message.length() > 0 ? message : statusText(state),
				CONTENT_WIDTH - 40));
		status.setForeground("SUCCESS".equals(state) || "READY".equals(state)
				? AVAILABLE : "SELECT_ITEM".equals(state) ? ACCENT : UNAVAILABLE);
		upgrade.setToolTipText(canUpgrade ? (glyphMode ? "Ulepsz glif" : "Wykonaj próbę ulepszenia") : statusText(state));
		setSize(getPreferredSize());
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
					currentByName.get("damage_min") + "-" + currentByName.get("damage_max"),
					upgradedByName.get("damage_min") + "-" + upgradedByName.get("damage_max")));
		}
		for (int index = 0; index < names.size(); index++) {
			final String name = names.get(index);
			if (("damage_min".equals(name) || "damage_max".equals(name))
					&& currentByName.containsKey("damage_min")
					&& currentByName.containsKey("damage_max")) {
				continue;
			}
			if (index < current.size() && index < upgradedValues.size()) {
				stats.add(glyphMode ? glyphStatRow(statLabel(name), creatingGlyph ? null : current.get(index), upgradedValues.get(index))
                        : statRow(statLabel(name), current.get(index), upgradedValues.get(index)));
			}
		}
		if (stats.getComponentCount() == 0) {
			stats.add(centered(previewHint()));
		}
	}

	private void updateMaterials(final RPEvent event) {
		materials.removeAll();
		final List<String> names = list(event, "material_names");
		final List<String> classes = list(event, "material_classes");
		final List<String> subclasses = list(event, "material_subclasses");
		final List<String> required = list(event, "material_values");
		final List<String> owned = list(event, "owned_material_values");
		if (names.isEmpty()) {
			showEmptyMaterials();
			return;
		}
		final int columns = Math.min(3, names.size());
		materials.setLayout(new GridLayout(0, columns, MATERIAL_ROW_GAP, MATERIAL_ROW_GAP));
		int rowHeight = 0;
		for (int index = 0; index < names.size(); index++) {
			final int have = index < owned.size() ? integer(owned.get(index)) : 0;
			final int need = index < required.size() ? integer(required.get(index)) : 0;
			final JComponent card = materialCard(names.get(index),
					index < classes.size() ? classes.get(index) : null,
					index < subclasses.size() ? subclasses.get(index) : null,
					have, need);
			materials.add(card);
			rowHeight = Math.max(rowHeight, card.getPreferredSize().height);
		}
		final int rows = (names.size() + columns - 1) / columns;
		setMaterialPanelHeight(rows * rowHeight + (rows - 1) * MATERIAL_ROW_GAP);
	}

	private void showEmptyMaterials() {
		materials.removeAll();
		materials.setLayout(new BorderLayout());
		final JLabel empty = centered(
				"Wymagania pojawią się po wybraniu przedmiotu.");
		materials.add(empty);
		setMaterialPanelHeight(empty.getPreferredSize().height);
	}

	private void setMaterialPanelHeight(final int contentHeight) {
		final Insets insets = materials.getInsets();
		final int height = contentHeight + insets.top + insets.bottom;
		final Dimension size = new Dimension(1, height);
		materials.setMinimumSize(size);
		materials.setPreferredSize(size);
	}

	private JComponent materialCard(final String name,
			final String itemClass, final String subclass, final int owned,
			final int required) {
		final boolean available = owned >= required;
		final Color stateColor = available ? AVAILABLE : UNAVAILABLE;
		final JPanel card = plainPanel(new BorderLayout(0, 3));
		final JPanel slot = plainPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		slot.add(new MaterialIcon(itemClass, subclass, required));
		card.add(slot, BorderLayout.NORTH);

		final JPanel description = plainPanel(new BorderLayout(0, 2));
		description.add(centered(wrappedText(name, MATERIAL_CARD_WIDTH)),
				BorderLayout.CENTER);
		final JLabel count = centered(owned + " / " + required);
		count.setForeground(stateColor);
		description.add(count, BorderLayout.SOUTH);
		card.add(description, BorderLayout.CENTER);
		final String tooltip = name + ": posiadasz " + owned
				+ ", wymagane " + required;
		card.setToolTipText(tooltip);
		final Dimension naturalSize = card.getPreferredSize();
		final Dimension cardSize = new Dimension(MATERIAL_CARD_WIDTH,
				naturalSize.height);
		card.setMinimumSize(cardSize);
		card.setPreferredSize(cardSize);
		return card;
	}

	private void send(final String command, final String path, final String token) {
		if (npcId == 0) {
			return;
		}
		final RPAction action = new RPAction();
		action.put("type", Actions.ITEM_UPGRADE);
		action.put("command", command);
		action.put("npc_id", npcId);
        if (glyphMode) action.put("service", "glyph");
        if (glyphMode && selectedRecipe != null) action.put("glyph_recipe", selectedRecipe);
		if (path != null) {
			action.put("target_path", decodePath(path));
		}
		if (token != null) {
			action.put("request_token", token);
		}
		StendhalClient.get().send(action);
	}

    private void updateRecipes(RPEvent event) {
        updatingRecipes = true;
        try {
            Point creationPosition = creationCategory.scroll.getViewport().getViewPosition();
            Point upgradePosition = upgradeCategory.scroll.getViewport().getViewPosition();
            List<RecipeRow> creationRows = new ArrayList<RecipeRow>(), upgradeRows = new ArrayList<RecipeRow>();
            List<String> ids = list(event, "recipe_ids"), names = list(event, "recipe_names"), levels = list(event, "recipe_levels");
            List<String> ready = list(event, "recipe_ready"), sprites = list(event, "recipe_subclasses");
            for (int i = 0; i < ids.size() && i < names.size() && i < levels.size(); i++) {
                RecipeRow row = new RecipeRow(ids.get(i), names.get(i), integer(levels.get(i)),
                        i < ready.size() && "1".equals(ready.get(i)), i < sprites.size() ? sprites.get(i) : null);
                (row.level == 1 ? creationRows : upgradeRows).add(row);
            }
            updateCategory(creationCategory, creationRows); updateCategory(upgradeCategory, upgradeRows);
            recipeCatalog.revalidate();
            creationCategory.scroll.getViewport().setViewPosition(creationPosition);
            upgradeCategory.scroll.getViewport().setViewPosition(upgradePosition);
        } finally { updatingRecipes = false; }
    }
    /** Availability changes update existing entries without interrupting a mouse gesture. */
    private void updateCategory(RecipeCategory category, List<RecipeRow> rows) {
        boolean sameOrder = category.model.size() == rows.size();
        for (int i = 0; sameOrder && i < rows.size(); i++) sameOrder = category.model.get(i).id.equals(rows.get(i).id);
        if (!sameOrder) category.model.clear();
        int selected = -1;
        for (int i = 0; i < rows.size(); i++) {
            RecipeRow row = rows.get(i);
            if (!sameOrder) category.model.addElement(row);
            else if (!row.sameContent(category.model.get(i))) category.model.set(i, row);
            if (row.id.equals(selectedRecipe)) selected = i;
        }
        if (category.rows.getSelectedIndex() != selected) category.rows.setSelectedIndex(selected);
    }
    private static final class RecipeRow {
        final String id, name, sprite; final int level; final boolean ready;
        RecipeRow(String id, String name, int level, boolean ready, String sprite) {
            this.id = id; this.name = name; this.level = level; this.ready = ready; this.sprite = sprite;
        }
        boolean sameContent(RecipeRow other) {
            return id.equals(other.id) && name.equals(other.name) && level == other.level
                    && ready == other.ready && Objects.equals(sprite, other.sprite);
        }
        @Override public String toString() { return name + " " + roman(level); }
    }
    private final class RecipeCategory {
        final JPanel panel = plainPanel(new BorderLayout());
        final DefaultListModel<RecipeRow> model = new DefaultListModel<RecipeRow>();
        final JList<RecipeRow> rows = new JList<RecipeRow>(model);
        final JButton header = new JButton();
        final JScrollPane scroll = new JScrollPane(rows);
        final String title;
        boolean expanded;
        RecipeCategory(String title, boolean initiallyExpanded) {
            this.title = title; this.expanded = initiallyExpanded;
            header.setText((expanded ? "▾ " : "▸ ") + ClientLanguage.getText(title));
            header.setHorizontalAlignment(SwingConstants.LEFT); header.setFocusable(false);
            header.setForeground(ACCENT); header.setFont(header.getFont().deriveFont(Font.BOLD));
            header.setMargin(new Insets(4, 6, 4, 6));
            header.addActionListener(event -> {
                expanded = !expanded; scroll.setVisible(expanded);
                header.setText((expanded ? "▾ " : "▸ ") + ClientLanguage.getText(title));
                layoutRecipeCategories();
            });
            rows.setOpaque(false); rows.setFixedCellHeight(25); rows.setFixedCellWidth(228);
            rows.setFont(new JLabel().getFont()); rows.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            rows.setCellRenderer(new RecipeRenderer()); ToolTipManager.sharedInstance().registerComponent(rows);
            rows.addListSelectionListener(event -> {
                if (updatingRecipes || rows.getSelectedValue() == null) return;
                RecipeRow selected = rows.getSelectedValue();
                if (selected.id.equals(selectedRecipe)) return;
                updatingRecipes = true;
                try {
                    if (creationCategory != null && creationCategory != this) creationCategory.rows.clearSelection();
                    if (upgradeCategory != null && upgradeCategory != this) upgradeCategory.rows.clearSelection();
                } finally { updatingRecipes = false; }
                selectedRecipe = selected.id; selectedPath = null; requestToken = null; selectionCleared = false;
                inventoryRefresh.stop();
                upgrade.setEnabled(false); status.setText("Sprawdzanie wymagań..."); send("preview", null, null);
            });
            scroll.setOpaque(false); scroll.getViewport().setOpaque(false); scroll.setBorder(BorderFactory.createEmptyBorder(2, 2, 4, 3));
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scroll.setPreferredSize(new Dimension(228, 120)); scroll.setVisible(expanded);
            panel.add(header, BorderLayout.NORTH); panel.add(scroll, BorderLayout.CENTER);
        }
    }
    private final class RecipeRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;
        @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean selected, boolean focus) {
            super.getListCellRendererComponent(list, value, index, selected, focus);
            RecipeRow recipe = (RecipeRow)value;
            setText(recipe.toString()); setIcon(new RecipeIcon(recipe)); setIconTextGap(6);
            setEnabled(true); setOpaque(selected); setBackground(new Color(105, 72, 34));
            setBorder(BorderFactory.createEmptyBorder(2, 7, 2, 4));
            setFont(list.getFont().deriveFont(selected ? Font.BOLD : Font.PLAIN));
            setForeground(recipe.ready ? new Color(243, 235, 217) : new Color(210, 194, 170, 120));
            setToolTipText(ClientLanguage.getText(recipe.ready ? "Wymagania są spełnione." : "Brakuje składników. Kliknij, aby sprawdzić wymagania."));
            return this;
        }
    }
    private final class RecipeIcon implements Icon {
        private final RecipeRow row;
        RecipeIcon(RecipeRow row) { this.row = row; }
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }
        @Override public void paintIcon(Component component, Graphics graphics, int x, int y) {
            if (row.sprite == null || row.sprite.isEmpty()) return;
            Graphics2D copy = (Graphics2D)graphics.create();
            if (!row.ready) copy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
            copy.translate(x, y); copy.scale(0.5, 0.5);
            itemSprite("glyph", row.sprite).draw(copy, 0, 0); copy.dispose();
        }
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

    private static void setSectionTitle(JPanel section, String title) {
        Border border = section.getBorder();
        if (border instanceof CompoundBorder) border = ((CompoundBorder)border).getOutsideBorder();
        if (border instanceof TitledBorder) ((TitledBorder)border).setTitle(ClientLanguage.getText(title));
    }

	private static JPanel sectionPanel(final String title,
			final JComponent body) {
		final JPanel section = plainPanel(new BorderLayout());
		final Style style = StyleUtil.getStyle();
		final Border frame = style != null ? style.getBorderDown()
				: BorderFactory.createEtchedBorder();
		final TitledBorder border = BorderFactory.createTitledBorder(frame, title);
		final Font font = style != null ? style.getFont() : new JLabel().getFont();
		border.setTitleFont(font.deriveFont(Font.BOLD));
		if (style != null) {
			border.setTitleColor(style.getForeground());
		}
		section.setBorder(BorderFactory.createCompoundBorder(border,
				BorderFactory.createEmptyBorder(4, 6, 6, 6)));
		section.add(body, BorderLayout.CENTER);
		return section;
	}

	private static JPanel plainPanel(final LayoutManager layout) {
		return new LayoutPanel(layout);
	}

	/**
	 * Layout-only containers share the window's texture instead of repainting
	 * and framing every nested panel through StyledPanelUI.
	 */
	private static class LayoutPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		LayoutPanel(final LayoutManager layout) {
			super(layout);
			setOpaque(false);
			setBorder(BorderFactory.createEmptyBorder());
		}
		@Override
		protected void paintComponent(final Graphics graphics) {
			// The enclosing window paints the shared background.
		}
	}

	private static String wrappedText(final String text, final int width) {
		return "<html><table width='" + width + "' cellspacing='0' cellpadding='0'>"
				+ "<tr><td align='center'>" + escapeHtml(text) + "</td></tr></table></html>";
	}

	private static String escapeHtml(final String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
	}

	private static JLabel statRow(final String name, final String current,
			final String upgraded) {
		return centered("<html><b>" + name + ":</b>&nbsp; " + current
				+ " &nbsp;-&gt;&nbsp; <font color='#5cbe69'><b>" + upgraded
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
		if ("atk".equals(stat)) return "Atak";
		if ("ratk".equals(stat)) return "Atak dystansowy";
		if ("def".equals(stat)) return "Obrona";
		if ("damage_min".equals(stat)) return "Obrażenia min.";
		if ("damage_max".equals(stat)) return "Obrażenia max.";
		if ("range".equals(stat)) return "Zasięg";
		if ("rate".equals(stat)) return "Czas ataku";
		return stat;
	}

	private String previewHint() { return glyphMode ? "Po wybraniu glifu zobaczysz zmianę jego mocy." : "Po wybraniu przedmiotu zobaczysz zmianę statystyk."; }
	private String selectionPrompt() { return glyphMode ? "Wybierz glif z listy" : "Przeciągnij tutaj przedmiot"; }
    private String selectionInfo() { return glyphMode ? "Glif pozostanie w ekwipunku." : "Przedmiot pozostanie w ekwipunku."; }
    private String selectionHint() { return glyphMode ? "Wybierz glif z listy po lewej stronie." : "Przeciągnij tutaj przedmiot z ekwipunku."; }
    private static String roman(int level) { return level >= 1 && level <= 3 ? new String[] {"I", "II", "III"}[level - 1] : ""; }
    private static JLabel glyphStatRow(String name, String current, String next) {
        return centered("<html><table width='" + (CONTENT_WIDTH - 56) + "' cellpadding='0' cellspacing='2'><tr><td width='"
                + (CONTENT_WIDTH - 180) + "'><b>" + escapeHtml(name) + "</b></td><td align='right' nowrap='nowrap'>"
                + (current == null ? "" : escapeHtml(current) + " &rarr; ") + "<font color='#5cbe69'><b>" + escapeHtml(next) + "</b></font></td></tr></table></html>");
    }
    private String statusText(final String state) {
        if (glyphMode) {
            if ("NO_UPGRADEABLE_ITEMS".equals(state)) return "Nie masz przy sobie glifu I lub II, który można rozwinąć.";
            if ("NPC_TOO_FAR".equals(state)) return "Podejdź do stołu runicznego, aby rozwijać glify.";
            if ("MAX_LEVEL".equals(state)) return "Glif osiągnął poziom III.";
            if ("INVALID_ITEM".equals(state) || "NOT_UPGRADEABLE".equals(state)) return "Tego glifu nie można ulepszyć.";
        }
		if ("SELECT_ITEM".equals(state)) return selectionHint();
		if ("NO_UPGRADEABLE_ITEMS".equals(state)) return "Nie masz przedmiotu, który można ulepszyć.";
		if ("READY".equals(state)) return "Wymagania są spełnione.";
		if ("NOT_ENOUGH_MONEY".equals(state)) return "Brakuje pieniędzy.";
		if ("MISSING_RESOURCES".equals(state)) return "Brakuje materiałów.";
		if ("INVALID_ITEM".equals(state)) return "Tego przedmiotu nie można ulepszyć.";
		if ("NOT_UPGRADEABLE".equals(state)) return "Tego przedmiotu nie można ulepszyć.";
		if ("MAX_LEVEL".equals(state)) return "Osiągnięto maksymalny poziom.";
		if ("STALE_PREVIEW".equals(state)) return "Dane się zmieniły. Sprawdź ponownie koszt i materiały.";
		if ("NPC_TOO_FAR".equals(state)) return "Musisz pozostać w pobliżu kowala.";
		if ("FAILURE".equals(state)) return "Próba ulepszenia nie powiodła się.";
		return "Nie można teraz ulepszyć przedmiotu.";
	}

	private static boolean isInteractionStatus(final String state) {
		return "NPC_TOO_FAR".equals(state);
	}

	private static String colorName(final String name, final String rarityId) {
		final ItemRarity rarity = ItemRarity.fromIdOrCommon(rarityId);
		return "<html><b><font color='" + rarity.getColorHex() + "'>"
				+ escapeHtml(name) + "</font></b></html>";
	}

	private void selectItem(final Item item) {
		selectionCleared = false;
		selectedPath = encodePath(item.getPath());
		requestToken = null;
		itemName.setText(item.getName());
		level.setText("Sprawdzanie przedmiotu...");
		icon.setItem(item.getEntityClass(), item.getEntitySubclass());
		icon.setToolTipText("Prawy przycisk myszy: wyczyść wybór.");
		upgrade.setEnabled(false);
		refresh.setEnabled(true);
		status.setForeground(ACCENT);
		status.setText(wrappedText("Sprawdzanie kosztu i materiałów...",
				CONTENT_WIDTH - 40));
		send("preview", selectedPath, null);
	}

    private Sprite itemSprite(String itemClass, String subclass) {
        String path = "data/sprites/items/" + itemClass + "/" + subclass + ".png";
        Sprite sprite = itemSprites.get(path);
        if (sprite == null) {
            SpriteStore store = SpriteStore.get(); Sprite sheet = store.getSprite(path);
            sprite = sheet.getWidth() > 32 ? store.getAnimatedSprite(sheet, 0, 0, sheet.getWidth() / 32, 32, 32, 100)
                    : sheet.getHeight() > 32 ? store.getTile(sheet, 0, 0, 32, 32) : sheet;
            itemSprites.put(path, sprite);
        }
        return sprite;
    }

	private final class ItemIcon extends JComponent implements DropTarget {
		private static final long serialVersionUID = 1L;
		private Sprite sprite;

		private ItemIcon() {
			setPreferredSize(new Dimension(48, 48));
			setMinimumSize(new Dimension(48, 48));
			setToolTipText(selectionHint());
		}

		private void setItem(final String itemClass, final String subclass) {
			if (itemClass == null || subclass == null) {
				sprite = null;
			} else {
                sprite = itemSprite(itemClass, subclass);
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
			return !glyphMode && entity instanceof Item;
		}

		@Override
		public void dropEntity(final IEntity entity, final int amount,
				final Point point) {
			if (!canAccept(entity)) {
				return;
			}
			selectItem((Item) entity);
		}
	}

	private final class MaterialIcon extends JComponent {
		private static final long serialVersionUID = 1L;
		private final Sprite sprite;
		private final Sprite quantity;

		private MaterialIcon(final String itemClass, final String subclass,
				final int required) {
			setPreferredSize(new Dimension(40, 40));
			setMinimumSize(new Dimension(40, 40));
            sprite = itemClass == null || itemClass.isEmpty() || subclass == null || subclass.isEmpty()
                    ? null : itemSprite(itemClass, subclass);
			quantity = required > 1 ? TextSprite.createTextSprite(
					Integer.toString(required), Color.WHITE) : null;
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			super.paintComponent(graphics);
			final int x = (getWidth() - SLOT_BACKGROUND.getWidth()) / 2;
			final int y = (getHeight() - SLOT_BACKGROUND.getHeight()) / 2;
			SLOT_BACKGROUND.draw(graphics, x, y);
			if (sprite != null) {
				final int spriteX = x
						+ (SLOT_BACKGROUND.getWidth() - sprite.getWidth()) / 2;
				final int spriteY = y
						+ (SLOT_BACKGROUND.getHeight() - sprite.getHeight()) / 2;
				sprite.draw(graphics, spriteX, spriteY);
			}
			if (quantity != null) {
				quantity.draw(graphics,
						x + SLOT_BACKGROUND.getWidth() - quantity.getWidth(), y);
			}
		}
	}
}
