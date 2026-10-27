package games.stendhal.server.maps.quests;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Test;
import games.stendhal.common.Level;
import games.stendhal.common.QuestMarker;
import games.stendhal.common.Translation;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.transformer.PlayerTransformer;
import marauroa.common.game.RPObject;
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
import games.stendhal.server.maps.zakopane.home.JanislawNPC;
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
		assertTrue(HuntingDifficulty.EASY.accepts(30, 18));
		assertTrue(HuntingDifficulty.EASY.accepts(30, 25));
		assertFalse(HuntingDifficulty.EASY.accepts(30, 26));
		assertTrue(HuntingDifficulty.MEDIUM.accepts(30, 26));
		assertTrue(HuntingDifficulty.MEDIUM.accepts(30, 30));
		assertFalse(HuntingDifficulty.MEDIUM.accepts(30, 32));
		assertTrue(HuntingDifficulty.HARD.accepts(30, 32));
		assertTrue(HuntingDifficulty.HARD.accepts(30, 35));
		assertFalse(HuntingDifficulty.HARD.accepts(30, 25));
		assertTrue(HuntingDifficulty.EASY.accepts(0, 1));
	}
	@Test public void completedHuntRewardsOnceAndStoresLevelBeforeOrdinaryXP() {
		accept();
		player.setAtkXP(Level.getXP(20) + 5);
		player.setDefXP(Level.getXP(5) + 5);
		int atk = player.getAtkXP(), def = player.getDefXP(), level = player.getLevel();
		int bonus = HuntingRewards.questBasisPoints(player.getQuest("hunting"));
		kills();
		assertTrue(engine.step(player, "załatwione"));
		assertEquals(atk + HuntingRewards.skillXPBonus(atk, bonus), player.getAtkXP());
		assertEquals(def + HuntingRewards.skillXPBonus(def, bonus), player.getDefXP());
		assertEquals(1, player.getNumberOfRepetitions("hunting", 2));
		Item chest = player.getFirstEquipped(HuntingRewardChest.NAME);
		assertNotNull(chest);
		assertEquals("hunter", chest.getBoundTo());
		assertEquals("MEDIUM;" + level, chest.getItemData());
		assertTrue(engine.step(player, "załatwione"));
		assertEquals(1, player.getNumberOfRepetitions("hunting", 2));
		assertEquals(1, chest.getQuantity());
		assertEquals(atk + HuntingRewards.skillXPBonus(atk, bonus), player.getAtkXP());
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
			int bonus = HuntingRewards.questBasisPoints(player.getQuest("hunting"));
			player.setSoloKillCount("szczur", 2);
			player.setAtkXP(Level.getXP(25)); player.setDefXP(Level.getXP(10));
			int atk = player.getAtkXP(), def = player.getDefXP();
			assertTrue(engine.step(player, "załatwione"));
			assertEquals(atk + HuntingRewards.skillXPBonus(atk, bonus), player.getAtkXP());
			assertEquals(def + HuntingRewards.skillXPBonus(def, bonus), player.getDefXP());
		}
	}
	@Test public void acceptedHuntKeepsPromisedBonusAfterLevellingAndCreatureChanges() {
		accept();
		String savedBonus = player.getQuest("hunting", 4);
		player.setLevel(player.getLevel() + 10);
		zone.getRespawnPointList().clear();
		kills();
		engine.step(player, "załatwione");
		assertEquals(savedBonus, player.getQuest("hunting", 4));
	}
	@Test public void invalidOfferRewardCannotBeAccepted() {
		engine.step(player, "średnie");
		String[] offer = player.getQuest("hunting_offer").split(";");
		player.setQuest("hunting_offer", offer[0] + ";" + offer[1] + ";" + offer[2] + ";" + offer[3] + ";999999");
		engine.step(player, "tak");
		assertFalse(player.hasQuest("hunting"));
	}
	private String cachedMedium(long age) {
		String offer = "MEDIUM;szczur;25;" + player.getLevel() + ";1307";
		player.setQuest("hunting_offer_MEDIUM", offer + ";" + (System.currentTimeMillis() - age));
		return offer;
	}
	@Test public void exactOfferRewardsArePrivateAndVisibleBeforeAcceptanceInBothLanguages() {
		player.setClientVersion("1.0");
		for (String language : new String[] {"pl", "en"}) {
			player.setLanguage(language);
			engine.setCurrentState(ConversationStates.ATTENDING);
			String expectedOffer = cachedMedium(0);
			player.clearEvents();
			assertTrue(engine.step(player, "medium"));
			String summary = PlayerTestHelper.getPrivateReply(player);
			assertNotNull("Exact rewards must be visible before accepting", summary);
			assertTrue(summary, summary.contains("13,07%"));
			assertTrue(summary, summary.contains("25%"));
			assertTrue(summary, summary.contains("25"));
			assertTrue(summary, summary.contains("pl".equals(language) ? "szczur" : "rat"));
			assertTrue(summary, summary.contains("pl".equals(language) ? "PD pełnego poziomu ataku i obrony" : "full attack and defense level's XP"));
			assertTrue(summary, summary.contains("pl".equals(language) ? "Skrzynię Myśliwego" : "Hunter's Chest"));
			assertFalse(player.hasQuest("hunting"));
			assertEquals(expectedOffer, player.getQuest("hunting_offer"));
			String speech = Translation.getText(language, SpeakerNPCTestHelper.getReply(NPCList.get().get("Janisław")));
			assertFalse("NPC speech must not recite reward percentages", speech.contains("%"));
			assertTrue(speech, speech.contains("pl".equals(language) ? "szczur" : "rat"));
			assertTrue(speech, speech.contains("25"));
			assertTrue(speech, speech.contains("pl".equals(language) ? "#tak" : "#yes"));
			assertTrue(speech, speech.contains("pl".equals(language) ? "#nie" : "#no"));
			assertTrue(engine.step(player, "no"));
		}
	}
	@Test public void currentHunterDialogueDoesNotFallBackToPolishInEnglish() {
		NPCList.get().clear();
		new JanislawNPC().configureZone(zone, null);
		SpeakerNPC hunter = NPCList.get().get("Janisław");
		quest = new Hunting();
		quest.addToWorld();
		engine = hunter.getEngine();
		for (String command : new String[] {"hi", "job", "help", "done", "cancel", "quest", "hard",
				"medium", "no", "medium", "yes", "quest", "easy", "done", "cancel", "no", "cancel", "yes",
				"done", "medium", "yes"}) {
			assertTrue(command, engine.step(player, command));
			String source = SpeakerNPCTestHelper.getReply(hunter);
			assertNotNull(command, source);
			assertNotEquals(command + " must have an English reply", source, Translation.getText("en", source));
		}
		kills();
		for (String command : new String[] {"done", "done", "bye"}) {
			assertTrue(command, engine.step(player, command));
			String source = SpeakerNPCTestHelper.getReply(hunter);
			assertNotNull(command, source);
			assertNotEquals(command + " must have an English reply", source, Translation.getText("en", source));
		}
		assertEquals("done", player.getQuest("hunting", 0));
	}
	@Test public void multiwordTargetRemainsRecognisableInTranslatedOffersAndReminders() {
		Creature target = SingletonRepository.getEntityManager().getCreature("szczur jaskiniowy");
		zone.add(new CreatureRespawnPoint(zone, 3, 3, target, 1));
		player.setLevel(target.getLevel());
		player.setQuest("hunting_offer_MEDIUM", "MEDIUM;szczur jaskiniowy;25;" + player.getLevel()
				+ ";1307;" + System.currentTimeMillis());
		for (String command : new String[] {"medium", "yes", "quest", "done"}) {
			assertTrue(command, engine.step(player, command));
			String source = SpeakerNPCTestHelper.getReply(NPCList.get().get("Janisław"));
			assertTrue(source, source.contains("szczur jaskiniowy"));
			String english = Translation.getText("en", source);
			assertTrue(english, english.contains("caverat"));
			assertFalse(english, english.contains("szczur"));
		}
	}
	@Test public void decliningPersistsTheDrawWithoutStartingAHunt() {
		engine.step(player, "średnie");
		String offer = player.getQuest("hunting_offer");
		assertNotNull(player.getQuest("hunting_offer_MEDIUM"));
		engine.step(player, "nie");
		assertFalse(player.hasQuest("hunting"));
		assertFalse(player.hasQuest("hunting_offer"));
		engine.step(player, "średnie");
		assertEquals(offer, player.getQuest("hunting_offer"));
	}
	@Test public void cachedOfferSurvivesSavedCharacterReloadAndDifficultySwitch() {
		String expected = cachedMedium(0);
		player = (Player) new PlayerTransformer().transform(new RPObject(player));
		engine.step(player, "trudne");
		engine.step(player, "średnie");
		assertEquals(expected, player.getQuest("hunting_offer"));
	}
	@Test public void cancellingAndAcceptingAgainDoesNotRerollTheOffer() {
		String expected = cachedMedium(0);
		accept();
		engine.step(player, "rezygnuję"); engine.step(player, "tak");
		engine.step(player, "średnie");
		assertEquals(expected, player.getQuest("hunting_offer"));
	}
	@Test public void sevenDayOfferExpiresButAnUnexpiredOfferDoesNot() {
		String expected = cachedMedium(7L * 24 * 60 * 60 * 1000 - 60000);
		engine.step(player, "średnie");
		assertEquals(expected, player.getQuest("hunting_offer"));
		engine.step(player, "nie");
		cachedMedium(7L * 24 * 60 * 60 * 1000 + 60000);
		engine.step(player, "średnie");
		assertNotEquals(expected, player.getQuest("hunting_offer"));
		assertNotEquals("1307", player.getQuest("hunting_offer").split(";")[4]);
	}
	@Test public void completionReleasesAllCachedOffersImmediately() {
		cachedMedium(0);
		player.setQuest("hunting_offer_EASY", "EASY;szczur;25;0;500;" + System.currentTimeMillis());
		accept(); kills(); engine.step(player, "załatwione");
		assertEquals("done", player.getQuest("hunting", 0));
		assertFalse(player.hasQuest("hunting_offer_MEDIUM"));
		assertFalse(player.hasQuest("hunting_offer_EASY"));
		engine.step(player, "średnie");
		assertNotEquals("1307", player.getQuest("hunting_offer").split(";")[4]);
	}
	@Test public void levellingDoesNotRerollAnOfferWhoseTargetStillFits() {
		cachedMedium(0);
		player.setLevel(player.getLevel() + 1);
		engine.step(player, "średnie");
		assertEquals("MEDIUM;szczur;25;" + player.getLevel() + ";1307", player.getQuest("hunting_offer"));
		engine.step(player, "tak");
		assertEquals("1307", player.getQuest("hunting", 4));
	}
	@Test public void cancellingAPrePatchActiveHuntAlsoPreservesItsOffer() {
		player.setQuest("hunting", "szczur,0,25,0,0;" + System.currentTimeMillis() + ";4;MEDIUM;1307");
		engine.step(player, "rezygnuję"); engine.step(player, "tak");
		engine.step(player, "średnie");
		assertEquals("MEDIUM;szczur;25;" + player.getLevel() + ";1307", player.getQuest("hunting_offer"));
		assertEquals(4, player.getNumberOfRepetitions("hunting", 2));
	}
	@Test public void decliningAPrePatchPreviewDoesNotDiscardItsDraw() {
		String expected = "MEDIUM;szczur;25;" + player.getLevel() + ";1307";
		player.setQuest("hunting_offer", expected);
		engine.setCurrentState(ConversationStates.QUESTION_1);
		engine.step(player, "nie");
		engine.step(player, "średnie");
		assertEquals(expected, player.getQuest("hunting_offer"));
	}
	@Test public void switchingDifficultyPreservesThePrePatchPreviewForItsOwnDifficulty() {
		String expected = "MEDIUM;szczur;25;" + player.getLevel() + ";1307";
		player.setQuest("hunting_offer", expected);
		engine.step(player, "trudne");
		engine.step(player, "średnie");
		assertEquals(expected, player.getQuest("hunting_offer"));
	}
	@Test public void cancellingAnOldThirtyFiveKillTaskKeepsItsCountButCapsTheNewOffer() {
		player.setQuest("hunting", "szczur,0,35,0,0;" + System.currentTimeMillis() + ";4");
		engine.step(player, "rezygnuję"); engine.step(player, "tak");
		engine.step(player, "średnie");
		assertEquals("MEDIUM;szczur;35;" + player.getLevel() + ";1500", player.getQuest("hunting_offer"));
		engine.step(player, "tak");
		assertEquals("35", player.getQuest("hunting", 0).split(",")[2]);
		assertEquals("1500", player.getQuest("hunting", 4));
		assertEquals(4, player.getNumberOfRepetitions("hunting", 2));
	}
	@Test public void oldUnacceptedCachedRewardIsCappedWithoutResettingTheDrawOrTimer() {
		long created = System.currentTimeMillis() - 60000;
		player.setQuest("hunting_offer_MEDIUM", "MEDIUM;szczur;25;" + player.getLevel() + ";8000;" + created);
		engine.step(player, "średnie");
		assertEquals("MEDIUM;szczur;25;" + player.getLevel() + ";1500", player.getQuest("hunting_offer"));
		assertEquals(Long.toString(created), player.getQuest("hunting_offer_MEDIUM").split(";")[5]);
		engine.step(player, "tak");
		assertEquals("1500", player.getQuest("hunting", 4));
	}
	@Test public void directlyAcceptingAnOldPreviewRequiresConfirmationOfTheNewReward() {
		player.setQuest("hunting_offer", "MEDIUM;szczur;25;" + player.getLevel() + ";8000");
		engine.setCurrentState(ConversationStates.QUESTION_1);
		engine.step(player, "tak");
		assertFalse(player.hasQuest("hunting"));
		assertEquals(ConversationStates.QUESTION_1, engine.getCurrentState());
		assertEquals("1500", player.getQuest("hunting_offer").split(";")[4]);
		engine.step(player, "tak");
		assertEquals("1500", player.getQuest("hunting", 4));
	}
	@Test public void alreadyAcceptedEightyPercentHuntKeepsItsPromisedReward() {
		player.setQuest("hunting", "szczur,0,25,0,0;0;3;MEDIUM;8000");
		player.setAtkXP(Level.getXP(20)); player.setDefXP(Level.getXP(20));
		int before = player.getAtkXP();
		int expected = (int) (((long) Level.getXP(21) - Level.getXP(20)) * 80 / 100);
		kills(); engine.step(player, "załatwione");
		assertEquals(before + expected, player.getAtkXP());
		assertEquals(before + expected, player.getDefXP());
		assertEquals("8000", player.getQuest("hunting", 4));
		assertEquals(4, player.getNumberOfRepetitions("hunting", 2));
	}
	@Test public void cancellingAnAcceptedHighRewardCannotCarryTheOldRewardIntoANewTask() {
		player.setQuest("hunting", "szczur,0,25,0,0;" + System.currentTimeMillis() + ";3;MEDIUM;8000");
		engine.step(player, "rezygnuję"); engine.step(player, "tak");
		engine.step(player, "średnie");
		assertEquals("1500", player.getQuest("hunting_offer").split(";")[4]);
		engine.step(player, "tak");
		assertEquals("1500", player.getQuest("hunting", 4));
	}
}
