package games.stendhal.server.maps.quests;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.athor.dressingroom_female.LifeguardNPC;
import games.stendhal.server.maps.athor.holiday_area.TouristFromAdosNPC;
import utilities.NPCTestHelper;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class SuntanCreamForZaraTest {

	private Player player = null;
	private SpeakerNPC npc = null;
	private Engine en = null;

	private String questSlot;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setUp() {
		final StendhalRPZone zone = new StendhalRPZone("admin_test");
		new TouristFromAdosNPC().configureZone(zone, null);
		new LifeguardNPC().configureZone(zone, null);
		
		// configure Pam production
		NPCTestHelper.loadProductions("Pam");

		AbstractQuest quest = new SuntanCreamForZara();
		quest.addToWorld();

		player = PlayerTestHelper.createPlayer("bob");

		questSlot = quest.getSlotName();
	}

	@Test
	public void testStartQuest() {

		npc = SingletonRepository.getNPCList().get("Zara");
		en = npc.getEngine();


		// -----------------------------------------------

		en.step(player, "hi");
		assertEquals("Miło Cię poznać!", getReply(npc));
		en.step(player, "task");
		assertEquals("Czuję się śpiąca na słońcu, a moja skóra jest teraz spalona. Czy możesz mi przynieść #'olejek do opalania' wyrabiany przez #ratowników?", getReply(npc));
		en.step(player, "olejek do opalania");
		assertEquals("#Ratownicy robią wspaniały olejek do ochrony przed słońcem i leczący oparzenia w tym samym czasie. Zdobędziesz go dla mnie?", getReply(npc));
		en.step(player, "lifeguards");
		assertEquals("Ratownikami są Pam i David. Przebywają chyba w przebieralniach. Zapytasz się ich dla mnie?", getReply(npc));
		en.step(player, "no");
		assertEquals("Dobrze. Mam dla ciebie nagrodę...", getReply(npc));
		en.step(player, "bye");
		assertEquals("Mam nadzieję, że zobaczymy się później!", getReply(npc));
		en.step(player, "hi");
		assertEquals("Miło Cię poznać!", getReply(npc));
		en.step(player, "task");
		assertEquals("Ostatnio zgodziłeś się mi pomóc, a moja skóra ma się coraz gorzej. Proszę czy mógłbyś przynieść mi #'olejek do opalania', który #ratownicy produkują?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dziękuję bardzo. Będę czekać na twój powrót!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Mam nadzieję, że zobaczymy się później!", getReply(npc));
		en.step(player, "hi");
		assertEquals("Wiem, że #'olejek do opalania' jest trudno dostać, ale mam nadzieję, że nie zapomniałeś o moim problemie...", getReply(npc));
		en.step(player, "task");
		assertEquals("Zapomniałeś o obietnicy zapytania się #ratowników o #'olejek do opalania'?", getReply(npc));
		en.step(player, "olejek do opalania");
		assertEquals("#Ratownicy robią wspaniały olejek do ochrony przed słońcem i także leczący oparzenia w tym samym czasie.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Mam nadzieję, że zobaczymy się później!", getReply(npc));

		assertEquals(player.getQuest(questSlot), "start");
	}

	@Test
	public void testGetCream() {

		npc = SingletonRepository.getNPCList().get("Pam");
		en = npc.getEngine();

		// doesn't matter what the player 'suntan cream for zara' quest slot is for making cream - pam is just a producer

		en.step(player, "hi");
		assertEquals("Hallo!", getReply(npc));
		en.step(player, "olejek do opalania");
		assertEquals("Olejek do opalania Davida i mój jest słynny na całą wyspę, ale że wejście do labiryntu jest zablokowane to nie możemy zdobyć wszystkich składników. Jeżeli przyniesiesz mi składniki to mogę zrobić dla Ciebie nasz specjalny krem do opalania. Powiedz tylko #zrób.", getReply(npc));
		en.step(player, "mix");
		assertEquals("Mogę zrobić olejek do opalania jeżeli przyniesiesz mi 1 #'mały eliksir', 1 #arandula, oraz 1 #kokuda.", getReply(npc));
		en.step(player, "arandula");
		assertEquals("Arandula jest ziołem rosnącym w okolicach Semos.", getReply(npc));
		en.step(player, "kokuda");
		assertEquals("Nie możemy zdobyć Kokudy, która rośnie na wyspie, ponieważ wejście do labiryntu gdzie można znaleźć to zioło jest zablokowane.", getReply(npc));
		en.step(player, "mały eliksir");
		assertEquals("Jest to mała buteleczka wypełniona miksturą. Możesz ją kupić w kilku miejscach.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Miłej zabawy!", getReply(npc));

		PlayerTestHelper.equipWithItem(player, "mały eliksir");
		PlayerTestHelper.equipWithItem(player, "kokuda");
		PlayerTestHelper.equipWithItem(player, "arandula");
		assertFalse(player.isEquipped("olejek do opalania"));

		en.step(player, "hi");
		assertEquals("Hallo!", getReply(npc));
		en.step(player, "mix");
		assertEquals("Potrzebuję, abyś przyniósł mi 1 #'mały eliksir', 1 #arandula, oraz 1 #kokuda do tej pracy, która zajmie 10 minut. Posiadasz to przy sobie?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dobrze, zrobię dla Ciebie olejek do opalania, ale zajmie mi to trochę czasu. Wróć za 10 minut.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Miłej zabawy!", getReply(npc));

		assertNotNull(player.getQuest("pamela_mix_cream"));
		assertFalse(player.isEquipped("mały eliksir"));
		assertFalse(player.isEquipped("kokuda"));
		assertFalse(player.isEquipped("arandula"));

		en.step(player, "hi");
		assertEquals("Witaj z powrotem! Wciąż zajmuje się twoim zleceniem olejek do opalania. Wróć za 10 minut, aby odebrać.", getReply(npc));
		en.step(player, "bye");
		assertEquals("Miłej zabawy!", getReply(npc));

		// [10:02] Admin kymara changed your state of the quest 'pamela_mix_cream' from '1;suntan cream;1288519190459' to '1;suntan cream;0'
		// [10:02] Changed the state of quest 'pamela_mix_cream' from '1;suntan cream;1288519190459' to '1;suntan cream;0'
		player.setQuest("pamela_mix_cream", "1;olejek do opalania;0");

		final int xp = player.getXP();
		en.step(player, "hi");
		assertEquals("Witaj z powrotem! Skończyłam twoje zlecenie. Trzymaj, oto olejek do opalania.", getReply(npc));
		// [10:02] kymara earns 1 experience point.
		en.step(player, "bye");
		assertEquals("Miłej zabawy!", getReply(npc));

		assertThat(player.getXP(), greaterThan(xp));
		// wow one whole xp

		assertTrue(player.isEquipped("olejek do opalania"));
	}

	@Test
	public void testCompleteQuest() {

		npc = SingletonRepository.getNPCList().get("Zara");
		en = npc.getEngine();

		player.setQuest(questSlot, "start");
		PlayerTestHelper.equipWithItem(player, "olejek do opalania");

		en.step(player, "hi");
		assertEquals("Wspaniale! Dostałeś olejek! Jest dla mnie?", getReply(npc));
		en.step(player, "no");
		assertEquals("Nie? Spójrz na mnie! Nie mogę uwierzyć, że jesteś takim samolubem!", getReply(npc));
		en.step(player, "task");
		assertEquals("Zapomniałeś o obietnicy zapytania się #ratowników o #'olejek do opalania'?", getReply(npc));
		en.step(player, "bye");
		assertEquals("Mam nadzieję, że zobaczymy się później!", getReply(npc));

		final int xp = player.getXP();
		final double karma = player.getKarma();

		en.step(player, "hi");
		assertEquals("Wspaniale! Dostałeś olejek! Jest dla mnie?", getReply(npc));
		en.step(player, "yes");
		assertEquals("Dziękuję! Czuję się lepiej! Weź ten klucz do mojego domu w Ados. Czuj się jak u siebie w domu tak długo jak tu będę!", getReply(npc));
		// [10:03] kymara earns 1000 experience points.
		en.step(player, "bye");
		assertEquals("Mam nadzieję, że zobaczymy się później!", getReply(npc));

		assertFalse(player.isEquipped("olejek do opalania"));
		assertTrue(player.isEquipped("kluczyk Zary"));

		assertEquals(player.getQuest(questSlot), "done");

		assertThat(player.getXP(), greaterThan(xp));
		assertThat(player.getKarma(), greaterThan(karma));

		en.step(player, "hi");
		assertEquals("Miło Cię poznać!", getReply(npc));
		en.step(player, "task");
		assertEquals("Nie mam nowego zadania dla ciebie. Ale dziękuję za krem do opalania. Czuję, że moja skóra ma się coraz lepiej!", getReply(npc));
		en.step(player, "bye");
		assertEquals("Mam nadzieję, że zobaczymy się później!", getReply(npc));
		// [10:03] You see the key for Zara's row house in Ados. It is a special quest reward for kymara, and cannot be used by others.
	}
}
