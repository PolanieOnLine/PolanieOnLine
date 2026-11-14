package games.stendhal.server.maps.quests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.npc.quest.BuiltQuest;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import utilities.SpeakerNPCTestHelper;

public class CiupagaInheritanceQuestTest {
	private static final String[] QUESTS = {"zlota_ciupaga", "zlota_ciupaga_was", "ciupaga_dwa_wasy", "ciupaga_trzy_wasy"};
	private static final String[] NPCS = {"Kowal Andrzej", "Józek", "Krasnolud", "Hadrin"};
	private static final String[] ITEMS = {"ciupaga", "złota ciupaga", "złota ciupaga z wąsem",
			"złota ciupaga z dwoma wąsami", "złota ciupaga z trzema wąsami"};

	@Before
	public void setUp() {
		PlayerTestHelper.createPlayer("ciupaga_setup");
		NPCList.get().clear();
		for (final String name : new String[] {"Kowal Andrzej", "Józek", "Krasnolud", "Hadrin", "Władca Smoków"}) {
			final SpeakerNPC npc = SpeakerNPCTestHelper.createSpeakerNPC(name);
			npc.addGreeting("Witaj.");
			npc.addGoodbye("Do widzenia.");
			NPCList.get().add(npc);
		}
		new BuiltQuest(new ZlotaCiupaga().story()).addToWorld();
		new BuiltQuest(new ZlotaCiupagaJedenWas().story()).addToWorld();
		new ZlotaCiupagaDwaWasy().addToWorld();
		new ZlotaCiupagaTrzyWasy().addToWorld();
	}

	@After
	public void tearDown() {
		NPCList.get().clear();
	}

	private void supply(final Player player, final String name, final int quantity) {
		final Item item = SingletonRepository.getEntityManager().getItem(name);
		if (item instanceof StackableItem) {
			((StackableItem) item).setQuantity(quantity);
		}
		player.getSlot("bag").add(item);
	}

	private void upgradeChain(final ItemRarity startingRarity) {
		final Player player = PlayerTestHelper.createPlayer("ciupaga_quest_player");
		player.setLevel(500);
		player.addKarma(5000);
		player.setQuest("kill_herszt_basehp", "done");
		player.setQuest("help_wielkolud_basehp", "done");
		player.setSoloKill("złota śmierć");
		Item source = SingletonRepository.getEntityManager().getItem(ITEMS[0], ItemCreationContext
				.builder(ItemCreationContext.Source.QUEST).withRarity(startingRarity).withAffixSeed(131L).build());
		source.setBoundTo(player.getName());
		player.getSlot("bag").add(source);
		for (int stage = 0; stage < QUESTS.length; stage++) {
			player.setLevel(500);
			final Map<String, String> previous = ItemAffixState.getValues(source);
			player.setQuest(QUESTS[stage], "start");
			final int[] money = {50000, 120000, 1200000, 2500000};
			final int[] wood = {5, 4, 10, 70};
			final int[] gold = {25, 70, 150, 240};
			supply(player, "money", money[stage]);
			supply(player, "polano", wood[stage]);
			supply(player, "sztabka złota", gold[stage]);
			if (stage == 1) {
				supply(player, "złoty róg", 1);
			} else if (stage == 2) {
				supply(player, "pazur zielonego smoka", 1);
				supply(player, "pazur czerwonego smoka", 1);
				supply(player, "pióro serafina", 2);
			} else if (stage == 3) {
				for (final String color : new String[] {"arktycznego", "niebieskiego", "czarnego", "złotego"}) {
					supply(player, "pazur " + color + " smoka", 1);
				}
				supply(player, "pióro azazela", 3);
				supply(player, "magiczna krew", 10);
				supply(player, "smocza krew", 5);
				supply(player, "cudowna krew", 1);
				supply(player, "wywar wąsatych smoków", 1);
			}
			final Engine engine = NPCList.get().get(NPCS[stage]).getEngine();
			assertTrue(engine.step(player, "hi"));
			final long before = System.currentTimeMillis();
			assertTrue("Crafting stage " + stage, engine.step(player, stage < 2 ? "forge" : "przedmioty"));
			assertEquals("forging", player.getQuest(QUESTS[stage], 0));
			assertTrue(Long.parseLong(player.getQuest(QUESTS[stage], 1)) >= before);
			assertFalse(new TimePassedCondition(QUESTS[stage], 1, 60).fire(player, null, null));
			assertEquals(0, player.getNumberOfEquipped(ITEMS[stage]));
			player.setQuest(QUESTS[stage], 1, "0");
			engine.step(player, "bye");
			assertTrue(engine.step(player, "hi"));
			if (stage >= 2) {
				assertTrue(engine.step(player, "nagroda"));
			}
			source = player.getFirstEquipped(ITEMS[stage + 1]);
			assertNotNull(source);
			assertEquals(startingRarity == ItemRarity.LEGENDARY ? ItemRarity.LEGENDARY : ItemRarity.EPIC, source.getRarity());
			assertTrue(ItemAffixState.getValues(source).entrySet().containsAll(previous.entrySet()));
			assertEquals(startingRarity == ItemRarity.LEGENDARY ? 4 : 2, ItemAffixState.getValues(source).size());
			assertEquals(player.getName(), source.getBoundTo());
			assertTrue(player.isQuestCompleted(QUESTS[stage]));
		}
	}

	@Test
	public void legendaryInputKeepsItsTierAndAllAffixesAtEveryBlacksmith() {
		upgradeChain(ItemRarity.LEGENDARY);
	}

	@Test
	public void rareInputGainsOneAffixThenKeepsItsTwoEpicAffixesAtEveryBlacksmith() {
		upgradeChain(ItemRarity.RARE);
	}
}
