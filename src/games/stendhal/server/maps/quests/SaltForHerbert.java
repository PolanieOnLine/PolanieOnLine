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
import games.stendhal.common.grammar.Grammar;
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
 * QUEST: SaltForHerbert
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Marianne, a little girl looking for eggs</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Marianne asks you for eggs for her pancakes</li>
 * <li> You collect 12 eggs from chickens</li>
 * <li> You give the eggs to Marianne.</li>
 * <li> Marianne gives you some seeds in return.<li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 20/40/60/80/100/120 money</li>
 * <li> 250 XP</li>
 * <li> Karma: 25</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> Unlimited, at least 60 minutes have to elapse before repeating</li>
 * </ul>
 */
public class SaltForHerbert extends AbstractQuest {
	private static final String QUEST_SLOT = "salt_for_herbert";
	private final SpeakerNPC npc = npcs.get("Herbert");

	private static final int REQUIRED_SALT = 8;
	private static final int REQUIRED_MINUTES = 60;

	private void prepareRequestingStep() {
		// player returns with the promised eggs
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
					new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new PlayerHasItemWithHimCondition("sól", REQUIRED_SALT)),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Witaj ponownie! Widzę, że masz przy sobie sól. " +
			"Wydobyłeś ją dla mnie?",
			null);

		//player returns without promised eggs
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(
							new PlayerHasItemWithHimCondition("sól", REQUIRED_SALT))),
			ConversationStates.ATTENDING,
			"Już wróciłeś? " +
			"Obiecałeś, że zbierzesz dla mnie trochę soli... " +
			"Nie masz przy sobie wystarczającą ilość!",
			null);

		// first chat of player with Marianne
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING, "Cześć... Potrzebuję małej przysługi.",
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
			"Potrzebuję soli. " +
			"Chciałem przygotować dla mojej rodziny obiad, lecz zabrakło mi kamiennej soli! " +
			"Czy mógłbyś wydobyć ją dla mnie?",
			null);

		// player returns - enough time has passed
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new QuestNotInStateCondition(QUEST_SLOT, "start"),
					new QuestStartedCondition(QUEST_SLOT),
					new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES)),
			ConversationStates.QUEST_OFFERED,
			"Znowu potrzebuję soli! " +
			"Czy mógłbym Cię ponownie poprosić o zebranie więcej soli dla mnie?",
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
					"Myślę, że sól, którą mi już przyniosłeś " +
					"wystarczy na jakąś chwilę..."));

		// player is willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"W porządku. Możesz znaleźć schodząc do naszej kopalni soli! " +
			"Wróć, kiedy zbierzesz co najmniej osiem kawałków kamiennej soli!",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Może ktoś inny mi pomoże...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void prepareBringingStep() {
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("sól", REQUIRED_SALT));
		reward.add(new IncreaseXPAction(250));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT));
		reward.add(new IncreaseKarmaAction(25));
		reward.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				int goldamount;
				goldamount = 20 * Rand.roll1D6();
				npc.say("Dziękuję Ci za pomoc! Proszę, weź " + goldamount + " dudków w nagrodę!");
				final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
				money.setQuantity(goldamount);
				player.equipOrPutOnGround(money);
				player.notifyWorldAboutChanges();
			}
		});

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("sól", REQUIRED_SALT),
			ConversationStates.ATTENDING, null,
			new MultipleActions(reward));

		//player said the eggs was for her but has dropped it from his bag or hands
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new NotCondition(new PlayerHasItemWithHimCondition("sól", REQUIRED_SALT)),
			ConversationStates.ATTENDING,
			"Hej! Gdzie masz sól?",
			null);

		// player had eggs but said it is not for Marianne
		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Cóż, może następnym razem mi przyniesiesz.",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Sól dla Herberta",
				"Herbert zamierza przygotować specjalną potrawę dla całej swojej rodziny, lecz potrzebuje soli.",
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
		res.add(Grammar.genderVerb(player.getGender(), "Poznałem") + " Herberta.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chcę pomagać Herbertowi.");
			return res;
		}
		res.add("Chcę pomóc Herbertowi");
		if (player.isEquipped("sól", REQUIRED_SALT) || isCompleted(player)) {
			res.add(Grammar.genderVerb(player.getGender(), "Wydobyłem") + " potrzebną sól dla Herbarta");
		}
		if (isCompleted(player)) {
			res.add(Grammar.genderVerb(player.getGender(), "Zaniosłem") + " Herbertowi sól.");
		}
		if(isRepeatable(player)){
			res.add("Herbert znowu potrzebuje sól.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Sól dla Herberta";
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
		return Region.WIELICZKA;
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
