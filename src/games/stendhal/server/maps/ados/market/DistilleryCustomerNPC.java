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
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Builds a npc in Ados (name: Hank) who is a customer of the distillery
 * 
 * @author storyteller
 *
 */
public class DistilleryCustomerNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Hank") {
		    
			@Override
			protected void createPath() {
				// no path
			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć! Jak się masz?");
				addHelp("Nie sądzę, abym mógł ci pomóc. Mam tu mały #drink w pięknej destylarni #Uncle #Dag. Może chciałbyś się przyłączyć? Moglibyśmy #porozmawiać o interesujących rzeczach."); 
				addJob("Pracowałem jako górnik w #kopalni węgla na półnoć od #Semos #City. Gdy zamknięto ją to przeniosłem się do Ados City z moją #rodziną próbując znaleść nową pracę.");
				addOffer("Mogę zaoferować ci #'drink', a wszystkie moje drobniaki już się rozszeszły.");
				addGoodbye("Dowidzenia! Trzymaj się!");
				addReply(ConversationPhrases.QUEST_MESSAGES, 
						"Aktualnie nic. Dziękuję.");
				addReply(Arrays.asList("talk", "porozmawiać"), "Nie znam najnowszych #plotek, a chcesz poznać jakieś? Lub jesteś zainteresowany moją byłą #pracą?");
				addReply(Arrays.asList("gossip", "plotek", "plotka"), "Zawsze słuchaj o czym inni mówią i rozmawiaj ze wszystkimi. Możesz się wtedy nauczyć interesujących i użytecznych rzeczy, które mogą ci się kiedyś przydać. Może to dobrze mieć czasem jakieś tematy do rozmowy.");
				addReply(Arrays.asList("Uncle Dag", "Uncle", "Dag"),
						"Uncle Dag to on! Zdrówko!");
				addReply("drink", "Fierywater... jest mocny! Trzymaj się tego!");
		        addReply(Arrays.asList("family", "rodziną", "rodzina"), "Moja ukochana #żona i moja #córka. Są naprawdę moimi aniołami!");
		        addReply(Arrays.asList("wife", "żona"), "Jest wspaniałą osobą. Bardzo ją kocham! Jeżeli znajdę wkrótce nową pracę to będzie bardzo szczęśliwa. Jutro spróbuję zdobyć pracę w dokach.");
		        addReply(Arrays.asList("daughter", "córka"), "Moja córka jest szczęśliwa, że się przenieśliśmy z #Semos #City do Ados. Jest w wieku, w którym lubi wychodzić w weekendy. Tutaj ma więcej przyjaciół i więcej się dzieje.");
		        addReply(Arrays.asList("Semos City", "Semos", "City"), 
		        		"Było dobrym miejscem do życia, ale nie ekscytującym. Tutaj w Ados jest inaczej. To wielkie miasto.");
		        addReply(Arrays.asList("mine", "kopalni", "kopalnia"), "Kopalnia Semos jest strasznie stara. Są tam miejsce do których lepiej nie chodzić. W pobliżu wejścia jest stosunkowo bezpiecznie, ale #inni #górnicy i ja słyszeliśmy dziwne odgłosy z głębi tunelów... WIele z nich jest teraz zablokowanych, ale nikt nie wie co tam #straszy...");
		        addReply(Arrays.asList("other miners", "other", "miners"), 
		        		"Oh mieliśmy wielu górników pracujących w kopalni Barbarus, Pickins, Nathan... Nie wiem, gdzie inni poszli po opuszczeniu kopalni. Słyszałem plotki, że #Barbarus wciąż kolekcjonuje stare narzędzia i materiały, aby je później sprzedać.");
		        addReply("Barbarus", "Może wciąż być w kopalni. Jeżeli to prawda to pewnie jest w pobliżu wejścia ponieważ wie, że w głębi tunelów jest niebezpiecznie... Jeżeli go znajdziesz to może narysuje ci mapę kopalni ponieważ zna ją całkiem nieźle. Na wszelki wypadek jak się zgubisz...");
		        addReply(Arrays.asList("haunting", "straszy"), "Niektórzy mówią, że w koaplni żyje smok, ale inni wieżą w inne dziwne rzeczy... Nie wiem co jest prawdą, ale nie zamierzam tego sprawdzać!");
			}
		};

		npc.setDescription("Oto Hank. Delektuje się swoim drinkiem.");
		npc.setEntityClass("hanknpc");
		npc.setPosition(37, 31);
		npc.setDirection(Direction.LEFT);
		npc.initHP(100);
		zone.add(npc);
	}
	
}
