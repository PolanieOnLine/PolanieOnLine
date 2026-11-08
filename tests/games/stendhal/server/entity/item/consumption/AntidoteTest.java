package games.stendhal.server.entity.item.consumption;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.item.ConsumableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.EatStatus;
import games.stendhal.server.entity.status.EatStatusTurnListener;
import games.stendhal.server.entity.status.PoisonStatus;
import games.stendhal.server.entity.status.PoisonStatusTurnListener;
import games.stendhal.server.entity.status.StatusType;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class AntidoteTest {
	private Player player;

	@BeforeClass
	public static void setUpClass() {
		MockStendlRPWorld.get();
	}

	@Before
	public void setUp() {
		player = PlayerTestHelper.createPlayer("antidote_test");
		player.setBaseHP(1000);
		player.setHP(500);
	}

	@After
	public void tearDown() {
		player.getStatusList().removeAll();
		TurnNotifier.get().dontNotify(new PoisonStatusTurnListener(player.getStatusList()));
		TurnNotifier.get().dontNotify(new StatusHealerEater(player, StatusType.POISONED));
		TurnNotifier.get().dontNotify(new EatStatusTurnListener(player.getStatusList()));
	}

	@Test
	public void variantsNeutralizeTheirOwnCapacityWithoutImmunity() {
		final String[] names = {"antidotum", "mocne antidotum"};
		final int[] strengths = {400, 800};
		for (int index = 0; index < names.length; index++) {
			final ConsumableItem antidote = item(names[index]);
			final PoisonStatus poison = poison(2000, 100);
			assertTrue(antidote.onUsed(player));
			assertEquals(2000 - strengths[index], remainingDamage(poison));
			assertFalse(player.getStatusList().isImmune(StatusType.POISONED));
			assertEquals(1, antidote.getQuantity());
			player.getStatusList().removeAll();
		}
	}

	@Test
	public void onlyRemainingPoisonIsReducedAndHealthIsNotRestored() {
		final PoisonStatus poison = poison(1000, 100);
		poison.consume();
		poison.consume();
		poison.consume();
		final ConsumableItem antidote = item("antidotum");
		assertTrue(FeederFactory.get(antidote).feed(antidote, player));
		assertEquals(300, remainingDamage(poison));
		assertEquals(500, player.getHP());
	}

	@Test
	public void capacityIsSharedAcrossPoisonsStartingWithTheMostDangerous() {
		final PoisonStatus slow = poison(500, 10);
		final PoisonStatus fast = poison(300, 100);
		final ConsumableItem antidote = item("antidotum");
		assertTrue(FeederFactory.get(antidote).feed(antidote, player));
		assertTrue(fast.consumed());
		assertEquals(400, remainingDamage(slow));
	}

	@Test
	public void noPoisonDoesNotConsumeTheBottleOrCreateProtection() {
		final ConsumableItem antidote = item("antidotum");
		assertFalse(FeederFactory.get(antidote).feed(antidote, player));
		assertEquals(2, antidote.getQuantity());
		assertFalse(player.getStatusList().isImmune(StatusType.POISONED));
	}

	@Test
	public void completeCureClearsIndicatorAndAllowsPoisoningAgain() {
		poison(150, 10);
		final ConsumableItem antidote = item("antidotum");
		assertTrue(FeederFactory.get(antidote).feed(antidote, player));
		assertFalse(player.hasStatus(StatusType.POISONED));
		assertFalse(player.has("poisoned"));
		poison(100, 10);
		assertTrue(player.hasStatus(StatusType.POISONED));
	}

	@Test
	public void treatmentDoesNotRemoveOtherStatuses() {
		player.getStatusList().inflictStatus(new EatStatus(100, 5, 10), null);
		poison(150, 10);
		final ConsumableItem antidote = item("antidotum");
		assertTrue(FeederFactory.get(antidote).feed(antidote, player));
		assertTrue(player.hasStatus(StatusType.EATING));
	}

	@Test
	public void questAntivenomRemainsAQuestIngredient() {
		final PoisonStatus poison = poison(200, 10);
		final ConsumableItem antivenom = item("antyjad");
		assertFalse(FeederFactory.get(antivenom).feed(antivenom, player));
		assertEquals(2, antivenom.getQuantity());
		assertEquals(200, remainingDamage(poison));
	}

	private ConsumableItem item(final String name) {
		final ConsumableItem item = (ConsumableItem) SingletonRepository.getEntityManager().getItem(name);
		item.setQuantity(2);
		assertTrue(player.equipToInventoryOnly(item));
		return item;
	}

	private PoisonStatus poison(final int total, final int tick) {
		final PoisonStatus poison = new PoisonStatus(-total, 5, -tick);
		assertTrue(player.getStatusList().inflictStatus(poison, null));
		return poison;
	}

	private int remainingDamage(final PoisonStatus poison) {
		int damage = 0;
		while (!poison.consumed()) {
			damage -= poison.consume();
		}
		return damage;
	}
}
