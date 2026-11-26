package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.item.BreakableWeapon;
import games.stendhal.server.entity.item.ConsumableItem;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Potion;
import games.stendhal.server.entity.item.consumption.FeederFactory;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.BleedingStatus;
import games.stendhal.server.entity.status.PoisonStatus;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import utilities.PlayerTestHelper;

public class EquipmentUtilityAffixServiceTest {
	@BeforeClass
	public static void initialize() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
	}

	@Test
	public void creatureKillHealsFromGlovesAndBeltWithoutRequiringLegendaryBoots() {
		final Player player = player("utility-recovery");
		player.getSlot("glove").add(affix("glove", "kill_recovery", 100));
		player.getSlot("pas").add(affix("belts", "kill_recovery", 100));
		player.setHP(100);
		HuntingAffixService.onKilled(player, creature(player, 0));
		assertEquals(300, player.getHP());
		player.setHP(9950);
		HuntingAffixService.onKilled(player, creature(player, 0));
		assertEquals(10000, player.getHP());
		player.setHP(10500);
		HuntingAffixService.onKilled(player, creature(player, 0));
		assertEquals(10500, player.getHP());
	}

	@Test
	public void poisonAndBleedKillsCreditTheirPlayerButPetsPvpAndLivingTargetsDoNotHeal() {
		final Player player = player("utility-status-credit");
		player.getSlot("glove").add(affix("glove", "kill_recovery", 75));
		player.setHP(100);
		HuntingAffixService.onKilled(new PoisonStatus(-10, 1, -10, player), creature(player, 0));
		assertEquals(175, player.getHP());
		HuntingAffixService.onKilled(new BleedingStatus(10, 1, 10, player), creature(player, 0));
		assertEquals(250, player.getHP());
		final Sheep pet = new Sheep();
		pet.initHP(100);
		pet.setHP(0);
		player.getZone().add(pet);
		HuntingAffixService.onKilled(player, pet);
		HuntingAffixService.onKilled(player, player);
		HuntingAffixService.onKilled(player, creature(player, 1));
		assertEquals(250, player.getHP());
	}

	@Test
	public void carriedAndWrongSlotRecoveryDoesNotHealAndDeadPlayerCannotBeRevived() {
		final Player player = player("utility-recovery-slots");
		player.getSlot("bag").add(affix("glove", "kill_recovery", 100));
		player.getSlot("armor").add(affix("armor", "kill_recovery", 100));
		player.setHP(100);
		HuntingAffixService.onKilled(player, creature(player, 0));
		assertEquals(100, player.getHP());
		player.getSlot("glove").add(affix("glove", "kill_recovery", 100));
		player.setHP(0);
		HuntingAffixService.onKilled(player, creature(player, 0));
		assertEquals(0, player.getHP());
	}

	@Test
	public void singleCombatCountsOneActualCreatureAndCapsEquippedDamageAtTenPercent() {
		final Player player = player("utility-single-combat");
		player.getSlot("armor").add(affix("armor", "single_combat_bonus", 0.05));
		player.getSlot("legs").add(affix("legs", "single_combat_bonus", 0.05));
		player.getSlot("feet").add(affix("boots", "single_combat_bonus", 0.05));
		final Creature target = creature(player, 100);
		player.setTarget(target);
		target.setTarget(player);
		assertEquals(110, HuntingAffixService.outgoingDamage(player, 100));
		creature(player, 100).setTarget(player);
		assertEquals(100, HuntingAffixService.outgoingDamage(player, 100));
	}

	@Test
	public void staleDeadAndDistantCreaturesDoNotDisableSingleCombatButAnotherActiveOneDoes() {
		final Player player = player("utility-single-active");
		player.getSlot("head").add(affix("helmet", "single_combat_bonus", 0.03));
		final Creature target = creature(player, 100);
		player.setTarget(target);
		player.rememberAttacker(creature(player, 100));
		creature(player, 0).setTarget(player);
		final Creature distant = creature(player, 100);
		distant.setPosition(35, 35);
		distant.setTarget(player);
		assertEquals(103, HuntingAffixService.outgoingDamage(player, 100));
		distant.setPosition(10, 11);
		assertEquals(100, HuntingAffixService.outgoingDamage(player, 100));
		distant.stopAttack();
		assertEquals(103, HuntingAffixService.outgoingDamage(player, 100));
	}

	@Test
	public void singleCombatDoesNotApplyToPvpPetsWrongEquipmentOrInactiveTargets() {
		final Player player = player("utility-single-exclusions");
		player.getSlot("cloak").add(affix("cloak", "single_combat_bonus", 0.05));
		player.getSlot("bag").add(affix("armor", "single_combat_bonus", 0.05));
		final Creature target = creature(player, 100);
		player.setTarget(target);
		assertEquals(100, HuntingAffixService.outgoingDamage(player, 100));
		player.getSlot("armor").add(affix("armor", "single_combat_bonus", 0.05));
		target.setHP(0);
		assertEquals(100, HuntingAffixService.outgoingDamage(player, 100));
		target.setHP(100);
		target.setPosition(35, 35);
		assertEquals(100, HuntingAffixService.outgoingDamage(player, 100));
		final Player opponent = PlayerTestHelper.createPlayer("utility-pvp-opponent");
		opponent.initHP(1000);
		player.getZone().add(opponent);
		opponent.setPosition(11, 10);
		player.setTarget(opponent);
		assertEquals(100, HuntingAffixService.outgoingDamage(player, 100));
		player.setTarget(target);
		target.setPosition(11, 10);
		opponent.setTarget(player);
		player.rememberAttacker(opponent);
		assertEquals(100, HuntingAffixService.outgoingDamage(player, 100));
		opponent.stopAttack();
		final Sheep pet = new Sheep();
		pet.initHP(100);
		player.getZone().add(pet);
		pet.setPosition(11, 10);
		player.setTarget(pet);
		assertEquals(100, HuntingAffixService.outgoingDamage(player, 100));
	}

	@Test
	public void successfulHealingPotionsKeepTheirFullEffectAndConserveAtMostTwentyPercent() {
		final Player player = player("utility-potions");
		for (final String slot : new String[] {"armor", "head", "legs", "feet"}) {
			final String type = "head".equals(slot) ? "helmet" : "feet".equals(slot) ? "boots" : slot;
			player.getSlot(slot).add(affix(type, "potion_conservation", 0.08));
		}
		final Potion potion = (Potion) SingletonRepository.getEntityManager().getItem("duży eliksir");
		potion.setQuantity(1001);
		player.getSlot("bag").add(potion);
		for (int index = 0; index < 1000; index++) {
			player.setHP(1);
			assertTrue(potion.onUsed(player));
			assertEquals(1 + potion.getAmount(), player.getHP());
		}
		assertTrue("Saved potions: " + (potion.getQuantity() - 1),
				potion.getQuantity() >= 101 && potion.getQuantity() <= 301);
	}

	@Test
	public void fullHealthPotionsKeepBaselineConsumptionAndRejectedUsesKeepTheBottleAndBleed() {
		final Player player = player("utility-potion-rejection");
		player.getSlot("armor").add(affix("armor", "potion_conservation", 0.08));
		player.getSlot("head").add(affix("helmet", "potion_conservation", 0.08));
		player.getSlot("legs").add(affix("legs", "potion_conservation", 0.08));
		final Potion potion = (Potion) SingletonRepository.getEntityManager().getItem("duży eliksir");
		potion.setQuantity(102);
		player.getSlot("bag").add(potion);
		for (int index = 0; index < 100; index++) {
			assertTrue(potion.onUsed(player));
		}
		assertEquals(2, potion.getQuantity());
		player.setHP(0);
		final BleedingStatus bleeding = new BleedingStatus(10, 1, 1, player);
		player.getStatusList().inflictStatus(bleeding, player);
		assertFalse(potion.onUsed(player));
		assertEquals(2, potion.getQuantity());
		assertTrue(player.getStatusList().getStatuses().contains(bleeding));
	}

	@Test
	public void nonPotionsAndRejectedDistantUseDoNotReceiveConservation() {
		final Player player = player("utility-non-potions");
		player.getSlot("armor").add(affix("armor", "potion_conservation", 0.08));
		final ConsumableItem food = (ConsumableItem) SingletonRepository.getEntityManager().getItem("ser");
		food.setQuantity(2);
		player.getSlot("bag").add(food);
		assertTrue(FeederFactory.get(food).feed(food, player));
		assertEquals(1, food.getQuantity());
		final ConsumableItem poison = (ConsumableItem) SingletonRepository.getEntityManager().getItem("mocna trucizna");
		poison.setQuantity(101);
		player.getSlot("bag").add(poison);
		for (int index = 0; index < 100; index++) {
			assertTrue(FeederFactory.get(poison).feed(poison, player));
		}
		assertEquals(1, poison.getQuantity());
		final ConsumableItem antidote = (ConsumableItem) SingletonRepository.getEntityManager().getItem("antidotum");
		antidote.setQuantity(101);
		player.getSlot("bag").add(antidote);
		for (int index = 0; index < 100; index++) {
			player.getStatusList().inflictStatus(new PoisonStatus(-10000, 1, -10), poison);
			assertTrue(FeederFactory.get(antidote).feed(antidote, player));
		}
		assertEquals(1, antidote.getQuantity());
		final Potion potion = (Potion) SingletonRepository.getEntityManager().getItem("duży eliksir");
		potion.setQuantity(2);
		player.getZone().add(potion);
		potion.setPosition(35, 35);
		assertFalse(potion.onUsed(player));
		assertEquals(2, potion.getQuantity());
	}

	@Test
	public void onlyTheUsedBreakableWeaponConservesItsOwnDurability() {
		final Player player = player("utility-durability");
		final Map<String, String> attributes = new HashMap<>();
		attributes.put("durability", "100000");
		attributes.put("uses", "0");
		final BreakableWeapon weapon = new BreakableWeapon("utility blade", "sword", "test", attributes);
		weapon.put(ItemAffixState.ATTRIBUTE, "durability_conservation", "0.20");
		for (int index = 0; index < 1000; index++) {
			weapon.deteriorate(player);
		}
		assertTrue("Durability consumed: " + weapon.getUses(), weapon.getUses() >= 700 && weapon.getUses() <= 900);
		final BreakableWeapon plain = new BreakableWeapon("plain blade", "sword", "test", attributes);
		player.getSlot("rhand").add(weapon);
		plain.deteriorate(player);
		assertEquals(1, plain.getUses());
		weapon.put("durability", 0);
		final int before = weapon.getUses();
		for (int index = 0; index < 100; index++) {
			weapon.deteriorate();
		}
		assertEquals(before + 100, weapon.getUses());
	}

	private Player player(final String name) {
		final Player player = PlayerTestHelper.createPlayer(name);
		player.initHP(10000);
		final StendhalRPZone zone = new StendhalRPZone(name, 40, 40);
		zone.add(player);
		player.setPosition(10, 10);
		return player;
	}

	private Creature creature(final Player player, final int hp) {
		final Creature creature = new Creature();
		creature.setGender("M");
		creature.put("name", "utility target");
		creature.initHP(100);
		creature.setHP(hp);
		player.getZone().add(creature);
		creature.setPosition(11, 10);
		player.rememberAttacker(creature);
		return creature;
	}

	private Item affix(final String type, final String name, final double value) {
		final Item item = new Item("utility affix", type, "test", new HashMap<String, String>());
		item.put(ItemAffixState.ATTRIBUTE, name, Double.toString(value));
		return item;
	}
}
