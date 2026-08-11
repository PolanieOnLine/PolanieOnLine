/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Level;
import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService;
import games.stendhal.server.core.rule.glyph.GlyphEffectService;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Weapon;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.player.UpdateConverter;
import games.stendhal.server.entity.slot.PlayerSlot;
import utilities.PlayerTestHelper;
import utilities.RPClass.CreatureTestHelper;
import utilities.RPClass.ItemTestHelper;

public class RPEntityWeaponDamageRollTest {
	private static final String OFFENSIVE_RUNE_SLOT = "offensive_rune";

	@BeforeClass
	public static void generateRPClasses() {
		ItemTestHelper.generateRPClasses();
		CreatureTestHelper.generateRPClasses();
	}

	@Test
	public void mixedWeaponRollsUseIndependentArmorMultipliers() {
		final Weapon dagger = fixedWeapon("dagger", 20);
		final Weapon axe = fixedWeapon("axe", 10);
		final TestEntity attacker = new TestEntity(Arrays.<Item>asList(
				dagger, axe), 50.0f);
		final Creature defender = new Creature();
		defender.setArmorType("heavy");

		// Heavy armor: dagger 40%, axe 130%. The extra 50 ATK represents
		// rings/glyphs/other equipment and must not receive either multiplier.
		final float expected = 50.0f + 20.0f * 0.40f + 10.0f * 1.30f;
		final float attack = attacker.getItemAtkForAttack(weapon ->
				WeaponArmorInteractionService.getDamageMultiplier(
						weapon, defender));

		assertEquals(expected, attack, 0.0001f);
	}

	@Test
	public void unmodifiedRollPreservesStableAttack() {
		final Weapon dagger = fixedWeapon("dagger", 20);
		final Weapon axe = fixedWeapon("axe", 10);
		final TestEntity attacker = new TestEntity(Arrays.<Item>asList(
				dagger, axe), 50.0f);

		assertEquals(80.0f, attacker.getItemAtkForAttack(), 0.0001f);
	}

	@Test
	public void glyphAttackPercentageFollowsActualModifiedWeaponRoll() {
		final Weapon dagger = fixedWeapon("dagger", 20);
		final Weapon axe = fixedWeapon("axe", 10);
		final TestDressedEntity attacker = new TestDressedEntity(
				Arrays.<Item>asList(dagger, axe), 50, 10.0);
		final Creature defender = new Creature();
		defender.setArmorType("heavy");

		// Stable equipment attack is 80, so the stable value is 88 with the
		// +10% glyph. Against heavy armor the actual weapon contribution is
		// 20*0.40 + 10*1.30 = 21. The percentage must follow that actual 21-point
		// contribution: (50 + 21) * 1.10 = 78.1. Applying the glyph to the stable
		// weapon value before substituting the roll would incorrectly produce 79.
		final float expected = (50.0f + 20.0f * 0.40f + 10.0f * 1.30f) * 1.10f;
		final float attack = attacker.getItemAtkForAttack(weapon ->
				WeaponArmorInteractionService.getDamageMultiplier(
						weapon, defender));

		assertEquals(expected, attack, 0.0001f);
	}

	@Test
	public void skillAttackGlyphDoesNotMutateStoredAttack() {
		final TestDressedEntity attacker = new TestDressedEntity(
				Arrays.<Item>asList(), 0, 0.0);
		attacker.setAtk(20);
		attacker.setSkillAttackBonus(5);

		assertEquals(20, attacker.getInt("atk"));
		assertEquals(25, attacker.getAtk());
		assertEquals(25, attacker.getCappedAtk());

		attacker.clearGlyphs();
		assertEquals(20, attacker.getInt("atk"));
		assertEquals(20, attacker.getAtk());
	}

	@Test
	public void skillAttackGlyphDoesNotCorruptAttackXpProgression() {
		final TestDressedEntity attacker = new TestDressedEntity(
				Arrays.<Item>asList(), 0, 0.0);
		attacker.setName("glyph-atk-xp-test");
		attacker.setAtk(20);
		attacker.setSkillAttackBonus(5);

		// Level 11 ATK XP should advance the trained/raw ATK from 20 to 21.
		// The equipped +5 skill_atk remains a dynamic effect on top of that raw
		// value instead of participating in the XP level-delta calculation.
		attacker.setAtkXP(Level.getXP(11));

		assertEquals(21, attacker.getInt("atk"));
		assertEquals(26, attacker.getAtk());

		attacker.clearGlyphs();
		assertEquals(21, attacker.getAtk());
	}

	@Test
	public void healthGlyphStatePreservesCurrentHpRules() {
		final Player player = PlayerTestHelper.createPlayer("glyph-health-test");
		final Item glyph = new Item("test health glyph", "glyph", "test", null);
		glyph.put("health", 100);

		player.setBaseHP(1000);
		player.setHP(900);
		GlyphEffectService.applyHealthOnEquipped(player, glyph);
		assertEquals(1100, player.getBaseHP());
		assertEquals(900, player.getHP());

		GlyphEffectService.applyHealthOnUnequipped(player, glyph);
		assertEquals(1000, player.getBaseHP());
		assertEquals(900, player.getHP());

		GlyphEffectService.applyHealthOnEquipped(player, glyph);
		player.setHP(1100);
		GlyphEffectService.applyHealthOnUnequipped(player, glyph);
		assertEquals(1000, player.getBaseHP());
		assertEquals(1000, player.getHP());
	}

	@Test
	public void loginBaseHpNormalizationUsesGlyphEffectService() {
		final Player player = PlayerTestHelper.createPlayer("glyph-login-health-test");
		final Item glyph = new Item("test login health glyph", "glyph", "test", null);
		glyph.put("health", 100);
		if (!player.hasSlot("healing_rune")) {
			player.addSlot(new PlayerSlot("healing_rune"));
		}
		player.getSlot("healing_rune").add(glyph);

		player.setBaseHP(50);
		player.setHP(50);
		UpdateConverter.updateBaseHP(player);

		// Level 0 base HP is 100 and the equipped glyph adds another 100.
		assertEquals(200, player.getBaseHP());
		assertEquals(50, player.getHP());
	}

	@Test
	public void legacyRateIncreaseKeyIsExposedAsAttackRateReduction() {
		final TestDressedEntity attacker = new TestDressedEntity(
				Arrays.<Item>asList(), 0, 0.0);
		attacker.setAttackRateReduction(1);

		assertEquals(1.0,
				GlyphEffectService.getAttackRateReduction(attacker), 0.0001);
	}

	private static Weapon fixedWeapon(final String weaponClass,
			final int damage) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", Integer.toString(damage));
		attributes.put("damage_min", Integer.toString(damage));
		attributes.put("damage_max", Integer.toString(damage));
		attributes.put("rate", "5");
		return new Weapon("test " + weaponClass, weaponClass, "test",
				attributes);
	}

	private static final class TestEntity extends RPEntity {
		private final List<Item> weapons;
		private final float nonWeaponAttack;

		private TestEntity(final List<Item> weapons,
				final float nonWeaponAttack) {
			this.weapons = weapons;
			this.nonWeaponAttack = nonWeaponAttack;
		}

		@Override
		public List<Item> getWeapons() {
			return weapons;
		}

		@Override
		public float getItemAtk() {
			float attack = nonWeaponAttack;
			for (final Item weapon : weapons) {
				attack += weapon.getAverageDamage();
			}
			return attack;
		}

		@Override
		protected void dropItemsOn(final Corpse corpse) {
			// no items
		}

		@Override
		public void logic() {
			// no logic
		}
	}

	private static final class TestDressedEntity extends DressedEntity {
		private final List<Item> weapons;
		private final Item glyph;

		private TestDressedEntity(final List<Item> weapons,
				final int flatGlyphAttack, final double glyphAttackPercentage) {
			this.weapons = weapons;
			addSlot(OFFENSIVE_RUNE_SLOT);
			glyph = new Item("test attack glyph", "glyph", "test", null);
			glyph.put("atk", flatGlyphAttack);
			glyph.put("atk_additional_bonus", glyphAttackPercentage);
			getSlot(OFFENSIVE_RUNE_SLOT).add(glyph);
		}

		private void setSkillAttackBonus(final int bonus) {
			glyph.put("skill_atk", bonus);
		}

		private void setAttackRateReduction(final int reduction) {
			glyph.put("rate_increase", reduction);
		}

		private void clearGlyphs() {
			getSlot(OFFENSIVE_RUNE_SLOT).clear();
		}

		@Override
		public List<Item> getWeapons() {
			return weapons;
		}

		@Override
		protected void dropItemsOn(final Corpse corpse) {
			// no items
		}

		@Override
		public void logic() {
			// no logic
		}
	}
}
