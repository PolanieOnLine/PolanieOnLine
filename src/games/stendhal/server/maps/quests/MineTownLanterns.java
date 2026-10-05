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
				"Wiatr porwał latarenki, które szykowałem na święto. Wypatruj ich przy drogach, na polanach i w innych zakątkach, którędy wiedzie cię wędrówka. Przynieś mi pięć moich zgub. Poznasz je po śladach jesiennej zawieruchy. Kupieckich nie potrzebuję. Gdy mi pomożesz, zajrzyj znów za dwanaście godzin.");
	}

	@Override
	protected String rewardOffer() {
		return "Oddam ci za nie srebrną skrzynię. Niech i tobie coś dobrego przypadnie na święto.";
	}

	@Override
	protected String rewardThanks() {
		return "To moje zguby. Dobrze, że znów je mam. Weź srebrną skrzynię, jak obiecałem. Dzięki tobie rozświetlimy noc.";
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		createFestivalNPC("body=0,dress=14,head=0,mouth=0,eyes=18,mask=0,hair=27,hat=0",
				41, 16, "Rozwieszam latarenki przy miejscu biesiady, żeby nikt po zmroku nie zabłądził.");
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
