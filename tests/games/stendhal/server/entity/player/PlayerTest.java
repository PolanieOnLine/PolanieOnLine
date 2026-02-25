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
package games.stendhal.server.entity.player;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.KeyedSlotUtil;
import games.stendhal.common.Level;
import games.stendhal.common.MasteryLevel;
import games.stendhal.common.constants.Nature;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.action.IncreaseXPDependentOnLevelAction;
import games.stendhal.server.entity.status.StatusType;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPObject;
import marauroa.server.game.db.DatabaseFactory;
import utilities.PlayerTestHelper;

public class PlayerTest {
	private String playername = "player";
	private Player player;
	private Player killer;
	private StendhalRPZone zone;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		new DatabaseFactory().initializeDatabase();
		MockStendlRPWorld.get();
	}

	@After
	public void tearDown() throws Exception {
		MockStendhalRPRuleProcessor.get().clearPlayers();
	}

	@Before
	public void setUp() throws Exception {
		zone = new StendhalRPZone("the zone where the corpse shall be slain");
		player = PlayerTestHelper.createPlayer(playername);
		zone.add(player);
		killer = PlayerTestHelper.createPlayer("killer");
	}

	/**
	 * Tests for hashCode.
	 */
	@Test
	public void testHashCode() {
		assertThat(player.hashCode(), is(playername.hashCode()));
	}

	/**
	 * Tests for equalsObject.
	 */
	@Test
	public void testEqualsObject() {
		assertThat(player, equalTo(player));
		assertThat(player, equalTo(PlayerTestHelper.createPlayer(playername)));
		assertThat(player, not(equalTo(PlayerTestHelper.createPlayer(playername + 's'))));
	}

	/**
	 * Tests for toString.
	 */
	@Test
	public void testToString() {
		assertThat(player.toString(), is("Player [" + playername + ", " + playername.hashCode() + ']'));
	}

	/**
	 * Tests for isObstacle.
	 */
	@Test
	public void testIsObstacle() {
		final Entity ent = new Entity() {
			// just a sub class
		};
		ent.setResistance(100);
		assertTrue(player.isObstacle(ent));
		ent.setResistance(95);

		assertFalse(player.isObstacle(ent));
		assertThat(player.getResistance(ent), is(95));
	}

	/**
	 * Tests for onAdded.
	 */
	@Test
	public void testOnAdded() {
		player.onAdded(new StendhalRPZone("playertest"));
		RPObject object = KeyedSlotUtil.getKeyedSlotObject(player, "!visited");
		assertNotNull("slot not found", object);
		assertTrue(object.has("playertest"));
		assertThat(player.get("visibility"), is("100"));
		player.onAdded(new StendhalRPZone(PlayerDieer.DEFAULT_DEAD_AREA));

		object = KeyedSlotUtil.getKeyedSlotObject(player, "!visited");
		assertNotNull("slot not found", object);
		assertTrue(object.has(PlayerDieer.DEFAULT_DEAD_AREA));
		assertThat(player.get("visibility"), is("50"));
		player.onRemoved(new StendhalRPZone(PlayerDieer.DEFAULT_DEAD_AREA));
		assertThat(player.get("visibility"), is("100"));
	}

	/**
	 * Tests for describe.
	 */
	@Test
	public void testDescribe() {
		player.setGender("M");

		final int hours = player.getAge() / 60;
		final int minutes = player.getAge() % 60;
		final String time = "Wiek " + hours + " godzinę i " + minutes + " minutę";
		assertThat(player.describe(), is("Oto parobek " + player.getTitle() + ".\n" + player.getTitle() + " posiada poziom "
				+ player.getLevel() + ". " + time + "."));
	}

	/**
	 * Tests for describeOfPlayerWithAwayMessage.
	 */
	@Test
	public void testDescribeOfPlayerWithAwayMessage() {
		player.setGender("M");

		final int hours = player.getAge() / 60;
		final int minutes = player.getAge() % 60;
		final String time = "Wiek " + hours + " godzinę i " + minutes + " minutę";
		player.setAwayMessage("I am away.");
		String description = player.describe();
		String expectedDescription = "Oto parobek " + player.getTitle() + ".\n"
				+ player.getTitle() + " posiada poziom " + player.getLevel()
				+ ". " + time + ".\n"
				+ player.getTitle() + " nie ma go teraz, ale zostawił wiadomość: "
				+ player.getAwayMessage();
		assertThat(description, is(expectedDescription));
	}

	/**
	 * Tests for describeOfPlayerWithGrumpyMessage.
	 */
	@Test
	public void testDescribeOfPlayerWithGrumpyMessage() {
		player.setGender("M");

		final int hours = player.getAge() / 60;
		final int minutes = player.getAge() % 60;
		final String time = "Wiek " + hours + " godzinę i " + minutes + " minutę";
		player.setGrumpyMessage("I am grumpy.");
		String description = player.describe();
		String expectedDescription = "Oto parobek " + player.getTitle() + ".\n"
				+ player.getTitle() + " posiada poziom " + player.getLevel()
				+ ". " + time + ".\n"
				+ player.getTitle() + " ukrywa się i zostawił wiadomość: "
				+ player.getGrumpyMessage();
		assertThat(description, is(expectedDescription));
	}

	/**
	 * Tests for describeOfPlayerWithAwayAndGrumpyMessage.
	 */
	@Test
	public void testDescribeOfPlayerWithAwayAndGrumpyMessage() {
		player.setGender("M");

		final int hours = player.getAge() / 60;
		final int minutes = player.getAge() % 60;
		final String time = "Wiek " + hours + " godzinę i " + minutes + " minutę";
		player.setAwayMessage("I am away.");
		player.setGrumpyMessage("I am grumpy.");
		String description = player.describe();
		String expectedDescription = "Oto parobek " + player.getTitle() + ".\n"
				+ player.getTitle() + " posiada poziom " + player.getLevel()
				+ ". " + time + ".\n"
				+ player.getTitle() + " nie ma go teraz, ale zostawił wiadomość: "
				+ player.getAwayMessage() + "\n"
				+ player.getTitle() + " ukrywa się i zostawił wiadomość: "
				+ player.getGrumpyMessage();
		assertThat(description, is(expectedDescription));
	}

	/**
	 * Tests for isGhost.
	 */
	@Test
	public void testIsGhost() {
		assertFalse(player.isGhost());
		player.setGhost(true);
		assertTrue(player.isGhost());
		player.setGhost(false);
		assertFalse(player.isGhost());

	}

	/**
	 * Tests for addGetUseKarma.
	 */
	@Test
	public void testAddGetUseKarma() {

		assertThat(player.getKarma(), is(10.0));
		player.addKarma(5.0);
		assertThat(player.getKarma(), is(15.0));
		assertThat(player.getDouble("karma"), is(player.getKarma()));
		player.useKarma(5.0);
		assertTrue(player.getKarma() >= 10.0);
		assertTrue(player.getKarma() <= 15.0);

	}

	/**
	 * Tests for isInvisible.
	 */
	@Test
	public void testIsInvisible() {
		final Player player2 = PlayerTestHelper.createPlayer("player2");
		assertThat(player2.isInvisibleToCreatures(), not(is(true)));
		player2.setInvisible(true);
		assertThat(player2.isInvisibleToCreatures(), is(true));
		player2.setInvisible(false);
		assertThat(player2.isInvisibleToCreatures(), not(is(true)));
	}

	/**
	 * Tests for setImmune.
	 */
	@Test
	public void testSetImmune() {
		Player bob = PlayerTestHelper.createPlayer("bob");
		assertFalse(bob.getStatusList().isImmune(StatusType.POISONED));
		bob.getStatusList().setImmune(StatusType.POISONED);
		assertTrue(bob.getStatusList().isImmune(StatusType.POISONED));
	}


	/**
	 * Tests for removeImmunity.
	 */
	@Test
	public void testRemoveImmunity() {
		Player bob = PlayerTestHelper.createPlayer("bob");
		assertFalse(bob.getStatusList().isImmune(StatusType.POISONED));
		bob.getStatusList().setImmune(StatusType.POISONED);
		assertTrue(bob.getStatusList().isImmune(StatusType.POISONED));
		bob.getStatusList().removeImmunity(StatusType.POISONED);
		assertFalse(bob.getStatusList().isImmune(StatusType.POISONED));

	}

	/**
	 * Tests for isBadBoy.
	 */
	@Test
	public void testIsBadBoy() {
		assertFalse(player.isBadBoy());
		assertFalse(killer.isBadBoy());

		player.onDead(killer);
		assertTrue(killer.isBadBoy());
		assertFalse(player.isBadBoy());
	}

	/**
	 * Tests for rehabilitate.
	 */
	@Test
	public void testRehabilitate() {
		assertFalse(player.isBadBoy());
		assertFalse(killer.isBadBoy());

		player.onDead(killer);
		assertTrue(killer.isBadBoy());
		assertFalse(player.isBadBoy());
		killer.rehabilitate();
		assertFalse(player.isBadBoy());
		assertFalse(killer.isBadBoy());

	}

	/**
	 * Tests for getWidth.
	 */
	@Test
	public void testgetWidth() {
		Player bob = PlayerTestHelper.createPlayer("bob");
		assertThat(bob.getWidth(), is(1.0));
		assertThat(bob.get("width"), is("1"));

		assertThat(bob.getHeight(), is(1.0));
		assertThat(bob.get("height"), is("1"));

		Player george = Player.createZeroLevelPlayer("george2", null);
		assertThat(george.getWidth(), is(1.0));
		assertThat(george.get("width"), is("1"));

		assertThat(george.getHeight(), is(1.0));
		assertThat(george.get("height"), is("1"));
	}

	/**
	 * Tests for quest.
	 */
	@Test
	public void testQuest() {
		Player player = PlayerTestHelper.createPlayer("questTestPlayer");
		player.setQuest("testquest", "start");
		assertThat(player.getQuest("testquest"), equalTo("start"));
		assertThat(player.getQuest("testquest", 0), equalTo("start"));
		assertThat(player.getQuest("testquest", 1), equalTo(""));

		player.setQuest("testquest", 0, "cont");
		assertThat(player.getQuest("testquest"), equalTo("cont"));
		assertThat(player.getQuest("testquest", 0), equalTo("cont"));
		assertThat(player.getQuest("testquest", 1), equalTo(""));

		player.setQuest("testquest", 1, "end");
		assertThat(player.getQuest("testquest"), equalTo("cont;end"));
		assertThat(player.getQuest("testquest", 0), equalTo("cont"));
		assertThat(player.getQuest("testquest", 1), equalTo("end"));
		assertThat(player.getQuest("testquest", 2), equalTo(""));

		player.setQuest("testquest", 0, "first");
		assertThat(player.getQuest("testquest"), equalTo("first;end"));
		assertThat(player.getQuest("testquest", 0), equalTo("first"));
		assertThat(player.getQuest("testquest", 1), equalTo("end"));
		assertThat(player.getQuest("testquest", 2), equalTo(""));

		player.setQuest("testquest", 1, "second");
		assertThat(player.getQuest("testquest"), equalTo("first;second"));
		assertThat(player.getQuest("testquest", 0), equalTo("first"));
		assertThat(player.getQuest("testquest", 1), equalTo("second"));
		assertThat(player.getQuest("testquest", 2), equalTo(""));

		player.setQuest("testquest2", 1, "second");
		assertThat(player.getQuest("testquest2"), equalTo(";second"));
		assertThat(player.getQuest("testquest2", 0), equalTo(""));
		assertThat(player.getQuest("testquest2", 1), equalTo("second"));
		assertThat(player.getQuest("testquest2", 2), equalTo(""));

		assertThat(player.getQuest("testquest3"), nullValue());
		assertThat(player.getQuest("testquest3", 0), nullValue());
		assertThat(player.getQuest("testquest3", 1), nullValue());
		assertThat(player.getQuest("testquest3", 2), nullValue());

	}

	@Test
	public void testAddMasteryXPClampsSingleHugeRewardAtMasteryCap() {
		Player masteryPlayer = PlayerTestHelper.createPlayer("mastery_big_single");
		final long masteryCapXP = MasteryLevel.getXP(MasteryLevel.maxLevel());

		masteryPlayer.addMasteryXP(Long.MAX_VALUE);

		assertThat(masteryPlayer.getMasteryXP(), is(masteryCapXP));
		assertThat(masteryPlayer.getMasteryLevel(), is(MasteryLevel.maxLevel()));
	}

	@Test
	public void testAddMasteryXPMultipleHugeRewardsNearLongMaxValue() {
		Player masteryPlayer = PlayerTestHelper.createPlayer("mastery_big_multi");
		final long masteryCapXP = MasteryLevel.getXP(MasteryLevel.maxLevel());

		masteryPlayer.addMasteryXP(Long.MAX_VALUE - 10L);
		masteryPlayer.addMasteryXP(Long.MAX_VALUE - 10L);

		assertThat(masteryPlayer.getMasteryXP(), is(masteryCapXP));
		assertThat(masteryPlayer.getMasteryLevel(), is(MasteryLevel.maxLevel()));
	}

	@Test
	public void testAddMasteryXPIsNoOpAfterMasteryCapReached() {
		Player masteryPlayer = PlayerTestHelper.createPlayer("mastery_cap_noop");
		final long masteryCapXP = MasteryLevel.getXP(MasteryLevel.maxLevel());

		masteryPlayer.setMasteryLevel(MasteryLevel.maxLevel());
		masteryPlayer.setMasteryXP(masteryCapXP - 1L);
		masteryPlayer.addMasteryXP(1L);

		assertThat(masteryPlayer.getMasteryXP(), is(masteryCapXP));
		assertThat(masteryPlayer.getMasteryLevel(), is(MasteryLevel.maxLevel()));

		masteryPlayer.addMasteryXP(5000L);
		assertThat(masteryPlayer.getMasteryXP(), is(masteryCapXP));
		assertThat(masteryPlayer.getMasteryLevel(), is(MasteryLevel.maxLevel()));
	}


	@Test
	public void testMasteryUnlockRequiresMaxLevelAndRebornFive() {
		Player masteryPlayer = PlayerTestHelper.createPlayer("mastery_unlock");

		masteryPlayer.setQuest("reset_level", "done;reborn_5");
		assertFalse(masteryPlayer.isMasteryUnlocked());

		masteryPlayer.setLevel(Level.maxLevel());
		assertTrue(masteryPlayer.isMasteryUnlocked());
	}

	@Test
	public void testAddXPAddsMasteryXPWhenMasteryUnlocked() {
		Player masteryPlayer = PlayerTestHelper.createPlayer("mastery_xp");
		masteryPlayer.setLevel(Level.maxLevel());
		masteryPlayer.setQuest("reset_level", "done;reborn_5");

		final int xpBefore = masteryPlayer.getXP();
		final long masteryXPBefore = masteryPlayer.getMasteryXP();

		masteryPlayer.addXP(42);

		assertThat(masteryPlayer.getXP(), is(xpBefore));
		assertThat(masteryPlayer.getMasteryXP(), is(masteryXPBefore + 42));
		assertThat(masteryPlayer.getFeature("mastery_unlocked_notification"), is("true"));
	}

	@Test
	public void testAddXPDoesNotRouteToMasteryWhenPlayerIsNotAtCap() {
		Player masteryPlayer = PlayerTestHelper.createPlayer("mastery_not_capped");
		masteryPlayer.setLevel(Level.maxLevel() - 1);
		masteryPlayer.setQuest("reset_level", "done;reborn_5");

		final int xpBefore = masteryPlayer.getXP();
		final long masteryXPBefore = masteryPlayer.getMasteryXP();

		masteryPlayer.addXP(42);

		assertThat(masteryPlayer.getXP(), is(xpBefore + 42));
		assertThat(masteryPlayer.getMasteryXP(), is(masteryXPBefore));
	}

	@Test
	public void testSubXPDoesNotReduceMasteryXPWhenMasteryProgressionIsActive() {
		Player masteryPlayer = PlayerTestHelper.createPlayer("mastery_subxp");
		masteryPlayer.setLevel(Level.maxLevel());
		masteryPlayer.setQuest("reset_level", "done;reborn_5");

		masteryPlayer.addXP(100);
		final int xpBeforePenalty = masteryPlayer.getXP();
		final long masteryXPBeforePenalty = masteryPlayer.getMasteryXP();

		masteryPlayer.subXP(20);

		assertThat(masteryPlayer.getXP(), is(xpBeforePenalty));
		assertThat(masteryPlayer.getMasteryXP(), is(masteryXPBeforePenalty));
	}

	@Test
	public void testIncreaseXPDependentOnLevelActionAddsMasteryXPAtCapWhenUnlocked() {
		Player masteryPlayer = PlayerTestHelper.createPlayer("mastery_daily");
		masteryPlayer.setLevel(Level.maxLevel());
		masteryPlayer.setQuest("reset_level", "done;reborn_5");

		final long masteryXPBefore = masteryPlayer.getMasteryXP();
		final double karmaBefore = masteryPlayer.getKarma();

		new IncreaseXPDependentOnLevelAction(5.0, 95.0).fire(masteryPlayer, null, null);

		assertTrue(masteryPlayer.getMasteryXP() > masteryXPBefore);
		assertThat(masteryPlayer.getKarma(), is(karmaBefore));
	}


	/**
	 * Test that the damage done by a player is of right type.
	 */
	@Test
	public void testGetDamageType() {
		Player player = PlayerTestHelper.createPlayer("don Quijote");
		assertThat("Default damage type", player.getDamageType(), is(Nature.CUT));
		Item item = new Item("torch", "junk", "subclass",
				new HashMap<String, String>());
		player.equip("rhand", item);
		for (Nature type : Nature.values()) {
			item.setDamageType(type);
			assertThat("Non weapon items should not change the damage type", player.getDamageType(), is(Nature.CUT));
		}
		// turn the item in to a weapon
		item.put("class", "club");
		for (Nature type : Nature.values()) {
			item.setDamageType(type);
			assertThat("Damage type should be got from the weapon", player.getDamageType(), is(type));
		}
	}

	/**
	 * Test that players susceptibility is calculated correctly
	 */
	@Test
	public void testGetSusceptibility() {
		Player player = PlayerTestHelper.createPlayer("test dummy");
		for (Nature type : Nature.values()) {
			assertThat("Default susceptibility", player.getSusceptibility(type), closeTo(1.0, 0.00001));
		}

		Item armor = new Item("rainbow armor", "armor", "subclass",
				new HashMap<String, String>());
		player.equip("armor", armor);
		HashMap<Nature, Double> armorMap = new HashMap<Nature, Double>();
		armor.setSusceptibilities(armorMap);

		for (Nature type : Nature.values()) {
			armorMap.put(type, 0.42);
			for (Nature type2 : Nature.values()) {
				if (type == type2) {
					assertThat(player.getSusceptibility(type2), closeTo(0.42, 0.00001));
				} else {
					assertThat(player.getSusceptibility(type2), closeTo(1.0, 0.00001));
				}
			}
			armorMap.remove(type);
		}

		Item legs = new Item("rainbow legs", "legs", "subclass",
				new HashMap<String, String>());
		player.equip("legs", legs);
		HashMap<Nature, Double> legsMap = new HashMap<Nature, Double>();
		legs.setSusceptibilities(legsMap);
		legsMap.put(Nature.ICE, 0.5);

		for (Nature type : Nature.values()) {
			armorMap.put(type, 0.42);
			for (Nature type2 : Nature.values()) {
				double expected;
				double ice = 1.0;
				if (type2 == Nature.ICE) {
					// Ice effect if we are checking ice resistance
					ice = 0.5;
				}
				if (type == type2) {
					// checking the type we gave resistance 0.42
					expected = ice * 0.42;
				} else {
					// only the ice effect, if any
					expected = ice;
				}

				assertThat("Susceptibility to " + type2, player.getSusceptibility(type2), closeTo(expected, 0.00001));
			}
			armorMap.remove(type);
		}
	}

	/**
	 * Test setting and restoring an outfit
	 */
	@Test
	public void testSetAndRestoreOutfit() {
		Player player = PlayerTestHelper.createPlayer("test dummy");

		// no original outfit
		assertThat(player.returnToOriginalOutfit(), is(false));

		// plain outfit change
		player.setOutfit(new Outfit(Integer.toString(0xfeedbeef)));
		assertThat(player.getOutfit().getCode(), is(0xfeedbeef));
		player.put("outfit_colors", "dress", 42);
		assertThat(player.getInt("outfit_colors", "dress"), is(42));
		// A temporary outfit should stash the colors
		player.setOutfit(new Outfit(Integer.toString(0xf00f)), true);
		assertThat(player.getOutfit().getCode(), is(0xf00f)); // old outfit parts are mapped to new ones, so code changes
		// cleared the old...
		assertThat(player.get("outfit_colors", "dress"), is(nullValue()));
		// ...and put it in store
		assertThat(player.getInt("outfit_colors", "dress_orig"), is(42));

		assertThat(player.returnToOriginalOutfit(), is(true));
		assertThat(player.getOutfit().getCode(), is(0xfeedbeef));
		assertThat(player.getInt("outfit_colors", "dress"), is(42));
		assertThat(player.get("outfit_colors", "dress_orig"), is(nullValue()));

		player.setOutfit(new Outfit(Integer.toString(0xf00f)), true);
		assertThat(player.getInt("outfit_colors", "dress_orig"), is(42));
		player.setOutfit(new Outfit());
		// regular outfit change should not use stored colors
		assertThat(player.get("outfit_colors", "dress"), is(nullValue()));
	}

	/**
	 * Test comparing client version to a known constant
	 */
	@Test
	public void testClientVersion() {
		Player player = PlayerTestHelper.createPlayer("test dummy");

		player.setClientVersion("0.42");
		assertThat(player.isClientNewerThan("0.41"), is(true));
		assertThat(player.isClientNewerThan("0.41.5"), is(true));
		assertThat(player.isClientNewerThan("0.42"), is(false));
		assertThat(player.isClientNewerThan("0.42.1"), is(false));
		assertThat(player.isClientNewerThan("0.53"), is(false));
		assertThat(player.isClientNewerThan("1.0"), is(false));
		assertThat(player.isClientNewerThan("1.53"), is(false));
		assertThat(player.isClientNewerThan("0.53.1"), is(false));

		// a bit future proofing
		player.setClientVersion("0.99");
		assertThat(player.isClientNewerThan("0.100"), is(false));
		player.setClientVersion("1.0");
		assertThat(player.isClientNewerThan("0.100"), is(true));
		assertThat(player.isClientNewerThan("1.1"), is(false));
		assertThat(player.isClientNewerThan("0.99"), is(true));
	}

	@Test
	public void testMagicSkill() throws Exception {
		Player player = PlayerTestHelper.createPlayer("harry");
		for (Nature nature : Nature.values()) {
			assertThat(player.getMagicSkillXp(nature), is(0));
		}
		player.increaseMagicSkillXp(Nature.LIGHT, 1);
		int magicSkillXp = player.getMagicSkillXp(Nature.LIGHT);
		assertThat(magicSkillXp, is(1));
		player.setSkill(Nature.LIGHT.toString()+"_xp", "blah");
		int magicSkillXpLater = player.getMagicSkillXp(Nature.LIGHT);
		assertThat(magicSkillXpLater, is(0));
	}
}
