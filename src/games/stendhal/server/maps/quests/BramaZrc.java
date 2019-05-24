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
// Based on UltimateCollector and HelpMrsYeti.
 
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.player.Player;

public class BramaZrc extends AbstractQuest {

	private static final String QUEST_SLOT = "brama_zrc";

	private static final String ARMOR_DAGOBERT_QUEST_SLOT = "armor_dagobert";

	private static Logger logger = Logger.getLogger(BramaZrc.class);


	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Cień");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isBadBoy()) {
						raiser.say("Z twej ręki zginął rycerz! Nie masz tu czego szukać, pozbądź się piętna czaszki. A teraz precz mi z oczu!");
					} else {
						if (player.isQuestCompleted(ARMOR_DAGOBERT_QUEST_SLOT)) {
							if (player.getLevel() >= 150) {
								if (player.hasKilledSolo("czerwony smok")) {
									if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
										raiser.say("Czyżbyś chciał przekroczyć bramy Zakonu Rycerzy Cienia poznać to co nie poznane? Jesteś zainteresowany?");
									} else if (player.isQuestCompleted(QUEST_SLOT)) {
										raiser.say("Och! Ponownie przybyłeś do mnie. Niestety mamy już stałego dostawce żywności i twoja pomoc jest zbędna.");
										raiser.setCurrentState(ConversationStates.ATTENDING);
									}
								} else {
									npc.say("Rozmawiam tylko z osobami, które wykazały się w walce zabijając samodzielnie czerwonego smoka.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Nie jesteś godny aby poznać wiedzę i mądrość Pradawnych. Wróć wtedy gdy twój stan społeczny się zmieni na lepsze. Musisz mieć minimum 50 lvl.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Pomóż mojemu staremu znajomemu Dagobert z Semos wtedy pogadamy!.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

					raiser.say("Będę potrzebował kilku #rzeczy do mojego magazynu żywności.");
					player.setQuest(QUEST_SLOT, "start");
					player.addKarma(10);

				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE,
			"Nie to nie.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Cień");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("rzeczy", "przedmiotów"),
				new QuestInStateCondition(QUEST_SLOT, "start"),
				ConversationStates.ATTENDING, "Mój magazyn jest pusty a rycerze tego zakonu muszą coś jeść. Tu mam #listę potrzebnych artykułów.",
				new SetQuestAction(QUEST_SLOT, "dostawca"));

	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Cień");

		npc.add(ConversationStates.ATTENDING, Arrays.asList("lista", "listę", "artykuły"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"dostawca"),
								 new NotCondition(
								 new AndCondition(new PlayerHasItemWithHimCondition("stek",100),
												  new PlayerHasItemWithHimCondition("ser",100),
												  new PlayerHasItemWithHimCondition("szynka",100),
												  new PlayerHasItemWithHimCondition("mięso",100),
												  new PlayerHasItemWithHimCondition("chleb",50),
												  new PlayerHasItemWithHimCondition("kanapka",25),
												  new PlayerHasItemWithHimCondition("jabłko",25),
												  new PlayerHasItemWithHimCondition("lody",5),
												  new PlayerHasItemWithHimCondition("butelka wody",25)))),
				ConversationStates.ATTENDING, "Potrzebuję:\n" 
									+ "#'100 steków'\n" 
									+ "#'100 sera'\n" 
									+ "#'100 szynki'\n" 
									+ "#'100 mięsa'\n"
									+ "#'50 chlebów'\n" 
									+ "#'25 kanapek'\n" 
									+ "#'25 jabłek'\n" 
									+ "#'5 lodów'\n" 
									+ "#'25 butek wody'\n" 
									+ "Proszę przynieś mi wszystko naraz. Słowo klucz to #'/artykuły/'. Dziękuję!", null);

		final List<ChatAction> cienactions = new LinkedList<ChatAction>();
		cienactions.add(new DropItemAction("stek",100));
		cienactions.add(new DropItemAction("ser",100));
		cienactions.add(new DropItemAction("szynka",100));
		cienactions.add(new DropItemAction("mięso",100));
		cienactions.add(new DropItemAction("chleb",50));
		cienactions.add(new DropItemAction("kanapka",25));
		cienactions.add(new DropItemAction("jabłko",25));
		cienactions.add(new DropItemAction("lody",5));
		cienactions.add(new DropItemAction("butelka wody",25));
		cienactions.add(new EquipItemAction("klucz do bram Zakonu", 1, true));
		cienactions.add(new IncreaseXPAction(100000));
		cienactions.add(new IncreaseKarmaAction(140));
		cienactions.add(new SetQuestAction(QUEST_SLOT, "done"));


		npc.add(ConversationStates.ATTENDING, Arrays.asList("lista", "listę", "artykuły"),
			new AndCondition(new QuestInStateCondition(QUEST_SLOT,"dostawca"),
							 new PlayerHasItemWithHimCondition("stek",100),
							 new PlayerHasItemWithHimCondition("ser",100),
							 new PlayerHasItemWithHimCondition("szynka",100),
							 new PlayerHasItemWithHimCondition("mięso",100),
							 new PlayerHasItemWithHimCondition("chleb",50),
							 new PlayerHasItemWithHimCondition("kanapka",25),
							 new PlayerHasItemWithHimCondition("jabłko",25),
							 new PlayerHasItemWithHimCondition("lody",5),
							 new PlayerHasItemWithHimCondition("butelka wody",25)),
			ConversationStates.ATTENDING, "Wspaniale! Nasza spiżarnia jest teraz pełna. W nagrodę weź ten klucz." +
										  " Nikt ci nie powiedział, że my musimy jeść aby żyć. Widzę, że nie znasz się jeszcze na duchach i zjawach." +
										  " Idź do zamku, aby posiąść wiedzę i mądrość Pradawnych o tym co widzialne i niewidzialne." +
										  " Bramy do niego od teraz stoją dla ciebie otworem!",
			new MultipleActions(cienactions));
	}


	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Brama ZRC",
				"Cień, strażnik bramy Zakonu Rycerzy Cienia ma dla ciebie zadanie.",
				true);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Spotkałem strażnika bram zakonu ZRC.");
		res.add("Cień poprosił mnie o uzupełnienie jego magazynów.");
		if ("rejected".equals(questState)) {
			res.add("Nie mam ochoty pomagać Cieniowi...");
			return res;
		} 
		if ("start".equals(questState)) {
			return res;
		} 
		res.add("Cień poprosił abym mu dostarczył: 100 steków, 100 sera, 100 szynki, 100 mięsa, 50 chlebów, 25 kanapek, 25 jabłek, 5 lodów i 25 butelek wody.");
		if ("dostawca".equals(questState)) {
			return res;
		} 
		res.add("Cień jest szczęśliwy z mojej pomocy. W zamian dostałem klucz do bram zakonu.");
		if (isCompleted(player)) {
			return res;
		} 

		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
		return debug;
	}

	@Override
	public String getName() {
		return "BramaZrc";
	}
	@Override
	public String getNPCName() {
		return "Cień";
	}
}
