/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import marauroa.common.game.IRPZone;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.item.HuntingRewardChest;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.maps.quests.hunting.HuntingDifficulty;
import games.stendhal.server.maps.quests.hunting.HuntingRewards;


import games.stendhal.common.Level;
import games.stendhal.common.QuestMarker;
import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import games.stendhal.server.util.KillsForQuestCounter;

public class Hunting extends AbstractQuest {
	private static final String QUEST_SLOT = "hunting";
	private final SpeakerNPC npc = npcs.get("Janisław");


	private static final String OFFER_SLOT = "hunting_offer";
	private static final ChatCondition ACTIVE = (player, sentence, entity) ->
			player.hasQuest(QUEST_SLOT) && player.getQuest(QUEST_SLOT, 0).split(",", -1).length == 5;

	/** Only actual public respawns: disabled seasonal content and private arenas
	 * cannot supply a target merely because their creature definition exists. */
	static List<Creature> availableCreatures() {
		Set<String> names = new HashSet<>();
		for (IRPZone rawZone : SingletonRepository.getRPWorld()) {
			StendhalRPZone zone = (StendhalRPZone) rawZone;
			if (!zone.isPublicAccessible()) { continue; }
			for (CreatureRespawnPoint point : zone.getRespawnPointList()) {
				Creature creature = point.getPrototypeCreature();
				if (!creature.isAbnormal() && !creature.isBoss()) { names.add(creature.getName()); }
			}
		}
		List<Creature> result = new ArrayList<>();
		for (String name : names) {
			Creature creature = SingletonRepository.getEntityManager().getCreature(name);
			if (creature != null && !creature.isAbnormal() && !creature.isBoss()) { result.add(creature); }
		}
		return result;
	}

	private void offer(Player player, EventRaiser raiser, HuntingDifficulty difficulty) {
		player.setQuest(OFFER_SLOT, null);
		if (player.isBadBoy()) {
			raiser.say("Z twej ręki zginął dzielny rycerz! Precz mi z oczu!");
			raiser.setCurrentState(ConversationStates.ATTENDING);
			return;
		}
		List<Creature> candidates = new ArrayList<>();
		List<Creature> available = availableCreatures();
		for (Creature creature : available) {
			if (difficulty.accepts(player.getLevel(), creature.getLevel())) { candidates.add(creature); }
		}
		if (candidates.isEmpty()) {
			raiser.say("Nie mam teraz dostępnych przeciwników na takie polowanie dla Twojego poziomu. Wybierz #łatwe, #średnie lub #trudne.");
			raiser.setCurrentState(ConversationStates.ATTENDING);
			return;
		}
		Creature creature = candidates.get(Rand.rand(candidates.size()));
		int count = 20 + Rand.rand(11);
		int bonus = HuntingRewards.rewardBasisPoints(difficulty, count, creature,
				HuntingRewards.referenceStrength(player.getLevel(), available));
		player.setQuest(OFFER_SLOT, difficulty.name() + ";" + creature.getName() + ";" + count + ";" + player.getLevel() + ";" + bonus);
		raiser.say("Polowanie " + difficulty.getLabel() + ": " + creature.getName() + " (poziom "
				+ creature.getLevel() + "), " + count + " sztuk. Oprócz dotychczasowych nagród otrzymasz "
				+ HuntingRewards.percentageLabel(bonus) + " PD pełnego poziomu ataku i obrony oraz Skrzynię Myśliwego. "
				+ (difficulty == HuntingDifficulty.EASY ? "Skrzynia zawiera zapasy."
				: difficulty == HuntingDifficulty.MEDIUM ? "Skrzynia zawiera wyposażenie: 25% szans na rzadkie."
				: "Skrzynia zawiera co najmniej rzadkie wyposażenie: 10% szans na epickie.")
				+ " Podejmujesz wyzwanie? #tak / #nie");
	}

	private void acceptOffer(Player player, EventRaiser raiser) {
		String offer = player.getQuest(OFFER_SLOT);
		player.setQuest(OFFER_SLOT, null);
		if (offer == null || ACTIVE.fire(player, null, null) || player.isBadBoy()) {
			raiser.say("Poproś o #zadanie i wybierz trudność.");
			return;
		}
		String[] parts = offer.split(";", -1);
		try {
			if (parts.length != 5) { throw new IllegalArgumentException(); }
			HuntingDifficulty difficulty = HuntingDifficulty.valueOf(parts[0]);
			int count = Integer.parseInt(parts[2]);
			int bonus = Integer.parseInt(parts[4]);
			if (count < 20 || count > 30 || bonus < 1 || bonus > 10000 || Integer.parseInt(parts[3]) != player.getLevel()) {
				throw new IllegalArgumentException();
			}
			boolean available = false;
			for (Creature creature : availableCreatures()) {
				if (creature.getName().equals(parts[1]) && difficulty.accepts(player.getLevel(), creature.getLevel())) {
					available = true;
					break;
				}
			}
			if (!available) { throw new IllegalArgumentException(); }
			int repetitions = player.hasQuest(QUEST_SLOT) ? player.getNumberOfRepetitions(QUEST_SLOT, 2) : 0;
			player.setQuest(QUEST_SLOT, parts[1] + ",0," + count + "," + player.getSoloKill(parts[1])
					+ "," + player.getSharedKill(parts[1]) + ";" + System.currentTimeMillis() + ";"
					+ repetitions + ";" + difficulty.name() + ";" + bonus);
			raiser.say("Upoluj " + count + " sztuk: " + parts[1] + ". Powiedz #załatwione, gdy skończysz. "
					+ "Zmiana trudności wymaga wcześniejszej rezygnacji: #rezygnuję.");
		} catch (IllegalArgumentException e) {
			raiser.say("Ta oferta nie jest już dostępna. Poproś o nowe #zadanie.");
		}
	}

	private String getCreatureToKillFromPlayer(Player player) {
		String actualQuestSlot = player.getQuest(QUEST_SLOT, 0);
		String[] split = actualQuestSlot.split(",");
		if (split.length > 1) {
			// only return object if the slot was in the format expected (i.e. not done;timestamp;count etc)
			return split[0];
		}
		return null;
	}

	private String getCountCreatureToKillFromPlayer(Player player) {
		String actualQuestSlot = player.getQuest(QUEST_SLOT, 0);
		String[] split = actualQuestSlot.split(",");
		if (split.length > 3) {
			// only return object if the slot was in the format expected (i.e. not done;timestamp;count etc)
			return split[2];
		}
		return null;
	}

	/**
	 * player said "quest"
	 */

	private void step_1() {
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, ACTIVE,
				ConversationStates.ATTENDING, null, (player, sentence, raiser) ->
					raiser.say("Masz aktywne polowanie: " + getCreatureToKillFromPlayer(player) + ", "
							+ getCountCreatureToKillFromPlayer(player) + " sztuk. Powiedz #załatwione lub #rezygnuję."));
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES, new NotCondition(ACTIVE),
				ConversationStates.ATTENDING, "Wybierz polowanie: #łatwe, #średnie albo #trudne. "
				+ "Im trudniejsze polowanie, tym większa premia do PD ataku i obrony za pokonanie przeciwników. "
				+ "Nagroda zależy też od ich liczby i siły. Przed przyjęciem zadania poznasz dokładną premię i zawartość skrzyni.", null);
		for (HuntingDifficulty difficulty : HuntingDifficulty.values()) {
			List<String> words = difficulty == HuntingDifficulty.EASY ? Arrays.asList("łatwe", "latwe", "easy")
					: difficulty == HuntingDifficulty.MEDIUM ? Arrays.asList("średnie", "srednie", "medium")
					: Arrays.asList("trudne", "hard");
			npc.add(ConversationStates.ATTENDING, words, new NotCondition(ACTIVE),
					ConversationStates.QUESTION_1, null, (player, sentence, raiser) -> offer(player, raiser, difficulty));
			npc.add(ConversationStates.ATTENDING, words, ACTIVE, ConversationStates.ATTENDING,
					"Najpierw zakończ obecne polowanie albo powiedz #rezygnuję. Przerwane zadanie nie daje nagrody.", null);
		}
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, null, (player, sentence, raiser) -> acceptOffer(player, raiser));
		npc.add(ConversationStates.QUESTION_1, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Nie przyjmujesz zadania. Możesz wybrać inną trudność.",
				(player, sentence, raiser) -> player.setQuest(OFFER_SLOT, null));
		npc.add(ConversationStates.ATTENDING, Arrays.asList("rezygnuję", "rezygnuje", "rezygnuj", "cancel"),
				ACTIVE, ConversationStates.QUESTION_2,
				"Chcesz przerwać polowanie bez nagrody? Dotychczasowy postęp tego zadania przepadnie. #tak / #nie", null);
		npc.add(ConversationStates.ATTENDING, Arrays.asList("rezygnuję", "rezygnuje", "rezygnuj", "cancel"),
				new NotCondition(ACTIVE), ConversationStates.ATTENDING, "Nie masz aktywnego polowania.", null);
		npc.add(ConversationStates.QUESTION_2, ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING, null, (player, sentence, raiser) -> {
					if (ACTIVE.fire(player, null, null)) {
						player.setQuest(QUEST_SLOT, 0, "cancelled");
						player.setQuest(OFFER_SLOT, null);
					}
					raiser.say("Polowanie przerwane bez nagrody. Liczba ukończonych polowań jest zachowana. Wybierz #łatwe, #średnie lub #trudne.");
				});
		npc.add(ConversationStates.QUESTION_2, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING, "Kontynuuj polowanie. Powiedz #załatwione, kiedy skończysz.", null);
	}

	private void step_2() {
		// kill the monster
	}

	/**
	 * player said "done"
	 */
	private void step_3() {
		// player never asked for this quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				(player, sentence, entity) -> !player.hasQuest(QUEST_SLOT) || "cancelled".equals(player.getQuest(QUEST_SLOT, 0)) || "rejected".equals(player.getQuest(QUEST_SLOT, 0)),
				ConversationStates.ATTENDING,
				"Obawiam się, że jeszcze nie dałem Tobie #zadania.",
				null);

		// player already completed this quest
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Już ukończyłeś ostatnie zadanie, które Tobie dałem.",
				null);

		// player didn't killed creature
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(
						ACTIVE,
						new NotCondition(
						        new KilledForQuestCondition(QUEST_SLOT, 0))),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(Player player, Sentence sentence, EventRaiser npc) {
							final String questKill = player.getQuest(QUEST_SLOT, 0).split(",")[0];
							final String questCountKill = player.getQuest(QUEST_SLOT, 0).split(",")[2];
							npc.say("Jeszcze nie upolowałeś " + questCountKill + " " + questKill
									+ ". Idź i zrób to oraz powiedz #załatwione, gdy skończysz.");
					}
				});

		final List<ChatAction> rewards = new LinkedList<ChatAction>();
		// Resolve legacy counts while the kill record still exists. New task
		// rewards are already frozen, so neither levelling nor event changes alter them.
		rewards.add((player, sentence, raiser) -> player.setQuest(QUEST_SLOT, 4,
				Integer.toString(HuntingRewards.questBasisPoints(player.getQuest(QUEST_SLOT)))));
		rewards.add(new IncrementQuestAction(QUEST_SLOT, 2, 1));
		rewards.add(new SetQuestAction(QUEST_SLOT, 0, "done"));
		rewards.add((player, sentence, raiser) -> {
			HuntingDifficulty difficulty = HuntingDifficulty.fromQuest(player.getQuest(QUEST_SLOT));
			int bonus = HuntingRewards.questBasisPoints(player.getQuest(QUEST_SLOT));
			int atkBonus = HuntingRewards.skillXPBonus(player.getAtkXP(), bonus);
			int defBonus = HuntingRewards.skillXPBonus(player.getDefXP(), bonus);
			Item chest = SingletonRepository.getEntityManager().getItem(HuntingRewardChest.NAME, ItemCreationContext.quest());
			chest.setBoundTo(player.getName());
			chest.setItemData(HuntingRewardChest.createRewardData(difficulty, player.getLevel()));
			player.addAtkXP(atkBonus);
			player.addDefXP(defBonus);
			player.equipOrPutOnGround(chest);
			player.sendPrivateText("Nagroda polowania: +" + atkBonus + " PD ataku, +" + defBonus
					+ " PD obrony i Skrzynia Myśliwego (" + difficulty.getLabel() + ").");
		});
		rewards.add(new ChatAction() {
			@Override
			public void fire(Player player, Sentence sentence, EventRaiser npc) {
				final int start = Level.getXP(player.getLevel());
				final int next = Level.getXP(player.getLevel() + 1);

				if (player.getNumberOfRepetitions(QUEST_SLOT, 2) <= 10) {
					int reward = (int) ((next - start) / 6);
					if (player.getLevel() >= Level.maxLevel()) {
						reward = 0;
						// no reward so give a lot karma instead
						player.addKarma(60.0);
					}
					player.addXP(reward);
					player.addKarma(5);

					if (player.getNumberOfRepetitions(QUEST_SLOT, 2) == 10) {
						final Item specialRing = SingletonRepository.getEntityManager().getItem(
								"pierścień powrotu", ItemCreationContext.quest());
						specialRing.setBoundTo(player.getName());
						player.equipOrPutOnGround(specialRing);
						player.notifyWorldAboutChanges();
					}

					final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
					if (player.getLevel() < 10) {
						money.setQuantity(Rand.roll1D100());
					} else if (player.getLevel() >= 10 && player.getLevel() <= 50) {
						money.setQuantity(Rand.roll1D1000());
					} else {
						money.setQuantity(Rand.roll1D2000());
					}
					player.equipOrPutOnGround(money);
					player.notifyWorldAboutChanges();
				} else if (player.getNumberOfRepetitions(QUEST_SLOT, 2) > 10 && player.getNumberOfRepetitions(QUEST_SLOT, 2) <= 34) {
					int reward = (int) ((next - start) / 5);
					if (player.getLevel() >= Level.maxLevel()) {
						reward = 0;
						// no reward so give a lot karma instead
						player.addKarma(90.0);
					}
					player.addXP(reward);
					player.addKarma(10);

					final StackableItem goldenbar = (StackableItem) SingletonRepository.getEntityManager().getItem("sztabka złota");
					if (player.getLevel() < 50) {
						goldenbar.setQuantity(Rand.roll1D10());
					} else {
						goldenbar.setQuantity(Rand.roll1D20());
					}
					player.equipOrPutOnGround(goldenbar);
					player.notifyWorldAboutChanges();
				} else if (player.getNumberOfRepetitions(QUEST_SLOT, 2) > 34) {
					int reward = (int) ((next - start) / 4);
					if (player.getLevel() >= Level.maxLevel()) {
						reward = 0;
						// no reward so give a lot karma instead
						player.addKarma(110.0);
					}
					player.addXP(reward);
					player.addKarma(15);

					final StackableItem goldenbar = (StackableItem) SingletonRepository.getEntityManager().getItem("sztabka złota");
					final StackableItem mithrilbar = (StackableItem) SingletonRepository.getEntityManager().getItem("sztabka mithrilu");
					goldenbar.setQuantity(Rand.roll1D20());
					if (player.getLevel() < 90) {
						mithrilbar.setQuantity(Rand.roll1D3());
					} else {
						mithrilbar.setQuantity(Rand.roll1D6());
					}
					player.equipOrPutOnGround(goldenbar);
					player.equipOrPutOnGround(mithrilbar);
					player.notifyWorldAboutChanges();
				}
			}
		});

		// player killed creature
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.FINISH_MESSAGES,
				new AndCondition(
						ACTIVE,
				        new KilledForQuestCondition(QUEST_SLOT, 0)),
				ConversationStates.ATTENDING,
				"Gratuluje! Zasłużyłeś na odpowiednią nagrodę!",
				new MultipleActions(rewards));
	}

	private String howManyWereKilled(final Player player, final String questState) {
		KillsForQuestCounter killsCounter = new KillsForQuestCounter(questState);
		final String creatureToKill = getCreatureToKillFromPlayer(player);
		int remaining = killsCounter.remainingKills(player, creatureToKill);
		return "Wciąż muszę zabić " + Grammar.quantityplnoun(remaining, creatureToKill) + ".";
	}

	private List<String> howManyWereKilledFormatted(final Player player, final String questState) {
		KillsForQuestCounter killsCounter = new KillsForQuestCounter(questState);
		final String creatureToKill = getCreatureToKillFromPlayer(player);
		final String creatureCountToKill = getCountCreatureToKillFromPlayer(player);
		int killed = Integer.parseInt(creatureCountToKill) - killsCounter.remainingKills(player, creatureToKill);

		String count;
		if (killed == 1) {
			count = " sztukę";
		} else if (killed >= 2 && killed <= 4) {
			count = " sztuki";
		} else {
			count = " sztuk";
		}

		List<String> entries = new ArrayList<>();
		entries.add(player.getGenderVerb("Upolowałem") + ": <tally>" + killed + count + "</tally>");
		return entries;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Polowanie",
				"Janisław proponuje wszystkim młodym wojom upolowanie stworzeń w zamian za nagrodę.",
				true);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public List<String> getHistory(final Player player) {
		return getHistory(player, false);
	}

	@Override
	public List<String> getFormattedHistory(final Player player) {
		return getHistory(player, true);
	}

	private List<String> getHistory(final Player player, boolean formatted) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(player.getGenderVerb("Spotkałem") + " Janisława w domku Zakopanego.");
		final String questStateFull = player.getQuest(QUEST_SLOT);
		final String[] parts = questStateFull.split(";");
		final String questState = parts[0];

		if ("cancelled".equals(questState)) {
			res.add("Przerwane polowanie nie dało nagrody. Mogę wybrać nowe zadanie.");
			return res;
		}
		if ("rejected".equals(questStateFull)) {
			res.add("Nie chcę polować na zwierzyny i inne stworzenia.");
			return res;
		}

		res.add("Chcę spróbować zapolować na stworzenia.");
		if (ACTIVE.fire(player, null, null)) {
			res.add("Trudność: " + HuntingDifficulty.fromQuest(questStateFull).getLabel() + ".");
			res.add("Premia za ukończenie: " + HuntingRewards.percentageLabel(HuntingRewards.questBasisPoints(questStateFull))
					+ " PD pełnego poziomu ataku i obrony.");
			final boolean questDone = new KilledForQuestCondition(QUEST_SLOT, 0).fire(player, null, null);
			final String creatureToKill = getCreatureToKillFromPlayer(player);
			final String creatureCountToKill = getCountCreatureToKillFromPlayer(player);
			if (!questDone) {
				res.add(player.getGenderVerb("Zostałem") + " " + player.getGenderVerb("poproszony") + " o upolowanie " + creatureToKill + " w ilości " + creatureCountToKill
						+ ". Jeszcze mi się nie udało.");
			} else {
				res.add(player.getGenderVerb("Upolowałem") + " wszystkie " + creatureToKill + ".");
			}

			if (formatted) {
				res.addAll(howManyWereKilledFormatted(player, questState));
			} else {
				res.add(howManyWereKilled(player, questState));
			}
		}
		if (player.isQuestCompleted(QUEST_SLOT)) {
			res.add(player.getGenderVerb("Zabiłem") + " ostatniego potwora o którego prosił mnie Janisław. Może znowu będzie miał dla mnie wyzwanie.");
		}
		final int repetitions = player.getNumberOfRepetitions(getSlotName(), 2);
		if (repetitions > 0) {
			res.add(player.getGenderVerb("Pomogłem") + " Janisławowi "
					+ Grammar.quantityplnounCreature(repetitions, "raz") + " do tej pory.");
		}
		return res;
	}

	@Override
	public String getName() {
		return "Polowanie";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public QuestMarker getNPCQuestMarker(final Player player, final SpeakerNPC targetNPC) {
		if (!"Janisław".equals(targetNPC.getName()) || player.isBadBoy()) { return QuestMarker.NONE; }
		if (ACTIVE.fire(player, null, null)) {
			return new KilledForQuestCondition(QUEST_SLOT, 0).fire(player, null, null)
					? QuestMarker.READY : QuestMarker.IN_PROGRESS;
		}
		return player.hasQuest(QUEST_SLOT) ? QuestMarker.REPEATABLE : QuestMarker.AVAILABLE;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return player.hasQuest(QUEST_SLOT) && !ACTIVE.fire(player, null, null);
	}
}
