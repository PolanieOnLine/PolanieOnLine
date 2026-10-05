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
				"Do rytuału potrzebuję 20 strasznych dyń. Najpierw pomóż Boguchwałowi przygotować festyn i oddaj Wolradowi zagubione latarenki. Nagrodę możesz otrzymać raz w tej edycji Mine Town.");
	}

	@Override
	protected boolean prerequisitesMet(final Player player) {
		return new MineTownFeast().completionCount(player) > 0
				&& new MineTownLanterns().completionCount(player) > 0;
	}
}
