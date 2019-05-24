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
import games.stendhal.server.entity.npc.behaviour.adder.ProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provides Uncle Dag NPC, in Ados Market.
 * He will produce fierywater bottles if he is given sugar canes (from cane fields)
 * 
 * @author omero
 */
public class FierywaterDistillerNPC implements ZoneConfigurator {

    @Override
    public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
        buildNPC(zone);
    }

    private void buildNPC(final StendhalRPZone zone) {
        final SpeakerNPC npc = new SpeakerNPC("Uncle Dag") {
            
            @Override
            protected void createPath() {
                setPath(null);
            }

            @Override
            protected void createDialog() {
                addGreeting("Cześć!");
                addHelp("Jestem tutaj nowy. Nie mogę ci za bardzo pomóc!");
                addQuest("Oh cóż... Nie jestem zbyt dobry w tych rzeczach... Jestem prostym człowiekiem z prostymi potrzebami, ale dziękuję, że zapytałeś."); 
                addJob("Mogę #wywarzyć #'ekstrakt litworowy' dla Ciebie o ile przyniesiesz mi wystarczająco dużo #trzciny #cukrowej i #polano!");
                addOffer("Jeżeli potrzebujesz #'ekstrakt litworowy' to poproś mnie o #wywarzenie dla Ciebie!");
                addReply(Arrays.asList("fierywater", "ekstrakt litworowy"),
                    "To moja specjalność! Z resztą składników sporządzę dla Ciebie i będziesz miał doskonale wywarzony napój."
                    +   " Wypij 100% czysty, a najprawdopodobniej nie dożyjesz chwili, aby opowiedzieć o doznaniach!");
                addReply(Arrays.asList("sugar", "cane", "canes", "sugar cane", "trzciny", "cukrowej","trzcina cukrowa"),
                    "Zdobywam potrzebną trzcinę cukrową z Athor island.");
                addReply(Arrays.asList("wood", "polano"),
                    "Możesz znaleść mnóstwo polan w pobliżu drzew, a lasy są najlepszym miejscem, gdzie znajdziesz drzewa!");
                addGoodbye("Zapraszam na rynek!");
            }

            @Override
            protected void onGoodbye(RPEntity player) {
                setDirection(Direction.DOWN);
            }
        };

        final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
        requiredResources.put("trzcina cukrowa", 5);
        requiredResources.put("polano", 1);

        final ProducerBehaviour behaviour = new ProducerBehaviour("uncle_dag_brew_fierywater",
            Arrays.asList("brew", "wywarzenie"), "ekstrakt litworowy", requiredResources, 20 * 60);
        new ProducerAdder().addProducer(npc, behaviour,
            "Cześć! Jestem Uncle Dag, gorzelnik! Jeżeli przyniesiesz mi #'trzcinę cukrową' i #polano to #wywarzę dla Ciebie #'ekstrakt litworowy'.");

        npc.setDescription("Oto Uncle Dag. Prowadzi gorzelnie na rynku Ados.");
        npc.setEntityClass("fierywaterdistillernpc");
        npc.setPosition(35, 30);
        npc.setDirection(Direction.DOWN);
        npc.initHP(100);
        zone.add(npc);
    }
}
