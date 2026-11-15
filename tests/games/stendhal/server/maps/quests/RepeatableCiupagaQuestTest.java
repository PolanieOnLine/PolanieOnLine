package games.stendhal.server.maps.quests;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.Level;
import games.stendhal.common.QuestMarker;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.achievement.Achievement;
import games.stendhal.server.core.rp.achievement.factory.CiupagaAchievementFactory;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.player.QuestMarkerService;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.SpeakerNPCTestHelper;

@RunWith(Parameterized.class)
public class RepeatableCiupagaQuestTest {
	@Parameterized.Parameters(name = "{0} wasy")
	public static Collection<Object[]> stages() { return Arrays.asList(new Object[][] {{2}, {3}}); }
	private final int stage;
	private Player player;
	private AbstractQuest quest;
	private String slot;
	private SpeakerNPC giver, smith;
	private String sourceName, targetName;
	private int hours, xp, karma;

	public RepeatableCiupagaQuestTest(final int stage) { this.stage = stage; }
	@BeforeClass public static void initialize() throws Exception { QuestHelper.setUpBeforeClass(); }
	@Before public void setup() {
		StendhalRPZone zone = new StendhalRPZone("repeat_ciupaga", 30, 30);
		for (String name : new String[] {"Władca Smoków", "Krasnolud", "Hadrin"}) {
			SpeakerNPC npc = SpeakerNPCTestHelper.createSpeakerNPC(name);
			npc.addGreeting("Witaj."); npc.addGoodbye(); zone.add(npc);
		}
		quest = stage == 2 ? new ZlotaCiupagaDwaWasy() {
			@Override public boolean removeFromWorld() { return true; }
		} : new ZlotaCiupagaTrzyWasy() {
			@Override public boolean removeFromWorld() { return true; }
		};
		SingletonRepository.getStendhalQuestSystem().loadQuest(quest);
		slot = quest.getSlotName();
		giver = SingletonRepository.getNPCList().get(stage == 2 ? "Władca Smoków" : "Hadrin");
		smith = SingletonRepository.getNPCList().get(stage == 2 ? "Krasnolud" : "Hadrin");
		sourceName = stage == 2 ? "złota ciupaga z wąsem" : "złota ciupaga z dwoma wąsami";
		targetName = stage == 2 ? "złota ciupaga z dwoma wąsami" : "złota ciupaga z trzema wąsami";
		hours = stage == 2 ? 12 : 24; xp = stage == 2 ? 500000 : 1000000; karma = stage == 2 ? 100 : 200;
		player = PlayerTestHelper.createPlayer("repeat_ciupaga_player");
		player.setXP(Level.getXP(500)); player.setLevel(500); player.addKarma(5000); zone.add(player);
		for (String prerequisite : new String[] {"krasnolud", "goralski_kolekcjoner3", "belts_collector", "gloves_collector", "krolewski_plaszcz"}) {
			player.setQuest(prerequisite, "done");
		}
		player.setSoloKill("serafin"); player.setSoloKill("azazel");
	}
	@After public void cleanup() {
		SingletonRepository.getStendhalQuestSystem().unloadQuest(quest);
		for (String name : new String[] {"Władca Smoków", "Krasnolud", "Hadrin"}) {
			SingletonRepository.getNPCList().remove(name);
		}
		PlayerTestHelper.removeAllPlayers();
	}
	private String say(final SpeakerNPC npc, final String text) {
		npc.getEngine().setCurrentState(ConversationStates.ATTENDING);
		npc.getEngine().step(player, text);
		return getReply(npc);
	}
	private void supply(final String name, final int count) {
		Item item = SingletonRepository.getEntityManager().getItem(name);
		if (item instanceof StackableItem) { ((StackableItem) item).setQuantity(count); }
		player.getSlot("bag").add(item);
	}
	private Item supplyAll(final int cycle) {
		Item source = SingletonRepository.getEntityManager().getItem(sourceName, ItemCreationContext
				.builder(ItemCreationContext.Source.QUEST).withRarity(ItemRarity.LEGENDARY).withAffixSeed(70L + cycle).build());
		source.setBoundTo(player.getName()); player.getSlot("bag").add(source);
		supply("money", stage == 2 ? 1200000 : 2500000);
		supply("sztabka złota", stage == 2 ? 150 : 240);
		supply("polano", stage == 2 ? 10 : 70);
		if (stage == 2) {
			supply("pazur zielonego smoka", 1); supply("pazur czerwonego smoka", 1); supply("pióro serafina", 2);
		} else {
			for (String color : new String[] {"arktycznego", "niebieskiego", "czarnego", "złotego"}) {
				supply("pazur " + color + " smoka", 1);
			}
			supply("pióro azazela", 3); supply("magiczna krew", 10); supply("smocza krew", 5);
			supply("cudowna krew", 1); supply("wywar wąsatych smoków", 1);
		}
		return source;
	}
	@Test public void legacyCompletionCanBeRepeatedTwiceWithFullRewardsAndInheritedAffixes() {
		player.setQuest(slot, "done");
		assertTrue(quest.isRepeatable(player));
		for (int cycle = 1; cycle <= 2; cycle++) {
			int beforeXP = player.getXP(); double beforeKarma = player.getKarma();
			assertNotNull(say(giver, "task"));
			assertEquals(ConversationStates.QUEST_OFFERED, giver.getEngine().getCurrentState());
			giver.getEngine().step(player, "tak");
			assertEquals("start", player.getQuest(slot, 0));
			assertEquals(cycle, quest.getCompletedCount(player));
			Item source = supplyAll(cycle);
			Map<String, String> affixes = ItemAffixState.getValues(source);
			say(smith, "przedmioty");
			assertEquals("forging", player.getQuest(slot, 0));
			assertEquals(0, player.getNumberOfEquipped(sourceName));
			say(smith, "nagroda");
			assertEquals("forging", player.getQuest(slot, 0));
			assertEquals(beforeXP, player.getXP());
			player.setQuest(slot, 1, "0");
			say(smith, "nagroda");
			assertTrue(player.isQuestCompleted(slot));
			assertEquals(cycle + 1, quest.getCompletedCount(player));
			assertEquals(beforeXP + xp, player.getXP());
			assertEquals(beforeKarma + 10 + karma, player.getKarma(), 0.0001);
			assertEquals(cycle, player.getNumberOfEquipped(targetName));
			Item result = player.getAllEquipped(targetName).get(cycle - 1);
			assertEquals(ItemRarity.LEGENDARY, result.getRarity());
			assertEquals(affixes, ItemAffixState.getValues(result));
			assertEquals(player.getName(), result.getBoundTo());
			say(smith, "nagroda");
			assertEquals(beforeXP + xp, player.getXP());
			assertEquals(cycle, player.getNumberOfEquipped(targetName));
		}
	}
	@Test public void askingAgainDuringForgingCannotResetTheOrderOrGrantAcceptanceKarma() {
		player.setQuest(slot, "forging;123456;1;snapshot");
		double before = player.getKarma();
		say(giver, "task");
		giver.getEngine().step(player, "tak");
		assertEquals("forging;123456;1;snapshot", player.getQuest(slot));
		assertEquals(before, player.getKarma(), 0.0001);
		say(giver, "task");
		giver.getEngine().step(player, "nie");
		assertEquals("forging;123456;1;snapshot", player.getQuest(slot));
	}
	@Test public void acceptanceStillChecksRequirementsIfTheyChangeDuringTheOffer() {
		player.setQuest(slot, "done");
		say(giver, "task");
		player.setLevel(1);
		double before = player.getKarma();
		giver.getEngine().step(player, "tak");
		assertEquals("done", player.getQuest(slot));
		assertEquals(before, player.getKarma(), 0.0001);
	}
	@Test public void oldTimestampedCompletionsAndDecliningARepeatKeepCompletionCredit() {
		player.setQuest(slot, "done;123456");
		say(giver, "task");
		assertEquals(ConversationStates.QUEST_OFFERED, giver.getEngine().getCurrentState());
		giver.getEngine().step(player, "nie");
		assertTrue(player.isQuestCompleted(slot));
		assertEquals(1, quest.getCompletedCount(player));
	}
	@Test public void journalWaitsForHoursRatherThanMinutes() {
		player.setQuest(slot, "forging;" + (System.currentTimeMillis() - 60L * 60 * 1000));
		String history = String.join(" ", quest.getHistory(player));
		assertTrue(history, history.contains(stage == 2 ? "dwanaście" : "dwadzieścia cztery"));
		player.setQuest(slot, 1, Long.toString(System.currentTimeMillis() - hours * 60L * 60 * 1000 - 1000));
		assertTrue(String.join(" ", quest.getHistory(player)).contains("już ucichła"));
	}
	@Test public void repeatedThirdStageStillQualifiesForEarnedCiupagaAchievements() {
		player.setQuest("ciupaga_trzy_wasy", "start;;1");
		for (Achievement achievement : new CiupagaAchievementFactory().createAchievements()) {
			assertTrue(achievement.isFulfilled(player));
		}
	}
	@Test public void completedQuestOffersARepeatMarkerWithoutStartingIt() {
		player.setQuest(slot, "done");
		assertEquals(QuestMarker.REPEATABLE, QuestMarkerService.getMarker(player, giver));
		assertEquals("done", player.getQuest(slot));
		player.setLevel(1);
		assertEquals(QuestMarker.NONE, QuestMarkerService.getMarker(player, giver));
	}
}
