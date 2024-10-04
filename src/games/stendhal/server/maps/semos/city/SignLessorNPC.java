/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.semos.city;

import java.awt.Rectangle;
import java.awt.Shape;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.CollisionAction;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.office.RentedSign;
import games.stendhal.server.entity.mapstuff.office.RentedSignList;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.RemoveStorableEntityAction;
import games.stendhal.server.entity.npc.condition.AdminCondition;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasStorableEntityCondition;
import games.stendhal.server.entity.npc.condition.TextHasParameterCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ChatOptionsEvent;
import games.stendhal.server.util.StringUtils;

/**
 * A merchant (original name: Gordon) who rents signs to players.
 *
 * The player has to have at least level 5 to prevent abuse by newly created characters.
 */
public class SignLessorNPC implements ZoneConfigurator {
	protected String text;

	// 1.5 minutes
	private static final int CHAT_TIMEOUT = 90;
	private static final int MONEY = 100;
	protected RentedSignList rentedSignList;

	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		final Shape shape = new Rectangle(21, 48, 17, 1);
		rentedSignList = new RentedSignList(zone, shape);
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Gordon") {
			@Override
			public void createDialog() {
				addGreeting("Witaj! Wynajmuję znaki i usuwam stare.");
				addJob("Wynajmuję znaki na jeden dzień. Powiedz tylko #wynajmij");
				addHelp("Jeżeli chcesz wynająć znak to powiedz #wynajmij i treść co powinno być na nim napisane. Jeśli chcesz usunąć to powiedz #usuń.");
				setPlayerChatTimeout(CHAT_TIMEOUT);

				add(ConversationStates.ATTENDING, "",
					new AndCondition(getRentMatchCond(), new LevelLessThanCondition(6)),
					ConversationStates.ATTENDING,
					"Przepraszam, ale nie wynajmuję znaków osobom, które mają mało doświadczenia jak ty.",
					null);

				add(ConversationStates.ATTENDING, "",
					new AndCondition(getRentMatchCond(), new LevelGreaterThanCondition(5), new NotCondition(new TextHasParameterCondition())),
					ConversationStates.ATTENDING,
					"Powiedz mi #wynajmij, a następnie tekst, który chciałbyś, abym umieścił na nim.",
					null);

				add(ConversationStates.ATTENDING, "wynajmij",
					new AndCondition(getRentMatchCond(), new LevelGreaterThanCondition(5), new TextHasParameterCondition()),
					ConversationStates.BUY_PRICE_OFFERED,
					null,
					new ChatAction() {
						public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
							text = sentence.getOriginalText().substring(9).trim();

							String reply = "Wynajęcie znaku na 24 godziny kosztuje " + MONEY + " money. Chcesz wynająć?";

							if (rentedSignList.getByName(player.getName()) != null) {
								reply = reply + " Pamiętaj, że zastąpię znak, który wcześniej wynająłeś.";
							}

							npc.say(reply);
						}

						@Override
						public String toString() {
							return "remember text";
						}
				});

				add(ConversationStates.ATTENDING, "rent",
					new AndCondition(getRentMatchCond(), new LevelGreaterThanCondition(5), new TextHasParameterCondition()),
					ConversationStates.BUY_PRICE_OFFERED,
					null,
					new ChatAction() {
						public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
							text = sentence.getOriginalText().substring(5).trim();

							String reply = "Wynajęcie znaku na 24 godziny kosztuje " + MONEY + " money. Chcesz wynająć?";

							if (rentedSignList.getByName(player.getName()) != null) {
								reply = reply + " Pamiętaj, że zastąpię znak, który wcześniej wynająłeś.";
							}

							npc.say(reply);
						}

						@Override
						public String toString() {
							return "remember text";
						}
				});

				add(ConversationStates.BUY_PRICE_OFFERED,
					ConversationPhrases.YES_MESSAGES,
					new NotCondition(new PlayerHasItemWithHimCondition("money", MONEY)),
					ConversationStates.ATTENDING,
					"Przepraszam ,ale nie masz pieniędzy", null);

				add(ConversationStates.BUY_PRICE_OFFERED,
					ConversationPhrases.YES_MESSAGES,
					new PlayerHasItemWithHimCondition("money", MONEY),
					ConversationStates.IDLE, null,
					new RentSignChatAction());

				add(ConversationStates.BUY_PRICE_OFFERED,
					ConversationPhrases.NO_MESSAGES, null,
					ConversationStates.ATTENDING,
					"Jeżeli zmienisz zdanie to porozmawiaj ze mną.", null);

				add(ConversationStates.ATTENDING, Arrays.asList("remove", "usuń"),
					new PlayerHasStorableEntityCondition(rentedSignList),
					ConversationStates.ATTENDING,
					"Dobrze usunę twój znak.",
					new RemoveStorableEntityAction(rentedSignList));

				add(ConversationStates.ATTENDING, Arrays.asList("remove", "usuń"),
					new NotCondition(new PlayerHasStorableEntityCondition(rentedSignList)),
					ConversationStates.ATTENDING,
					"Nie wynająłeś żadnego znaku, więc co mam usunąć.", null);

				// admins may remove signs (even low level admins)
				add(ConversationStates.ATTENDING, Arrays.asList("delete", "usuń"),
					new AdminCondition(100),
					ConversationStates.ATTENDING, null,
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
							if (sentence.getExpressions().size() < 2) {
								npc.say("Składnia: usuń <imię wojownika>");
								return;
							}
							final String playerName = sentence.getOriginalText().substring("delete ".length()).trim();
							if (rentedSignList.removeByName(playerName)) {
								final String message = player.getName() + " usunął znak " + playerName;
								SingletonRepository.getRuleProcessor().sendMessageToSupporters("SignLessorNPC", message);
								new GameEvent(player.getName(), "sign", "deleted", playerName).raise();
							} else {
								player.sendPrivateText("Nie mogę znaleźć znaku postawionego przez " + playerName);
							}
						}

						@Override
						public String toString() {
							return "administrator usunął znak";
						}
				});

				addGoodbye();
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(20,50));
				nodes.add(new Node(38, 50));
				nodes.add(new Node(38, 51));
				nodes.add(new Node(20, 51));
				setPath(new FixedPath(nodes, true));
			}
		};

		npc.addKnownChatOptions("rent");
		ChatOptionsEvent.addMerchantActivity("rent");

		npc.setDescription("Oto Gordon. Umieszcza wiadomości na tabliczkach.");
		npc.setEntityClass("signguynpc");
		npc.setGender("M");
		npc.setPosition(20, 50);
		npc.setCollisionAction(CollisionAction.STOP);
		zone.add(npc);
	}

	private static ChatCondition getRentMatchCond() {
		return new ChatCondition() {
			@Override
			public boolean fire(Player player, Sentence sentence, Entity npc) {
				String txt = sentence.getOriginalText();

            	//TODO replaced by using sentence matching "[you] rent"
				if (txt.startsWith("rent") || txt.startsWith("you rent")  || txt.startsWith("wynajmij")  || txt.startsWith("wynająłeś")) {
					return true;
				} else {
					return false;
				}
			}
		};
	}

	class RentSignChatAction implements ChatAction {
		private final Logger logger = Logger.getLogger(RentSignChatAction.class);

		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
			if (text.length() > 1000) {
				text = text.substring(0, 1000) + "...";
			}

			// do not accept all upper case
			if (StringUtils.countLowerCase(text) < StringUtils.countUpperCase(text) * 2) {
				text = text.toLowerCase();
			}

			// put the sign up
			rentedSignList.removeByName(player.getName());
			final RentedSign sign = new RentedSign(player, text);
			final boolean success = rentedSignList.add(sign);

			// confirm, log, tell postman
			if (success) {
				player.drop("money", MONEY);
				npc.say("Dobrze, postawie znak z twoją wiadomością.");

				// inform IRC using postman
				final Player postman = SingletonRepository.getRuleProcessor().getPlayer("postman");
				String message = player.getName() + " wynajęty znak mówi \"" + text + "\"";
				if (postman != null) {
					postman.sendPrivateText(message);
				}
				logger.log(Level.toLevel(System.getProperty("stendhal.support.loglevel"), Level.DEBUG), message);
				new GameEvent(player.getName(), "sign", "rent", text).raise();
			} else {
				npc.say("Przepraszam, ale teraz jest zbyt wiele postawionych znaków. Nie mam miejsca na kolejne.");
			}
		}

		@Override
		public String toString() {
			return "put up sign";
		}
	}
}
