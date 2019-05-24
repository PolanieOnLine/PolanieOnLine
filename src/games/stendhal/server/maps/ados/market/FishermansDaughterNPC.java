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
package games.stendhal.server.maps.ados.market;

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.Map;

/**
 * Builds a NPC in a house on Ados market (name:Caroline) who is the daughter of fisherman Fritz
 * 
 * @author Vanessa Julius 
 *
 */
public class FishermansDaughterNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		createFishermansDaughterSellingNPC(zone);
	}

	public void createFishermansDaughterSellingNPC(final StendhalRPZone zone) {

		    
		final SpeakerNPC npc = new SpeakerNPC("Caroline") {
			@Override
			protected void createPath() {
				setPath(null);

			}

			@Override
			protected void createDialog() {
				addGreeting("Cześć, miło Cię poznać!");
				addHelp("Odwiedziłeś już targ w Ados? Znajdziesz tam bardzo dużo #'fajnych ludzi' spacerujących wokoło i robiących #'przepyszne jedzenie'!");
				addReply("przepyszne jedzenie", "Próbowałam zupy rybnej i grilowanych steków, były przesmaczne!");
				addReply("fajnych ludzi", "Florence Boullabaisse jest jednym z moich najlepszych przyjaciół, a Haunchy to naprawdę przemiły gość!");
				addQuest("Obecnie staram się dowiedzieć, w jaki sposób założyć swoją własną firmę. Uwielbiam gotować i chcę otworzyć już wkrótce własną firmę #kateringową."); 
				addReply(Arrays.asList("kateringową","kateringiem"), "Słyszałam, że niektóre #hotele potrzebują obiadów dla gości, a ich kuchnie są zbyt małe, więc potrzebują kogoś, kto wyręczy ich w gotowaniu i przygotuje coś to jedzenia i przyniesie jakieś potrzebne składniki.");
				addReply("hotele", "Ogromny hotel w Fado jest dobrze znany nowożeńcom. To naprawdę miłe i przyjemne miejsce, by świętować w nim swój ślub.");
				addJob("Mój tata, #Fritz, jest rybakiem. A ja myślę o otworzeniu własnej firmy, zajmującej się #kateringiem.");
				addReply("sztorm", "Burza zjawiła się znikąd, gdy mój tata, Firtz, łowił ryby. Byłam taka szczęśliwa, gdy tata wrócił bezpiecznie!");
				addReply("Fritz","To mój ukochany tata. Był rybakiem, zanim wielka #sztorm nie zniszczyła jego łodzi.");
				addOffer("Nic nie mogę Ci teraz zaoferować, ale kiedyś - kto wie.");
				addGoodbye("Dziękuję, że odwiedziłeś nas tutaj! Miłego dnia! :)");
			}
			
			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.DOWN);
			}
		};

		npc.setDescription("Widzisz Caroline. Wydaje się być miłą, choć zdecydowaną, panią, która stara się dotrzeć do wybranego celu.");
		npc.setEntityClass("fishermansdaughternpc");
		npc.setPosition(70, 78);
		npc.setDirection(Direction.DOWN);
		npc.initHP(100);
		zone.add(npc);
	}
}
