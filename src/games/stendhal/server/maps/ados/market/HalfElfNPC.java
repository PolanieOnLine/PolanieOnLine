/***************************************************************************
 *                     (C) Copyright 2015 - Stendhal                       *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.ados.market;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds a npc in Ados (name: Aerianna) who is a half elf
 *
 * @author storyteller
 *
 */
public class HalfElfNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Aerianna") {

			@Override
			protected void createPath() {
                final List<Node> nodes = new LinkedList<Node>();
                nodes.add(new Node(55, 47));
                nodes.add(new Node(55, 35));
                nodes.add(new Node(73, 35));
                nodes.add(new Node(73, 23));
                nodes.add(new Node(76, 23));
                nodes.add(new Node(76, 11));
                nodes.add(new Node(71, 11));
                nodes.add(new Node(71, 7));
                nodes.add(new Node(57, 7));
                nodes.add(new Node(57, 23));
                nodes.add(new Node(28, 23));
                nodes.add(new Node(28, 28));
                nodes.add(new Node(21, 28));
                nodes.add(new Node(21, 44));
                nodes.add(new Node(31, 44));
                nodes.add(new Node(31, 47));
                setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Witaj #'przyjacielu. Cieszę się, że ciebie widzę.");
				addHelp("Potrzebujesz pomocy? Mogę powiedzieć, że zdobywanie #doświadczenia i #wiedzy może być dla ciebie korzystne.");
				addJob("Jestem pół-elfem. Mój #ojciec był #człowiekiem, a moja #matka #elfem z #Nalwor. Jako, że chciałam lepiej poznać obie #kultury to zdecydowałam się na podróż po #Faiumoni, aby nauczyć się jak najwięcej o #kulturach i zwyczajach.");
				addOffer("Nie mam nic do zaoferowania z wyjątkiem mojego #czasu na rozmowę z tobą.");
				addGoodbye("Dziękuję za rozmowę ze mną!");

		        addReply(Arrays.asList("quest", "zadanie"), "#Przyjacielu nie mam dla ciebie żadnego zadania. Tylko zwiedzam rynek w Ados City.");
		        addReply(Arrays.asList("friend", "przyjacielu", "przyjaciel"), "Lubię poznawać nowych przyjaciół także ludzi z innych #kultur. Możemy uczyć się od siebie!");
		        addReply(Arrays.asList("father", "ojciec"), "Mój ojciec był bardzo dzielnym #człowiekiem nie bojącym się wejść do lasu Nalwor. Był mądry i potrafił dotrzeć do miasta #Nalwor. Tam spotkał moją #matkę i od razu się w niej zakochał...");
		        addReply(Arrays.asList("mother", "matka", "matkę"), "Moja matka jest #elfem mieszkającym w #Nalwor City. Zakochała się w moim #ojcu jakiś czas po tym jak przybył do Nalwor wiele #lat temu i tak się urodziłem jako pół-elf.");
		        addReply(Arrays.asList("time", "czasu", "czas"), "Czas jest cenną rzeczą dla mnie. Jako pół-elf żyję o wiele dłużej niż #człowiek, ale nie tak długo jak #elf.");
		        addReply("Faiumoni", "Faiumoni jest wielkim kontynentem! Byłam w lesie na południu, spinałem się na górę na północy i nawet spędziłem trochę czasu na wielkiej pustyni na południowym-wschodzie. Jest dużo miejsc do odkrycia!");
		        addReply(Arrays.asList("experience", "doświadczenia", "doświadczenie"), "Zdobywasz doświdczenie wykonując różne rzeczy takie jak dbanie o otoczenie i uczenie się od innych.");
		        addReply(Arrays.asList("knowledge", "wiedzy", "wiedza"), "Wiedza jest ważna, aby zrozumieć jak wszystko funkcjonuje. Lubię się uczyć o naszym świecie. Wiem, że nigdy nie nauczy się wszystkiego.");
		        addReply("Nalwor", "Nie słyszałeś o Nalwor City? Cóż prawdopodobnie nie. Jest ukryte głęboko w lesie, ale nie daleko stąd... Urodziłem się tam wiele #lat temu...");
		        addReply(Arrays.asList("years", "lat"), "Powinieneś wiedzieć, że jestem starszy niż na to wygląda... Elfy mogą żyć bardzo długo tak jak moja #matka, która jest #elfem ja także tak szybko się nie starzeję, ale wolę utrzymywać swój wiek jako mały sekret! *hihi*");
		        addReply(Arrays.asList("cultures", "kultury", "kulturach", "kultur", "kultura"), "Jest wiele różnych kultur w Faiumoni, które #różnią się w zależności od regionu i rasy, ale często są #podobne do innych.");
		        addReply(Arrays.asList("differ", "różnią"), "Kultura elfów jest w pewnym sensie inna od ludzkich kultur i kompletnie inna od kultury krasnali. #Krasnal nigdy nie wybierze życia w lesie, ale zawsze woli życie w górach lub głęboko pod ziemią!");
		        addReply(Arrays.asList("similar", "podobne"), "Czy wiesz, że pewne plemiona #orków także odprawiają rytuał podobny do ludzkiego ślubu? Ale zwykle slub orków nie trwa dłużej niż do momentu, aż ich dzieci dorosną i mogą same polować. Innym przykładem podobieństwa jest to, że pewne ludzkie plemiona podróżują dookoła jako nomadowie tak jak elfy albinosy! Czasem kultury są tak bardzo podobne wystarczy się im tylko dobrze przyjrzeć!");
		        addReply(Arrays.asList("human", "człowiekiem", "człowiek"), "Coż ponieważ urodziłam się i wychowywałam w #Nalwor to najprawdopodbniej ty będziesz więdział lepiej o ludziach niż ja mimo to, że jestem pół-elfem, którego ojciem był człowiek. Ale z tego co się dowiedziałam o ludziach to to, że są bardzo mądrzy w wynalezieniu rzeczy i rozwiązywaniu problemów. Często nawet walczą, a inni wykazują symaptię dla innych ludzi i istot.");
		        addReply(Arrays.asList("elf", "elfem"), "Elfy są dumne ze swojej urody, inteligencji i magii. Nie mówie tego z powodu, że jestem pół-elfem! *hihi* Żyją odseparowani od innych ras w lesie lub innych miejscach, gdzie magia jest mocna. Elfy mogą być bardzo stare, a ty tego nie zawuważysz. Jeżeli możesz się zaprzyjaźnić z jakimiś elfami to możesz się od nich sporo nauczyć!");
		        addReply(Arrays.asList("dwarf", "krasnal"), "Krasnale są małe, ale dzielne! Żyją pod ziemią lub w górach ponieważ kochają wykopywanie cennych materiałów. Cała kultura krasnali posiada wiele tradycji takich jak tworzenie klanów. Odwiedziałam klan krasnali jakiś czas temu i po tygodniu nieufności krasnale stały bardzo wielkoduszne i przyjacielskie!");
		        addReply(Arrays.asList("orc", "orków", "ork"), "Orki żyją prostym życiem, którre głównie opiera się na polowaniu i walce. Miałam szczęście uratować szefa orków, który samotnie próbował pokonać zielonego smoka, ale o mało co nie zginął ponieważ miał pecha i tak zaprosił mnie do odwiedzenia jego plemienia. Sporo się nauczyłam o ich kulturze podczas tego pobytu. Nie są źli nawet jeśli inni ludzie tak myślą.");
			}
		};

		npc.setDescription("Oto Aerianna. Piękna młoda kobieta w elfickimi uszami.");
		npc.setEntityClass("halfelfnpc");
		npc.setPosition(55, 47);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}

}
