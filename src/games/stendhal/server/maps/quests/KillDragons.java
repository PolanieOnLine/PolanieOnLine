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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import marauroa.common.Pair;

public class KillDragons extends AbstractQuest {
	private static final String QUEST_SLOT = "kill_dragons";
	private static final int DELAY_IN_MINUTES = 60*24;
	private static Logger logger = Logger.getLogger(KillDragons.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step1() {
		final SpeakerNPC npc = npcs.get("Alicja");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Koszmary już mi się nie śnią. Teraz moje sny są wesołe i kolorowe.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Co noc śnią mi się straszne koszmary. Myślisz, że mógłbyś mi pomóc?",
				null);

		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
				toKill.put("zielony smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
				actions.add(new IncreaseKarmaAction(5.0));
				actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
				actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Och, wspaniale! Co noc śni mi się straszny #'/zielony smok/', proszę odszukaj go i zabij. Przynieś mi też 5 skór zielonego smoka."
				+ " Jeśli włożę je pod poduszkę, będą odstraszać koszmary. Pamiętaj, że musisz zabić go samodzielnie, bo skóra nie będzie działała jak talizman!",
				new MultipleActions(actions));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Proszę, jeśli zmienisz zdanie to mi powiedz. Boję się zasypiać, a mama mi powiedziała, że dzieci rosną w czasie snu. Ja chcę urosnąć!",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void step2() {
		final SpeakerNPC npc = npcs.get("Alicja");

		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie zielonego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"start"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("skóra zielonego smoka", 5))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś zielonego smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, przynieś mi te 5 skór zielonego smoka.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"start"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("skóra zielonego smoka", 5)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #zielony.");
						player.drop("skóra zielonego smoka", 5);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "czas_szkielet;"+System.currentTimeMillis());
					}
				});
	}

	private void czas1() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "zielony";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_szkielet"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_szkielet"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"szkielet?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("szkielet smoka", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "szkielet"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomogę"),
					new QuestInStateCondition(QUEST_SLOT, "szkielet?"),
					ConversationStates.ATTENDING,
					"Och, wspaniale! Tej nocy przyśnił mi się #'/szkielet smoka/', proszę odszukaj go i zabij."
					+ " Przynieś mi też jego 20 kości dla psa. Jeśli włożę je pod poduszkę, będą odstraszały koszmary."
					+ " Pamiętaj, że musisz zabić go samodzielnie, bo kości nie będą działały jak talizman!",
				new MultipleActions(actions));
	}

	private void step3() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "szkielet"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie szkielet smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"szkielet"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("kość dla psa", 20))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś szkielet smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, przynieś mi 20 kości dla psa.");
					}
				});
		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"szkielet"),
							 new KilledForQuestCondition(QUEST_SLOT, 1),
							 new PlayerHasItemWithHimCondition("kość dla psa", 20)),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #szkielet.");
					player.drop("kość dla psa", 20);
					player.addKarma(10);
					player.setQuest(QUEST_SLOT, "czas_niebieski;"+System.currentTimeMillis());
				}
			});
	}

	private void czas2() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "szkielet";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_niebieski"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_niebieski"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"niebieski?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("błękitny smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "niebieski"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "niebieski?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/błękitny smok/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 5 skór niebieskiego smoka. Jeśli włożę je pod poduszkę, będą odstraszały koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo skóry nie będą działały jak talizman!",
				new MultipleActions(actions));
	}

	private void step4() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "niebieski"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie błękitnego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"niebieski"),
							 new KilledForQuestCondition(QUEST_SLOT, 1),
							 new NotCondition(new PlayerHasItemWithHimCondition("skóra niebieskiego smoka", 5))),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Nawet jeśli zabiłeś błękitnego smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, przynieś mi skóry niebieskiego smoka.");
				}
			});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"niebieski"),
							 new KilledForQuestCondition(QUEST_SLOT, 1),
							 new PlayerHasItemWithHimCondition("skóra niebieskiego smoka", 5)),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #niebieski.");
					player.drop("skóra niebieskiego smoka", 5);
					player.addKarma(10);
					player.setQuest(QUEST_SLOT, "czas_zgnily;"+System.currentTimeMillis());
				}
			});
	}

	private void czas3() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "niebieski";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_zgnily"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_zgnily"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1,DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"zgnily?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("zgniły szkielet smoka", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "zgnily"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "zgnily?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/zgniły szkielet smoka/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 30 kości dla psa. Jeśli włożę je pod poduszkę, będą odstraszały koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo kości nie będą działały jak talizman!",
				new MultipleActions(actions));

	}

	private void step5() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "zgnily"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie zgniły szkielet smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"zgnily"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("kość dla psa", 30))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś zgniły szkielet smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, przynieś mi kości dla psa.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"zgnily"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("kość dla psa", 30)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #zgnily.");
						player.drop("kość dla psa", 30);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "czas_czerwony;"+System.currentTimeMillis());
					}
				});
	}

	private void czas4() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "zgnily";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_czerwony"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_czerwony"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"czerwony?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("czerwony smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "czerwony"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "czerwony?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/czerwony smok/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 5 skór czerwonego smoka. Jeśli włożę je pod poduszkę, będą odstraszały koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo skóry nie będą działały jak talizman!",
				new MultipleActions(actions));
	}

	private void step6() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "czerwony"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie czerwonego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"czerwony"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("skóra czerwonego smoka", 5))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś czerwonego smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, dostarcz mi skóry czerwonego smoka.");
					}
		});
		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"czerwony"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("skóra czerwonego smoka", 5)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #czerwony.");
						player.drop("skóra czerwonego smoka", 5);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "czas_pustynny;"+System.currentTimeMillis());
					}
				});
	}

	private void czas5() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "czerwony";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_pustynny"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_pustynny"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"pustynny?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("pustynny smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "pustynny"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "pustynny?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/pustynny smok/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 5 skór złotego smoka. Jeśli włożę je pod poduszkę, będą odstraszały koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo skóry nie będą działały jak talizman!",
				new MultipleActions(actions));
	}

	private void step7() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "pustynny"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie pustynnego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"pustynny"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("skóra złotego smoka", 5))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś pustynnego smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, podrzuć mi skóry złotego smoka.");
					}
				});
		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"pustynny"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("skóra złotego smoka", 5)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #pustynny.");
						player.drop("skóra złotego smoka", 5);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "czas_zloty;"+System.currentTimeMillis());
					}
				});
	}

	private void czas6() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "pustynny";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_zloty"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_zloty"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"zloty?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("złoty smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "zloty"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "zloty?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/złoty smok/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 5 skór złotego smoka. Jeśli włożę je pod poduszkę, będą odstraszały koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo skóry nie będą działały jak talizman!",
				new MultipleActions(actions));
	}

	private void step8() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "zloty"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie złotego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"zloty"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("skóra złotego smoka", 5))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś złotego smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, przynieś mi jeszcze skóry złotego smoka.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"zloty"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("skóra złotego smoka", 5)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #zloty.");
						player.drop("skóra złotego smoka", 5);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "czas_dwa_niebieski;"+System.currentTimeMillis());
					}
				});
	}

	private void czas7() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "zloty";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_dwa_niebieski"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_dwa_niebieski"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"dwa_niebieski?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("dwugłowy niebieski smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "dwa_niebieski"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "dwa_niebieski?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/dwugłowy niebieski smok/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 10 skór niebieskiego smoka. Jeśli włożę je pod poduszkę, będą odstraszały koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo skóry nie będą działały jak talizman!",
				new MultipleActions(actions));
	}

	private void step9() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "dwa_niebieski"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie dwugłowego niebieskiego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"dwa_niebieski"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("skóra niebieskiego smoka", 10))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś dwugłowego niebieskiego smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, dostarcz mi skóry niebieskiego smoka.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"dwa_niebieski"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("skóra niebieskiego smoka", 10)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #dwuglowy #niebieski.");
						player.drop("skóra niebieskiego smoka", 10);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "czas_dwu_zielony;"+System.currentTimeMillis());
					}
				});
	}

	private void czas8() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "dwuglowy niebieski";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_dwu_zielony"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1,DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_dwu_zielony"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"dwu_zielony?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("dwugłowy zielony smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "dwu_zielony"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "dwu_zielony?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/dwugłowy zielony smok/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 10 skór zielonego smoka. Jeśli włożę je pod poduszkę, będą odstraszały koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo skóry nie będą działały jak talizman!",
				new MultipleActions(actions));
	}

	private void step10() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "dwu_zielony"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie dwugłowego zielonego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"dwu_zielony"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("skóra zielonego smoka", 10))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, dostarcz mi te skóry zielonego smoka.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"dwu_zielony"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("skóra zielonego smoka", 10)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #dwuglowy #zielony.");
						player.drop("skóra zielonego smoka", 10);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "czas_arktyczny;"+System.currentTimeMillis());
					}
				});
	}

	private void czas9() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "dwuglowy zielony";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_arktyczny"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_arktyczny"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"arktyczny?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("smok arktyczny", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "arktyczny"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "arktyczny?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/smok arktyczny/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 10 kłów smoka. Jeśli włożę je pod poduszkę, będą odstraszały koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo kły nie będą działały jak talizman!",
				new MultipleActions(actions));
	}

	private void step11() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "arktyczny"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie arktycznego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"arktyczny"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("kieł smoka", 10))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś arktycznego smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, przynieś kły smoka.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"arktyczny"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("kieł smoka", 10)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #arktyczny.");
						player.drop("kieł smoka", 10);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "czas_dru_czerwony;"+System.currentTimeMillis());
					}
				});
	}

	private void czas10() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "arktyczny";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_dru_czerwony"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_dru_czerwony"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"dru_czerwony?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("dwugłowy czerwony smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "dru_czerwony"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "dru_czerwony?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/dwugłowy czerwony smok/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 10 skór czerwonego smoka. Jeśli włożę je pod poduszkę, będą odstraszały koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo skóry nie będą działały jak talizman!",
				new MultipleActions(actions));
	}

	private void step12() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "dru_czerwony"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie dwugłowego czerwonego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"dru_czerwony"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("skóra czerwonego smoka", 10))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, dostarcz mi te skóry czerwonego smoka, o które prosiłam.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"dru_czerwony"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("skóra czerwonego smoka", 10)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #dwuglowy #czerwony.");
						player.drop("skóra czerwonego smoka", 10);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "czas_czarny;"+System.currentTimeMillis());
					}
				});
	}

	private void czas11() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "dwuglowy czerwony";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_czarny"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_czarny"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"czarny?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("czarny smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "czarny"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "czarny?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/czarny smok/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 15 skór czarnego smoka. Jeśli włożę je pod poduszkę, będą odstraszały koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo skóry nie będą działały jak talizman!",
				new MultipleActions(actions));
	}

	private void step13() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "czarny"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie czarnego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"czarny"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("skóra czarnego smoka", 15))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, przynieś mi te skóry czarnego smoka.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"czarny"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("skóra czarnego smoka", 15)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #czarny.");
						player.drop("skóra czarnego smoka", 15);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "czas_dwuglowy_czarny;"+System.currentTimeMillis());
					}
				});
	}

	private void czas12() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "czarny";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_dwuglowy_czarny"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_dwuglowy_czarny"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"dwuglowy_czarny?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("dwugłowy czarny smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "dwuglowy_czarny"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "dwuglowy_czarny?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/dwugłowy czarny smok/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 25 skór czarnego smoka. Jeśli włożę je pod poduszkę, będą odstraszały koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo skóry nie będą działały jak talizman!",
				new MultipleActions(actions));
	}

	private void step14() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "dwuglowy_czarny"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie dwugłowego czarnego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"dwuglowy_czarny"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("skóra czarnego smoka", 25))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, przynieś dla mnie skóry czarnego smoka.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"dwuglowy_czarny"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("skóra czarnego smoka", 25)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #czarny #dwuglowy.");
						player.drop("skóra czarnego smoka", 25);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "czas_czar_latajacy;"+System.currentTimeMillis());
					}
				});
	}

	private void czas13() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "czarny dwuglowy";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_czar_latajacy"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_czar_latajacy"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"czar_latajacy?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("latający czarny smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "czar_latajacy"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "czar_latajacy?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/latający czarny smok/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 2 pazury czarnego smoka. Jeśli włożę je pod poduszkę, będą odstraszały koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo pazury nie będą działały jak talizman!",
				new MultipleActions(actions));
	}

	private void step15() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "czar_latajacy"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie latającego czarnego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"czar_latajacy"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("pazur czarnego smoka", 2))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, dostarcz mi pazury czarnego smoka.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"czar_latajacy"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("pazur czarnego smoka", 2)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #czarny #latajacy."
						+ " Aha i weź te kilka butelek napoju uzdrawiającego.");
						player.drop("pazur czarnego smoka", 2);
						player.addKarma(150);
						final StackableItem potion = (StackableItem) SingletonRepository.getEntityManager().getItem("gigantyczny eliksir");
						final int potionamount = 3;
						potion.setQuantity(potionamount);
						player.equipOrPutOnGround(potion);
						player.setQuest(QUEST_SLOT, "czas_latajacy_zloty;"+System.currentTimeMillis());
					}
				});
	}

	private void czas14() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "czarny latajacy";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_latajacy_zloty"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_latajacy_zloty"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"latajacy_zloty?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("latający złoty smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "latajacy_zloty"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "latajacy_zloty?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/latający złoty smok/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 1 pazur złotego smoka. Jeśli włożę go pod poduszkę, będzie odstraszał koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo pazur nie będzie działał jak talizman!",
				new MultipleActions(actions));
	}

	private void step16() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "latajacy_zloty"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie latającego złotego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"latajacy_zloty"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("pazur złotego smoka"))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, dostarcz mi pazur złotego smoka.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"latajacy_zloty"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("pazur złotego smoka")),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, pokonałeś go! Dziękuję! Wróć proszę za 24 godziny, powiem ci, czy talizman zadziałał. Przypomnij mi mówiąc #zloty #latajacy. Aha i weź te kilka butelek napoju uzdrawiającego");
						player.drop("pazur złotego smoka");
						player.addKarma(150);
						final StackableItem potion1 = (StackableItem) SingletonRepository.getEntityManager().getItem("gigantyczny eliksir");
						final int potion1amount = 5;
						potion1.setQuantity(potion1amount);
						player.equipOrPutOnGround(potion1);
						player.setQuest(QUEST_SLOT, "czas_wawelski;"+System.currentTimeMillis());
					}
				});
	}

	private void czas15() {
		final SpeakerNPC npc = npcs.get("Alicja");
		final String extraTrigger = "zloty latajacy";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_wawelski"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła doba. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_wawelski"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Talizman zadziałał! Niestety tej nocy przyśnił mi się inny koszmar. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"wawelski?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("Smok Wawelski", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "wawelski"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomoge"),
				new QuestInStateCondition(QUEST_SLOT, "wawelski?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tej nocy przyśnił mi się #'/smok wawelski/', proszę odszukaj go i zabij."
				+ " Przynieś mi też jego 1 pazur zielonego smoka. Jeśli włożę go pod poduszkę, będzie odstraszał koszmary."
				+ " Pamiętaj, że musisz zabić go samodzielnie, bo pazur nie będzie działał jak talizman!",
				new MultipleActions(actions));

	}

	private void step17() {
		final SpeakerNPC npc = npcs.get("Alicja");
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "wawelski"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie smoka wawelskiego, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"wawelski"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("pazur zielonego smoka"))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, przynieś mi pazur zielonego smoka, o który prosiłam.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"wawelski"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("pazur zielonego smoka")),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Talizman zadziałał! Tej nocy nie miałam więcej koszmarów! Weź proszę tą broń. Należała do mojego dziadka, on był wielkim bohaterem. Myślę, że tobie bardziej się przyda.");
						player.drop("pazur zielonego smoka");
						final Item hammer = SingletonRepository.getEntityManager().getItem("młot wulkanów");
						hammer.setBoundTo(player.getName());
						player.equipOrPutOnGround(hammer);
						player.setBaseHP(150 + player.getBaseHP());
						player.heal(150, true);
						player.addKarma(200);
						player.setQuest(QUEST_SLOT, "done");
					}
				});
	}


	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Koszmary Alicji",
				"Alicji śnią się koszmary, w tym celu potrzebuje talizmanu zrobionego ze skór i pazurów smoków.",
				false);

		step1();
		step2();
		czas1();
		step3();
		czas2();
		step4();
		czas3();
		step5();
		czas4();
		step6();
		czas5();
		step7();
		czas6();
		step8();
		czas7();
		step9();
		czas8();
		step10();
		czas9();
		step11();
		czas10();
		step12();
		czas11();
		step13();
		czas12();
		step14();
		czas13();
		step15();
		czas14();
		step16();
		czas15();
		step17();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Spotkałem Alicje w domku Zakopane.");
		if ("rejected".equals(questState)) {
			res.add("Nie mam ochoty na walkę ze smokami.");
			return res;
		}
		res.add("Alicja ma koszmary nocne. Mam zabić zielonego smoka i przynieść 5 skór zielonego smoka.");
		if ("start".equals(questState)) {
			return res;
		}
		res.add("Zabiłem zielonego smoka i zaniosłem Alicji jego skóry!");
		if (questState.startsWith("czas_szkielet")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: zielony.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: zielony.");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("szkielet?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić szkielet smoka i przynieść 10 kości dla psa.");
		if ("szkielet".equals(questState)) {
			return res;
		}
		res.add("Zabiłem szkielet smoka i zaniosłem Alicji kości dla psa!");
		if (questState.startsWith("czas_niebieski")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: szkielet");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: szkielet.");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("niebieski?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić błękitnego smoka i przynieść 5 skór niebieskiego smoka.");
		if ("niebieski".equals(questState)) {
			return res;
		}
		res.add("Zabiłem błękitnego smoka i zaniosłem Alicji skóry niebieskiego smoka!");
		if (questState.startsWith("czas_zgnily")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: niebieski.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: niebieski");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("zgnily?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić zgniły szkielet smoka i przynieść 30 kości dla psa.");
		if ("zgnily".equals(questState)) {
			return res;
		}
		res.add("Zabiłem zgniły szkielet smoka i zaniosłem Alicji jego kości!");
		if (questState.startsWith("czas_czerwony")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: zgnily.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: zgnily");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("czerwony?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić czerwonego smoka i przynieść 5 skór czerwonego smoka.");
		if ("czerwony".equals(questState)) {
			return res;
		}
		res.add("Zabiłem czerwonego smoka i zaniosłem Alicji skóry czerwonego smoka!");
		if (questState.startsWith("czas_pustynny")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: czerwony.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: czerwony");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("pustynny?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić pustynnego smoka i przynieść 5 skór złotego smoka.");
		if ("pustynny".equals(questState)) {
			return res;
		}
		res.add("Zabiłem pustynnego smoka i zaniosłem Alicji skóry złotgo smoka!");
		if (questState.startsWith("czas_zloty")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: pustynny.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: pustynny");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("zloty?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić złotego smoka i przynieść 5 skór złotego smoka.");
		if ("zloty".equals(questState)) {
			return res;
		}
		res.add("Zabiłem złotego smoka i zaniosłem Alicji skóry złotego smoka!");
		if (questState.startsWith("czas_dwa_niebieski")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: zloty.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: zloty");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("dwa_niebieski?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić niebieskiego dwugłowego smoka i przynieść 10 skór niebieskiego smoka.");
		if ("dwa_niebieski".equals(questState)) {
			return res;
		}
		res.add("Zabiłem niebieskiego smoka i zaniosłem Alicji skóry!");
		if (questState.startsWith("czas_dwu_zielony")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: dwuglowy niebieski.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: dwuglowy niebieski");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("dwu_zielony?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić zielonego dwugłowego smoka i przynieść 10 skór zielonego smoka.");
		if ("dwu_zielony".equals(questState)) {
			return res;
		}
		res.add("Zabiłem zielonego dwugłowego smoka i zaniosłem Alicji skóry!");
		if (questState.startsWith("czas_arktyczny")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: dwuglowy zielony.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: dwuglowy zielony");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("arktyczny?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić arktycznego smoka i przynieść 10 kłów smoka.");
		if ("arktyczny".equals(questState)) {
			return res;
		}
		res.add("Zabiłem arktycznego smoka i zaniosłem Alicji kły smoka!");
		if (questState.startsWith("czas_dru_czerwony")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: arktyczny.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: arktyczny");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("dru_czerwony?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić czerwonego dwugłowego smoka i przynieść 10 skór czerwonego smoka.");
		if ("dru_czerwony".equals(questState)) {
			return res;
		}
		res.add("Zabiłem czerwonego dwugłowego smoka i zaniosłem Alicji skóry!");
		if (questState.startsWith("czas_czarny")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: dwuglowy czerwony.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: dwugłowy czerwony");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("czarny?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić czarnego smoka i przynieść 15 skór czarnego smoka.");
		if ("czarny".equals(questState)) {
			return res;
		}
		res.add("Zabiłem czarnego smoka i zaniosłem Alicji skóry czarnego smoka!");
		if (questState.startsWith("czas_dwuglowy_czarny")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: czarny.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: czarny");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("dwuglowy_czarny?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić czarnego dwugłowego smoka i przynieść 25 skór czarnego smoka.");
		if ("dwuglowy_czarny".equals(questState)) {
			return res;
		}
		res.add("Zabiłem czarnego dwugłowego smoka i zaniosłem Alicji skóry czarnego smoka!");
		if (questState.startsWith("czas_czar_latajacy")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: czarny dwuglowy.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: czarny dwuglowy");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("czar_latajacy?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić czarnego latającego smoka i przynieść 2 pazury czarnego smoka.");
		if ("czar_latajacy".equals(questState)) {
			return res;
		}
		res.add("Zabiłem czarnego latającego smoka i zaniosłem Alicji pazury!");
		if (questState.startsWith("czas_latajacy_zloty")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: czarny latajacy.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: czarny latajacy");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("latajacy_zloty?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić złotego latającego smoka i przynieść jego pazur.");
		if ("latajacy_zloty".equals(questState)) {
			return res;
		}
		res.add("Zabiłem złotego latającego smoka i zaniosłem Alicji jego pazur!");
		if (questState.startsWith("czas_wawelski")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Odwiedzę Alicję, aby sprawdzić czy amulet zadziałał. Hasło: zloty latajacy.");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut, wtedy okaże się czy amulet zadziałał. Hasło: zloty latajacy");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("wawelski?".equals(questState)) {
			return res;
		}
		res.add("Alicja ma dalej koszmary nocne. Tym razem mam zabić smoka wawelskiego samodzielnie i przynieść jego zielony pazur.");
		if ("wawelski".equals(questState)) {
			return res;
		}
		res.add("Alicja może w końcu spać bez koszmarów. Za pomoc dostałem młot wulkanów, który należał do jej dziadka");
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
		return "KillDragons";
	}

	@Override
	public String getNPCName() {
		return "Alicja";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
