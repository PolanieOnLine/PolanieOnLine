/***************************************************************************
 *                 (C) Copyright 2003-2022 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import games.stendhal.common.MathHelper;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseBaseHPOnlyOnceAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.MultiTask;
import games.stendhal.server.entity.npc.quest.QuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

public class KillHerszt implements QuestManuscript {
	final static String QUEST_SLOT = "kill_herszt";
	public QuestBuilder<?> story() {
		QuestBuilder<MultiTask> quest = new QuestBuilder<>(new MultiTask());

		quest.info()
			.name("Pozbycie się Rozbójników")
			.description("Pogłoski krażą po zimowej krainie iż Jędrzej ma kłopoty z rozbójnikami, którzy zasiedlili się w jaskini nie daleko miasta.")
			.internalName(QUEST_SLOT)
			.repeatableAfterMinutes(MathHelper.MINUTES_IN_ONE_WEEK * 2)
			.minLevel(30)
			.region(Region.ZAKOPANE_CITY)
			.questGiverNpc("Gazda Jędrzej");

		quest.history()
			.whenNpcWasMet("Gazda Jędrzej spotkany na górze niedaleko miasta i wejścia do jaskini.")
			.whenQuestWasRejected("Zgraja zbójów wydaje się być bardzo groźna dla mojego cennego życia.")
			.whenQuestWasAccepted("Banda zbójników wydaje się groźna dla reszty mieszkańców tego miasta, muszę się ich jak najszybciej pozbyć oraz przynieść jakiś dowód iż herszt nie będzie sprawiał problemów.")
			.whenTaskWasCompleted("Moja wyprawa na rozbójników w jaskini nieco uspokoiła nerwy gazdy Jędrzeja.")
			.whenQuestWasCompleted("Jędrzej podziękował za moją ciężką pracę i podarował niewielki prezent.")
			.whenQuestCanBeRepeated("Minęło trochę czasu od ostatniego spotkania z gazdą Jędrzejem, być może znów potrzebuje pomocy ze zbójnikami.");

		quest.offer()
			.respondToRequest("Nie możemy uwolnić się od zbójników grasujących na tym terenie, a w szczególności od Herszta górskich zbójników. Czy mógłbyś udać się do pobliskiej #jaskini i pozbyć się ich?")
			.respondToUnrepeatableRequest("Bardzo dziękuję za pomoc w imieniu reszty. Dobrze, że pytasz ponieważ zbójnicy mogą powrócić w każdej chwili...")
			.respondToRepeatedRequest("Znowu potrzebujemy Twojej pomocy w sprawie rozbójników. Czy możesz znowu się nimi zająć, prosimy?")
			.respondToAccept("Wspaniale! Proszę znajdź ich. Kręcą się gdzieś tutaj. Na pewną są w tej jaskini. Niech zapłacą za swoje winy! Pamiętaj, przynieś mi coś, czym możesz udowodnić iż pozbyłeś się herszta bandy!")
			.acceptationKarmaReward(5.0)
			.respondToReject("Rozumiem. Każdy się ich boi. Poczekam na kogoś odpowiedniego do tego zadania.")
			.rejectionKarmaPenalty(5.0)
			.respondTo("cave", "mine", "jaskini", "jaskinia", "kopalnia").saying("Najbliższe wejście do jaskini znajduje się tuż za tym wzniesieniem. Wypatruj ich!")
			.remind("Już się Ciebie pytałem o pozbyciu się zbójników z naszych terenów! Pamiętaj również o udowodnieniu swego czynu...");

		quest.task()
			.requestItem(1, "pióro herszta hordy zbójeckiej")
			.requestKill(1, "zbójnik górski herszt")
			.requestKill(2, "zbójnik górski")
			.requestKill(2, "zbójnik górski goniec")
			.requestKill(3, "zbójnik górski złośliwy")
			.requestKill(2, "zbójnik górski zwiadowca")
			.requestKill(1, "zbójnik górski starszy");

		quest.complete()
			.greet("Wieść o twych czynach dotarła tu przed tobą! Godzien jesteś czci rycerskiej, lecz pamiętaj, że droga nie będzie łatwa, bo na każdym kroku musisz udowodnić swoje męstwo i odwagę!")
			.rewardWith(new IncreaseXPAction(5000))
			.rewardWith(new IncreaseKarmaAction(15.0))
			.rewardWith(new IncreaseBaseHPOnlyOnceAction(QUEST_SLOT, 20))
			.rewardWith(new EquipItemAction("bryłka mithrilu"));

		return quest;
	}
}
