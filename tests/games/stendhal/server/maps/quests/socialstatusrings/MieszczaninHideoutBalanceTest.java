/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MieszczaninHideoutBalanceTest {

	@Test
	public void existingHideoutBanditsAreRaisedAboveQuestEntryLevel() {
		assertEquals(165, MieszczaninQuestBandit.challengeLevel(130));
		assertEquals(170, MieszczaninQuestBandit.challengeLevel(135));
		assertEquals(175, MieszczaninQuestBandit.challengeLevel(140));

		assertEquals(1480, MieszczaninQuestBandit.challengeAtk(130, 740));
		assertEquals(1425, MieszczaninQuestBandit.challengeAtk(135, 500));
		assertEquals(1650, MieszczaninQuestBandit.challengeAtk(140, 800));

		assertEquals(85, MieszczaninQuestBandit.challengeDef(130, 27));
		assertEquals(97, MieszczaninQuestBandit.challengeDef(135, 32));
		assertEquals(112, MieszczaninQuestBandit.challengeDef(140, 57));

		assertEquals(2800, MieszczaninQuestBandit.challengeHP(130, 800));
		assertEquals(4000, MieszczaninQuestBandit.challengeHP(135, 1200));
		assertEquals(5200, MieszczaninQuestBandit.challengeHP(140, 1100));
		assertTrue(MieszczaninQuestBandit.minimumSpeed() >= 0.9);
	}
}
