/***************************************************************************
 *                   Copyright (C) 2026 - PolanieOnLine                    *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 ***************************************************************************/
package games.stendhal.server.entity.npc.fsm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.condition.*;
import games.stendhal.server.entity.player.Player;

/** Only audited, read-only conditions can be evaluated outside a conversation. */
public final class QuestMarkerConditions {
	private static final Set<Class<?>> READ_ONLY = new HashSet<>(Arrays.asList(
			AdminCondition.class,
			AgeGreaterThanCondition.class,
			AgeLessThanCondition.class,
			AlwaysFalseCondition.class,
			AlwaysTrueCondition.class,
			AreaIsFullCondition.class,
			DarklightCondition.class,
			DaylightCondition.class,
			EmoteCondition.class,
			GreetingMatchesNameCondition.class,
			HasEarnedMoneyCondition.class,
			HasEarnedTotalMoneyCondition.class,
			HasSpentMoneyCondition.class,
			HasSpentTotalMoneyCondition.class,
			KarmaGreaterThanCondition.class,
			KarmaLessThanCondition.class,
			KilledCondition.class,
			KilledForQuestCondition.class,
			KilledInSumForQuestCondition.class,
			LevelGreaterThanCondition.class,
			LevelLessThanCondition.class,
			MinTotalCreaturesKilledCondition.class,
			NakedCondition.class,
			OutfitCompatibleWithClothesCondition.class,
			OutfitIsTemporaryCondition.class,
			PlayerCanEquipItemCondition.class,
			PlayerGotNumberOfItemsFromWellCondition.class,
			PlayerHasCompletedAchievementsCondition.class,
			PlayerHasCorrectGateKey.class,
			PlayerHasHarvestedNumberOfItemsCondition.class,
			PlayerHasItemdataItemWithHimCondition.class,
			PlayerHasItemEquippedInSlot.class,
			PlayerHasItemWithHimCondition.class,
			PlayerHasKilledNumberOfCreaturesCondition.class,
			PlayerHasPetOrSheepCondition.class,
			PlayerHasRecordedItemWithHimCondition.class,
			PlayerHasShieldEquippedCondition.class,
			PlayerHasStorableEntityCondition.class,
			PlayerInAreaCondition.class,
			PlayerIsAGoodBoyCondition.class,
			PlayerIsWearingOutfitCondition.class,
			PlayerLootedNumberOfItemsCondition.class,
			PlayerManaGreaterThanCondition.class,
			PlayerMinedNumberOfItemsCondition.class,
			PlayerNextToCondition.class,
			PlayerOwnsItemIncludingBankCondition.class,
			PlayerProducedNumberOfItemsCondition.class,
			PlayerStatLevelCondition.class,
			PlayerUpgradesNumberOfItemCondition.class,
			PlayerVisitedZonesCondition.class,
			PlayerVisitedZonesInRegionCondition.class,
			QuestActiveCondition.class,
			QuestCompletedCondition.class,
			QuestInStateCondition.class,
			QuestNotActiveCondition.class,
			QuestNotCompletedCondition.class,
			QuestNotInStateCondition.class,
			QuestNotStartedCondition.class,
			QuestRegisteredCondition.class,
			QuestSmallerThanCondition.class,
			QuestStartedCondition.class,
			QuestStateGreaterThanCondition.class,
			QuestStateStartsWithCondition.class,
			SentenceHasErrorCondition.class,
			StatLevelComparisonCondition.class,
			SystemPropertyCondition.class,
			TextHasNumberCondition.class,
			TextHasParameterCondition.class,
			TimePassedCondition.class,
			TimeReachedCondition.class,
			TriggerExactlyInListCondition.class,
			TriggerInListCondition.class,
			TriggerIsNPCNameForUnstartedQuestCondition.class,
			TriggerIsProducedItemOfClassCondition.class,
			TriggerMatchesQuestSlotCondition.class));

	private QuestMarkerConditions() {
	}

	/** null means unknown, not false; negating an unknown must remain unknown. */
	public static Boolean evaluate(final PreTransitionCondition condition, final Player player,
			final Sentence sentence, final Entity npc) {
		if (condition == null) {
			return Boolean.TRUE;
		}
		if (condition instanceof AndCondition) {
			boolean unknown = false;
			for (final ChatCondition part : ((AndCondition) condition).getConditions()) {
				final Boolean result = evaluate(part, player, sentence, npc);
				if (Boolean.FALSE.equals(result)) {
					return Boolean.FALSE;
				}
				unknown |= result == null;
			}
			return unknown ? null : Boolean.TRUE;
		}
		if (condition instanceof OrCondition) {
			boolean unknown = false;
			for (final ChatCondition part : ((OrCondition) condition).getConditions()) {
				final Boolean result = evaluate(part, player, sentence, npc);
				if (Boolean.TRUE.equals(result)) {
					return Boolean.TRUE;
				}
				unknown |= result == null;
			}
			return unknown ? null : Boolean.FALSE;
		}
		if (condition instanceof NotCondition) {
			final Boolean result = evaluate(((NotCondition) condition).getCondition(), player, sentence, npc);
			return result == null ? null : !result;
		}
		// In particular, KillsQuestSlotNeedUpdateCondition can write quest state.
		// Unknown scripted/lambda conditions require an explicit IQuest override.
		return READ_ONLY.contains(condition.getClass()) ? condition.fire(player, sentence, npc) : null;
	}
}
