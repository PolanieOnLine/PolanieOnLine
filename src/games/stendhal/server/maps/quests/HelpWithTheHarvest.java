/**
 * 
 */
package games.stendhal.server.maps.quests;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.block.Block;
import games.stendhal.server.entity.mapstuff.block.BlockTarget;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipRandomAmountOfItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.ResetBlockChatAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateGreaterThanCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * In this quest the player can help Eheneumniranin by bringing
 * two carts with straw up to the barn near Karl.
 * 
 * (proof of concept for pushable blocks)
 * 
 * @author madmetzger
 * 
 * 
 * QUEST: Help with the Harvest
 * 
 * PARTICIPANTS:
 * <ul>
 * <li> Eheneumniranin (the half-elf on Ados farm) </li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li> Eheneumniranin asks you to push some carts full of straw to Karl's barn </li>
 * <li> Push 2 carts to the designated spots in front of the barn </li> 
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li> 500 XP </li>
 * <li> some karma (5 + (2 | -2)) </li>
 * <li> between 10 and 20 <item>grain</item> </li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class HelpWithTheHarvest extends AbstractQuest {
	
	private static final String QUEST_SLOT = "helpwiththeharvest";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(Player player) {
		List<String> result = new ArrayList<String>();
		if(new QuestStartedCondition(QUEST_SLOT).fire(player, null, null) && !createFinishedCondition().fire(player, null, null)) {
			result.add("Chcę pomóc Eheneumniranin w jego żniwach.");
		}
		if (player.isQuestInState(QUEST_SLOT, "rejected")) {
		    result.add("Na dzień dzisiejszy praca na farmie jest zbyt ciężka dla mnie.");
		}

		if(constructHayCartsNotYetCompletedCondition().fire(player, null, null)) {
			result.add("Muszę doprowadzić dwa wóżki siana do stodoły na północ od Eheneumniranina.");
		}
		if(createTaskFinishedCondition().fire(player, null, null)) {
			result.add("Doprowadziłem wystarczającą ilość wózków do stodoły. Mogę teraz powiedzieć Eheneumniraninowi, że skończyłem.");
		}
		if(createFinishedCondition().fire(player, null, null)) {
			result.add("Pomogłem " + getNPCName() + " i dostałem swoją nagrodę.");
		}
		return result;
	}

	@Override
	public String getName() {
		return "Pomoc w żniwach";
	}
	
	@Override
	public int getMinLevel() {
		return 5;
	}

	@Override
	public void addToWorld() {
		placeCartsAndTargets();
		configureNPC();
		fillQuestInfo(getName(), "Eheneumniranin potrzebuje pomocy w żniwach.", false);
	}

	private void configureNPC() {
		SpeakerNPC npc = npcs.get("Eheneumniranin");
		
		/*
		 * Add a reply on the trigger phrase "quest"
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Jesteś tutaj, aby pomóc mi trochę w moich żniwach?",
				null);
		
		/*
		 * Player is interested in helping, so explain the quest.
		 */
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"To bardzo miło. Już byłem zmęczony dostarczaniem tych dwóch wóżków do Karla. Proszę #przepchnij dwa wózki do Karla i powiedz mi, gdy #skończysz.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start;2", 2.0));

		npc.addReply(Arrays.asList("push", "pchnąć", "przepchnij"), "Możesz łatwo poruszyć wózki pchając je przed wejście do stodoły. Uważaj, aby gdzieś nie utknąć, bo nie będziesz mógł ich przesunąć dalej.");
		
		npc.addReply(Arrays.asList("barn", "stodoła", "stodoły"), "Stodołe Karla możesz znaleść na północ stąd. Wyróżnia się ogromnym znakiem z jego imieniem.");

		/*
		 * Player refused to help - end the conversation.
		 */
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Już myślałem, że trochę mi pomożesz, ale dobrze. Dowidzenia.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -2.0));

		
		/*
		 * Player has not yet put the carts to the right spots
		 */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("done", "zrobiłem", "zrobiłeś"),
				constructHayCartsNotYetCompletedCondition(),
				ConversationStates.ATTENDING,
				"Jeszcze nie dostarczyłeś obu wózków z sianem do stodoły leżącej na północ stąd.",
				null);
		
		/*
		 * Player asks for a quest although he has it already open
		 */
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Już cię prosiłem o dostarczenie dwóch wózków z sianem do stodoły Karla. Daj znąć jeśli już to #zrobiłeś.",
				null);
		
		/*
		 * Player has put both carts at the right spots
		 */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("done"),
				createTaskFinishedCondition(),
				ConversationStates.ATTENDING,
				"Dziękuję za twoją pomoc przy żniwach. Oto twoja nagroda",
				createReward());
		/*
		 * Player has finished the quest and can get additional information
		 */
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("jenny"),
				createFinishedCondition(),
				ConversationStates.ATTENDING,
				"#Jenny możesz znaleść w pobliżu młyna Semos. Zmieli dla ciebie zboże na #mąkę o ile przyniesiesz jej kilak kłosów.",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("flour", "mąka", "mąkę"),
				createFinishedCondition(),
				ConversationStates.ATTENDING,
				"#Jenny zmieli zboże, które dałem ci jako nagrodę na mąkę, którą możesz użyć na #chleb?",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("bread", "chleb"),
				createFinishedCondition(),
				ConversationStates.ATTENDING,
				"#Erna jescze nie upiekła dla ciebie? Naprawdę warto ponieważ #Leander może użyć chleba do zrobienia #kanapek dla ciebie.",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("sandwich", "kanapka", "kanapek", "kanapki"),
				createFinishedCondition(),
				ConversationStates.ATTENDING,
				"Jeszcze nie próbowałeś #kanapek zrobionych przez #Leander? Są smaczne.",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("leander", "leandera"),
				createFinishedCondition(),
				ConversationStates.ATTENDING,
				"Leander prowadzi piekarnię w mieście Semos i może zrobić dla ciebie #kanapki o ile przyniesiesz mu składniki. Dlaczego nie złożysz mu wizyty?",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("erna"),
				createFinishedCondition(),
				ConversationStates.ATTENDING,
				"Erna jest asystentką #Leandera w piekarni. Jeżeli przyniesiesz jej #mąkę to upiecze dla ciebie #chleb.",
				null);

        /*
         * Add a reply on the trigger phrase "quest" after it is finished
         */
        npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, createFinishedCondition(), ConversationStates.ATTENDING, "We already brought in the complete harvest, thanks again for your help.", null);
	}


	/**
	 * Place the carts and targets into the zone
	 */
	private void placeCartsAndTargets() {
		StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("0_ados_forest_w2");
		
		ChatCondition c = constructHayCartsNotYetCompletedCondition();
		
		String cartDescription = "Oto wózek ze zbożem. Czy możesz go popchać do stodoły Karla?";
		
		Block cartOne = new Block(true, "hay_cart");
		cartOne.setPosition(87, 100);
		cartOne.setDescription(cartDescription);
		Block cartTwo = new Block(true, "hay_cart");
		cartOne.setPosition(79, 106);
		cartTwo.setDescription(cartDescription);
		
        ChatAction a = new MultipleActions(new IncrementQuestAction(QUEST_SLOT, 1, -1), new ResetBlockChatAction(cartOne), new ResetBlockChatAction(cartTwo));

		zone.add(cartOne);
		zone.add(cartTwo);
		
		BlockTarget targetOne = new BlockTarget();
		targetOne.setPosition(64, 75);
		targetOne.setDescription("Oto wyraźny punkt na ziemi. Wcześniej coś ciężkiego tutaj stało.");
		targetOne.setCondition(c);
		targetOne.setAction(a);
		
		BlockTarget targetTwo = new BlockTarget();
		targetTwo.setPosition(65, 75);
		targetTwo.setDescription("Oto wyraźny punkt na ziemi. Wcześniej coś ciężkiego tutaj stało.");
		targetTwo.setAction(a);
		targetTwo.setCondition(c);
		
		zone.add(targetOne);
		zone.add(targetTwo);
	}

	/**
	 * Create condition determining if straw carts have not been moved completely to the barn
	 * 
	 * @return the condition
	 */
	private ChatCondition constructHayCartsNotYetCompletedCondition() {
		ChatCondition c = new AndCondition(
								new QuestStartedCondition(QUEST_SLOT), 
								new QuestInStateCondition(QUEST_SLOT, 0, "start"), 
								new QuestStateGreaterThanCondition(QUEST_SLOT, 1, 0));
		return c;
	}
	
	/**
	 * Create condition determining when straw carts were move to the barn
	 * 
	 * @return the condition
	 */
	private ChatCondition createTaskFinishedCondition() {
		ChatCondition c = new AndCondition(
				new QuestStartedCondition(QUEST_SLOT), 
				new QuestInStateCondition(QUEST_SLOT, 0, "start"), 
				new QuestInStateCondition(QUEST_SLOT, 1, "0"));
		return c;
	}
	
	/**
	 * Create the reward action
	 * 
	 * @return the action for rewarding finished quest
	 */
	private ChatAction createReward() {
		return new MultipleActions(
					new IncreaseKarmaAction(5),
					new IncreaseXPAction(500),
					new EquipRandomAmountOfItemAction("zboże", 10, 20),
					new SetQuestAction(QUEST_SLOT, "done"));	
	}
	
	/**
	 * Create the condition determining if quest is finished
	 * 
	 * @return the condition
	 */
	private ChatCondition createFinishedCondition() {
		return new QuestCompletedCondition(QUEST_SLOT);
	}

	@Override
	public String getRegion() {
		return Region.ADOS_SURROUNDS;
	}

	@Override
	public String getNPCName() {
		return "Eheneumniranin";
	}

}
