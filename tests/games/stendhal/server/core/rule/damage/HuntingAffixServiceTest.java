package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.ConsumableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.HeavyStatus;
import games.stendhal.server.entity.status.PoisonStatus;
import games.stendhal.server.entity.status.PoisonAttacker;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import utilities.PlayerTestHelper;

public class HuntingAffixServiceTest {
	@BeforeClass
	public static void initialize() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
	}

	@Test
	public void lastStandUsesNormalMaximumHealthAndDoesNotStack() {
		final Player player = player("last-stand");
		player.getSlot("pas").add(affix("belts", "legendary_last_stand", 1));
		player.getSlot("glove").add(affix("glove", "legendary_last_stand", 1));
		player.setHP(3000);
		assertEquals(100, HuntingAffixService.outgoingDamage(player, 100));
		player.setHP(2999);
		assertEquals(120, HuntingAffixService.outgoingDamage(player, 100));
		assertEquals(0, HuntingAffixService.outgoingDamage(player, 0));
	}

	@Test
	public void defenseCircleCountsOnlyAdditionalNearbyActiveCreaturesAndCapsAtTwelvePercent() {
		final Player player = player("circle");
		player.getSlot("armor").add(affix("armor", "legendary_defense_circle", 1));
		final Creature attacker = creature(player, 100);
		attacker.setTarget(player);
		assertEquals(100, HuntingAffixService.reduceCreatureHit(player, attacker, 100));
		final Creature additional = creature(player, 100);
		additional.setTarget(player);
		assertEquals(97, HuntingAffixService.reduceCreatureHit(player, attacker, 100));
		for (int index = 0; index < 5; index++) {
			creature(player, 100).setTarget(player);
		}
		assertEquals(88, HuntingAffixService.reduceCreatureHit(player, attacker, 100));
		assertEquals(1, HuntingAffixService.reduceCreatureHit(player, attacker, 1));
		assertEquals(100, HuntingAffixService.reduceCreatureHit(player, player, 100));
	}

	@Test
	public void inactiveAndDistantAttackersCannotProvideFreeDefense() {
		final Player player = player("circle-range");
		player.getSlot("armor").add(affix("armor", "legendary_defense_circle", 1));
		final Creature attacker = creature(player, 100);
		attacker.setTarget(player);
		creature(player, 100);
		final Creature distant = creature(player, 100);
		distant.setPosition(35, 35);
		distant.setTarget(player);
		assertEquals(100, HuntingAffixService.reduceCreatureHit(player, attacker, 100));
	}

	@Test
	public void poisonPotencyBoostsTheWholePoisonWithoutGrantingPoisonChance() {
		final Player player = player("poison-potency");
		final Item weapon = affix("dagger", "poison_potency", 0.25);
		player.getSlot("rhand").add(weapon);
		player.getSlot("glove").add(affix("glove", "poison_potency", 0.15));
		final PoisonStatus poison = HuntingAffixService.weaponPoison(player, 100, 2, 20);
		assertEquals(-140, poison.getAmount());
		assertEquals(-28, poison.getRegen());
		assertFalse(weapon.has("poison_on_hit"));
		int consumed = 0;
		while (!poison.consumed()) {
			consumed += poison.consume();
		}
		assertEquals(-140, consumed);
	}

	@Test
	public void nativeWeaponPoisonAlsoReceivesPotencyAndCanCreditItsKillingBlow() {
		final Player player = runner("native-poison");
		player.getSlot("glove").add(affix("glove", "poison_potency", 0.25));
		final Creature victim = creature(player, 100);
		final ConsumableItem poisonItem = (ConsumableItem) SingletonRepository.getEntityManager()
				.getItem("mocna trucizna");
		new PoisonAttacker(100, poisonItem).onAttackAttempt(victim, player);
		final PoisonStatus applied = (PoisonStatus) victim.getStatusList().getStatuses().get(0);
		assertEquals((int) Math.round(poisonItem.getAmount() * 1.25), applied.getAmount());
		assertSame(player, applied.getSource());
		victim.setHP(0);
		HuntingAffixService.onKilled(applied, victim);
		assertEquals(1.1, player.getSpeed(), 0.00001);
		HuntingAffixService.reset(player);
	}

	@Test
	public void killRefreshesPaceWithoutStackingAndExpiryKeepsTheOriginalBaseSpeed() {
		final Player player = runner("pace-refresh");
		final long now = System.currentTimeMillis();
		HuntingAffixService.onKill(player, creature(player, 0), now);
		assertEquals(1.1, player.getSpeed(), 0.00001);
		assertEquals(1.0, player.getBaseSpeed(), 0.00001);
		HuntingAffixService.onKill(player, creature(player, 0), now + 3000);
		assertEquals(1.1, player.getSpeed(), 0.00001);
		HuntingAffixService.tick(player, now + 4000);
		assertEquals(1.1, player.getSpeed(), 0.00001);
		HuntingAffixService.tick(player, now + 7000);
		assertEquals(1.0, player.getSpeed(), 0.00001);
	}

	@Test
	public void movementCanStartDuringPaceAndUnequippingBootsRemovesItImmediately() {
		final Player player = runner("pace-start");
		player.stop();
		HuntingAffixService.onKill(player, creature(player, 0), System.currentTimeMillis());
		assertTrue(player.stopped());
		assertEquals(1.1, player.getMovementSpeed(), 0.00001);
		player.setSpeed(player.getMovementSpeed());
		player.getBoots().onUnequipped();
		assertEquals(1.0, player.getSpeed(), 0.00001);
	}

	@Test
	public void slowAndPaceCanExpireInEitherOrderWithoutLeavingASpeedBonusBehind() {
		for (final boolean paceFirst : new boolean[] {true, false}) {
			final Player player = runner("pace-slow-" + paceFirst);
			final long now = System.currentTimeMillis();
			final HeavyStatus slow = new HeavyStatus(10);
			HuntingAffixService.onKill(player, creature(player, 0), now);
			player.getStatusList().inflictStatus(slow, null);
			assertEquals(0.55, player.getSpeed(), 0.00001);
			if (paceFirst) {
				HuntingAffixService.tick(player, now + 4000);
				assertEquals(0.5, player.getSpeed(), 0.00001);
				player.getStatusList().remove(slow);
			} else {
				player.getStatusList().remove(slow);
				assertEquals(1.1, player.getSpeed(), 0.00001);
				HuntingAffixService.tick(player, now + 4000);
			}
			assertEquals(1.0, player.getSpeed(), 0.00001);
		}
	}

	@Test
	public void sameNameSessionsAndInventoryBootsDoNotShareTheRunningBonus() {
		final Player first = runner("pace-session");
		final Player second = runner("pace-session");
		HuntingAffixService.onKill(first, creature(first, 0), System.currentTimeMillis());
		assertEquals(1.0, second.getMovementSpeed(), 0.00001);
		HuntingAffixService.reset(first);
		assertEquals(1.0, first.getSpeed(), 0.00001);
		final Player inventory = player("pace-bag");
		inventory.getSlot("bag").add(affix("boots", "legendary_hunters_pace", 1));
		HuntingAffixService.onKill(inventory, creature(inventory, 0), System.currentTimeMillis());
		assertEquals(1.0, inventory.getMovementSpeed(), 0.00001);
	}

	private Player runner(final String name) {
		final Player player = player(name);
		player.getSlot("feet").add(affix("boots", "legendary_hunters_pace", 1));
		player.setSpeed(player.getBaseSpeed());
		return player;
	}

	private Player player(final String name) {
		final Player player = PlayerTestHelper.createPlayer(name);
		player.initHP(10000);
		final StendhalRPZone zone = new StendhalRPZone("hunting_" + name, 40, 40);
		zone.add(player);
		player.setPosition(10, 10);
		return player;
	}

	private Creature creature(final Player player, final int hp) {
		final Creature creature = new Creature();
		creature.setGender("M");
		creature.put("name", "hunting target");
		creature.initHP(100);
		creature.setHP(hp);
		player.getZone().add(creature);
		creature.setPosition(11, 10);
		player.rememberAttacker(creature);
		return creature;
	}

	private Item affix(final String type, final String name, final double value) {
		final Map<String, String> attributes = new HashMap<>();
		attributes.put("damage_min", "100");
		attributes.put("damage_max", "100");
		final Item item = new Item("hunting affix", type, "test", attributes);
		item.put(ItemAffixState.ATTRIBUTE, name, Double.toString(value));
		return item;
	}
}
