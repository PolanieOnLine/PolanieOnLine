/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.entity.player;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.Nature;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.transformer.ItemTransformer;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;

public class EquipmentSetElementalResistanceTest {

	private static final String[] SLOTS = {
			"armor", "head", "cloak", "legs", "feet", "lhand", "glove", "pas" };
	private static final String[] BLACK = {
			"czarna zbroja", "czarny hełm", "czarny płaszcz", "czarne spodnie",
			"czarne buty", "czarna tarcza", "czarne rękawice", "czarny pas" };
	private static final String[] MITHRIL = {
			"zbroja z mithrilu", "hełm z mithrilu", "płaszcz z mithrilu", "spodnie z mithrilu",
			"buty z mithrilu", "tarcza z mithrilu", "rękawice z mithrilu", "pas z mithrilu" };
	private static final double[] PROTECTION = { 0.9, 0.9, 0.9, 0.95, 0.95, 0.9, 0.98, 0.95 };
	private static final double[] VULNERABILITY = { 1.2, 1.2, 1.2, 1.15, 1.15, 1.15, 1.15, 1.15 };
	private static final double SET_PROTECTION = 0.55127326275;
	private static final double SET_VULNERABILITY = 3.47562522;
	private static final double EPSILON = 0.000000001;
	private static final String[] ICE = {
			"lodowa zbroja", "hełm lodowy", "lodowy płaszcz", "lodowe spodnie", "lodowe buty", "lodowa tarcza", "lodowe rękawice", "lodowy pas" };
	private static final String[] FIRE = {
			"ognista zbroja", null, null, "ogniste spodnie", "ogniste buty", "ognista tarcza", "ogniste rękawice", "ognisty pas" };
	private static final String[] SHADOW = {
			"zbroja cieni", "hełm cieni", "płaszcz cieni", "spodnie cieni", "buty cieni", "tarcza cieni", "rękawice cieni", "pas cieni" };
	private static final String[] ELVISH = {
			"zbroja elficka", "hełm elficki", "płaszcz elficki", "spodnie elfickie", "buty elfickie", "tarcza elficka", "rękawice elfickie", "pas elficki" };
	private static final String[] MAGIC = {
			"magiczna zbroja płytowa", "magiczny hełm kolczy", "magiczny płaszcz", "magiczne spodnie płytowe", "magiczne buty płytowe", "magiczna tarcza płytowa", "magiczne rękawice płytowe", "magiczny pas płytowy" };
	private static final String[] ROYAL = {
			"zbroja monarchistyczna", "hełm monarchistyczny", "płaszcz monarchistyczny", "spodnie monarchistyczne", "buty monarchistyczne", "tarcza monarchistyczna", null, null };

	@BeforeClass
	public static void setUpWorld() {
		MockStendlRPWorld.get();
	}

	@Test
	public void blackSetProtectsAgainstDarkAndIsVulnerableToLight() {
		assertSet(BLACK, Nature.DARK, Nature.LIGHT, 289);
	}

	@Test
	public void mithrilSetProtectsAgainstLightAndIsVulnerableToDark() {
		assertSet(MITHRIL, Nature.LIGHT, Nature.DARK, 316);
	}

	@Test
	public void equipmentInBackpackDoesNotProvideElementalProtection() {
		final Player player = PlayerTestHelper.createPlayer("backpack_resistance");
		for (final String name : BLACK) {
			assertTrue(player.equipToInventoryOnly(createItem(name)));
		}
		for (final Nature nature : Nature.values()) {
			assertEquals(nature.name(), 1.0, player.getSusceptibility(nature), EPSILON);
		}
	}

	@Test
	public void restoredUpgradedEquipmentUsesCurrentElementalDefinitions() throws IOException {
		for (final String[] set : new String[][] { BLACK, MITHRIL, ICE, FIRE, SHADOW, ELVISH, MAGIC, ROYAL }) {
			for (final String name : set) {
				if (name == null) {
					continue;
				}
				final Item original = createItem(name);
				original.setID(new ID(201, "equipment_resistance"));
				if (original.has(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE)) {
					original.setUpgradeLevel(original.getInt(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE));
				}
				final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				original.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
				final RPObject saved = new RPObject();
				saved.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
				final Item restored = new ItemTransformer().transform(saved);
				assertNotNull(name, restored);
				assertEquals(name, original.getUpgradeLevel(), restored.getUpgradeLevel());
				for (final Nature nature : Nature.values()) {
					assertEquals(name + " " + nature, original.getSusceptibility(nature),
							restored.getSusceptibility(nature), EPSILON);
				}
			}
		}
	}

	private void assertSet(final String[] names, final Nature own, final Nature opposite,
			final int expectedDefense) {
		final Player player = PlayerTestHelper.createPlayer("equipment_resistance");
		player.setLevel(597);
		for (int i = 0; i < names.length; i++) {
			final Item item = createItem(names[i]);
			assertEquals(names[i], PROTECTION[i], item.getSusceptibility(own), EPSILON);
			assertEquals(names[i], VULNERABILITY[i], item.getSusceptibility(opposite), EPSILON);
			assertTrue(names[i], player.equip(SLOTS[i], item));
		}
		assertEquals(SET_PROTECTION, player.getSusceptibility(own), EPSILON);
		assertEquals(SET_VULNERABILITY, player.getSusceptibility(opposite), EPSILON);
		assertEquals(expectedDefense, player.getItemDef(), EPSILON);
		for (final Nature nature : Nature.values()) {
			if (nature != own && nature != opposite) {
				assertEquals(nature.name(), 1.0, player.getSusceptibility(nature), EPSILON);
			}
		}
	}


	@Test
	public void iceSetHasBalancedProtectionAndVulnerability() {
		assertSpecializedSet(ICE, Nature.ICE, Nature.FIRE, SET_PROTECTION, SET_VULNERABILITY, 153);
		assertEquals(0.95, equipSet(ICE).getSusceptibility(Nature.WATER), EPSILON);
	}

	@Test
	public void fireSetIsBalancedAcrossItsSixAvailablePieces() {
		assertSpecializedSet(FIRE, Nature.FIRE, Nature.ICE, 0.559234125, 3.51, 128);
		final Player player = equipSet(FIRE);
		assertEquals(0.9, player.getSusceptibility(Nature.EARTH), EPSILON);
		assertEquals(1.1, player.getSusceptibility(Nature.WATER), EPSILON);
	}

	@Test
	public void shadowSetHasBalancedProtectionAndVulnerability() {
		assertSpecializedSet(SHADOW, Nature.DARK, Nature.LIGHT, SET_PROTECTION, SET_VULNERABILITY, 196);
	}

	@Test
	public void elvishSetHasBalancedProtectionAndVulnerability() {
		assertSpecializedSet(ELVISH, Nature.EARTH, Nature.FIRE, SET_PROTECTION, SET_VULNERABILITY, 159);
	}

	@Test
	public void elvishHatKeepsTheSameElementalProfileAsElvishHelmet() {
		final String[] alternative = ELVISH.clone();
		alternative[1] = "kapelusz elficki";
		assertSpecializedSet(alternative, Nature.EARTH, Nature.FIRE,
				SET_PROTECTION, SET_VULNERABILITY, 153);
	}

	@Test
	public void magicSetDistributesUniversalProtectionAcrossEightPieces() {
		assertUniversalSet(MAGIC, 0.8334890961549375, 254);
	}

	@Test
	public void royalSetKeepsTheStendhalUniversalProtection() {
		assertUniversalSet(ROYAL, 0.7987202615, 252);
	}

	@Test
	public void endgameNocturiumSetKeepsItsExistingProtectionWithoutVulnerability() {
		final Player player = equipSet(new String[] {
				"zbroja ciemnomithrilowa", "hełm ciemnomithrilowy", "płaszcz ciemnomithrilowy",
				"spodnie ciemnomithrilowe", "buty ciemnomithrilowe", "tarcza ciemnomithrilowa",
				null, "pas ciemnomithrilowy" });
		assertEquals(0.4782969, player.getSusceptibility(Nature.DARK), EPSILON);
		assertEquals(0.4782969, player.getSusceptibility(Nature.LIGHT), EPSILON);
		assertEquals(1.0, player.getSusceptibility(Nature.FIRE), EPSILON);
		assertEquals(1.0, player.getSusceptibility(Nature.ICE), EPSILON);
	}

	private void assertSpecializedSet(final String[] names, final Nature own, final Nature opposite,
			final double protection, final double vulnerability, final int expectedDefense) {
		final Player player = equipSet(names);
		assertEquals(protection, player.getSusceptibility(own), EPSILON);
		assertEquals(vulnerability, player.getSusceptibility(opposite), EPSILON);
		assertEquals(1.0, player.getSusceptibility(Nature.CUT), EPSILON);
		assertEquals(expectedDefense, player.getItemDef(), EPSILON);
		assertTrue("Full-set elemental resistance must be between 40 and 50 percent",
				protection >= 0.5 && protection <= 0.6);
		assertTrue("Opposing element must deal about 250 percent more damage",
				vulnerability >= 3.4 && vulnerability <= 3.6);
	}

	private void assertUniversalSet(final String[] names, final double protection, final int expectedDefense) {
		final Player player = equipSet(names);
		for (final Nature nature : Nature.values()) {
			final boolean protectedElement = nature == Nature.FIRE || nature == Nature.ICE
					|| nature == Nature.DARK || nature == Nature.LIGHT;
			assertEquals(nature.name(), protectedElement ? protection : 1.0,
					player.getSusceptibility(nature), EPSILON);
		}
		assertEquals(expectedDefense, player.getItemDef(), EPSILON);
	}

	private Player equipSet(final String[] names) {
		final Player player = PlayerTestHelper.createPlayer("balanced_equipment");
		player.setLevel(597);
		for (int i = 0; i < names.length; i++) {
			if (names[i] != null) {
				assertTrue(names[i], player.equip(SLOTS[i], createItem(names[i])));
			}
		}
		return player;
	}

	private Item createItem(final String name) {
		final Item item = SingletonRepository.getEntityManager().getItem(name, ItemCreationContext.starter());
		assertNotNull(name, item);
		return item;
	}
}
