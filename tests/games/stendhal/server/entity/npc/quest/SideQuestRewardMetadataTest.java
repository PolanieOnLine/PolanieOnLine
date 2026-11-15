package games.stendhal.server.entity.npc.quest;

import static org.junit.Assert.*;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.*;
import games.stendhal.common.QuestRewardPreview;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.*;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class SideQuestRewardMetadataTest {
	private final Map<String, SpeakerNPC> previous = new LinkedHashMap<>();
	private Player player;
	@BeforeClass public static void init() throws Exception { QuestHelper.setUpBeforeClass(); }
	@Before public void setup() {
		player = PlayerTestHelper.createPlayer("reward-metadata-reader");
		StendhalRPZone zone = new StendhalRPZone("reward_metadata", 20, 20);
		for (String name : new String[] {"Hayunn Naratha", "Haunchy Meatoch", "Ceryl", "Jynath", "Tad", "Ilisa", "Ketteh Wehoh", "Hackim Easso"}) {
			previous.put(name, NPCList.get().remove(name)); zone.add(new SpeakerNPC(name));
		}
	}
	@After public void cleanup() {
		for (Map.Entry<String, SpeakerNPC> entry : previous.entrySet()) {
			NPCList.get().remove(entry.getKey()); if (entry.getValue() != null) NPCList.get().add(entry.getValue());
		}
	}
	private void fixed(QuestRewardPreview rewards, int xp, int coins) {
		assertNotNull(rewards); assertEquals(xp, rewards.xp); assertEquals(coins, rewards.coins); assertFalse(rewards.variable);
	}
	@Test public void simpleBuilderQuestsExposeTheirActualFixedRewards() {
		fixed(new BeerForHayunn().story().info().getRewards(), 50, 20);
		fixed(new LookBookforCeryl().story().info().getRewards(), 100, 50);
	}
	@Test public void legacyQuestTotalsIncludeIntermediatePaymentsAndTheActualWoodenShield() {
		MedicineForTad medicine = new MedicineForTad(); medicine.addToWorld();
		fixed(medicine.getQuestInfo(player).getRewards(), 550, 100);
		assertFalse(medicine.getQuestInfo(player).getRewards().notes.isEmpty());
		MeetHackim hackim = new MeetHackim(); hackim.addToWorld();
		fixed(hackim.getQuestInfo(player).getRewards(), 350, 75);
		MeetHayunn hayunn = new MeetHayunn(); hayunn.addToWorld();
		QuestRewardPreview reward = hayunn.getQuestInfo(player).getRewards();
		fixed(reward, 220, 150); assertEquals(1, reward.items.size());
		assertEquals("drewniana tarcza", reward.items.get(0).name); assertFalse(reward.items.get(0).bound);
	}
	@Test public void randomAndDeliveryRewardsAreExplicitOverviewsWithoutInventedItemRolls() {
		QuestRewardPreview coal = new CoalForHaunchy().story().info().getRewards();
		assertNotNull(coal); assertEquals(200, coal.xp); assertTrue(coal.variable);
		assertTrue(coal.items.isEmpty()); assertTrue(coal.notes.stream().anyMatch(note -> note.contains("1 do 4")));
		QuestRewardPreview pizza = new PizzaDelivery().story().info().getRewards();
		assertNotNull(pizza); assertTrue(pizza.variable); assertEquals(0, pizza.xp); assertEquals(0, pizza.coins);
		assertFalse(pizza.notes.isEmpty());
		assertEquals(0, player.getXP()); assertEquals(0, player.getNumberOfEquipped("money"));
	}
}
