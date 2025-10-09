package games.stendhal.server.entity.item.money;

public class SilverCoin extends AbstractCoin {
	public SilverCoin() {
		super("talar", 100);
	}

	public SilverCoin(int amount) {
		this();
		setQuantity(amount);
	}

	@Override
	protected boolean canConvert() {
		return true; // srebrniki można zamieniać dalej
	}

	@Override
	protected AbstractCoin createHigherTier(int amount) {
		return new GoldCoin(amount);
	}

	@Override
	protected String getHigherTierName() {
		return "dukat";
	}
}
