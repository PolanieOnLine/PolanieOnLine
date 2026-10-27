/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.engine.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Base64;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.glyph.GlyphProgressionService;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.PlayerSlot;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;
import marauroa.common.game.RPSlot;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class GlyphRestoreMigrationTest {
	@Test
	public void knowledgeRestoreKeepsOwnerDeathProtectionAndExperienceTooltip() throws IOException {
		final Item saved = SingletonRepository.getEntityManager().getItem("glif wiedzy", ItemCreationContext.quest());
		saved.setID(new ID(203, "glyph_restore_test"));
		saved.setBoundTo("knowledge-owner");
		final Item restored = new ItemTransformer().transform(serializedCopy(saved));
		assertEquals("knowledge-owner", restored.getBoundTo());
		assertTrue(restored.isUndroppableOnDeath());
		assertEquals("10", restored.getMap(ItemTooltip.ATTRIBUTE).get(ItemTooltip.COMBAT_EXPERIENCE_BONUS));
		assertNull(restored.getRarity());
	}
	@BeforeClass
	public static void setUpClasses() {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void existingGlyphRefreshesDefinitionStatsAndDropsLegacyRarity()
			throws IOException {
		final Item current = SingletonRepository.getEntityManager().getItem(
				"glif kryzysu", ItemCreationContext.restore());
		final String currentDescription = current.getDescription();

		final Item saved = SingletonRepository.getEntityManager().getItem(
				"glif kryzysu", ItemCreationContext.restore());
		saved.setID(new ID(201, "glyph_restore_test"));
		saved.put("critical_chance", 3.0);
		saved.put("description", "stary opis glifu");
		saved.put(Item.RARITY_ID, "rare");
		saved.put(Item.RARITY_PROFILE, "default");
		saved.put(Item.RARITY_MODIFIERS, "critical_chance", "1.10");

		final Item restored = new ItemTransformer().transform(serializedCopy(saved));

		assertEquals(6.0,
				restored.getDouble("critical_chance"), 0.0);
		assertEquals(currentDescription, restored.getDescription());
		assertFalse(restored.has(Item.RARITY_ID));
		assertFalse(restored.hasMap(Item.RARITY_MODIFIERS));
		assertNull(restored.getRarity());
	}

	@Test
	public void strzybogRestorePublishesCurrentRateReductionWithoutRarity()
			throws IOException {
		final Item saved = SingletonRepository.getEntityManager().getItem(
				"glif Strzyboga", ItemCreationContext.restore());
		saved.setID(new ID(202, "glyph_restore_test"));
		saved.put("rate_increase", 7);
		saved.put(Item.RARITY_ID, "rare");
		saved.put(Item.RARITY_PROFILE, "default");

		final Item restored = new ItemTransformer().transform(serializedCopy(saved));

		assertEquals(0.4, restored.getDouble("glyph_attack_rate_reduction"), 0.00001);
		assertEquals("0.4", restored.getMap(ItemTooltip.ATTRIBUTE)
				.get(ItemTooltip.RATE_INCREASE));
		assertFalse(restored.has(Item.RARITY_ID));
		assertNull(restored.getRarity());
	}


    @Test
    public void newGlyphsStartAtOneAndRestoredLevelsKeepIdentityAndDoNotCompound() throws IOException {
        final Item fresh = SingletonRepository.getEntityManager().getItem("glif daru Mokoszy", ItemCreationContext.quest());
        assertEquals(1, fresh.getInt("glyph_level"));
        assertEquals(100, fresh.getInt("health"));
        for (int level = 1; level <= 3; level++) {
            fresh.setID(new ID(204, "glyph_restore_test"));
            fresh.setBoundTo("glyph-owner");
            GlyphProgressionService.applyLevel(fresh, level);
            Item restored = new ItemTransformer().transform(serializedCopy(fresh));
            restored = new ItemTransformer().transform(serializedCopy(restored));
            assertEquals(level, restored.getInt("glyph_level"));
            assertEquals(new int[] {100, 250, 500}[level - 1], restored.getInt("health"));
            assertEquals(fresh.getID(), restored.getID());
            assertEquals("glyph-owner", restored.getBoundTo());
            assertEquals(Integer.toString(level), restored.getMap(ItemTooltip.ATTRIBUTE).get(ItemTooltip.GLYPH_LEVEL));
        }
    }

    @Test
    public void loginChangesOnlyEquippedHealthDeltaAndNeverRepeatsIt() throws Exception {
        final Player player = PlayerTestHelper.createPlayer("glyph-login");
        player.setBaseHP(1500); player.setHP(1500);
        final Item legacy = SingletonRepository.getEntityManager().getItem("glif daru Mokoszy", ItemCreationContext.restore());
        legacy.setID(new ID(205, "glyph_restore_test"));
        legacy.put("health", 500);
        RPSlot raw = serializedSlot(legacy, "healing_rune");
        final Method load = PlayerTransformer.class.getDeclaredMethod("loadSlotContent",
                Player.class, RPSlot.class,
                PlayerSlot.class);
        load.setAccessible(true);
        PlayerSlot target = new PlayerSlot("healing_rune");
        load.invoke(new PlayerTransformer(), player, raw, target);
        assertEquals(1, target.size());
        assertEquals(1100, player.getBaseHP()); assertEquals(1100, player.getHP());
        raw = serializedSlot(target.iterator().next(), "healing_rune");
        target = new PlayerSlot("healing_rune");
        load.invoke(new PlayerTransformer(), player, raw, target);
        assertEquals(1100, player.getBaseHP()); assertEquals(1100, player.getHP());
        raw = serializedSlot(legacy, "bag");
        load.invoke(new PlayerTransformer(), player, raw, new PlayerSlot("bag"));
        assertEquals(1100, player.getBaseHP());
        player.setBaseHP(40000); player.setHP(1000);
        GlyphProgressionService.restoreEquippedHealth(player,
                (Item) target.iterator().next(), legacy, "healing_rune");
        assertEquals(39600, player.getUncappedBaseHP()); assertEquals(1000, player.getHP());
    }

    private RPSlot serializedSlot(RPObject item, String name) throws IOException {
        RPObject owner = new RPObject(); owner.setID(new ID(206, "glyph_restore_test"));
        RPSlot slot = new RPSlot(name);
        owner.addSlot(slot); slot.add(serializedCopy(item)); return slot;
    }

    @Test
    public void binaryGlyphWrittenBeforeProgressionStillLoadsWithLegacyShortField() throws IOException {
        // Captured with the pre-progression Item RPClass, not the current serializer.
        final byte[] bytes = Base64.getDecoder().decode("BAAAAGl0ZW0LAAAA//8FAAAAYm91bmQMbGVnYWN5LW93bmVy//8LAAAAZGVzY3JpcHRpb24SAGxlZ2FjeS1kZXNjcmlwdGlvbv//BAAAAHR5cGUEaXRlbf//DQAAAHJhdGVfaW5jcmVhc2UBAP//AQAAAHgAAP//CAAAAHN1YmNsYXNzDmdseXBoX3N0cnp5Ym9n//8EAAAAbmFtZQ5nbGlmIFN0cnp5Ym9nYf//AQAAAHkAAP//BgAAAHpvbmVpZBJnbHlwaF9yZXN0b3JlX3Rlc3T//wIAAABpZM8AAAD//wUAAABjbGFzcwVnbHlwaAEAAAAAAAAAAAAAAAAAAAAAAAA=");
        RPObject saved = new RPObject();
        saved.readObject(new InputSerializer(new ByteArrayInputStream(bytes)));
        assertEquals(1, saved.getInt("rate_increase"));
        assertEquals("legacy-owner", saved.get("bound"));
        Item restored = new ItemTransformer().transform(saved);
        assertEquals("glif Strzyboga", restored.getName());
        assertEquals("legacy-owner", restored.getBoundTo());
        assertEquals(0.4, restored.getDouble("glyph_attack_rate_reduction"), 0.00001);
        assertFalse(restored.has("rate_increase"));
        assertEquals(1, restored.getInt("glyph_level"));
    }

	private RPObject serializedCopy(final RPObject source) throws IOException {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		source.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final RPObject copy = new RPObject();
		copy.readObject(new InputSerializer(
				new ByteArrayInputStream(bytes.toByteArray())));
		return copy;
	}
}
