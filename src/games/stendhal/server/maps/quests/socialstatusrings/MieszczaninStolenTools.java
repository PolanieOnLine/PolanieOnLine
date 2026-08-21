/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.quest.PlayerPrivateQuestUseableProp;
import games.stendhal.server.entity.player.Player;

/** Stach's stolen tools recovered from the private hideout. */
final class MieszczaninStolenTools extends PlayerPrivateQuestUseableProp {

	MieszczaninStolenTools(final Player owner) {
		super(owner, "item/dropped_tools", 0, 1, false);
		setMenu("Zabierz|Użyj");
		setDescription("Oto zestaw Stacha: ciężki młotek, piła i dłuta do naprawy wozów oraz drewnianych przepraw. Rękojeści są starte od pracy, a na metalu widać świeże zadrapania po rabunku.");
	}

	@Override
	public String getDescriptionName() {
		return "skradzione narzędzia Stacha";
	}

	@Override
	protected boolean onUsedByOwner(final Player player) {
		if (!player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_TRACKS)
				|| !MieszczaninHideoutProgress.isMessengerFreed(player)) {
			player.sendPrivateText("Najpierw upewnij się, że uwięziony posłaniec jest bezpieczny.");
			return false;
		}
		if (MieszczaninHideoutProgress.areToolsRecovered(player)) {
			return false;
		}

		MieszczaninHideoutProgress.markToolsRecovered(player);
		player.sendPrivateText("Zbierasz narzędzia Stacha. Wróć do przysiółka i oddaj mu je. Wyjście z kryjówki jest przy południowej ścianie, dokładnie tam, gdzie wszedłeś.");

		final StendhalRPZone zone = getZone();
		if (zone != null) {
			zone.remove(getID());
		}
		return true;
	}
}
