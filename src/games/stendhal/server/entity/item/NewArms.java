package games.stendhal.server.entity.item;

import java.util.Map;

public class NewArms extends StatusResistantItem {
	public NewArms(StatusResistantItem item) {
		super(item);
	}

	public NewArms(String name, String clazz, String subclass, Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	@Override
	public String describe() {
		return super.itemDescribe();
	}
}
