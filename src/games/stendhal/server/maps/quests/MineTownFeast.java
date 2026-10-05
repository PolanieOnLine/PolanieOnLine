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
				"Gości przybywa, a spiżarnia pustoszeje. Przynieś mi sto porcji mięsa, pięćdziesiąt porcji szynki, sto kawałków sera i czterdzieści steków. Tyle wystarczy na sześć godzin biesiady. Potem znów przyda się twoja pomoc.");
	}

	@Override
	protected String rewardOffer() {
		return "Mam dla ciebie skrzynkę schowaną na tę okazję. Nie odejdziesz z pustymi rękami.";
	}

	@Override
	protected String rewardThanks() {
		return "Dzięki ci, dobry człowieku. Będzie czym nakarmić gości. Weź tę skrzynkę i sam też odpocznij przy ogniu.";
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
				45, 16, "Doglądam strawy na święto. Pilnuję, żeby żaden gość nie odszedł głodny.");
	}
}
