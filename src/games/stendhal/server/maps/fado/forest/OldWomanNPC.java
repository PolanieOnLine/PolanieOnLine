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
package games.stendhal.server.maps.fado.forest;

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
 * Creates Jefs mother Amber in Fado Forest and other areas (she moves in different zones)
 *
 * @author Vanessa Julius
 */
public class OldWomanNPC implements ZoneConfigurator {

	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Amber") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(38,8));
				nodes.add(new Node(14,8));
				nodes.add(new Node(14,30));
				nodes.add(new Node(34,47));
				nodes.add(new Node(45,47));
				nodes.add(new Node(45,61));
				nodes.add(new Node(70,61));
				nodes.add(new Node(70,74));		
				nodes.add(new Node(52,74));
				nodes.add(new Node(52,70));
				nodes.add(new Node(29,70));
				nodes.add(new Node(29,96));
				nodes.add(new Node(41,96));
				nodes.add(new Node(41,104));
				nodes.add(new Node(53,104));
				nodes.add(new Node(53,110));
				nodes.add(new Node(42, 110));
				nodes.add(new Node(42, 125));
				nodes.add(new Node(53,125));
				nodes.add(new Node(59,125));
				nodes.add(new Node(59,120));
				nodes.add(new Node(73,120));
				nodes.add(new Node(89,120));
				nodes.add(new Node(89,113));
				nodes.add(new Node(107,113));
				nodes.add(new Node(107,99));
				nodes.add(new Node(114,99));
				nodes.add(new Node(114,95));
				nodes.add(new Node(124,95));
				nodes.add(new Node(124,93));
				nodes.add(new Node(127,93));
				nodes.add(new Node(127,94));
				nodes.add(new Node(125,94));
				nodes.add(new Node(125,110));
				nodes.add(new Node(118,110));
				nodes.add(new Node(118,118));
				nodes.add(new Node(110,118));
				nodes.add(new Node(110,121));
				nodes.add(new Node(99,121));
				nodes.add(new Node(99,118));
				nodes.add(new Node(74,118));
				nodes.add(new Node(74,111));
				nodes.add(new Node(47,111));
				nodes.add(new Node(47,99));
				nodes.add(new Node(33,99));
				nodes.add(new Node(33,88));
				nodes.add(new Node(60,88));
				nodes.add(new Node(60,69));
				nodes.add(new Node(45,69));
				nodes.add(new Node(45,49));
				nodes.add(new Node(52,49));
				nodes.add(new Node(52,38));
				nodes.add(new Node(29,38));
				nodes.add(new Node(29,26));
				nodes.add(new Node(14,26));
				nodes.add(new Node(14,22));
				nodes.add(new Node(9,22));
				nodes.add(new Node(9,11));
				nodes.add(new Node(30,11));
				nodes.add(new Node(30,8));
				nodes.add(new Node(37,8));
				nodes.add(new Node(38,8));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("O ktoś spotkał mnie na mojej drodze. Witam.");
				addJob("Aktualnie nie pracuję. Powinnam być dobrą matką, ale jakoś #zawiodłam.");
				addReply(Arrays.asList("failed, fail", "zawiodłam"), "Opuściłam mojego biednego syna na tak długo, że wątpię, aby kiedykolwiek się do mnie odezwał. Było tak dużo #problemów...");
				addReply(Arrays.asList("problem", "problemów"), "Zakochałam się w człowieku z Kirdneh, który nazywa się Roger Frampton. Niestety zerwaliśmy i nie wiedząc co dalej robić opuściłam miasto i spędziłam trochę czasu #sama.");
				addReply(Arrays.asList("own", "sama"), "Drzewa i zwierzęta stali się moimi największymi towarzyszami porzucając przy tym moją prawdziwą rodzinę. Tęsknie bardzo za moim synem #Jefem.");
				addReply("Jef", "Taki miły i cichy młody chłopiec. Wiem, że czeka na mój powrót, ale nie jestem jeszcze gotowa.");
				addQuest("Nie mam teraz dla Ciebie żadnych zadań.");
				addHelp("Odwiedź tutaj kilku moich nowych #przyjaciół. Są mili i łagodni!");
				addReply(Arrays.asList("friend", "przyjaciół"), "Aldrin wyrabia na prawdę pyszny miód, a jego pszczoły ciężko pracują, aby utrzymać dobrą jakość.");
				addOffer("Przykro mi, ale nie mam nic do sprzedania.");
				addGoodbye("Dowidzennia i bądź miły dla mojego syna.");
			}
		};

		npc.setEntityClass("oldwomannpc");
		npc.setPosition(38, 8);
		npc.initHP(100);
		npc.setDescription("Oto starsza kobieta. Wygląda na trochę zdezorientowaną.");
		zone.add(npc);
	}
}
