/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.Collections;

import games.stendhal.server.core.events.seasonal.MineTownLanternSpawns;
import games.stendhal.server.entity.player.Player;

/** Recover five festival lanterns, independently of accepting this quest. */
public class MineTownLanterns extends MineTownCollectionQuest {
	public static final String QUEST_NAME = "Zagubione latarenki";
	public static final String QUEST_SLOT = "minetown_lanterns_[year]";
	public static final int REQUIRED_LANTERNS = 5;
	private final MineTownLanternSpawns spawns = new MineTownLanternSpawns();

	public MineTownLanterns() {
		super(QUEST_SLOT, QUEST_NAME, "Wolrad", "srebrna skrzynia", 12,
				Collections.singletonMap("latarenka", REQUIRED_LANTERNS),
				"Przynieś 5 latarenek zagubionych podczas tej edycji Mine Town. Szukaj ich na mapach świata, nawet jeśli nie przyjmiesz zadania. Sklepowe latarenki się nie liczą. Możesz pomóc ponownie po 12 godzinach.");
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		createFestivalNPC("body=0,dress=14,head=0,mouth=0,eyes=18,mask=0,hair=27,hat=0",
				41, 16, "Odnajduję latarenki potrzebne do oświetlenia festynu.");
		try {
			spawns.start();
		} catch (final RuntimeException e) {
			removeFromWorld();
			throw e;
		}
	}

	@Override
	protected boolean hasRequiredItems(final Player player) {
		return player.isEquippedWithItemdata("latarenka", MineTownLanternSpawns.itemData(), REQUIRED_LANTERNS);
	}

	@Override
	protected void consumeRequiredItems(final Player player) {
		player.dropWithItemdata("latarenka", MineTownLanternSpawns.itemData(), REQUIRED_LANTERNS);
	}

	@Override
	public boolean removeFromWorld() {
		spawns.stop();
		return super.removeFromWorld();
	}
}
