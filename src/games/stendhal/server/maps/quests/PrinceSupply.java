/***************************************************************************
 *                   (C) Copyright 2018-2021 - Stendhal                    *
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

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.chest.Chest;
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
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.ChestSlot;
import games.stendhal.server.maps.Region;
import marauroa.common.game.SlotIsFullException;

public class PrinceSupply extends AbstractQuest {
	private static final Logger logger = Logger.getLogger(PrinceSupply.class);

	public static final String QUEST_SLOT = "prince_supply";
	private final SpeakerNPC npc = npcs.get("Książę");

	private static final int REQUIRED_MINUTES = 1440;
	private static final int CHEST_BASE_X = 4;
	private static final int CHEST_BASE_Y = 2;
	private static final int CHEST_MAX_RADIUS = 3;

	private void prepareRequestingStep() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, 
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.QUEST_OFFERED, 
			"Zdradziecki oddział magnata Gonta przejął warszawski arsenał i więzi moje zapasy. Potrzebuję emisariusza, który odzyska rycerski ekwipunek, nim morale upadnie. Wesprzesz mnie?",
			null);

		// player asks about quest which he has done already and he is allowed to repeat it
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES,
			new AndCondition(
					new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_MINUTES),
					new QuestStateStartsWithCondition(QUEST_SLOT, "done;")),
			ConversationStates.QUEST_OFFERED,
			"Czas znów odświeżyć regiment. Czy ponownie przenikniesz do arsenału i zabierzesz to, co należy do Korony?",
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
			"Królowscy kwartmistrze nadal liczą zapasy. Wróć do mnie za "));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Idź do budynku arsenału obok kuźni kowala. Wewnątrz znajdziesz skrzynię oznaczoną Twoim imieniem. Buntownicy czuwają, więc działaj roztropnie!",
			new MultipleActions(
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0),
				new ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						PrinceSupply.prepareChest(player);
					}
				}));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			"Rozumiem. Jednak bez bohaterów takich jak Ty moje królestwo wiele traci.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}

	private static void removeAllQuestChests() {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_warszawa_armory");
		final List<Entity> chestsToRemove = new ArrayList<Entity>();
		for (Entity entity : zone.getEntitiesOfClass(PrinceArmoryChest.class)) {
			chestsToRemove.add(entity);
		}
		for (Entity entity : chestsToRemove) {
			zone.remove(entity.getID());
		}
	}

	private void prepareBringingStep() {
		npc.add(
				ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestStateStartsWithCondition(QUEST_SLOT, "start"),
						new NotCondition(
								new AndCondition(
										new PlayerHasItemdataItemWithHimCondition("kolczuga", QUEST_SLOT),
										new PlayerHasItemdataItemWithHimCondition("zbroja płytowa", QUEST_SLOT),
										new PlayerHasItemdataItemWithHimCondition("spodnie kolcze", QUEST_SLOT),
										new PlayerHasItemdataItemWithHimCondition("hełm kolczy", QUEST_SLOT),
										new PlayerHasItemdataItemWithHimCondition("buty kolcze", QUEST_SLOT)))),
				ConversationStates.ATTENDING,
				"Nie wracaj bez całego regimentu! Każdy element jest zaklęty na Twoją pieczęć — tylko komplet ocali mój oddział.",
				new ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
						PrinceSupply.ensureChestForPlayer(player);
					}
				});

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
					PrinceSupply.removeChest(player);
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
			"Doskonale! Dzięki Tobie Gont będzie szeptał ze strachu, a moi rycerze znów staną do marszu. Królestwo zawsze zapamięta Twoją odwagę.",
			new MultipleActions(reward));
	}

	private static void prepareChest(final Player player) {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_warszawa_armory");

		removeChest(player);

		final PrinceArmoryChest chest = new PrinceArmoryChest(player);
		final Point chestSpot = findAvailableChestSpot(zone, chest);
		if (chestSpot == null) {
			logger.warn("No free spot found for PrinceSupply chest in int_warszawa_armory");
			player.sendPrivateText("Zbrojownia jest teraz zatłoczona. Zaczekaj chwilę, aż strażnicy uprzątną miejsce na Twoją skrzynię.");
			return;
		}

		chest.setPosition(chestSpot.x, chestSpot.y);
		zone.add(chest);

		try {
			Item item = SingletonRepository.getEntityManager().getItem("kolczuga");
			item.setItemData(QUEST_SLOT);
			item.setDescription("Kolczuga odznacza się herbem Księcia i reaguje na dotyk jej wybranego opiekuna.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("zbroja płytowa");
			item.setItemData(QUEST_SLOT);
			item.setDescription("Zbroja płytowa z warszawskiej kuźni. Książę wydał ją tylko Tobie.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("spodnie kolcze");
			item.setItemData(QUEST_SLOT);
			item.setDescription("Spodnie kolcze z arsenału Księcia. Rozpoznają Twoją pieczęć zadania.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("hełm kolczy");
			item.setItemData(QUEST_SLOT);
			item.setDescription("Hełm kolczy odbija światło na kolor rodowej korony Księcia.");
			chest.add(item);

			item = SingletonRepository.getEntityManager().getItem("buty kolcze");
			item.setItemData(QUEST_SLOT);
			item.setDescription("Buty kolcze zostały zaczarowane tak, by kroczył w nich tylko bohater zadania.");
			chest.add(item);
		} catch (SlotIsFullException e) {
			logger.info("Could not add items to quest chest", e);
		}

		player.setQuest(QUEST_SLOT, "start;" + player.getName());
	}

	private static Point findAvailableChestSpot(final StendhalRPZone zone, final PrinceArmoryChest chest) {
		for (int radius = 0; radius <= CHEST_MAX_RADIUS; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dy = -radius; dy <= radius; dy++) {
					if (radius != 0 && Math.max(Math.abs(dx), Math.abs(dy)) != radius) {
						continue;
					}

					final int candidateX = CHEST_BASE_X + dx;
					final int candidateY = CHEST_BASE_Y + dy;
					if (candidateX < 0 || candidateY < 0 || candidateX >= zone.getWidth() || candidateY >= zone.getHeight()) {
						continue;
					}

					if (isChestSpotFree(zone, chest, candidateX, candidateY)) {
						return new Point(candidateX, candidateY);
					}
				}
			}
		}
		return null;
	}

	private static boolean isChestSpotFree(final StendhalRPZone zone, final PrinceArmoryChest chest, final int x, final int y) {
		if (zone.collides(chest, x, y)) {
			return false;
		}
		for (PrinceArmoryChest existing : zone.getEntitiesAt(x, y, PrinceArmoryChest.class)) {
			if (Math.round(existing.getX()) == x && Math.round(existing.getY()) == y) {
				return false;
			}
		}
		return true;
	}

	private static boolean hasChest(final Player player) {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_warszawa_armory");
		for (Entity entity : zone.getEntitiesOfClass(PrinceArmoryChest.class)) {
			final PrinceArmoryChest chest = (PrinceArmoryChest) entity;
			if (chest.isOwnedBy(player)) {
				return true;
			}
		}
		return false;
	}

	private static void ensureChestForPlayer(final Player player) {
		if (!hasChest(player)) {
			prepareChest(player);
		}
	}

	private static void removeChest(final Player player) {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("int_warszawa_armory");

		for (Entity entity : zone.getEntitiesOfClass(PrinceArmoryChest.class)) {
			final PrinceArmoryChest chest = (PrinceArmoryChest) entity;
			if (chest.isOwnedBy(player)) {
				zone.remove(chest.getID());
			}
		}
	}

	private static class PrinceArmoryChest extends Chest {
		private final String ownerName;
		private Player attending;

		PrinceArmoryChest(final Player owner) {
			super();
			this.ownerName = owner.getName();
			this.attending = null;
			super.removeSlot("content");
			super.addSlot(new OwnerLockedChestSlot(this));
			setDescription("Na skrzyni widnieje pieczęć Księcia i imię " + ownerName + ".");
		}

		boolean isOwnedBy(final Player player) {
			return ownerName.equals(player.getName());
		}

		private boolean isAttendedBy(final Player player) {
			return attending == player;
		}

		@Override
		public void close() {
			attending = null;
			super.close();
		}

		@Override
		public boolean onUsed(final RPEntity user) {
			if (user instanceof Player) {
				final Player player = (Player) user;
				if (!isOwnedBy(player)) {
				        player.sendPrivateText("Pieczęć na skrzyni żarzy się, odpychając Twoje dłonie. To wyposażenie należy do " + ownerName + ".");
				        return false;
				}
				if (!isOpen()) {
				        attending = player;
				} else {
				        attending = null;
				}
			}
			return super.onUsed(user);
		}

		private class OwnerLockedChestSlot extends ChestSlot {
			OwnerLockedChestSlot(final PrinceArmoryChest chest) {
				super(chest);
			}

			@Override
			public boolean isReachableForTakingThingsOutOfBy(final Entity entity) {
				if (!(entity instanceof Player)) {
				        return false;
				}
				final Player player = (Player) entity;
				if (!isOwnedBy(player)) {
				        setErrorMessage("Pieczęć na skrzyni chroni zapasy przed niepowołanymi dłońmi.");
				        return false;
				}
				if (!isAttendedBy(player)) {
				        setErrorMessage("Skrzynia reaguje tylko na dotyk właściciela, który ją obecnie otworzył.");
				        return false;
				}
				return super.isReachableForTakingThingsOutOfBy(entity);
			}
		}
	}
	@Override
	public void addToWorld() {
		removeAllQuestChests();
		fillQuestInfo(
				"Odbicie Arsenału",
				"Książęca armia musi odbić swój arsenał z rąk buntowników.",
				false);
		prepareRequestingStep();
		prepareBringingStep();
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
			res.add("Uznałem, że ryzyko infiltracji arsenału jest zbyt wielkie.");
		} else if (questState.startsWith("start")) {
			res.add(player.getGenderVerb("Przyjąłem") + " misję odbicia wyposażenia od buntowników magnata Gonta.");
			res.add("Książę powierzył mi skrzynię oznaczoną moim imieniem — muszę wrócić z całym regimentem.");
		} else if (questState.startsWith("done")) {
			res.add(player.getGenderVerb("Oddałem") + " odzyskane uzbrojenie i odbudowałem morale armii.");
		}
		if(isRepeatable(player)){
			res.add("Książę ponownie szykuje ofensywę; wkrótce znów poprosi mnie o wsparcie.");
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
		if (!player.hasQuest(QUEST_SLOT)) {
			return false;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.startsWith("start")) {
			return false;
		}
		return !"rejected".equals(questState);
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(
				new NotCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "start")),
				new QuestStartedCondition(QUEST_SLOT),
				new TimePassedCondition(QUEST_SLOT, REQUIRED_MINUTES)).fire(player, null, null);
	}
}
