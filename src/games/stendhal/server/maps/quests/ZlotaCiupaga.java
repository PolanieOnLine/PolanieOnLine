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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.MathHelper;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

/**
 * QUEST: The złota ciupaga upgrading.
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Józek will upgrade for you the złota ciupaga.
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Józek tells you about the złota ciupaga z wąsem.
 * <li> He offers to upgrade a złota ciupaga for you if you bring him what it
 * needs.
 * <li> You give him all what he ask you.
 * <li> He tells you you must have killed a złota śmierć
 * <li> Józek upgrade the złota ciupaga to złota ciupaga z wąsem for you
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>złota ciupaga
 * <li>20000 XP
 * <li>100 Karma
 * </ul>
 * 
 * 
 * REPETITIONS:
 * <ul>
 * <li> None.
 * </ul>
 */
public class ZlotaCiupaga extends AbstractQuest {

	private static final int REQUIRED_WAIT_DAYS = 1;

	private static final int REQUIRED_MINUTES = 360;

	private static final String QUEST_SLOT = "andrzej_make_zlota_ciupaga";

	private static final String GAZDA_JEDRZEJ_NAGRODA_QUEST_SLOT = "gazda_jedrzej_nagroda";

	private static Logger logger = Logger.getLogger(ZlotaCiupaga.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Kowal Andrzej");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isQuestCompleted(GAZDA_JEDRZEJ_NAGRODA_QUEST_SLOT)) {
						if(player.getLevel() >= 120) {
							if(player.getKarma() >=100) {
								if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
									raiser.say("Jakiś czas temu zrobiłem złotą ciupagę. Mogę zrobić ją dla Ciebie. Jesteś zainteresowany?");
								} else if (player.getQuest(QUEST_SLOT).startsWith("done;")) {
									final String[] waittokens = player.getQuest(QUEST_SLOT).split(";");
									final long waitdelay = REQUIRED_WAIT_DAYS * MathHelper.MILLISECONDS_IN_ONE_DAY;
									final long waittimeRemaining = (Long.parseLong(waittokens[1]) + waitdelay) - System.currentTimeMillis();
									if (waittimeRemaining > 0) {
										raiser.say("Jestem zmęczony. Powróć za " + TimeUtil.approxTimeUntil((int) (waittimeRemaining / 1000L)) + ".");
									} else {
										raiser.say("Potrzebujesz kolejnej złotej ciupagi?");
										raiser.setCurrentState(ConversationStates.QUEST_OFFERED);
									}
								} else if (player.getQuest(QUEST_SLOT).startsWith("make;")) {
									raiser.say("Dlaczego zawracasz mi głowę, jeszcze pracuję nad twoją ciupagą." );
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Nie jesteś godny, aby dzierżyć tak wspaniałą broń. Popracuj nad dobrymi uczynkami. Twoja karma musi być minimum 100.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Twój stan społeczny jest zbyt niski aby podjąć te zadanie. Wróć gdy zdobędziesz 120 lvl.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					} else {
						npc.say("Nie zdobyłeś uznania u Gazdy Jędrzeja.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

					raiser.say("Będę potrzebował kilku #rzeczy do zrobienia ciupagi.");
					player.setQuest(QUEST_SLOT, "start");
					player.addKarma(10);

				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE,
			"Twoja strata.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Kowal Andrzej");


		final List<ChatAction> ciupagaactions = new LinkedList<ChatAction>();
		ciupagaactions.add(new DropItemAction("ciupaga",1));
		ciupagaactions.add(new DropItemAction("sztabka złota",25));
		ciupagaactions.add(new DropItemAction("polano",5));
		ciupagaactions.add(new DropItemAction("money",50000));
		ciupagaactions.add(new SetQuestAction(QUEST_SLOT, "make;" + System.currentTimeMillis()));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("rzeczy", "przedmiot", "nagroda", "ciupaga", "przypomnij"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"start"),
								 new PlayerHasItemWithHimCondition("ciupaga",1),
								 new PlayerHasItemWithHimCondition("sztabka złota",25),
								 new PlayerHasItemWithHimCondition("polano",5),
								 new PlayerHasItemWithHimCondition("money",50000)),
				ConversationStates.ATTENDING, "Widzę, że masz wszystko o co cię prosiłem. Wróć za 6 godzin a ciupaga będzie gotowa.",
				new MultipleActions(ciupagaactions));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("rzeczy", "przedmiot", "nagroda", "ciupaga", "przypomnij"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT,"start"),
								 new NotCondition(
								 new AndCondition(new PlayerHasItemWithHimCondition("ciupaga",1),
												  new PlayerHasItemWithHimCondition("sztabka złota",25),
												  new PlayerHasItemWithHimCondition("polano",5),
												  new PlayerHasItemWithHimCondition("money",50000)))),
				ConversationStates.ATTENDING, "Potrzebuję:\n"
									+"#'1 ciupagę'\n"
									+"#'25 sztabek złota'\n"
									+"#'5 polan'\n"
									+"#'50000 money'\n"
									+"Proszę przynieś mi to wszystko naraz."
									+" Jeżeli zapomnisz co masz przynieść to powiedz #przypomnij. Dziękuję!", null);

	}

	private void step_3() { 
		final SpeakerNPC npc = npcs.get("Kowal Andrzej");

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("ciupaga", "złota", "nagroda"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
			new QuestStateStartsWithCondition(QUEST_SLOT, "make;")),
			ConversationStates.IDLE, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
					final long delay = REQUIRED_MINUTES * MathHelper.MILLISECONDS_IN_ONE_MINUTE; 
					final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();

					if (timeRemaining > 0L) {
						raiser.say("Wciąż pracuje nad twoim zleceniem. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ", aby odebrać nagrodę. No chyba że chcesz abym odlał dla ciebie #żelazo.");
						return;
					}

					raiser.say("Warto było czekać. A oto złota ciupaga. Dowidzenia!");
					player.addXP(15000);
					player.addKarma(25);
					final Item zlotaCiupaga = SingletonRepository.getEntityManager().getItem("złota ciupaga");
					zlotaCiupaga.setBoundTo(player.getName());
					player.equipOrPutOnGround(zlotaCiupaga);
					player.notifyWorldAboutChanges();
					player.setQuest(GAZDA_JEDRZEJ_NAGRODA_QUEST_SLOT, "rejected");
					player.setQuest(QUEST_SLOT, "done" + ";" + System.currentTimeMillis());

				}
			});
	}


	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Rzeczy dla Andrzeja",
			"Andrzej o ile na to zasługujesz wykona dla Ciebie złotą ciupagę.",
			false);

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
		res.add("Spotkałem kowala Andrzeja w kuźni Zakopane.");
		res.add("Kowal Andrzej zaproponował mi zrobienie złotej ciupagi.\n"+
				"Zażądał za zrobienie złotej ciupagi: 1 ciupagę, 25 sztabek złota, 5 polan i 50000 money. Mam mu to wszystko razem dostarczyć.");
		if ("rejected".equals(questState)) {
			res.add("Po co mi ciupaga.");
			return res;
		} 
		if ("start".equals(questState)) {
			return res;
		}
		res.add("Dostarczyłem wszystkie potrzebne rzeczy Andrzejowi.");
		if (questState.startsWith("make")) {
			if (new TimePassedCondition(QUEST_SLOT,1,REQUIRED_MINUTES).fire(player, null, null)) {
				res.add("Wstąpię do Anrzeja po poją ciupagę. Hasło: ciupaga.");
			} else {
				res.add("Muszę poczekać 6 godzin. Tyle czasu zajmie Andrzejowi zrobienie ciupagi. Hasło: ciupaga.");
			}
			return res;
		}
		res.add("Kowal Andrzej wręczył mi złotą ciupagę.");
		if (isCompleted(player)) {
			return res;
		}
		res.add("Kowal Andrzej jest gotowy zrobić dla mnie złotą ciupagę jeszcze raz!");
		if(isRepeatable(player)) {
			return res;
		} else {
			res.add("Kowal Andrzej jest teraz zajęty mam przyjść innym razem.");
		}
		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
		return debug;
	}

	@Override
	public String getName() {
		return "ZlotaCiupaga";
	}
	@Override
	public String getNPCName() {
		return "Kowal Andrzej";
	}
}