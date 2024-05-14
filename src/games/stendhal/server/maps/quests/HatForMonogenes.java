/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.BringItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.semos.city.GreeterNPC;
import games.stendhal.server.util.ResetSpeakerNPC;

/**
 * QUEST: Hat For Monogenes
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Monogenes, an old man in Semos city.</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Monogenes asks you to buy a hat for him.</li>
 * <li> Xin Blanca sells you a leather helmet.</li>
 * <li> Monogenes sees your leather helmet and asks for it and then thanks you.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>50 XP</li>
 * <li>Karma: 10</li>
 * </ul>
 *
 * REPETITIONS: - None.
 */
public class HatForMonogenes implements QuestManuscript {
	@Override
	public BringItemQuestBuilder story() {
		BringItemQuestBuilder quest = new BringItemQuestBuilder();

		quest.info()
			.name("Czapka Monogenesa")
			.description("Monogenes potrzebuje czapki, aby pomogła mu się ogrzać podczas zimy.")
			.internalName("hat_monogenes")
			.notRepeatable()
			.minLevel(0)
			.region(Region.SEMOS_CITY)
			.questGiverNpc("Monogenes");

		quest.history()
			.whenNpcWasMet("Spotkałem Monogenesa przy źródle w wiosce Semos.")
			.whenQuestWasRejected("Poprosił mnie o czapkę, ale nie chcę mu pomagać.")
			.whenQuestWasAccepted("Muszę znaleźć mu czapkę, coś skórzanego, żeby ogrzać mu głowę.")
			.whenTaskWasCompleted("Znalazłem czapkę.")
			.whenQuestWasCompleted("Oddałem Monogenesowi czapkę, żeby ogrzała mu łysą głowę.");

		quest.offer()
			.respondToRequest("Czy mógłbyś przynieść mi #czapkę, żeby przykryć moją łysą głowę? Brrrrr! Dni tutaj w Semos robią się naprawdę zimne...")
			.respondToUnrepeatableRequest("Dzięki za ofertę, dobry przyjacielu, ale ta czapka wystarczy mi na co najmniej pięć zim, a nie tak, żebym potrzebował więcej niż jednej.")
			.respondToAccept("Dzięki, mój dobry przyjacielu. Będę tu czekać na twój powrót!")
			.respondToReject("Z pewnością masz ważniejsze rzeczy do zrobienia, a mało czasu, by je zrobić. Chyba zostanę tu i zamarznę... *pociągnięcie nosem*")
			.rejectionKarmaPenalty(5.0)
			.remind("Hej, mój dobry przyjacielu, pamiętasz tę skórzaną czapkę, o której ci mówiłem wcześniej? Wciąż tu dosyć zimno...")
			.respondTo("czapka").saying("Nie wiesz, co to jest czapka?! Cokolwiek lekkiego, co może przykryć moją głowę; na przykład skóra. No, zrobisz to?");

		quest.task()
			.requestItem(1, "skórzany hełm");

		quest.complete()
			.greet("Hej! Czy ta skórzana czapka jest dla mnie?")
			.respondToReject("Chyba ktoś bardziej szczęśliwy dziś dostanie swoją czapkę... *kichnięcie*")
			.respondToAccept("Błogosławię cię, mój dobry przyjacielu! Teraz moja głowa będzie ładnie ciepła.")
			.rewardWith(new IncreaseXPAction(50))
			.rewardWith(new IncreaseKarmaAction(10));

			return quest;
	}

	public boolean removeFromWorld() {
		final boolean res = ResetSpeakerNPC.reload(new GreeterNPC(), "Monogenes");
		// reload other quests associated with Monogenes
		SingletonRepository.getStendhalQuestSystem().reloadQuestSlots("Monogenes");
		return res;
	}
}