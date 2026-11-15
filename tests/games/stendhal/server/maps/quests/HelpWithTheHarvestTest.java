package games.stendhal.server.maps.quests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.block.Block;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;
import utilities.SpeakerNPCTestHelper;

public class HelpWithTheHarvestTest {

	private StendhalRPZone zone;
	private Player first;
	private Player second;
	private Block firstCart;
	private Block secondCart;

	@BeforeClass
	public static void initialize() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@Before
	public void setup() {
		zone = new StendhalRPZone("0_ados_forest_w2", 128, 128);
		SingletonRepository.getRPWorld().addRPZone(zone);
		zone.add(SpeakerNPCTestHelper.createSpeakerNPC("Eheneumniranin"));
		new HelpWithTheHarvest().addToWorld();
		firstCart = zone.getEntitiesAt(87, 100, Block.class).get(0);
		secondCart = zone.getEntitiesAt(79, 106, Block.class).get(0);
		first = PlayerTestHelper.createPlayer("first_harvester");
		second = PlayerTestHelper.createPlayer("second_harvester");
		first.setQuest("helpwiththeharvest", "start;1");
		second.setQuest("helpwiththeharvest", "start;2");
	}

	@After
	public void cleanup() {
		SingletonRepository.getTurnNotifier().dontNotify(firstCart);
		SingletonRepository.getTurnNotifier().dontNotify(secondCart);
		SingletonRepository.getNPCList().remove("Eheneumniranin");
		SingletonRepository.getRPWorld().removeZone(zone);
		PlayerTestHelper.removeAllPlayers();
	}

	@Test
	public void deliveryNeverResetsTheOtherPlayersCart() {
		secondCart.setPosition(65, 77);
		secondCart.push(second, Direction.UP);
		assertEquals(76, secondCart.getY());
		firstCart.setPosition(64, 76);
		firstCart.push(first, Direction.UP);
		assertEquals("start;0", first.getQuest("helpwiththeharvest"));
		assertEquals("start;2", second.getQuest("helpwiththeharvest"));
		assertEquals(87, firstCart.getX());
		assertEquals(100, firstCart.getY());
		assertEquals("The second cart must stay beside the barn", 65, secondCart.getX());
		assertEquals("The second cart must stay beside the barn", 76, secondCart.getY());

		secondCart.push(second, Direction.UP);
		assertEquals("start;1", second.getQuest("helpwiththeharvest"));
		assertEquals(79, secondCart.getX());
		assertEquals(106, secondCart.getY());
	}
}
