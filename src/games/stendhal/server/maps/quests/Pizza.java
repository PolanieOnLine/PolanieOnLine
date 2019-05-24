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

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.InflictStatusOnNPCAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OutfitCompatibleWithClothesCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotActiveCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * QUEST: Pizza Delivery
 * <p>
 * PARTICIPANTS:
 * <ul>
 * <li> Ernest (the baker in Semos)
 * <li> NPC's all over the world (as customers)
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Ernest gives you a pizza and tells you who ordered it, and how much
 * time you have to deliver.
 * <li> As a gimmick, you get a pizza delivery uniform.
 * <li> You walk to the customer and say "pizza".
 * <li> The customer takes the pizza. If you were fast enough, you get a tip.
 * <li> You put on your original clothes automatically.
 * </ul>
 * REWARD:
 * <ul>
 * <li> XP (Amount varies depending on customer. You only get half of the XP if
 * the pizza has become cold.)
 * <li> some karma if delivered on time (5)
 * <li> gold coins (As a tip, if you were fast enough; amount varies depending
 * on customer.)
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li> As many as wanted, but you can't get a new task while you still have the
 * chance to do the current delivery on time.
 * </ul>
 */
public class Pizza extends AbstractQuest {
	private static final Logger logger = Logger.getLogger(Pizza.class);
	// FIXME: return to "final" after outfit testing is finished
	private static Outfit UNIFORM;
	
	public Pizza() {
		UNIFORM = new Outfit(null, null, null, Integer.valueOf(75), null);
	}

	/**
	 * A customer data object.
	 */
	static class CustomerData {
		/** A hint where to find the customer. */
		private final String npcDescription;

		/** The pizza style the customer likes. */
		private final String flavor;

		/** The time until the pizza should be delivered. */
		private final int expectedMinutes;

		/** The money the player should get on fast delivery. */
		private final int tip;

		/**
		 * The experience the player should gain for delivery. When the pizza
		 * has already become cold, the player will gain half of this amount.
		 */
		private final int xp;

		/**
		 * The text that the customer should say upon quick delivery. It should
		 * contain %d as a placeholder for the tip, and can optionally contain
		 * %s as a placeholder for the pizza flavor.
		 */
		private final String messageOnHotPizza;

		/**
		 * The text that the customer should say upon quick delivery. It can
		 * optionally contain %s as a placeholder for the pizza flavor.
		 */
		private final String messageOnColdPizza;

		/**
		 * The min level player who can get to this NPC
		 */
		private final int level ;

		/**
		 * Creates a CustomerData object.
		 *
		 * @param npcDescription
		 * @param flavor
		 * @param expectedTime
		 * @param tip
		 * @param xp
		 * @param messageHot
		 * @param messageCold
		 * @param level
		 */
		CustomerData(final String npcDescription, final String flavor,
				final int expectedTime, final int tip, final int xp, final String messageHot,
				final String messageCold, final int level) {
			this.npcDescription = npcDescription;
			this.flavor = flavor;
			this.expectedMinutes = expectedTime;
			this.tip = tip;
			this.xp = xp;
			this.messageOnHotPizza = messageHot;
			this.messageOnColdPizza = messageCold;
			this.level = level;
		}
		
		/**
		 * Get the minimum level needed for the NPC
		 * 
		 * @return minimum level
		 */
		public int getLevel() {
			return level;
		}
	}

	private static final String QUEST_SLOT = "dostawca_pizzy2";

	private static Map<String, CustomerData> customerDB;



	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Spotkałem Ernesta i zgodził się pomóc przy roznoszeniu pizzy.");
		if (!"done".equals(questState)) {
			final String[] questData = questState.split(";");
			final String customerName = questData[0];
			final CustomerData customerData = customerDB.get(customerName);
			res.add("Ernest dał mi " + customerData.flavor + " dla " + customerName + ".");
			res.add("Ernest powiedział do mnie: \"" + customerData.npcDescription + "\"");
			if (!isDeliveryTooLate(player)) {
				res.add("Jeśli pośpiesze się, doniosę pizze jeszcze gorąco.");
			} else {
				res.add("Pizza jest już zimna.");
			}
		} else {
			res.add("Mam przy sobie pizzę, którą ostatnio dał mi Ernest.");
		}
		return res;
	}

	// Don't add Sally here, as it would conflict with Ernest telling
	// about his daughter.
	private static void buildCustomerDatabase() {
		customerDB = new HashMap<String, CustomerData>();

		customerDB.put("Malteis",
			new CustomerData(
				"Malteis jest rycerzem naszego miasteczka, żyje na wschód od centrum miasta.",
				"Pizza Prosciutto",
				// minutes to deliver. Tested by mort: 6:30
				// min, with killing some orcs.
				5,  
				// tip when delivered on time. Quite
				// high because you can't do much
				// senseful on top of the hill and must
				// walk down again.
				100,
				// experience gain for delivery
				150, 
				"Dziękuję! Zastanawiam się jakim sposobem udało Ci się dostarczyć pizzę tak szybko. Weź te %d monet jako napiwek. Tutaj nie mam ich jak wydać!",
				"Brrr. ta %s nie jest gorąca. Dziękuję, że próbowaleś.", 
				10));

		customerDB.put("Stary Baca",
			new CustomerData(
				"Stary Baca powinieneś go znaleźć gdzieś w okolicach kościeliska na zachód od zakopane. Łatwo go rozpoznać, posiada laskę oraz siwe włosy jak i siwą brodę, nosi zarówno góralską czapkę.",
				"Pizza Hawaii",
				// minutes to deliver. You need about 6 min
				// to Eliza, up to 12 min to wait for the
				// ferry, 5 min for the crossing, and 0.5
				// min from the docks to the beach, so you
				// need a bit of luck for this one.
				15, 
				// tip when delivered on time
				300, 
				// experience gain for delivery
				500, 
				"Nie sądziłem, że uda Ci się to dostarczyć! Jesteś wielki! Weź tę %s pieniędzy!",
				"Przyszła zimna, ale czego się mogłem spodziewać skoro pizza jest z piekarni daleko stąd... Dzięki.",
				20));

		customerDB.put("Kazimierz",
			new CustomerData(
				"Kazimierz, zwany kiedyś jako #'Bankier Kazimierz', znajduje się aktualnie w naszym banku.",
				"Pizza del Mare",
				// minutes to deliver. Tested by mort: 6
				// min, ignoring slow animals and not
				// walking through the swamps.
				3, 
				// tip when delivered on time.
				50, 
				// experience gain for delivery
				100, 
				"Nieprawdopodobne! Wciąż gorąca! Kup coś sobie za %d złota!",
				"Co za szkoda. Przyszła zimna. W każdym razie dziękuję!",
				20));

		customerDB.put("Aligern",
			new CustomerData(
				"Aligern mieszka w chatce na plaży w Gdańsku.",
				"Pizza Napoli",
				// minutes to deliver. Tested by mort: about
				// 6 min, outrunning all enemies.
				2,  
				// tip when delivered on time
				25, 
				// experience gain for delivery
				75, 
				"Dziękuję bardzo! Urodziłeś się wprost do roznoszenia pizzy. Trzymaj %d złota jako napiwek!",
				"Brr. Zimna pizza.",
				15));

		customerDB.put("Wielkolud",
			new CustomerData(
				"Wielkolud jest najwyższym człowiekiem mieszkającym na tutejszej Ziemi. Chodzi w okolicach północno-wschodnich regionach kościeliska.",
				"Pizza Diavolo",
				// minutes to deliver. Tested by kymara:
				// exactly 3 min.
				8,  
				// tip when delivered on time
				160, 
				// experience gain for delivery
				220, 
				"Ach, moja %s! I jest świeżo wyjęta z pieca! Weź te %d monet jako napiwek!",
				"Mam nadzieję, że następnym razem dostanę gorącą pizzę.",
				10));

		customerDB.put(
			"Ratownik Mariusz",
			new CustomerData(
				"Ratownik Mariusz jest ratownikiem, chodzi po pólnocno-wschodnich regionach zakopane.",
				"Pizza Margherita",
				// minutes to deliver. Tested by mort: can
				// be done in 1:15 min, with no real danger.
				7,  
				// tip when delivered on time
				140, 
				// experience gain for delivery
				200, 
				"Ach, przyniosłeś mi %s! Bardzo miło! Weź te %d monet jako napiwek!",
				"Co za szkoda. Twój serwis pizzy nie umie dostarczyć gorącej pizzy choć piekarnia jest tuż za rogiem.",
				15));

		customerDB.put("Jerzy",
			new CustomerData(
				"Jerzy jest naszym złotnikiem. Powinieneś go odnaleźć, mieszka nie daleko naszej piekarni.",
				"Pizza Funghi",
				// minutes to deliver. Tested by mort: 5:30
				// min, leaving the slow monsters on the way
				// behind.
				1,  
				// tip when delivered on time
				10, 
				// experience gain for delivery
				30, 
				"Och. Nie  spodziewałam się tak wcześnie pizzy. Super! Zazwyczaj nie daje napiwków, ale dla Ciebie zrobię wyjątek. Weź %d złota.",
				"Niedobrze... Będę musiała użyć extra silnego zaklęcia, aby pizza była gorąca.",
				2));

		customerDB.put("Farmer Mścisław",
			new CustomerData(
				"Farmer Mścisław posiada własną farmę w północno-zachodnich częściach miasta Gdańsk.",
				"Pizza Vegetale",
				// minutes to deliver. Tested by kymara in
				// 3:25 min, leaving behind the orcs.
				2,  
				// tip when delivered on time
				25, 
				// experience gain for delivery
				100, 
				"A! Moja %s! Weź %d złota jako napiwek!",
				"Ee. Nie cierpię zimnej pizzy. Chyba nakarmię nią zwierzęta.",
				2));

		customerDB.put("Janek", 
			new CustomerData(
				"Janek jest przewoźnikiem pomiędzy Krakowem, a Warszawą. Znajdziesz go w okolicach wschodnich Krakówa", "Pizza Tonno",
				// minutes to deliver. Tested by kymara: takes longer than before due to fence in village
				10, 
				// tip when delivered on time. A bit higher than Jenny
				// because you can't do anything else in the jail and need
				// to walk out again.
				150, 
				// experience gain for delivery
				350, 
				"Ach, moja %s! Oto twój napiwek: %d złota.",
				"Nareszcie! Dlaczego zajęło to Tobie tyle czasu?",
				20));

		customerDB.put("Staszek",
			new CustomerData(
				"Staszek jest przewoźnikiem między Gdańskiem, a Warszawą. Zawsze go widuję przy swoim porcie na północ stąd.",
				"Pizza Pasta",
				// minutes to deliver. Tested by mort: easy
				// to do in less than 1 min.
				2,  
				// tip when delivered on time
				30, 
				// experience gain for delivery
				75,  
				"Dziękuję! Szybki jesteś. Weź te %d złota jako napiwek!",
				"Niedobrze. Przyniosłeś zimną. W każdym razie dziękuję.",
				2));

		customerDB.put("Gazda Wojtek",
			new CustomerData(
				"Gazda Wojtek jest burmistrzem miasta Zakopane. Powinieneś go łatwo znaleźć.",
				"Pizza Quattro Stagioni",
				// minutes to deliver. Tested by mort: can
				// be done in 45 sec with no danger.
				3,  
				// tip when delivered on time
				50, 
				// experience gain for delivery
				105,  
				"Dziękuję! Dobrze jest mieć usługę pizzy zaraz za rogiem. Weź %d monet!",
				"Powinienem osobiście kupić pizzę, Byłoby szybciej.",
				5));

		customerDB.put("Bercik",
			new CustomerData(
				"Bercik jest egzaminatorem. Znajdziesz go w pewnym budynku na południowo-wschodnich okolic kościeliska.",
				"Pizza Bolognese",
				// minutes to deliver. You need about 6 mins
				// to Eliza, and once you board the ferry,
				// about 15 sec to deliver. If you have bad
				// luck, you need to wait up to 12 mins for
				// the ferry to arrive at the mainland, so
				// you need a bit of luck for this one.
				5, 
				// tip when delivered on time
				100, 
				// experience gain for delivery
				175, 
				"Dziękuję bardzo! Nareszcie dostałem lepsze jedzenie niż to które gotuje Laura. Weź te %d złota jako napiwek!",
				"Niedobrze. Jest zimna. Miałem nadzieje, że dostanę coś lepszego niż okrętowe jedzenie.",
				10));

		customerDB.put("Cheng",
			new CustomerData(
				"Cheng jest handlarzem rzadkich minerałów. Podobno znajduje się jeszcze w sukiennicach. Powinieneś go łatwo rozpoznać, ubiera się tylko w ciemne ubrania.",
				// "Pizza sheep" in Italian ;)
				"Pizza Pecora", 
				// minutes to deliver. Tested by kymara:
				// done in about 8 min, with lots of monsters getting in your way.
				14, 
				// tip when delivered on time
				200,
				// experience gain for delivery
				400, 
				"Pychota %s! Weź %d pieniędzy!",
				"Grrr. Pizza zimna. Ruszasz sie jak owca.",
				15));
				
    customerDB.put("Gazda Jędrzej",
				new CustomerData(
					"Gazda Jędrzej jest góralem. Musisz go szukać gdzieś na wzgórzu wschodniej części Zakopane.",
					"Pizza Fiorentina",
					// minutes to deliver. Time for Fidorea was 7, so 8 should be ok for martin
					8,  
					// tip when delivered on time
					160, 
					// experience gain for delivery
					220, 
					"Ooooh, Uwielbiam świeżą, gorącą pizze, dziękuję. Weź %d pieniędzy...!",
					"Hmpf.. zimna pizza.. ok.. Niech już będzie. Ale następnym razem proszę pospiesz się.",
					10));		
	}

	private void startDelivery(final Player player, final EventRaiser npc) {

		final String name = Rand.rand(getAllowedCustomers(player));
		final CustomerData data = customerDB.get(name);

		final Item pizza = SingletonRepository.getEntityManager().getItem("pizza");
		pizza.setInfoString(data.flavor);
		pizza.setDescription("Oto " + data.flavor + ".");
		pizza.setBoundTo(name);

		if (player.equipToInventoryOnly(pizza)) {
    		npc.say("Musisz przynieść "
    			+ data.flavor
    			+ " do "
    			+ Grammar.quoteHash("#" + name)
    			+ " w ciągu "
    			+ Grammar.quantityplnoun(data.expectedMinutes, "minute", "one")
    			+ ". Powiedz \"pizza\", a "
    			+ name
				+ " będzie wiedział, że ja Ciebie przysłałem. Aha załóż ten uniform i i nie upuść na ziemie " + data.flavor + "! Nasi klienci chcą świeżą.");
    		player.setOutfit(UNIFORM, true);
    		player.setQuest(QUEST_SLOT, name + ";" + System.currentTimeMillis());
		} else {
			npc.say("Wróć gdy będziesz miał miejsce na pizzę!");
		}
	}
	
	/**
	 * Get a list of customers appropriate for a player
	 * 
	 * @param player the player doing the quest
	 * @return list of customer data
	 */
	private List<String> getAllowedCustomers(Player player) {
		List<String> allowed = new LinkedList<String>();
		int level = player.getLevel();
		for (Map.Entry<String, CustomerData> entry : customerDB.entrySet()) {
			if (level >= entry.getValue().getLevel()) {
				allowed.add(entry.getKey());
			}
		}		
		return allowed;
	}

	/**
	 * Checks whether the player has failed to fulfil his current delivery job
	 * in time.
	 * 
	 * @param player
	 *            The player.
	 * @return true if the player is too late. false if the player still has
	 *         time, or if he doesn't have a delivery to do currently.
	 */
	private boolean isDeliveryTooLate(final Player player) {
		if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
			final String[] questData = player.getQuest(QUEST_SLOT).split(";");
			final String customerName = questData[0];
			final CustomerData customerData = customerDB.get(customerName);
			final long bakeTime = Long.parseLong(questData[1]);
			final long expectedTimeOfDelivery = bakeTime 
				+ (long) 60 * 1000 * customerData.expectedMinutes;
			if (System.currentTimeMillis() > expectedTimeOfDelivery) {
				return true;
			}
		}
		return false;

	}

	private void handOverPizza(final Player player, final EventRaiser npc) {
		if (player.isEquipped("pizza")) {
			final CustomerData data = customerDB.get(npc.getName());
			for (final Item pizza : player.getAllEquipped("pizza")) {
				final String flavor = pizza.getInfoString();
				if (data.flavor.equals(flavor)) {
					player.drop(pizza);
					// Check whether the player was supposed to deliver the
					// pizza. 
					if (player.hasQuest(QUEST_SLOT) && !player.isQuestCompleted(QUEST_SLOT)) {
						if (isDeliveryTooLate(player)) {
							if (data.messageOnColdPizza.contains("%s")) {
								npc.say(String.format(data.messageOnColdPizza, data.flavor));
							} else {
								npc.say(data.messageOnColdPizza);
							}
							player.addXP(data.xp / 2);
						} else {
							if (data.messageOnHotPizza.contains("%s")) {
								npc.say(String.format(data.messageOnHotPizza,
										data.flavor, data.tip));
							} else {
								npc.say(String.format(data.messageOnHotPizza,
										data.tip));
							}
							final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
							money.setQuantity(data.tip);
							player.equipOrPutOnGround(money);
							player.addXP(data.xp);
							player.addKarma(5);
						}
						new InflictStatusOnNPCAction("pizza").fire(player, null, npc);
						player.setQuest(QUEST_SLOT, "done");
						putOffUniform(player);
					} else {
						// This should not happen: a player cannot pick up a pizza from the ground 
						// that did have a flavor, those are bound. If a pizza has flavor the player
						// should only have got it from the quest.
						npc.say("Blee! Ta pizza jest brudna! Znalazłeś ją na ziemi?");
					}
					return;
				}
			}
			// The player has brought the pizza to the wrong NPC, or it's a plain pizza.
			npc.say("Nie, dziękuję. Lubię " + data.flavor + ".");
		} else {
			npc.say("Pizza? Gdzie?");
		}
	}

	/** Takes away the player's uniform, if the he is wearing it. 
	 * @param player to remove uniform from*/
	private void putOffUniform(final Player player) {
		if (UNIFORM.isPartOf(player.getOutfit())) {
			player.returnToOriginalOutfit();
		}
	}

	private void prepareBaker() {
		final SpeakerNPC Ernest = npcs.get("Ernest");

		// haven't done the pizza quest before or already delivered the last one, ok to wear pizza outfit
		Ernest.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new OutfitCompatibleWithClothesCondition(), new QuestNotActiveCondition(QUEST_SLOT)),
				ConversationStates.QUEST_OFFERED, 
				"Musisz szybko dostarczyć gorącą pizzę. Jeżeli będziesz szybki to dostaniesz niezły napiwek. Dasz radę?",
				null);
		
		// haven't done the pizza quest before or already delivered the last one, outfit would be incompatible with pizza outfit
		Ernest.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new OutfitCompatibleWithClothesCondition()), new QuestNotActiveCondition(QUEST_SLOT)),
				ConversationStates.ATTENDING, 
				"Przykro mi, ale nie możesz nosić naszego stroju dostawcy pizzy tak wyglądając. Jeżeli się przebierzesz to możesz znów mnie zapytać o #zadanie.",
				null);
		
		// pizza quest is active: check if the delivery is too late already or not
		Ernest.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, new QuestActiveCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						final String[] questData = player.getQuest(QUEST_SLOT)
								.split(";");
						final String customerName = questData[0];
						if (isDeliveryTooLate(player)) {
							// If the player still carries any pizza due for an NPC,
							// take it away because the baker is angry,
							// and because the player probably won't
							// deliver it anymore anyway.
							for (final Item pizza : player.getAllEquipped("pizza")) {
								if (pizza.getInfoString()!=null) {
									player.drop(pizza);
								}
							}
							npc.say("Widzę, że nie dostarczyłeś pizzy na czas do "
								+ customerName
								+ " Czy tym razem będziesz niezawodny i zdążysz dostarczyć pizzę nim wystygnie?");
						} else {
							npc.say("Wciąż masz pizzę do dostarczenia dla "
									+ customerName + " i pospiesz się!");
							npc.setCurrentState(ConversationStates.ATTENDING);
						}
				}
			});

		Ernest.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					startDelivery(player, npc);
				}
			});

		Ernest.add(
			ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Niedobrze. Będę musiał chyba poszukać bardziej odpowiedzialnej osoby do dostarczania pizzy.",
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
					putOffUniform(player);
				}
			});

		for (final String name : customerDB.keySet()) {
			final CustomerData data = customerDB.get(name);
			Ernest.addReply(name, data.npcDescription);
		}
	}

	private void prepareCustomers() {
		for (final String name : customerDB.keySet()) {
			final SpeakerNPC npc = npcs.get(name);
			if (npc == null) {
				logger.error("NPC " + name + " is used in the Pizza Delivery quest but does not exist in game.", new Throwable());
				continue;
			}

			npc.add(ConversationStates.ATTENDING, "pizza", null,
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
						handOverPizza(player, npc);
					}
				});
		}
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Dostawca Pizzy",
				"*Mniam!* Wspaniale pachnie. Jest gorąca... Pospiesz się z dostawą!",
				false);
		buildCustomerDatabase();
		prepareBaker();
		prepareCustomers();
	}

	@Override
	public String getName() {
		return "DostawcaPizzy2";
	}
	
	@Override
	public String getRegion() {
		return Region.GDANSK_CITY;
	}

	@Override
	public String getNPCName() {
		return "Ernest";
	}
}
