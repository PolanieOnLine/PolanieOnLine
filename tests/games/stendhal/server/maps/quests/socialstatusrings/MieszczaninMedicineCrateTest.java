/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.quest.PlayerPrivateQuestProp;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.RPClass.BlockTestHelper;

public class MieszczaninMedicineCrateTest {

	@BeforeClass
	public static void beforeClass() {
		BlockTestHelper.generateRPClasses();
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Test
	public void crateAppearsOnlyAfterPlayerAcceptsMedicineSearch() {
		final Player player = PlayerTestHelper.createPlayer("Alice");
		final StendhalRPZone zone = new StendhalRPZone("road_scene", 128, 128);
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_ROAD);

		MieszczaninRoadScene.ensureWreckProps(zone, player);
		assertEquals(0, countOwnedCrates(zone, player));

		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_MEDICINE);
		MieszczaninRoadScene.ensureWreckProps(zone, player);

		int count = 0;
		for (final Entity entity : zone.getEntitiesOfClass(MieszczaninMedicineCrate.class)) {
			final MieszczaninMedicineCrate crate = (MieszczaninMedicineCrate) entity;
			if (crate.isOwnedBy(player)) {
				count++;
				assertEquals(MieszczaninRoadScene.MEDICINE_CRATE_X, crate.getX());
				assertEquals(MieszczaninRoadScene.MEDICINE_CRATE_Y, crate.getY());
				assertEquals("item/pot/crate_small", crate.get(PlayerPrivateQuestProp.TILESET_ATTRIBUTE));
				assertEquals("1", crate.get(PlayerPrivateQuestProp.TILE_INDEX_ATTRIBUTE));
				assertEquals("2", crate.get(PlayerPrivateQuestProp.TILESET_COLUMNS_ATTRIBUTE));
				assertEquals("Podnieś|Użyj", crate.get("menu"));
			}
		}
		assertEquals(1, count);

		MieszczaninRoadScene.ensureWreckProps(zone, player);
		assertEquals(1, countOwnedCrates(zone, player));
	}

	@Test
	public void ownerRecoversMedicineOnlyDuringMedicineStage() {
		final Player player = PlayerTestHelper.createPlayer("Alice");
		player.setPosition(10, 10);
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_MEDICINE);

		final MieszczaninMedicineCrate crate = new MieszczaninMedicineCrate(player);
		crate.setPosition(11, 10);

		assertTrue(crate.onUsed(player));
		assertTrue(player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_MEDICINE_FOUND));
	}

	@Test
	public void crateDoesNotAdvanceOtherStages() {
		final Player player = PlayerTestHelper.createPlayer("Alice");
		player.setPosition(10, 10);
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_ROAD);

		final MieszczaninMedicineCrate crate = new MieszczaninMedicineCrate(player);
		crate.setPosition(11, 10);

		assertFalse(crate.onUsed(player));
		assertTrue(player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_ROAD));
	}

	@Test
	public void finishedMedicineStageDoesNotRespawnCrate() {
		final Player player = PlayerTestHelper.createPlayer("Alice");
		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_MEDICINE);
		final StendhalRPZone zone = new StendhalRPZone("road_scene_cleanup", 128, 128);
		MieszczaninRoadScene.ensureWreckProps(zone, player);
		assertEquals(1, countOwnedCrates(zone, player));

		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_MEDICINE_FOUND);
		MieszczaninRoadScene.ensureWreckProps(zone, player);

		assertEquals(0, countOwnedCrates(zone, player));
	}

	private int countOwnedCrates(final StendhalRPZone zone, final Player player) {
		int count = 0;
		for (final Entity entity : zone.getEntitiesOfClass(MieszczaninMedicineCrate.class)) {
			if (((MieszczaninMedicineCrate) entity).isOwnedBy(player)) {
				count++;
			}
		}
		return count;
	}
}
