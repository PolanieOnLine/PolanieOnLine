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
package games.stendhal.server.maps.quests;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.item.RingOfLife;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * QUEST: The Ring Maker
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Ognir, who works in the weapon shop in Fado
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>If you go to Ognir with a broken emerald ring he offers to fix it </li>
 * <li>Bring him the money he wants (a lot) and gold to fix the ring.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>Fixed Ring</li>
 * <li>500 XP</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>Anytime you need it</li>
 * </ul>
 * 
 * NOTE: This quest uses the same NPC as Marriage.java, we need to be careful
 * not to interfere with that mission.
 */
public class RingMaker extends AbstractQuest {

	private static final String FORGING = "forging";

	private static final int REQUIRED_GOLD = 2;

	private static final int REQUIRED_MONEY = 80000;

	private static final int REQUIRED_GEM = 1;

	private static final int REQUIRED_MINUTES = 10;

	private static final String QUEST_SLOT = "fix_emerald_ring";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	void fixRingStep(final SpeakerNPC npc) {

		npc.add(ConversationStates.ATTENDING, Arrays.asList("pierścień szmaragdowy","pierścień szmaragdowy", "life", "szmaragd"),
			new AndCondition(new PlayerHasItemWithHimCondition("pierścień szmaragdowy"), 
					         new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, FORGING))),
			ConversationStates.QUEST_ITEM_BROUGHT, 
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final RingOfLife emeraldRing = (RingOfLife) player.getFirstEquipped("pierścień szmaragdowy");
					
						if (emeraldRing.isBroken()) {
							npc.say("Co za szkoda twój pierścień szmaragdowy jest zepsuty. Mogę go naprawić ale to #kosztuje.");
						} else {
							// ring is not broken so he just lets player know
							// where it can be fixed
							npc.say("Cześć. Na twoim pierścieniu znajduje się rzadki szmaragd. Jeżeli się zepsuje to przyjdź do mnie, a ja go naprawię.");
							npc.setCurrentState(ConversationStates.ATTENDING);
						}
					
				}
			});
		
		npc.add(ConversationStates.ATTENDING, 
				Arrays.asList("pierścień szmaragdowy", "life", "szmaragd"),
				new AndCondition(new NotCondition(new PlayerHasItemWithHimCondition("pierścień szmaragdowy")), 
				         new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, FORGING))),

				ConversationStates.ATTENDING, 
				"Ciężko jest zdobyć pierścień życia. Wyświadczysz przysługę wszechmocnemu elfowi w Nalwor i będziesz mógł dostać nagrodę."
				, null);
	
		npc.add(ConversationStates.ATTENDING, 
				Arrays.asList("pierścień szmaragdowy", "life", "szmaragd"),
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, FORGING),
						new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, FORGING + "unbound")),
						new TimePassedCondition(QUEST_SLOT,1,REQUIRED_MINUTES)),
				ConversationStates.ATTENDING, 
				"Z radością ci powiem, że twój pierścień szmaragdowy udało się naprawić! Jest teraz jak nowy.",
				new MultipleActions(
						new IncreaseXPAction(500),
						new SetQuestAction(QUEST_SLOT, "done"),
						new EquipItemAction("pierścień szmaragdowy", 1, true)));
		
		npc.add(ConversationStates.ATTENDING, 
				Arrays.asList("pierścień szmaragdowy", "life", "szmaragd"),
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, FORGING),
						new QuestStateStartsWithCondition(QUEST_SLOT, FORGING + "unbound"),
						new TimePassedCondition(QUEST_SLOT,1,REQUIRED_MINUTES)),
				ConversationStates.ATTENDING, 
				"Z radością ci powiem, że twój pierścień życia udało się naprawić! Jest teraz jak nowy",
				new MultipleActions(
						new IncreaseXPAction(500),
						new SetQuestAction(QUEST_SLOT, "done"),
						new EquipItemAction("pierścień szmaragdowy", 1, false)));

		npc.add(ConversationStates.ATTENDING, 
				Arrays.asList("pierścień szmaragdowy", "life", "szmaragd"),
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, FORGING),
						new NotCondition(new TimePassedCondition(QUEST_SLOT,1,REQUIRED_MINUTES))),
				ConversationStates.IDLE, null,
				new SayTimeRemainingAction(QUEST_SLOT,1, REQUIRED_MINUTES,"Nie skończyłem jeszcze naprawy twego pierścienia życia. Powróć proszę za "));
		
		
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT, 
				"kosztuje", 
				null,
				ConversationStates.QUEST_ITEM_BROUGHT,
				"Zapłata za moją usługę wynosi " + REQUIRED_MONEY
					+ " money oraz potrzebuję " + REQUIRED_GOLD
					+ " sztabka złota, a także " + REQUIRED_GEM
					+ " szmaragd, aby naprawić pierścień. Czy chcesz teraz zapłacić?",
				null);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES, 
				new AndCondition(
						new PlayerHasItemWithHimCondition("sztabka złota", REQUIRED_GOLD),
						new PlayerHasItemWithHimCondition("money", REQUIRED_MONEY),
						new PlayerHasItemWithHimCondition("szmaragd", REQUIRED_GEM)), 
				ConversationStates.IDLE,
				"Dobrze to wszystko czego potrzebuję do naprawienia pierścienia. Wróć za "
						+ REQUIRED_MINUTES
						+ " minutę" + ", a będzie gotowy. Dowidzenia.",
				new MultipleActions(
						new DropItemAction("sztabka złota", REQUIRED_GOLD),
						new DropItemAction("szmaragd", REQUIRED_GEM),
						new DropItemAction("money", REQUIRED_MONEY), 
						new ChatAction() {
							@Override
							public void fire(final Player player,
									final Sentence sentence,
									final EventRaiser npc) {
								final RingOfLife emeraldRing = (RingOfLife) player.getFirstEquipped("pierścień szmaragdowy");
								if (player.isBoundTo(emeraldRing)) {
									player.setQuest(QUEST_SLOT, "forging;"
											+ System.currentTimeMillis());
								} else {
									player.setQuest(QUEST_SLOT, "forgingunbound;"
											+ System.currentTimeMillis());
								}

							}
						},
						new DropItemAction("pierścień szmaragdowy")));
		
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES, 
				new NotCondition(new  AndCondition(
						new PlayerHasItemWithHimCondition("sztabka złota", REQUIRED_GOLD),
						new PlayerHasItemWithHimCondition("money", REQUIRED_MONEY),
						new PlayerHasItemWithHimCondition("szmaragd", REQUIRED_GEM))),
				ConversationStates.IDLE, 
				"Wróć jak będziesz miał pieniądze, szmaragd oraz złoto. Tymczasem żegnaj.",
				null);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES, 
			null,
			ConversationStates.ATTENDING, 
			"Nie ma problemu. Wróć jak będziesz miał pieniądze, szmaragd i złoto.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Twórca Pierścienia",
				"Ognir, ekspert pierścieni, może naprawić uszkodzony pierścień życia.",
				false);
		fixRingStep(npcs.get("Ognir"));
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (player.isEquipped("pierścień szmaragdowy")) { 
			final RingOfLife emeraldRing = (RingOfLife) player.getFirstEquipped("pierścień szmaragdowy");
			if (emeraldRing.isBroken()) {
				res.add("O nie! Mój pierścień szmaragdowy jest uszkodzony. Muszę poszukać rzemieślnika, który mi go naprawi.");
			}
		}
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		// Note: this will not be seen till the forging stage starts as no quest slot is set before then.
		res.add("Ognir powiedział, że może naprawić mój pierścień życia za: szmaragd, 80000 money i 2 złote sztabki.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.startsWith(FORGING) && new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES).fire(player,null, null)) {
				res.add("Mój pierścień jest gotowy do odebrania u Ognira! Muszę powiedzieć mu \"szmaragd \", aby go odzyskać.");
		} else if (questState.startsWith(FORGING) || isCompleted(player))  {
				res.add("Ognir powiedział, że naprawa pierścienia będzie trwać 10 minut. Muszę mu powiedzieć \"szmaragd\" aby wiedział, że to mój pierścień.");
		}
		if (isCompleted(player)) {
			res.add("Mój pierścień życia jest jak nowy i znowu będzie działać, aby ograniczyć moje straty, gdy zginę.");
		}
		return res;
	}

	@Override
	public String getName() {
		return "RingMaker";
	}
	
	@Override
	public String getRegion() {
		return Region.FADO_CITY;
	}

	@Override
	public String getNPCName() {
		return "Ognir";
	}
}
