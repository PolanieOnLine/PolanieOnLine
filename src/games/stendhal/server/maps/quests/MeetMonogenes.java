/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.ExamineChatAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: Speak with Monogenes PARTICIPANTS: - Monogenes
 * 
 * STEPS: - Talk to Monogenes to activate the quest and keep speaking with
 * Monogenes. - Be polite and say "bye" at the end of the conversation to get a
 * small reward.
 * 
 * REWARD: broken (- 100 XP (check that user's level is lesser than 2) - No money)
 * 
 * REPETITIONS: - None
 * 
 */
public class MeetMonogenes extends AbstractQuest {
	@Override
	public String getSlotName() {
		return "Monogenes";
	}
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Spotkanie Monogenesa",
				"Słuchaj uważnie mądrego starego człowieka w Semos. Jego mapa może ci pomóc w poruszaniu się po miasteczku.",
				false);
		final SpeakerNPC npc = npcs.get("Monogenes");

		npc.addGreeting(null, new SayTextAction("Witaj ponownie [name]. W czym mogę #pomóc tym razem?"));
		
		// A little trick to make NPC remember if it has met
        // player before and react accordingly
        // NPC_name quest doesn't exist anywhere else neither is
        // used for any other purpose
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotCompletedCondition("Monogenes")), 
				ConversationStates.INFORMATION_1, 
				"Witaj nieznajomy! Nie bądź zbyt onieśmielony, gdy ludzie siedzą cicho lub są zajęci... " +
				"strach przed Blordroughtem i jego wojskami padł na cały kraj. Jesteśmy " +
				"trochę zaniepokojeni. Mogę dać Tobie trochę rad odnośnie zawierania przyjaźni. Chciałbyś je usłyszeć?",
				new SetQuestAction("Monogenes", "done"));
				
		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.HELP_MESSAGES,
			null,
			ConversationStates.INFORMATION_1,
			"Mogę udzielić Tobie kilka rad o sposobie prowadzenia rozmów z mieszkańcami Semos. Jeżeli chcesz?",
			null);

		npc.add(
			ConversationStates.INFORMATION_1,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Po pierwsze powinieneś się przywitać mówiąc \"cześć\". Później spróbuj podtrzymać rozmowę poruszając odpowiednie " +
			"tematy, które są podświetlone #jak #to. Zazwyczaj " +
			"bezpiecznymi tematami rozmowy jest osobista #praca, pytanie o #pomoc, zapytanie czy nie mają " +
			"jakiejś #oferty i pytanie o #zadanie do zrobienia. " +
			"W każdym razie czy chcesz usłyszeć krótki opis budynków w Semos?",
			null);

		npc.add(
			ConversationStates.INFORMATION_1,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"I jak chcesz wiedzieć co się dzieje? Czytając Semos Tribune? Hah! Dowidzenia.",
			null);
		
		final List<String> yesnotriggers = new ArrayList<String>();
		yesnotriggers.addAll(ConversationPhrases.YES_MESSAGES);
		yesnotriggers.addAll(ConversationPhrases.NO_MESSAGES);
		
		npc.add(
				ConversationStates.INFORMATION_1,
				"",
				new NotCondition(new TriggerInListCondition(yesnotriggers)),
				ConversationStates.INFORMATION_1,
				"Zapytałem Ciebie 'tak lub nie' na pytanie: Mogę dać ci kilka podpowiedzi. Chcesz je usłyszeć?",
			null);

		// he puts 'like this' into blue and so many people try that first
		npc.addReply(
				Arrays.asList("like this", "jak to"),
				"To prawda jak to! Teraz mogę ci pokazać #mapę lub kierunek do #banku, #biblioteki, #oberży, #świątyni, #kuźni, #piekarni lub starej #wioski.");
		
		npc.addReply(
			Arrays.asList("buildings", "budynki"),
			"Mogę pokazać Tobie #mapę lub pokazać gdzie jest #bank, #biblioteka, #oberża, #świątynia, #kowal, #piekarnia, #publiczna #skrzynia lub gdzie jest stara #wioska.");

		npc.add(
			ConversationStates.ATTENDING,
			"mapa", null, ConversationStates.ATTENDING,
			"Zaznaczyłem poniższe lokalizacje na mojej mapie:\n"
			+ "1 Ratusz, mieszka tam Burmistrz,   2 Biblioteka,   3 Bank,   4 Magazyn,\n"
			+ "5 Piekarnia,   6 Kowal, Carmen,   7 Hotel, Margaret \n"
        	+ "8 Świątynia, Ilisa,   9 Niebezpieczne Podziemia, \n"
        	+ "10 Publiczna Skrzynia, \n"
        	+ "A Wioska Semos,   B Północne Równiny i Kopalnia, \n"
        	+ "C Długa droga do Ados, \n"
        	+ "D Południowe Równiny i Las Nalwor, \n"
        	+ "E Otwarte Tereny Wioski Semos",
        	new ExamineChatAction("map-semos-city.png", "Miasto Semos", "Mapa miasta Semos"));

		npc.addReply(
			Arrays.asList("bank", "banku"),
			"Widzisz ten wielki budynek na przeciwko mnie z wielką atrapą skrzyni skarbów? To jest to tam. Pewnie pomyślałeś, że to oczywiste.");

		npc.addReply(
			Arrays.asList("library", "biblioteka", "biblioteki"),
			"Idź ścieżką stąd na zachód, a zobaczysz budynek z dwoma wejściami i szyldem na którym widać pióro oraz książkę.");

		npc.addReply(
			Arrays.asList("tavern", "oberża", "oberży"),
			"Trzymając się ścieżki idź na południowy-wschód. Nie możesz jej przegapić. Ma duży znak z napisem INN.");

		npc.addReply(
			Arrays.asList("temple", "świątynia", "świątyni"),
			"Świątynia jest na południowy-wschód stąd za oberżą ( #tavern ). Na dachu znajduje się charakterystyczny krzyż.");

		npc.addReply(
			Arrays.asList("bakery", "piekarnia", "piekarni"),
			"Nasza lokalna piekarnia jest we wschodniej części placu. Mają szyld z rysunkiem przedstawiającym bochenek chleba.");

		npc.addReply(
			Arrays.asList("blacksmith", "kowal", "kowala", "kuźni"),
			"Prosto na południowy-zachód, aby dostać się do kowala. Obok drzwi wisi obrazek z kowadłem. Powinieneś zauważyć.");

		npc.addReply(Arrays.asList("public", "public chest", "community chest", "chest"),
			"Podążaj za pomarańczową ścieżką na mapie, aby dotrzeć do publicznej skrzyni. Mieszkańcy Faiumoni i odważni wojownicy wrzucają trochę przydatnych przedmiotów, któe można wziąć za darmo. Pamiętaj: Zawsze dobrze jest się podzielić i dać to, czego już więcej nie potrzebujesz.");
		
		npc.addReply(
			Arrays.asList("village", "wioska", "wioski"),
			"Prosto na południowy-zachód, miń #kowala, a szybko dojdziesz do starej wioski Semos. Nishiya wciąż tam sprzedaje owce.");


		/** Give the reward to the polite newcomer user */
		// npc.add(ConversationStates.ATTENDING,
		// SpeakerNPC.GOODBYE_MESSAGES,
		// null,
		// ConversationStates.IDLE,
		// null,
		// new SpeakerNPC.ChatAction() {
		// @Override
		// public void fire(Player player, Sentence sentence, SpeakerNPC engine) {
		// if (player.getLevel() < 2) {
		// engine.say("Goodbye! I hope I was of some use to you.");
		// player.addXP(100);
		// player.notifyWorldAboutChanges();
		// } else {
		// engine.say("I hope to see you again sometime.");
		// }
		// }
		// });
		npc.addGoodbye();
	}

	@Override
	public String getName() {
		return "MeetMonogenes";
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest("Monogenes")) {
				return res;
			}
			if (isCompleted(player)) {
				res.add("Rozmawiałem z Monogenes i on zaproponował mi mapę. Zawsze mogę spytać się jego o mapę i ją dostanę.");
			} 
			return res;
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}
	@Override
	public String getNPCName() {
		return "Monogenes";
	}
}
