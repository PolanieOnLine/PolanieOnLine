/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.mountain;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.Level;
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
import games.stendhal.server.entity.npc.action.TeleportAction;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.player.RebornSystem;

/** Yerena, guardian of the character reborn system. */
public class ResetLVLNPC implements ZoneConfigurator {
	private static final String HOME = "int_zakopane_home";

	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Yerena") {
			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(5, 18));
				nodes.add(new Node(16, 18));
				setPath(new FixedPath(nodes, true));
			}

			@Override
			protected void createDialog() {
				addRebornGreeting(this);
				addRebornConversation(this);

				addJob("Strzegę nici czasu wojowników. Gdy zamkniesz całą drogę swojego rozwoju, mogę pozwolić ci przeżyć ją od początku.");
				addOffer("Gdy osiągniesz najwyższy poziom, zapytaj mnie o #odrodzenie.");
				addHelp("Odrodzenie cofa poziom i doświadczenie, ale zachowuje twoje zadania, umiejętności, wyposażenie oraz dary zdobyte podczas wcześniejszych powrotów.");
				addGoodbye();
			}
		};

		npc.setDescription("Oto Yerena. Smok, który włada czasem.");
		npc.setEntityClass("dragon3npc");
		npc.setGender("F");
		npc.setPosition(16, 18);
		zone.add(npc);
	}

	private void addRebornGreeting(final SpeakerNPC npc) {
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new GreetingMatchesNameCondition(npc.getName()),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence,
							final EventRaiser raiser) {
						if (RebornSystem.hasPendingRewards(player)
								&& RebornSystem.claimPendingRewards(player)) {
							raiser.say("Witaj ponownie. Zanim dotkniemy nici czasu, oddaję ci pamiątkę należną za jedną z twoich wcześniejszych podróży.");
						} else {
							raiser.say("Witaj, wojowniku. Czuję ślady czasu, który już za tobą pozostał.");
						}
					}
				});
	}

	private void addRebornConversation(final SpeakerNPC npc) {
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("odrodzenie", "odrodzić", "odrodzic", "czas", "zadanie"),
				null,
				ConversationStates.QUESTION_1,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence,
							final EventRaiser raiser) {
						if (!RebornSystem.canReborn(player)) {
							raiser.say("Twoja obecna droga jeszcze się nie zakończyła. Wróć do mnie, gdy osiągniesz poziom "
									+ Level.maxLevel() + ". Teraz masz poziom " + player.getLevel() + ".");
							raiser.setCurrentState(ConversationStates.ATTENDING);
							return;
						}

						final int nextReborn = RebornSystem.getRebornCount(player) + 1;
						if (nextReborn <= 5) {
							raiser.say(firstFiveOffer(nextReborn));
						} else {
							raiser.say("Pięć najważniejszych darów czasu już do ciebie należy. Mogę jednak ponownie cofnąć twoją drogę. Nie otrzymasz kolejnej premii do siły, zdrowia ani doświadczenia, ale twoje następne odrodzenie pozostanie częścią historii tej postaci. Czy chcesz tego?");
						}
					}
				});

		npc.add(ConversationStates.QUESTION_1,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.QUESTION_2,
				"Gdy cofnę twój czas, utracisz obecny poziom i doświadczenie. Zachowasz zadania, umiejętności, wyposażenie i wszystkie wcześniejsze dary. Czy mam rozpocząć odrodzenie?",
				null);

		npc.add(new ConversationStates[] {
				ConversationStates.QUESTION_1, ConversationStates.QUESTION_2 },
				ConversationPhrases.NO_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Niech więc czas płynie dalej swoim biegiem. Wróć, gdy będziesz gotów.",
				null);

		final ChatCondition canReborn = new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence,
					final Entity entity) {
				return RebornSystem.canReborn(player);
			}
		};

		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				canReborn,
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
						// Teleport first. Zone changes clear transient events, so the
						// achievement event must be created after the teleport.
						new TeleportAction(HOME, 11, 4, Direction.DOWN),
						new ChatAction() {
							@Override
							public void fire(final Player player, final Sentence sentence,
									final EventRaiser raiser) {
								final int reborns = RebornSystem.performReborn(player);
								if (reborns > 0) {
									player.sendPrivateText(rebornCompleteText(reborns));
								}
							}
						}));

		npc.add(ConversationStates.QUESTION_2,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Nić czasu wymknęła się z moich szponów. Osiągnij ponownie najwyższy poziom i wtedy spróbujemy jeszcze raz.",
				null);
	}

	private String firstFiveOffer(final int nextReborn) {
		final int attackBonus = nextReborn * 2;
		final int experienceBonus = nextReborn * 5;
		final StringBuilder text = new StringBuilder();
		text.append("Mogę cofnąć twoją drogę po raz ").append(nextReborn)
				.append(". Po tym odrodzeniu twoje ciosy otrzymają łącznie ")
				.append(attackBonus).append(" procent premii, a doświadczenie zdobywane w walce ")
				.append(experienceBonus).append(" procent premii.");

		if (nextReborn == 3) {
			text.append(" Zachowam też dla ciebie dwa szczególne sztylety jako pamiątkę trzeciej podróży.");
		} else if (nextReborn == 4) {
			text.append(" Czwarta podróż przyniesie ci także mithrilowy amulet.");
		} else if (nextReborn == 5) {
			text.append(" Piąta podróż zakończy drogę szczególnych darów i przyniesie ci ekskalibur.");
		}
		text.append(" Czy chcesz narodzić się ponownie?");
		return text.toString();
	}

	private String rebornCompleteText(final int reborns) {
		if (reborns <= 5) {
			return "Yerena cofnęła twoją nić czasu. To twoje odrodzenie numer "
					+ reborns + ". Wróć do świata i rozpocznij kolejną część swojej historii.";
		}
		return "Nić czasu ponownie zatoczyła krąg. To twoje odrodzenie numer "
				+ reborns + ". Wszystkie szczególne dary Yereny są już z tobą, lecz twoja droga może rozpocząć się ponownie.";
	}
}
