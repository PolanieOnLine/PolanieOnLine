/***************************************************************************
 *                   (C) Copyright 2019-2021 - Stendhal                    *
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
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

/**
 * QUEST: EggsForMarianne
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Marianne, a little girl looking for eggs</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Marianne asks you for eggs for her pancakes</li>
 * <li> You collect a dozen of eggs from chickens</li>
 * <li> You give a dozen of eggs to Marianne.</li>
 * <li> Marianne gives you some flowers in return.<li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> some pansy or daisies</li>
 * <li> 100 XP</li>
 * <li> Karma: 50</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> Unlimited, at least 60 minutes have to elapse before repeating</li>
 * </ul>
 */
public class EggsForMarianne extends AbstractQuest {
	private static final String QUEST_SLOT = "eggs_for_marianne";
	private final SpeakerNPC npc = npcs.get("Marianne");

	private static final int REQUIRED_EGGS = 12; //a dozen of eggs
	private static final int REQUIRED_MINUTES = 60; //60 minutes before quest can be repeated

	private void prepareRequestingStep() {
		// player returns with the promised eggs
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
					new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("jajo", REQUIRED_EGGS)),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Witaj ponownie! Widzę, że masz kilka jajek. " +
			"Czy masz tyle jaj, o ile wcześniej prosiłam?",
			null);

		//player returns without promised eggs
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(
							new PlayerHasItemWithHimCondition("jajo", REQUIRED_EGGS))),
			ConversationStates.ATTENDING,
			"Już wróciłeś? " +
			"Obiecałeś, że zbierzesz dla mnie trochę jajek... " + 
			"Ale nie wydaje się, abyś miał przy sobie wystarczająco dużo jajek!",
			null);

		// first chat of player with Marianne
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING, "Cześć... Potrzebuję małej przysługi, jeśli już tak uprzejmie wyglądasz...",
			null);

		// player who is rejected or 'done' but waiting to start again, returns
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotInStateCondition(QUEST_SLOT, "start"),
					new QuestStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witam ponownie!",
			null);

		// if they ask for quest while on it, remind them
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Obiecałeś mi już, że przyniesiesz jajka, nie pamiętasz?",
			null);

		// first time player asks/ player had rejected
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Potrzebuję jaja. " +
				"Moja mama poprosiła mnie o zebranie tuzin jajek, a ona zrobi mi naleśniki! " +
				"Boję się zbliżyć do tych kurczaków! " +
				"Czy możesz zebrać jajka dla mnie? " +
				"Pamiętaj... Potrzebuję jaja...",
				null);

		// player returns - enough time has passed
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestNotInStateCondition(QUEST_SLOT, "start"),
						new QuestStartedCondition(QUEST_SLOT),
						new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Moja mama znów potrzebuje jajek! " +
				"Czy mogłabym Cię ponownie poprosić o zebranie tuzin jaj dla mnie?",
				null);

		// player returns - enough time has passed
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "start"),
				new QuestStartedCondition(QUEST_SLOT),
				new NotCondition(new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT,REQUIRED_MINUTES,
						"Dziękuję! " +
						"Myślę, że jajka, które mi już przyniosłeś " +
						"wystarczą na jakąś chwilę..."));

		// player is willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"W porządku. Możesz znaleźć jaja polując na kurczaki! " +
			"Możesz je znaleźć w całym mieście. "+
			"Wróć, kiedy zbierzesz tuzin jajek!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 2));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Och, co mam zrobić z tymi wszystkimi kwiatkami? " +
			"Może po prostu posadzę je wokół niektórych grobów...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void prepareBringingStep() {
		// player has eggs and tells Marianne, yes, it is for her
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("jajo", REQUIRED_EGGS));
		reward.add(new IncreaseXPAction(100));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT));
		reward.add(new IncreaseKarmaAction(50));
		reward.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				String rewardClass;
				if (Rand.throwCoin() == 1) {
					rewardClass = "bratek";
				} else {
					rewardClass = "stokrotki";
				}
				npc.say("Dziękuję Ci! Proszę, weź trochę " + rewardClass + "!");
				final StackableItem reward = (StackableItem) SingletonRepository.getEntityManager().getItem(rewardClass);
				reward.setQuantity(REQUIRED_EGGS);
				player.equipOrPutOnGround(reward);
				player.notifyWorldAboutChanges();
			}
		});

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("jajo", REQUIRED_EGGS),
			ConversationStates.ATTENDING, null,
			new MultipleActions(reward));

		//player said the eggs was for her but has dropped it from his bag or hands
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new NotCondition(new PlayerHasItemWithHimCondition("jajo", REQUIRED_EGGS)),
			ConversationStates.ATTENDING,
			"Hej! Gdzie zostawiłeś jajka?",
			null);

		// player had eggs but said it is not for Marianne
		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Och ... cóż, mam nadzieję, że znajdziesz szybko. " +
			"Robię się głodna!",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Kurze Jaja",
				"Matka Marianny zamierza zrobić naleśniki, lecz potrzebuje jajka.",
				true);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Marianne w okolic Deniran.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pomagać Mariannie.");
			return res;
		}
		res.add("Chętnie pomogę biednej Mariannie w zdobyciu tuzin kurzyj jaj.");
		if (player.isEquipped("egg", REQUIRED_EGGS) || isCompleted(player)) {
			res.add("Znalazłem jajka dla Marianny");
		}
		if (isCompleted(player)) {
			res.add("Zaniosłem Mariannie kurze jaja, a w zamian otrzymałem pięknego kwiatka.");
		}
		if(isRepeatable(player)){
			res.add("Marianna znowu potrzebuje kurzych jaj.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Kurze Jaja";
	}

	@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getRegion() {
		return Region.DENIRAN;
	}

	@Override
	public boolean isCompleted(final Player player) {
		return player.hasQuest(QUEST_SLOT) && !"start".equals(player.getQuest(QUEST_SLOT)) && !"rejected".equals(player.getQuest(QUEST_SLOT));
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(
				new QuestNotInStateCondition(QUEST_SLOT, "start"),
				new QuestStartedCondition(QUEST_SLOT),
				new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES)).fire(player, null, null);
	}
}
