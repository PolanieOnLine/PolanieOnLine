package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.entity.item.Item;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

public class HuntingAffixDefinitionTest {
	@BeforeClass
	public static void initialize() {
		MockStendlRPWorld.get();
	}

	@Test
	public void allFiveAffixesAreRegisteredInTheirOwnRarityPools() {
		assertNotNull(ItemAffixRegistry.getInstance().get("steady_hand"));
		assertNotNull(ItemAffixRegistry.getInstance().get("poison_potency"));
		assertNotNull(LegendaryItemAffixRegistry.getInstance().get("legendary_defense_circle"));
		assertNotNull(LegendaryItemAffixRegistry.getInstance().get("legendary_hunters_pace"));
		assertNotNull(LegendaryItemAffixRegistry.getInstance().get("legendary_last_stand"));
	}

	@Test
	public void steadyHandRaisesOnlyTheMinimumAndNeverChangesTheMaximum() {
		final Item weapon = weapon(100, 160);
		weapon.put(ItemAffixState.ATTRIBUTE, "steady_hand", "0.10");
		assertEquals(110, weapon.getDamageMin());
		assertEquals(160, weapon.getDamageMax());
		final Item narrow = weapon(100, 105);
		narrow.put(ItemAffixState.ATTRIBUTE, "steady_hand", "0.10");
		assertEquals(105, narrow.getDamageMin());
		assertEquals(105, narrow.getDamageMax());
	}

	@Test
	public void mapOnlyAffixesSurviveFullSerializationAndRestoreWithoutNewAttributes() throws Exception {
		final Item original = weapon(100, 160);
		assertTrue(AdditionalCombatAffixDefinition.STEADY_HAND.apply(original, new Random(2)));
		final String rolled = original.getMap(ItemAffixState.ATTRIBUTE).get("steady_hand");
		assertFalse(original.has("steady_hand"));
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		original.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final RPObject saved = (RPObject) new InputSerializer(new ByteArrayInputStream(bytes.toByteArray()))
				.readObject(new RPObject());
		final Item restored = weapon(100, 160);
		ItemAffixState.restore(restored, saved);
		assertEquals(rolled, restored.getMap(ItemAffixState.ATTRIBUTE).get("steady_hand"));
		assertFalse(restored.has("steady_hand"));
		assertEquals(original.getDamageMin(), restored.getDamageMin());
	}

	@Test
	public void equipmentEligibilityMatchesTheProposedSlotsAndDoesNotRepeatAnAffix() {
		assertTrue(AdditionalCombatAffixDefinition.POISON_POTENCY.isEligible(item("glove")));
		assertFalse(AdditionalCombatAffixDefinition.POISON_POTENCY.isEligible(item("boots")));
		assertTrue(AdditionalCombatAffixDefinition.DEFENSE_CIRCLE.isEligible(item("armor")));
		assertTrue(AdditionalCombatAffixDefinition.DEFENSE_CIRCLE.isEligible(item("shield")));
		assertFalse(AdditionalCombatAffixDefinition.DEFENSE_CIRCLE.isEligible(item("ring")));
		final Item boots = item("boots");
		assertTrue(AdditionalCombatAffixDefinition.HUNTERS_PACE.apply(boots, new Random(1)));
		assertFalse(AdditionalCombatAffixDefinition.HUNTERS_PACE.isEligible(boots));
		assertFalse(AdditionalCombatAffixDefinition.HUNTERS_PACE.isEligible(item("cloak")));
		assertTrue(AdditionalCombatAffixDefinition.LAST_STAND.isEligible(item("belt")));
		assertTrue(AdditionalCombatAffixDefinition.LAST_STAND.isEligible(item("gloves")));
		assertFalse(AdditionalCombatAffixDefinition.LAST_STAND.isEligible(item("helmet")));
	}

	private Item item(final String type) {
		return new Item("affix test", type, "test", null);
	}

	private Item weapon(final int minimum, final int maximum) {
		final Map<String, String> attributes = new HashMap<>();
		attributes.put("damage_min", Integer.toString(minimum));
		attributes.put("damage_max", Integer.toString(maximum));
		return new Item("range test", "sword", "test", attributes);
	}
}
