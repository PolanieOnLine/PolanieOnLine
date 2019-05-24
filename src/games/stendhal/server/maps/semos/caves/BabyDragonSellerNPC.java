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
package games.stendhal.server.maps.semos.caves;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.creature.BabyDragon;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BabyDragonSellerNPC implements ZoneConfigurator {

	private static final String QUEST_SLOT = "hatching_dragon";
	// A baby dragon takes this long to hatch
	private static final long REQUIRED_DAYS = 7L;
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildHouseArea(zone);
	}

	private void buildHouseArea(final StendhalRPZone zone) {

		final SpeakerNPC npc = new SpeakerNPC("Terry") {
			@Override
			protected void createPath() {
			      	final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(66, 8));
				nodes.add(new Node(69, 8));
				nodes.add(new Node(69, 17));
				nodes.add(new Node(74, 17));
				nodes.add(new Node(74, 11));
				nodes.add(new Node(73, 11));
				nodes.add(new Node(73, 10));
				nodes.add(new Node(72, 10));
				nodes.add(new Node(72, 9));
				nodes.add(new Node(66, 9));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addGreeting(null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					    if (player.hasQuest(QUEST_SLOT)) {
						final long delay = REQUIRED_DAYS * MathHelper.MILLISECONDS_IN_ONE_DAY;
						final long timeRemaining = (Long.parseLong(player.getQuest(QUEST_SLOT))
								      + delay) - System.currentTimeMillis();
						if (timeRemaining > 0L) {
						    raiser.say("Jajo wciąż się nie wykluło, i będzie wysiadywane przez następne "
										+ TimeUtil.timeUntil((int) (timeRemaining / 1000L))
										+ ".");
								return;
					        }

    						if (player.hasPet()) {
    						    // we don't want him to give a dragon if player already has a pet
    						    raiser.say("Nie powierzę Tobie dopiero co narodzonego smoka jeśli nie będę miał pewności, że skupisz na nim całą swoją uwagę! Wróć kiedy już nie będziesz miał tego zwierzaka.");
    						    return;
    						}

							raiser.say("Z jaja wykluł się smok! Mały żwawy smoczek jest tylko twój. Nie zapomnij dać mu #jeść. Pamiętaj! Smoka trzeba #strzec!");
					       	final BabyDragon babydragon = new BabyDragon(player);

					       	babydragon.setPosition(raiser.getX(), raiser.getY() + 1);

					       	player.setPet(babydragon);
    						// clear the quest slot completely when it's not
    						// being used to store egg hatching times
					       	player.removeQuest(QUEST_SLOT);
					       	player.notifyWorldAboutChanges();
					    } else if (player.isEquipped("mityczne jajo")) {
					    	raiser.say("Skąd masz to jajo?! Zresztą, lepiej nie pytać. Czy chcesz abym o nie zadbał? Może coś się z niego #wykluje. Takie mam hobby.");
					    } else {
							raiser.say("Witaj! Niezbyt często ktoś tu do mnie przybywa.");
					    }
					}
				});
		        addReply(Arrays.asList("hatch", "wykluje", "wykluj"), null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					    if (player.hasPet()) {
						// there's actually also a check for this when the egg is hatched,
						// but we might as well warn player here that they wouldn't be allowed two.
							raiser.say("Ale masz już zwierzątko. Jeśli dałbym Ci drugie to mogłyby się pogryźć... albo gorzej...");
					   } else {
						if (player.isEquipped("mityczne jajo")) {
						    player.drop("mityczne jajo");
						    raiser.say("Dobrze wezmę Twoje jajko i wykluję je w jednym z tych ohydnych pudełek. Wróć za " + 7 + " dzień i powinieneś być wtedy dumny z posiadania nowo narodzonego smoczka.");
						    player.setQuest(QUEST_SLOT, Long.toString(System.currentTimeMillis()));
						    player.notifyWorldAboutChanges();
						} else {
						    raiser.say("No cóż, nie posiadasz mitycznego jaja. Wróć jeśli jakieś znajdziesz, tylko wtedy mogę Ci pomóc.");
						}
					    }
					}
				    });
				addJob("Hoduję smoki. Musisz mieć smocze jajo, z którego #wykluje się mały smok.");
				addQuest("Jeśli zdobędziesz mityczne jajo, zaopiekuję się nim aż #wykluje się z niego smok.");
				addHelp("Jestem smoczym wychowawcą. Jeśli masz smocze jajo może coś się z niego #wykluje. Mogę Ci opowiedzieć jak #podróżować ze zwierzakiem i #dbać o niego. Jeśli przypadkiem spotkasz porzuconego smoka możesz go mieć na #własność.");
				addGoodbye("Uważaj na giganty gdy będziesz stąd wychodził!");
				addReply(Arrays.asList("food", "jeść"), "Małe smoki jedzą mięsiwo, szynkę, steki, a i nie pogardzą kiełbasą. Najbardziej jednak lubią pizzę, jeśli wiesz gdzie jej szukać.");
				addReply(Arrays.asList("care", "dbać"),
						"Małego smoka musisz karmić mięsem, szynką, stekami, kiełbasą i pizzą. Po prostu rzuć jedzenie obok siebie, a smok podleci i je zje. Możesz zobaczyć jak rośnie, klikając na niego prawym przyciskiem myszy. Każdy kawałek, który zjedzą pozwala im urosnąć o jeden punkt.");
				addReply(Arrays.asList("travel", "podróżować"),
						"Oczywiście smok musi być blisko Ciebie, zwłaszcza w czasie przechodzenia na sąsiednią mapę. Możesz go przywołać mówiąc #pet. Jeśli kiedyś zdecydujesz się porzucić zwierzaka, kliknij na siebie i wybierz 'Zostaw zwierzątko'.");
				addReply(Arrays.asList("protect", "strzec"),
					 "Inne stwory mają dobry węch. Wyczuwają twojego małego smoka z daleka i atakują go. Oczywiście będzie się bronił, ale potrzebuje twojej pomocy. Jeśli mu nie pomożesz na pewno zginie.");
				addReply(Arrays.asList("own", "własność"),
						"Tak jak inne zwierzątka, tak i małego smoka można przygarnąć. Wystarczy kliknąć na niego prawym przyciskiem myszy i wybrać 'Przygarnij'. Będzie szedł wszędzie za tobą i założę się, że za chwilę zgłodnieje i będzie wołał: #jeść!");
			}
		};

		npc.setEntityClass("man_005_npc");
		npc.setDescription("oto Terry. W wolnym czasie bawi się z małymi smokami.");
		npc.setPosition(66, 8);
		npc.initHP(100);
		zone.add(npc);

		// Also put a dragon in the caves (people can't Own it as it is behind rocks)
		final BabyDragon drag = new BabyDragon();
                drag.setPosition(62, 8);
                zone.add(drag);
	}
}
