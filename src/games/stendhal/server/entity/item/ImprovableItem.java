package games.stendhal.server.entity.item;

import java.util.Map;

public class ImprovableItem extends Item {
	public ImprovableItem(String name, String clazz, String subclass, Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	public ImprovableItem(final ImprovableItem item) {
		super(item);
	}

	public void upgrade() {
		put("improves", getImprove() + 1);
	}

	@Override
	public String getDescription() {
		return super.getDescription() + " " + getImproveNumber() + ".";
	}

	@Override
	public int getAttack() {
		if (has("atk")) {
			return super.getAttack() + getMaxImproves();
		}
		return 0;
	}
	@Override
	public int getDefense() {
		if (has("def")) {
			return super.getDefense() + getMaxImproves();
		}
		return 0;
	}
	@Override
	public int getRangedAttack() {
		if (has("ratk")) {
			return super.getRangedAttack() + getMaxImproves();
		}
		return 0;
	}

	private String getImproveNumber() {
		final String improve = String.valueOf(getMaxImproves());

		return "Ulepszenie +" + improve;
	}

	public int getMaxImproves() {
		return getInt("max_improves");
	}
	public int getImprove() {
		return getInt("improve");
	}
}
