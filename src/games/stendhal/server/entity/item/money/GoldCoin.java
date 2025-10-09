package games.stendhal.server.entity.item.money;

public class GoldCoin extends AbstractCoin {
	public GoldCoin() {
		super("dukat", 10000);
	}

	public GoldCoin(int amount) {
		this();
		setQuantity(amount);
	}

	@Override
	protected boolean canConvert() {
		return false;
	}

	@Override
	protected AbstractCoin createHigherTier(int amount) {
		return null; // brak wyższej jednostki
	}

	@Override
	protected String getHigherTierName() {
		return null; // brak wyższej monety
	}
}
