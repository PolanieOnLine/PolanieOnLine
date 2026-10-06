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
		for (final String[] set : new String[][] { BLACK, MITHRIL }) {
			for (final String name : set) {
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

	private Item createItem(final String name) {
		final Item item = SingletonRepository.getEntityManager().getItem(name, ItemCreationContext.starter());
		assertNotNull(name, item);
		return item;
	}
}
