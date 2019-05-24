/* $Id: GawedziarzNPC.java,v 1.27 2011/06/19 14:05:01 Legolas Exp $ */
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
 //zrobione na podstawie ExperiencedWarriorNPC z Semos/plains
 
package games.stendhal.server.maps.zakopane.city;

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
 * Experienced warrior knowing a lot about creatures (location zakopane_s).
 * Original name: Starkad
 *
 * @author johnnnny
 */

public class GawedziarzNPC implements ZoneConfigurator  {

	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		buildNPC(zone);
	}

	/**
	 * cost of the information for players. Final cost is: INFORMATION_BASE_COST +
	 * creatureLevel * INFORMATION_COST_LEVEL_FACTOR
	 */
	static final int INFORMATION_BASE_COST = 20;

	/**
	 * multiplier of the creature level for the information cost.
	 */
	static final double INFORMATION_COST_LEVEL_FACTOR = 2;

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
	 * literal for how dangerous a creature is based on the percentual
	 * difference to player level %s is replaced with singular creature name, %S
	 * with plural.
	 */
	private static Map<Double, String> dangerLiterals;

	/**
	 * literals for line starts. %s is replaced with singular creature name, %S
	 * plural.
	 */
	private static final String[] LINE_STARTS = new String[] { "O, tak. Znam stwory zwane %s!",
			"Kiedy zek był w twoich latach z mej ręki padł niejeden potwór zwany %s!",
			"%s , ale zek go lubiał gonić!",
			"Niekze przybocym... %s... Teroz juz wiem!",
			"Raz prawie zek padł, gdym spotkoł potwóra zwanego %a !",
			"Stoczyłem piekne potyczki z potworem co sie zwie %S!", };

	static {
		probabilityLiterals = new LinkedHashMap<Double, String>();
		probabilityLiterals.put(100.0, "zawse %s");
		probabilityLiterals.put(99.99, "prawie zawse %s");
		probabilityLiterals.put(75.0, "niezwykle cynsto %s");
		probabilityLiterals.put(55.0, "cynściej niz co drugi raz %s");
		probabilityLiterals.put(40.0, "bardzo cynsto %s");
		probabilityLiterals.put(20.0, "cynsto %s");
		probabilityLiterals.put(5.0, "czasem %s");
		probabilityLiterals.put(1.0, "rzadko %s");
		probabilityLiterals.put(0.1, "barz rzadko %s");
		probabilityLiterals.put(0.001, "niezwykle rzadko %s");
		probabilityLiterals.put(0.0001, "raz na tysionc ma przy sobie %s");
		probabilityLiterals.put(0.00000001,
				"podobno ma tys %s, ale o tym zek nie słysoł");
		probabilityLiterals.put(0.0, "nigdy %s");

		amountLiterals = new LinkedHashMap<Integer, String>();
		amountLiterals.put(2000, "tysiene %s");
		amountLiterals.put(200, "setki %s");
		amountLiterals.put(100, "wiele %s");
		amountLiterals.put(10, "pare %s");
		amountLiterals.put(2, "kilka %s");
		amountLiterals.put(1, "%a");

		dangerLiterals = new LinkedHashMap<Double, String>();
		dangerLiterals.put(40.0, "%s usiekł by cie za jednym razem!");
		dangerLiterals.put(15.0,
				"%s jest raczej zabójczy dla ciebie, nie atakuj go w pojedynkę!");
		dangerLiterals.put(2.0, "%s stośnie niebezpiecny dla ciebie, uwazuj!");
		dangerLiterals.put(1.8, "%S jest łokropnie niebezpiecny, uwazuj!");
		dangerLiterals.put(1.7,
				"%S są niebezpiecne dlo ciebie, trzymoj mikstury w zapasku!");
		dangerLiterals.put(1.2,
				"Dyć i moze jest niebezpiecny dlo ciebie, uwazuj na swe zycie!");
		dangerLiterals.put(0.8,
				"Zabicie jednego moze być honorne dlo ciebie!");
		dangerLiterals.put(0.5, "Zabicie potwora co sie zwie %s powinno dlo ciebie jak splunonć!");
		dangerLiterals.put(0.3, "Zabicie potwora co sie zwie %s to dlo ciebie łatwizna.");
		dangerLiterals.put(0.0, "%s nie stanowi dlo ciebie wyzwania.");
	}

	/**
	 * %1 = time to respawn.
	 */
	private static final String[] RESPAWN_TEXTS = new String[] {
			"Jezeli pocekos w odpowiednim miejscu przez %1 to dyć i moze obocys jednego.",
			"Zrodzi sie %1 po swej śmiyżci", "Upolowanie go za %1 da tobie szanse znalezienia cegoś w jego truchle." };

	/**
	 * %1 = list of items dropped.
	 */
	private static final String[] CARRY_TEXTS = new String[] { "Mają przy sobie %1.",
			"Martwy ma %1.", "Truchło zawiera %1." };

	/**
	 * no attributes.
	 */
	private static final String[] CARRY_NOTHING_TEXTS = new String[] {
			"Nie wiem czy cosik noszą.",
			"Nie widziołek, aby który z nich cosik nosił." };

	/**
	 * %1 = list of locations.
	 */
	private static final String[] LOCATION_TEXTS = new String[] {
			"Widziołek %1.", "Powinieneś znaleść %1.",
			"Zabiłek kilka %1." };

	/**
	 * %1 = name of the creature.
	 */
	private static final String[] LOCATION_UNKNOWN_TEXTS = new String[] { "Pewnie nie tak sie zwał ten potwór. Jo nigdy nie słysołek o %1." };

	private static CreatureInfoZ creatureInfoz = new CreatureInfoZ(probabilityLiterals,
																amountLiterals, dangerLiterals, LINE_STARTS, RESPAWN_TEXTS,
																CARRY_TEXTS, CARRY_NOTHING_TEXTS, LOCATION_TEXTS,
																LOCATION_UNKNOWN_TEXTS);
																
  private void buildNPC(StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Sabała") {

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
		setLevel(600);

		addJob("Moja praca? Jestem dobrze znanym gawędziarzem w tych górach. Dziwne, ześ nie słysoł o mnie!");
		addQuest("Teroz nie trza mi pomocy.");
		addHelp("Jeśli kcesz moge ci łopowiedzieć o #potworach, które zek spotkał w casie polowacek.");
		addOffer("Z mej stony za kilka groszy moge ci oferować informacje o #potworach, które zek kiedyś widzioł.");

		add(ConversationStates.ATTENDING, Arrays.asList("creature", "potworach", "potwór"), null,
				ConversationStates.QUESTION_1,
				"O którym potworze chciałbyś cosik usłyseć?", null);

		add(ConversationStates.QUESTION_1, "",
				new NotCondition(new TriggerInListCondition(ConversationPhrases.GOODBYE_MESSAGES)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					public void fire(final Player player, final Sentence sentence, final EventRaiser speakerNPC) {
						final String creatureName = sentence.getTriggerExpression().getNormalized();
						final DefaultCreature creature = SingletonRepository.getEntityManager().getDefaultCreature(creatureName);
						if (creature == null) {
							speakerNPC.say("Nigdy nie słysołek o takim potworze! Moze nazwe pokręciłeś. Powiedz jesce roz jak sie zwał.");
							speakerNPC.setCurrentState(ConversationStates.QUESTION_1);
						} else {
							stateInfo.setCreatureName(creatureName);
							if (INFORMATION_BASE_COST > 0) {
								final int informationCost = getCost(player, creature);
								stateInfo.setInformationCost(informationCost);
								speakerNPC.say("Ta informacja kosztuje "
										+ informationCost
										+ ". Masz tyle dutków?");
								speakerNPC.setCurrentState(ConversationStates.BUY_PRICE_OFFERED);
							} else {
								speakerNPC.say(getCreatureInfoZ(player,
										stateInfo.getCreatureName())
										+ " Jeśli chcesz posłuchać o następnym potworze, to powiedź o którym.");
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
								String infoString = getCreatureInfoZ(player, stateInfo.getCreatureName());
								infoString += " Jeśli chcesz posłuchać o następnym potworze, to powiedź o którym.";
								speakerNPC.say(infoString);
								speakerNPC.setCurrentState(ConversationStates.QUESTION_1);
							} else {
								speakerNPC.say("Nie masz tyle dutków.");
							}
						}
					}
				});

		add(ConversationStates.BUY_PRICE_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"To moze wróć później, o ile bedzies jesce chcioł.", null);

		addGoodbye("Idź z Bogiem!");
	}
	
	   @Override
			protected void createPath() {
				final List<Node> nodes = new LinkedList<Node>();
				nodes.add(new Node(35,46));
				nodes.add(new Node(35,82));
				nodes.add(new Node(29,82));
				nodes.add(new Node(29,97));
				nodes.add(new Node(23,97));
				nodes.add(new Node(23,106));
				nodes.add(new Node(2,106));
				nodes.add(new Node(2,77));
				nodes.add(new Node(1,77));
				nodes.add(new Node(1,43));
				nodes.add(new Node(21,43));
				nodes.add(new Node(21,46));

				setPath(new FixedPath(nodes, true));
			}

		};
		npc.setPosition(35, 46);
		npc.setEntityClass("npcstaryprzewodnik");
		npc.setDescription("Oto Sabała, góral podhalański, honorowy przewodnik tatrzański, muzykant, swietny myśliwy, gawędziarz i pieśniarz.");
		zone.add(npc);	
	}	
				
	private static String getCreatureInfoZ(final Player player, final String creatureName) {
		String result;
		final DefaultCreature creature = SingletonRepository.getEntityManager().getDefaultCreature(creatureName);
		if (creature != null) {
			result = creatureInfoz.getCreatureInfoZ(player, creature, 3, 8, true);
		} else {
			result = "Nigdy nie słysołek o takim potworze!";
		}
		return result;
	}
}
