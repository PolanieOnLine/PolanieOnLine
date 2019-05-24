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
package games.stendhal.server.maps.quests.mithrilcloak;

import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.parser.ExpressionType;
import games.stendhal.common.parser.JokerExprMatcher;
import games.stendhal.common.parser.Sentence;
import games.stendhal.common.parser.SimilarExprMatcher;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TextHasNumberCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author kymara
*/

class GettingTools {

	private MithrilCloakQuestInfo mithrilcloak;
	
	private final NPCList npcs = SingletonRepository.getNPCList();

	public GettingTools(final MithrilCloakQuestInfo mithrilcloak) {
		this.mithrilcloak = mithrilcloak;
	}


	private static final int REQUIRED_MINUTES_SCISSORS = 10;

	private static final int REQUIRED_HOURS_SEWING = 24;

	private void getScissorsStep() {

		// Careful not to overlap with any states from VampireSword quest

		final SpeakerNPC npc = npcs.get("Hogart");

		// player asks about scissors. they will need a random number of eggshells plus the metal
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("scissors", "magical","nożyczki", "magical scissors", "ida", "mithril", "cloak", "mithril cloak"),
			new QuestInStateCondition(mithrilcloak.getQuestSlot(), "need_scissors"),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					final int neededEggshells = Rand.randUniform(2, 4);
					raiser.say("Ach tak, Ida wysłała mi wiadomość o magicznych nożyczkach. Potrzebuję żelaza i sztabki mithrilu, a także " + Integer.toString(neededEggshells) + " magiczne #skorupki. Zapytaj mnie o #nożyczki, gdy wrócisz i będziesz miał wszystkie przedmioty.");
					// store the number of needed eggshells in the quest slot so he remembers how many he asked for
					player.setQuest(mithrilcloak.getQuestSlot(), "need_eggshells;" + Integer.toString(neededEggshells));
				}
			});

		// player needs eggshells
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("scissors", "magical", "zaczarowane nożyczki","nożyczki", "ida", "mithril", "cloak", "mithril cloak"),
			new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "need_eggshells"),
			ConversationStates.SERVICE_OFFERED,
			"Przyniosłeś mi przedmioty potrzebne do magicznych nożyc?", null);

		// player asks about eggshells, hint to find terry
		npc.add(
			ConversationStates.ATTENDING,
			"skorupki", 
			null,
			ConversationStates.ATTENDING,
			"Muszą być ze smoczych jaj. Sądzę, że powinieneś znaleźć kogoś kto zajmuje się wykluwaniem smoków!",
			null);

		// player says yes they brought the items needed
		// we can't use the nice ChatActions here because the needed number is stored in the quest slot i.e. we need a fire
		npc.add(
			ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.YES_MESSAGES, 
			new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "need_eggshells"),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String[] questslot = player.getQuest(mithrilcloak.getQuestSlot()).split(";");
					final int neededEggshells = Integer.valueOf(questslot[1]);
					if (player.isEquipped("żelazo")
						&& player.isEquipped("sztabka mithrilu")
						&& player.isEquipped("magiczne skorupki", neededEggshells)) {
							player.drop("żelazo");
							player.drop("sztabka mithrilu");
							player.drop("magiczne skorupki", neededEggshells);
							npc.say("Dobrze. Zajmie mi to trochę czasu. Wróć za " 
									   + REQUIRED_MINUTES_SCISSORS + " minutę" + ", aby odebrać nożyczki.");
							player.addXP(100);
							player.setQuest(mithrilcloak.getQuestSlot(), "makingscissors;" + System.currentTimeMillis());
							player.notifyWorldAboutChanges();
						} else {
							npc.say("Kłamca nie masz wszystkiego czego potrzebuję. Zapytaj mnie o #nożyczki, gdy będziesz miał żelazo, sztabkę mithrilu i " 
									+ questslot[1] + " magiczne skorupki i nie marnuj mojego cennego czasu!");
						}
				}
			});

		// player says they didn't bring the stuff yet
		npc.add(
			ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.NO_MESSAGES, 
			null,
			ConversationStates.ATTENDING,
			"Czemu wciąż tu jesteś? Idź zdobądź je!",
			null);

		// player returns while hogart is making scissors or has made them
		npc.add(ConversationStates.ATTENDING, 
			Arrays.asList("scissors", "magical", "zaczarowane nożyczki", "ida", "mithril", "cloak", "mithril cloak"),
			new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "makingscissors;"),
			ConversationStates.ATTENDING, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String[] tokens = player.getQuest(mithrilcloak.getQuestSlot()).split(";");
					// minutes -> milliseconds
					final long delay = REQUIRED_MINUTES_SCISSORS * MathHelper.MILLISECONDS_IN_ONE_MINUTE;
					final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();
					if (timeRemaining > 0L) {
						npc.say("Pff jesteś niecierpliwy? Jeszcze nie skończyłem Twoich nożyc. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + ".");
						return;
					}
					npc.say("Dziękuję za przypomnienie. Oto ukończone nożyczki dla Idy. Nie wiem po co są jej potrzebne, ale lepiej zanieś jej teraz.");
					player.addXP(100);
					player.addKarma(15);
					final Item scissors = SingletonRepository.getEntityManager().getItem(
									"zaczarowane nożyczki");
					scissors.setBoundTo(player.getName());
					player.equipOrPutOnGround(scissors);
					player.setQuest(mithrilcloak.getQuestSlot(), "got_scissors");
					player.notifyWorldAboutChanges();
				}
			});

	}

	private void getEggshellsStep() {

		final int REQUIRED_POISONS = 6;

		final SpeakerNPC npc = npcs.get("Terry");

		// offer eggshells when prompted
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("eggshells", "magical", "magiczne skorupki","skorupki", "nożyczki", "hogart", "ida", "cloak", "mithril cloak", "specials", "specjały"),
				new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "need_eggshells"),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Tak sprzedaję magicznych skorupek. Są dla mnie sporo warte. Dam ci jedną za każdą " + Integer.toString(REQUIRED_POISONS) + " zabójczą truciznę, którą mi przyniesiesz. Potrzebuję ich do wybijania szczurów. Ile potrzebujesz skorupek?",
				null);

		// respond to question of how many eggshells are desired. terry expects a number or some kind
		npc.addMatching(ConversationStates.QUEST_ITEM_QUESTION,
				// match for all numbers as trigger expression
				ExpressionType.NUMERAL, new JokerExprMatcher(),
				new TextHasNumberCondition(1, 5000),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {

                        final int required = (sentence.getNumeral().getAmount());
						if (player.drop("zabójcza trucizna", required * REQUIRED_POISONS)) {
							npc.say("Dobrze oto Twoje " + Integer.toString(required) + " skorupek. Ciesz się!");
							new EquipItemAction("magiczne skorupki", required, true).fire(player, sentence, npc);
						} else {
							npc.say("Dobrze zapytaj mnie ponownie, gdy będziesz miał " + Integer.toString(required * REQUIRED_POISONS) + " zabójczą truciznę ze sobą.");
						}
					}
				});

		// didn't want eggshells yet
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				Arrays.asList("no", "none", "nothing", "nic", "nie"),
				null,
				ConversationStates.ATTENDING,
				"Nie ma problemu. Gdy będziesz potrzebował pomocy to mów.",
				null);

 	}

	private void giveScissorsStep() {

		final SpeakerNPC npc = npcs.get("Ida");

		// take scissors and ask for needle now
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("scissors", "magical", "zaczarowane nożyczki", "mithril", "cloak", "mithril cloak", "task", "quest", "zadanie", "misja"),
				new AndCondition(new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_scissors"), new PlayerHasItemWithHimCondition("zaczarowane nożyczki")),
				ConversationStates.ATTENDING,
				"Przyniosłeś magiczne nożyczki! Doskonale! Teraz mogę ciąć tkaninę. Potrzebuję teraz magiczną igłę. Możesz je kupić od handlarza w opuszczonej warowni w górach Ados. Nazywa się #Ritati Dragon lub jakoś inaczej. Idź do niego i zapytaj o 'specjały'.",
				new MultipleActions(
									 new DropItemAction("zaczarowane nożyczki"), 
									 new SetQuestAndModifyKarmaAction(mithrilcloak.getQuestSlot(), "need_needle;", 10.0), 
									 new IncreaseXPAction(100)
									 )
				);

		// remind about scissors
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("scissors", "magical", "zaczarowane nożyczki","nożyczki", "mithril", "cloak", "mithril cloak", "task", "quest", "zadanie", "misja"),
				new OrCondition(
								new QuestInStateCondition(mithrilcloak.getQuestSlot(), "need_scissors"),
								new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "need_eggshells;"),
								new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "makingscissors;"),
								new AndCondition(new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_scissors"),
												 new NotCondition(new PlayerHasItemWithHimCondition("zaczarowane nożyczki")))
								),
				ConversationStates.ATTENDING,
				"Zapytaj #Hogart o #nożyczki. Jestem pewna, że pamięta moją wiadomość, którą mu wysłałam!",
				null);

		npc.addReply("Ritati", "Jest gdzieś w opuszczonej warowni w górach na północny-wschód stąd.");
	}



	private void getNeedleStep() {

		final int NEEDLE_COST = 1500;
		
		final Map<Integer, String> jokes = new HashMap<Integer, String>();
		
			jokes.put(1, "Jeżeli pośrodku lasu stoi mężczyzna, który rozmawia i nie ma kobiety, która mogłaby go słuchać to wciąż się myli?");
			jokes.put(2, "Każdy ma fotograficzną pamięć, ale nie którzy zapomnieli załadować karty pamięci.");
			jokes.put(3, "Orły szybują, wolne i dumne, ale łasice nigdy nie wpadają do silników odrzutowców.");
			jokes.put(4, "Nie ma przyszłości w podróżowaniu w czasie.");
			jokes.put(5, "Sztuczna inteligencja nie pasuje do naturalnej głupoty.");
			jokes.put(6, "Krzyknij jeżeli kochasz spokój i ciszę.");
			jokes.put(7, "Zawsze pamiętaj, że jesteś wyjątkowy tak jak każdy inny.");
			jokes.put(8, "Połowa ludzi na świecie jest poniżej średniej wieku.");

		
		final SpeakerNPC npc = npcs.get("Ritati Dragontracker");

		// ask for joke when prompted for needle
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("needle", "magical", "magical needle","igła", "ida", "cloak", "mithril cloak", "specials", "specjały"),
				new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "need_needle"),
				ConversationStates.ATTENDING,
				"Dobrze mam małą zasadę. Nigdy nie robię poważnych interesów z kimś dopóki" 
				+ "mnie nie rozśmieszy. Przyjdź do mnie i opowiedz mi #dowcip, a ja sprzedam Ci igłę.",
				null);
		
		// ask for joke when player says joke
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("joke", "dowcip"),
				new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "need_needle"),
				ConversationStates.QUESTION_1,
				"Dobrze posłuchajmy Twojego żartu. Mam nadzieję, że jest z książki pochodzącej z biblioteki w Nalwor. To moja ulubiona. Który dowcip wybrałeś?",
				null);

		npc.add(ConversationStates.QUESTION_1, "", null,
				ConversationStates.QUEST_ITEM_QUESTION, null,
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
							for (int i = 1; i < 9; i++) {
								String joke = jokes.get(i);

								final Sentence answer = sentence.parseAsMatchingSource();
								final Sentence expected = ConversationParser.parse(joke, new SimilarExprMatcher());

								if (answer.matchesFull(expected)) {
									final String[] questslot = player.getQuest(mithrilcloak.getQuestSlot()).split(";");
									if (questslot.length > 2) {
										// if the split worked, we had stored a needle number before and we need to store it again
										int needles = Integer.parseInt(questslot[1]);
										int saidjoke = Integer.parseInt(questslot[2]);
										if (i == saidjoke) {
											npc.say("Ostatnio opowiedziałeś mi ten dowcip. Wróć z nowym! Dowidzenia.");
											npc.setCurrentState(ConversationStates.IDLE);
//											// stop looking through the joke list
											return;
										} else {
											player.setQuest(mithrilcloak.getQuestSlot(), "told_joke;" + Integer.toString(needles) + ";" + Integer.toString(i));
										}
									} else {
										player.setQuest(mithrilcloak.getQuestSlot(), "told_joke;" + Integer.toString(i));
									}
									// this might have been his favourite joke, which is determined randomly
									if (Rand.randUniform(1, 8) == i) {
										npc.say("To najśmieszniejszy dowcip jaki słyszałem! Sądzę, że jest to teraz mój ulubiony. Oto twoja igła za darmo... i wynocha stąd! Za długo tu już siedzisz.");
										new EquipItemAction("zaczarowana igła", 1, true).fire(player, sentence, npc);
										npc.setCurrentState(ConversationStates.IDLE);
//										// stop looking through the joke list
										return;
									} else {
										 npc.say("*rubaszny śmiech* Dobrze. Bierzmy się do interesów. Magiczna igła kosztuje "
											+ Integer.toString(NEEDLE_COST) + " money. Chcesz kupić?");
										// stop looking through the joke list
										npc.setCurrentState(ConversationStates.QUEST_ITEM_QUESTION);
										return;
									 }
								}
							}
							if (ConversationPhrases.GOODBYE_MESSAGES.contains(sentence.getTriggerExpression().getNormalized())) {
									npc.say("Dobrze, dowidzenia.");
										npc.setCurrentState(ConversationStates.IDLE);
							} else if (sentence.getTriggerExpression().getNormalized().equals("none")
												|| sentence.getTriggerExpression().getNormalized().equals("nic")) {
								npc.say("Dobrze, dowidzenia.");
								npc.setCurrentState(ConversationStates.IDLE);
							} else {
								npc.say("Ten dowcip nie jest śmieszny. Wróć do biblioteki w Nalwor i znajdź inny.");
								npc.setCurrentState(ConversationStates.IDLE);
							}
			}
		});
		
		
		// offer needle when prompted if they already told a joke
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("needle", "magical", "zaczarowana igła","igła", "ida", "cloak", "mithril cloak", "specials", "specjały"),
				new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "told_joke"),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Mam kilka magicznych igieł, ale kosztują sporo "
				+ Integer.toString(NEEDLE_COST) + " money. Czy chcesz kupić?",
				null);

		// agrees to buy 1 needle
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES, 
				new PlayerHasItemWithHimCondition("money", NEEDLE_COST),
				ConversationStates.IDLE,
				"Dobrze. Oto one. Bądź z nimi ostrożny, bo łatwo się łamią. "+
				"Jeżeli złamiesz to inne igły, które kupiłeś ode mnie stracą swoją magię. "+
				"Teraz odejdź, bo za długo tu przesiadujesz.",				
				new MultipleActions(
					new DropItemAction("money", NEEDLE_COST), 
					new EquipItemAction("zaczarowana igła", 1, true)
					));

		// said he had money but he didn't
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES, 
				new NotCondition(new PlayerHasItemWithHimCondition("money", NEEDLE_COST)),
				ConversationStates.ATTENDING,
				"Co jest ... nie masz pieniędzy! Odejdź stąd!",
				null);

		// doesn't want to buy needle
		npc.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Dobrze nie ma pośpiechu. Może chciałbyś skorzystać z innych #ofert.",
				null);
		
		// specials response for if the queststate condition is not met
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("needle", "magical", "zaczarowana igła","igła", "ida", "cloak", "mithril cloak", "specials", "specjały"),
				null, 
				ConversationStates.ATTENDING,
				"Przyjdzie czas gdy będziesz potrzebował specjaów, ale nie teraz.",
				null);

	}

	private void giveNeedleStep() {

		final SpeakerNPC npc = npcs.get("Ida");

		// player brings needle for first or subsequent time
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("needle", "zaczarowana igła", "magical", "mithril", "cloak", "mithril cloak", "task", "quest", "zadanie", "misja"),
				new AndCondition(new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "told_joke"), new PlayerHasItemWithHimCondition("zaczarowana igła")),
				ConversationStates.ATTENDING,
				null,
				new MultipleActions(
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
							final String[] questslot = player.getQuest(mithrilcloak.getQuestSlot()).split(";");		
							int needles = 1;
							int saidjoke = 1;
							if (questslot.length > 2) {
								// if the split works, we had stored a needle number before
								needles = Integer.parseInt(questslot[1]);
								saidjoke = Integer.parseInt(questslot[2]);
								npc.say("Bardzo przepraszam za złamanie poprzednich igieł. Zacznę od nowa pracę nad Twoim płaszczem. " 
										+ "Wróć za " + REQUIRED_HOURS_SEWING + " godzinę.");
							 } else if (questslot.length > 1) {
								// it wasn't split with a needle number, only joke
								// so this is the first time we brought a needle
								saidjoke = Integer.parseInt(questslot[1]);
								npc.say("Wygląda na to, że znalazłeś Ritattiego. Dobrze. Zacznę pracę nad płaszczem!" 
										 + " Szwaczka potrzebuje trochę czasu. Wróć za " + REQUIRED_HOURS_SEWING + " godzinę.");
								// ida breaks needles - she will need 1 - 3
								needles = Rand.randUniform(1, 3);
							}
							player.setQuest(mithrilcloak.getQuestSlot(), "sewing;" + System.currentTimeMillis() + ";" + needles + ";" + saidjoke);
						}
					},
					new DropItemAction("zaczarowana igła")
					)
				);

		// remind about needle
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("needle", "zaczarowana igła","igła", "magical", "mithril", "cloak", "mithril cloak", "task", "quest"),
				new OrCondition(new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "need_needle"),
								new AndCondition(
									new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "told_joke"),
									new NotCondition(new PlayerHasItemWithHimCondition("zaczarowana igła"))
								)
						),
				ConversationStates.ATTENDING,
				"Zapytaj Ritati czy jak mu tam o 'specjały' lub zapytaj o #igłę.",
				null);
	}

	private void sewingStep() {

		final SpeakerNPC npc = npcs.get("Ida");

		// the quest slot that starts with sewing is the form "sewing;number;number" where the first number is the time she started sewing
		// the second number is the number of needles that she's still going to use - player doesn't know number

		npc.add(ConversationStates.ATTENDING, 
				Arrays.asList("magical", "mithril", "cloak", "mithril cloak", "task", "quest", "zadanie", "misja"),
				new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "sewing;"),
				ConversationStates.ATTENDING, null, new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
							final String[] tokens = player.getQuest(mithrilcloak.getQuestSlot()).split(";");
							// hours -> milliseconds
							final long delay = REQUIRED_HOURS_SEWING * MathHelper.MILLISECONDS_IN_ONE_HOUR;
							final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
								- System.currentTimeMillis();
							if (timeRemaining > 0L) {
								npc.say("Wciąż szyję twój płaszcz. Wróć za "
										+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + " - i nie naciskaj mnie, albo przez pomyłkę złamię igłę.");
								return;
							}
							// ida breaks needles, but if it is the last one,
							// she pricks her finger on that needle
							if (Integer.valueOf(tokens[2]) == 1) {
								npc.say("Och! Ukłułam się w palec przez tę igłę! Czuję się zamroczona ...");
								player.setQuest(mithrilcloak.getQuestSlot(), "twilight_zone");
							} else {
								npc.say("Te magiczne igły są bardzo kruche. Obawiam się, że musisz pójść po następną, bo ostatnia się złamała. Mam nadzieje, żę Ritati ma ich pełno.");
								final int needles = Integer.parseInt(tokens[2]) - 1;
								int saidjoke = Integer.parseInt(tokens[3]);
								player.setQuest(mithrilcloak.getQuestSlot(), "need_needle;" + needles + ";" + saidjoke);
							}							
				}
			});
	}

	public void addToWorld() {
		getScissorsStep();
		getEggshellsStep();
		giveScissorsStep();
		getNeedleStep();
		giveNeedleStep();
		sewingStep();
	}

}
