	/***************************************************************************
	 *                 (C) Copyright 2024 - PolanieOnLine                 *
	 ***************************************************************************
	 ***************************************************************************
	 *                                                                         *
	 *   This program is free software; you can redistribute it and/or modify  *
	 *   it under the terms of the GNU General Public License as published by  *
	 *   the Free Software Foundation; either version 2 of the License, or     *
	 *   (at your option) any later version.                                   *
	 *                                                                         *
	 ***************************************************************************/
package games.stendhal.client.events;

import static games.stendhal.common.constants.Actions.QUEST_CRAFT;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.InternalManagedWindow;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import marauroa.common.game.RPAction;

class QuestCraftingEvent extends Event<RPEntity> {
	private static final Logger logger = Logger.getLogger(QuestCraftingEvent.class);

	@Override
	public void execute() {
		if (!(entity instanceof User) || !entity.equals(User.get())) {
			return;
		}

		if (!event.has("product")) {
			logger.warn("Quest crafting event missing product attribute");
			return;
		}

		CraftingItem product = CraftingItem.parse(event.get("product"));
		if (product == null) {
			logger.warn("Unable to parse quest crafting product payload: " + event.get("product"));
			return;
		}

		List<CraftingItem> requiredItems = CraftingItem.parseList(event.has("required") ? event.get("required") : null);
		int waitingTime = event.has("waiting_time") ? event.getInt("waiting_time") : 0;
		boolean canCraft = event.has("can_craft");
		String npcName = event.has("npc") ? event.get("npc") : null;
		String questSlot = event.has("quest") ? event.get("quest") : null;
		String buttonLabel = event.has("button") ? event.get("button") : "Wytwórz";
		String title = event.has("title") ? event.get("title") : buildDefaultTitle(npcName, product.name);

		new QuestCraftingWindow(title, npcName, questSlot, product, requiredItems, waitingTime, canCraft, buttonLabel);
	}

	private String buildDefaultTitle(String npcName, String productName) {
		if (npcName != null && !npcName.isEmpty()) {
			return npcName + " - Wytwarzanie";
		}
		return productName + " - wytwarzanie";
	}

	private static final class QuestCraftingWindow extends InternalManagedWindow {
		private static final long serialVersionUID = 1L;

		private final String npcName;
		private final String questSlot;
		private final CraftingItem product;
		private final List<CraftingItem> requiredItems;
		private final int waitingTime;
		private final boolean canCraft;
		private final String buttonLabel;

		QuestCraftingWindow(String title, String npcName, String questSlot, CraftingItem product,
				List<CraftingItem> requiredItems, int waitingTime, boolean canCraft, String buttonLabel) {
			super("questcrafting", title);

			this.npcName = npcName;
			this.questSlot = questSlot;
			this.product = product;
			this.requiredItems = requiredItems;
			this.waitingTime = waitingTime;
			this.canCraft = canCraft;
			this.buttonLabel = buttonLabel;

			setHideOnClose(true);
			setMinimizable(true);
			setMovable(true);

			setContent(buildContent());

			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					pack();
					j2DClient client = j2DClient.get();
					if (client != null) {
						client.addWindow(QuestCraftingWindow.this);
						center();
						getParent().validate();
					}
				}
			});
		}

		private JComponent buildContent() {
			JComponent content = SBoxLayout.createContainer(SBoxLayout.VERTICAL, SBoxLayout.COMMON_PADDING);
			content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			JComponent columns = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, SBoxLayout.COMMON_PADDING);
			columns.setAlignmentX(Component.LEFT_ALIGNMENT);
			columns.add(createColumn("Wymagane przedmioty", requiredItems, true), SLayout.EXPAND_AXIAL);
			columns.add(createColumn("Rezultat", toList(product), false), SLayout.EXPAND_AXIAL);
			content.add(columns);

			content.add(createFooter());

			if (!canCraft) {
				JLabel warning = new JLabel("Brakuje wymaganych przedmiotów.");
				warning.setForeground(Color.RED);
				content.add(warning);
			}

			return content;
		}

		private JComponent createColumn(String title, List<CraftingItem> items, boolean showQuantity) {
			JComponent column = SBoxLayout.createContainer(SBoxLayout.VERTICAL, SBoxLayout.COMMON_PADDING);
			column.setAlignmentY(Component.TOP_ALIGNMENT);

			JLabel heading = new JLabel(title);
			heading.setFont(heading.getFont().deriveFont(Font.BOLD));
			column.add(heading);

			if (items == null || items.isEmpty()) {
				JLabel placeholder = new JLabel("Brak danych o wymaganych przedmiotach.");
				column.add(placeholder);
				return column;
			}

			for (CraftingItem item : items) {
				column.add(createItemRow(item, showQuantity));
			}
			return column;
		}

		private JComponent createItemRow(CraftingItem item, boolean showQuantity) {
			JComponent row = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, SBoxLayout.COMMON_PADDING);
			row.setAlignmentX(Component.LEFT_ALIGNMENT);

			if (showQuantity) {
				JLabel quantity = new JLabel(item.quantity + "x");
				row.add(quantity);
			}

			Sprite sprite = item.loadSprite();
			ImageIcon icon = new ImageIcon(sprite.getImage());
			JLabel iconLabel = new JLabel(icon);
			row.add(iconLabel);

			JLabel name = new JLabel(item.name);
			row.add(name, SLayout.EXPAND_AXIAL);

			return row;
		}

		private JComponent createFooter() {
			JComponent footer = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, SBoxLayout.COMMON_PADDING);
			footer.setAlignmentX(Component.LEFT_ALIGNMENT);

			if (waitingTime > 0) {
				footer.add(new JLabel("Czas produkcji: " + waitingTime + " min"));
			} else {
				footer.add(new JLabel(""));
			}

			SBoxLayout.addSpring(footer);

			JButton craftButton = new JButton(buttonLabel);
			craftButton.setEnabled(canCraft && npcName != null && questSlot != null);
			craftButton.addActionListener(e -> sendCraftAction());
			footer.add(craftButton);

			return footer;
		}

		private void sendCraftAction() {
			if (!canCraft || npcName == null || questSlot == null) {
				return;
			}

			RPAction action = new RPAction();
			action.put("type", QUEST_CRAFT);
			action.put("npc", npcName);
			action.put("quest", questSlot);
			ClientSingletonRepository.getClientFramework().send(action);
			setVisible(false);
		}

		private List<CraftingItem> toList(CraftingItem item) {
			List<CraftingItem> list = new ArrayList<CraftingItem>(1);
			list.add(item);
			return list;
		}
	}

	private static final class CraftingItem {
		private final String name;
		private final int quantity;
		private final String clazz;
		private final String subclass;

		private CraftingItem(String name, int quantity, String clazz, String subclass) {
			this.name = name;
			this.quantity = quantity;
			this.clazz = clazz;
			this.subclass = subclass;
		}

		static CraftingItem parse(String encoded) {
			if (encoded == null || encoded.isEmpty()) {
				return null;
			}

			String[] parts = encoded.split("\\|");
			if (parts.length < 4) {
				return null;
			}

			int quantity = 1;
			try {
				quantity = Integer.parseInt(parts[1]);
			} catch (NumberFormatException e) {
				logger.warn("Invalid quantity in quest crafting payload: " + encoded, e);
			}

			return new CraftingItem(parts[0], Math.max(1, quantity), parts[2], parts[3]);
		}

		static List<CraftingItem> parseList(String encoded) {
			List<CraftingItem> result = new ArrayList<CraftingItem>();
			if (encoded == null || encoded.isEmpty()) {
				return result;
			}

			String[] entries = encoded.split(";");
			for (String entry : entries) {
				if (entry == null || entry.isEmpty()) {
					continue;
				}
				CraftingItem item = parse(entry);
				if (item != null) {
					result.add(item);
				}
			}
			return result;
		}

		Sprite loadSprite() {
			String path = "/data/sprites/items/" + clazz + "/" + subclass + ".png";
			Sprite sprite = SpriteStore.get().getSprite(path);
			if (sprite == null) {
				sprite = SpriteStore.get().getFailsafe();
			}
			return sprite;
		}
	}
}
