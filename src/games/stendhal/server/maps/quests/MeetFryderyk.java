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
// Based on MeetMonogomes
package games.stendhal.server.maps.quests;

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

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

/**
 * QUEST: Speak with Monogenes PARTICIPANTS: - Fryderyk
 *
 * STEPS: - Talk to Fryderyk to activate the quest and keep speaking with
 * Fryderyk. - Be polite and say "bye" at the end of the conversation to get a
 * small reward.
 *
 * REWARD: broken (- 10 XP (check that user's level is lesser than 2) - No money)
 *
 * REPETITIONS: - None
 *
 */
public class MeetFryderyk extends AbstractQuest {
	@Override
	public String getSlotName() {
		return "meetFryderyk";
	}
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Spotkanie Fryderyka",
				"Słuchaj uważnie mądrego starego człowieka w Zakopanem. Jego mapa może ci pomóc w poruszaniu się po miasteczku.",
				false);
		final SpeakerNPC npc = npcs.get("Fryderyk");

		npc.addGreeting(null, new SayTextAction("Witaj ponownie [name]. W czym mogę #pomóc tym razem?"));

		// A little trick to make NPC remember if it has met
		// player before and react accordingly
		// NPC_name quest doesn't exist anywhere else neither is
		// used for any other purpose
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new GreetingMatchesNameCondition(npc.getName()),
						new QuestNotCompletedCondition("Fryderyk")),
				ConversationStates.INFORMATION_1,
				"Witaj nieznajomy! Nie bądź zbyt onieśmielony, gdy ludzie siedzą cicho lub są zajęci... " +
				"strach przed zbójami i ich hersztem padł na całe Zakopane. Jesteśmy " +
				"trochę zaniepokojeni. Mogę dać Tobie trochę rad odnośnie zawierania przyjaźni. Chciałbyś je usłyszeć?",
				new SetQuestAction("Fryderyk", "done"));

		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.HELP_MESSAGES,
			null,
			ConversationStates.INFORMATION_1,
			"Mogę Tobie udzielić kilka rad o sposobie prowadzenia rozmów z mieszkańcami Zakopanego. Oczywiście, jeżeli chcesz?",
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
			"W każdym razie czy chcesz usłyszeć krótki opis budynków w Zakopanem?",
			null);

		npc.add(
			ConversationStates.INFORMATION_1,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.IDLE,
			"I jak chcesz wiedzieć co się dzieje? Czytając Zakopane Tribune Tutki? Hah!" +
			" Zważaj także uwagę na znaki to może coś sie z nich dowiesz się(Małe drewniane tabliczki)... Dowidzenia!",
			null);

		final List<String> yesnotriggers = new ArrayList<String>();
		yesnotriggers.addAll(ConversationPhrases.YES_MESSAGES);
		yesnotriggers.addAll(ConversationPhrases.NO_MESSAGES);

		npc.add(
				ConversationStates.INFORMATION_1,
				"",
				new NotCondition(new TriggerInListCondition(yesnotriggers)),
				ConversationStates.INFORMATION_1,
				"Zapytałem Ciebie 'tak' lub 'nie' na pytanie: Mogę dać ci kilka podpowiedzi. Chcesz je usłyszeć?",
			null);

		// he puts 'like this' into blue and so many people try that first
		npc.addReply(
				Arrays.asList("like this", "jak to"),
				"To prawda jak to! Teraz mogę ci pokazać #mapę lub kierunek do #banku, #biblioteki, #oberży, #świątyni, #kuźni, #piekarni lub starej #wioski.");

		npc.addReply(
			Arrays.asList("buildings", "budynki","budynków"),
			"Mogę Ci pokazać mapę lub powiedzieć jak trafić do miejsc takich jak: #bank, #kuźnia, #szpital, #burmistrz Zakopanego, #boisko, jak dojść do #krakowa, #dom #gier oraz #kościelisko.");

		npc.add(
			ConversationStates.ATTENDING,
			"mapa", null, ConversationStates.ATTENDING, "Mapa miasta Zakopane\n"
			+ "1 Domek, tu stawiamy pierwsze kroki,   2 Bank,   3 Kuźnia,   4 Boisko,\n"
			+ "A do burmistrza,   B szpital C dom gier, \n"
			+ "D Długa droga, cały czas prosto do Krakowa, \n"
			+ "E Zachód kierunek kościelisko",
			new ExamineChatAction("map-zakopane-city.png", "Miasto Zakopane", "Mapa miasta Zakopane"));

		npc.addReply(
			Arrays.asList("bank", "Bank"),
			"Wyjdziesz z tego budynku i patrząc na wschód#(prawo #na #mapie) widzisz małą rzeczkę oraz most, przez który możesz iść. Gdy go przejdziesz będziesz koło boiska gdzie będzie chodził Herold, z którym możesz zamienić parę słów, jeżeli masz ochotę rzecz jasna. Idąc dalej na wschód#(prawo #na #mapie) zobaczysz budynek, który będzie miał z boku żółty napis #BANK. To jest budynek, o który pytałeś przed chwilą.");

		npc.addReply(
			Arrays.asList("szpital", "Szpital"),
			"Skieruj się na północ, lecz nie wchodź na most, tylko skieruj się w górę po prawej stronie mostu. Zobaczysz rzekę skieruj się wydłuż niej na północ po czasie zobaczysz, iż rzeka się kończy, lecz po prawo masz budynek z czerwonym krzyżem. Tak, właśnie tak jak już się domyśliłeś to szpital.");

		npc.addReply(
			Arrays.asList("burmistrz Zakopanego", "Burmistrz Zakopanego"),
			"Gazdę Wojtka znajdziesz nad boiskiem w zakopanem:)");

		npc.addReply(
			Arrays.asList("dom gier", "Dom gier"),
			"Idąc na północ wymijasz most z prawej strony potem wciąż idąc na wschód trafiasz na małą rzeczkę, przez którą prowadzi most. Przekrocz most i pokieruj się na północ natrafisz na mały domek, który jest domem gier.");

		npc.addReply(
			Arrays.asList("boisko", "Boisko"),
			"Już opuszczając ten budynek powinieneś go dostrzec na mini mapce. Boisko znajduje się na wschód z stąd za małą rzeczką. Herold, który ta chodzi zajmuje się wystawianiem znaków.");

		npc.addReply(
			Arrays.asList("kuźnia", "Kuźnia"),
			"Wychodząc stąd pokierujesz się na północny-zachód, aby dotrzeć do kowala. Nad drzwi wisi obrazek z kowadłem. Powinieneś trafić bez większego problemu.");

		npc.addReply(
			Arrays.asList("kościelisko", "Kościelisko"),
			"Wychodząc z tego budynku po lewej masz ścieżkę. Idą nią na południe dojdziesz do kościeliska Se. Lecz gdy pójdziesz na zachód natrafisz koło rzeki na ścieżkę, która zaprowadzi cię na kościelisko E. Idą dalej po niej dojdziesz do kościeliska Ne.");

		npc.addReply(
			Arrays.asList("kraków", "Kraków"),
			"Idź na północ przekrocz most i skieruj się lewą stroną torów aż do przejścia, które przechodzisz naturalnie i dalej idziesz na północ jeszcze spory kawałek.  Natrafisz na ścieżkę, która będzie prowadziła na wchód i zachód. Skieruj się na wschód i idź nią cały czas bez ustanku aż dojdziesz do mostu. Potem już od ciebie zależy gdzie pójdziesz dalej.");

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
		// player.addXP(10);
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
		return "MeetFryderyk";
	}

	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest("Fryderyk")) {
				return res;
			}
			if (isCompleted(player)) {
				res.add("Rozmawiałem z Fryderykiem i on zaproponował mi mapę. Zawsze mogę spytać się jego o mapę i ją dostanę.");
			}
			return res;
	}
	@Override
	public String getNPCName() {
		return "Fryderyk";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
