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
package games.stendhal.server.maps.quests;

import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * QUEST: Speak with Zynn PARTICIPANTS: - Zynn
 * 
 * STEPS: - Talk to Zynn to activate the quest and keep speaking with Zynn.
 * 
 * REWARD: - 10 XP (check that user's level is lower than 5) - 5 gold
 * REPETITIONS: - As much as wanted.
 */
public class MeetZynn extends AbstractQuest {
	@Override
	public String getSlotName() {
		return "meetzynn";
	}
	private void step_1() {

		final SpeakerNPC npc = npcs.get("Zynn Iwuhos");

		/**
		 * Quest can always be started again. Just check that no reward is given
		 * for players higher than level 15.
		 */

		npc
				.addReply(
						Arrays.asList("history", "historii"),
						"Obecnie znajdują się dwie znaczące potęgi w Faiumoni Imperium Deniran i mroczne legiony Blordroughta. Blordrought rządzi południową wyspą i ma we władaniu kilka kopalni żelaza oraz wielkie kopalnie złota. Obecnie, Deniran wciąż kontroluje centralną i północną część Faiumoni, w której znajduje się kilka kopalni złota i żelaza.");

		npc
				.addReply(
						Arrays.asList("news", "wiadomościami", "wiadomości"),
						"Imperium Deniran aktualnie szuka łowców przygód, aby wstąpili do wojska jako najemnicy i pomogli odebrać południowe Faiumoni siłom Blordroughta. Niestety Blordrought wciąż stawia zacięty opór i odpiera wszelkie ataki.");

		npc
				.addReply(
						Arrays.asList("geography", "geografii"),
						"Porozmawiajmy o różnych #miejscach, które możesz odwiedzić na Faiumoni! Mogę Ci także pomóc #zdobyć mapę i nauczyć Cię z niej #skorzystać, albo dać Tobie radę jak używać psychicznego systemu #SPPOL.");

		npc
				.addReply(
						Arrays.asList("places", "miejscach"),
						"Najważniejsze lokacje to #Faiumoni, Zamek #Or'ril, #Semos, #Ados, #Narwol, i oczywiście Miasto #Deniran.");

		npc
				.addReply(
						"Faiumoni",
						"Faiumoni to wyspa na której stoisz! Prawdopodobnie już zauważyłeś góry na północy. W południowej części wyspy znajduje się wielka pustynia i oczywiście rzeka, która ją dzieli. Poniżej na południu znajduje się Zamek #Or'ril.");

		npc
				.addReply(
						"Semos",
						"Semos jest naszym miastem, w którym obecnie przebywasz. Jesteśmy po północnej stronie Faiumoni, miasto liczy sobie około 40-50 mieszkańców.");

		npc
				.addReply(
						"Ados",
						"Ados to ważne przybrzeżne miasto leżące na wschód od #Semos, gdzie kupcy przywożą towary z #Deniran. Uważa się, że jest jednym z najważniejszych szlaków morskich Imperium.");

		npc
				.addReply(
						"Or'ril",
						"Zamek Or'ril jest jednym z zamków służących do obrony imperialnej drogi między #Ados, a #Deniran. Gdy jest używany to mieści się w nim 60 szermierzy plus pomocniczy sztab. Teraz większość wojska Imperialnego została wysłana na południe, aby odpierać najeźdźców,a zamek opustoszał. Z tego co wiem to powinien być teraz pusty. Dochodzą do mnie plotki, chociaż...");

		npc
				.addReply(
						"Nalwor",
						"Nalwor to starożytne elfickie miasto, zbudowane w głębi lasu na południowym-wschodzie na długo przed ludźmi, którzy przybyli na wyspę. Elfy nie lubią mieszać się  z innymi rasami i dostaliśmy do zrozumienia, że Nalwor został zbudowany do obrony ich stolicy Teruykeh przed złymi siłami.");

		npc
				.addReply(
						"Deniran",
						"Imperialna stolica Deniranu mieści się w sercu Faiumoni i jest główną bazą operacyjną dla Denirańskich wojsk. Imperialna stolica Deniran leży w samym sercu Faiumoni jest główną bazą operacyjną dla wojsk Deniran. Większość Imperialnych szlaków handlowych z innymi krajami została zapoczątkowana w tym mieście, później została rozszerzona na północ przez #Ados i na południe do Sikhw. Niestety południowe drogi zostały zniszczone, gdy Blordrought i jego armia podbił Sikhwów jakiś czas temu.");

		npc
				.addReply(
						"Zakopane",
						"Zimowa stolica Polski mieszcząca się w południowej części kraju u podnóża Tatr.");

		npc
				.addReply(
						Arrays.asList("use", "skorzystać", "użyj"),
						"Gdy #zdobędziesz mapę to znajdziesz na niej trzy skale, które musisz zrozumieć. Pierwsza to #poziomy mapy, które musisz porównać z konwencjami #nazewnictw dla różny stref wewnątrz tych poziomów. Powinieneś nauczyć się #ustalania #położenia osoby wewnątrz strefy. Z braku czasu będziemy mieli z Ciebie nawigatora!");

		npc
				.addReply(
						Arrays.asList("levels", "poziomy"),
						"Mapy są podzielone na poziomy zaczynając od najwyższych znajdujących się powyżej powierzchni ziemi do najniższych będących pod ziemią. Obszary na powierzchni ziemi mają poziom 0. Numer poziomu znajduje się na początku nazwy mapy. Na przykład #Semos jest na powierzchni ziemi i dlatego ma poziom 0, a mapa nazywa się \"0_semos_city\". Pierwszy poziom podziemi jest na poziomie -1 i mapa nazywa się \"-1_semos_dungeon\". Powinieneś zapamiętać, że mapy budynków mają poziom położenia na końcu nazwy, a zaczynają się od skrótu \"int\" (od \"interior\"). Na przykład wyższe piętro oberży będzie oznaczone jako \"int_semos_tavern_1\".");

		npc
				.addReply(
						Arrays.asList("naming", "nazewnictw"),
						"Zazwyczaj każda mapa jest podzielona na \"zestawy\" stref. Centralna strefa jest używana jako punkt odniesienia. Strefy otaczające centralną strefę są nazywane wg położenia od niej. Na przykład od centralnej strefy \"0_semos_city\" możesz podróżować na zachód do starej wioski, która jest w \"0_semos_village_w\" lub możesz podróżować przez dwie strefy na północ i raz na zachód do gór w \"0_semos_mountain_n2_w\".");

		npc
				.addReply(
						Arrays.asList("positioning", "ustalania", "położenia"),
						"Ustalanie pozycji wewnątrz stref jest prosto opisane w zasadach koordynatów. Weźmy pod uwagę lewy górny kąt ( północno-zachodni kąt ) jest to punkt początkowy o koordynatach ( 0,0 ). Pierwsza pozycja ( 'x' ) zwiększa się, gdy idziesz w prawo ( to znaczy na wschód ) wewnątrz strefy, a druga pozycja ( 'y' ) zwiększa się, gdy idziesz w dół ( czyli na południe ).");

		npc
				.addReply(
						Arrays.asList("get", "zdobyć", "zdobędziesz"),
						"Mapę PolskaGra możesz zobaczyć na stronie #http://www.polskagra.net/strona/kraina-pras%C5%82owia%C5%84ska jeżeli chcesz. Nie spodziewaj się, że będą na niej oznaczone potwory!");

		npc
				.addReply(
						"SPPOL",
						"SPPOL znaczy System Pozycjonowania PolskaGra. Możesz zapytać #Io w Świątyni o szczegóły jak to działa. W zasadzie pozwala Tobie ustalić dokładną lokację siebie lub przyjaciela.");

		npc
				.addReply(
						"Io",
						"Nazywa się \"Io Flotto\". Większość czasu spędza latając w Świątyni. Wygląda na niesamowitą, ale jej \"intuicja\" działa lepiej niż nie jeden kompas.");

		/**
		 * I still have to think of a way to reward a good amount of XP to the
		 * most interested player for this long reading... How about keeping a
		 * list of all the things the player has asked and reward him when the
		 * list is complete?
		 */
		npc.add(ConversationStates.ATTENDING, "bye",
			new LevelLessThanCondition(15),
			ConversationStates.IDLE,
			"Dowidzenia. Hej jeżeli zamierzasz isć do biblioteki to nie zapominaj, aby zachowywać się tam cicho. Ludzie mogą tam coś czytać!",
			null);

		npc.add(ConversationStates.ATTENDING, "bye",
			new LevelGreaterThanCondition(14),
			ConversationStates.IDLE,
			"Dowidzenia. Hej powinieneś się zastanowić nad zdobyciem karty bibliotecznej.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Spotkanie Zynna Iwuhos",
				"Spotkaj Zynn Iwuhos w bibliotece w Semos i zapytaj go o użyteczne informacje.",
				false);
		step_1();
	}
	
	@Override
	public String getName() {
		return "MeetZynn";
	}
	
	// no quest slots ever get set so making it visible seems silly
	// however, there is an entry for another quest slot in the games.stendhal.server.maps.semos.library.HistorianGeographerNPC file 
	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}
	@Override
	public String getNPCName() {
		return "Zynn Iwuhos";
	}
}
