package games.stendhal.client.gui.shop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import marauroa.common.game.RPObject;

public class ShopItem {
	public enum Category {
		WEAPON,
		ELIXIR,
		OTHER
	}

	private static final Set<String> WEAPON_CLASSES;
	private static final List<String> ELIXIR_KEYWORDS;

	static {
		Set<String> classes = new HashSet<String>(Arrays.asList(
			"axe",
			"bow",
			"club",
			"dagger",
			"hammer",
			"knife",
			"mace",
			"spear",
			"staff",
			"sword",
			"weapon",
			"whip"
		));
		WEAPON_CLASSES = Collections.unmodifiableSet(classes);

		List<String> keywords = new ArrayList<String>();
		keywords.add("eliksir");
		keywords.add("elixir");
		keywords.add("potion");
		keywords.add("wywar");
		ELIXIR_KEYWORDS = Collections.unmodifiableList(keywords);
	}

	private final String name;
	private final String description;
	private final String flavorText;
	private final int priceCopper;
	private final boolean stackable;
	private final String itemClass;
	private final String itemSubclass;
	private final Category category;

	private Sprite sprite;

	private ShopItem(String name, String description, String flavorText, int priceCopper, boolean stackable, String itemClass, String itemSubclass) {
		this.name = name;
		this.description = description;
		this.flavorText = flavorText;
		this.priceCopper = priceCopper;
		this.stackable = stackable;
		this.itemClass = itemClass;
		this.itemSubclass = itemSubclass;
		this.category = determineCategory();
	}

	public static ShopItem from(RPObject object) {
		String name = object.get("name");
		String description = object.has("description_info") ? object.get("description_info") : "";
		String flavor = object.has("flavor_text") ? object.get("flavor_text") : "";
		int price = object.has("price_copper") ? object.getInt("price_copper") : (object.has("price") ? object.getInt("price") : 0);
		boolean stackable = object.has("stackable") && (object.getInt("stackable") > 0);
		String clazz = object.has("class") ? object.get("class") : "";
		String subclass = object.has("subclass") ? object.get("subclass") : "";
		return new ShopItem(name, description, flavor, price, stackable, clazz, subclass);
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getFlavorText() {
		return flavorText;
	}

	public int getPriceCopper() {
		return priceCopper;
	}

	public boolean isStackable() {
		return stackable;
	}

	public String getItemClass() {
		return itemClass;
	}

	public String getItemSubclass() {
		return itemSubclass;
	}

	public Category getCategory() {
		return category;
	}

	public Sprite getSprite() {
		if (sprite == null) {
			SpriteStore store = ClientSingletonRepository.getSpriteStore();
			String path = getSpritePath();
			Sprite loaded = store.getSprite(path);
			if (loaded == null) {
				loaded = store.getFailsafe();
			}
			if ((loaded != null) && !loaded.isConstant()) {
				loaded = store.getAnimatedSprite(loaded, 100);
			}
			sprite = loaded;
		}
		return sprite;
	}

	public String getSpritePath() {
		String clazz = (itemClass == null || itemClass.isEmpty()) ? "misc" : itemClass.toLowerCase(Locale.ROOT);
		String subclass = (itemSubclass == null || itemSubclass.isEmpty()) ? clazz : itemSubclass.toLowerCase(Locale.ROOT);
		return "/data/sprites/items/" + clazz + "/" + subclass + ".png";
	}

	private Category determineCategory() {
		String clazz = itemClass == null ? "" : itemClass.toLowerCase(Locale.ROOT);
		String subclass = itemSubclass == null ? "" : itemSubclass.toLowerCase(Locale.ROOT);
		String lowerName = name == null ? "" : name.toLowerCase(Locale.ROOT);

		if (WEAPON_CLASSES.contains(clazz) || WEAPON_CLASSES.contains(subclass)) {
			return Category.WEAPON;
		}

		if ("drink".equals(clazz) || "potion".equals(clazz)) {
			for (String keyword : ELIXIR_KEYWORDS) {
				if (subclass.contains(keyword) || lowerName.contains(keyword)) {
					return Category.ELIXIR;
				}
			}
		}

		for (String keyword : ELIXIR_KEYWORDS) {
			if (subclass.contains(keyword) || lowerName.contains(keyword)) {
				return Category.ELIXIR;
			}
		}

		return Category.OTHER;
	}
}
