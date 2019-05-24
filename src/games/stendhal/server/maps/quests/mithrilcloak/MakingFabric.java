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
package games.stendhal.server.maps.quests.mithrilcloak;

import games.stendhal.common.MathHelper;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.NPCList;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.ProducerBehaviourAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


/**
 * @author kymara
*/

class MakingFabric {

	private static final int REQUIRED_MINUTES_THREAD = 10;
	private static final int REQUIRED_HOURS_MITHRIL_THREAD = 4;
	private static final int REQUIRED_HOURS_FABRIC = 2;

	private MithrilCloakQuestInfo mithrilcloak;
	
	private final NPCList npcs = SingletonRepository.getNPCList();

	/**
	 * Behaviour parse result in the current conversation.
	 * Remark: There is only one conversation between a player and the NPC at any time.
	 */
	private ItemParserResult currentBehavRes;

	public MakingFabric(final MithrilCloakQuestInfo mithrilcloak) {
		this.mithrilcloak = mithrilcloak;
	}

	private void makeThreadStep() {
    	final SpeakerNPC npc = npcs.get("Vincento Price");
		
		npc.addReply("przędza","Bądź cicho. Dobrze? Utkam jedwabną nici pochodzącą z #'gruczołów przędzy' królowej pająków. Jeżeli chcesz, abym to zrobiła to powiedz #zrób.");
		npc.addReply("gruczołów przędzy","Jak już powiedziałam pochodzą od królowa pająków."); 
				
		final Map<String, Integer> requiredResources = new TreeMap<String, Integer>();
		requiredResources.put("gruczoł przędzy", Integer.valueOf(1));
		

		// we want to add something to the beginning of quest slot so override classes using it.

		class SpecialProducerBehaviour extends ProducerBehaviour { 
			SpecialProducerBehaviour(final List<String> productionActivity,
									 final String productName, final Map<String, Integer> requiredResourcesPerItem,
									 final int productionTimePerItem) {
				super(mithrilcloak.getQuestSlot(), productionActivity, productName,
					  requiredResourcesPerItem, productionTimePerItem, false);
			}

			/**
			 * Tries to take all the resources required to produce the agreed amount of
			 * the product from the player. If this is possible, initiates an order.
			 * 
			 * @param npc
			 *            the involved NPC
			 * @param player
			 *            the involved player
			 */
			@Override
				public boolean transactAgreedDeal(ItemParserResult res, final EventRaiser npc, final Player player) {
				int amount = res.getAmount();

				if (getMaximalAmount(player) < amount) {
					// The player tried to cheat us by placing the resource
					// onto the ground after saying "yes"
					npc.say("Hej! Jestem tutaj! Lepiej, żebyś nie próbował mnie oszukać...");
					return false;
				} else {
					for (final Map.Entry<String, Integer> entry : getRequiredResourcesPerItem().entrySet()) {
						final int amountToDrop = amount * entry.getValue();
						player.drop(entry.getKey(), amountToDrop);
					}
					final long timeNow = new Date().getTime();
					player.setQuest(mithrilcloak.getQuestSlot(), "makingthread;" + amount + ";" + getProductName() + ";"
									+ timeNow);
					npc.say("Trochę niekonwencjonalne, ale zrobię dla Ciebie "
							+ amount
							+ " "
							+ getProductName()
							+ ". Bądź dyskretny i wróć za "
							+ TimeUtil.approxTimeUntil(REQUIRED_MINUTES_THREAD * amount * MathHelper.SECONDS_IN_ONE_MINUTE) + ".");
					return true;
				}
			}
			
			/**
			 * This method is called when the player returns to pick up the finished
			 * product. It checks if the NPC is already done with the order. If that is
			 * the case, the player is told to get the product from another NPC. 
			 * Otherwise, the NPC asks the player to come back later.
			 * 
			 * @param npc
			 *            The producing NPC
			 * @param player
			 *            The player who wants to fetch the product
			 */
			@Override
				public void giveProduct(final EventRaiser npc, final Player player) {
				final String orderString = player.getQuest(mithrilcloak.getQuestSlot());
				final String[] order = orderString.split(";");
				final int numberOfProductItems = Integer.parseInt(order[1]);
				// String productName = order[1];
				final long orderTime = Long.parseLong(order[3]);
				final long timeNow = new Date().getTime();
				final long timeRemaining = orderTime + ((long)REQUIRED_MINUTES_THREAD * numberOfProductItems * MathHelper.MILLISECONDS_IN_ONE_MINUTE) - timeNow;
				if (timeRemaining > 0L) {
					npc.say("Ciii wciąż pracuję nad Twoim zleceniem na "
							+ getProductName()
							+ ". Skończę za " + TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + ".");
				} else {
					npc.say("Dałem Tobie "
							+ Grammar.quantityplnoun(numberOfProductItems,
													 getProductName(), "") + " dla mojego studenta Borisa Karlova. Zabierz je od niego.");
					player.notifyWorldAboutChanges();
				}
			}
		}
		
		final ProducerBehaviour behaviour = new SpecialProducerBehaviour(Arrays.asList("make", "zrób"), "przędza jedwabna",
																		 requiredResources, REQUIRED_MINUTES_THREAD * MathHelper.SECONDS_IN_ONE_MINUTE);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("make", "zrób"),
				new QuestInStateCondition(mithrilcloak.getQuestSlot(), "need_fabric"), ConversationStates.ATTENDING, null,
				new ProducerBehaviourAction(behaviour) {
					@Override
					public void fireRequestOK(final ItemParserResult res, Player player, Sentence sentence, EventRaiser npc) {
						// Find out how much items we shall produce.
						if (res.getAmount() < 40) {
							npc.say("Chcesz kilka? Nie będę marnowała na to czasu! Każdy przyzwoity kawałek materiału potrzebuje co najmniej 40 szpulek nici! Powinieneś powiedzieć #zrób #40.");
							return;
						} else if (res.getAmount() > 1000) {
							/*logger.warn("Decreasing very large amount of "
							 *		+ behaviour.getAmount()
							 *		+ " " + behaviour.getChosenItemName()
							 *		+ " to 40 for player "
							 *		+ player.getName() + " talking to "
							 *		+ npc.getName() + " saying " + sentence);
							 */
							res.setAmount(40);
						}

						if (behaviour.askForResources(res, npc, player)) {
							currentBehavRes = res;
							npc.setCurrentState(ConversationStates.PRODUCTION_OFFERED);
						}
					}

					@Override
					public void fireRequestError(final ItemParserResult res, final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say(behaviour.getErrormessage(res, Arrays.asList("#make", "#zrób"), "produce"));
					}
				});
		
		npc.add(ConversationStates.PRODUCTION_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						behaviour.transactAgreedDeal(currentBehavRes, npc, player);

						currentBehavRes = null;
					}
				});

		npc.add(ConversationStates.PRODUCTION_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Dobrze nie ma problemu.", null);

		npc.add(ConversationStates.ATTENDING,
				behaviour.getProductionActivity(),
				new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "makingthread;"), 
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence,
							final EventRaiser npc) {
						npc.say("Wciąż nie skończyłam Twojego zlecenia!");
					}
				});
		// player returns and says hi while sacs being made
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "makingthread;")),
			ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence,
							final EventRaiser npc) {
						behaviour.giveProduct(npc, player);
					}
				});
		// player returns and doesn't need fabric and sacs not being made
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new NotCondition(
							new OrCondition(
									 new QuestInStateCondition(mithrilcloak.getQuestSlot(), "need_fabric"),
									 new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "makingthread;")
							)
					)),
			ConversationStates.IDLE, "Ha ha he he łoo hoo!!!",
			null);


		// player returns and needs fabric
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(mithrilcloak.getQuestSlot(), "need_fabric")),
			ConversationStates.ATTENDING, "Ha ha he he łoo hoo ... ha ... Przepraszam, ale czasami mnie ponosi. Czego potrzebujesz?",
			null);


	}
	private void fetchThreadStep() {
		final SpeakerNPC npc = npcs.get("Boris Karlova");

		// player returns and says hi while sacs being made
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "makingthread;")),
			ConversationStates.IDLE, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence,
									 final EventRaiser npc) {
						final String orderString = player.getQuest(mithrilcloak.getQuestSlot());
						final String[] order = orderString.split(";");
						final int numberOfProductItems = Integer.parseInt(order[1]);
						final long orderTime = Long.parseLong(order[3]);
						final long timeNow = new Date().getTime();
						if (timeNow - orderTime < (long)REQUIRED_MINUTES_THREAD * numberOfProductItems * MathHelper.MILLISECONDS_IN_ONE_MINUTE) {
							npc.say("Haaaa heee łoooo hoo!");
						} else {
							npc.say("Szef dał mi "  
									+ Grammar.quantityplnoun(numberOfProductItems, "przędza jedwabna","") 
									+ ". Pieniądze dostaje jego student, aby za niego wykonał brudną robotę.");
							final StackableItem products = (StackableItem) SingletonRepository.getEntityManager().getItem(
																														  "przędza jedwabna");
							
							player.addXP(100);
							products.setQuantity(numberOfProductItems);
							products.setBoundTo(player.getName());
							player.setQuest(mithrilcloak.getQuestSlot(), "got_thread");
							player.equipOrPutOnGround(products);
							player.notifyWorldAboutChanges();
						}
					}
				}
				);

		// player returns and doesn't need fabric and sacs not being made
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new NotCondition(new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "makingthread;"))),
			ConversationStates.IDLE, "Ha ha he he łoo hoo!!!",
			null);

	}

	private void makeMithrilThreadStep() {
		final SpeakerNPC npc = npcs.get("Kampusch");
		
		npc.addReply("balonik","Aha! Gubione są przez czarujące małe aniołki, które żyją na Kikareukin Islands. Potrzebuję jednego dla mojej córki.");
		npc.addReply("przędzy jedwabnej","To jest jedwab z gruczołów gigantycznych pająków. Potrzebujesz 40 szpulek prządzy jedwabnej, aby zrobić coś większego jak płaszcz.");
		npc.addReply("przędza","To jest jedwab z gruczołów gigantycznych pająków.");
		npc.addReply("bryłek mithrilu","Możesz sam je znaleźć.");
		npc.addReply("Whiggins","Znajdź czarodzieja Whiggins w domku w magicznym mieście.");
		npc.addReply("scientists","Słyszałem o eksperymentach głęboko w zamku Kalavan. Naukowcy są szaleni, ale poszukaj naukowca Vincento Price. Może on będzie na tyle normalny, aby Ci pomóc.");


		// player says yes they brought the items needed
		// we can't use the nice ChatActions here because we have to timestamp the quest slot
		npc.add(
			ConversationStates.ATTENDING,
			Arrays.asList("fuse", "złącz"), 
			new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_thread"),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (player.isEquipped("przędza jedwabna", 40)
						&& player.isEquipped("bryłka mithrilu", 7)
						&& player.isEquipped("balonik")) {
						player.drop("przędza jedwabna", 40);
					    player.drop("bryłka mithrilu", 7);
						player.drop("balonik");
						final long timeNow = new Date().getTime();
						player.setQuest(mithrilcloak.getQuestSlot(), "fusingthread;" + timeNow);
						npc.say("Zrobię 40 przędzy z mithrilu dla Ciebie. Wróć za "
								+ TimeUtil.approxTimeUntil((int) (REQUIRED_HOURS_MITHRIL_THREAD * MathHelper.MILLISECONDS_IN_ONE_HOUR / 1000L)) 
								+ ".");
						player.notifyWorldAboutChanges();
					} else {
						npc.say("Dla 40 szpulek przędzy z mithrilu, które potrzebne są do płaszcza potrzebuję 40 #przędzy #jedwabnej, 7 #bryłek #mithrilu i #balonik.");
					}
				}
			});

		// player returns while fabric is still being woven, or is ready
		npc.add(ConversationStates.IDLE, 
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "fusingthread;")),
				ConversationStates.ATTENDING, null, new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String orderString = player.getQuest(mithrilcloak.getQuestSlot());
						final String[] order = orderString.split(";");
						final long delay = REQUIRED_HOURS_MITHRIL_THREAD * MathHelper.MILLISECONDS_IN_ONE_HOUR;
						final long timeRemaining = (Long.parseLong(order[1]) + delay)
							- System.currentTimeMillis();
						if (timeRemaining > 0L) {
							npc.say("Witam. Wciąż pracuję nad Twoim zleceniem wyrobieniem przędzy z mithrilu"
									+ ". Wróć za "
									+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + ".");
						} else {
							final StackableItem products = (StackableItem) SingletonRepository.
										getEntityManager().getItem("przędza z mithrilu");
	
							products.setQuantity(40);
						
							products.setBoundTo(player.getName());
							player.equipOrPutOnGround(products);
							npc.say("Witaj ponownie. Skończyłem. Oto 40 szpulek przędzy z mithrilu. Idź teraz do #Whiggins, aby zrobił #sukno.");
							player.setQuest(mithrilcloak.getQuestSlot(), "got_mithril_thread");
							// give some XP as a little bonus for industrious workers
							player.addXP(100);
							player.notifyWorldAboutChanges();	
					}
				  }
				}
		);

		// don't fuse thread unless state correct
		npc.add(
				ConversationStates.ATTENDING,
				Arrays.asList("fuse","złącz","wyrobić","utkaj","połączyć"),
				new NotCondition(new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_thread")), 
				ConversationStates.ATTENDING, "Mogę stworzyć przędzę z mithrilu tylko, gdy zdobędziesz trochę gruczołów przędzy. Pamiętaj, że będę wiedział czy naprawdę potrzebujesz magii czy nie.", null);
		
			// player returns and hasn't got thread yet/got thread already and 
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new NotCondition(
								 new OrCondition(
												 new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_thread"),
												 new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "fusingthread;")
												 )
						)),
				ConversationStates.ATTENDING, "Pozdrawiam. Co za ciekawe miejsce.",
				null);

		// player needs thread fused
		npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_thread")),
				ConversationStates.ATTENDING, "Pozdrawiam czy mogę ci coś #zaoferować?",
				null);

	}
	private void makeMithrilFabricStep() {

		final SpeakerNPC npc = npcs.get("Whiggins");

		// player asks about fabric/quest
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("weave", "fabric", "magical", "sukno z mithrilu", "ida", "mithril", "cloak", "mithril cloak", "task", "quest", "tekstylia", "zadanie", "misja"),
				new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_mithril_thread"),
				ConversationStates.QUEST_OFFERED,
				"Z przyjemnością utkałbym tkaninę, ale moja głowa zaprzątnięta jest innymi rzeczami. Obraziłem znajomego czarodzieja i przez całą noc pisałem list z przeprosinami, ale nie ma kto go doręczyć. Dopóki ... to jest to ... czy mógłbyś dla mnie doręczyć ten list?",
				null);
			
		// Player says yes they want to help 
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Wspaniale! Spadł mi kamień z serca! Proszę weź tę kopertę do Pedinghaus. Znajdziesz go w kuźni w Ados. Powiedz mu, że masz #list dla niego.",
				new MultipleActions(new EquipItemAction("koperta zalakowana", 1, true),
									new SetQuestAction(mithrilcloak.getQuestSlot(), "taking_letter"))
				);
		
		// player said no they didn't want to help
		npc.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.QUEST_OFFERED,
			"Och tak się martwię. Pomożesz mi?",
			null);
	
		// player returns without having taking letter
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("weave", "fabric", "magical", "sukno z mithrilu", "ida", "mithril", "cloak", "mithril cloak", "pedinghaus", "task","list", "quest", "letter", "note", "tekstylia", "zadanie", "misja", "list", "notatka"),
				new QuestInStateCondition(mithrilcloak.getQuestSlot(), "taking_letter"),
				ConversationStates.ATTENDING,
				"Nie zapomnij zabrać tego listu do Pedinghaus. Dużo dla mnie znaczy.", null);

		// player returns having taking letter
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("weave", "fabric", "magical", "sukno z mithrilu", "ida", "mithril", "cloak", 
							  "mithril cloak", "pedinghaus", "regards", "forgiven", "task", "quest", "tekstylia", "zadanie","list", "misja"),
				new QuestInStateCondition(mithrilcloak.getQuestSlot(), "took_letter"),
				ConversationStates.SERVICE_OFFERED,
				"Dziękuję bardzo za zabranie listu! Masz 40 szpulek przędzy z mithrilu "
				+ "w takim razie mogę utkać dla Ciebie kilka jardów tkaniny?", null);

		// player's quest state is in nothing to do with the letter, thread or weaving.
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("weave", "fabric", "magical", "sukno z mithrilu", "ida", "mithril", "cloak", "mithril cloak", "pedinghaus", "task", "quest", "tekstylia", "zadanie", "misja"),
				new NotCondition(
								 new OrCondition(new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_mithril_thread"),
												 new QuestInStateCondition(mithrilcloak.getQuestSlot(), "taking_letter"),
												 new QuestInStateCondition(mithrilcloak.getQuestSlot(), "took_letter"),
												 new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "weavingfabric;")
												 )
								 ),
				ConversationStates.ATTENDING,
				"Nie mam zadania dla Ciebie.", null);
									

		// player says yes they brought the items needed
		// we can't use the nice ChatActions here because we have to timestamp the quest slot
		npc.add(
			ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.YES_MESSAGES, 
			new QuestInStateCondition(mithrilcloak.getQuestSlot(), "took_letter"),
			ConversationStates.ATTENDING,
			null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					if (player.isEquipped("przędza z mithrilu", 40)) {
						
							player.drop("przędza z mithrilu", 40);
							npc.say("Wspaniale. Za " 
									   + REQUIRED_HOURS_FABRIC + " godzinę" + " twoja tkanina będzie gotowa.");
							player.setQuest(mithrilcloak.getQuestSlot(), "weavingfabric;" + System.currentTimeMillis());
							player.notifyWorldAboutChanges();
						} else {
							npc.say("Nie masz ze sobą 40 szpulek przędzy z mithrilu. Przepraszam, ale nic nie mogę dla Ciebie zrobić.");
						}
				}
			});

		// player says they didn't bring the stuff yet
		npc.add(
			ConversationStates.SERVICE_OFFERED,
			ConversationPhrases.NO_MESSAGES, 
			null,
			ConversationStates.ATTENDING,
			"Och dobrze. Mam nadzieje, że ich nie zgubiłeś. Są bardzo cenne!",
			null);

		// player returns while fabric is still being woven, or is ready
		npc.add(ConversationStates.ATTENDING, 
			Arrays.asList("weave", "fabric", "magical", "sukno z mithrilu", "ida", "mithril", "cloak", "mithril cloak", "task", "quest", "tekstylia", "zadanie", "misja"),
			new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "weavingfabric;"),
			ConversationStates.ATTENDING, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					final String[] tokens = player.getQuest(mithrilcloak.getQuestSlot()).split(";");
					final long delay = REQUIRED_HOURS_FABRIC * MathHelper.MILLISECONDS_IN_ONE_HOUR;
					final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();
					if (timeRemaining > 0L) {
						npc.say("Przepraszam, ale jesteś zbyt wcześnie. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + ".");
						return;
					}
					npc.say("Twoja tkanina jest już gotowa! Czyż nie jest wspaniała?");
					player.addXP(100);
					player.addKarma(15);
					final Item fabric = SingletonRepository.getEntityManager().getItem(
									mithrilcloak.getFabricName());
					fabric.setBoundTo(player.getName());
					player.equipOrPutOnGround(fabric);
					player.setQuest(mithrilcloak.getQuestSlot(), "got_fabric");
					player.notifyWorldAboutChanges();
				}
			});
	}

	private void giveLetterStep() {	

		final SpeakerNPC npc = npcs.get("Pedinghaus");

		// accept the letter
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("letter", "note", "whiggins", "apology", "list", "notatka"),
				new AndCondition(new QuestInStateCondition(mithrilcloak.getQuestSlot(), "taking_letter"), new PlayerHasItemWithHimCondition("koperta zalakowana")),
				ConversationStates.ATTENDING,
				"*czyta* ... *czyta* ... Muszę powiedzieć, że ulżyło mi. Bardzo dziękuję. Proszę przekaż Whiggins najgorętsze pozdrowienia. Wybaczyłem mu.",
				new MultipleActions(
									 new DropItemAction("koperta zalakowana"), 
									 new SetQuestAndModifyKarmaAction(mithrilcloak.getQuestSlot(), "took_letter", 10.0)
				));
	}

	private void giveFabricStep() {	

		final SpeakerNPC npc = npcs.get("Ida");

		// accept the fabric and ask for scissors
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("fabric", "mithril", "cloak", "mithril cloak", "task", "quest", "tekstylia", "zadanie", "misja"),
				new AndCondition(new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_fabric"), new PlayerHasItemWithHimCondition(mithrilcloak.getFabricName())),
				ConversationStates.ATTENDING,
				"Wspaniale masz " + mithrilcloak.getFabricName() + " wyglądają na dłuższe niż się spodziewałem! Teraz do obcięcia potrzebuję zaczarowanych #nożyczek. Możesz je dostać od #Hogart. Będę czekał na Twój powrót.",
				new MultipleActions(
									 new DropItemAction(mithrilcloak.getFabricName()), 
									 new SetQuestAndModifyKarmaAction(mithrilcloak.getQuestSlot(), "need_scissors", 10.0)
				));

		// remind about fabric. there are so many steps to getting fabric 
		// that the player could be in many quest states and she still is just waiting for fabric
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("fabric", "mithril", "cloak", "mithril cloak", "task", "quest", "tekstylia", "zadanie", "misja"),
				new OrCondition(
								new QuestInStateCondition(mithrilcloak.getQuestSlot(), "need_fabric"),
								new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "makingthread;"),
								new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_thread"),
								new QuestStateStartsWithCondition(mithrilcloak.getQuestSlot(), "fusingthread;"),
								new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_mithril_thread"),
								new QuestInStateCondition(mithrilcloak.getQuestSlot(), "taking_letter"),
								new QuestInStateCondition(mithrilcloak.getQuestSlot(), "took_letter"),
								new AndCondition(new QuestInStateCondition(mithrilcloak.getQuestSlot(), "got_fabric"),
												 new NotCondition(new PlayerHasItemWithHimCondition(mithrilcloak.getFabricName()))
												)
								 ),
				ConversationStates.ATTENDING,
				"Wciąż czekam na " + mithrilcloak.getFabricName() 
				+ ", bym mógł zacząć pracę nad twoim płaszczem z mithrilu. Powinieneś zapytać #Kampusch o #tekstylia.",
				null);

		npc.addReply("Hogart", "To zrzędliwy stary krasnal, który mieszka w kopalni na terenie Or'ril. Już wysłałem mu wiadomość, że potrzebuję nowych nożyczek, ale nie odpowiedział.");
	}

	public void addToWorld() {
		makeThreadStep();
		fetchThreadStep();
		makeMithrilThreadStep();
		makeMithrilFabricStep();
		giveLetterStep();
		giveFabricStep();	
	}

}
