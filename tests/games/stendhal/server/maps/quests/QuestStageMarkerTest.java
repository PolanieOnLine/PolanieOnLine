package games.stendhal.server.maps.quests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.QuestMarker;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.player.QuestMarkerService;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.SpeakerNPCTestHelper;

public class QuestStageMarkerTest {

	private Player player;
	private StendhalRPZone zone;
	private final List<SpeakerNPC> actors = new ArrayList<>();
	private AbstractQuest quest;

	@BeforeClass
	public static void initialize() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setup() {
		player = PlayerTestHelper.createPlayer("stage_marker_player");
		zone = new StendhalRPZone("stage_marker_zone", 30, 30);
		zone.add(player);
	}

	@After
	public void cleanup() {
		if (quest != null) {
			SingletonRepository.getStendhalQuestSystem().unloadQuest(quest);
		}
		for (SpeakerNPC npc : actors) {
			SingletonRepository.getNPCList().remove(npc.getName());
		}
		PlayerTestHelper.removeAllPlayers();
	}

	private SpeakerNPC actor(final String name) {
		SpeakerNPC npc = SpeakerNPCTestHelper.createSpeakerNPC(name);
		zone.add(npc);
		actors.add(npc);
		return npc;
	}

	private void load(final AbstractQuest value) {
		quest = value;
		SingletonRepository.getStendhalQuestSystem().loadQuest(quest);
		assertTrue(SingletonRepository.getStendhalQuestSystem().isLoaded(quest));
	}

	private SpeakerNPC ouchit() {
		SpeakerNPC npc = actor("Ouchit");
		actor("Karl");
		load(new BowsForOuchit() {
			@Override public boolean removeFromWorld() { return true; }
		});
		return npc;
	}

	@Test
	public void ouchitOffersHisQuestBeforeTheExtraIntroductoryQuestion() {
		SpeakerNPC npc = ouchit();
		assertEquals(QuestMarker.AVAILABLE, QuestMarkerService.getMarker(player, npc));
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, actors.get(1)));
		assertFalse(player.hasQuest(BowsForOuchit.QUEST_SLOT));
	}

	@Test
	public void woodAndHorseHairEachMakeTheCurrentStepReadyWithoutTakingItems() {
		SpeakerNPC npc = ouchit();
		SpeakerNPC karl = actors.get(1);
		player.setQuest(BowsForOuchit.QUEST_SLOT, "wood");
		assertEquals(QuestMarker.IN_PROGRESS, QuestMarkerService.getMarker(player, npc));
		PlayerTestHelper.equipWithStackableItem(player, "polano", 9);
		assertEquals(QuestMarker.IN_PROGRESS, QuestMarkerService.getMarker(player, npc));
		PlayerTestHelper.equipWithStackableItem(player, "polano", 1);
		assertEquals(QuestMarker.READY, QuestMarkerService.getMarker(player, npc));
		assertEquals(10, player.getNumberOfEquipped("polano"));
		assertEquals("wood", player.getQuest(BowsForOuchit.QUEST_SLOT));
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, karl));

		npc.getEngine().setCurrentState(ConversationStates.ATTENDING);
		npc.getEngine().step(player, "polano");
		assertEquals("hair", player.getQuest(BowsForOuchit.QUEST_SLOT));
		assertEquals(0, player.getNumberOfEquipped("polano"));
		assertEquals(QuestMarker.IN_PROGRESS, QuestMarkerService.getMarker(player, npc));
		assertEquals(QuestMarker.READY, QuestMarkerService.getMarker(player, karl));
		assertFalse(player.isEquipped("końskie włosie"));

		karl.getEngine().setCurrentState(ConversationStates.ATTENDING);
		karl.getEngine().step(player, "Ouchit");
		assertEquals(QuestMarker.READY, QuestMarkerService.getMarker(player, npc));
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, karl));
		npc.getEngine().step(player, "końskie włosie");
		assertEquals("done", player.getQuest(BowsForOuchit.QUEST_SLOT));
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, npc));
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, karl));
	}

	@Test
	public void medicineMarksFlaskAndHerbHandInsAtTheCorrectNpc() {
		SpeakerNPC tad = actor("Tad");
		SpeakerNPC ilisa = actor("Ilisa");
		SpeakerNPC ketteh = actor("Ketteh Wehoh");
		load(new MedicineForTad() {
			@Override public boolean removeFromWorld() { return true; }
		});
		String slot = quest.getSlotName();
		assertEquals(QuestMarker.AVAILABLE, QuestMarkerService.getMarker(player, tad));
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, ilisa));
		player.setQuest(slot, "start");
		assertEquals(QuestMarker.IN_PROGRESS, QuestMarkerService.getMarker(player, tad));
		PlayerTestHelper.equipWithItem(player, "butelka");
		assertEquals(QuestMarker.READY, QuestMarkerService.getMarker(player, tad));
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, ilisa));
		player.setQuest(slot, "ilisa");
		assertEquals(QuestMarker.READY, QuestMarkerService.getMarker(player, ilisa));
		player.setQuest(slot, "corpse&herbs");
		assertEquals(QuestMarker.IN_PROGRESS, QuestMarkerService.getMarker(player, ilisa));
		PlayerTestHelper.equipWithItem(player, "arandula");
		assertEquals(QuestMarker.READY, QuestMarkerService.getMarker(player, ilisa));
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, ketteh));
		assertEquals("corpse&herbs", player.getQuest(slot));
		assertTrue(player.isEquipped("arandula"));
		player.setQuest(slot, "eliksir");
		assertEquals(QuestMarker.READY, QuestMarkerService.getMarker(player, tad));
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, ilisa));
		player.setQuest(slot, "done");
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, tad));
	}
}
