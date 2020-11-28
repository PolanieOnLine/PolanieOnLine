package games.stendhal.server.entity.item;

import java.util.Map;

/**
 * @author KarajuSs
 */
public class Improve extends ImprovableItem {
	public Improve(String name, String clazz, String subclass, Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}
	public Improve(final Improve item) {
		super(item);
	}
}