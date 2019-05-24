/***************************************************************************
 *                   (C) Copyright 2003-2016 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.temple;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

public class TelepathNPC implements ZoneConfigurator {
	/**
	 * Configure a zone.
	 *
	 * @param	zone		The zone to be configured.
	 * @param	attributes	Configuration attributes.
	 */
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildSemosTempleArea(zone);
	}

	private void buildSemosTempleArea(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Io Flotto") {

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(8, 19));
				nodes.add(new Node(8, 20));
				nodes.add(new Node(15, 20));
				nodes.add(new Node(15, 19));
				nodes.add(new Node(16, 19));
				nodes.add(new Node(16, 14));
				nodes.add(new Node(15, 14));
				nodes.add(new Node(15, 13));
				nodes.add(new Node(12, 13));
				nodes.add(new Node(8, 13));
				nodes.add(new Node(8, 14));
				nodes.add(new Node(7, 14));
				nodes.add(new Node(7, 19));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {

				// player has met io before and has a pk skull
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestStartedCondition("meet_io"),
								new ChatCondition() {
									@Override
									public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
										return player.isBadBoy() ;
									}
								}),
				        ConversationStates.QUESTION_1,
				        null,
				        new SayTextAction("Witaj ponownie [name]. Wyczuwam, że zostałeś naznaczony znakiem czaszki. Czy chcesz, abym to usunęła?"));
				
				// player has met io before and has not got a pk skull
				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestStartedCondition("meet_io"),
								new ChatCondition() {
									@Override
									public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
										return !player.isBadBoy() ;
									}
								}),
				        ConversationStates.ATTENDING,
				        null,
				        new SayTextAction("Witaj ponownie [name]. W czym mogę #pomóc? Nie to. Tego jeszcze nie wiem..."));
				
				// first meeting with player
				add(ConversationStates.IDLE, 
						ConversationPhrases.GREETING_MESSAGES, 
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestNotStartedCondition("meet_io")),
						ConversationStates.ATTENDING,
				        null, 
				        new MultipleActions(
				        		new SayTextAction("Czekałam na Ciebie [name]. Skąd znam twoje imię? To proste jestem Flotto telepatka. Chcesz, abym pokazała Ci sześć podstaw telepatii?"),
				        		new SetQuestAction("meet_io", "start")));
	
				add(ConversationStates.QUESTION_1, ConversationPhrases.YES_MESSAGES, null, ConversationStates.ATTENDING,
				        null, new ChatAction() {

					        @Override
					        public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						       	if ((player.getLastPVPActionTime() > System.currentTimeMillis()
											- 2 * MathHelper.MILLISECONDS_IN_ONE_WEEK)) {
									// player attacked another within the last two weeks
									long timeRemaining = player.getLastPVPActionTime() - System.currentTimeMillis() 
										+ 2 * MathHelper.MILLISECONDS_IN_ONE_WEEK;
									raiser.say("Musisz powstrzymać się od atakowania innych wojowników na dwa pełne tygodnie. Wróć za " + TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + ". Pamiętaj, że będę wiedziała jeżeli pomyślisz o czymś złym!");
								} else if (player.getKarma() < 5) {
									// player does not have much good karma
									raiser.say("Mówią, że spotykają ludzi dobre rzeczy z Twojej strony. Znów masz dobrą karmę. Oznacza to, że zmieniasz się i robisz dobre uczynki dla innych. Wróć, gdy będziesz miał lepszą karmę.");
								} else {
									// player has fulfilled all requirements to be rehabilitated
									raiser.say("Żałujesz swoich czynów?");
									raiser.setCurrentState(ConversationStates.QUESTION_2);
								}
							} 
					    }
				);
				// player didn't want pk icon removed, offer other help
				add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES, null, ConversationStates.ATTENDING, "Dobrze! Mogę ci #pomóc w różnych rzeczach jeżeli chcesz.", null);
				// player satisfied the pk removal requirements and said yes they were sorry
				add(ConversationStates.QUESTION_2, ConversationPhrases.YES_MESSAGES, null, ConversationStates.ATTENDING,
				        "Dobrze. Wiedziałam.", new ChatAction() {

					        @Override
					        public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
								player.rehabilitate(); 	
							} });
				// player said no they are not really sorry
				add(ConversationStates.QUESTION_2, ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE, "Myślałam, że nie! Dowidzenia!", null);
				addJob("Potrafię użyć całą moc ludzkiego umysłu. Zrobiłam wielkie postępy w telepatii i telekinezie. Jednakże nadal nie mogę przewidywać przyszłości. Nie jestem pewna czy będziemy w stanie zniszczyć mroczne legiony Blordroughów...");
				addQuest("Na razie nie potrzebuję pomocy od nikogo i... Hej! Nie próbujesz czasem czytać w moich myślach? Zawsze przed zrobieniem czegoś takiego powinieneś zapytać o pozwolenie!");
				addGoodbye();
				// further behaviour is defined in the MeetIo quest.
			}
		};

		npc.setEntityClass("floattingladynpc");
		npc.setDescription("Oto Io Flotto. Widzi ciebie.");
		npc.setPosition(8, 19);
		npc.initHP(100);
		zone.add(npc);
	}
}
