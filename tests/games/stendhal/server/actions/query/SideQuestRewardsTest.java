package games.stendhal.server.actions.query;

import static org.junit.Assert.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.*;
import games.stendhal.common.QuestRewardPreview;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;
import games.stendhal.server.maps.quests.QuestRewards;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class SideQuestRewardsTest {
	private Player player;
	private Fixture side, main, unknown;
	@BeforeClass public static void init() throws Exception { QuestHelper.setUpBeforeClass(); }
	@Before public void setup() {
		player = PlayerTestHelper.createPlayer("side-reward-reader");
		side = load("preview_side", false, true);
		main = load("preview_main", true, true);
		unknown = load("preview_unknown", false, false);
	}
	private Fixture load(String slot, boolean isMain, boolean hasRewards) {
		Fixture q = new Fixture(slot, isMain, hasRewards);
		SingletonRepository.getStendhalQuestSystem().loadQuest(q);
		player.setQuest(slot, "start"); return q;
	}
	@After public void cleanup() {
		for (Fixture q : Arrays.asList(side, main, unknown)) SingletonRepository.getStendhalQuestSystem().unloadQuest(q);
	}
	private RPEvent details(String category, String title) {
		RPAction request = new RPAction(); request.put("progress_type", category); request.put("item", title);
		new ProgressStatusQueryAction().onAction(player, request);
		RPEvent found = null;
		for (RPEvent event : player.events()) if (event.has("item") && title.equals(event.get("item"))) found = event;
		assertNotNull(found); return found;
	}
	@Test public void sideAndCompletedQuestsUseTheExistingRewardProtocol() {
		QuestRewardPreview rewards = QuestRewardPreview.read(details("Zadania poboczne", side.getName()));
		assertNotNull("Side quest preview must not be limited to main quests", rewards);
		assertEquals(50, rewards.xp); assertEquals(20, rewards.coins); assertFalse(rewards.completed);
		player.setQuest(side.getSlotName(), "done");
		rewards = QuestRewardPreview.read(details("Ukończone zadania", side.getName()));
		assertNotNull(rewards); assertTrue(rewards.completed);
		assertNotNull(QuestRewardPreview.read(details("Zadania główne", main.getName())));
	}
	@Test public void missingMetadataNeverInventsRewardsAndReadingNeverPaysThemOut() {
		int xp = player.getXP(), money = player.getNumberOfEquipped("money");
		assertNull(QuestRewardPreview.read(details("Zadania poboczne", unknown.getName())));
		for (int i = 0; i < 3; i++) details("Zadania poboczne", side.getName());
		assertEquals(xp, player.getXP()); assertEquals(money, player.getNumberOfEquipped("money"));
		assertEquals("start", player.getQuest(side.getSlotName()));
	}
	@Test public void variableRewardsRemainAnOverviewAndOldServersDefaultToFixedRewards() {
		QuestRewardPreview preview = new QuestRewards(200, 0).variable().note("Losowo od 1 do 4 grillowanych steków.").build();
		RPEvent event = new games.stendhal.server.events.ProgressStatusEvent("Zadania poboczne", Collections.emptyList());
		preview.write(event, true);
		assertTrue(QuestRewardPreview.read(event).variable);
		assertEquals(0, QuestRewardPreview.read(event).items.size());
		event.remove("reward_variable");
		assertFalse(QuestRewardPreview.read(event).variable);
	}
	private static class Fixture extends AbstractQuest {
		private final String slot;
		Fixture(String slot, boolean isMain, boolean hasRewards) {
			this.slot = slot; fillQuestInfo(slot, "Opis zadania.", false);
			if (isMain) questInfo.setMainQuest("Rozdział", 1, "Rozpocznij.");
			if (hasRewards) questInfo.setRewards(new QuestRewards(50, 20).build());
		}
		@Override public String getSlotName() { return slot; }
		@Override public String getName() { return slot; }
		@Override public void addToWorld() { }
		@Override public boolean removeFromWorld() { return true; }
		@Override public List<String> getHistory(Player p) { return Collections.singletonList("Przyjęto zadanie."); }
	}
}
