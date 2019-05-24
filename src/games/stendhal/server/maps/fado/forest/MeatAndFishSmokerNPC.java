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

import games.stendhal.common.Direction;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.adder.MultiProducerAdder;
import games.stendhal.server.entity.npc.behaviour.impl.MultiProducerBehaviour;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provides a Meat and Fish professional smoker in Fado forest.
 *
 * @author omero 
 */
public class MeatAndFishSmokerNPC implements ZoneConfigurator {
    /**
     * Configure a zone.
     *
     * @param   zone        The zone to be configured.
     * @param   attributes  Configuration attributes.
     */
    @Override
    public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
        buildNPC(zone);
    }

    private void buildNPC(final StendhalRPZone zone) {
        final SpeakerNPC npc = new SpeakerNPC("Olmo") {

            @Override
            protected void createPath() {
                final List<Node> nodes = new LinkedList<Node>();
                nodes.add(new Node(26, 53));
                nodes.add(new Node(26, 58));
                nodes.add(new Node(22, 58));
                nodes.add(new Node(29, 58));
                nodes.add(new Node(29, 52));
                nodes.add(new Node(31, 52));
                nodes.add(new Node(27, 52));
                nodes.add(new Node(27, 53));
                nodes.add(new Node(25, 53));
                nodes.add(new Node(25, 52));
                nodes.add(new Node(22, 52));
                nodes.add(new Node(22, 50));
                nodes.add(new Node(22, 53));

                setPath(new FixedPath(nodes, true));
            }

            @Override
            protected void createDialog() {
                addJob("Mogę #uwędzić Tobie #mięso #wędzone, #szynka #wędzona, #wędzony #pstrąg lub #wędzony #dorsz. Zapytaj mnie tylko!");
                addOffer("#Uwędze dla Ciebie #mięso #wędzone, #szynka #wędzona, #wędzony #pstrąg lub #wędzony #dorsz, ale musisz mi przynieść to co potrzebuję.");
                addHelp("Zapytaj mnie o #uwędzenie dla Ciebie #mięso #wędzone, #szynka #wędzona, #wędzony #pstrąg lub #wędzony #dorsz. To jest to w czym jestem dobry i jeśli gdy przyniesiesz mi to co potrzebuję.");

                addReply(Arrays.asList("smoked", "smoked meat", "smoked ham", "smoked trout", "smoked cod", "uwędzić", "uwędze", "uwędzenie", "wędzenia", "uwędzonych", "mięso wędzone", "szynka wędzona", "wędzony pstrąg", "wędzony dorsz"),
                    "Sekret tkwi w tym jakie zioła i polano mi przyniesiesz do #wędzenia.");
                addReply(Arrays.asList("sclaria", "kekik"),
                    "Rośnie w wielu miejscach na krawędziach lub na skrajach lasu.");
                addReply(Arrays.asList("trout", "cod", "pstrąg", "dorsz"),
                    "Nie chciałbym ujawniać tobie moich miejsc łowienia ryb, ale poleciłbym ci poszukanie książek na ten temat.");
                addReply(Arrays.asList("meat","ham", "mięso", "szynka"),
                    "Nie interesuje mnie skąd pochodzi ze słonia czy lwa... Mogę to #uwędzić dla Ciebie!"); 
                    
                addGoodbye("S' veg!");

                final HashSet<String> productsNames = new HashSet<String>();
                productsNames.add("mięso wędzone");
                productsNames.add("szynka wędzona");
                productsNames.add("wędzony pstrąg");
                productsNames.add("wędzony dorsz");

                final Map<String, Integer> reqRes_smokedMeat = new TreeMap<String, Integer>();
                reqRes_smokedMeat.put("polano", 2);
                reqRes_smokedMeat.put("mięso", 1);
                reqRes_smokedMeat.put("kekik", 1);

                final Map<String, Integer> reqRes_smokedHam = new TreeMap<String, Integer>();
                reqRes_smokedHam.put("polano", 3);
                reqRes_smokedHam.put("szynka", 1);
                reqRes_smokedHam.put("kekik", 2);

                final Map<String, Integer> reqRes_smokedTrout = new TreeMap<String, Integer>();
                reqRes_smokedTrout.put("polano", 1);
                reqRes_smokedTrout.put("pstrąg", 1);
                reqRes_smokedTrout.put("sclaria", 1);

                final Map<String, Integer> reqRes_smokedCod = new TreeMap<String, Integer>();
                reqRes_smokedCod.put("polano", 1);
                reqRes_smokedCod.put("dorsz", 1);
                reqRes_smokedCod.put("sclaria", 2);


                final HashMap<String, Map<String, Integer>> requiredResourcesPerProduct = new HashMap<String, Map<String, Integer>>();
                requiredResourcesPerProduct.put("mięso wędzone", reqRes_smokedMeat);
                requiredResourcesPerProduct.put("szynka wędzona", reqRes_smokedHam);
                requiredResourcesPerProduct.put("wędzony pstrąg", reqRes_smokedTrout);
                requiredResourcesPerProduct.put("wędzony dorsz", reqRes_smokedCod);

                final HashMap<String, Integer> productionTimesPerProduct = new HashMap<String, Integer>();
                productionTimesPerProduct.put("mięso wędzone", 5 * 60);
                productionTimesPerProduct.put("szynka wędzona", 8 * 60);
                productionTimesPerProduct.put("wędzony pstrąg", 4 * 60);
                productionTimesPerProduct.put("wędzony dorsz", 6 * 60);

                final HashMap<String, Boolean> productsBound = new HashMap<String, Boolean>();
                productsBound.put("mięso wędzone", false);
                productsBound.put("szynka wędzona", true);
                productsBound.put("wędzony pstrąg", true);
                productsBound.put("wędzony dorsz", false);

                final MultiProducerBehaviour behaviour = new MultiProducerBehaviour(
                    "olmo_smoke_meatandfish",
                    Arrays.asList("smoke", "uwędź"),
                    productsNames,
                    requiredResourcesPerProduct,
                    productionTimesPerProduct,
                    productsBound);

                new MultiProducerAdder().addMultiProducer(this, behaviour,
                        "Witaj! Jestem pewien, że poczułeś aromat pochodzący z moich #uwędzonych produktów!");
            }
        };

        npc.setEntityClass("meatandfishsmokernpc");
        npc.setDirection(Direction.DOWN);
        npc.setPosition(26, 53);
        npc.initHP(100);
        npc.setDescription("Oto Olmo. Wygląda na zajętego wędzeniem mięsa i ryb.");
        zone.add(npc);
    }
}
