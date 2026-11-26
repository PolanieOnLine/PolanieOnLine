package games.stendhal.server.entity.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class QuickRecoveryAffixTest {
	@BeforeClass
	public static void setupWorld() {
		MockStendlRPWorld.get();
	}

	@Test
	public void eachSupportedStatusSchedulesItsShorterExpiryAndCanStillBeRemovedNormally() {
		Status[] statuses = {new StunnedStatus(6), new ConfuseStatus()};
		for (Status status : statuses) {
			Player player = PlayerTestHelper.createPlayer("quick recovery");
			Item armour = new Item("recovery armour", "armor", "test", null);
			armour.put(ItemTooltip.LEGENDARY_QUICK_RECOVERY, 1.0);
			player.getSlot("armor").add(armour);
			TurnNotifier notifier = TurnNotifier.get();
			Set<TurnListener> before = listeners(notifier);
			assertTrue(player.getStatusList().inflictStatus(status, null));
			Set<TurnListener> added = listeners(notifier);
			added.removeAll(before);
			assertEquals(1, added.size());
			TurnListener expiry = added.iterator().next();
			try {
				assertTrue(player.getStatusList().hasStatus(status.getStatusType()));
				assertEquals(status instanceof ConfuseStatus ? 141 : 15, notifier.getRemainingTurns(expiry));
				expiry.onTurnReached(notifier.getCurrentTurnForDebugging() + notifier.getRemainingTurns(expiry));
				assertFalse(player.getStatusList().hasStatus(status.getStatusType()));
			} finally {
				notifier.dontNotify(expiry);
			}
		}
	}

	@Test
	public void quickRecoveryDoesNotShortenAuthoredCreatureSlow() {
		for (HeavyStatus status : new HeavyStatus[] {new HeavyStatus(6), new HeavyStatus()}) {
			Player player = PlayerTestHelper.createPlayer("creature slow target");
			Item armour = new Item("recovery armour", "armor", "test", null);
			armour.put(ItemTooltip.LEGENDARY_QUICK_RECOVERY, 1.0);
			player.getSlot("armor").add(armour);
			Creature attacker = new Creature();
			attacker.setName("slow creature");
			double originalSpeed = player.getBaseSpeed();
			TurnNotifier notifier = TurnNotifier.get();
			Set<TurnListener> before = listeners(notifier);
			assertTrue(player.getStatusList().inflictStatus(status, attacker));
			Set<TurnListener> added = listeners(notifier);
			added.removeAll(before);
			assertEquals(1, added.size());
			TurnListener expiry = added.iterator().next();
			try {
				assertEquals(originalSpeed / 2, player.getBaseSpeed(), 0.0);
				int turns = notifier.getRemainingTurns(expiry);
				if (status.getDurationSeconds() != null) {
					assertEquals(21, turns);
				} else {
					assertTrue(turns >= SingletonRepository.getRPWorld().getTurnsInSeconds(99) + 1);
					assertTrue(turns <= SingletonRepository.getRPWorld().getTurnsInSeconds(990) + 1);
				}
				assertTrue(player.getStatusList().inflictStatus(new HeavyStatus(1), attacker));
				assertEquals(originalSpeed / 2, player.getBaseSpeed(), 0.0);
				assertEquals(turns, notifier.getRemainingTurns(expiry));
				expiry.onTurnReached(notifier.getCurrentTurnForDebugging() + turns);
				assertFalse(player.getStatusList().hasStatus(StatusType.HEAVY));
				assertEquals(originalSpeed, player.getBaseSpeed(), 0.0);
			} finally {
				player.getStatusList().removeAll(StatusType.HEAVY);
				notifier.dontNotify(expiry);
			}
		}
	}

	private Set<TurnListener> listeners(final TurnNotifier notifier) {
		Set<TurnListener> result = new HashSet<TurnListener>();
		for (Set<TurnListener> listeners : notifier.getEventListForDebugging().values()) {
			result.addAll(listeners);
		}
		return result;
	}
}
