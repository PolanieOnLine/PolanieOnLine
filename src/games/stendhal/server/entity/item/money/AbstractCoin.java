package games.stendhal.server.entity.item.money;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;

public abstract class AbstractCoin extends StackableItem {
	protected int conversionRate = 100; // 100 niższych = 1 wyższy

	public AbstractCoin(String name, int value) {
		super(name, "money", name, null);
		setQuantity(1);
	}

	public AbstractCoin(String name, int value, int quantity) {
		this(name, value);
		setQuantity(quantity);
	}

	/**
	 * Override add() żeby automatycznie konwertować monety przy łączeniu stacków.
	 */
	@Override
	public int add(final StackableItem other) {
		int newQuantity = super.add(other);

		try {
			RPObject container = getContainer();
			while (container != null && !(container instanceof Player)) {
				container = container.getContainer();
			}
			if (container instanceof Player) {
				AbstractCoin coin = (AbstractCoin) this;
				coin.tryAutoConvert((Player) container);
			}
		} catch (Exception e) {
			// bezpieczne pominięcie błędów
		}

		return newQuantity;
	}

	/**
	 * Próbuje automatycznie wymienić niższe monety na wyższą jednostkę.
	 */
	public void tryAutoConvert(final Player player) {
		if (!canConvert()) return;

		int total = getQuantity();
		if (total >= conversionRate) {
			int higherCount = total / conversionRate;
			int remaining = total % conversionRate;

			if (remaining > 0) {
				setQuantity(remaining);
			} else {
				removeFromWorld(); // usuń stack z 0 sztuk
			}

			StackableItem item = (StackableItem) SingletonRepository
					.getEntityManager()
					.getItem(getHigherTierName());
			item.setQuantity(higherCount);
			AbstractCoin higherCoin = (AbstractCoin) item;

			if (higherCoin != null) {
				player.equipOrPutOnGround(higherCoin);
				player.sendPrivateText("Twoje monety zostały automatycznie wymienione na "
						+ higherCount + " × " + higherCoin.getName() + ".");
			}

			player.notifyWorldAboutChanges();
		}
	}

	protected abstract String getHigherTierName();
	protected abstract boolean canConvert();
	protected abstract AbstractCoin createHigherTier(int amount);
}
