package games.stendhal.server.maps.quests;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.dragon.cave.eFuRNPC;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.RPClass.ItemTestHelper;

public class ClearQuestPierscienTest {
	private static final String SERVICE_SLOT = "clear_questy_pierscieni";
	private static final String CLOAK_SLOT = "mithril_cloak";

	private Player player;
	private SpeakerNPC npc;
	private Engine engine;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
		MockStendlRPWorld.get();
		final StendhalRPZone zone = new StendhalRPZone("test_efur_reset");
		new eFuRNPC().configureZone(zone, null);
		new ClearQuestPierscien().addToWorld();
	}

	@Before
	public void setUp() {
		player = PlayerTestHelper.createPlayer("resetter");
		player.setLevel(150);
		npc = SingletonRepository.getNPCList().get("eFuR");
		engine = npc.getEngine();
	}

	@Test
	public void activeCloakCanBePreparedButOrdinaryYesDoesNotResetIt() {
		player.setQuest(CLOAK_SLOT, "machine;olejek=1");

		engine.step(player, "hi");
		engine.step(player, "zadanie");
		engine.step(player, "płaszcz");

		assertThat(player.getQuest(SERVICE_SLOT), equalTo("confirm:plaszcz"));
		assertThat(getReply(npc), containsString("potwierdzam"));

		engine.step(player, "tak");
		assertThat(player.getQuest(CLOAK_SLOT), equalTo("machine;olejek=1"));
		assertThat(player.getQuest(SERVICE_SLOT), equalTo("confirm:plaszcz"));
	}

	@Test
	public void confirmationResetsCloakWithoutAwardingExperience() {
		player.setQuest(CLOAK_SLOT, "machine;olejek=1");
		final Item money = ItemTestHelper.createItem("money", 2000000);
		player.getSlot("bag").add(money);
		final int xp = player.getXP();

		engine.step(player, "hi");
		engine.step(player, "płaszcz");
		engine.step(player, "potwierdzam");

		assertFalse(player.hasQuest(CLOAK_SLOT));
		assertFalse(player.hasQuest(SERVICE_SLOT));
		assertThat(player.getXP(), equalTo(xp));
		assertThat(player.getNumberOfEquipped("money"), equalTo(0));
	}

	@Test
	public void incompletePaymentDoesNotConsumeAnythingOrResetQuest() {
		player.setQuest(CLOAK_SLOT, "machine;olejek=1");
		final Item money = ItemTestHelper.createItem("money", 1000000);
		player.getSlot("bag").add(money);

		engine.step(player, "hi");
		engine.step(player, "płaszcz");
		engine.step(player, "potwierdzam");

		assertTrue(player.hasQuest(CLOAK_SLOT));
		assertThat(player.getQuest(CLOAK_SLOT), equalTo("machine;olejek=1"));
		assertThat(player.getNumberOfEquipped("money"), equalTo(1000000));
		assertThat(player.getQuest(SERVICE_SLOT), equalTo("confirm:plaszcz"));
	}

	@Test
	public void oldPendingSelectionStillRequiresExplicitConfirmation() {
		player.setQuest(CLOAK_SLOT, "machine;olejek=1");
		player.setQuest(SERVICE_SLOT, "plaszcz");

		engine.step(player, "hi");
		engine.step(player, "zadanie");

		assertThat(getReply(npc), containsString("potwierdzam"));
		assertTrue(player.hasQuest(CLOAK_SLOT));
	}
}
