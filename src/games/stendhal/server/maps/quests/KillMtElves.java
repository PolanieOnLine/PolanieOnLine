/***************************************************************************
 *                   (C) Copyright 2003-2024 - PolanieOnLine               *
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

import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.KillCreaturesQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

public class KillMtElves implements QuestManuscript {
	@Override
	public KillCreaturesQuestBuilder story() {
		KillCreaturesQuestBuilder quest = new KillCreaturesQuestBuilder();

		quest.info()
			.name("Zguba Górskich Elfów")
			.description("Po oczyszczeniu wieży Czarnoksiężnik ujawnia kolejny problem związany z górskimi elfami.")
			.internalName("kill_mountain_elves")
			.notRepeatable()
			.minLevel(70)
			.region(Region.ZAKOPANE_CITY)
			.questGiverNpc("Czarnoksiężnik");

		quest.history()
			.whenNpcWasMet("Po oczyszczeniu starej wieży wróciłem do Czarnoksiężnika.")
			.whenQuestWasRejected("Odmówiłem udziału w konflikcie Czarnoksiężnika z górskimi elfami.")
			.whenQuestWasAccepted("Czarnoksiężnik poprosił mnie o pokonanie grup górskich elfów, które odcięły mu dostęp do terenów potrzebnych do dalszych badań.")
			.whenTaskWasCompleted("Pokonałem wskazane przez Czarnoksiężnika górskie elfy. Mogę wrócić po dalsze wyjaśnienia.")
			.whenQuestWasCompleted("Czarnoksiężnik uznał drugą próbę za zakończoną i dał mi hełm kolczy, który ma się przydać przy jego ostatnim doświadczeniu.");

		quest.offer()
			.offerState(ConversationStates.QUEST_2_OFFERED)
			.respondToPreconditionIssue("Najpierw oczyść moją starą wieżę. Nie będę omawiał z tobą poważniejszych spraw, dopóki nie zobaczę, że potrafisz dokończyć pierwsze zadanie.")
			.respondToRequest("Poradziłeś sobie z wieżą, więc mogę powiedzieć więcej. Górskie elfy odcięły mi dostęp do miejsc potrzebnych do badań i nie zamierzają ustąpić. Chcę, żebyś przełamał ich opór. Podejmiesz się tego?")
			.respondToUnrepeatableRequest("Sprawa górskich elfów jest już zakończona. Teraz interesuje mnie ostatni etap moich badań.")
			.respondToAccept("Dobrze. Pokonaj wskazane grupy górskich elfów i wróć do mnie, gdy droga będzie znowu otwarta.")
			.respondToReject("Rozumiem. Bez twojej pomocy będę musiał znaleźć inny sposób na odzyskanie dostępu do tych terenów.")
			.rejectionKarmaPenalty(15.0)
			.acceptWith(new IncreaseKarmaAction(10.0))
			.remind("Droga nadal nie jest bezpieczna. Pokonaj wszystkie grupy górskich elfów, o których ci mówiłem.");

		quest.task()
			.requestKill(3, "elf górski maskotka")
			.requestKill(1, "elf górski dama")
			.requestKill(1, "elf górski strażniczka")
			.requestKill(1, "elf górski kapłan")
			.requestKill(1, "elf górski czarownica")
			.requestKill(1, "elf górski lider")
			.requestKill(2, "elf górski lord")
			.requestKill(1, "elf górski czarnoksiężnik")
			.requestKill(1, "elf górski wojownik")
			.requestKill(1, "elf górski służka")
			.requestKill(1, "elf górski król")
			.requestKill(1, "elf górski królowa")
			.requireCompletedQuest("clear_tower");

		quest.complete()
			.greet("Dobrze. Droga jest znowu otwarta. Przyjmij ten hełm kolczy. Nie jest przypadkową nagrodą, będzie ci potrzebny, jeśli zgodzisz się pomóc mi przy ostatnim doświadczeniu.")
			.rewardWith(new EquipItemAction("hełm kolczy", 1, true))
			.rewardWith(new IncreaseXPAction(20000))
			.rewardWith(new IncreaseKarmaAction(20.0));

		return quest;
	}
}
