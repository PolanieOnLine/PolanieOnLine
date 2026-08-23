/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.entity.npc.quest;

import static org.junit.Assert.assertSame;

import org.junit.Test;

import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.maps.quests.BringMagic;
import games.stendhal.server.maps.quests.ClearTower;
import games.stendhal.server.maps.quests.KillMtElves;

public class QuestOfferBuilderTest {

	@Test
	public void defaultsToFirstQuestOfferState() {
		final SimpleQuestOfferBuilder offer = new SimpleQuestOfferBuilder();

		assertSame(ConversationStates.QUEST_OFFERED, offer.offerState);
	}

	@Test
	public void secondQuestCanUseSeparateOfferState() {
		final SimpleQuestOfferBuilder offer = new SimpleQuestOfferBuilder();

		assertSame(offer, offer.offerState(ConversationStates.QUEST_2_OFFERED));
		assertSame(ConversationStates.QUEST_2_OFFERED, offer.offerState);
	}

	@Test
	public void czarnoksieznikQuestsUseDifferentOfferStates() {
		final KillCreaturesQuestBuilder clearTower = new ClearTower().story();
		final KillCreaturesQuestBuilder mountainElves = new KillMtElves().story();
		final CraftItemQuestBuilder bringMagic = new BringMagic().story();

		assertSame(ConversationStates.QUEST_OFFERED, clearTower.offer.offerState);
		assertSame(ConversationStates.QUEST_2_OFFERED, mountainElves.offer.offerState);
		assertSame(ConversationStates.QUEST_3_OFFERED, bringMagic.offer.offerState);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNullOfferState() {
		new SimpleQuestOfferBuilder().offerState(null);
	}
}
