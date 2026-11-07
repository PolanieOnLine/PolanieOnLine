package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Weapon;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class TacticalAffixCombatServiceTest {
	private Player player;
	private Creature target;
	private StendhalRPZone zone;

	@BeforeClass
	public static void setupWorld() {
		MockStendlRPWorld.get();
	}

	@Before
	public void setupFight() {
		zone = new StendhalRPZone("expanded-affixes", 20, 20);
		zone.protectionMap.init(20, 20);
		player = PlayerTestHelper.createPlayer("affix fighter");
		player.setPosition(5, 5);
		zone.add(player);
		target = creature(6, 5);
		player.setTarget(target);
	}

	@Test
	public void crushingOnlySpeedsUpArmorAndCannotBecomeHealthDamageOnOverflow() {
		weapon(ItemTooltip.ARMOR_CRUSHING, 0.15);
		Item gloves = armour("glove", ItemTooltip.ARMOR_CRUSHING, 0.15);
		player.getSlot("glove").add(gloves);
		assertEquals(1.30, TacticalAffixCombatService.getArmorMultiplier(player), 0.00001);
		target.initArmorHP(65);
		ArmorPoolService.DamageResult hit = target.receiveCombatDamage(player, 100,
				TacticalAffixCombatService.getArmorMultiplier(player));
		assertEquals(65, hit.getArmorDamage());
		assertEquals(50, hit.getHealthDamage());
		assertEquals(100, target.receiveCombatDamage(player, 100,
				TacticalAffixCombatService.getArmorMultiplier(player)).getHealthDamage());
	}

	@Test
	public void hardeningCapsOnlyTheExtraCriticalDamage() {
		for (String[] entry : new String[][] {{"armor", "armor"}, {"head", "helmet"},
				{"legs", "legs"}, {"feet", "boots"}}) {
			player.getSlot(entry[0]).add(armour(entry[1], ItemTooltip.CRITICAL_HARDENING, 0.08));
		}
		assertEquals(170, TacticalAffixCombatService.reduceCriticalBonus(player, 100, 200));
		assertEquals(100, TacticalAffixCombatService.reduceCriticalBonus(player, 100, 100));
		assertEquals(200, TacticalAffixCombatService.reduceCriticalBonus(target, 100, 200));
	}

	@Test
	public void tenacityBuildsOnSuccessfulHitsAndResetsOnTargetTimeoutAndWeaponChange() {
		weapon(ItemTooltip.LEGENDARY_TENACITY, 1);
		for (int hit = 0; hit < 8; hit++) {
			assertEquals(100 + 3 * Math.min(5, hit),
					TacticalAffixCombatService.prepareHit(player, target, 100, 1000 + hit));
			hit(target, 10, 1000 + hit, false);
		}
		assertEquals(100, TacticalAffixCombatService.prepareHit(player, target, 100, 7000));
		hit(target, 10, 7000, false);
		Creature other = creature(6, 6);
		assertEquals(100, TacticalAffixCombatService.prepareHit(player, other, 100, 7001));
		assertEquals(100, TacticalAffixCombatService.prepareHit(player, target, 100, 7002));
		hit(target, 10, 7002, false);
		player.getSlot("rhand").clear();
		weapon(ItemTooltip.LEGENDARY_TENACITY, 1);
		assertEquals(100, TacticalAffixCombatService.prepareHit(player, target, 100, 7003));
	}

	@Test
	public void breachImprovesSeveralFollowingHitsOnlyForTheBreakerAndForSixSeconds() {
		weapon(ItemTooltip.LEGENDARY_BREACH, 1);
		target.initArmorHP(50);
		assertEquals(100, TacticalAffixCombatService.prepareHit(player, target, 100, 1000));
		hit(target, 100, 1000, false);
		assertEquals(115, TacticalAffixCombatService.prepareHit(player, target, 100, 1001));
		assertEquals(115, TacticalAffixCombatService.prepareHit(player, target, 100, 2000));
		Creature otherTarget = creature(6, 6);
		player.setTarget(otherTarget);
		assertEquals(100, TacticalAffixCombatService.prepareHit(player, otherTarget, 100, 2001));
		player.setTarget(target);
		assertEquals(115, TacticalAffixCombatService.prepareHit(player, target, 100, 2002));
		assertEquals(100, TacticalAffixCombatService.prepareHit(player, target, 100, 7000));
		target.setArmorHP(50);
		assertEquals(100, TacticalAffixCombatService.prepareHit(player, target, 100, 2001));
		Player other = PlayerTestHelper.createPlayer("other breaker");
		zone.add(other);
		assertEquals(100, TacticalAffixCombatService.prepareHit(other, target, 100, 2001));
	}

	@Test
	public void brieflyRemovingAndReequippingTheSameWeaponCannotPreserveTenacity() {
		weapon(ItemTooltip.LEGENDARY_TENACITY, 1);
		Item original = player.getWeapon();
		hit(target, 10, 1000, false);
		hit(target, 10, 1001, false);
		assertEquals(106, TacticalAffixCombatService.prepareHit(player, target, 100, 1002));
		original.onUnequipped();
		player.getSlot("rhand").clear();
		player.getSlot("rhand").add(original);
		original.onEquipped(player, "rhand");
		assertEquals(100, TacticalAffixCombatService.prepareHit(player, target, 100, 1003));
	}

	@Test
	public void cleaveHitsAtMostTwoNearbyAggressorsAndNeverNeutralCreaturesOrPlayers() {
		weapon(ItemTooltip.LEGENDARY_CLEAVE, 1);
		Creature first = creature(4, 5);
		Creature second = creature(5, 4);
		Creature third = creature(5, 6);
		Creature neutral = creature(4, 4);
		Creature distant = creature(10, 10);
		for (Creature enemy : new Creature[] {first, second, third, distant}) {
			enemy.setTarget(player);
		}
		Player bystander = PlayerTestHelper.createPlayer("bystander");
		bystander.setPosition(4, 6);
		zone.add(bystander);
		int bystanderHP = bystander.getHP();
		for (int hit = 0; hit < 3; hit++) {
			hit(target, 100, 1000 + hit, true);
		}
		assertEquals(1000, first.getHP());
		hit(target, 100, 1003, true);
		assertEquals(2940, first.getHP() + second.getHP() + third.getHP());
		assertEquals(1000, neutral.getHP());
		assertEquals(1000, distant.getHP());
		assertEquals(bystanderHP, bystander.getHP());
		assertTrue(first.getHP() >= 970 && second.getHP() >= 970 && third.getHP() >= 970);
	}

	@Test
	public void recoveryUsesTurnsAndDoesNotStackAcrossArmourPieces() {
		player.getSlot("armor").add(armour("armor", ItemTooltip.LEGENDARY_QUICK_RECOVERY, 1));
		player.getSlot("head").add(armour("helmet", ItemTooltip.LEGENDARY_QUICK_RECOVERY, 1));
		assertEquals(14, TacticalAffixCombatService.statusDurationTurns(player, 6));
		assertEquals(200, TacticalAffixCombatService.statusDurationTurns(target, 60));
		assertEquals(140, TacticalAffixCombatService.statusDurationTurns(player, 60));
	}

	private void hit(final Creature victim, final int damage, final long time, final boolean melee) {
		int armorBefore = victim.getArmorHP();
		ArmorPoolService.DamageResult result = victim.receiveCombatDamage(player, damage);
		TacticalAffixCombatService.onHit(player, victim, armorBefore, result, melee, time);
	}

	private Creature creature(final int x, final int y) {
		Creature creature = new Creature();
		creature.setBaseHP(1000);
		creature.setHP(1000);
		creature.setPosition(x, y);
		zone.add(creature);
		return creature;
	}

	private void weapon(final String attribute, final double value) {
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "30");
		attributes.put("rate", "5");
		Weapon weapon = new Weapon("expanded weapon", "sword", "test", attributes);
		weapon.put(attribute, value);
		player.getSlot("rhand").add(weapon);
	}

	private Item armour(final String itemClass, final String attribute, final double value) {
		Item item = new Item("expanded armour", itemClass, "test", null);
		item.put(attribute, value);
		return item;
	}
}
