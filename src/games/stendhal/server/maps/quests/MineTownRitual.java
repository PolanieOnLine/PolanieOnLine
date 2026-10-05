/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.Collections;

import games.stendhal.server.entity.player.Player;

/** One gold chest per character and yearly event edition, not per activation. */
public class MineTownRitual extends MineTownCollectionQuest {
	public static final String QUEST_NAME = "Rytuał Guślarza";
	public static final String QUEST_SLOT = "minetown_ritual_[year]";

	public MineTownRitual() {
		super(QUEST_SLOT, QUEST_NAME, "Guślarz", "złota skrzynia", 0,
				Collections.singletonMap("straszna dynia", 20),
				"W jesienne noce duchy podchodzą blisko naszych ognisk. Przynieś mi dwadzieścia strasznych dyń, a odprawimy gusła. Niech Boguchwał wpierw napełni stoły, a Wolrad rozświetli noc. Takiego daru duchy oczekują od ciebie tylko raz w roku.");
	}

	@Override
	protected String rewardOffer() {
		return "Za twoją pomoc oddam ci złotą skrzynię. Strzegłem jej, czekając na kogoś odważnego.";
	}

	@Override
	protected String rewardThanks() {
		return "Ogień płonie spokojnie. Duchy przyjęły nasz dar. Weź złotą skrzynię i wracaj do swoich. Niech przodkowie mają cię w opiece.";
	}

	@Override
	protected boolean prerequisitesMet(final Player player) {
		return new MineTownFeast().completionCount(player) > 0
				&& new MineTownLanterns().completionCount(player) > 0;
	}
}
