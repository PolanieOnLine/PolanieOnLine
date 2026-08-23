/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.quest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.Definition;
import marauroa.common.game.RPClass;
import utilities.PlayerTestHelper;
import utilities.RPClass.BlockTestHelper;

public class PlayerPrivateQuestPropTest {

	@BeforeClass
	public static void beforeClass() {
		BlockTestHelper.generateRPClasses();
		PlayerTestHelper.generatePlayerRPClasses();
		PlayerTestHelper.generateCreatureRPClasses();
		PlayerTestHelper.generateNPCRPClasses();
	}

	@Test
	public void blockRpClassDeclaresPrivatePerceptionAttributes() {
		final Set<String> names = new HashSet<String>();
		for (final Definition definition : RPClass.getRPClass("block").getDefinitions()) {
			names.add(definition.getName());
		}

		assertTrue(names.contains(PlayerPrivateQuestProp.PERCEPTION_KEY_ATTRIBUTE));
		assertTrue(names.contains(PlayerPrivateQuestProp.PERCEPTION_VALUE_ATTRIBUTE));
	}

	@Test
	public void storesOwnerAndTilesetSelection() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final PlayerPrivateQuestProp prop = new PlayerPrivateQuestProp(
				owner, "item/pot/barrels_1", 5, 4, true);

		assertEquals("name", prop.get(PlayerPrivateQuestProp.PERCEPTION_KEY_ATTRIBUTE));
		assertEquals("Alice", prop.getOwnerName());
		assertEquals("item/pot/barrels_1", prop.get(PlayerPrivateQuestProp.TILESET_ATTRIBUTE));
		assertEquals(5, prop.getInt(PlayerPrivateQuestProp.TILE_INDEX_ATTRIBUTE));
		assertEquals(4, prop.getInt(PlayerPrivateQuestProp.TILESET_COLUMNS_ATTRIBUTE));
		assertEquals(100, prop.getResistance());
		assertEquals("Oto drewniana beczka.", prop.getDescription());
	}

	@Test
	public void solidPropBlocksOnlyItsOwnerByDefault() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final Player stranger = PlayerTestHelper.createPlayer("Bob");
		final PlayerPrivateQuestProp prop = new PlayerPrivateQuestProp(
				owner, "object/hay_cart", 0, 1, true);

		assertTrue(prop.isObstacle(owner));
		assertFalse(prop.isObstacle(stranger));
	}

	@Test
	public void privateInstanceSolidPropBlocksAllRPEntities() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final Player stranger = PlayerTestHelper.createPlayer("Bob");
		final Creature creature = new Creature();
		final SpeakerNPC npc = new SpeakerNPC("Radomir");
		final PlayerPrivateQuestProp prop = new PlayerPrivateQuestProp(
				owner, "item/pot/crate_small", 0, 1, true, true);

		assertTrue(prop.isObstacle(owner));
		assertTrue(prop.isObstacle(stranger));
		assertTrue(prop.isObstacle(creature));
		assertTrue(prop.isObstacle(npc));
	}

	@Test
	public void zoneCollisionIgnoresPrivatePropForOtherPlayer() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final Player stranger = PlayerTestHelper.createPlayer("Bob");
		final PlayerPrivateQuestProp prop = new PlayerPrivateQuestProp(
				owner, "object/hay_cart", 0, 1, true);
		final StendhalRPZone zone = new StendhalRPZone("test_private_prop_collision", 10, 10);

		owner.setPosition(1, 1);
		stranger.setPosition(2, 1);
		prop.setPosition(4, 4);
		zone.add(owner);
		zone.add(stranger);
		zone.add(prop);

		assertTrue(zone.collidesObjects(owner, owner.getArea(4, 4)));
		assertFalse(zone.collidesObjects(stranger, stranger.getArea(4, 4)));
	}

	@Test
	public void zoneCollisionInPrivateInstanceBlocksOtherActors() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final Player stranger = PlayerTestHelper.createPlayer("Bob");
		final PlayerPrivateQuestProp prop = new PlayerPrivateQuestProp(
				owner, "item/pot/crate_small", 0, 1, true, true);
		final StendhalRPZone zone = new StendhalRPZone("test_private_instance_prop_collision", 10, 10);

		owner.setPosition(1, 1);
		stranger.setPosition(2, 1);
		prop.setPosition(4, 4);
		zone.add(owner);
		zone.add(stranger);
		zone.add(prop);

		assertTrue(zone.collidesObjects(owner, owner.getArea(4, 4)));
		assertTrue(zone.collidesObjects(stranger, stranger.getArea(4, 4)));
	}

	@Test
	public void decorativePropDoesNotBlockOwner() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final PlayerPrivateQuestProp prop = new PlayerPrivateQuestProp(
				owner, "item/bazaar_produce", 2, 2, false, true);

		assertFalse(prop.isObstacle(owner));
		assertFalse(prop.isObstacle(new Creature()));
		assertEquals(0, prop.getResistance());
	}

	@Test
	public void privatePropAlwaysHasPlayerFacingFallbackDescription() {
		final Player owner = PlayerTestHelper.createPlayer("Alice");
		final PlayerPrivateQuestProp prop = new PlayerPrivateQuestProp(
				owner, "quest/unknown_visual", 0, 1, false);

		assertTrue(prop.hasDescription());
		assertEquals("Oto przedmiot należący do tej sceny zadania.", prop.getDescription());
		assertFalse(prop.describe().contains("prywatnej sceny zadania"));
	}
}
