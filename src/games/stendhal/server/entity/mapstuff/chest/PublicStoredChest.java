package games.stendhal.server.entity.mapstuff.chest;

import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.Definition.Type;

public class PublicStoredChest extends StoredChest {
	private static final String CHEST_RPCLASS_NAME = "chest_public";

	public PublicStoredChest() {
		setRPClass(CHEST_RPCLASS_NAME);
		put("type", CHEST_RPCLASS_NAME);
	}

	/**
	 * Creates a new chest.
	 *
	 * @param object
	 *            RPObject
	 */
	public PublicStoredChest(final RPObject object) {
		super(object);
		setRPClass(CHEST_RPCLASS_NAME);
		put("type", CHEST_RPCLASS_NAME);
	}

	public static void generateRPClass() {
		if (!RPClass.hasRPClass(CHEST_RPCLASS_NAME)) {
			final RPClass chest = new RPClass(CHEST_RPCLASS_NAME);
			chest.isA("entity");
			chest.addAttribute("open", Type.FLAG);
			chest.addRPSlot("content", 36);
		}
	}
}
