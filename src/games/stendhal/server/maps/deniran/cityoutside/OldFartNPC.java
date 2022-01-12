/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.deniran.cityoutside;

import java.awt.Color;
import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.behaviour.impl.MonologueBehaviour;

/**
 * This old fart sits on a bench in Deniran. From eating cabbage soup he has
 * clearly audible flatulence. One can even smell it from afar. He's named after
 * Louis de Funès' character in the movie "The Cabbage Soup" and the French word
 * for storyteller. He's not interested in what one tells him and only drops
 * hints to stories if someone speaks to him. That's also the reason why he does
 * not insist in receiving a yes or no answer. His irritating behaviour is
 * intended. It's unclear if the old fart is somehow senile or is making fun of
 * players. Some stories he talks about are from the game others just made up or
 * old stories, songs, nursery rhymes or from pop culture.
 * 
 * MonologueBehaviour could be combined with real fart sounds in the future.
 * There could be another old fart sitting near this NPC who somehow reacts on
 * the farts (and maybe the stories.) If that one is ever added, the stories
 * might move to a different NPC somewhere else.
 * 
 * @author kribbel
 */
public class OldFartNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final String[] farts = { "!me odpuszcza", "!me pozwala jednemu rozerwać tak bardzo, że wstrząsa domem", "!me przełamuje wiatr",
				"!me pozwala mu się rozerwać", "!me kroi ser", "!me rozwiązuje zagwozdkę" };
		new MonologueBehaviour(buildNPC(zone), farts, 1);
	}

	private SpeakerNPC buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC oldFart = new SpeakerNPC("Claude Conteur") {
			@Override
			protected void createPath() {
				setPath(null);
			}

			@Override
			protected void createDialog() {
				addGreeting("O, cześć!");
				add(ConversationStates.ATTENDING,
						Arrays.asList("help", "job", "work", "offer", "task", "quest", "story", "stories", "pomoc", "praca", "oferta", "zadanie", "historia"),
						ConversationStates.ATTENDING, null, new MultipleActions(
								new SayTextAction("Czy słyszałeś kiedyś historię o "), new SayTextAction(stories)));
				add(ConversationStates.ATTENDING, ConversationPhrases.YES_MESSAGES, null, ConversationStates.IDLE, null,
						new MultipleActions(new SayTextAction(
								"Naprawdę!? To świetnie, bo o tym zapomniałem. Powinieneś iść i to zapisać."),
								new SayTextAction("!me chichocze i nie zwraca na ciebie uwagi.")));
				add(ConversationStates.ATTENDING, ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE, null,
						new MultipleActions(new SayTextAction("Ja też nie."),
								new SayTextAction("!me chichocze i nie zwraca na ciebie uwagi.")));
				addGoodbye("!me nie zwraca na ciebie uwagi.");
				// the following is just in case someone happens to ask
				add(ConversationStates.ATTENDING,
						Arrays.asList("unpleasant", "smell", "stench", "air", "fart", "farting", "magic", "magician",
								"sorcery", "sorcerer", "witch", "witchery", "witchcraft", "joke", "fun", "smrud",
								"zapach", "powietrze", "magia", "wiedzma", "czarownik", "żart"),
						ConversationStates.ATTENDING, null,
						new MultipleActions(
								new SayTextAction("Wiesz, jestem potężnym czarownikiem. Potrafię sprawić, by powietrze pachniało."),
								new SayTextAction("!me chichocze i wyraźnie się cieszy."),
								new SayTextAction("Poczuj magiczną moc kapuścianej #zupy.")));
				addReply(Arrays.asList("cabbage", "kapusta", "kapuścianej"),
						"Och, nigdzie nie znajdziesz kapusty. Ale mam tajnego, "
								+ "niezawodnego dostawcę daleko na północy. Dlatego nigdy nie muszę się bać, "
								+ "że zabraknie kapuścianej #zupy.");
				add(ConversationStates.ATTENDING, Arrays.asList("soup", "zupa", "zaupy"), null, ConversationStates.ATTENDING, null,
						new MultipleActions(new SayTextAction("Zupa z #kapusty jest taka pyszna."),
								new SayTextAction("!me oblizuje usta i pociera brzuch."),
								new SayTextAction("Jem to codziennie.")));
				add(ConversationStates.ATTENDING, Arrays.asList("French", "France", "français", "francais", "Francja", "francuski"),
						ConversationStates.ATTENDING, null, new SayTextAction("Je ne parle pas français!"));
			}

			// Make the NPC not turn to player when talking to show the lack of interest
			@Override
			public void say(final String text) {
				say(text, false);
			}
		};
		oldFart.setOutfit(0, 80, 0, 3, 6, 0, 46, 0, 0);
		oldFart.setOutfitColor("dress", Color.GREEN);
		oldFart.setDescription("Oto Claude Conteur, starszy mieszkaniec Deniran, który odpoczywa na ławeczce.");
		oldFart.setGender("M");
		oldFart.setPosition(46, 11);
		oldFart.setIdleDirection(Direction.DOWN);
		zone.add(oldFart);
		return oldFart;
	}

	String[] stories = { "krasnoludzie, który wpadł do studni życzeń?", // made up
			"ogrze, który znalazł rybny placek w wydrążonym drzewie?", // made up
			"gnomie, który błagał wiedźmę, by zmieniła go w potężnego słonia?", // made up
			"pandach, które planowały zawładnąć światem?", // made up
			"karzełeku, który zapomniał, gdzie ukrył swój garnek złota?", // made up
			"dzielnym kurczaku, który zabił bohatera kradnącego jajka?", // made up
			"bohaterach, którzy weszli do jaskiń Deniran i nic nie znaleźli?", // made up
			"owcach, którzy utworzyli Front Wyzwolenia owiec Faiumoni?", // made up
			"zgniłym zombie, który spał na Bagnach Ados?", // made up
			"powstaniu i upadeku królestwa szczurołaków?", // made up
			"zabójcach, którzy potajemnie przejęli całe imperium Deniran, nie zauważalnie przez nikogo?", // made up
			"łuczniku madaramowym, który zdołał strzelić sobie strzałą w tyłek?", // made up
			"starszym ogrze, za którym stale podążał żółw, który usiłował ugryźć go w duży palec u nogi?",
			// made up
			"bezgłowym potwórze, który zakochał się w mózgu elfa czarnoksiężnika?", // made up
			"niezwykłym bohaterze, któremu udało się zrobić kanapkę z tuńczykiem?",
			// made up, you can't make but only buy one
			"bandzie orków z Kotoch, która próbowała przeprawić się zimą przez góry Ados?", // made up
			"żywiołaku ognia i żywiołaku lodu, którzy poszli na szkolny program wymiany między rodzinami?", // made up
			"wielkiej wojna Drzewców z bobrami zapoczątkowana przez bobry spiętrzające potok i zapuszczające drzewce pod wodę?", // made up
			"żołnierzach, którzy weszli do Semos Dungeon i nigdy więcej ich nie widziano?", // Stendhal Kanmararn Soldiers
			"barbarzyńcy, który wykopał tunel pod oceanem?", // Stendhal Lorenz
			"bohaterze, który stał się wiecznym więźniem demonów piekła?", // Stendhal Tomi
			"małej dziewczynce uprowadzonej w drodze do domu z przyjęcia, którą dwa lata później znaleziono na statku w porcie Ados?", // Stendhal Susi
			"Lordzie Lichester, który zbudował Zamek Or'ril?", // Stendhal
			"władcy wampirów jak trafił do Semos?", // Stendhal
			"stateku, który zatonął u wybrzeży Athor?", // Stendhal
			"kanibalu, który nosił plemienną maskę wyglądającą jak wielka cytryna?", // Game: Monkey Island
			"bohaterze, który nauczył się latać, czytając książkę zrób to sam w bibliotece?", // Game: Indiana Jones 3
			"błazeneku, który wraz z pokolcem przeszukał cały ocean w poszukiwaniu syna?",
			// movie: Finding Nemo
			"amulecie, że ktokolwiek go nosił, był w stanie zrozumieć język wszystkich zwierząt?",
			// changed old story, variation of The White Snake
			"pięknej dziewczynie, która zaprzyjaźniła się z siedmioma krasnoludami?", // old story: Snow White
			"czarownicy, która zwabiła dzieci do swojej chaty, żeby je usmażyć ​​i zjeść?", // old story: Hansel & Gretel
			"pięknej księżniczce, która spała sto lat?", // old story: Sleeping Beauty
			"chłopcu wychowanego przez wilki w lesie Semos?", // old story: Jungle Book
			"osiłeku, który stał się prawie nieśmiertelny po kąpieli w smoczej krwi?", // old story: Nibelungen
			"myszce, która wyciągnęła cierń z łapy lwa?", // old story
			"lisie, który nie zjadł winogron, ponieważ zawisły zbyt wysoko?", // old story
			"słońcu i wiatrze, które rywalizowały ze sobą o to, kto byłby najsilniejszy?", // old story
			"małpkach, które biegały przez las w desperackim poszukiwaniu kokosa?",
			// song, German: Die Affen rasen durch den Wald
			// https://en.wikipedia.org/wiki/List_of_nursery_rhymes
			"starym królu, który był starą, wesołą duszą i jego trzema skrzypkami?", // nursery rhyme: Old King Cole
			"dziewięćdziesięciu dziewięciu butelekach piwa na ścianie?", // nursery rhyme: 99 Bottles of Beer
			"trzech ślepych myszkach, które straciły ogony od noża?", // nursery rhyme: Three Blind Mice
			"trzech małych kociętach, które zgubiły rękawiczki?", // nursery rhyme: Three Little Kittens
			"facecie, który poszedł na targ, żeby kupić tłustego wieprza, jiggety i ciasto śliwkowe?",
			// nursery rhyme: To Market, to Market
			"Tomie, Tomie, synu dudziarza, który ukradł świnię i uciekł?",
			// nursery rhyme: Tom, Tom, the Piper's Son
			"tym z czego zrobieni są mali chłopcy i małe dziewczynki?", // nursery rhyme: What Are Little Boys Made Of?
			"starym rolniku, który miał farmę z dużą ilością głośnych zwierząt?", // nursery rhyme: Old MacDonald
			"dzieciaku, który lubi jeść, jeść, jeść jabłka i banany?", // nursery rhyme: Apples and Bananas
			"rolniku, który miał psa, a Bingo było jego imieniem?", // nursery rhyme: Bingo
			"a-tisket a-tasket zielono-żółty koszyk?", // nursery rhyme: A-Tisket, A-Tasket
			"czarnej owcy, która miała trzy worki pełne wełny?", // nursery rhyme: Baa, Baa, Black Sheep
			"mnichu, który pozwolił, by odpowiedni moment na bicie dzwonów minął, bo zaspał?"
			// nursery rhyme: Brother John, although the English version distorts the
			// original meaning
	};
}
