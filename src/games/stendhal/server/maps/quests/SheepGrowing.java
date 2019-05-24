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

import games.stendhal.common.Level;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.maps.semos.city.SheepBuyerNPC.SheepBuyerSpeakerNPC;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * QUEST: Sheep Growing for Nishiya
 * 
 * PARTICIPANTS:
 * <ul>
 * <li>Nishiya (the sheep seller in Semos village)</li>
 * <li>Sato (the sheep buyer in Semos city)</li>
 * </ul>
 * 
 * STEPS:
 * <ul>
 * <li>Nishiya asks you to grow a sheep.</li>
 * <li>Sheep grows to weight 100.</li>
 * <li>Sheep is handed over to Sato.</li>
 * <li>Nishiya thanks you.</li>
 * </ul>
 * 
 * REWARD:
 * <ul>
 * <li>Maximum of (XP to level 2) or (30XP)</li>
 * <li>Karma: 10</li>
 * </ul>
 * 
 * REPETITIONS:
 * <ul>
 * <li>None</li>
 * </ul>
 */
public class SheepGrowing extends AbstractQuest {

	private static final String QUEST_SLOT = "sheep_growing";
	private static final String TITLE = "Sheep Growing for Nishiya";
	private static final int MIN_XP_GAIN = 30;

	@Override
	public void addToWorld() {
		fillQuestInfo(
				TITLE,
				"Nishiya, sprzedawca owiec, obiecał Sato, że da mu owcę. " +
					"Jest bardzo zajęty i potrzebuje pomocy w opiece nad " +
					"jedną z owiec i przyprowadzeniu jej do Sato.",
				true);
		generalInformationDialogs();
		preparePlayerGetsSheepStep();
		preparePlayerHandsOverSheepStep();
		preparePlayerReturnsStep();
	}
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new LinkedList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Nishiya zapytał mnie, czy mógłbym zająć się owcą dla niego.");
		
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Mówiłem Nishiyi, że mam teraz ważniejsze sprawy na głowie... może będę mieć na to czas później.");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "handed_over", "done")) {
			res.add("Obiecuję zaopiekować się jedną z jego owiec.");
		}
		if (player.isQuestInState(QUEST_SLOT, "handed_over", "done")) {
			res.add("Przekazałem owcę, by podrosła, Sato. Powinienem wrócić teraz do Nishiyi.");
		}
		if(questState.equals("done")) {
			res.add("Wróciłem do sprzedawcy owiec. Nishiya był bardzo szczęśliwy, że mu pomogłem!");
		}
		return res;
	}
	
	@Override
	public String getName() {
		return TITLE;
	}
	
	/**
	 * General information for the player related to the quest.
	 */
	private void generalInformationDialogs() {
		final SpeakerNPC npc = npcs.get("Nishiya");
		
		npc.add(ConversationStates.ATTENDING, "Sato", null, ConversationStates.ATTENDING, "Sato zajmuje się skupem owiec w mieście Semos. " +
				"Znajdziesz go, jeśli pójdziesz ścieżką na wschód.", null);
		npc.add(ConversationStates.QUEST_OFFERED, "Sato", null, ConversationStates.QUEST_OFFERED, "Sato zajmuje się skupem owiec w mieście Semos. " +
				"Znajdziesz go, jeśli pójdziesz ścieżką na wschód.", null);
		
		List<String> berryStrings = new ArrayList<String>();
		berryStrings.add("red berries");
		berryStrings.add("berries");
		berryStrings.add("sheepfood");
		berryStrings.add("sheep food");
		berryStrings.add("trawa");
		npc.addReply(berryStrings, "Owce lubią czerwone jagody z krzewów brusznicy i trawę.");
		
		npc.addReply("owca", "Sprzedaję puszyste owce, to moja #'praca'.");
	}
	/**
	 * The step where the player speaks with Nishiya about quests and gets the sheep.
	 */
	private void preparePlayerGetsSheepStep() {
		final SpeakerNPC npc = npcs.get("Nishiya");
		
		// If quest is not done or started yet ask player for help (if he does not have a sheep already)
		ChatCondition playerHasNoSheep = new ChatCondition() {
            @Override
			public boolean fire(Player player, Sentence sentence, Entity npc) {
				return !player.hasSheep();
			}
		};
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(playerHasNoSheep, 
						new QuestNotInStateCondition(QUEST_SLOT, "start"), 
						new QuestNotInStateCondition(QUEST_SLOT, "handed_over"),
						new QuestNotInStateCondition(QUEST_SLOT, "done")),
				ConversationStates.QUEST_OFFERED,
				"Ostatnio jestem bardzo zajęty wszystkimi moimi owcami. " +
				"Czy byłbyś skłonny  zająć się jedną z moich owiec i oddać ją #'Sato'? " +
				"Trzeba tylko ją utuczyć - pozwolić jej jeść czerwone jagody, dopóki nie będzie ważyć " + Sheep.MAX_WEIGHT + " kilo." +
				"Zająłbyś się tym?",
				new SetQuestAction(QUEST_SLOT, "asked"));
		
		// If quest is offered and player says no reject the quest
		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES,
				new AndCondition(playerHasNoSheep, 
						new QuestInStateCondition(QUEST_SLOT, "asked")),
				ConversationStates.IDLE,
				"Ok... muszę pracować dwa razy ciężej w najbliższych dniach...",
				new SetQuestAction(QUEST_SLOT, "rejected"));
		
		// If quest is still active but not handed over do not give an other sheep to the player
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
					new QuestActiveCondition(QUEST_SLOT),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT, "asked")),
					new NotCondition(new QuestInStateCondition(QUEST_SLOT, "handed_over"))),
				ConversationStates.ATTENDING,
				"Dałem Ci już jedną z moich owiec. " +
				"Jeśli zostawisz ją, sprzedam Ci kolejną. Po prostu powiedz #kupię #sheep.",
				null);
		
		// If quest is offered and player says yes, give a sheep to him.
		List<ChatAction> sheepActions = new LinkedList<ChatAction>();
		sheepActions.add(new SetQuestAction(QUEST_SLOT, "start"));
		sheepActions.add(new ChatAction() {
            @Override
			public void fire(Player player, Sentence sentence, EventRaiser npc) {
				final Sheep sheep = new Sheep(player);
				StendhalRPAction.placeat(npc.getZone(), sheep, npc.getX(), npc.getY() + 1);
			}
		});
		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(playerHasNoSheep, 
						new QuestInStateCondition(QUEST_SLOT, "asked")),
				ConversationStates.IDLE,
				"Dzięki! *uśmiecha się* Proszę, o to Twój puszysty wychowanek. Opiekuj się nim! " +
				"Jeśli owieczka umrze albo ją zostawisz, będziesz zmuszony kupić nową. " +
				"Oh... I nie sprzedaj przypadkowo owcy Sato - porozmawiaj z nim, gdy owca urośnie.",
				new MultipleActions(sheepActions));
	}
	/**
	 * The step where the player goes to Sato to give him the grown up sheep.
	 */
	private void preparePlayerHandsOverSheepStep() {
		// Remove action
		final List<ChatAction> removeSheepAction = new LinkedList<ChatAction>();
		removeSheepAction.add(new ChatAction() {
            @Override
			public void fire(Player player, Sentence sentence, EventRaiser npc) {
				// remove sheep
				final Sheep sheep = player.getSheep();
				if(sheep != null) {
					player.removeSheep(sheep);
					player.notifyWorldAboutChanges();
					if(npc.getEntity() instanceof SheepBuyerSpeakerNPC) {
						((SheepBuyerSpeakerNPC)npc.getEntity()).moveSheep(sheep);
					} else {
						// only to prevent that an error occurs and the sheep does not disappear
						sheep.getZone().remove(sheep);
					}
				} else {
					// should not happen
					npc.say("Co? Jaka owca? Zapomniałem o czymś?");
					npc.setCurrentState(ConversationStates.IDLE);
					return;
				}
			}
		});
		removeSheepAction.add(new SetQuestAction(QUEST_SLOT, "handed_over"));
		
		// Hand-Over condition
		ChatCondition playerHasFullWeightSheep = new ChatCondition() {
            @Override
			public boolean fire(Player player, Sentence sentence, Entity npc) {
				return player.hasSheep()
					&& player.getSheep().getWeight() >= Sheep.MAX_WEIGHT;
			}
		};
		
		// Sato asks for sheep
		final SpeakerNPC npc = npcs.get("Sato");
		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT,"start"),
						playerHasFullWeightSheep),
				ConversationStates.QUEST_ITEM_BROUGHT,
				"Cześć. Cóż za piękna i zdrowa owca przyszła tutaj z Tobą! Czyżby była ona dla mnie?",
				null);

		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT,"start"),
						new NotCondition(playerHasFullWeightSheep)),
				ConversationStates.IDLE,
				"Witaj. Powinieneś mieć owcę od Nishiyi dla mnie! Ale chcę najedzoną. Wróć, gdy będziesz miał taką. Dowidzenia!",
				null);
		
		// Player answers yes - Sheep is given to Sato
		npc.add(
				ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT,"start"),
						playerHasFullWeightSheep),
				ConversationStates.IDLE,
				"Wiedziałem! Kupiłeś ją u Nishiyi, prawda? Czekałem na nią! " +
				"To prezent dla mojego przyjaciela. Byłoby mi wstyd, gdybym nie przyniósł mu prezentu urodzinowego! " +
				"Przekaż Nishiyi moje szczere podziękowania.",
				new MultipleActions(removeSheepAction));
		
		// Player answers no - Sheep stays at player
		npc.add(
				ConversationStates.QUEST_ITEM_BROUGHT,
				ConversationPhrases.NO_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT,"start"),
						playerHasFullWeightSheep),
				ConversationStates.IDLE,
				"Chciał wysłać mi jedną jakiś czas temu ...",
				null);
        

		npc.add(
				ConversationStates.ATTENDING, 
				ConversationPhrases.QUEST_MESSAGES,
						new QuestInStateCondition(QUEST_SLOT, "handed_over"),
				ConversationStates.ATTENDING, 
				"Dziękuję za przyniesienie mi owcy Nishiyi! Mój przyjaciel był z tego szczęśliwy.", null);
	}
	
	/**
	 * The step where the player returns to Nishiya to get his reward.
	 */
	private void preparePlayerReturnsStep() {
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new ChatAction() {
            @Override
			public void fire(Player player, Sentence sentence, EventRaiser npc) {
				// give XP to level 2
                int reward = Level.getXP( 2 ) - player.getXP();
				if(reward > MIN_XP_GAIN) {
					player.addXP(reward);
				} else {
					player.addXP(MIN_XP_GAIN);
				}
				player.notifyWorldAboutChanges();
			}
		});
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction( 10 ));
		
		final SpeakerNPC npc = npcs.get("Nishiya");
		// Asks player if he handed over the sheep
		npc.add(
				ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "handed_over"),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Dałeś już owcę Sato?",
				null);
		// Player answers yes - give reward
		npc.add(
				ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "handed_over"),
				ConversationStates.IDLE,
				"Dziękuję! Nie wiesz, jak wiele muszę teraz robić! Jestem bardzo zapracowany. " +
				"Naprawdę mi pomogłeś.",
				new MultipleActions(reward));
		// Player answers no - 
		npc.add(
				ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "handed_over"),
				ConversationStates.IDLE,
				"W porządku... Nie nie zapomnij o tym. Sato pilnie potrzebuje owcy!",
				null);
		
		// Player asks for quest after solving the quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Przykro mi, ale nie mam teraz żadnego zadania dla Ciebie. Ale jeszcze raz dziękuję za pomoc!",
				null);
	}
	
	@Override
	public String getRegion() {
		return Region.SEMOS_CITY;
    }

	@Override
	public String getNPCName() {
		return "Nishiya";
	}
}