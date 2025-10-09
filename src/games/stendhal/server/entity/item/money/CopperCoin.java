package games.stendhal.server.entity.item.money;

public class CopperCoin extends AbstractCoin {
	public CopperCoin() {
		super("miedziak", 1);
	}

	public CopperCoin(int amount) {
		this();
		setQuantity(amount);
	}

	@Override
	protected boolean canConvert() {
		return true; // miedziaki można zamieniać
	}

	@Override
	protected AbstractCoin createHigherTier(int amount) {
		return new SilverCoin(amount);
	}

	@Override
	protected String getHigherTierName() {
		return "talar";
	}
}
