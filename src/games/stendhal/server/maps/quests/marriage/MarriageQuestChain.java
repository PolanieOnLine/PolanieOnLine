/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.marriage;

import games.stendhal.common.Rand;
import games.stendhal.server.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;


/**
 * QUEST: Marriage
 * <p>
 * PARTICIPANTS:
 * <li> Sister Benedicta, the nun of Fado Church
 * <li> the Priest of Fado Church
 * <li> Ognir, the Ring Maker in Fado
 * <p>
 * STEPS:
 * <li> The nun explains that when two people are married, they can be together
 * whenever they want
 * <li> When two players wish to become engaged, they tell the nun
 * <li> The nun gives them invitation scrolls for the wedding, marked with the
 * church
 * <li>The players get a wedding ring made to give the other at the wedding
 * <li> They can get dressed into an outfit in the hotel
 * <li> When an engaged player goes to the priest, he knows they are there to be
 * married
 * <li> The marriage rites are performed
 * <li> The players are given rings
 * <li> When they go to the Hotel they choose a lovers room
 * <li> Champagne and fruit baskets is put in their bag (room if possible)
 * <li> They leave the lovers room when desired with another marked scroll
 * 
 * <p>
 * REWARD:
 * <li> Wedding Ring that teleports you to your spouse if worn - 1500 XP in
 * total
 * <li> nice food in the lovers room
 * <p>
 * 
 * REPETITIONS:
 * <li> None.
 * 
 * @author kymara
 */
public class MarriageQuestChain  {
	private static MarriageQuestInfo marriage = new MarriageQuestInfo();

	private static Logger logger = Logger.getLogger(MarriageQuestChain.class);


	public void addToWorld() {
		new Engagement(marriage).addToWorld();
		new MakeRings(marriage).addToWorld();
		new GetOutfits(marriage).addToWorld();	
		new Marriage(marriage).addToWorld();
		new Honeymoon(marriage).addToWorld();
		new Divorce(marriage).addToWorld();
	}

	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(marriage.getQuestSlot())) {
			return res;
		}
		final String questState = player.getQuest(marriage.getQuestSlot());
		res.add("Ja i " + getSpouseOrNickname(player) + " porozmawialiśmy z Sister Benedicta i potwierdziliśmy nasze zaręczyny.");
		res.add("Każdy z nas musi zrobć ślubną obrączkę, by użyć jej podczas ceremonii. Ognir nam pomoże.");
		if ("engaged".equals(questState)) {
			return res;
		} 
		res.add("Ognir zabrał złoto, które zgromadziłem i zgodził się odlać obrączkę, którą dam  " + getSpouseOrNickname(player)  + ".");
		if (questState.startsWith("forging")) {
			return res;
		} 
		res.add("Zabrałem mój pierścionek od Ognira. Napomknął, że moglibyśmy dostać specjalne stroje u Tymoteusza i Tamary w hotelu w Fado.");
		if ("engaged_with_ring".equals(questState)) {
	        res.add("Teraz muszę się tylko upewnić, że " + getSpouseOrNickname(player) + "zrobił pierścionek i będziemy mogli udać się razem do kościoła.");
			return res;
		} 
		res.add("Poślubiłem " + getSpouseOrNickname(player) + " na miłej ceremonii w kościele w Fado.");
		if ("just_married".equals(questState)) {
			res.add("Nie odbyliśmy jeszcze podróży poślubnej, powinniśmy o nią zapytać Lindę.");
			return res;
		} 
		res.add(getSpouseOrNickname(player) + " i ja spędziliśmy wspaniały miesiąc miodowy w Hotelu Fado! Pomogła nam w tym Linda.");
		if ("done".equals(questState)) {
			return res;
		}
		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu gracza " + player.getTitle() + " w stanie zadania " + questState);
		return debug;
	}
	
	private String getSpouseOrNickname(final Player player) {
		String spouse = player.getQuest(marriage.getSpouseQuestSlot());
		if(spouse == null) {
			// probably just engaged so we didn't get a spouse yet, just set this to something generic and sickly sweet
			spouse = Rand.rand(Arrays.asList("my dearest", "my love", "my honeypie", "the one I love"));
		}
		return spouse;
	}

}
