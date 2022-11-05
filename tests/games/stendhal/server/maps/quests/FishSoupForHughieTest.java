package games.stendhal.server.maps.quests;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.ados.farmhouse.FarmersWifeNPC;
import games.stendhal.server.maps.ados.farmhouse.MotherNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class FishSoupForHughieTest {

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	private String questSlot;
	private AbstractQuest quest;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		final StendhalRPZone zone = new StendhalRPZone("int_ados_farm_house_1");
		MockStendlRPWorld.get().addRPZone(zone);

		new MotherNPC().configureZone(zone, null);
		new FarmersWifeNPC().configureZone(zone, null);

		quest = new FishSoupForHughie();
		quest.addToWorld();

		questSlot = quest.getSlotName();

		player = PlayerTestHelper.createPlayer("bob");
	}

	@After
	public void tearDown() {
		PlayerTestHelper.removeNPC("Anastasia");
		PlayerTestHelper.removeNPC("Philomena");
	}

	@Test
	public void testGetSlotName() {
		assertEquals(questSlot,"fishsoup_for_hughie");
	}

	@Test
	public void testMeetPhilomenaToGetHint() {

		npc = SingletonRepository.getNPCList().get("Philomena");
		en = npc.getEngine();

		en.step(player, "hi");
		assertEquals("Dzień dobry!", getReply(npc));
		en.step(player, "offer");
		assertEquals("Sprzedaję osełka masła oraz mleko.", getReply(npc));
		en.step(player, "help");
		assertEquals("Mogę sprzedać Ci butelkę mleka albo trochę masła, prosto od naszych kochanych krów! Jeśli chcesz, oczywiście.", getReply(npc));
		en.step(player, "task");
		assertEquals("Gdybyś potrafił rozwiązywać Junit testy, moja córka potrzebowałaby Cię. Zapytaj Diogenesa, jak możesz pomóc jej w projekcie.", getReply(npc));
		en.step(player, "job");
		assertEquals("Mój mąż prowadzi to gospodarstwo, a ja głównie opiekuję się jego młodszą siostrą i jej chłopakiem, są na górze. Jeżeli możesz powiedzieć coś pomocnego na ich temat, to mów. Słyszałam wcześniej jej płacz...", getReply(npc));
		en.step(player, "bye");
		assertEquals("Żegnaj.", getReply(npc));

	}

	@Test
	public void testQuest() {

		npc = SingletonRepository.getNPCList().get("Anastasia");
		en = npc.getEngine();

		assertNull(player.getQuest(questSlot));

		en.step(player, "hi");
		assertEquals("Cześć, naprawdę przydałaby mi się #przysługa, proszę.", getReply(npc));
		en.step(player, "favor");
		assertEquals("Mój biedny chłopak jest chory, a lekarstwa, które mu podaję, nie działają! Proszę, przyniesiesz dla niego zupę rybną?", getReply(npc));
		en.step(player, "no");
		assertEquals("Nie, proszę! On jest naprawdę bardzo chory.", getReply(npc));

		assertEquals(player.getQuest(questSlot), "rejected");

		en.step(player, "task");
		assertEquals("Mój biedny chłopak jest chory, a lekarstwa, które mu podaję, nie działają! Proszę, przyniesiesz dla niego zupę rybną?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dziękuję! Możesz poprosić Florence Boullabaisse, aby ci ugotowała zupę rybną. Myślę, że znajdziesz ją na targu w Ados.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		assertEquals(player.getQuest(questSlot), "start");

		en.step(player, "hi");
		assertEquals("Już wróciłeś? Hugie jest coraz bardziej chory! Nie zapomnij o zupie rybnej dla niego, bardzo Cię proszę! Obiecuję, że sowicie Cię wynagrodzę!", getReply(npc));
		en.step(player, "task");
		assertEquals("Obiecałeś mi już, że przyniesiesz zupę rybną dla Hughiego! Pospiesz się, proszę!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		PlayerTestHelper.equipWithItem(player, "zupa rybna");

		en.step(player, "hi");
		assertEquals("Hej, widzę, że masz zupę rybną, czy ona jest dla Hugiego?", getReply(npc));
		en.step(player, "no");
		assertEquals("Och... ale mój biedny chłopczyk...", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		// get values to test after reward
		final int xp = player.getXP();
		final double karma  = player.getKarma();

		en.step(player, "hi");
		assertEquals("Hej, widzę, że masz zupę rybną, czy ona jest dla Hugiego?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dziękuję! Jestem Ci niesamowicie wdzięczna za Twoją przysługę. Nakarmię Hughiego, gdy się obudzi. Proszę, weź te mikstury - Hughiemu nie pomagają i tak.", getReply(npc));
		// [17:37] kymara earns 200 experience points.
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

        // test reward
		assertEquals(xp + 200, player.getXP());
		assertThat(player.getKarma(), greaterThan(karma));
		assertTrue(player.isEquipped("eliksir", 10));
		assertFalse(player.isEquipped("zupa rybna"));
		assertTrue(quest.isCompleted(player));

		en.step(player, "hi");
		assertEquals("Witaj ponownie.", getReply(npc));
		en.step(player, "task");
		assertEquals("Teraz Hugie śpi - ma gorączkę. Mam nadzieję, że wyzdrowieje. Jestem Ci dozgonnie wdzięczna!", getReply(npc));
		en.step(player, "offer");
		assertNull(getReply(npc));
		en.step(player, "job");
		assertEquals("Mój brat opiekuje się tą farmą. Ja tylko opiekuję się tutaj synem.", getReply(npc));
		en.step(player, "help");
		assertEquals("Philomena sprzeda Ci mleko i masło.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

	}

	@Test
	public void testRepeatingQuest() {

		npc = SingletonRepository.getNPCList().get("Anastasia");
		en = npc.getEngine();

		player.setQuest(questSlot, "-1");

		// [17:37] Admin kymara changed your state of the quest 'fishsoup_for_hughie' from '1294594642173' to '-1'
		// [17:37] Changed the state of quest 'fishsoup_for_hughie' from '1294594642173' to '-1'

		en.step(player, "hi");
		assertEquals("Witaj ponownie.", getReply(npc));
		en.step(player, "task");
		assertEquals("Mój Hughie znowu zaczyna chorować! Możesz przynieść mi jeszcze jedną zupę rybną? Pomóż mi, proszę, ostatni raz!", getReply(npc));
		en.step(player, "no");
		assertEquals("Nie, proszę! On jest naprawdę bardzo chory.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));

		en.step(player, "hi");
		assertEquals("Cześć, naprawdę przydałaby mi się #przysługa, proszę.", getReply(npc));
		en.step(player, "task");
		assertEquals("Mój biedny chłopak jest chory, a lekarstwa, które mu podaję, nie działają! Proszę, przyniesiesz dla niego zupę rybną?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dziękuję! Możesz poprosić Florence Boullabaisse, aby ci ugotowała zupę rybną. Myślę, że znajdziesz ją na targu w Ados.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Do widzenia.", getReply(npc));
	}
}
