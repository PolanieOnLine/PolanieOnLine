/* $Id$ */
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
package games.stendhal.server.maps.quests;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Dice;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.CroupierNPC;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import marauroa.common.Pair;

public class DiceGambling extends AbstractQuest {

	private static final int STAKE = 100;

	@Override
	public String getSlotName() {
		return "dice_gambling";
	}

	@Override
	public void addToWorld() {

		final CroupierNPC ricardo = (CroupierNPC) SingletonRepository.getNPCList().get("Ricardo");

		final Map<Integer, Pair<String, String>> prizes = initPrices();

		ricardo.setPrizes(prizes);

		final StendhalRPZone zone = ricardo.getZone();

		Sign blackboard = new Sign();
		blackboard.setPosition(25, 0);
		blackboard.setEntityClass("blackboard");
		StringBuilder prizelistBuffer = new StringBuilder("NAGRODY:\n");
		for (int i = 18; i >= 13; i--) {
			prizelistBuffer.append("\n" + i + ": " + prizes.get(i).first());
		}
		blackboard.setText(prizelistBuffer.toString());
		zone.add(blackboard);

		blackboard = new Sign();
		blackboard.setPosition(26, 0);
		blackboard.setEntityClass("blackboard");
		prizelistBuffer = new StringBuilder("NAGRODY:\n");
		for (int i = 12; i >= 7; i--) {
			prizelistBuffer.append("\n" + i + ": " + prizes.get(i).first());
		}
		blackboard.setText(prizelistBuffer.toString());
		zone.add(blackboard);

		ricardo.add(ConversationStates.ATTENDING, Arrays.asList("play", "graj", "zagraj", "gramy", "zagram"), null,
				ConversationStates.QUESTION_1,
				"Aby zagrać musisz obstawić " + STAKE
						+ " złota. Chcesz zapłacić?", null);

		ricardo.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("money", STAKE),
			ConversationStates.ATTENDING,
			"Dobrze, oto kości do gry. Rzuć je na stół, gdy będziesz gotowy. Powodzenia!",
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					player.drop("money", STAKE);
					final Dice dice = (Dice) SingletonRepository.getEntityManager()
							.getItem("kości do gry");
					dice.setCroupierNPC((CroupierNPC) npc.getEntity());
					player.equipOrPutOnGround(dice);
				}
			});
		
		ricardo.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES, 
			new NotCondition(new PlayerHasItemWithHimCondition("money", STAKE)),
			ConversationStates.ATTENDING,
			"Hej! Nie masz pieniędzy!", null);

		ricardo.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Tchórz! Jak chcesz zostać bohaterem skoro boisz się zaryzykować?",
			null);
		
		fillQuestInfo(
				"Hazard Koścmi",
				"Spróbuj szczęścia w kości w Semos's Tavern.",
				true);
	}

	private Map <Integer, Pair<String, String>> initPrices() {
		Map<Integer, Pair<String, String>> map = new HashMap<Integer, Pair<String, String>>();
		map.put(3, new Pair<String, String>("lazurowa tarcza",
				"Facet, jesteś jednym wielkim pechowcem! Tak mi przykro! Weź tą tarczę lazurową."));
		map.put(7, new Pair<String, String>("sok z chmielu",
				"Oto nagroda pocieszenia, sok z chmielu."));
		map.put(8, new Pair<String, String>("napój z winogron",
				"Wygrałeś ten wyborny napój z winogron!"));
		map.put(9, new Pair<String, String>("tarcza ćwiekowa",
				"Weź tą zwykłą tarczę jako nagrodę."));
		map.put(10, new Pair<String, String>("spodnie kolcze",
				"Mam nadzieję, że przydadzą się Tobie te spodnie kolcze."));
		map.put(11,	new Pair<String, String>("antidotum",
			   "To antidotum  przyda Ci się, gdy będziesz walczył z zatruwającymi potworami."));
		map.put(12, new Pair<String, String>("kanapka",
				"Wygrałeś pyszną kanapkę!"));
		map.put(13, new Pair<String, String>("hotdog z serem",
				"Weź ten smaczny hotdog z serem."));
		map.put(14, new Pair<String, String>("zwój semos",
				"Wygrałeś ten użyteczny zwój semos!"));
		map.put(15,	new Pair<String, String>("duży eliksir",
				"Wygrałeś duży eliksir, ale z Twoim szczęściem pewnie nigdy go nie użyjesz!"));
		map.put(16, new Pair<String, String>("długi łuk",
				"Z tą nagrodą możesz być budzącym grozę łucznikiem!"));
		map.put(17,	new Pair<String, String>("płaszcz karmazynowy",
				"Wyglądasz niesamowicie w tym modnym karmazynowym płaszczu!"));
		map.put(18, new Pair<String, String>("hełm zabójcy",
				"Trafiłeś JACKPOT! Hełm zabójcy!"));
		
		return map;
	}

	@Override
	public String getName() {
		return "DiceGambling";
	}
	
	@Override
	public boolean isVisibleOnQuestStatus() {
		return false;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		return new ArrayList<String>();
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
	}

	@Override
	public String getNPCName() {
		return "Ricardo";
	}

}
