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
package games.stendhal.server.maps.koscielisko.zrc;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * @author ?
 */
public class EdgardNPC implements ZoneConfigurator {
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
		final SpeakerNPC npc = new SpeakerNPC("Edgard") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(2, 42));
				nodes.add(new Node(14, 42));
				nodes.add(new Node(14, 44));
				nodes.add(new Node(30, 44));
				nodes.add(new Node(30, 58));
				nodes.add(new Node(26, 58));
				nodes.add(new Node(26, 62));
				nodes.add(new Node(8, 62));
				nodes.add(new Node(8, 69));
				nodes.add(new Node(37, 69));
				nodes.add(new Node(37, 66));
				nodes.add(new Node(51, 66));
				nodes.add(new Node(51, 42));
				nodes.add(new Node(2, 42));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting("Ha! Widzisz, jak latam? Zazdrościsz, co? Tylko nieliczni potrafią tak jak ja.");
				addJob("Jestem posłańcem. #Wiadomości z dalekich pól #bitew? Rozkazy od #Mistrza #Zakonu? To wszystko moja działka. Bez mnie ten Zakon by stanął w miejscu.");
				addHelp("Pomocy? Proszę cię, ja latam, gdy ty chodzisz. Co mógłbyś mi zaoferować?");
				addGoodbye("Do zobaczenia na wietrze, śmiertelniku!");

				addReply("zakon", "Zakon Cieni? To jak mgła nad rzeką – zawsze obecny, ale nieuchwytny. Wierz mi, nie chcesz wiedzieć, jak naprawdę działa.");
				addReply("mistrz", "Mistrz Zakonu... Mówią, że widzi wszystko i kieruje każdym cieniem. Czasem zastanawiam się, czy nawet ja nie jestem tylko pionkiem w jego grze.");
				addReply("wiadomości", "Niektóre wiadomości są jak ostrze miecza – szybkie, ciche i śmiertelne. Inne... cóż, bardziej przypominają listę zakupów.");
				addReply("bitwy", "Pola bitew... miejsce, gdzie cienie tańczą w rytm śmierci. To nie jest widok dla każdego.");
				addReply("tajemnice", "Powiem ci jedną tajemnicę: lepiej nie pytać. Cienie mają długą pamięć, a ja nie jestem tu, by cię chronić.");
			}
		};

		npc.setDescription("Oto Edgard, posłaniec Zakonu Cieni, szybki jak wiatr i równie tajemniczy. Niewiele mówi o sobie, ale jego przenikliwe spojrzenie zdradza, że wie więcej, niż chciałbyś usłyszeć.");
		npc.setEntityClass("npcedgard");
		npc.setGender("M");
		npc.setShadowStyle("48x64_floating");
		npc.setPosition(2, 42);
		zone.add(npc);
	}
}
