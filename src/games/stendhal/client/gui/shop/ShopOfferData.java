package games.stendhal.client.gui.shop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ShopOfferData {
	private final String npcName;
	private final String title;
	private final String backgroundPath;
	private final List<ShopItem> sellingItems;
	private final List<ShopItem> buyingItems;

	public ShopOfferData(String npcName, String title, String backgroundPath, List<ShopItem> sellingItems, List<ShopItem> buyingItems) {
		this.npcName = npcName;
		this.title = title;
		this.backgroundPath = backgroundPath;
		this.sellingItems = Collections.unmodifiableList(new ArrayList<ShopItem>(sellingItems));
		this.buyingItems = Collections.unmodifiableList(new ArrayList<ShopItem>(buyingItems));
	}

	public String getNpcName() {
		return npcName;
	}

	public String getTitle() {
		return title;
	}

	public String getBackgroundPath() {
		return backgroundPath;
	}

	public List<ShopItem> getSellingItems() {
		return sellingItems;
	}

	public List<ShopItem> getBuyingItems() {
		return buyingItems;
	}
}
