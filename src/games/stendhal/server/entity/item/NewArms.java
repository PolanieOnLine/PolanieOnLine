package games.stendhal.server.entity.item;

import java.util.Map;

public class NewArms extends StatusResistantItem {
	/**
	 * Default constructor.
	 *
	 * @param name
	 * 		Item's name
	 * @param clazz
	 * 		Item's class or type
	 * @param subclass
	 * 		Item's subclass
	 * @param attributes
	 * 		Attributes available to this item
	 */
	public NewArms(final String name, final String clazz, final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 * 		Item to copy
	 */
	public NewArms(final NewArms item) {
		super(item);
	}

	@Override
	public String describe() {
		return super.itemDescribe();
	}
}
