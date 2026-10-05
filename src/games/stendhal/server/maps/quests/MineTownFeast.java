/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.LinkedHashMap;
import java.util.Map;

/** Supplies for the festival, repeatable six hours after delivery. */
public class MineTownFeast extends MineTownCollectionQuest {
	public static final String QUEST_NAME = "Przygotowania do Mine Town";
	public static final String QUEST_SLOT = "minetown_feast_[year]";

	public MineTownFeast() {
		super(QUEST_SLOT, QUEST_NAME, "Boguchwał", "skrzynka", 6, requirements(),
				"Przygotowuję jedzenie na festyn. Przynieś 100 mięsa, 50 szynki, 100 sera i 40 steków. Możesz pomóc ponownie po 6 godzinach od oddania zapasów.");
	}

	private static Map<String, Integer> requirements() {
		final Map<String, Integer> items = new LinkedHashMap<String, Integer>();
		items.put("mięso", 100);
		items.put("szynka", 50);
		items.put("ser", 100);
		items.put("stek", 40);
		return items;
	}

	@Override
	public void addToWorld() {
		super.addToWorld();
		createFestivalNPC("body=0,dress=33,head=0,mouth=0,eyes=18,mask=0,hair=27,hat=1",
				45, 16, "Przygotowuję posiłki dla uczestników festynu Mine Town.");
	}
}
