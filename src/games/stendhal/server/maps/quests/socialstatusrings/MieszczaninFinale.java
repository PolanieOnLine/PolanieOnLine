/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

/** Attaches the completed social trial finale to Marianek. */
public final class MieszczaninFinale {
	private static final int REWARD_XP = 1000;
	private static final int REQUIRED_SILVER = 2;
	private static final int REQUIRED_COPPER = 1;
	private static final int REQUIRED_CHARCOAL = 3;
	private static final int FORGING_MINUTES = 60;
	private static final String FORGING_PREFIX = "forging;";

	private MieszczaninFinale() {
		// utility class
	}

	private static AndCondition approvedCondition() {
		return new AndCondition(
				new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
						PierscienMieszczanina.STATE_REPAIR),
				new QuestInStateCondition(MieszczaninRepairProgress.SLOT,
						MieszczaninRepairProgress.COMMUNITY_APPROVED));
	}

	private static AndCondition hasForgingMaterials() {
		return new AndCondition(
				new PlayerHasItemWithHimCondition("sztabka srebra", REQUIRED_SILVER),
				new PlayerHasItemWithHimCondition("sztabka miedzi", REQUIRED_COPPER),
				new PlayerHasItemWithHimCondition("węgiel drzewny", REQUIRED_CHARCOAL));
	}

	private static ChatCondition forgingCondition(final boolean ready) {
		return new ChatCondition() {
			@Override
			public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
				if (!player.hasQuest(PierscienMieszczanina.QUEST_SLOT)) {
					return false;
				}
				final String state = player.getQuest(PierscienMieszczanina.QUEST_SLOT);
				if (state == null || !state.startsWith(FORGING_PREFIX)) {
					return false;
				}
				return isForgingReady(state) == ready;
			}
		};
	}

	private static boolean isForgingReady(final String state) {
		try {
			final long started = Long.parseLong(state.substring(FORGING_PREFIX.length()));
			return System.currentTimeMillis() >= started + FORGING_MINUTES * TimeUtil.MILLISECONDS_IN_MINUTE;
		} catch (final NumberFormatException e) {
			// A damaged timestamp must not permanently trap a player after materials were taken.
			return true;
		}
	}

	private static long remainingForgingMillis(final Player player) {
		final String state = player.getQuest(PierscienMieszczanina.QUEST_SLOT);
		try {
			final long started = Long.parseLong(state.substring(FORGING_PREFIX.length()));
			return Math.max(0L,
					started + FORGING_MINUTES * TimeUtil.MILLISECONDS_IN_MINUTE - System.currentTimeMillis());
		} catch (final RuntimeException e) {
			return 0L;
		}
	}

	private static ChatAction startForgingAction() {
		return new MultipleActions(
				new DropItemAction("sztabka srebra", REQUIRED_SILVER),
				new DropItemAction("sztabka miedzi", REQUIRED_COPPER),
				new DropItemAction("węgiel drzewny", REQUIRED_CHARCOAL),
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						player.setQuest(PierscienMieszczanina.QUEST_SLOT,
								FORGING_PREFIX + System.currentTimeMillis());
					}
				});
	}

	private static ChatAction finishForgingAction() {
		return new MultipleActions(
				new EquipItemAction("pierścień mieszczanina", 1, true),
				new IncreaseXPAction(REWARD_XP),
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						MieszczaninRepairProgress.clear(player);
						MieszczaninHideoutProgress.clear(player);
					}
				},
				new SetQuestAction(PierscienMieszczanina.QUEST_SLOT,
						PierscienMieszczanina.STATE_DONE));
	}

	/** Attach final quest responses to the already-created Marianek NPC. */
	public static void attach(final SpeakerNPC marianek) {
		marianek.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				approvedCondition(),
				ConversationStates.INFORMATION_7,
				"Witomir i Dobrawa już mi opowiedzieli, co wydarzyło się na trakcie. Dobrawa nie znała żadnej próby, a mimo to napisała, że można na tobie #polegać.",
				null);

		marianek.add(ConversationStates.INFORMATION_7,
				"polegać",
				approvedCondition(),
				ConversationStates.INFORMATION_8,
				"I tego potrzebowałem się dowiedzieć. Znak mieszczanina ci się należy, lecz pierścienia nie wykuwa się z powietrza. Jeśli ma przetrwać drogę i lata, trzeba dać mu dobre #materiały.",
				null);

		marianek.add(ConversationStates.INFORMATION_8,
				"materiały",
				approvedCondition(),
				ConversationStates.QUEST_ITEM_QUESTION,
				"Przynieś 2 sztabki srebra, 1 sztabkę miedzi i 3 kawałki węgla drzewnego. Srebro da obręczy blask, miedź zwiąże metal, a węgiel nakarmi ogień. Masz wszystko i oddajesz pod mój młot?",
				null);

		marianek.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(approvedCondition(), hasForgingMaterials()),
				ConversationStates.IDLE,
				"Dobrze. Rozpalę palenisko, oczyszczę srebro i domknę obręcz miedzią. Ogień i metal mają własny rytm; wróć mniej więcej za godzinę. Wtedy zapytaj o #pierścień.",
				startForgingAction());

		marianek.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.YES_MESSAGES,
				new AndCondition(approvedCondition(), new NotCondition(hasForgingMaterials())),
				ConversationStates.ATTENDING,
				"Jeszcze nie. Brakuje któregoś z darów dla kuźni: 2 sztabek srebra, 1 sztabki miedzi albo 3 kawałków węgla drzewnego. Z pustego paleniska nawet Swaróg niczego nie wyciągnie.",
				null);

		marianek.add(ConversationStates.QUEST_ITEM_QUESTION,
				ConversationPhrases.NO_MESSAGES,
				approvedCondition(),
				ConversationStates.ATTENDING,
				"Nie będę kuł na siłę. Zbierz metal i węgiel, a gdy będziesz gotowy, wróć do #materiałów.",
				null);

		marianek.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				forgingCondition(false),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final long remaining = remainingForgingMillis(player);
						raiser.say("Jeszcze nie. Obręcz jest w ogniu, a pośpiech zostawia pęknięcia. Daj mi około "
								+ TimeUtil.approxTimeUntil((int) (remaining / 1000L))
								+ ". Potem wróć po #pierścień.");
					}
				});

		marianek.add(ConversationStates.ATTENDING,
				"pierścień",
				forgingCondition(false),
				ConversationStates.ATTENDING,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						final long remaining = remainingForgingMillis(player);
						raiser.say("Jeszcze nie. Młot zrobił swoje, ale metal musi dojść w żarze. Wróć za około "
								+ TimeUtil.approxTimeUntil((int) (remaining / 1000L)) + ".");
					}
				});

		marianek.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				forgingCondition(true),
				ConversationStates.INFORMATION_9,
				"Ogień przygasł, a obręcz już trzyma kształt. Twój #pierścień jest gotowy.",
				null);

		marianek.add(ConversationStates.ATTENDING,
				"pierścień",
				forgingCondition(true),
				ConversationStates.ATTENDING,
				"Gotowe. Srebro pamięta ogień, miedź trzyma obręcz, a znak przypomina o słowie Dobrawy. Noś pierścień mieszczanina tak, by ludzie dalej mogli na tobie polegać.",
				finishForgingAction());

		marianek.add(ConversationStates.INFORMATION_9,
				"pierścień",
				forgingCondition(true),
				ConversationStates.ATTENDING,
				"Gotowe. Srebro pamięta ogień, miedź trzyma obręcz, a znak przypomina o słowie Dobrawy. Noś pierścień mieszczanina tak, by ludzie dalej mogli na tobie polegać.",
				finishForgingAction());

		marianek.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestInStateCondition(PierscienMieszczanina.QUEST_SLOT,
								PierscienMieszczanina.STATE_REPAIR),
						new NotCondition(new QuestInStateCondition(MieszczaninRepairProgress.SLOT,
								MieszczaninRepairProgress.COMMUNITY_APPROVED))),
				ConversationStates.ATTENDING,
				"Jeszcze nie pora na pierścień. Dokończ przejazd, pozwól Stachowi sprawdzić naprawę i porozmawiaj z Dobrawą.",
				null);
	}
}
