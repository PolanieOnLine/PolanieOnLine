/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static utilities.PlayerTestHelper.getPrivateReply;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Rand;
import games.stendhal.common.constants.Nature;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.damage.ParryService;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Projectile;
import games.stendhal.server.entity.item.Weapon;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.AttackEvent;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.server.game.db.DatabaseFactory;
import marauroa.common.game.RPEvent;
import utilities.PlayerTestHelper;

public class StendhalRPActionTest {
	private StendhalRPZone zone;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
		new DatabaseFactory().initializeDatabase();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		PlayerTestHelper.removeAllPlayers();
	}

	@Before
	public void setUp() throws Exception {
		zone = new StendhalRPZone("zone", 20, 20);
		zone.protectionMap.init(20, 20);
		MockStendlRPWorld.get().addRPZone(zone);
	}

	@After
	public void tearDown() throws Exception {
		MockStendlRPWorld.get().removeZone(zone);

	}

	// Sets the square below the victim protected
	private void protectMap() {
		// this seems to be enough for now
		zone.protectionMap.setCollide(0, 0);
	}

	@Test
	public void startAttackingOneself() {
		final Player player = PlayerTestHelper.createPlayer("lunatic");
		zone.add(player);
		// players can't attack themselves. should always fail
		StendhalRPAction.startAttack(player, player);
		assertNull("don't allow targeting oneself", player.getAttackTarget());
	}

	// Trying to attack creatures should always succeed
	@Test
	public void startAttackingCreature() {
		final Player player = PlayerTestHelper.createPlayer("hyde");

		Creature victim = SingletonRepository.getEntityManager().getCreature("mysz domowa");
		zone.add(victim);

		// both at level 0
		StendhalRPAction.startAttack(player, victim);
		assertSame(player.getAttackTarget(), victim);
		player.stopAttack();
		assertNull(player.getAttackTarget());

		// protecting the mouse should not matter
		protectMap();
		StendhalRPAction.startAttack(player, victim);
		assertSame(player.getAttackTarget(), victim);
		player.stopAttack();

		// strong player, weak monster
		player.setLevel(100);
		StendhalRPAction.startAttack(player, victim);
		assertSame(player.getAttackTarget(), victim);

		// weak player, strong monster
		player.setLevel(0);
		victim = SingletonRepository.getEntityManager().getCreature("anioł ciemności");
		zone.add(victim);
		StendhalRPAction.startAttack(player, victim);
		assertSame(player.getAttackTarget(), victim);

		// both are somewhat strong
		player.setLevel(100);
		StendhalRPAction.startAttack(player, victim);
		assertSame(player.getAttackTarget(), victim);
	}

	@Test
	public void startAttackingEqualPlayer() {
		final Player jekyll = PlayerTestHelper.createPlayer("jekyll");
		final Player hyde = PlayerTestHelper.createPlayer("hyde");

		zone.add(jekyll);
		zone.add(hyde);

		for (int level = 0; level < 200; level++) {
			hyde.setLevel(level);
			jekyll.setLevel(level);
			StendhalRPAction.startAttack(hyde, jekyll);
			// equal level. should always succeed
			assertSame("Attacking player at unprotected area", hyde.getAttackTarget(), jekyll);
			hyde.stopAttack();
		}

		// protect jekyll; the attack should fail
		protectMap();
		for (int level = 0; level < 200; level++) {
			hyde.setLevel(level);
			jekyll.setLevel(level);
			StendhalRPAction.startAttack(hyde, jekyll);
			assertNull("Attacking player at protected area", hyde.getAttackTarget());
			assertEquals("message at attacking at protected area",
					"Silna ochronna aura w tym miejscu powstrzymuje Ciebie od atakowania jekyll.", getPrivateReply(hyde));
			hyde.clearEvents();
		}
	}

	@Test
	public void startAttackingStrongerPlayer() {
		final Player jekyll = PlayerTestHelper.createPlayer("jekyll");
		final Player hyde = PlayerTestHelper.createPlayer("hyde");

		zone.add(jekyll);
		zone.add(hyde);

		for (int level = 0; level < 200; level++) {
			jekyll.setLevel(level);
			StendhalRPAction.startAttack(hyde, jekyll);
			// jekyll is stronger than hyde, so attacking should succeed
			assertSame("Attacking player at unprotected area", hyde.getAttackTarget(), jekyll);
			hyde.stopAttack();
		}

		// protect jekyll; the attack should fail
		protectMap();
		for (int level = 0; level < 200; level++) {
			jekyll.setLevel(level);
			StendhalRPAction.startAttack(hyde, jekyll);
			assertNull("Attacking player at protected area", hyde.getAttackTarget());
			assertEquals("message at attacking at protected area",
					"Silna ochronna aura w tym miejscu powstrzymuje Ciebie od atakowania jekyll.", getPrivateReply(hyde));
			hyde.clearEvents();
		}
	}

	@Test
	public void startAttackingWeakPlayer() {
		final Player jekyll = PlayerTestHelper.createPlayer("jekyll");
		final Player hyde = PlayerTestHelper.createPlayer("hyde");

		// First with 0 stats so that they do not mess the level comparisons
		jekyll.setAtk(0);
		jekyll.setDef(0);
		hyde.setAtk(0);
		hyde.setDef(0);

		zone.add(jekyll);
		zone.add(hyde);

		for (int defenderLevel = 0; defenderLevel < 200; defenderLevel += 10) {
			jekyll.setLevel(defenderLevel);
			for (int attackerLevel = (int) (1.3 * defenderLevel); 0.74 * attackerLevel  <= defenderLevel + 2; attackerLevel++) {
				hyde.setLevel(attackerLevel);
				StendhalRPAction.startAttack(hyde, jekyll);
				if ((jekyll.getLevel()) < hyde.getLevel() * 0.75) {
					assertNull("Attacking a too weak player. Level " + hyde.getLevel() + " vs Level " + jekyll.getLevel(),
							hyde.getAttackTarget());

					assertEquals("message at attacking at protected area",
							"Twoje sumienie może mieć problem jeżeli stoisz za tym atakiem.", getPrivateReply(hyde));
					hyde.clearEvents();

					// check that self defense works
					StendhalRPAction.startAttack(jekyll, hyde);
					StendhalRPAction.startAttack(hyde, jekyll);
					assertSame("Self defence against a weak enemy", hyde.getAttackTarget(), jekyll);
					jekyll.stopAttack();
				} else {
					// the victim is not too weak
					assertSame("Attacking only a bit weaker victim", hyde.getAttackTarget(), jekyll);
				}
				hyde.stopAttack();
			}
		}

		// check the skill effect separately
		jekyll.setLevel(0);
		hyde.setLevel(0);
		// 0 skills have already been checked
		hyde.setAtk(10);
		jekyll.setAtk(10);
		StendhalRPAction.startAttack(hyde, jekyll);
		assertSame(hyde.getAttackTarget(), jekyll);
		hyde.stopAttack();
		hyde.setDef(5);
		StendhalRPAction.startAttack(hyde, jekyll);
		assertNull(hyde.getAttackTarget());
		// Should be make jekyll strong enough
		jekyll.setDef(2);
		StendhalRPAction.startAttack(hyde, jekyll);
		assertSame(hyde.getAttackTarget(), jekyll);
	}

	@Test
	public void startAttackingPet() {
		final Player jekyll = PlayerTestHelper.createPlayer("jekyll");
		final Player hyde = PlayerTestHelper.createPlayer("hyde");
		final Sheep sheep = new Sheep();

		zone.add(hyde);
		zone.add(sheep);
		// attacking wild sheep should be ok
		StendhalRPAction.startAttack(hyde, sheep);
		assertSame("Attacking a sheep in unprotected area", hyde.getAttackTarget(), sheep);
		hyde.stopAttack();
		// also if you are the owner
		sheep.setOwner(hyde);
		StendhalRPAction.startAttack(hyde, sheep);
		assertSame("Attacking a sheep in unprotected area", hyde.getAttackTarget(), sheep);
		hyde.stopAttack();
		// but attacking someone elses pet is a no-no
		sheep.setOwner(jekyll);
		StendhalRPAction.startAttack(hyde, sheep);
		assertNull("Attacking someone else's sheep", hyde.getAttackTarget());
		assertEquals("message at attacking someone else's sheep",
				"Za bardzo żal ci jekyll sheep, by go zabić.",
				hyde.events().get(0).get("text"));
		hyde.stopAttack();
		hyde.clearEvents();
		sheep.setOwner(null);

		// Protected. should fail
		protectMap();
		StendhalRPAction.startAttack(hyde, sheep);
		assertNull("Attacking a sheep in protected area ", hyde.getAttackTarget());
		assertEquals("message at attacking a sheep in protected area",
				"Silna ochronna aura w tym miejscu powstrzymuje Ciebie od atakowania sheep.",
				getPrivateReply(hyde));
		hyde.stopAttack();
		hyde.clearEvents();

		// the same with an owned sheep
		sheep.setOwner(hyde);
		StendhalRPAction.startAttack(hyde, sheep);
		assertNull("Attacking a sheep in protected area ", hyde.getAttackTarget());
		assertEquals("message at attacking a sheep in protected area",
				"Silna ochronna aura w tym miejscu powstrzymuje Ciebie od atakowania hyde sheep.",
				getPrivateReply(hyde));
		hyde.clearEvents();

		// ...regarless of the owner
		sheep.setOwner(jekyll);
		StendhalRPAction.startAttack(hyde, sheep);
		assertNull("Attacking a sheep in protected area ", hyde.getAttackTarget());
		assertEquals("message at attacking a sheep in protected area",
				"Silna ochronna aura w tym miejscu powstrzymuje Ciebie od atakowania jekyll sheep.",
				getPrivateReply(hyde));
	}

	@Test
	public void playerAttackRespectsHitRollsImmortalityAndAmmunition() {
		for (int scenario = 0; scenario < 3; scenario++) {
			final boolean hits = scenario > 0;
			final boolean immortal = scenario == 2;
			final Player player = new Player(PlayerTestHelper.createPlayer("unarmed_" + scenario)) {
				@Override
				public boolean canHit(final RPEntity defender) {
					return hits;
				}

				@Override
				public int damageDone(final RPEntity defender, final double attack, final Nature nature) {
					return 1;
				}
			};
			final Creature victim = SingletonRepository.getEntityManager()
					.getCreature("mysz domowa");
			if (immortal) {
				victim.setAIProfiles(new HashMap<String, String>(victim.getAIProfiles()));
				victim.getAIProfiles().put("immortal", "");
			}
			victim.initHP(100);
			player.setPosition(5, 5);
			victim.setPosition(6, 5);
			zone.add(player);
			zone.add(victim);

			assertNull(player.getWeapon());
			assertEquals(hits && !immortal, StendhalRPAction.playerAttack(player, victim));
			if (!hits || immortal) {
				assertEquals(100, victim.getHP());
			}
			zone.remove(player);
			zone.remove(victim);
		}
		for (boolean hits : new boolean[] {false, true}) {
			for (boolean adjacent : new boolean[] {false, true}) {
				verifyAmmunitionUse("ranged", "strzała", hits, adjacent);
				verifyAmmunitionUse("wand", "magia ziemi", hits, adjacent);
			}
		}
	}

	@Test
	public void pvpParryBlocksDamageAndArmsOneDuelMasterRiposte() throws Exception {
		final Player attacker = combatPlayer("parry_attacker", true, "sword");
		final Player defender = combatPlayer("parry_defender", true, "sword");
		defender.getWeapon().put(ParryService.LEGENDARY_DUEL_MASTER_ATTRIBUTE, 1.0);
		defender.initArmorHP(50);
		attacker.setHP(500);
		attacker.getWeapon().put("lifesteal", 1.0);

		assertFalse(attackWithParryRoll(attacker, defender, 0.0));
		assertEquals(1000, defender.getHP());
		assertEquals(50, defender.getArmorHP());
		assertEquals("a parried hit cannot steal life", 500, attacker.getHP());
		final AttackEvent event = lastAttackEvent(attacker);
		assertTrue(event.has("parried"));
		assertEquals(0, event.getInt("damage"));

		assertTrue(attackWithParryRoll(defender, attacker, 0.99));
		assertEquals("the riposte deals 30% more damage", 370, attacker.getHP());
		assertTrue(attackWithParryRoll(defender, attacker, 0.99));
		assertEquals("the riposte is consumed exactly once", 270, attacker.getHP());
	}

	@Test
	public void failedPvpParryAllowsNormalDamageWithoutArmingRiposte() throws Exception {
		final Player attacker = combatPlayer("failed_parry_attacker", true, "sword");
		final Player defender = combatPlayer("failed_parry_defender", true, "sword");
		defender.getWeapon().put(ParryService.LEGENDARY_DUEL_MASTER_ATTRIBUTE, 1.0);

		assertTrue(attackWithParryRoll(attacker, defender, 0.99));
		assertEquals(900, defender.getHP());
		assertFalse(lastAttackEvent(attacker).has("parried"));
		assertTrue(attackWithParryRoll(defender, attacker, 0.99));
		assertEquals(900, attacker.getHP());
	}

	@Test
	public void pvpProjectilesCannotBeParriedEvenAtPointBlankRange() throws Exception {
		for (final String weaponClass : new String[] {"ranged", "wand"}) {
			for (final boolean adjacent : new boolean[] {false, true}) {
				final String suffix = weaponClass + adjacent;
				final Player attacker = combatPlayer("projectile_attacker_" + suffix, true, weaponClass);
				final Player defender = combatPlayer("projectile_defender_" + suffix, true, "sword");
				defender.getWeapon().put(ParryService.PARRY_CHANCE_ATTRIBUTE, 0.15);
				defender.setPosition(adjacent ? 6 : 9, 5);
				final String ammunitionName = "wand".equals(weaponClass) ? "magia ziemi" : "strzała";
				final Projectile ammunition = (Projectile) SingletonRepository.getEntityManager()
						.getItem(ammunitionName);
				ammunition.setQuantity(2);
				attacker.getSlot("rhand").add(ammunition);

				assertTrue(attackWithParryRoll(attacker, defender, 0.0));
				assertEquals(900, defender.getHP());
				assertEquals(1, ammunition.getQuantity());
				assertFalse(lastAttackEvent(attacker).has("parried"));
				zone.remove(attacker);
				zone.remove(defender);
			}
		}
	}

	@Test
	public void pvpParryCanBlockInevitabilityWithoutCountingAsAnAccuracyMiss() throws Exception {
		final Player attacker = combatPlayer("inevitable_parry_attacker", false, "sword");
		final Player defender = combatPlayer("inevitable_parry_defender", true, "sword");
		attacker.getWeapon().put(ItemAffixState.ATTRIBUTE, "legendary_inevitability", "1.0");
		defender.getWeapon().put(ParryService.PARRY_CHANCE_ATTRIBUTE, 0.15);

		assertFalse(attackWithParryRoll(attacker, defender, 0.0));
		assertFalse(lastAttackEvent(attacker).has("parried"));
		assertFalse(attackWithParryRoll(attacker, defender, 0.0));
		assertFalse(lastAttackEvent(attacker).has("parried"));
		assertFalse(attackWithParryRoll(attacker, defender, 0.0));
		assertTrue(lastAttackEvent(attacker).has("parried"));
		assertFalse(attackWithParryRoll(attacker, defender, 0.0));
		assertFalse(lastAttackEvent(attacker).has("parried"));
		assertEquals(1000, defender.getHP());
	}

	private Player combatPlayer(final String name, final boolean hits, final String weaponClass) {
		final Player player = new Player(PlayerTestHelper.createPlayer(name)) {
			@Override
			public boolean canHit(final RPEntity defender) {
				return hits;
			}

			@Override
			public int damageDone(final RPEntity defender, final double attack, final Nature nature) {
				return 100;
			}
		};
		final Weapon weapon = new Weapon("test pvp weapon", weaponClass, "test", null);
		weapon.put("atk", 10);
		weapon.put("damage_min", 100);
		weapon.put("damage_max", 100);
		weapon.put("rate", 5);
		weapon.put("range", 7);
		player.getSlot("lhand").add(weapon);
		player.initHP(1000);
		player.setPosition(5, 5);
		zone.add(player);
		return player;
	}

	private boolean attackWithParryRoll(final Player attacker, final Player defender,
			final double parryRoll) throws Exception {
		final Field randomField = Rand.class.getDeclaredField("rand");
		randomField.setAccessible(true);
		final Random previous = (Random) randomField.get(null);
		randomField.set(null, new Random() {
			@Override
			public double nextDouble() {
				return parryRoll;
			}

			@Override
			public int nextInt(final int bound) {
				return bound - 1;
			}
		});
		attacker.clearEvents();
		try {
			return StendhalRPAction.playerAttack(attacker, defender);
		} finally {
			randomField.set(null, previous);
		}
	}

	private AttackEvent lastAttackEvent(final Player player) {
		AttackEvent last = null;
		for (final RPEvent event : player.events()) {
			if (event instanceof AttackEvent) {
				last = (AttackEvent) event;
			}
		}
		assertNotNull("an attack event must reach the client", last);
		return last;
	}

	@Test
	public void lifestealWaitsUntilTheHitReachesHealth() {
		final Player player = new Player(PlayerTestHelper.createPlayer("armor_lifesteal")) {
			@Override public boolean canHit(final RPEntity defender) { return true; }
			@Override public int damageDone(final RPEntity defender, final double attack, final Nature nature) { return 100; }
		};
		final Weapon weapon = new Weapon("test lifesteal sword", "sword", "test", null);
		weapon.put("atk", 30);
		weapon.put("damage_min", 100);
		weapon.put("damage_max", 100);
		weapon.put("rate", 5);
		weapon.put("lifesteal", 0.5);
		player.getSlot("rhand").add(weapon);
		player.initHP(100);
		player.setHP(50);
		final Creature target = SingletonRepository.getEntityManager().getCreature("mysz domowa");
		target.initHP(1000);
		target.initArmorHP(1000);
		player.setPosition(5, 5);
		target.setPosition(6, 5);
		zone.add(player);
		zone.add(target);
		assertTrue(StendhalRPAction.playerAttack(player, target));
		assertTrue(target.getArmorHP() < 1000);
		assertEquals(1000, target.getHP());
		assertEquals(50, player.getHP());
		target.setArmorHP(0);
		assertTrue(StendhalRPAction.playerAttack(player, target));
		assertTrue(target.getHP() < 1000);
		assertTrue(player.getHP() > 50);
	}

	@Test
	public void weaponMatchupsApplyOnceToArmorAndHealthIncludingAfterArmorBreaks() {
		verifyPersistentMatchup("dagger", 40);
		verifyPersistentMatchup("sword", 80);
		verifyPersistentMatchup("axe", 130);
	}

	private void verifyPersistentMatchup(final String weaponClass, final int expectedDamage) {
		final Player player = new Player(PlayerTestHelper.createPlayer("armor_" + weaponClass)) {
			@Override public boolean canHit(final RPEntity defender) { return true; }
			@Override public int damageDone(final RPEntity defender, final double attack, final Nature nature) { return 100; }
		};
		final Weapon weapon = new Weapon("test matchup weapon", weaponClass, "test", null);
		weapon.put("atk", 30);
		weapon.put("damage_min", 100);
		weapon.put("damage_max", 100);
		weapon.put("rate", 5);
		player.getSlot("rhand").add(weapon);
		player.initHP(100);
		final Creature target = SingletonRepository.getEntityManager().getCreature("mysz domowa");
		target.initHP(1000);
		target.setArmorType("heavy");
		target.initArmorHP(50);
		player.setPosition(5, 5);
		target.setPosition(6, 5);
		zone.add(player);
		zone.add(target);
		assertTrue(StendhalRPAction.playerAttack(player, target));
		final int firstDamage = expectedDamage * (target.has("crit") ? 2 : 1);
		assertEquals(weaponClass, Math.max(0, 50 - firstDamage), target.getArmorHP());
		assertEquals(weaponClass, 1000 - Math.max(0, firstDamage - 50), target.getHP());
		target.setArmorHP(0);
		final int healthBefore = target.getHP();
		assertTrue(StendhalRPAction.playerAttack(player, target));
		assertEquals(weaponClass, healthBefore - expectedDamage * (target.has("crit") ? 2 : 1), target.getHP());
		zone.remove(player);
		zone.remove(target);
	}

	private void verifyAmmunitionUse(final String weaponClass, final String ammunitionName,
			final boolean hits, final boolean adjacent) {
		final Player player = new Player(PlayerTestHelper.createPlayer("ammunition_user")) {
			@Override public boolean canHit(final RPEntity defender) { return hits; }
			@Override public int damageDone(final RPEntity defender, final double attack, final Nature nature) { return 1; }
		};
		final Weapon weapon = new Weapon("test launcher", weaponClass, "test", null);
		weapon.put("atk", 10);
		weapon.put("damage_min", 10);
		weapon.put("damage_max", 10);
		weapon.put("range", 7);
		weapon.put("rate", 5);
		player.getSlot("lhand").add(weapon);
		final float attackWithoutAmmunition = player.getItemAtkForAttack();
		final Projectile ammunition = (Projectile) SingletonRepository.getEntityManager().getItem(ammunitionName);
		ammunition.setQuantity(2);
		player.getSlot("rhand").add(ammunition);
		assertTrue(player.getItemAtkForAttack() > attackWithoutAmmunition);
		final Creature victim = SingletonRepository.getEntityManager().getCreature("mysz domowa");
		victim.initHP(100);
		player.setPosition(5, 5);
		victim.setPosition(adjacent ? 6 : 9, 5);
		zone.add(player);
		zone.add(victim);
		assertEquals(hits, StendhalRPAction.playerAttack(player, victim));
		assertEquals(weaponClass + ", adjacent=" + adjacent + ", hits=" + hits, 1, ammunition.getQuantity());
		zone.remove(player);
		zone.remove(victim);
	}
}
