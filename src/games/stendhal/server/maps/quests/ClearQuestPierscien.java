/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.condition.QuestStartedCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Paid quest reset service offered by eFuR.
 *
 * A reset is destructive, so choosing a quest only prepares an offer. The
 * player must explicitly say "potwierdzam" before any progress or resources
 * are removed.
 */
public class ClearQuestPierscien extends AbstractQuest {
	private static final String QUEST_SLOT = "clear_questy_pierscieni";
	private static final int MIN_LEVEL = 150;
	private static final String CONFIRM_PREFIX = "confirm:";
	private static final String GOLD = "sztabka złota";

	private final SpeakerNPC npc = npcs.get("eFuR");

	private static final List<ResetOption> OPTIONS = Arrays.asList(
			new ResetOption("mieszczanin", "pierscien_mieszczanina", "pierścień mieszczanina",
					250000, 30, "mieszczanin", "mieszczanina"),
			new ResetOption("rycerz", "pierscien_rycerza", "pierścień rycerza",
					350000, 50, "rycerz", "rycerza"),
			new ResetOption("baron", "pierscien_barona", "pierścień barona",
					450000, 70, "baron", "barona"),
			new ResetOption("magnat", "pierscien_magnata", "pierścień magnata",
					600000, 100, "magnat", "magnata"),
			new ResetOption("plaszcz", "mithril_cloak", "płaszcz z mithrilu",
					2000000, 0, "płaszcz", "plaszcz", "płaszcza", "plaszcza"),
			new ResetOption("tarcza", "mithrilshield_quest", "tarczę z mithrilu",
					1500000, 0, "tarcza", "tarczę", "tarcze", "tarczy"));

	private static final class ResetOption {
		private final String key;
		private final String questSlot;
		private final String displayName;
		private final int money;
		private final int gold;
		private final List<String> aliases;

		ResetOption(final String key, final String questSlot, final String displayName,
				final int money, final int gold, final String... aliases) {
			this.key = key;
			this.questSlot = questSlot;
			this.displayName = displayName;
			this.money = money;
			this.gold = gold;
			this.aliases = Collections.unmodifiableList(Arrays.asList(aliases));
		}
	}

	private void addIntroduction() {
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("zadanie", "anulowanie", "anuluj", "reset", "resetuj"),
				null,
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (!canUseService(player, raiser)) {
							return;
						}
						final ResetOption selected = getSelectedOption(player);
						if (selected != null) {
							raiser.say(buildConfirmationText(selected));
						} else {
							raiser.say("Mogę całkowicie usunąć postęp jednego z tych zadań: #mieszczanin, #rycerz, #baron, #magnat, #płaszcz albo #tarcza. Po resecie zadanie będzie wyglądało tak, jakbyś nigdy go nie rozpoczął. Najpierw wybierz, które zadanie mam przygotować do anulowania.");
						}
					}
				});
	}

	private void addSelections() {
		for (final ResetOption option : OPTIONS) {
			npc.add(ConversationStates.ATTENDING,
					option.aliases,
					null,
					ConversationStates.ATTENDING,
					null,
					new ChatAction() {
						@Override
						public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
							if (!canUseService(player, raiser)) {
								return;
							}
							if (!player.hasQuest(option.questSlot)) {
								player.setQuest(QUEST_SLOT, null);
								raiser.say("Nie masz zapisanego postępu zadania na " + option.displayName + ", więc nie mam czego anulować.");
								return;
							}
							player.setQuest(QUEST_SLOT, CONFIRM_PREFIX + option.key);
							raiser.say(buildConfirmationText(option));
						}
					});
		}
	}

	private void addConfirmation() {
		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("potwierdzam", "potwierdź", "potwierdz"),
				new QuestStartedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final ResetOption option = getSelectedOption(player);
						if (option == null) {
							player.setQuest(QUEST_SLOT, null);
							raiser.say("Nie mam przygotowanego żadnego resetu. Powiedz #zadanie i wybierz, co chcesz anulować.");
							return;
						}
						if (!player.hasQuest(option.questSlot)) {
							player.setQuest(QUEST_SLOT, null);
							raiser.say("To zadanie nie ma już zapisanego postępu. Niczego nie pobieram.");
							return;
						}
						if (!hasPayment(player, option)) {
							raiser.say("Nie masz pełnej zapłaty. " + paymentText(option)
									+ " Nie pobiorę części należności. Gdy zbierzesz wszystko, ponownie powiedz #potwierdzam albo #rezygnuję.");
							return;
						}

						player.drop("money", option.money);
						if (option.gold > 0) {
							player.drop(GOLD, option.gold);
						}
						player.setQuest(option.questSlot, null);
						player.setQuest(QUEST_SLOT, null);
						raiser.say("Gotowe. Usunąłem cały zapis zadania na " + option.displayName
								+ ". Możesz rozpocząć je od początku. Za anulowanie nie otrzymujesz doświadczenia ani dodatkowej nagrody.");
					}
				});

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.NO_MESSAGES,
				new HasSelectedResetCondition(),
				ConversationStates.ATTENDING,
				"Dobrze. Niczego nie zmieniam i niczego nie pobieram.",
				new ClearSelectionAction());

		npc.add(ConversationStates.ATTENDING,
				Arrays.asList("rezygnuję", "rezygnuje", "zrezygnuj", "anuluj wybór", "anuluj wybor"),
				new HasSelectedResetCondition(),
				ConversationStates.ATTENDING,
				"Dobrze. Niczego nie zmieniam i niczego nie pobieram.",
				new ClearSelectionAction());
	}

	private boolean canUseService(final Player player, final EventRaiser raiser) {
		if (player.isBadBoy()) {
			raiser.say("Najpierw pozbądź się piętna czaszki. Nie będę mieszał w twoich zadaniach, dopóki ciąży na tobie taki znak.");
			return false;
		}
		if (player.getLevel() < MIN_LEVEL) {
			raiser.say("Tak poważne zmiany robię dopiero wojownikom od poziomu " + MIN_LEVEL + ".");
			return false;
		}
		return true;
	}

	private static boolean hasPayment(final Player player, final ResetOption option) {
		if (player.getNumberOfEquipped("money") < option.money) {
			return false;
		}
		return option.gold == 0 || player.getNumberOfEquipped(GOLD) >= option.gold;
	}

	private static String paymentText(final ResetOption option) {
		final StringBuilder text = new StringBuilder("Cena to ")
				.append(Grammar.quantityplnoun(option.money, "money"));
		if (option.gold > 0) {
			text.append(" oraz ").append(Grammar.quantityplnoun(option.gold, GOLD));
		}
		text.append('.');
		return text.toString();
	}

	private static String buildConfirmationText(final ResetOption option) {
		return "Wybrałeś anulowanie zadania na " + option.displayName + ". " + paymentText(option)
				+ " To całkowicie usunie zapis tego zadania i obecny postęp przepadnie. Jeśli właśnie tego chcesz, powiedz #potwierdzam. Jeśli nie, powiedz #rezygnuję.";
	}

	private static ResetOption getSelectedOption(final Player player) {
		if (!player.hasQuest(QUEST_SLOT)) {
			return null;
		}
		String state = player.getQuest(QUEST_SLOT);
		if (state == null) {
			return null;
		}
		if (state.startsWith(CONFIRM_PREFIX)) {
			state = state.substring(CONFIRM_PREFIX.length());
		}
		// Old saves used just the option key. Treat them as a pending selection,
		// but require the new explicit confirmation before doing anything.
		for (final ResetOption option : OPTIONS) {
			if (option.key.equals(state)) {
				return option;
			}
		}
		return null;
	}

	private static final class HasSelectedResetCondition implements ChatCondition {
		@Override
		public boolean fire(final Player player, final Sentence sentence, final games.stendhal.server.entity.Entity entity) {
			return getSelectedOption(player) != null;
		}
	}

	private static final class ClearSelectionAction implements ChatAction {
		@Override
		public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
			player.setQuest(QUEST_SLOT, null);
		}
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Anulowanie Zadań",
				"eFuR może za opłatą całkowicie usunąć zapis wybranych zadań. Każda operacja wymaga osobnego, wyraźnego potwierdzenia.",
				true);
		addIntroduction();
		addSelections();
		addConfirmation();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> result = new ArrayList<String>();
		final ResetOption option = getSelectedOption(player);
		if (option == null) {
			return result;
		}
		result.add("eFuR przygotował anulowanie zadania na " + option.displayName + ".");
		result.add(paymentText(option));
		result.add("Anulowanie całkowicie usunie zapis tego zadania. Muszę świadomie powiedzieć mu „potwierdzam”, aby wykonał operację.");
		return result;
	}

	@Override
	public String getName() {
		return "Anulowanie Zadań";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
}
