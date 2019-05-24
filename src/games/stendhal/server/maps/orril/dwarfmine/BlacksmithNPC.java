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
package games.stendhal.server.maps.orril.dwarfmine;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Configure Orril Dwarf Blacksmith (Underground/Level -3).
 * 
 * @author kymara
 */
public class BlacksmithNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildBlacksmith(zone);
	}

	private void buildBlacksmith(final StendhalRPZone zone) {
		final SpeakerNPC hogart = new SpeakerNPC("Hogart") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(20, 11));
				nodes.add(new Node(12, 11));
				nodes.add(new Node(12, 7));
				nodes.add(new Node(12, 10));
				nodes.add(new Node(20, 10));
				nodes.add(new Node(20, 8));
				nodes.add(new Node(20, 11));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting();
				addJob("Jestem głównym kowalem. W tajemnicy wykuwam broń dla krasnali, ale oni zapomnieli o mnie i moich #opowieściach.");
				addHelp("Mógłbym opowiedzieć Tobie #historyjkę...");
				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("story", "stories", "historyjkę", "historyjka", "historyjki", "opowieściach"),
				        ConversationStates.ATTENDING,
				        "Domyślam się, że taki obdartus jak ty nigdy nie słyszał o Lady Tembells? Była piękna. Zmarła młodo, a jej szalony mąż poprosił potężnego Lorda o przywrócenie jej do życia. Głupiec nie wiedział do czego doprowadził. Stała się ona wtedy #wampirem.",
				        null);
				add(
				        ConversationStates.ATTENDING,
				        Arrays.asList("vampire", "wampirem"),
				        ConversationStates.ATTENDING,
				        "Mąż zatrudnił do pomocy Lorda Wampirów! Lady stała się jego panną młodą, a jej służące stały się upiorami. Katakumby na północ od Semos są teraz zabójczym miejscem.",
				        null);
				addGoodbye("Dowidzenia. Założę się, że dziś będziesz miał problemy z zaśnięciem.");
				addReply(Arrays.asList("bobbin", "szpulka"),"Szpulki? SZPULKI?! Czy myślisz, że jestem kobietą?! Pfff idź znajdź jakiegoś innego kowala. Nie zajmuję się produkcją szpulek.");
				
			} //remaining behaviour defined in maps.quests.VampireSword and  maps.quests.MithrilCloak
		};

		hogart.setDescription("Oto Hogart, emerytowany główny kowal krasnali.");
		hogart.setEntityClass("olddwarfnpc");
		hogart.setPosition(20, 11);
		hogart.initHP(100);
		zone.add(hogart);
	}
}
