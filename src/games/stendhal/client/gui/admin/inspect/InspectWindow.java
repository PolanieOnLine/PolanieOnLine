/***************************************************************************
*                   (C) Copyright 2003-2024 - Stendhal                    *
***************************************************************************
***************************************************************************
*                                                                         *
*   This program is free software; you can redistribute it and/or modify  *
*   it under the terms of the GNU General Public License as published by  *
*   the Free Software Foundation; either version 2 of the License, or     *
*   (at your option) any later version.                                   *
*                                                                         *
***************************************************************************/
package games.stendhal.client.gui.admin.inspect;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import games.stendhal.client.gui.InternalManagedWindow;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

/**
* Administrator inspect window.
*/
public final class InspectWindow extends InternalManagedWindow {
	private static final long serialVersionUID = 1L;
	private static final Dimension ITEM_SLOT_SIZE = new Dimension(48, 48);

	private InspectData data;

	private final JLabel nameLabel;
	private final JLabel classLabel;
	private final JLabel idLabel;
	private final JLabel locationLabel;

	private final JLabel outfitCurrentValue;
	private final JLabel outfitTemporaryValue;
	private final JLabel outfitExpiresValue;
	private final JLabel outfitClassValue;
	private final JLabel outfitWarningValue;

	private final JPanel statsContent;
	private final JPanel resistancesContent;
	private final JTextArea metaArea;

	private final JPanel equipmentContent;
	private final JPanel runeContent;
	private final JTabbedPane containersTabs;
	private final JTabbedPane bankTabs;

	private final JTextArea activeQuestsArea;
	private final JTextArea completedQuestsArea;
	private final JTextArea repeatableQuestsArea;

	private final JTabbedPane tabs;

	public InspectWindow() {
		super("admin_inspect", "Inspekcja gracza");
		setPreferredSize(new Dimension(820, 640));
		setMinimumSize(new Dimension(680, 520));

		final JPanel root = new JPanel(new BorderLayout(10, 10));
		root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		root.setOpaque(false);
		setContent(root);

		final JPanel headerPanel = new JPanel();
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
		headerPanel.setOpaque(false);

		nameLabel = new JLabel();
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 20f));
		headerPanel.add(nameLabel);

		headerPanel.add(Box.createVerticalStrut(2));

		classLabel = new JLabel();
		headerPanel.add(classLabel);

		headerPanel.add(Box.createVerticalStrut(2));

		idLabel = new JLabel();
		headerPanel.add(idLabel);

		headerPanel.add(Box.createVerticalStrut(2));

		locationLabel = new JLabel();
		headerPanel.add(locationLabel);

		headerPanel.add(Box.createVerticalStrut(6));

		root.add(headerPanel, BorderLayout.NORTH);

		tabs = new JTabbedPane();
		root.add(tabs, BorderLayout.CENTER);

		final JPanel overviewContent = new JPanel();
		overviewContent.setLayout(new BoxLayout(overviewContent, BoxLayout.Y_AXIS));
		overviewContent.setOpaque(false);

		statsContent = new JPanel(new GridBagLayout());
		statsContent.setOpaque(false);
		overviewContent.add(createGroupPanel("Statystyki", statsContent));
		overviewContent.add(Box.createVerticalStrut(8));

		resistancesContent = new JPanel(new GridBagLayout());
		resistancesContent.setOpaque(false);
		overviewContent.add(createGroupPanel("Odporności", resistancesContent));
		overviewContent.add(Box.createVerticalStrut(8));

		final JPanel outfitPanel = new JPanel(new GridBagLayout());
		outfitPanel.setOpaque(false);
		outfitCurrentValue = new JLabel();
		outfitTemporaryValue = new JLabel();
		outfitExpiresValue = new JLabel();
		outfitClassValue = new JLabel();
		outfitWarningValue = new JLabel();
		outfitWarningValue.setForeground(new Color(200, 60, 60));
		populateOutfitPanel(outfitPanel);
		overviewContent.add(createGroupPanel("Ubiór", outfitPanel));
		overviewContent.add(Box.createVerticalStrut(8));

		metaArea = createTextArea();
		overviewContent.add(createGroupPanel("Sloty techniczne", wrapInPanel(metaArea)));

		tabs.addTab("Przegląd", wrapScroll(overviewContent));

		equipmentContent = new JPanel(new java.awt.GridLayout(0, 4, 8, 8));
		equipmentContent.setOpaque(false);
		tabs.addTab("Wyposażenie", wrapScroll(equipmentContent));

		containersTabs = new JTabbedPane();
		tabs.addTab("Torby", containersTabs);

		bankTabs = new JTabbedPane();
		tabs.addTab("Banki", bankTabs);

		final JTabbedPane questTabs = new JTabbedPane();
		activeQuestsArea = createTextArea();
		questTabs.addTab("Aktywne", wrapScroll(activeQuestsArea));
		completedQuestsArea = createTextArea();
		questTabs.addTab("Ukończone", wrapScroll(completedQuestsArea));
		repeatableQuestsArea = createTextArea();
		questTabs.addTab("Powtarzalne", wrapScroll(repeatableQuestsArea));
		tabs.addTab("Zadania", questTabs);

		runeContent = new JPanel(new java.awt.GridLayout(0, 4, 8, 8));
		runeContent.setOpaque(false);
		tabs.addTab("Runy", wrapScroll(runeContent));

		showPlaceholderTabs();
	}

	public void updateData(final InspectData newData) {
		data = newData;
		if (data == null) {
			return;
		}

		updateHeader();
		updateStats();
		updateResistances();
		updateOutfit();
		updateMeta();
		updateEquipment();
		updateContainers();
		updateBanks();
		updateQuests();
		updateRunes();

		revalidate();
		repaint();
	}

	private void updateHeader() {
		final InspectData.EntityInfo entity = data.getEntity();
		final StringBuilder nameBuilder = new StringBuilder();
		if (entity.getTitle() != null) {
			nameBuilder.append(entity.getTitle()).append(' ');
		}
		if (entity.getName() != null) {
			nameBuilder.append(entity.getName());
		} else {
			nameBuilder.append("(nieznane)");
		}
		nameLabel.setText(nameBuilder.toString());

		final StringBuilder classBuilder = new StringBuilder();
		if (entity.getType() != null) {
			classBuilder.append(entity.getType());
		}
		if (entity.getClassName() != null) {
			if (classBuilder.length() > 0) {
				classBuilder.append(" · ");
			}
			classBuilder.append(entity.getClassName());
		}
		if (entity.getGender() != null) {
			if (classBuilder.length() > 0) {
				classBuilder.append(" · ");
			}
			classBuilder.append("Płeć: ").append(entity.getGender());
		}
		classLabel.setText(classBuilder.toString());

		idLabel.setText("ID: " + safeValue(data.getId()));

		final InspectData.Location location = data.getLocation();
		final String zone = location.getZone() != null ? location.getZone() : "(brak strefy)";
		locationLabel.setText("Lokacja: " + zone + " (" + location.getX() + ", " + location.getY() + ")");
	}

	private void updateStats() {
		fillKeyValuePanel(statsContent, data.getStats(), "Brak statystyk do wyświetlenia.");
	}

	private void updateResistances() {
		fillKeyValuePanel(resistancesContent, data.getResistances(), "Brak odporności.");
	}

	private void updateOutfit() {
		final InspectData.Outfit outfit = data.getOutfit();
		outfitCurrentValue.setText(formatValue(outfit.getCurrent()));
		outfitTemporaryValue.setText(formatValue(outfit.getTemporary()));
		outfitExpiresValue.setText(formatValue(outfit.getExpires()));
		outfitClassValue.setText(formatValue(outfit.getOutfitClass()));
		outfitWarningValue.setText(outfit.getWarning() != null ? outfit.getWarning() : "");
		outfitWarningValue.setVisible(outfit.getWarning() != null && !outfit.getWarning().isEmpty());
	}

	private void updateMeta() {
		final List<InspectData.MetaSlot> metaSlots = data.getMetaSlots();
		if (metaSlots.isEmpty()) {
			metaArea.setText("Brak slotów technicznych.");
		} else {
			final StringBuilder builder = new StringBuilder();
			for (InspectData.MetaSlot slot : metaSlots) {
				builder.append(slot.getName()).append(':');
				if (slot.getValues().isEmpty()) {
					builder.append(" [puste]");
				}
				builder.append('\n');
				if (!slot.getValues().isEmpty()) {
					for (String value : slot.getValues()) {
						builder.append("  • ").append(value).append('\n');
					}
				}
				builder.append('\n');
			}
			metaArea.setText(builder.toString().trim());
		}
		metaArea.setCaretPosition(0);
	}

	private void updateEquipment() {
		equipmentContent.removeAll();

		final List<String> order = data.getEquipmentOrder();
		final Set<String> handled = new HashSet<String>();
		for (String slotName : order) {
			final InspectData.Slot slot = data.getSlot(slotName);
			if (slot != null) {
				equipmentContent.add(createSlotCard(slot));
				handled.add(slot.getName());
			}
		}
		for (InspectData.Slot slot : data.getSlotsByCategory("equipment")) {
			if (!handled.contains(slot.getName())) {
				equipmentContent.add(createSlotCard(slot));
			}
		}

		if (equipmentContent.getComponentCount() == 0) {
			equipmentContent.add(createPlaceholderLabel("Brak wyposażenia."));
		}
	}

	private void updateRunes() {
		runeContent.removeAll();

		final List<String> order = data.getRuneOrder();
		final Set<String> handled = new HashSet<String>();
		for (String slotName : order) {
			final InspectData.Slot slot = data.getSlot(slotName);
			if (slot != null) {
				runeContent.add(createSlotCard(slot));
				handled.add(slot.getName());
			}
		}
		for (InspectData.Slot slot : data.getSlotsByCategory("rune")) {
			if (!handled.contains(slot.getName())) {
				runeContent.add(createSlotCard(slot));
			}
		}

		if (runeContent.getComponentCount() == 0) {
			runeContent.add(createPlaceholderLabel("Brak run."));
		}
	}

	private void updateContainers() {
		containersTabs.removeAll();

		final List<InspectData.Slot> slots = new LinkedList<InspectData.Slot>();
		slots.addAll(data.getSlotsByCategory("bag"));
		slots.addAll(data.getSlotsByCategory("container"));
		slots.addAll(data.getSlotsByCategory("reserve"));
		slots.addAll(data.getSlotsByCategory("other"));

		if (slots.isEmpty()) {
			containersTabs.addTab("Brak", wrapScroll(createPlaceholderPanel("Brak dodatkowych slotów.")));
			return;
		}

		for (InspectData.Slot slot : slots) {
			containersTabs.addTab(resolveSlotLabel(slot), wrapScroll(createSlotItemsPanel(slot)));
		}
	}

	private void updateBanks() {
		bankTabs.removeAll();
		final List<InspectData.Slot> slots = data.getSlotsByCategory("bank");
		if (slots.isEmpty()) {
			bankTabs.addTab("Brak", wrapScroll(createPlaceholderPanel("Brak kont bankowych.")));
			return;
		}
		for (InspectData.Slot slot : slots) {
			bankTabs.addTab(resolveSlotLabel(slot), wrapScroll(createSlotItemsPanel(slot)));
		}
	}

	private void updateQuests() {
		fillQuestArea(activeQuestsArea, data.getActiveQuests(), "Brak aktywnych zadań.");
		fillQuestArea(completedQuestsArea, data.getCompletedQuests(), "Brak ukończonych zadań.");
		fillQuestArea(repeatableQuestsArea, data.getRepeatableQuests(), "Brak zadań powtarzalnych.");
	}

	private void fillKeyValuePanel(final JPanel panel, final Map<String, String> source, final String emptyMessage) {
		panel.removeAll();
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(2, 6, 2, 6);
		constraints.anchor = GridBagConstraints.LINE_START;
		constraints.gridy = 0;

		if (source == null || source.isEmpty()) {
			constraints.gridx = 0;
			panel.add(new JLabel(emptyMessage), constraints);
			return;
		}

		final List<Map.Entry<String, String>> entries = new ArrayList<Map.Entry<String, String>>(source.entrySet());
		Collections.sort(entries, (left, right) -> left.getKey().compareToIgnoreCase(right.getKey()));

		for (Map.Entry<String, String> entry : entries) {
			constraints.gridx = 0;
			panel.add(new JLabel(entry.getKey() + ":"), constraints);
			constraints.gridx = 1;
			panel.add(new JLabel(entry.getValue()), constraints);
			constraints.gridy++;
		}
	}

	private JPanel createSlotCard(final InspectData.Slot slot) {
		final JPanel card = new JPanel(new BorderLayout(0, 4));
		card.setOpaque(false);
		final JLabel title = new JLabel(resolveSlotLabel(slot), SwingConstants.CENTER);
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		card.add(title, BorderLayout.NORTH);
		card.add(createItemsFlow(slot), BorderLayout.CENTER);
		return card;
	}

	private JPanel createItemsFlow(final InspectData.Slot slot) {
		final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
		panel.setOpaque(false);
		if (slot.getItems().isEmpty()) {
			panel.add(createPlaceholderLabel("Puste"));
			return panel;
		}
		for (InspectData.Item item : slot.getItems()) {
			panel.add(createItemComponent(item));
		}
		return panel;
	}

	private JPanel createSlotItemsPanel(final InspectData.Slot slot) {
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setOpaque(false);

		panel.add(Box.createVerticalStrut(4));
		panel.add(createItemsFlow(slot));

		if (!slot.getRaw().isEmpty()) {
			panel.add(Box.createVerticalStrut(8));
			panel.add(new JLabel("Wartości surowe:"));
			for (String value : slot.getRaw()) {
				panel.add(new JLabel("  • " + value));
			}
		}
		return panel;
	}

	private JComponent createItemComponent(final InspectData.Item item) {
		final Sprite sprite = resolveSprite(item);
		final String tooltip = buildTooltip(item);
		return new ItemSlotComponent(sprite, item.getQuantity(), tooltip);
	}

	private String buildTooltip(final InspectData.Item item) {
		final StringBuilder builder = new StringBuilder("<html><b>");
		builder.append(safeValue(item.getName()));
		if (item.getQuantity() > 1) {
			builder.append(" × ").append(item.getQuantity());
		}
		builder.append("</b><br/>");
		builder.append("ID: ").append(safeValue(item.getId()));
		if (item.getItemClass() != null) {
			builder.append("<br/>").append(item.getItemClass());
			if (item.getSubclass() != null) {
				builder.append(" / ").append(item.getSubclass());
			}
		}
		if (item.getQuality() != null) {
			builder.append("<br/>Jakość: ").append(item.getQuality());
		}
		if (item.getDescription() != null) {
			builder.append("<br/>").append(item.getDescription());
		}
		builder.append("</html>");
		return builder.toString();
	}

	private Sprite resolveSprite(final InspectData.Item item) {
		final SpriteStore store = SpriteStore.get();
		Sprite sprite = null;
		String path = null;
		if (item.getItemClass() != null) {
			path = item.getItemClass();
			if (item.getSubclass() != null) {
				path += "/" + item.getSubclass();
			}
		}
		if (path == null && item.getType() != null) {
			path = item.getType();
		}
		if (path != null) {
			sprite = store.getSprite("data/sprites/items/" + path + ".png");
		}
		if (sprite == null) {
			sprite = store.getFailsafe();
		}
		return sprite;
	}

	private void fillQuestArea(final JTextArea area, final List<InspectData.QuestEntry> entries, final String emptyMessage) {
		if (entries.isEmpty()) {
			area.setText(emptyMessage);
			area.setCaretPosition(0);
			return;
		}
		final StringBuilder builder = new StringBuilder();
		for (InspectData.QuestEntry entry : entries) {
			builder.append(entry.getName());
			if (entry.getWarning() != null && !entry.getWarning().isEmpty()) {
				builder.append(" (⚠ ").append(entry.getWarning()).append(')');
			}
			builder.append('
');
			if (entry.getState() != null) {
				builder.append("Stan: ").append(entry.getState()).append('
');
			}
			if (entry.getSlot() != null) {
				builder.append("Slot: ").append(entry.getSlot()).append('
');
			}
			if (entry.getDescription() != null && !entry.getDescription().isEmpty()) {
				builder.append(entry.getDescription()).append('
');
			}
			if (!entry.getHistory().isEmpty()) {
				for (String history : entry.getHistory()) {
					builder.append("  • ").append(history).append('
');
				}
			}
			builder.append('
');
		}
		area.setText(builder.toString().trim());
		area.setCaretPosition(0);
	}

	private void populateOutfitPanel(final JPanel panel) {
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(2, 6, 2, 6);
		constraints.anchor = GridBagConstraints.LINE_START;

		constraints.gridx = 0;
		constraints.gridy = 0;
		panel.add(new JLabel("Aktualny:"), constraints);
		constraints.gridx = 1;
		panel.add(outfitCurrentValue, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		panel.add(new JLabel("Tymczasowy:"), constraints);
		constraints.gridx = 1;
		panel.add(outfitTemporaryValue, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		panel.add(new JLabel("Wygasa:"), constraints);
		constraints.gridx = 1;
		panel.add(outfitExpiresValue, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		panel.add(new JLabel("Klasa:"), constraints);
		constraints.gridx = 1;
		panel.add(outfitClassValue, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		constraints.gridwidth = 2;
		panel.add(outfitWarningValue, constraints);
	}

	private JPanel createGroupPanel(final String title, final JComponent content) {
		final JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		final TitledBorder border = BorderFactory.createTitledBorder(title);
		panel.setBorder(border);
		panel.add(content, BorderLayout.CENTER);
		return panel;
	}

	private JScrollPane wrapScroll(final JComponent component) {
		final JScrollPane scrollPane = new JScrollPane(component);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setOpaque(false);
		return scrollPane;
	}

	private JTextArea createTextArea() {
		final JTextArea area = new JTextArea();
		area.setEditable(false);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setOpaque(false);
		area.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		return area;
	}

	private JPanel createPlaceholderPanel(final String message) {
		final JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		panel.add(createPlaceholderLabel(message), BorderLayout.CENTER);
		return panel;
	}

	private JLabel createPlaceholderLabel(final String message) {
		final JLabel label = new JLabel(message, SwingConstants.CENTER);
		label.setFont(label.getFont().deriveFont(Font.ITALIC));
		return label;
	}

	private void showPlaceholderTabs() {
		containersTabs.addTab("Brak", wrapScroll(createPlaceholderPanel("Oczekiwanie na dane.")));
		bankTabs.addTab("Brak", wrapScroll(createPlaceholderPanel("Oczekiwanie na dane.")));
	}

	private String resolveSlotLabel(final InspectData.Slot slot) {
		return slot.getLabel() != null ? slot.getLabel() : slot.getName();
	}

	private String formatValue(final String value) {
		return value != null ? value : "—";
	}

	private String safeValue(final String value) {
		return value != null ? value : "—";
	}

	private JPanel wrapInPanel(final JComponent component) {
		final JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		panel.add(component, BorderLayout.CENTER);
		return panel;
	}

	private static final class ItemSlotComponent extends JComponent {
		private static final long serialVersionUID = 1L;

		private final Sprite sprite;
		private final int quantity;

		ItemSlotComponent(final Sprite sprite, final int quantity, final String tooltip) {
			this.sprite = sprite;
			this.quantity = quantity;
			setPreferredSize(ITEM_SLOT_SIZE);
			setToolTipText(tooltip);
			setOpaque(false);
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			super.paintComponent(graphics);
			final int width = getWidth();
			final int height = getHeight();

			graphics.setColor(new Color(255, 255, 255, 40));
			graphics.fillRoundRect(0, 0, width - 1, height - 1, 10, 10);
			graphics.setColor(new Color(140, 110, 70));
			graphics.drawRoundRect(0, 0, width - 1, height - 1, 10, 10);

			if (sprite != null) {
				final int imageX = (width - sprite.getWidth()) / 2;
				final int imageY = (height - sprite.getHeight()) / 2;
				sprite.draw(graphics, imageX, imageY);
			}

			if (quantity > 1) {
				final String text = Integer.toString(quantity);
				final Font font = graphics.getFont().deriveFont(Font.BOLD, 11f);
				graphics.setFont(font);
				final FontMetrics metrics = graphics.getFontMetrics();
				final int boxWidth = Math.max(18, metrics.stringWidth(text) + 6);
				final int boxHeight = metrics.getHeight();
				final int boxX = width - boxWidth - 4;
				final int boxY = height - boxHeight - 4;
				graphics.setColor(new Color(20, 20, 20, 170));
				graphics.fillRoundRect(boxX, boxY, boxWidth, boxHeight, 8, 8);
				graphics.setColor(Color.WHITE);
				final int textX = boxX + (boxWidth - metrics.stringWidth(text)) / 2;
				final int textY = boxY + metrics.getAscent();
				graphics.drawString(text, textX, textY);
			}
		}
	}
}

