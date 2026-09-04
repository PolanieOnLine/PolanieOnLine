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

/** Charges one fixed stake, moves the player to the arena and starts a run. */
public final class StartChallengeArenaAction implements ChatAction {
	private final ChallengeArenaTier tier;

	public StartChallengeArenaAction(final ChallengeArenaTier tier) {
		this.tier = tier;
	}

	@Override
	public void fire(final Player player, final Sentence sentence,
			final EventRaiser raiser) {
		if (player == null || tier == null) {
			return;
		}

		final ChallengeArenaInfo arenaInfo = ChallengeArenaManager.getArenaInfo();
		if (arenaInfo == null) {
			raiser.say("Arena nie jest teraz dostępna.");
			return;
		}

		if (ChallengeArenaManager.isReserved() || !arenaInfo.isEmpty()) {
			raiser.say("Arena Wyzwań jest teraz zajęta.");
			return;
		}

		ChallengeArenaState previous = ChallengeArenaState.parse(
				player.getQuest(ChallengeArenaState.QUEST_SLOT));
		if (previous != null
				&& previous.getLifecycle() == ChallengeArenaState.Lifecycle.ACTIVE) {
			previous = previous.withLifecycle(ChallengeArenaState.Lifecycle.FAILED);
			player.setQuest(ChallengeArenaState.QUEST_SLOT, previous.serialize());
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

		if (!arenaInfo.teleportIntoArena(player) || !arenaInfo.hasOnlyPlayer(player)) {
			MoneyUtils.giveMoney(player, tier.getStake());
			ChallengeArenaManager.release(player.getName());
			if (arenaInfo.isInArena(player)) {
				arenaInfo.teleportToLobby(player);
			}
			raiser.say("Nie udało się wejść na arenę. Wpisowe zostało zwrócone.");
			return;
		}

		if (!arenaInfo.startSession(player, tier)) {
			MoneyUtils.giveMoney(player, tier.getStake());
			ChallengeArenaManager.release(player.getName());
			arenaInfo.teleportToLobby(player);
			raiser.say("Nie udało się rozpocząć walki. Wpisowe zostało zwrócone.");
			return;
		}

		ChallengeArenaRewardService.recordEntry(player, tier);
		raiser.setAttending(null);
		raiser.setCurrentState(ConversationStates.IDLE);
	}
}
