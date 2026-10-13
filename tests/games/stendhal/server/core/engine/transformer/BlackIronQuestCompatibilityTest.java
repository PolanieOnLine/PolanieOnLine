/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.engine.transformer;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.mapstuff.useable.sources.SourceShadow;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.player.UpdateConverter;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.gdansk.museum.MuseumDirectorNPC;
import games.stendhal.server.maps.quests.DailyItemGdanskQuest;
import games.stendhal.server.maps.quests.Gornictwo;
import games.stendhal.server.maps.tatry.kuznice.blacksmith.ZygfrydNPC;
import utilities.NPCTestHelper;
import utilities.QuestHelper;

/** Checks real quest dialogue and production after loading former shadow materials. */
public class BlackIronQuestCompatibilityTest extends QuestHelper {

	private StendhalRPZone zone;
	private Player player;

	@Before
	public void prepare() {
		SingletonRepository.getNPCList().clear();
		SingletonRepository.getProducerRegister().getProducers().clear();
		zone = new StendhalRPZone("black_iron_compatibility");
		MockStendlRPWorld.get().addRPZone(zone);
		player = createPlayer("black-iron-quest-test");
		zone.add(player);
	}

	@After
	public void cleanup() throws Exception {
		SingletonRepository.getNPCList().clear();
		SingletonRepository.getProducerRegister().getProducers().clear();
		MockStendlRPWorld.get().removeRPZone(zone.getID());
	}

	private void equipOldMaterial(final String currentName, final String oldName, final int quantity) {
		final StackableItem item = (StackableItem) SingletonRepository.getEntityManager().getItem(currentName);
		item.put("name", oldName);
		item.setQuantity(quantity);
		player.getSlot("bag").add(item);
		new PlayerTransformer().loadItemsIntoSlots(player);
		UpdateConverter.updateQuests(player);
	}

	@Test
	public void smithDeliversAnOrderPlacedBeforeTheRename() {
		new ZygfrydNPC().configureZone(zone, null);
		NPCTestHelper.loadProductions("Zygfryd");
		final SpeakerNPC smith = SingletonRepository.getNPCList().get("Zygfryd");
		player.setPosition(7, 5);
		player.setQuest("zygfryd_cast_shadow", "4;sztabka cieni;1700000000001");
		UpdateConverter.updateQuests(player);

		assertTrue(smith.getEngine().step(player, "hi"));
		assertEquals("done", player.getQuest("zygfryd_cast_shadow"));
		assertTrue(player.isEquipped("sztabka czarnego żelaza", 4));
		assertEquals(4, player.getQuantityOfProducedItems("sztabka czarnego żelaza"));
		assertTrue(getReply(smith).contains("czarnego żelaza"));
	}

	@Test
	public void newSmithOrdersUseRestoredOreAndKeepTheSameRecipe() {
		new ZygfrydNPC().configureZone(zone, null);
		NPCTestHelper.loadProductions("Zygfryd");
		final SpeakerNPC smith = SingletonRepository.getNPCList().get("Zygfryd");
		player.setPosition(7, 5);
		equipOldMaterial("ruda czarnego żelaza", "ruda cieni", 2);
		assertTrue(equipWithStackableItem(player, "polano", 4));
		assertTrue(equipWithMoney(player, 140));

		assertTrue(smith.getEngine().step(player, "hi"));
		assertTrue(smith.getEngine().step(player, "odlej 2 sztabka czarnego żelaza"));
		assertTrue(smith.getEngine().step(player, "tak"));
		assertEquals("2", player.getQuest("zygfryd_cast_shadow", 0));
		assertEquals("sztabka czarnego żelaza", player.getQuest("zygfryd_cast_shadow", 1));
		assertFalse(player.isEquipped("ruda czarnego żelaza"));
		assertFalse(player.isEquipped("polano"));
		assertFalse(player.isEquipped("money"));
	}

	@Test
	public void oldMiningStageAcceptsRestoredOreAndKeepsTheNextStage() {
		final SpeakerNPC miner = new SpeakerNPC("Górnik");
		miner.setGender("M");
		miner.setPosition(5, 5);
		zone.add(miner);
		new Gornictwo().addToWorld();
		player.setPosition(5, 6);
		player.setQuest("gornictwo", "dig_shadow");
		equipOldMaterial("ruda czarnego żelaza", "ruda cieni", 12);

		assertTrue(miner.getEngine().step(player, "hi"));
		assertEquals("dig_silver", player.getQuest("gornictwo"));
		assertFalse(player.isEquipped("ruda czarnego żelaza"));
		assertTrue(getReply(miner).contains("czarne żelazo"));
	}

	@Test
	public void museumAcceptsBothFormerMaterialsWithoutResettingCompletedDeliveries() {
		new MuseumDirectorNPC().configureZone(zone, null);
		new DailyItemGdanskQuest().addToWorld();
		final SpeakerNPC director = SingletonRepository.getNPCList().get("Mieczysław");
		player.setPosition(24, 4);
		final String[] oldNames = { "ruda cieni", "sztabka cieni" };
		final String[] newNames = { "ruda czarnego żelaza", "sztabka czarnego żelaza" };
		for (int material = 0; material < oldNames.length; material++) {
			player.setQuest("daily_museum_gdansk_quest", oldNames[material] + "=8;1700000000000;3");
			equipOldMaterial(newNames[material], oldNames[material], 9);
			assertTrue(director.getEngine().step(player, "hi"));
			assertTrue(director.getEngine().step(player, "załatwione"));
			assertEquals("done", player.getQuest("daily_museum_gdansk_quest", 0));
			assertEquals("4", player.getQuest("daily_museum_gdansk_quest", 2));
			assertTrue(player.isEquipped(newNames[material], 1));
			assertFalse(player.isEquipped(newNames[material], 2));
			assertTrue(director.getEngine().step(player, "bye"));
		}
	}

	@Test
	public void mineProducesTheNewOreAndRecordsItsNewName() {
		final TestOreSource source = new TestOreSource();
		source.setPosition(3, 3);
		zone.add(source);
		source.finish(player);
		assertTrue(player.isEquipped("ruda czarnego żelaza", 1));
		assertEquals(1, player.getQuantityOfMinedItems("ruda czarnego żelaza"));
	}

	private static class TestOreSource extends SourceShadow {
		void finish(final Player miner) {
			super.onFinished(miner, true);
		}
	}
}
