package games.stendhal.server.maps.quests.trail;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalQuestSystem;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.quests.IQuest;
import games.stendhal.server.maps.semos.bank.CustomerAdvisorNPC;
import games.stendhal.server.maps.semos.library.LibrarianNPC;
import games.stendhal.server.maps.semos.tavern.BarMaidNPC;
import games.stendhal.server.maps.semos.townhall.LeaderNPC;
import marauroa.common.game.RPEvent;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class SemosTrailTest {
	private static final String[] NAMES = { "Lieutenant Drilenun", "Dagobert",
			"Ceryl", "Margaret", "Tragarz Olek" };
	private final StendhalQuestSystem system = StendhalQuestSystem.get();
	private final Map<String, SpeakerNPC> oldNpcs = new HashMap<>();
	private final List<StendhalRPZone> zones = new ArrayList<>();
	private List<IQuest> loaded, oldQuests;
	private Player player;
	private StendhalRPZone bank, library, city, townhall, tavern;

	@BeforeClass
	public static void init() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		Field field = StendhalQuestSystem.class.getDeclaredField("quests");
		field.setAccessible(true);
		loaded = (List<IQuest>) field.get(null);
		oldQuests = new ArrayList<>(loaded);
		loaded.clear();
		for (String name : NAMES)
			oldNpcs.put(name, NPCList.get().remove(name));
		city = zone("0_semos_city");
		bank = zone("int_semos_bank");
		library = zone("int_semos_library");
		townhall = zone("int_semos_townhall");
		tavern = zone("int_semos_tavern_0");
		new LeaderNPC().configureZone(townhall, null);
		new CustomerAdvisorNPC().configureZone(bank, null);
		new LibrarianNPC().configureZone(library, null);
		new BarMaidNPC().configureZone(tavern, null);
		new SemosTrailSites().configureZone(city, null);
		system.loadQuest(new LetterFromZakopaneQuest());
		system.loadQuest(new ThreeCirclesQuest());
		system.loadQuest(new FalsePickupQuest());
		player = PlayerTestHelper.createPlayer("semos-investigator");
		city.add(player);
		MockStendhalRPRuleProcessor.get().addPlayer(player);
	}

	private StendhalRPZone zone(String name) {
		StendhalRPZone zone = new StendhalRPZone(name, 128, 128);
		zones.add(zone);
		MockStendlRPWorld.get().addRPZone(zone);
		return zone;
	}

	@After
	public void tearDown() {
		loaded.clear();
		loaded.addAll(oldQuests);
		MockStendhalRPRuleProcessor.get().clearPlayers();
		for (String name : NAMES) {
			NPCList.get().remove(name);
			if (oldNpcs.get(name) != null)
				NPCList.get().add(oldNpcs.get(name));
		}
		for (StendhalRPZone zone : zones)
			MockStendlRPWorld.get().removeZone(zone);
	}

	private String say(String name, String text) {
		SpeakerNPC npc = NPCList.get().get(name);
		assertTrue(name + " should understand " + text,
				npc.getEngine().step(player, text));
		return getReply(npc);
	}

	private void visit(String name) {
		NPCList.get().get(name).getEngine().setCurrentState(
				ConversationStates.IDLE);
		say(name, "hi");
	}

	private void accept(String name, String topic) {
		visit(name);
		say(name, topic);
		say(name, "tak");
	}

	private boolean use(TrailEvidence.Kind kind, StendhalRPZone zone) {
		TrailEvidence prop = new TrailEvidence(kind);
		PlayerPrivateScene.assign(prop, player);
		prop.setPosition(10, 10);
		zone.add(prop);
		move(zone);
		player.setPosition(9, 10);
		boolean used = prop.onUsed(player);
		zone.remove(prop);
		return used;
	}

	private void move(StendhalRPZone zone) {
		if (player.getZone() != zone) {
			player.getZone().remove(player);
			zone.add(player);
		}
	}

	private void letter() {
		player.setQuest("trail_debt", "done");
		accept("Lieutenant Drilenun", "list");
		visit("Dagobert");
		say("Dagobert", "kwit");
		assertTrue(use(TrailEvidence.Kind.SEMOS_PAYMENT, bank));
		visit("Lieutenant Drilenun");
		say("Lieutenant Drilenun", "list");
		assertTrue(player.isQuestCompleted("trail_semos_letter"));
	}

	private void sign() {
		letter();
		accept("Dagobert", "trzy kółka");
		visit("Ceryl");
		say("Ceryl", "trzy kółka");
		assertTrue(use(TrailEvidence.Kind.SEMOS_ARCHIVE, library));
		say("Ceryl", "suchy ładunek");
		assertTrue(player.isQuestCompleted("trail_semos_sign"));
	}

	private StackableItem wood(int quantity) {
		StackableItem item = (StackableItem) SingletonRepository.getEntityManager().getItem(
				"polano");
		item.setQuantity(quantity);
		return item;
	}

	@Test
	public void chapterIsHiddenUntilZakopaneIsFinishedAndStepsCannotBeSkipped() {
		assertTrue(system.getMainQuests(player).isEmpty());
		visit("Lieutenant Drilenun");
		say("Lieutenant Drilenun", "list");
		assertFalse(player.hasQuest("trail_semos_letter"));
		player.setQuest("trail_debt", "done");
		assertEquals(1, system.getMainQuests(player).size());
		visit("Dagobert");
		say("Dagobert", "trzy kółka");
		assertFalse(player.hasQuest("trail_semos_sign"));
		accept("Lieutenant Drilenun", "list");
		assertEquals("bank", player.getQuest("trail_semos_letter"));
		assertFalse(use(TrailEvidence.Kind.SEMOS_ARCHIVE, library));
		assertFalse(use(TrailEvidence.Kind.SEMOS_CRATE, city));
	}

	@Test
	public void phraseMustBeReadBeforeItCompletesTheLibraryTask() {
		letter();
		accept("Dagobert", "trzy kółka");
		visit("Ceryl");
		say("Ceryl", "suchy ładunek");
		assertFalse(player.isQuestCompleted("trail_semos_sign"));
		say("Ceryl", "trzy kółka");
		assertEquals("shelf", player.getQuest("trail_semos_sign"));
		say("Ceryl", "suchy ładunek");
		assertFalse(player.isQuestCompleted("trail_semos_sign"));
		use(TrailEvidence.Kind.SEMOS_ARCHIVE, library);
		say("Ceryl", "suchy ładunek");
		assertTrue(player.isQuestCompleted("trail_semos_sign"));
	}

	@Test
	public void decoyUsesOnlyCarriedWoodAndCannotChargeTwice() {
		sign();
		accept("Margaret", "odbiór");
		player.getSlot("bank").add(wood(2));
		say("Margaret", "polana");
		assertEquals("wood", player.getQuest("trail_semos_pickup"));
		StackableItem carried = wood(3);
		player.getSlot("bag").add(carried);
		say("Margaret", "polano");
		assertEquals("crate", player.getQuest("trail_semos_pickup"));
		assertEquals(1, player.getNumberOfSubmittableEquipped("polano"));
		say("Margaret", "przygotuj skrzynię");
		assertEquals(1, player.getNumberOfSubmittableEquipped("polano"));
	}

	@Test
	public void fullPickupStoryRequiresTheCrateAndPhraseThenRewardsOnce() {
		sign();
		accept("Margaret", "odbiór");
		visit("Tragarz Olek");
		say("Tragarz Olek", "suchy ładunek");
		assertEquals("wood", player.getQuest("trail_semos_pickup"));
		player.getSlot("bag").add(wood(2));
		visit("Margaret");
		say("Margaret", "przygotuj skrzynię");
		assertTrue(use(TrailEvidence.Kind.SEMOS_CRATE, city));
		visit("Tragarz Olek");
		say("Tragarz Olek", "mokry ładunek");
		assertEquals("contact", player.getQuest("trail_semos_pickup"));
		say("Tragarz Olek", "suchy ładunek");
		assertEquals("question", player.getQuest("trail_semos_pickup"));
		player.clearEvents();
		say("Tragarz Olek", "kwit");
		assertReceiptShown();
		assertEquals("return", player.getQuest("trail_semos_pickup"));
		player.clearEvents();
		assertTrue(say("Tragarz Olek", "kwit").contains("Radan"));
		assertReceiptShown();
		assertEquals("return", player.getQuest("trail_semos_pickup"));
		visit("Lieutenant Drilenun");
		say("Lieutenant Drilenun", "tragarz");
		assertTrue(player.isQuestCompleted("trail_semos_pickup"));
		int xp = player.getXP(), coins = player.getTotalNumberOf("money");
		say("Lieutenant Drilenun", "tragarz");
		visit("Margaret");
		say("Margaret", "odbiór");
		assertEquals(xp, player.getXP());
		assertEquals(coins, player.getTotalNumberOf("money"));
		assertTrue(system.getMainQuests(player).isEmpty());
	}

	private void assertReceiptShown() {
		for (RPEvent event : player.events()) {
			if ("examine".equals(event.getName())) {
				assertEquals("/data/sprites/examine/kwit-radana.png", event.get("path"));
				assertEquals("Kwit odbioru", event.get("title"));
				return;
			}
		}
		fail("Olek should show his receipt in the existing image viewer");
	}

	@Test
	public void journalUsesSemosRegionAndPersistsWhatWasRead() {
		player.setQuest("trail_debt", "done");
		accept("Lieutenant Drilenun", "list");
		String title = system.getMainQuests(player).get(0);
		IQuest quest = system.getQuest(player, title);
		assertEquals("Ślad trzech kółek",
				quest.getQuestInfo(player).getMainQuestChapter());
		assertEquals(Region.SEMOS_CITY, quest.getRegion());
		use(TrailEvidence.Kind.SEMOS_PAYMENT, bank);
		Player restored = new Player(player);
		assertEquals("checked", restored.getQuest("trail_semos_letter"));
		assertTrue(
				system.getQuestProgressDetails(restored, title).get(0).contains(
						"Drilenun"));
	}
}
