/***************************************************************************
 *                   (C) Copyright 2022 - PolanieOnLine                    *
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

import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.quest.BringItemTask;
import games.stendhal.server.entity.npc.quest.QuestBuilder;
import games.stendhal.server.entity.npc.quest.QuestManuscript;
import games.stendhal.server.maps.Region;

public class HelpOgi implements QuestManuscript {
	public QuestBuilder<?> story() {
		QuestBuilder<BringItemTask> quest = new QuestBuilder<>(new BringItemTask());

		quest.info()
			.name("Rozpalenie Ogniska")
			.description("Nieco zamarznięty Ogi prosi o pomoc z rozpaleniem jego ogniska w lesie.")
			.internalName("help_ogi")
			.repeatable(true)
			.repeatableAfterMinutes(0)
			.region(Region.TATRY_MOUNTAIN)
			.questGiverNpc("Ogi");

		quest.history()
			.whenNpcWasMet("Napotkany został Ogi między ścieżką a lasem w paśmie górskim.")
			.whenQuestWasRejected("Szkoda mojej energii na jego ognisko.")
			.whenQuestWasAccepted("Zrobiło mi się szkoda biednego Ogiego także przyniosę coś co pomoże rozpalić zamarznięte ognisko.")
			.whenTaskWasCompleted("Mam potrzebne elementy do rozpalenia ogniska.")
			.whenQuestWasCompleted("Nieudało nam się rozpalić ogniska, lecz Ogi podziękował mi za moją chęć pomocy.")
			.whenQuestCanBeRepeated("Muszę spróbować jeszcze raz z rozpaleniem ogniska!");

		quest.offer()
			.respondToRequest("Ppo...po...trz...ebuję pp...omocy zzz ogg...iskiem. Ppp...omożesz?")
			.respondToUnrepeatableRequest("Dz...dz...ęku...ję, pp...przy...najmniej pr...pr...óbowaliśmy.")
			.respondToRepeatedRequest("M...asz może cz...cz... ym roz...zz...zpa...palić oog...gg...isko? Ppp...omożesz?")
			.respondToAccept("S...ss...u...u...per! C...c...oś z...z...z ognia si...się nada.")
			.respondToReject("Mm...mu...szę ch...chy...ba pp...po...szukać w...wio...s...ss...ki.")
			.rejectionKarmaPenalty(5.0)
			.remind("C...c...oś z...z...z ognia si...się nada.");

		quest.task()
			.requestItem(1, "miecz ognisty")
			.alternativeItem(1, "ognista tarcza")
			.alternativeItem(1, "ognista zbroja")
			.alternativeItem(1, "ogniste spodnie")
			.alternativeItem(1, "ogniste rękawice")
			.alternativeItem(1, "ogniste buty")
			.alternativeItem(25, "magia płomieni");

		quest.complete()
			.greet("C... c... oś mm... ma... sz?")
			.respondToReject("Mm... mu... szę ch... chy... ba pp... po... szukać w... wio... s... ss... ki.")
			.respondToAccept("S... ss... u... u... per! K... kur... rczę... nie ud... ało się rozp... pp... alić nn... nam...")
			.rewardWith(new IncreaseXPAction(500))
			.rewardWith(new IncreaseKarmaAction(5));

		return quest;
	}
}
