/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** Charges one fixed stake and starts a Challenge Arena run. */
public final class StartChallengeArenaAction implements ChatAction {
	private final ChallengeArenaInfo arenaInfo;
	private final ChallengeArenaTier tier;

	public StartChallengeArenaAction(final ChallengeArenaInfo arenaInfo,
			final ChallengeArenaTier tier) {
		this.arenaInfo = arenaInfo;
		this.tier = tier;
	}

	@Override
	public void fire(final Player player, final Sentence sentence,
			final EventRaiser raiser) {
		if (player == null || arenaInfo == null || tier == null) {
			return;
		}

		if (!arenaInfo.hasOnlyPlayer(player)) {
			raiser.say("Arena musi być pusta zanim rozpoczniesz płatne wyzwanie.");
			return;
		}

		ChallengeArenaState previous = ChallengeArenaState.parse(
				player.getQuest(ChallengeArenaState.QUEST_SLOT));
		if (previous != null
				&& previous.getLifecycle() == ChallengeArenaState.Lifecycle.ACTIVE) {
			if (ChallengeArenaManager.isReservedBy(player.getName())) {
				raiser.say("Twoje poprzednie wyzwanie wciąż trwa.");
				return;
			}
			previous = previous.withLifecycle(ChallengeArenaState.Lifecycle.FAILED);
			player.setQuest(ChallengeArenaState.QUEST_SLOT, previous.serialize());
		}

		final String deathmatch = player.getQuest("deathmatch");
		if (deathmatch != null && deathmatch.length() > 0
				&& !deathmatch.startsWith("done")
				&& !"cancel".equals(deathmatch)) {
			raiser.say("Najpierw zakończ rozpoczęty Deathmatch.");
			return;
		}

		if (!MoneyUtils.hasEnoughMoney(player, tier.getStake())) {
			raiser.say("Nie masz wystarczająco dużo pieniędzy na tę stawkę.");
			return;
		}

		if (!ChallengeArenaManager.reserve(player.getName())) {
			raiser.say("Arena Wyzwań jest teraz zajęta.");
			return;
		}

		if (!MoneyUtils.removeMoney(player, tier.getStake())) {
			ChallengeArenaManager.release(player.getName());
			raiser.say("Nie udało się pobrać wpisowego.");
			return;
		}

		if (!arenaInfo.startSession(player, tier, raiser)) {
			MoneyUtils.giveMoney(player, tier.getStake());
			ChallengeArenaManager.release(player.getName());
			raiser.say("Nie udało się rozpocząć walki. Wpisowe zostało zwrócone.");
			return;
		}

		ChallengeArenaRewardService.recordEntry(player, tier);
		raiser.setAttending(null);
		raiser.setCurrentState(ConversationStates.IDLE);
	}
}
