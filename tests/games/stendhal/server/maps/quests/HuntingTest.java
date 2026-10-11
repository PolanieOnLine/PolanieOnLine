package games.stendhal.server.maps.quests;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Test;
import games.stendhal.common.Level;
import games.stendhal.common.QuestMarker;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.HuntingRewardChest;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.quests.hunting.HuntingDifficulty;
import games.stendhal.server.maps.quests.hunting.HuntingRewards;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.SpeakerNPCTestHelper;

public class HuntingTest {
	private Player player;
	private Engine engine;
	private Hunting quest;
	private StendhalRPZone zone;
	@BeforeClass public static void init() throws Exception { QuestHelper.setUpBeforeClass(); }
	@Before public void setup() {
		NPCList.get().clear();
		SpeakerNPC npc = SpeakerNPCTestHelper.createSpeakerNPC("Janisław");
		NPCList.get().add(npc);
		quest = new Hunting();
		quest.addToWorld();
		engine = npc.getEngine();
		engine.setCurrentState(ConversationStates.ATTENDING);
		player = PlayerTestHelper.createPlayer("hunter");
		zone = new StendhalRPZone("hunting_test", 64, 64);
		zone.setPublicAccessible(true);
		MockStendlRPWorld.get().addRPZone(zone);
		zone.add(player);
		Creature rat = SingletonRepository.getEntityManager().getCreature("szczur");
		zone.add(new CreatureRespawnPoint(zone, 2, 2, rat, 1));
		player.setLevel(rat.getLevel());
	}
	@After public void cleanup() { MockStendlRPWorld.get().removeZone(zone); NPCList.get().clear(); }
	private void accept() {
		assertTrue(engine.step(player, "średnie"));
		assertEquals(ConversationStates.QUESTION_1, engine.getCurrentState());
		assertTrue(engine.step(player, "tak"));
		assertTrue(player.hasQuest("hunting"));
	}
	private void kills() {
		String[] fields = player.getQuest("hunting", 0).split(",");
		player.setSoloKillCount(fields[0], Integer.parseInt(fields[2]) + Integer.parseInt(fields[3]));
	}
	@Test public void previewDoesNotStartOrReward() {
		assertTrue(engine.step(player, "zadanie"));
		assertFalse(player.hasQuest("hunting"));
		assertTrue(engine.step(player, "średnie"));
		assertFalse(player.hasQuest("hunting"));
		assertFalse(player.isEquipped(HuntingRewardChest.NAME));
		assertTrue(engine.step(player, "nie"));
		assertFalse(player.hasQuest("hunting_offer"));
	}
	@Test public void acceptedHuntRecordsBaselineAndDifficulty() {
		player.setSoloKillCount("szczur", 100);
		accept();
		String[] parts = player.getQuest("hunting", 0).split(",");
		assertEquals("100", parts[3]);
		assertTrue(Integer.parseInt(parts[2]) >= 20 && Integer.parseInt(parts[2]) <= 30);
		assertEquals("MEDIUM", player.getQuest("hunting", 3));
		assertEquals(0, player.getNumberOfRepetitions("hunting", 2));
		assertTrue(engine.step(player, "załatwione"));
		assertFalse(player.isEquipped(HuntingRewardChest.NAME));
	}
	@Test public void difficultyDoesNotResetActiveProgress() {
		accept();
		String state = player.getQuest("hunting");
		assertTrue(engine.step(player, "trudne"));
		assertEquals(state, player.getQuest("hunting"));
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
	}
	@Test public void cancellationRequiresConfirmationAndPreservesRepetitions() {
		player.setQuest("hunting", "done;0;9");
		accept();
		String state = player.getQuest("hunting");
		assertTrue(engine.step(player, "rezygnuję"));
		assertEquals(state, player.getQuest("hunting"));
		assertTrue(engine.step(player, "nie"));
		assertEquals(state, player.getQuest("hunting"));
		assertTrue(engine.step(player, "rezygnuję"));
		assertTrue(engine.step(player, "tak"));
		assertEquals("cancelled", player.getQuest("hunting", 0));
		assertEquals(9, player.getNumberOfRepetitions("hunting", 2));
		assertFalse(player.isEquipped(HuntingRewardChest.NAME));
		assertTrue(engine.step(player, "załatwione"));
		assertEquals(9, player.getNumberOfRepetitions("hunting", 2));
		assertFalse(quest.getHistory(player).isEmpty());
		accept();
		assertEquals(9, player.getNumberOfRepetitions("hunting", 2));
	}
	@Test public void unavailableOfferCannotBeConfirmed() {
		assertTrue(engine.step(player, "średnie"));
		zone.getRespawnPointList().clear();
		assertTrue(engine.step(player, "tak"));
		assertFalse(player.hasQuest("hunting"));
	}
	@Test public void changedLevelInvalidatesPreview() {
		assertTrue(engine.step(player, "średnie"));
		player.setLevel(player.getLevel() + 1);
		assertTrue(engine.step(player, "tak"));
		assertFalse(player.hasQuest("hunting"));
	}
	@Test public void unavailableHardHuntDoesNotFallBackToWeakCreature() {
		assertTrue(engine.step(player, "trudne"));
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertFalse(player.hasQuest("hunting_offer"));
		assertFalse(player.hasQuest("hunting"));
	}
	@Test public void privateRespawnsAndUnspawnedDefinitionsAreNotTargets() {
		assertTrue(Hunting.availableCreatures().stream().anyMatch(c -> "szczur".equals(c.getName())));
		zone.setPublicAccessible(false);
		assertFalse(Hunting.availableCreatures().stream().anyMatch(c -> "szczur".equals(c.getName())));
	}
	@Test public void difficultyRangesAreDistinctAndDoNotFallbackToTrivialHardHunts() {
		assertTrue(HuntingDifficulty.EASY.accepts(30, 25));
		assertTrue(HuntingDifficulty.MEDIUM.accepts(30, 30));
		assertTrue(HuntingDifficulty.HARD.accepts(30, 35));
		assertFalse(HuntingDifficulty.HARD.accepts(30, 25));
		assertFalse(HuntingDifficulty.EASY.accepts(30, 35));
		assertTrue(HuntingDifficulty.EASY.accepts(0, 1));
	}
	@Test public void completedHuntRewardsOnceAndStoresLevelBeforeOrdinaryXP() {
		accept();
		player.setAtkXP(Level.getXP(20) + 5);
		player.setDefXP(Level.getXP(5) + 5);
		int atk = player.getAtkXP(), def = player.getDefXP(), level = player.getLevel();
		kills();
		assertTrue(engine.step(player, "załatwione"));
		assertEquals(atk + HuntingRewards.skillXPBonus(atk, HuntingDifficulty.MEDIUM), player.getAtkXP());
		assertEquals(def + HuntingRewards.skillXPBonus(def, HuntingDifficulty.MEDIUM), player.getDefXP());
		assertEquals(1, player.getNumberOfRepetitions("hunting", 2));
		Item chest = player.getFirstEquipped(HuntingRewardChest.NAME);
		assertNotNull(chest);
		assertEquals("hunter", chest.getBoundTo());
		assertEquals("MEDIUM;" + level, chest.getItemData());
		assertTrue(engine.step(player, "załatwione"));
		assertEquals(1, player.getNumberOfRepetitions("hunting", 2));
		assertEquals(1, chest.getQuantity());
		assertEquals(atk + HuntingRewards.skillXPBonus(atk, HuntingDifficulty.MEDIUM), player.getAtkXP());
	}
	@Test public void legacyHuntKeepsProgressAndTenthHuntRing() {
		player.setQuest("hunting", "szczur,0,2,0,0;0;9");
		assertEquals(HuntingDifficulty.MEDIUM, HuntingDifficulty.fromQuest(player.getQuest("hunting")));
		assertFalse(quest.getFormattedHistory(player).isEmpty());
		player.setSoloKillCount("szczur", 2);
		assertTrue(engine.step(player, "załatwione"));
		assertEquals(10, player.getNumberOfRepetitions("hunting", 2));
		assertTrue(player.isEquipped("pierścień powrotu"));
		assertTrue(player.isEquipped(HuntingRewardChest.NAME));
	}
	@Test public void markerReflectsOfferProgressReadyAndCancelled() {
		SpeakerNPC npc = NPCList.get().get("Janisław");
		assertEquals(QuestMarker.AVAILABLE, quest.getNPCQuestMarker(player, npc));
		accept();
		assertEquals(QuestMarker.IN_PROGRESS, quest.getNPCQuestMarker(player, npc));
		kills();
		assertEquals(QuestMarker.READY, quest.getNPCQuestMarker(player, npc));
		engine.step(player, "rezygnuję"); engine.step(player, "tak");
		assertEquals(QuestMarker.REPEATABLE, quest.getNPCQuestMarker(player, npc));
	}
	@Test public void bossRespawnsAreExcluded() {
		Creature rat = SingletonRepository.getEntityManager().getCreature("szczur");
		rat.setAIProfiles(java.util.Collections.singletonMap("boss", ""));
		zone.getRespawnPointList().clear();
		zone.add(new CreatureRespawnPoint(zone, 3, 3, rat, 1));
		assertTrue(Hunting.availableCreatures().isEmpty());
	}
	@Test public void allDifficultiesAwardTheirRespectiveSkillBonus() {
		for (HuntingDifficulty difficulty : HuntingDifficulty.values()) {
			player.setQuest("hunting", "szczur,0,2,0,0;0;0;" + difficulty.name());
			player.setSoloKillCount("szczur", 2);
			player.setAtkXP(Level.getXP(25)); player.setDefXP(Level.getXP(10));
			int atk = player.getAtkXP(), def = player.getDefXP();
			assertTrue(engine.step(player, "załatwione"));
			assertEquals(atk + HuntingRewards.skillXPBonus(atk, difficulty), player.getAtkXP());
			assertEquals(def + HuntingRewards.skillXPBonus(def, difficulty), player.getDefXP());
		}
	}
}
