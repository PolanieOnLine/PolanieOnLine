/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import games.stendhal.server.entity.mapstuff.quest.PlayerPrivateQuestProp;
import games.stendhal.server.entity.player.Player;

/** Upper half of the two-tile timber prop used at the damaged crossing. */
final class MieszczaninRepairBeam extends PlayerPrivateQuestProp {

	MieszczaninRepairBeam(final Player owner) {
		super(owner, "item/logs", 1, 1, false);
		setDescription("Oto górna część prowizorycznie ułożonych belek przy uszkodzonym przejeździe. Drewno jest już dopasowane, ale całość wciąż wymaga spięcia i wzmocnienia.");
	}

	@Override
	public String getDescriptionName() {
		return "prowizorycznie ułożone belki";
	}
}
