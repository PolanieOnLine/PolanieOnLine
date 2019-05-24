/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
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
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
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
import marauroa.common.game.IRPZone;

/**
 * QUEST: FishSoupForHughie
 *
 * PARTICIPANTS:
 * <ul>
 * <li> Anastasia, a worried mother in Ados farmhouse</li>
 * <li> Hughie, her son</li>
 * </ul>
 *
 * STEPS:
 * <ul>
 * <li> Anastasia asks for some fish soup for her sick boy</li>
 * <li> You collect the fish soup</li>
 * <li> You give the fish soup to Anastasia.</li>
 * <li> Anastasia rewards you.<li>
 * </ul>
 *
 * REWARD:
 * <ul>
 * <li> 10 potions</li>
 * <li> xp </li>
 * <li> Karma: 5</li>
 * </ul>
 *
 * REPETITIONS:
 * <ul>
 * <li> Unlimited, but 7 days of waiting are required between repetitions</li>
 * </ul>
 */
public class FishSoupForHughie extends AbstractQuest {

	private static final int REQUIRED_MINUTES = 7 * MathHelper.MINUTES_IN_ONE_DAY;

	private static final String QUEST_SLOT = "fishsoup_for_hughie";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public boolean isCompleted(final Player player) {
		return player.hasQuest(QUEST_SLOT) && !"start".equals(player.getQuest(QUEST_SLOT)) && !"rejected".equals(player.getQuest(QUEST_SLOT));
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "start"), new QuestStartedCondition(QUEST_SLOT), new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES)).fire(player, null, null);
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Anastasia poprosiła mnie, żebym przyniósł zupę rybną dla jej syna, Hughiego.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie chce pomóc dla Hughiego.");
			return res;
		}
		res.add("Naprawdę chcę pomóc Hughiemu i Anastasii.");
		if (player.isEquipped("zupa rybna") || isCompleted(player)) {
			res.add("Mam składniki niezbędne do zrobienia zupy rybnej, która uleczy Hugiego.");
		}
		if (isCompleted(player)) {
			res.add("Hughie zjadł swoją zupę, a Anastasia dała mi eliksiry.");
		}
		if(isRepeatable(player)){
			res.add("Minęło już trochę czasu, odkąd sprawdziłem, co słychać u Hugiego i Anastasii. Muszę pamiętać, żeby znowu zobaczyć się z nimi.");
		}
		return res;
	}



	private void prepareRequestingStep() {
		final SpeakerNPC npc = npcs.get("Anastasia");

		// player returns with the promised fish soup
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"), new PlayerHasItemWithHimCondition("zupa rybna")),
			ConversationStates.QUEST_ITEM_BROUGHT,
			"Hej, widzę, że masz zupę rybną, czy ona jest dla Hugiego?",
			null);

		//player returns without promised fish soup
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"), new NotCondition(new PlayerHasItemWithHimCondition("zupa rybna"))),
			ConversationStates.ATTENDING,
			"Już wróciłeś? Hugie jest coraz bardziej chory! Nie zapomnij o zupie rybnej dla niego, bardzo Cię proszę! Obiecuję, że sowicie Cię wynagrodzę!",
			null);

		// first chat of player with Anastasia
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING, "Witam, naprawdę może mi pomóc, proszę.",
			null);

		// player who is rejected or 'done' but waiting to start again, returns
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestNotInStateCondition(QUEST_SLOT, "start"),
					new QuestStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj ponownie.",
			null);

		// if they ask for quest while on it, remind them
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestInStateCondition(QUEST_SLOT, "start"),
			ConversationStates.ATTENDING,
			"Obiecałeś mi już, że przyniesiesz mi zupę rybną dla Hughiego! Pospiesz się, proszę!",
			null);

		// first time player asks/ player had rejected
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Mój biedny chłopak jest chory, a lekarstwa, które mu podaję, nie działają! Proszę, przyniesiesz dla niego zupę rybną?",
				null);

		// player returns - enough time has passed
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "start"), new QuestStartedCondition(QUEST_SLOT), new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Mój Hughie znowu zaczyna chorować! Możesz przynieść mi jeszcze jedną zupę rybną? Pomóż mi, proszę, ostatni raz!",
				null);

		// player returns - not enough time has passed
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new QuestNotInStateCondition(QUEST_SLOT, "start"), new QuestStartedCondition(QUEST_SLOT), new NotCondition(new TimePassedCondition(QUEST_SLOT,REQUIRED_MINUTES))),
				ConversationStates.ATTENDING,
				"Teraz Hugie śpi - ma gorączkę. Mam nadzieję, że wyzdrowieje. Jestem Ci dozgonnie wdzięczna!.",
				null);

		// player is willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Dziękuję! Możesz poprosić Florence Bouillabaisse, aby ci ugotowała zupę rybną. Myślę, że znajdziesz ją na targu w Ados. ",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));

		// player is not willing to help
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Nie, proszę! On jest naprawdę bardzo chory.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void prepareBringingStep() {
		final SpeakerNPC npc = npcs.get("Anastasia");
		// player has fish soup and tells Anastasia, yes, it is for her

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("zupa rybna"));
		reward.add(new IncreaseXPAction(200));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT));
		reward.add(new IncreaseKarmaAction(10));
		reward.add(new EquipItemAction("eliksir",10));
		reward.add(new ChatAction() {
			@Override
			public void fire(final Player player, final Sentence sentence, final EventRaiser npc) {
				final Item soup = SingletonRepository.getEntityManager()
				.getItem("zupa rybna");
				final IRPZone zone = SingletonRepository.getRPWorld().getZone("int_ados_farm_house_1");
				// place on table
				soup.setPosition(32, 5);
				// only allow Hughie, our npc, to eat the soup
				soup.setBoundTo("Hughie");
				zone.add(soup);
			}
		});

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new PlayerHasItemWithHimCondition("zupa rybna"),
			ConversationStates.ATTENDING, "Dziękuję! Jestem Ci niesamowicie wdzięczna za Twoją przysługę. Nakarmię Hughiego, gdy się obudzi. Proszę, weź te mikstury - Hughiemu nie pomagają i tak.",
			new MultipleActions(reward));

		//player said the fish soup was for her but has dropped it from his bag or hands
		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			new NotCondition(new PlayerHasItemWithHimCondition("zupa rybna")),
			ConversationStates.ATTENDING,
			"Oh! Gdzie masz zupę rybną?",
			null);

		// player had fish soup but said it is not for Hughie
		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Oh...ale mój biedny chłopczyk...",
			null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zupa Rybna dla Hughie",
				"Syn Anastasii, Hugie, jest chory i potrzebuje czegoś, co go wyleczy.",
				true);
		prepareRequestingStep();
		prepareBringingStep();
	}

	@Override
	public String getName() {
		return "Fish Soup For Hughie";
	}

	@Override
	public int getMinLevel() {
		return 10;
	}

	@Override
	public String getNPCName() {
		return "Anastasia";
	}

	@Override
	public String getRegion() {
		return Region.ADOS_SURROUNDS;
	}
}
