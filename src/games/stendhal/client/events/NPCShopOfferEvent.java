package games.stendhal.client.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.shop.ShopItem;
import games.stendhal.client.gui.shop.ShopOfferData;
import games.stendhal.client.gui.shop.ShopWindowManager;
import games.stendhal.common.constants.Events;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

class NPCShopOfferEvent extends Event<RPEntity> {
	@Override
	public void execute() {
		if (event == null) {
			return;
		}

		String status = event.has("status") ? event.get("status") : "";
		String npcName = event.has("npc") ? event.get("npc") : "";

		if ("close".equalsIgnoreCase(status)) {
			ShopWindowManager.get().close(npcName);
			return;
		}

		if (!"open".equalsIgnoreCase(status)) {
			return;
		}

		ShopOfferData data = buildOfferData(npcName);
		if (data != null) {
			ShopWindowManager.get().openShop(data);
		}
	}

	private ShopOfferData buildOfferData(String npcName) {
		String title = event.has("title") ? event.get("title") : npcName;
		String background = event.has("background") ? event.get("background") : null;

		List<ShopItem> selling = readItemsFromSlot("sell");
		List<ShopItem> buying = readItemsFromSlot("buy");

		if (selling.isEmpty() && buying.isEmpty()) {
			return null;
		}

		return new ShopOfferData(npcName, title, background, selling, buying);
	}

	private List<ShopItem> readItemsFromSlot(String slotName) {
		if (!event.has(slotName)) {
			return Collections.emptyList();
		}

		RPSlot slot = event.getSlot(slotName);
		if (slot == null || slot.size() == 0) {
			return Collections.emptyList();
		}

		List<ShopItem> items = new ArrayList<ShopItem>(slot.size());
		for (RPObject object : slot) {
			items.add(ShopItem.from(object));
		}
		return items;
	}

	@Override
	public void init(RPEntity entity, RPEvent event) {
		super.init(entity, event);
		if (!Events.NPC_SHOP_OFFER.equals(event.getName())) {
			throw new IllegalArgumentException("Unexpected event type: " + event.getName());
		}
	}
}
