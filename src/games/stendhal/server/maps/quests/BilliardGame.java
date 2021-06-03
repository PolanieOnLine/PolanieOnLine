/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Ball;
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
import marauroa.common.Pair;

public class BilliardGame extends AbstractQuest {
	private static final String QUEST_SLOT = "bilard";

	private static final int STAKE = 250; // Cost

	@Override
	public void addToWorld() {
		final CroupierNPC npc = (CroupierNPC) SingletonRepository.getNPCList().get("Maris");

		final Map<Integer, Pair<String, String>> prizes = initPrices();

		npc.setPrizes(prizes);

		final StendhalRPZone zone = npc.getZone();

		Sign blackboard = new Sign();
		blackboard.setPosition(3, 1);
		blackboard.setEntityClass("blackboard");
		StringBuilder prizelistBuffer = new StringBuilder("NAGRODY:\n");
		for (int i = 9; i >= 5; i--) {
			prizelistBuffer.append("\n" + i + ": " + prizes.get(i).first());
		}
		blackboard.setText(prizelistBuffer.toString());
		zone.add(blackboard);

		blackboard = new Sign();
		blackboard.setPosition(4, 1);
		blackboard.setEntityClass("blackboard");
		prizelistBuffer = new StringBuilder("NAGRODY:\n");
		for (int i = 4; i >= 1; i--) {
			prizelistBuffer.append("\n" + i + ": " + prizes.get(i).first());
		}
		blackboard.setText(prizelistBuffer.toString());
		zone.add(blackboard);

		npc.add(ConversationStates.ATTENDING, Arrays.asList("play", "graj", "zagraj", "gramy", "zagram"), null,
				ConversationStates.QUESTION_1,
				"Aby zagrać musisz zapłacić " + STAKE
						+ " złota. Chcesz zagrać?", null);

		npc.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("money", STAKE),
			ConversationStates.ATTENDING,
			"Dobrze, oto biała bila do gry w bilarda. Połóż ją na stole, gdy będziesz gotowy wekij od bilarda i rozbij bile. Masz tylko jedną szansę! Powodzenia!",
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					player.drop("money", STAKE);
					final Ball ball = (Ball) SingletonRepository.getEntityManager()
							.getItem("biała bila");
					ball.setCroupierNPC((CroupierNPC) npc.getEntity());
					player.equipOrPutOnGround(ball);
				}
			});

		npc.add(ConversationStates.QUESTION_1,
			ConversationPhrases.YES_MESSAGES,
			new NotCondition(new PlayerHasItemWithHimCondition("money", STAKE)),
			ConversationStates.ATTENDING,
			"Hej! Nie masz pieniędzy!", null);

		npc.add(
			ConversationStates.QUESTION_1,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Tchórz! Jak chcesz zostać bohaterem skoro boisz się zaryzykować?",
			null);

		fillQuestInfo(
				"Gra w bilarda",
				"Spróbuj szczęścia w 9 ball w tawernie w Gdańsku.",
				true);
	}

	private Map <Integer, Pair<String, String>> initPrices() {
		Map<Integer, Pair<String, String>> map = new HashMap<Integer, Pair<String, String>>();
		map.put(1, new Pair<String, String>("zwój tatrzański",
				"Łał, udało ci się zbić bilę o numerze 1. Tak więc... w nagrodę otrzymujesz zwój tatrzański!"));
		map.put(2, new Pair<String, String>("sok z chmielu",
				"Oto nagroda pocieszenia, sok z chmielu."));
		map.put(3, new Pair<String, String>("napój z winogron",
				"Wygrałeś ten wyborny napój z winogron!"));
		map.put(4, new Pair<String, String>("złota kolczuga",
				"Weź złotą kolczugę jako nagrodę."));
		map.put(5, new Pair<String, String>("spodnie lazurowe",
				"Mam nadzieję, że przydadzą się Tobie te spodnie lazurowe."));
		map.put(6,	new Pair<String, String>("tarcza piaskowa",
			    "Oto nowa i lśniąca tarcza piaskowa. Może Cię przed czymś ochroni!"));
		map.put(7, new Pair<String, String>("kanapka",
				"Wygrałeś pyszną kanapkę!"));
		map.put(8, new Pair<String, String>("ciupaga",
				"Chyba zostaniesz od dziś góralem. Oto ciupaga!"));
		map.put(9, new Pair<String, String>("spodnie mainiocyjskie",
				"GOLDEN BREAK! ŁAŁ!! Oto główna nagroda, spodnie mainiocyjskie!"));

		return map;
	}

	@Override
	public String getName() {
		return "Gra w Bilarda";
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
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getRegion() {
		return Region.GDANSK_CITY;
	}

	@Override
	public String getNPCName() {
		return "Maris";
	}
}
