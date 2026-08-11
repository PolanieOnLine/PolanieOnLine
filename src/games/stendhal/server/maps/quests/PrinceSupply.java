/***************************************************************************
 *                   (C) Copyright 2018-2026 - Stendhal                    *
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

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.ZoneEnterExitListener;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.chest.PlayerPrivateStoredChest;
import games.stendhal.server.entity.mapstuff.chest.StoredChest;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemdataItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemdataItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import marauroa.common.game.RPObject;
import marauroa.common.game.SlotIsFullException;

public class PrinceSupply extends AbstractQuest {
	private static final Logger logger = Logger.getLogger(PrinceSupply.class);

	public static final String QUEST_SLOT = "prince_supply";
	private static final String ARMORY_ZONE = "int_warszawa_armory";
	private static final int CHEST_X = 4;
	private static final int CHEST_Y = 2;
	private static final int REQUIRED_MINUTES = 1440;

	private final SpeakerNPC npc = npcs.get("Książę");

	private final ZoneEnterExitListener armoryListener = new ZoneEnterExitListener() {
		@Override
		public void onEntered(final RPObject object, final StendhalRPZone zone) {
			if (object instanceof Player) {
				final Player player = (Player) object;
				if (player.isQuestInState(QUEST_SLOT, "start")) {
					ensurePrivateChestForPlayer(player);
				}
			}
		}

		@Override
		public void onExited(final RPObject object, final StendhalRPZone zone) {
			// The chest is persistent and remains available for the quest owner.
		}
	};

	private void prepareRequestingStep() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED,
			"Buntownicy opanowali arsenał w Warszawie! Potrzebuję śmiałka, który odbierze im rycerski rynsztunek mojej armii. Pomożesz mi odzyskać te skarby?",
			null);

		// player asks about quest which he has done already and he is allowed to repeat it
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES),
					new QuestStateStartsWithCondition(QUEST_SLOT, "done;")),
			ConversationStates.QUEST_OFFERED,
			"Moja armia musi być przygotowana na wygnanie buntowników z zamku! Pomożesz mi odzyskać te skarby?",
			null);

		// player asks about quest which he has done already but it is not time to repeat it
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
				new NotCondition(
					new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES)),
					new QuestStateStartsWithCondition(QUEST_SLOT, "done;")),
			ConversationStates.ATTENDING,
			null,
			new SayTimeRemainingAction(QUEST_SLOT, 1, REQUIRED_MINUTES,
			"Musimy przeliczyć wyposażenie. Wróć do mnie w ciągu "));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Wejdź do budynku arsenału, który znajduje się obok kuźni kowala. Bądź ostrożny, buntownicy pilnują swojego łupu!",
			new MultipleActions(
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0),
				new ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						ensurePrivateChestForPlayer(player);
					}
				}));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Być może nie zasłużyłeś na odpowiednią nagrodę.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private void prepareBringingStep() {
		npc.add(
			ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestInStateCondition(QUEST_SLOT, "start"),
					new NotCondition(
						new AndCondition(
								new PlayerHasItemdataItemWithHimCondition("kolczuga", QUEST_SLOT),
								new PlayerHasItemdataItemWithHimCondition("zbroja płytowa", QUEST_SLOT),
								new PlayerHasItemdataItemWithHimCondition("spodnie kolcze", QUEST_SLOT),
								new PlayerHasItemdataItemWithHimCondition("hełm kolczy", QUEST_SLOT),
								new PlayerHasItemdataItemWithHimCondition("buty kolcze", QUEST_SLOT)))),
			ConversationStates.ATTENDING,
			"Nie wracaj bez pełnego wyposażenia! Potrzebuję całego kompletu, inaczej rycerze nie będą gotowi do walki.",
			null);

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemdataItemAction("kolczuga", QUEST_SLOT));
		reward.add(new DropItemdataItemAction("zbroja płytowa", QUEST_SLOT));
		reward.add(new DropItemdataItemAction("spodnie kolcze", QUEST_SLOT));
		reward.add(new DropItemdataItemAction("hełm kolczy", QUEST_SLOT));
		reward.add(new DropItemdataItemAction("buty kolcze", QUEST_SLOT));
		reward.add(new IncreaseXPAction(9500));
		reward.add(new SetQuestAction(QUEST_SLOT, "done;"));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		reward.add(new IncreaseKarmaAction(15));
		reward.add(
			new ChatAction() {
				@Override
				public void fire(Player player, Sentence sentence, EventRaiser npc) {
					removePrivateChest(player);
				}
			});

		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(
					new GreetingMatchesNameCondition(npc.getName()),
					new PlayerHasItemdataItemWithHimCondition("kolczuga", QUEST_SLOT),
					new PlayerHasItemdataItemWithHimCondition("zbroja płytowa", QUEST_SLOT),
					new PlayerHasItemdataItemWithHimCondition("spodnie kolcze", QUEST_SLOT),
					new PlayerHasItemdataItemWithHimCondition("hełm kolczy", QUEST_SLOT),
					new PlayerHasItemdataItemWithHimCondition("buty kolcze", QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Doskonale! Dzięki Tobie buntownicy zostali upokorzeni, a moja armia znów jest gotowa. Królestwo będzie Ci wdzięczne.",
			new MultipleActions(reward));
	}

	static void ensurePrivateChestForPlayer(final Player player) {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(ARMORY_ZONE);
		removeLegacySharedChests(zone);

		if (findPrivateChest(zone, player) != null) {
			return;
		}

		final PlayerPrivateStoredChest chest = new PlayerPrivateStoredChest(player);
		chest.setPosition(CHEST_X, CHEST_Y);
		populateMissingQuestItems(player, chest);
		zone.add(chest);
		zone.storeToDatabase();
	}

	private static PlayerPrivateStoredChest findPrivateChest(final StendhalRPZone zone,
			final Player player) {
		for (final Entity entity : zone.getEntitiesOfClass(PlayerPrivateStoredChest.class)) {
			final PlayerPrivateStoredChest chest = (PlayerPrivateStoredChest) entity;
			if (chest.isOwnedBy(player)) {
				return chest;
			}
		}
		return null;
	}

	private static void removePrivateChest(final Player player) {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(ARMORY_ZONE);
		final List<Entity> chestsToRemove = new ArrayList<Entity>();
		for (final Entity entity : zone.getEntitiesOfClass(PlayerPrivateStoredChest.class)) {
			if (((PlayerPrivateStoredChest) entity).isOwnedBy(player)) {
				chestsToRemove.add(entity);
			}
		}
		for (final Entity chest : chestsToRemove) {
			zone.remove(chest.getID());
		}
		if (!chestsToRemove.isEmpty()) {
			zone.storeToDatabase();
		}
	}

	private static void removeLegacySharedChests(final StendhalRPZone zone) {
		final List<Entity> chestsToRemove = new ArrayList<Entity>();
		for (final Entity entity : zone.getEntitiesOfClass(StoredChest.class)) {
			if (!(entity instanceof PlayerPrivateStoredChest)
					&& Math.round(entity.getX()) == CHEST_X
					&& Math.round(entity.getY()) == CHEST_Y) {
				chestsToRemove.add(entity);
			}
		}
		for (final Entity chest : chestsToRemove) {
			zone.remove(chest.getID());
		}
	}

	private static void populateMissingQuestItems(final Player player,
			final PlayerPrivateStoredChest chest) {
		try {
			addQuestItemIfMissing(player, chest, "kolczuga",
					"Oto kolczuga należąca do specjalnego wyposażenia armii Książęcej.");
			addQuestItemIfMissing(player, chest, "zbroja płytowa",
					"Oto zbroja płytowa należąca do specjalnego wyposażenia armii Książęcej.");
			addQuestItemIfMissing(player, chest, "spodnie kolcze",
					"Oto spodnie kolcze należące do specjalnego wyposażenia armii Książęcej.");
			addQuestItemIfMissing(player, chest, "hełm kolczy",
					"Oto hełm kolczy należące do specjalnego wyposażenia armii Książęcej.");
			addQuestItemIfMissing(player, chest, "buty kolcze",
					"Oto buty kolcze należące do specjalnego wyposażenia armii Książęcej.");
		} catch (SlotIsFullException e) {
			logger.info("Could not add items to private PrinceSupply quest chest", e);
		}
	}

	private static void addQuestItemIfMissing(final Player player,
			final PlayerPrivateStoredChest chest, final String itemName,
			final String description) {
		if (new PlayerHasItemdataItemWithHimCondition(itemName, QUEST_SLOT)
				.fire(player, null, null)) {
			return;
		}

		final Item item = SingletonRepository.getEntityManager().getItem(
				itemName, ItemCreationContext.quest());
		item.setItemData(QUEST_SLOT);
		item.setDescription(description);
		chest.add(item);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Odbicie Arsenału",
				"Książęca armia musi odbić swój arsenał z rąk buntowników.",
				false);
		prepareRequestingStep();
		prepareBringingStep();

		final StendhalRPZone armory = SingletonRepository.getRPWorld().getZone(ARMORY_ZONE);
		if (armory != null) {
			armory.addZoneEnterExitListener(armoryListener);
		}
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(player.getGenderVerb("Rozmawiałem") + " z księciem.");
		final String questState = player.getQuest(QUEST_SLOT);
		if ("rejected".equals(questState)) {
			res.add("Nie wykonam zadania księcia, ponieważ obawiam się, że zginę!");
		}
		if (player.isQuestInState(QUEST_SLOT, "start", "done")) {
			res.add(player.getGenderVerb("Zgodziłem") + " się na odzyskanie arsenału dla armii książecej.");
		}

		if (isCompleted(player)) {
			res.add(player.getGenderVerb("Przekazałem") + " potrzebny arsenał Księciu.");
		}
		if(isRepeatable(player)){
			res.add("Podejrzewam, że Książe przeliczył już wyposażenie armii i będzie znów potrzebował pomocy.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Odbicie Arsenału";
	}

	@Override
	public String getRegion() {
		return Region.WARSZAWA;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
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
				new TimePassedCondition(QUEST_SLOT, REQUIRED_MINUTES)).fire(player, null, null);
	}
}
