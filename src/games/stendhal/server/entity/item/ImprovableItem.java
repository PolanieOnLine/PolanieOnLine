package games.stendhal.server.entity.item;

import java.util.Map;

public class ImprovableItem extends Item {

	public ImprovableItem(String name, String clazz, String subclass, Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}
	public ImprovableItem(final ImprovableItem item) {
		super(item);
	}

	@Override
	public String getDescription() {
		return super.getDescription() + " " + getImproveNumber() + ".";
	}

	public boolean isUpgradeable() {
		return getMaxImproves() > 0;
	}

	public void upgrade() {
		put("improve", maxImproves());
	}
	private int maxImproves() {
		if (getImprove() == getMaxImproves()) {
			return getMaxImproves();
		}
		return getImprove() + 1;
	}

	@Override
	public int getAttack() {
		if (has("atk")) {
			return super.getAttack() + getImprove();
		}
		return 0;
	}
	@Override
	public int getDefense() {
		if (has("def")) {
			return super.getDefense() + getImprove();
		}
		return 0;
	}
	@Override
	public int getRangedAttack() {
		if (has("ratk")) {
			return super.getRangedAttack() + getImprove();
		}
		return 0;
	}

	private String getImproveNumber() {
		final String improve = String.valueOf(getImprove());

		return "Ulepszenie +" + improve;
	}

	public int getMaxImproves() {
		return getInt("max_improves");
	}
	public int getImprove() {
		return getInt("improve");
	}
}
