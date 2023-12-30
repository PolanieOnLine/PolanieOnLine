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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.spawner.PassiveEntityRespawnPoint;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.BringItemQuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Plink's Toy
 * <p>
 * PARTICIPANTS: <ul><li> Plink <li> some wolves </ul>
 *
 * STEPS: <ul><li> Plink tells you that he got scared by some wolves and ran away
 * dropping his teddy. <li> Find the teddy in the Park Of Wolves <li> Bring it back to
 * Plink </ul>
 *
 * REWARD: <ul><li> a smile <li> 20 XP <li> 10 Karma </ul>
 *
 * REPETITIONS: <ul><li> None. </ul>
 */
public class PlinksToy implements QuestManuscript {
	@Override
	public BringItemQuestBuilder story() {
		BringItemQuestBuilder quest = new BringItemQuestBuilder();

		quest.info()
			.name("Zabawka Plinka")
			.description("Plink to słodki mały chłopiec, i jak wiele małych chłopców, boi się wilków.")
			.internalName("plink_toy")
			.notRepeatable()
			.minLevel(0)
			.region(Region.SEMOS_SURROUNDS)
			.questGiverNpc("Plink");

		quest.history()
			.whenNpcWasMet("Spotkałem Plinka.")
			.whenQuestWasRejected("Nie chcę szukać pluszaka Plinka.")
			.whenQuestWasAccepted("Plink błagał mnie, żebym poszukał jego misia w ogrodzie pełnym wilków.")
			.whenTaskWasCompleted("Znalazłem pluszowego misia Plinka.")
			.whenQuestWasCompleted("Oddałem misia Plinkowi, a on był bardzo szczęśliwy.");

		quest.offer()
			.begOnGreeting("*płacze* W parku były wilki! *pociągnięcie nosem* Uciekłem, ale upuściłem mojego pluszaka! Czy możesz go dla mnie znaleźć? *pociągnięcie nosem* Proszę?")
			.respondToRequest("*płacze* W parku były wilki! *pociągnięcie nosem* Uciekłem, ale upuściłem mojego pluszaka! Czy możesz go dla mnie znaleźć? *pociągnięcie nosem* Proszę?")
			.respondToUnrepeatableRequest("Znalazłeś mojego pluszaka niedaleko wilków! Wciąż go ściskam i przytulam :)")
			.respondToAccept("*pociągnięcie nosem* Dziękuję bardzo! *uśmiech*")
			.respondToReject("*pociągnięcie nosem* Ale... ale... PROSZĘ! *płacze*")
			.rejectionKarmaPenalty(10.0)
			.remind("Zgubiłem mojego pluszaka w parku na wschód, tam gdzie kręcą się wilki.")
			.respondTo("wolf", "wolves", "wilk").saying("Przyszły z równin i teraz kręcą się wokół parku na wschód trochę dalej. Nie wolno mi się do nich zbliżać, są niebezpieczne.")
			.respondTo("park").saying("Rodzice mi mówili, żebym nie chodził do parku sam, ale zgubiłem się, gdy się bawiłem... Proszę, nie mów im! Czy możesz przynieść mi mojego pluszaka z powrotem?")
			.respondTo("teddy", "pluszak", "miś").saying("Pluszak to moja ulubiona zabawka! Czy możesz go dla mnie przynieść?");

		final SpeakerNPC npc = NPCList.get().get("Plink");
		npc.addReply(Arrays.asList("wolf", "wolves", "wilk"), "Przyszły z równin i teraz kręcą się wokół parku na wschód trochę dalej. Nie wolno mi się do nich zbliżać, są niebezpieczne.");
		npc.addReply("park", "Rodzice mi mówili, żebym nie chodził do parku sam, ale zgubiłem się, gdy się bawiłem... Proszę, nie mów im!");
		npc.addReply(Arrays.asList("teddy", "miś"), "Pluszak to moja ulubiona zabawka! Proszę, przynieś go mi z powrotem.");

		quest.task()
	    	.requestItem(1, "pluszowy miś");
		step2();

		quest.complete()
	    	.greet("Znalazłeś mojego misia! Proszę, proszę, czy mogę go odzyskać?")
	    	.respondToReject("*pociągnięcie nosem*")
	    	.respondToAccept("*ściska pluszaka* Dziękuję, dziękuję! *uśmiech*")
			.rewardWith(new IncreaseXPAction(20))
			.rewardWith(new IncreaseKarmaAction(10));

		return quest;
	}

	private void step2() {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("0_semos_plains_n");
		final PassiveEntityRespawnPoint teddyRespawner = new PassiveEntityRespawnPoint("pluszowy miś", 1500);
		teddyRespawner.setPosition(107, 84);
		teddyRespawner.setDescription("Na piasku jest odcisk pluszowego misia.");
		zone.add(teddyRespawner);

		teddyRespawner.setToFullGrowth();
	}
}