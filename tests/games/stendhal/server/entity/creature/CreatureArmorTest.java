/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.creature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.status.PoisonStatus;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.core.rule.damage.ArmorPoolService.DamageResult;
import games.stendhal.server.core.config.CreatureGroupsXMLLoader;
import games.stendhal.server.core.rule.defaultruleset.DefaultCreature;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import utilities.RPClass.CreatureTestHelper;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.RPClass;

public class CreatureArmorTest {
	@BeforeClass
	public static void generateRPClasses() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
		CreatureTestHelper.generateRPClasses();
	}

	@Test
	public void armorAbsorbsHitsAndOnlyTheRemainderReachesHealth() {
		final Creature target = armoredTarget();
		final Creature attacker = new Creature();
		target.getZone().add(attacker);
		target.onDamaged(attacker, 60);
		assertEquals(100, target.getHP());
		assertEquals(20, target.getInt("armor_hp"));
		target.onDamaged(attacker, 30);
		assertEquals(90, target.getHP());
		assertEquals(0, target.getInt("armor_hp"));
	}

	@Test
	public void ordinaryHealingAndExistingPoisonDamageDoNotRestoreOrConsumeArmor() {
		final Creature target = armoredTarget();
		target.setHP(50);
		target.heal(20);
		assertEquals(70, target.getHP());
		assertEquals(80, target.getInt("armor_hp"));
		target.damage(15, new PoisonStatus(-15, 1, -15));
		assertEquals(55, target.getHP());
		assertEquals(80, target.getInt("armor_hp"));
	}

	@Test
	public void newlySpawnedCopyStartsWithFullIndependentArmor() {
		final Creature source = armoredTarget();
		source.put("armor_hp", 12);
		final Creature spawned = source.getNewInstance();
		assertEquals(80, spawned.getInt("base_armor_hp"));
		assertEquals(80, spawned.getInt("armor_hp"));
		assertEquals(12, source.getInt("armor_hp"));
	}

	@Test
	public void armorBreakingAdvantageDoesNotMultiplyTheHealthRemainder() {
		final Creature target = armoredTarget();
		target.setArmorType("heavy");
		target.initArmorHP(65);
		final Creature attacker = new Creature();
		target.getZone().add(attacker);
		final DamageResult dealt = target.receiveCombatDamage(attacker, 100, 1.30);
		assertEquals(65, dealt.getArmorDamage());
		assertEquals(50, dealt.getHealthDamage());
		assertEquals(50, target.getHP());
		assertEquals("heavy", target.getArmorType());
		assertEquals("none", target.getEffectiveArmorType());
	}

	@Test
	public void armorDamageCountsTowardsKillContribution() {
		final Creature source = armoredTarget();
		final TrackedCreature target = new TrackedCreature(source);
		final Creature attacker = new Creature();
		source.getZone().add(target);
		source.getZone().add(attacker);
		target.onDamaged(attacker, 60);
		assertEquals(60, target.contribution(attacker));
		assertEquals(100, target.getHP());
		target.onDamaged(attacker, 30);
		assertEquals(90, target.contribution(attacker));
	}

	@Test
	public void xmlValuesReachSpawnedCreaturesWithoutLeakingToTheNextDefinition() throws Exception {
		boolean foundArmored = false;
		boolean foundUnarmored = false;
		for (final DefaultCreature definition : new CreatureGroupsXMLLoader("/data/conf/creatures.xml").load()) {
			if ("dwugłowy czarny smok".equals(definition.getCreatureName())) {
				final Creature creature = definition.getCreature();
				assertEquals("heavy", creature.getArmorType());
				assertEquals(6600, creature.getBaseHP());
				assertEquals(6600, creature.getHP());
				assertEquals(1650, creature.getBaseArmorHP());
				assertEquals(1650, creature.getArmorHP());
				foundArmored = true;
			} else if ("szczur".equals(definition.getCreatureName())) {
				assertEquals(0, definition.getCreature().getBaseArmorHP());
				foundUnarmored = true;
			}
		}
		assertTrue(foundArmored && foundUnarmored);
	}

	private static final class TrackedCreature extends Creature {
		TrackedCreature(final Creature source) { super(source); }
		int contribution(final Entity attacker) { return damageReceived.getCount(attacker); }
	}

	private Creature armoredTarget() {
		final Creature target = SingletonRepository.getEntityManager().getCreature("szarak");
		target.setName("opancerzony przeciwnik");
		target.initHP(100);
		target.put("base_armor_hp", 80);
		target.put("armor_hp", 80);
		final StendhalRPZone zone = new StendhalRPZone("armor-pool-test", 20, 20);
		zone.add(target);
		return target;
	}

	@Test
	public void missingArmorIsUnarmoredRegardlessOfDefense() {
		final Creature creature = new Creature();
		creature.setDef(150);

		assertEquals("none", creature.getArmorType());
	}

	@Test
	public void explicitArmorTypeIsStored() {
		final Creature creature = new Creature();
		creature.setDef(1);
		creature.setArmorType("heavy");

		assertEquals("heavy", creature.getArmorType());
	}

	@Test
	public void settingNoneClearsExplicitArmorType() {
		final Creature creature = new Creature();
		creature.setArmorType("medium");
		creature.setArmorType("none");

		assertEquals("none", creature.getArmorType());
		assertEquals("none", creature.get("armor_type"));
	}

	@Test
	public void armorIsPublishedWithoutExposingCombatMultipliers() {
		final Definition definition = RPClass.getRPClass("creature")
				.getDefinition(DefinitionClass.ATTRIBUTE, "armor_type");
		assertTrue(definition.isVisible());
		assertFalse(definition.isPrivate());
		assertFalse(definition.isHidden());
		assertEquals("none", new Creature().get("armor_type"));
	}

	@Test
	public void restoredCreatureRetainsItsConfiguredArmor() {
		final Creature creature = new Creature();
		creature.setArmorType("heavy");
		assertEquals("heavy", new Creature((marauroa.common.game.RPObject) creature).getArmorType());
	}

	@Test
	public void lookDescriptionUsesPolishArmorNamesAndKeepsOriginalDescription() {
		final Creature creature = new Creature();
		creature.setDescription("Oto przeciwnik testowy.");
		creature.setLevel(20);
		for (final String[] entry : new String[][] {
				{"light", "Pancerz lekki"}, {"medium", "Pancerz średni"},
				{"heavy", "Pancerz ciężki"}}) {
			creature.setArmorType(entry[0]);
			assertEquals("Oto przeciwnik testowy. Poziom 20. " + entry[1] + ".", creature.describe());
		}
		creature.setArmorType(null);
		assertEquals("none", creature.get("armor_type"));
	}

	@Test
	public void unarmoredLookKeepsDescriptionAndLevelWithoutAnArmorNotice() {
		final Creature creature = new Creature();
		creature.setDescription("Oto przeciwnik testowy.");
		creature.setLevel(20);
		for (final String armor : new String[] {"none", null, "unknown"}) {
			creature.setArmorType(armor);
			assertEquals("Oto przeciwnik testowy. Poziom 20.", creature.describe());
		}
		creature.remove("armor_type");
		assertEquals("Oto przeciwnik testowy. Poziom 20.", creature.describe());
	}
}
