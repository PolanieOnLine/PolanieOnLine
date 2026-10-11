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
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.common.constants.ItemRarity;
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
		ChallengeArenaBuyerNPC.create(zone, 40, 47);
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
