package games.stendhal.server.entity.item;

import java.util.Map;

/**
 * Items are improvable.
 * 
 * @author KarajuSs
 */
public class ImprovableItem extends Item {

	public ImprovableItem(String name, String clazz, String subclass, Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}
	public ImprovableItem(final ImprovableItem item) {
		super(item);
	}

	/**
	 * Change item description for upgraded one.
	 */
	@Override
	public String getDescription() {
		return super.getDescription() + " " + getImproveNumber() + ".";
	}

	/**
	 * Checks the improvable state of the item.
	 * 
	 * @return
	 * 		<code>true</code> if the item has a possibility to be upgraded.
	 */
	public boolean isUpgradeable() {
		return getMaxImproves() > 0;
	}

	/**
	 * Sets the item improve up.
	 */
	public void upgrade() {
		put("improve", maxImproves());
	}

	/**
	 * Checks max of possibility improves for item.
	 * 
	 * @return
	 * 		<code>true</code> if the item has been max improved.
	 */
	private int maxImproves() {
		if (getImprove() == getMaxImproves()) {
			return getMaxImproves();
		}
		return getImprove() + 1;
	}

	/**
	 * Increasing attack from item.
	 */
	@Override
	public int getAttack() {
		if (has("atk")) {
			return super.getAttack() + getImprove();
		}
		return 0;
	}
	/**
	 * Increasing defense from item.
	 */
	@Override
	public int getDefense() {
		if (has("def")) {
			return super.getDefense() + getImprove();
		}
		return 0;
	}
	/**
	 * Increasing range attack from item.
	 */
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
