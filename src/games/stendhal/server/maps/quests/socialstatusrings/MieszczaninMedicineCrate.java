/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.quest.PlayerPrivateQuestUseableProp;
import games.stendhal.server.entity.player.Player;

/** Private medicine crate stolen from Witomir's delivery. */
final class MieszczaninMedicineCrate extends PlayerPrivateQuestUseableProp {

	MieszczaninMedicineCrate(final Player owner) {
		super(owner, "item/pot/crate_small", 1, 2, true);
		setMenu("Podnieś|Użyj");
		setDescription("Oto porzucona niewielka skrzynka. Na drewnie widać znak zielonego liścia, o którym mówił Witomir.");
	}

	@Override
	public String getDescriptionName() {
		return "skrzynka z lekarstwem";
	}

	@Override
	protected boolean onUsedByOwner(final Player player) {
		if (!player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_MEDICINE)) {
			player.sendPrivateText("Ta skrzynka nie jest teraz tym, czego szukasz.");
			return false;
		}

		player.setQuest(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_MEDICINE_FOUND);
		player.sendPrivateText("Odzyskałeś skrzynkę z lekarstwem oznaczoną zielonym liściem. Zanieś ją do Dobrawy przy studni.");

		final StendhalRPZone zone = getZone();
		if (zone != null) {
			zone.remove(getID());
		}
		return true;
	}
}
