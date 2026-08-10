/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.creature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.RPClass.CreatureTestHelper;

public class EliteCreatureServiceTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
		CreatureTestHelper.generateRPClasses();
	}

	@Test
	public void tutorialLevelCreatureIsNotEligible() {
		final Creature creature = createCreature();
		creature.setLevel(EliteCreatureService.MINIMUM_ELITE_LEVEL - 1);

		assertFalse(EliteCreatureService.isEligible(creature));
	}

	@Test
	public void normalEnemyAboveMinimumLevelIsEligible() {
		final Creature creature = createCreature();

		assertTrue(EliteCreatureService.isEligible(creature));
	}

	@Test
	public void authoredBossIsNotEligible() {
		final Creature creature = createCreature();
		final Map<String, String> profiles = new HashMap<String, String>();
		profiles.put("boss", "");
		creature.setAIProfiles(profiles);

		assertFalse(EliteCreatureService.isEligible(creature));
	}

	@Test
	public void normalCreatureKeepsSingleRarityRoll() {
		final ItemCreationContext context =
				EliteCreatureService.getDropCreationContext(createCreature());

		assertEquals(1, context.getRarityRolls());
		assertTrue(context.isGenerateAffixes());
	}

	@Test
	public void eliteCreatureGetsTwoRarityRolls() {
		final Creature creature = createCreature();
		EliteCreatureService.promote(creature);

		final ItemCreationContext context =
				EliteCreatureService.getDropCreationContext(creature);

		assertEquals(EliteCreatureService.ELITE_RARITY_ROLLS,
				context.getRarityRolls());
		assertTrue(context.isGenerateAffixes());
	}

	@Test
	public void promotionMarksAndScalesOnlyTheInstance() {
		final Creature creature = createCreature();

		EliteCreatureService.promote(creature);

		assertTrue(EliteCreatureService.isElite(creature));
		assertEquals("elite", creature.get("title_type"));
		assertEquals("Elitarny testowy potwór", creature.get("title"));
		assertEquals("testowy potwór", creature.getName());
		assertEquals(140, creature.getBaseHP());
		assertEquals(140, creature.getHP());
		assertEquals(115, creature.getAtk());
		assertEquals(58, creature.getRatk());
		assertEquals(88, creature.getDef());
		assertEquals(1500, creature.getXP());
	}

	@Test
	public void promotionCannotBeAppliedTwice() {
		final Creature creature = createCreature();
		EliteCreatureService.promote(creature);

		EliteCreatureService.promote(creature);

		assertEquals(140, creature.getBaseHP());
		assertEquals(115, creature.getAtk());
		assertEquals(88, creature.getDef());
		assertEquals(1500, creature.getXP());
	}

	private Creature createCreature() {
		final Creature creature = new Creature();
		creature.setName("testowy potwór");
		creature.setLevel(50);
		creature.setBaseHP(100);
		creature.setHP(100);
		creature.setAtk(100);
		creature.setRatk(50);
		creature.setDef(80);
		creature.setXP(1000);
		return creature;
	}
}
