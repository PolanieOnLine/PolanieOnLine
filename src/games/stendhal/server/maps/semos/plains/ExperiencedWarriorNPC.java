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
package games.stendhal.server.maps.semos.plains;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.rule.defaultruleset.DefaultCreature;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.TriggerInListCondition;
import games.stendhal.server.entity.player.Player;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Experienced warrior knowing a lot about creatures (location semos_plains_s).
 * Original name: Starkad
 *
 * @author johnnnny
 */

public class ExperiencedWarriorNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone,
			Map<String, String> attributes) {
		buildNPC(zone);
	}

	/**
	 * cost of the information for players. Final cost is: INFORMATION_BASE_COST +
	 * creatureLevel * INFORMATION_COST_LEVEL_FACTOR
	 */
	static final int INFORMATION_BASE_COST = 2;

	/**
	 * multiplier of the creature level for the information cost.
	 */
	static final double INFORMATION_COST_LEVEL_FACTOR = 3;

	/**
	 * literals for probabilities. %s is replaced with item description (name
	 * and amount)
	 */
	private static Map<Double, String> probabilityLiterals;

	/**
	 * literal for item amounts %s is replaced with singular item name, %a with
	 * "a/an item name" depending on the item name.
	 */
	private static Map<Integer, String> amountLiterals;

	/**
	 * literal for how dangerous a creature is based on the percentage
	 * difference to player level %s is replaced with singular creature name, %S
	 * with plural.
	 */
	private static Map<Double, String> dangerLiterals;

	/**
	 * literals for line starts. %s is replaced with singular creature name, %S
	 * plural.
	 */
	private static final String[] LINE_STARTS = new String[] { "O, tak. Znam stwory zwane %s!",
			"Kiedy byłem w twoim wieku z mojej ręki zginął niejeden potwór zwany %S!",
			"%S to jeden z moich ulubieńców!",
			"Niech pomyślę... %s... Teraz pamiętam!",
			"Raz prawie nie zginąłem, gdy napadł na mnie potwór zwany %a !",
			"Stoczyłem piękne pojedynki z potworem zwanym %S!", };

	static {
		probabilityLiterals = new LinkedHashMap<Double, String>();
		probabilityLiterals.put(100.0, "zawsze %s");
		probabilityLiterals.put(99.99, "prawie zawsze %s");
		probabilityLiterals.put(75.0, "niezwykle często %s");
		probabilityLiterals.put(55.0, "częściej niż co drugi raz %s");
		probabilityLiterals.put(40.0, "bardzo często %s");
		probabilityLiterals.put(20.0, "często %s");
		probabilityLiterals.put(5.0, "czasami %s");
		probabilityLiterals.put(1.0, "rzadko %s");
		probabilityLiterals.put(0.1, "bardzo rzadko %s");
		probabilityLiterals.put(0.001, "niezwykle rzadko %s");
		probabilityLiterals.put(0.0001, "jeden na tysiąc ma przy sobie %s");
		probabilityLiterals.put(0.00000001,
				"podobno też %s, ale o tym tylko słyszałem");
		probabilityLiterals.put(0.0, "nigdy %s");

		amountLiterals = new LinkedHashMap<Integer, String>();
		amountLiterals.put(2000, "tysiące %s");
		amountLiterals.put(200, "setki %s");
		amountLiterals.put(100, "wiele %s");
		amountLiterals.put(10, "parę %s");
		amountLiterals.put(2, "kilka %s");
		amountLiterals.put(1, "%a");

		dangerLiterals = new LinkedHashMap<Double, String>();
		dangerLiterals.put(40.0, "%s zabiłby Cię w sekundę!");
		dangerLiterals.put(15.0,
				"%s jest raczej zabójczy dla Ciebie, nie atakuj go w pojedynkę!");
		dangerLiterals.put(2.0, "%s ekstremalnie niebezpieczny dla Ciebie, uważaj!");
		dangerLiterals.put(1.8, "%S jest bardzo niebezpieczny, uważaj!");
		dangerLiterals.put(1.7,
				"%S są niebezpieczne dla Ciebie, trzymaj mikstury w pogotowiu!");
		dangerLiterals.put(1.2,
				"Prawdopodobnie jest niebezpieczny dla Ciebie, uważaj na zdrowie!");
		dangerLiterals.put(0.8,
				"Zabicie jednego może być nie lada wyzwaniem dla Ciebie!");
		dangerLiterals.put(0.5, "Zabicie potwora zwanego %s powinno dla Ciebie być banalne.");
		dangerLiterals.put(0.3, "Zabicie potwora zwanego %s to dla Ciebie łatwizna.");
		dangerLiterals.put(0.0, "%s nie stanowi dla Ciebie wyzwania.");
	}

	/**
	 * %1 = time to respawn.
	 */
	private static final String[] RESPAWN_TEXTS = new String[] {
			"Jeżeli poczekasz w odpowiednim miejscu przez %1 to może zobaczysz jednego.",
			"Odradzają się %1 po swojej śmierci", "Upolowanie ich za %1 da Tobie szansę znalezienia czegoś." };

	/**
	 * %1 = list of items dropped.
	 */
	private static final String[] CARRY_TEXTS = new String[] { "Mają przy sobie %1.",
			"Martwy ma %1.", "Zwłoki zawierają %1." };

	/**
	 * no attributes.
	 */
	private static final String[] CARRY_NOTHING_TEXTS = new String[] {
			"Nie wiem czy coś noszą.",
			"Nie widziałem, aby któryś z nich coś nosił." };

	/**
	 * %1 = list of locations.
	 */
	private static final String[] LOCATION_TEXTS = new String[] {
			"Widziałem ich %1.", "Powinieneś znaleźć ich %1.",
			"Zabiłem kilka z nich %1." };

	/**
	 * %1 = name of the creature.
	 */
	private static final String[] LOCATION_UNKNOWN_TEXTS = new String[] { "Nie mam pojęcia, gdzie możesz znaleźć potwora zwanego %1." };

	private static CreatureInfo creatureInfo = new CreatureInfo(probabilityLiterals,
																amountLiterals, dangerLiterals, LINE_STARTS, RESPAWN_TEXTS,
																CARRY_TEXTS, CARRY_NOTHING_TEXTS, LOCATION_TEXTS,
																LOCATION_UNKNOWN_TEXTS);

	private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Starkad") {

	@Override
	@SuppressWarnings("all") // "dead"
			public void createDialog() {
				class StateInfo {
					private String creatureName;

					private int informationCost;

					void setCreatureName(final String creatureName) {
						this.creatureName = creatureName;
					}

					String getCreatureName() {
						return creatureName;
					}

					void setInformationCost(final int informationCost) {
						this.informationCost = informationCost;
					}

					int getInformationCost() {
						return informationCost;
					}
				}

		final StateInfo stateInfo = new StateInfo();

				addGreeting();
				setLevel(368);

				addJob("Moja praca? Jestem dobrze znanym wojownikiem. Dziwne, że nie słyszałeś o mnie!");
				addQuest("Dzięki, ale na razie nie potrzebuję pomocy.");
				addHelp("Jeżeli chcesz mogę Ci powiedzieć o #potworach, które spotkałem.");
				addOffer("Oferuję Ci informacje o #potworach, które widziałem. Oczywiście za rozsądną cenę.");

				add(ConversationStates.ATTENDING, Arrays.asList("creature", "potworach", "potwór"), null,
						ConversationStates.QUESTION_1,
						"O którym potworze chciałbyś się dowiedzieć?", null);

				add(ConversationStates.QUESTION_1, "",
						new NotCondition(new TriggerInListCondition(ConversationPhrases.GOODBYE_MESSAGES)),
						ConversationStates.ATTENDING, null,
						new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser speakerNPC) {
						final String creatureName = sentence.getTriggerExpression().getNormalized();
						final DefaultCreature creature = SingletonRepository.getEntityManager().getDefaultCreature(creatureName);
						if (creature == null) {
							speakerNPC.say("Nigdy nie słyszałem o takim potworze! Podaj jeszcze raz jego nazwę.");
							speakerNPC.setCurrentState(ConversationStates.QUESTION_1);
						} else {
							stateInfo.setCreatureName(creatureName);
							if (INFORMATION_BASE_COST > 0) {
								final int informationCost = getCost(player, creature);
								stateInfo.setInformationCost(informationCost);
								speakerNPC.say("Ta informacja kosztuje "
										+ informationCost
										+ ". Wciąż jesteś zainteresowany?");
								speakerNPC.setCurrentState(ConversationStates.BUY_PRICE_OFFERED);
							} else {
								speakerNPC.say(getCreatureInfo(player,
										stateInfo.getCreatureName())
										+ " Jeżeli chcesz posłuchać o następnym potworze to powiedz o którym.");
								speakerNPC.setCurrentState(ConversationStates.QUESTION_1);
							}
						}
					}

					private int getCost(final Player player, final DefaultCreature creature) {
						return (int) (INFORMATION_BASE_COST + INFORMATION_COST_LEVEL_FACTOR
								* creature.getLevel());
					}
				});

				add(ConversationStates.BUY_PRICE_OFFERED,
						ConversationPhrases.YES_MESSAGES, null,
						ConversationStates.ATTENDING, null,
						new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser speakerNPC) {
						if (stateInfo.getCreatureName() != null) {
							if (player.drop("money",
									stateInfo.getInformationCost())) {
								String infoString = getCreatureInfo(player, stateInfo.getCreatureName());
								infoString += " Jeżeli chcesz posłuchać o następnym potworze to powiedz o którym.";
								speakerNPC.say(infoString);
								speakerNPC.setCurrentState(ConversationStates.QUESTION_1);
							} else {
								speakerNPC.say("Nie masz tyle pieniędzy.");
							}
						}
					}
				});

				add(ConversationStates.BUY_PRICE_OFFERED,
						ConversationPhrases.NO_MESSAGES, null, ConversationStates.ATTENDING,
						"Dobrze wróć później, o ile będziesz nadal zainteresowany. Co jeszcze mogę dla ciebie zrobić?", null);

				addGoodbye("Żegnaj i powodzenia!");
			}

			@Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(37,2));
				nodes.add(new Node(37,16));
				nodes.add(new Node(85,16));
				nodes.add(new Node(85,32));
				nodes.add(new Node(107,32));
				nodes.add(new Node(107,2));

				setPath(new FixedPath(nodes, true));
			}

		};
		npc.setPosition(37, 2);
		npc.setEntityClass("experiencedwarriornpc");
		npc.setDescription("Oto Starkad wielki wojownik i obrońca Semos.");
		zone.add(npc);	
	}

	private static String getCreatureInfo(final Player player, final String creatureName) {
		String result;
		final DefaultCreature creature = SingletonRepository.getEntityManager().getDefaultCreature(creatureName);
		if (creature != null) {
			result = creatureInfo.getCreatureInfo(player, creature, 3, 8, true);
		} else {
			result = "Nigdy nie słyszałem o takim potworze!";
		}
		return result;
	}
}
