/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.events;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import marauroa.common.game.DetailLevel;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;

public class ItemUpgradeEventCompatibilityTest {
	private static final String TEST_OWNER =
			"item_upgrade_compatibility_test_owner";

	@BeforeClass
	public static void setUpClass() {
		PlayerTestHelper.generatePlayerRPClasses();
		ItemUpgradeEventCompatibility.generateRPClasses();
		if (!RPClass.hasRPClass(TEST_OWNER)) {
			final RPClass owner = new RPClass(TEST_OWNER);
			owner.addRPEvent(ItemUpgradeEventCompatibility.LEGACY_EVENT,
					Definition.PRIVATE);
			owner.addRPEvent(ItemUpgradeEventCompatibility.CURRENT_EVENT,
					Definition.PRIVATE);
		}
	}

	@Test
	public void testRestoresCurrentUpgradeEventFromCharacterSave()
			throws IOException {
		assertRoundTrip(ItemUpgradeEventCompatibility.CURRENT_EVENT,
				"material_classes");
	}

	@Test
	public void testRestoresLegacyUpgradeEventFromCharacterSave()
			throws IOException {
		assertRoundTrip(ItemUpgradeEventCompatibility.LEGACY_EVENT,
				"candidate_names");
	}

	@Test
	public void testPersistedAttributeCodesRemainCompatible() {
		final RPClass legacy = RPClass.getRPClass(
				ItemUpgradeEventCompatibility.LEGACY_EVENT);
		assertEquals(11, legacy.getCode(DefinitionClass.ATTRIBUTE,
				"candidate_names"));
		assertEquals(29, legacy.getCode(DefinitionClass.ATTRIBUTE,
				"material_values"));
		assertEquals(30, legacy.getCode(DefinitionClass.ATTRIBUTE,
				"owned_material_values"));

		final RPClass current = RPClass.getRPClass(
				ItemUpgradeEventCompatibility.CURRENT_EVENT);
		assertEquals(11, current.getCode(DefinitionClass.ATTRIBUTE,
				"selected_path"));
		assertEquals(28, current.getCode(DefinitionClass.ATTRIBUTE,
				"material_classes"));
		assertEquals(29, current.getCode(DefinitionClass.ATTRIBUTE,
				"material_subclasses"));
		assertEquals(30, current.getCode(DefinitionClass.ATTRIBUTE,
				"material_values"));
		assertEquals(31, current.getCode(DefinitionClass.ATTRIBUTE,
				"owned_material_values"));
	}

	private void assertRoundTrip(final String eventName,
			final String compatibilityAttribute) throws IOException {
		final RPObject storedOwner = new RPObject();
		storedOwner.setRPClass(TEST_OWNER);
		final RPEvent event = new RPEvent(eventName);
		event.put("phase", "preview");
		event.put(compatibilityAttribute, "test");
		storedOwner.addEvent(event);

		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		storedOwner.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final RPObject restoredOwner = new RPObject();
		restoredOwner.readObject(new InputSerializer(
				new ByteArrayInputStream(bytes.toByteArray())));
		final RPEvent restoredEvent = restoredOwner.events().get(0);

		assertEquals(eventName, restoredEvent.getName());
		assertEquals("test", restoredEvent.get(compatibilityAttribute));
	}
}
