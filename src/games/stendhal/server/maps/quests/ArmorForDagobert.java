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

import java.util.Arrays;

import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.BringItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Armor for Dagobert
 *
 * PARTICIPANTS:
 * <ul>
 * <li>Dagobert, the consultant at the bank of Semos</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li>Dagobert asks you to find a leather cuirass.</li>
 * <li>You get a leather cuirass, e.g. by killing a cyclops.</li>
 * <li>Dagobert sees your leather cuirass and asks for it and then thanks you.</li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li>50 XP</li>
 * <li>80 gold</li>
 * <li>Karma: 10</li>
 * <li>Access to vault</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class ArmorForDagobert implements QuestManuscript {
	@Override
	public BringItemQuestBuilder story() {
		BringItemQuestBuilder quest = new BringItemQuestBuilder();

		quest.info()
			.name("Zbroja Dagoberta")
			.description("Dagobert, konsultant w banku Semos, potrzebuje ochrony.")
			.internalName("armor_dagobert")
			.notRepeatable()
			.minLevel(0)
			.region(Region.SEMOS_CITY)
			.questGiverNpc("Dagobert");

		quest.history()
			.whenNpcWasMet("W banku napotkany został Dagobert. Jest konsultantem w banku w Semos.")
			.whenQuestWasRejected("Poprosił mnie o znalezienie skórzanego kirysu, ale nie zamierzam pomagać z jego prośbą.")
			.whenQuestWasAccepted("Znajdę dla niego skórzany kirys, ponieważ został okradziony.")
			.whenTaskWasCompleted("Znalazł się skórzany kirys i niosę już go Dagobertowi.")
			.whenQuestWasCompleted("Skórzany kirys został zwrócony Dagobertowi. W ramach drobnej wdzięczności pozwoli mi skorzystać z prywatnego skarbca.");

		quest.offer()
			.respondToRequest("Tak bardzo boję się, że zostanę okradziony podczas jakiegoś napadu. Nie mam żadnej ochrony. Myślisz, że jesteś w stanie mi pomóc?")
			.respondToUnrepeatableRequest("Dziękuję bardzo za zbroję, ale nie mam dla ciebie innego zadania.")
			.respondToAccept("Kiedyś miałem fajny #'skórzany kirys', ale uległ zniszczeniu podczas ostatniego napadu. Jeśli znajdziesz nowy, dam ci nagrodę.")
			.respondToReject("Cóż, w takim razie chyba po prostu schylę się i ukryję.")
			.remind("Na szczęście nie okradziono mnie, kiedy cię nie było. Chętnie przyjmę skórzany kirys. Tak czy inaczej, jak mogę #pomóc?");

		SpeakerNPC npc = NPCList.get().get("Dagobert");
		npc.addReply(Arrays.asList("leather cuirass", "leather", "cuirass"), "Skórzany kirys to tradycyjna zbroja cyklopa. Niektórzy cyklopi żyją w lochach głęboko pod miastem.");

		quest.task()
			.requestItem(1, "skórzany kirys")
			.alternativeItem(1, "skórzany kirys z naramiennikami");

		quest.complete()
			.greet("Przepraszam! Zauważyłem skórzany kirys, który nosisz. Czy to dla mnie?")
			.respondToReject("No cóż, mam nadzieję, że znajdziesz inny i możesz mi go dać, zanim znowu zostanę obrabowany.")
			.respondToAccept("Och, jestem bardzo wdzięczny! Oto trochę złota, które znalazłem… ehm… gdzieś. Masz moją dozgodną wdzięczność oraz zaufanie, w nagrodę teraz możesz mieć dostęp do własnego prywatnego skarbca, kiedy tylko chcesz.")
			.rewardWith(new EquipItemAction("money", 80))
			.rewardWith(new IncreaseXPAction(50))
			.rewardWith(new IncreaseKarmaAction(10));

		return quest;
	}
}