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
	private static final double SET_PROTECTION = 0.55127326275;
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
		assertSpecializedSet(BLACK, Nature.DARK, Nature.LIGHT, 0.51258968208, 3.79787616, 289);
		assertPieceProfile(BLACK, Nature.DARK, Nature.LIGHT,
				new double[] { 0.9, 0.85, 0.85, 0.95, 0.9, 0.96, 0.98, 0.98 },
				new double[] { 1.2, 1.3, 1.3, 1.2, 1.2, 1.25, 1.02, 1.02 });
	}

	@Test
	public void mithrilSetProtectsAgainstLightAndIsVulnerableToDark() {
		assertSpecializedSet(MITHRIL, Nature.LIGHT, Nature.DARK, 0.55009339812, 4.11436584, 316);
		assertPieceProfile(MITHRIL, Nature.LIGHT, Nature.DARK,
				new double[] { 0.9, 0.9, 0.9, 0.9, 0.9, 0.97, 0.98, 0.98 },
				new double[] { 1.2, 1.3, 1.3, 1.3, 1.2, 1.25, 1.02, 1.02 });
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

	@Test
	public void blackAndMithrilSetsDoNotAffectOtherElements() {
		for (final String[] set : new String[][] { BLACK, MITHRIL }) {
			final Player player = equipSet(set);
			for (final Nature nature : Nature.values()) {
				if (nature != Nature.DARK && nature != Nature.LIGHT) {
					assertEquals(nature.name(), 1.0, player.getSusceptibility(nature), EPSILON);
				}
			}
		}
	}


	@Test
	public void iceSetExtendsStendhalArmorAndShieldWithSixAdditionalPieces() {
		assertSpecializedSet(ICE, Nature.ICE, Nature.FIRE, 0.46310789109375, 3.055942725, 153);
		assertPieceProfile(ICE, Nature.ICE, Nature.FIRE,
				new double[] { 0.7, 0.95, 0.95, 0.95, 0.95, 0.9, 0.95, 0.95 },
				new double[] { 1.5, 1.1, 1.1, 1.1, 1.1, 1.15, 1.1, 1.1 });
		assertEquals(0.95, equipSet(ICE).getSusceptibility(Nature.WATER), EPSILON);
	}

	@Test
	public void fireSetIsBalancedAcrossItsSixAvailablePieces() {
		assertSpecializedSet(FIRE, Nature.FIRE, Nature.ICE, 0.5131389375, 2.5255725, 128);
		assertPieceProfile(FIRE, Nature.FIRE, Nature.ICE,
				new double[] { 0.7, 1.0, 1.0, 0.95, 0.95, 0.9, 0.95, 0.95 },
				new double[] { 1.5, 1.0, 1.0, 1.1, 1.1, 1.15, 1.1, 1.1 });
		final Player player = equipSet(FIRE);
		assertEquals(0.9, player.getSusceptibility(Nature.EARTH), EPSILON);
		assertEquals(1.1, player.getSusceptibility(Nature.WATER), EPSILON);
	}

	@Test
	public void shadowSetKeepsProtectionWithAboutSixHundredPercentExtraLightDamage() {
		assertSpecializedSet(SHADOW, Nature.DARK, Nature.LIGHT, 0.31814208, 7.0533845971875, 196);
		assertPieceProfile(SHADOW, Nature.DARK, Nature.LIGHT,
				new double[] { 0.8, 0.85, 0.8, 0.9, 0.8, 0.9, 0.95, 0.95 },
				new double[] { 1.35, 1.35, 1.35, 1.35, 1.35, 1.3, 1.1, 1.1 });
	}

	@Test
	public void elvishSetHasBalancedProtectionAndVulnerability() {
		assertSpecializedSet(ELVISH, Nature.EARTH, Nature.FIRE, SET_PROTECTION, 4.4189517409375, 159);
		assertPieceProfile(ELVISH, Nature.EARTH, Nature.FIRE, PROTECTION,
				new double[] { 1.3, 1.3, 1.3, 1.15, 1.15, 1.15, 1.15, 1.15 });
	}

	@Test
	public void elvishHatKeepsTheSameElementalProfileAsElvishHelmet() {
		final String[] alternative = ELVISH.clone();
		alternative[1] = "kapelusz elficki";
		assertSpecializedSet(alternative, Nature.EARTH, Nature.FIRE,
				SET_PROTECTION, 4.4189517409375, 153);
	}

	@Test
	public void magicSetDistributesUniversalProtectionAcrossEightPieces() {
		assertUniversalSet(MAGIC, 0.8165659002209295, 254);
		for (final String name : new String[] { MAGIC[6], MAGIC[7] }) {
			final Item item = createItem(name);
			for (final Nature nature : new Nature[] { Nature.FIRE, Nature.ICE, Nature.DARK, Nature.LIGHT }) {
				assertEquals(name, 0.97, item.getSusceptibility(nature), EPSILON);
			}
		}
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
	}


	@Test
	public void additionalSetPiecesIncreaseBothProtectionAndOpposingVulnerability() {
		final String[][] sets = { ICE, FIRE, SHADOW, BLACK, MITHRIL };
		final Nature[] own = { Nature.ICE, Nature.FIRE, Nature.DARK, Nature.DARK, Nature.LIGHT };
		final Nature[] opposite = { Nature.FIRE, Nature.ICE, Nature.LIGHT, Nature.LIGHT, Nature.DARK };
		final double[] baseProtection = { 0.63, 0.63, 0.352512, 0.5337252, 0.5727753 };
		final double[] baseVulnerability = { 1.725, 1.725, 5.82924346875, 3.6504, 3.9546 };
		for (int i = 0; i < sets.length; i++) {
			final String[] base = sets[i].clone();
			for (int slot = 0; slot < base.length; slot++) {
				if (i >= 2 ? slot >= 6 : slot != 0 && slot != 5) {
					base[slot] = null;
				}
			}
			final Player basePlayer = equipSet(base);
			assertEquals(baseProtection[i], basePlayer.getSusceptibility(own[i]), EPSILON);
			assertEquals(baseVulnerability[i], basePlayer.getSusceptibility(opposite[i]), EPSILON);
			final Player fullPlayer = equipSet(sets[i]);
			assertTrue(fullPlayer.getSusceptibility(own[i]) < basePlayer.getSusceptibility(own[i]));
			assertTrue(fullPlayer.getSusceptibility(opposite[i]) > basePlayer.getSusceptibility(opposite[i]));
		}
	}

	@Test
	public void blackAndMithrilExtraSlotsStayWithinFivePercentOfStendhalMultipliers() {
		final String[][] sets = { BLACK, MITHRIL };
		final Nature[] own = { Nature.DARK, Nature.LIGHT };
		final Nature[] opposite = { Nature.LIGHT, Nature.DARK };
		final double[] stendhalProtection = { 0.5337252, 0.5727753 };
		final double[] stendhalVulnerability = { 3.6504, 3.9546 };
		for (int i = 0; i < sets.length; i++) {
			final Player player = equipSet(sets[i]);
			assertTrue(player.getSusceptibility(own[i]) >= stendhalProtection[i] * 0.95);
			assertTrue(player.getSusceptibility(opposite[i]) <= stendhalVulnerability[i] * 1.05);
		}
	}

	private void assertPieceProfile(final String[] names, final Nature own, final Nature opposite,
			final double[] protection, final double[] vulnerability) {
		for (int i = 0; i < names.length; i++) {
			if (names[i] != null) {
				final Item item = createItem(names[i]);
				assertEquals(names[i], protection[i], item.getSusceptibility(own), EPSILON);
				assertEquals(names[i], vulnerability[i], item.getSusceptibility(opposite), EPSILON);
			}
		}
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


	@Test
	public void balancedEquipmentUsesAtMostTwoDecimalPlacesForElementalValues() {
		for (final String[] set : new String[][] { BLACK, MITHRIL, ICE, FIRE, SHADOW, ELVISH, MAGIC, ROYAL }) {
			for (final String name : set) {
				if (name == null) {
					continue;
				}
				final Item item = createItem(name);
				for (final Nature nature : Nature.values()) {
					final double scaledValue = item.getSusceptibility(nature) * 100.0;
					assertEquals(name + " " + nature, Math.rint(scaledValue), scaledValue, EPSILON);
				}
			}
		}
	}

	private Item createItem(final String name) {
		final Item item = SingletonRepository.getEntityManager().getItem(name, ItemCreationContext.starter());
		assertNotNull(name, item);
		return item;
	}
}
