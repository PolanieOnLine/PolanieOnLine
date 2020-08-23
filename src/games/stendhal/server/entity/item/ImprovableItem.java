package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

public class ImprovableItem extends Item {
	private boolean notified = false;

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
		if (getImprove() >= getMaxImproves()) {
			put("improve", getImprove() + 1);
			notified = false;
		} else {
			notified = true;
		}
	}

	@Override
	public void deteriorate() {
		put("improve", 0);
	}

	@Override
	public void deteriorate(final RPEntity player) {
		deteriorate();

		if (getImprove() == getMaxImproves()) {
			onMaxImproves(player);
		}
	}

	private void onMaxImproves(final RPEntity player) {
		if (!notified) {
			if (player instanceof Player) {
				((Player) player).sendPrivateText("Twój przedmiot " + getName() + " jest już maksymalnie ulepszony.");
				notified = true;
			}
		}
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
