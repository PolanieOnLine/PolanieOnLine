package games.stendhal.server.events;

import java.util.Collection;

import org.apache.log4j.Logger;

import games.stendhal.server.entity.item.Item;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPSlot;
import marauroa.common.game.SyntaxException;

public class ShowItemRecipeListEvent extends RPEvent {
	private static final Logger logger = Logger.getLogger(ShowItemRecipeListEvent.class);

	private static final String RPCLASS_NAME = "show_item_recipe_list";
	private static final String TITLE = "title";
	private static final String CAPTION = "caption";
	private static final String ITEM_SLOT = "content";
	private static final String ITEMS_RECIPE_SLOT = "content_recipe";

	/**
	 * Creates the rpclass.
	 */
	public static void generateRPClass() {
		try {
			final RPClass rpclass = new RPClass(RPCLASS_NAME);
			rpclass.addAttribute(TITLE, Type.STRING, Definition.PRIVATE);
			rpclass.addAttribute(CAPTION, Type.STRING, Definition.PRIVATE);
			rpclass.addRPSlot(ITEM_SLOT, 999);
			rpclass.addRPSlot(ITEMS_RECIPE_SLOT, 999);
		} catch (final SyntaxException e) {
			logger.error("cannot generateRPClass", e);
		}
	}

	public ShowItemRecipeListEvent(final String title, final String caption, final Collection<Item> items, final Collection<Item> items_recipe) {
		super(RPCLASS_NAME);
		put(TITLE, title);
		if (caption != null) {
			put(CAPTION, caption);
		}
		super.addSlot(ITEM_SLOT);
		RPSlot slot = super.getSlot(ITEM_SLOT);
		for (Item item : items) {
			slot.add(item);
		}
		super.addSlot(ITEMS_RECIPE_SLOT);
		RPSlot slot_recipe = super.getSlot(ITEMS_RECIPE_SLOT);
		for (Item item : items_recipe) {
			slot_recipe.add(item);
		}
	}
}
