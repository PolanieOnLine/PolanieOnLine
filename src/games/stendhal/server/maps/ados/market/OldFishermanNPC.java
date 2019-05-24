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
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a npc in Ados (name:Fritz) who is an old fisherman on the market
 * 
 * @author storyteller (idea) and Vanessa Julius (implemented)
 *
 */
public class OldFishermanNPC implements ZoneConfigurator {

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Fritz") {
		    
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(67, 79));
				nodes.add(new Node(62, 79));
                nodes.add(new Node(62, 77));
                nodes.add(new Node(61, 77));  
                nodes.add(new Node(61, 76));
                nodes.add(new Node(55, 76)); 
                nodes.add(new Node(55, 74));
                nodes.add(new Node(58, 74));
                nodes.add(new Node(58, 73)); 
                nodes.add(new Node(60, 73));
                nodes.add(new Node(60, 77));
                nodes.add(new Node(62, 77));
				nodes.add(new Node(62, 79));
               	setPath(new FixedPath(nodes, true));

			}

			@Override
			protected void createDialog() {
				addGreeting("Ahoj!");
				addHelp("Harr mój przyjacielu. #Całe #życie spędzam na łodzi próbując złapać kilka ryb...  Możesz zapytać o pomoc mojego przyjaciela Santiago. Mieszka w chatce rybackiej w mieście!");
				addReply(Arrays.asList("whole life", "Całe życie"),"Uczucie, gdy masz rybę na wędce jest naprawdę wspaniałe! Próbowałeś kiedyś sam złapać #własnoręcznie?");
				addQuest("Ahoj Przyjacielu! Nie mam dla Ciebie zadań, ale inni tak!"); 
				addJob("Jestem rybakiem. Przez wiele, wiele lat codziennie chodzę na ryby. Podczas wielu sztormów wyławiałem naprawdę wielkie sztuki, które omało nie przyczyniły się do zatonięcia mojego statku. ale oczywiście zawsze udawało mi się je #złapać!");
				addReply(Arrays.asList("catch", "złapać"),"Hehe! Mam nadzieje, że będę mógł kiedyś dostarczyć trochę ryb na rynek...");
				addOffer("Moje miejsca połowów na morzu są nawiedzane przez niebezpieczne #sztormy. Nie mogę tam teraz płynąć.");
				addReply(Arrays.asList("storm", "sztormy"), "Duży sztorm prawie zniszczył moją łódź! Przykro mi przyjacielu. Nie mogę ci nic zaoferować w tym momencie...");
				addGoodbye("Dozobaczenia przyjacielu! I uważaj na ten śliski grunt!");
				
			}
		};

		npc.setDescription("Oto Fritz. Nie, on nie śmierdzi! On tylko nie prał swoich rzeczy od wieków będąc od dłuższego czasu na morzu.");
		npc.setEntityClass("oldfishermannpc");
		npc.setPosition(67, 79);
		npc.setDirection(Direction.RIGHT);
		npc.initHP(100);
		zone.add(npc);
	}
}
