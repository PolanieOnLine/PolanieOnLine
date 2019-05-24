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
package games.stendhal.server.maps.kirdneh.museum;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds a wizard npc, an expert in textiles.
 *
 * @author kymara 
 */
public class WizardNPC implements ZoneConfigurator {

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
		final SpeakerNPC npc = new SpeakerNPC("Kampusch") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(23, 3));
				nodes.add(new Node(23, 44));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			    protected void createDialog() {
				addHelp("Nie jestem kuratorem tego muzeum. Ja tylko zwiedzam tak jak ty.");
				addOffer("Nauczę Cię o #'przędzy jedwabnej' i tkaniu #sukna oraz jak czarodziej może połączyć #mithril z tkaniną.");
				addJob("Jestem czarodziejem. Specjalizuje się w magicznych tkaninach. Mogę Ci wszystko opowiedzieć co powinieneś wiedzieć o #przędzy jedwabnej i tkaniu #sukna.");
				addReply("przędzy jedwabnej","Najlepszą nicią ze wszystkich jest jasna i silna nić zwana jedwabiem. Pochodzi z pajęczej przędzy. Wyrabianie jedwabiu z pajęczej przędzy jest brudną robotą. Czarodziej nie upadnie tak nisko. Naukowcy /#Scientists/ najlepiej się nadają do wyrabiania nici jeżeli ich potrzebujesz.");
				addReply("sukna","Ubrania mają różne standardy, które pewnie zauważyłeś w swoich pelerynach. Tkanina z mithrilu jest najlepsza i wytrzymalsza od wszystkich innych. Mógłbym powiedzieć, że pochodzi od Mithrilbourghtów... Musisz znaleźć mnóstwo pajęczych nici, które zabierzesz do naukowca ( #scientist ), aby zrobił nić, Gdy będziesz miał jedwab to przynieś go do mnie, abym połączył ( #fuse ) z mithrilem. Na końcu będziesz musiał wziąć nić z mithrilu do #Whiggins, aby mieć tkaninę.");
				addReply("mithril","Powinieneś go mieć. Mogę połączyć ( #fuse ) mithril nugget i nić z silk razem. Nie oferuję tej magii byle komu... Gdy będziesz miał nić z mithrilu to #Whiggins utka tkaninę.");
				addGoodbye("Żegnaj.");
				// remaining behaviour defined in maps.quests.MithrilCloak
	 	     }
		    
		};

		npc.setDescription("Oto czarodziej mithrilbourghtów zwiedzający muzeum");
		npc.setEntityClass("mithrilforgernpc");
		npc.setPosition(23, 3);
		npc.initHP(100);
		zone.add(npc);
	}
}
