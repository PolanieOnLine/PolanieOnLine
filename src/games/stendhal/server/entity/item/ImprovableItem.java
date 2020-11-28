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

	public int getImprove() {
		return getInt("improve");
	}
	public int getMaxImproves() {
		return getInt("max_improves");
	}

	/**
	 * Sets the item to increase up.
	 */
	public void upgrade() {
		put("improve", setImproves());
	}

	/**
	 * Increasing the attack of item.
	 */
	@Override
	public int getAttack() {
		if (has("atk")) {
			return super.getAttack() + getImprove();
		}
		return 0;
	}
	/**
	 * Increasing the defense of item.
	 */
	@Override
	public int getDefense() {
		if (has("def")) {
			return super.getDefense() + getImprove();
		}
		return 0;
	}
	/**
	 * Increasing the range attack of item.
	 */
	@Override
	public int getRangedAttack() {
		if (has("ratk")) {
			return super.getRangedAttack() + getImprove();
		}
		return 0;
	}
	/**
	 * Decreasing the rate attack of item when item has been max improved.
	 */
	@Override
	public int getAttackRate() {
		if (has("rate")) {
			if (isMaxImproved() && super.getAttackRate() > 2) {
				return super.getAttackRate() - 1;
			}
			return super.getAttackRate();
		}
		return Item.getDefaultAttackRate();
	}
	/**
	 * Increasing the range of bows and wands when item has been max improved.
	 */
	@Override
	public int getRange() {
		if (has("range")) {
			if (isMaxImproved()) {
				return super.getRange() + 1;
			}
			return super.getRange();
		}
		return 0;
	}

	/**
	 * Checks the improvable state of item.
	 * 
	 * @return
	 * 		<code>true</code> if the item has a possibility to be upgraded.
	 */
	public boolean isUpgradeable() {
		return has("max_improves") && getMaxImproves() > 0;
	}

	/**
	 * Checks max of possibility improves for item.
	 * 
	 * @return
	 * 		<code>true</code> if the item has been max improved.
	 */
	private int setImproves() {
		// fix it if value of 'max_improves' has been changed and it is lower than before
		// FIXME: należy przenieść ten kod
		if (getImprove() > getMaxImproves()) {
			return getMaxImproves();
		}

		if (isMaxImproved()) {
			return getMaxImproves();
		}
		return getImprove() + 1;
	}

	public boolean isMaxImproved() {
		if (getImprove() == getMaxImproves()) {
			return true;
		}
		return false;
	}

	/**
	 * @return description
	 */
	private String getImproveNumber() {
		final String improve = String.valueOf(getImprove());

		return "Ulepszenie +" + improve;
	}

	/**
	 * Change item description for upgraded one.
	 */
	@Override
	public String getDescription() {
		return super.getDescription() + " " + getImproveNumber() + ".";
	}
}
