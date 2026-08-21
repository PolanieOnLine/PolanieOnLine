/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.quest.PlayerPrivateQuestUseableProp;
import games.stendhal.server.entity.player.Player;

/** Private damaged crossing repaired during the final Mieszczanin stage. */
final class MieszczaninRepairSite extends PlayerPrivateQuestUseableProp {

	MieszczaninRepairSite(final Player owner) {
		super(owner, "item/logs", 2, 1, false);
		setMenu("Napraw|use");
		setDescription("Oto dolna część uszkodzonego przejazdu. Stach przygotował już belki i klamry, ale drewno trzeba jeszcze dopasować, spiąć i usztywnić, zanim znów przejedzie tędy pełny wóz.");
	}

	@Override
	public String getDescriptionName() {
		return "uszkodzony przejazd";
	}

	@Override
	protected boolean onUsedByOwner(final Player player) {
		if (!player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_REPAIR)) {
			player.sendPrivateText("Nie masz teraz powodu, żeby naprawiać ten fragment traktu.");
			return false;
		}
		if (MieszczaninRepairProgress.isRepaired(player)) {
			player.sendPrivateText("Przejazd jest już naprawiony.");
			return false;
		}

		MieszczaninRepairProgress.markRepaired(player);
		player.sendPrivateText("Dopasowujesz przygotowane przez Stacha belki, spinając obie części klamrami i wzmacniając luźne deski. Przejazd znów wygląda na bezpieczny. Wróć do Stacha, żeby sprawdził robotę.");

		final StendhalRPZone zone = getZone();
		if (zone != null) {
			MieszczaninRepairStage.removeRepairSite(zone, player);
		}
		return true;
	}
}
