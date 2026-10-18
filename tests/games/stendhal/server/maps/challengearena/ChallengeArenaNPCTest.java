/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.Direction;
import games.stendhal.common.tiled.StendhalMapStructure;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

public class ChallengeArenaNPCTest {
	@BeforeClass
	public static void beforeClass() throws Exception {
		QuestHelper.setUpBeforeClass();
	}

	@After
	public void after() {
		SingletonRepository.getNPCList().clear();
	}

	@Test
	public void arenaBuyerQuotesAndBuysLootThroughNormalConversation() {
		final StendhalRPZone zone = new StendhalRPZone("arena-buyer-dialog", 64, 64);
		ChallengeArenaBuyerNPC.create(zone, ChallengeArenaZone.BUYER_X, ChallengeArenaZone.BUYER_Y);
		final SpeakerNPC npc = (SpeakerNPC) zone.getEntitiesOfClass(SpeakerNPC.class).iterator().next();
		final Player player = PlayerTestHelper.createPlayer("ArenaSellerDialog");
		zone.add(player);
		final Item item = SingletonRepository.getEntityManager().getItem("kolczuga",
				ItemCreationContext.builder(ItemCreationContext.Source.DROP)
				.withFactoryRarity(ItemRarity.RARE).generateAffixes(false).build());
		player.getSlot("bag").add(item);
		final Engine engine = npc.getEngine();
		engine.step(player, "cześć");
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		engine.step(player, "sprzedaj kolczuga");
		assertEquals(ConversationStates.SELL_PRICE_OFFERED, engine.getCurrentState());
		assertTrue(getReply(npc).contains(MoneyUtils.formatPrice(item.getValue())));
		engine.step(player, "tak");
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertTrue(!player.isEquipped("kolczuga"));
		int paid = 0;
		for (final Item money : player.getAllEquipped("money")) { paid += money.getQuantity(); }
		assertEquals(item.getValue(), paid);
	}

	@Test
	public void arenaBuyerQuotesAndPaysReducedElixirPrices() {
		final StendhalRPZone zone = new StendhalRPZone("arena-buyer-elixirs", 64, 64);
		ChallengeArenaBuyerNPC.create(zone, ChallengeArenaZone.BUYER_X, ChallengeArenaZone.BUYER_Y);
		final SpeakerNPC npc = (SpeakerNPC) zone.getEntitiesOfClass(SpeakerNPC.class).iterator().next();
		final Player player = PlayerTestHelper.createPlayer("ArenaPotionSeller");
		zone.add(player);
		final StackableItem potions = (StackableItem) SingletonRepository.getEntityManager()
				.getItem("wielki eliksir", ItemCreationContext.restore());
		potions.setQuantity(5);
		player.getSlot("bag").add(potions);
		final Engine engine = npc.getEngine();
		engine.step(player, "cześć");
		engine.step(player, "sprzedaj 3 wielki eliksir");
		assertEquals(ConversationStates.SELL_PRICE_OFFERED, engine.getCurrentState());
		assertTrue(getReply(npc).contains(MoneyUtils.formatPrice(879)));
		engine.step(player, "tak");
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertEquals(879, MoneyUtils.getTotalMoneyInCopper(player));
		assertEquals(2, potions.getQuantity());
		assertEquals(1174, potions.getValue());
	}

	@Test
	public void arenaBuyerPaysTwiceTheGreatElixirPriceForGiantElixirs() {
		final StendhalRPZone zone = new StendhalRPZone("arena-buyer-giant-elixirs", 64, 64);
		ChallengeArenaBuyerNPC.create(zone, ChallengeArenaZone.BUYER_X, ChallengeArenaZone.BUYER_Y);
		final SpeakerNPC npc = (SpeakerNPC) zone.getEntitiesOfClass(SpeakerNPC.class).iterator().next();
		final Player player = PlayerTestHelper.createPlayer("ArenaGiantPotionSeller");
		zone.add(player);
		final StackableItem potions = (StackableItem) SingletonRepository.getEntityManager()
				.getItem("gigantyczny eliksir", ItemCreationContext.restore());
		potions.setQuantity(5);
		player.getSlot("bag").add(potions);
		final Engine engine = npc.getEngine();
		engine.step(player, "cześć");
		engine.step(player, "sprzedaj 3 gigantyczny eliksir");
		assertEquals(ConversationStates.SELL_PRICE_OFFERED, engine.getCurrentState());
		assertTrue(getReply(npc).contains(MoneyUtils.formatPrice(1758)));
		engine.step(player, "tak");
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertEquals(1758, MoneyUtils.getTotalMoneyInCopper(player));
		assertEquals(2, potions.getQuantity());
		assertEquals(48000, potions.getValue());
	}

	@Test
	public void blackItemCannotBeSoldUntilUltimateCollectorIsCompleted() {
		final StendhalRPZone zone = new StendhalRPZone("arena-buyer-baldwin-locked", 64, 64);
		ChallengeArenaBuyerNPC.create(zone, ChallengeArenaZone.BUYER_X, ChallengeArenaZone.BUYER_Y);
		final SpeakerNPC npc = (SpeakerNPC) zone.getEntitiesOfClass(SpeakerNPC.class).iterator().next();
		final Player player = PlayerTestHelper.createPlayer("ArenaBlackLocked");
		zone.add(player);
		final Item black = SingletonRepository.getEntityManager().getItem("czarna tarcza",
				ItemCreationContext.restore());
		player.getSlot("bag").add(black);
		final Engine engine = npc.getEngine();
		engine.step(player, "cześć");
		engine.step(player, "sprzedaj czarna tarcza");
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertTrue(getReply(npc).contains("Balduina"));
		assertTrue(player.isEquipped("czarna tarcza"));
		assertEquals(0, MoneyUtils.getTotalMoneyInCopper(player));

		player.setQuest("ultimate_collector", "done");
		engine.step(player, "sprzedaj czarna tarcza");
		assertEquals(ConversationStates.SELL_PRICE_OFFERED, engine.getCurrentState());
		assertTrue(getReply(npc).contains(MoneyUtils.formatPrice(black.getValue())));
		engine.step(player, "tak");
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertTrue(!player.isEquipped("czarna tarcza"));
		assertEquals(black.getValue(), MoneyUtils.getTotalMoneyInCopper(player));
	}

	@Test
	public void blackItemOfferIsRecheckedBeforeCompletingTheSale() {
		final StendhalRPZone zone = new StendhalRPZone("arena-buyer-baldwin-recheck", 64, 64);
		ChallengeArenaBuyerNPC.create(zone, ChallengeArenaZone.BUYER_X, ChallengeArenaZone.BUYER_Y);
		final SpeakerNPC npc = (SpeakerNPC) zone.getEntitiesOfClass(SpeakerNPC.class).iterator().next();
		final Player player = PlayerTestHelper.createPlayer("ArenaBlackRecheck");
		zone.add(player);
		final Item black = SingletonRepository.getEntityManager().getItem("czarna tarcza",
				ItemCreationContext.restore());
		player.getSlot("bag").add(black);
		player.setQuest("ultimate_collector", "done");
		final Engine engine = npc.getEngine();
		engine.step(player, "cześć");
		engine.step(player, "sprzedaj czarna tarcza");
		assertEquals(ConversationStates.SELL_PRICE_OFFERED, engine.getCurrentState());
		player.setQuest("ultimate_collector", "start");
		engine.step(player, "tak");
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertTrue(player.isEquipped("czarna tarcza"));
		assertEquals(0, MoneyUtils.getTotalMoneyInCopper(player));
	}

	@Test
	public void buyerHasMerchantClothesAndFacesLeftAtTheRequestedSpot() throws Exception {
		final StendhalRPZone zone = new StendhalRPZone("arena-buyer-location", 64, 64);
		final StendhalMapStructure map = TMXLoader.load("data/maps/interiors/tarnow/challenge_arena.tmx");
		zone.addCollisionLayer(zone.getName() + ".collision", map.getLayer("collision"));
		ChallengeArenaBuyerNPC.create(zone, ChallengeArenaZone.BUYER_X, ChallengeArenaZone.BUYER_Y);
		final SpeakerNPC npc = (SpeakerNPC) zone.getEntitiesOfClass(SpeakerNPC.class).iterator().next();
		assertEquals(38, npc.getX());
		assertEquals(50, npc.getY());
		assertEquals(Direction.LEFT, npc.getDirection());
		assertEquals("buyer2npc", npc.get("class"));
		assertTrue(new java.io.File("data/sprites/npc/buyer2npc.png").isFile());
		assertTrue("The buyer's tile must not be blocked", !zone.collisionMap.collides(38, 50));
		assertTrue("Players must be able to approach from the left", !zone.collisionMap.collides(37, 50));
	}

	@Test
	public void buyerOffersSoundLikeConversationAndExplainHowToSell() {
		final StendhalRPZone zone = new StendhalRPZone("arena-buyer-copy", 64, 64);
		ChallengeArenaBuyerNPC.create(zone, ChallengeArenaZone.BUYER_X, ChallengeArenaZone.BUYER_Y);
		final SpeakerNPC npc = (SpeakerNPC) zone.getEntitiesOfClass(SpeakerNPC.class).iterator().next();
		final Player player = PlayerTestHelper.createPlayer("ArenaBuyerCopy");
		final Engine engine = npc.getEngine();
		engine.step(player, "cześć");
		assertEquals("Witaj! Masz coś po walce? Chętnie odkupię twoje zdobycze.", getReply(npc));
		engine.step(player, "oferta");
		assertEquals("Chętnie odkupię broń, zbroje i inne łupy. Za rzadsze okazy zapłacę więcej. Monet, kluczy i rzeczy przypisanych do postaci nie biorę.", getReply(npc));
		engine.step(player, "pomoc");
		assertTrue(getReply(npc).contains("#'sprzedaj polano'"));
	}

	@Test
	public void buyerQuotesOneAndSeveralLogsWithoutBrokenGenderOrCoinEndings() {
		final StendhalRPZone zone = new StendhalRPZone("arena-buyer-polano", 64, 64);
		ChallengeArenaBuyerNPC.create(zone, ChallengeArenaZone.BUYER_X, ChallengeArenaZone.BUYER_Y);
		final SpeakerNPC npc = (SpeakerNPC) zone.getEntitiesOfClass(SpeakerNPC.class).iterator().next();
		final Player player = PlayerTestHelper.createPlayer("ArenaLogSeller");
		zone.add(player);
		final StackableItem logs = (StackableItem) SingletonRepository.getEntityManager().getItem("polano");
		logs.setQuantity(22);
		player.getSlot("bag").add(logs);
		final Engine engine = npc.getEngine();
		engine.step(player, "cześć");
		engine.step(player, "sprzedaj polano");
		assertEquals(ConversationStates.SELL_PRICE_OFFERED, engine.getCurrentState());
		assertEquals("Dam ci 1 monetę za polano. Sprzedajesz?", getReply(npc));
		engine.step(player, "nie");
		engine.step(player, "sprzedaj 22 polano");
		assertEquals(ConversationStates.SELL_PRICE_OFFERED, engine.getCurrentState());
		assertEquals("Dam ci 22 monety za 22 polana. Sprzedajesz?", getReply(npc));
		engine.step(player, "tak");
		assertEquals(22, MoneyUtils.getTotalMoneyInCopper(player));
		assertTrue(!player.isEquipped("polano"));
	}

	@Test
	public void missingLootDoesNotPretendThatTheItemIsWorthless() {
		final StendhalRPZone zone = new StendhalRPZone("arena-buyer-missing", 64, 64);
		ChallengeArenaBuyerNPC.create(zone, ChallengeArenaZone.BUYER_X, ChallengeArenaZone.BUYER_Y);
		final SpeakerNPC npc = (SpeakerNPC) zone.getEntitiesOfClass(SpeakerNPC.class).iterator().next();
		final Player player = PlayerTestHelper.createPlayer("ArenaMissingLoot");
		final Engine engine = npc.getEngine();
		engine.step(player, "cześć");
		engine.step(player, "sprzedaj polano");
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());
		assertEquals("Nie mogę odkupić tych rzeczy. Sprawdź, czy masz je przy sobie i czy nie są przypisane do postaci.", getReply(npc));
	}

	@Test
	public void tierSelectionWorksAcrossArenaConversationStates() {
		final StendhalRPZone zone = new StendhalRPZone(
				"int_tarnow_challenge_arena_test", 64, 64);
		ChallengeArenaNPC.create(zone, 32, 46);

		SpeakerNPC npc = null;
		for (final Entity entity : zone.getEntitiesOfClass(SpeakerNPC.class)) {
			final SpeakerNPC candidate = (SpeakerNPC) entity;
			if ("Mistrz Wyzwań".equals(candidate.getName())) {
				npc = candidate;
				break;
			}
		}
		assertTrue(npc != null);

		final Player player = PlayerTestHelper.createPlayer("ArenaDialogTester");
		final Engine engine = npc.getEngine();

		engine.setCurrentState(ConversationStates.IDLE);
		engine.step(player, "hi");
		assertEquals(ConversationStates.ATTENDING, engine.getCurrentState());

		final String[] triggers = {
				"próba", "potyczka", "łowca", "weteran", "czempion", "legenda"
		};
		final ChallengeArenaTier[] tiers = {
				ChallengeArenaTier.TRIAL,
				ChallengeArenaTier.SKIRMISH,
				ChallengeArenaTier.HUNTER,
				ChallengeArenaTier.VETERAN,
				ChallengeArenaTier.CHAMPION,
				ChallengeArenaTier.LEGEND
		};

		// Tier keywords must work directly after greeting, matching real player use.
		for (int i = 0; i < triggers.length; i++) {
			engine.setCurrentState(ConversationStates.ATTENDING);
			engine.step(player, triggers[i]);
			assertEquals(ConversationStates.QUESTION_2, engine.getCurrentState());
			assertEquals(tiers[i].name(),
					player.getQuest(SelectChallengeArenaTierAction.SELECTION_SLOT));
		}

		// They must also work after asking the NPC to show the arena offer.
		for (int i = 0; i < triggers.length; i++) {
			engine.setCurrentState(ConversationStates.ATTENDING);
			engine.step(player, "arena");
			assertEquals(ConversationStates.QUESTION_1, engine.getCurrentState());
			assertTrue(getReply(npc).contains("#próba"));

			engine.step(player, triggers[i]);
			assertEquals(ConversationStates.QUESTION_2, engine.getCurrentState());
			assertEquals(tiers[i].name(),
					player.getQuest(SelectChallengeArenaTierAction.SELECTION_SLOT));
		}
	}
}
