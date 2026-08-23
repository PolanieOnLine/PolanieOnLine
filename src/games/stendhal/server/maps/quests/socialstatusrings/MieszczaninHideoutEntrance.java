/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import org.apache.log4j.Logger;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.mapstuff.quest.PlayerPrivateQuestUseableProp;
import games.stendhal.server.entity.player.Player;
import marauroa.server.game.rp.InstanceScope;
import marauroa.server.game.rp.InstanceZoneManager;

/** Final private trail mark that opens the player's bandit hideout instance. */
final class MieszczaninHideoutEntrance extends PlayerPrivateQuestUseableProp {
	private static final Logger logger = Logger.getLogger(MieszczaninHideoutEntrance.class);

	MieszczaninHideoutEntrance(final Player owner) {
		super(owner, MieszczaninRoadScene.TRACK_TILESET, 4,
				MieszczaninRoadScene.TRACK_TILESET_COLUMNS, false);
		put("z", MieszczaninRoadScene.TRACK_Z_ORDER);
		setMenu("Wejdź|use");
		setDescription("Oto ukryte przejście między drzewami. Głębokie rysy po ciągniętym ładunku "
				+ "i liczne ślady butów znikają w gęstwinie. To wejście do kryjówki napastników. "
				+ "Użyj go, aby wejść.");
	}

	@Override
	public String getDescriptionName() {
		return "ukryte wejście do kryjówki";
	}

	@Override
	protected boolean onUsedByOwner(final Player player) {
		if (!player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
				PierscienMieszczanina.STATE_TRACKS)) {
			player.sendPrivateText("Nie masz teraz powodu, żeby zapuszczać się tędy w las.");
			return false;
		}

		final StendhalRPZone returnZone = player.getZone();
		if (returnZone == null || !MieszczaninRoadScene.ZONE_NAME.equals(returnZone.getName())) {
			return false;
		}

		final InstanceZoneManager manager = SingletonRepository.getRPWorld().getInstanceZoneManager();
		final MieszczaninHideoutInstanceFactory factory = new MieszczaninHideoutInstanceFactory(
				player, returnZone.getName(), player.getX(), player.getY());

		try {
			final StendhalRPZone hideout = (StendhalRPZone) manager.acquire(
					MieszczaninHideoutInstanceFactory.BASE_ZONE_ID,
					MieszczaninHideoutInstanceFactory.VARIANT_ID,
					InstanceScope.player(player.getName()), player.getName(), factory);

			if (!player.teleport(hideout,
					MieszczaninHideoutInstanceFactory.START_X,
					MieszczaninHideoutInstanceFactory.START_Y,
					Direction.UP, player)) {
				manager.release(hideout.getID(), player.getName());
				player.sendPrivateText("Nie udało się wejść między drzewa. Spróbuj ponownie.");
				return false;
			}

			if (MieszczaninHideoutProgress.isCleared(player)) {
				player.sendPrivateText("Wracasz do ukrytej kryjówki. Napastnicy już nie stanowią zagrożenia. Odszukaj Radomira albo odzyskaj narzędzia Stacha, jeśli jeszcze ich nie zabrałeś.");
			} else {
				player.sendPrivateText("Przeciskasz się między gałęziami i wchodzisz do ukrytej kryjówki napastników. Najpierw rozpraw się z ludźmi pilnującymi środka, potem sprawdź uwięzionego człowieka przy północno wschodniej ścianie.");
			}
			return true;
		} catch (final Exception e) {
			logger.error("Nie udało się utworzyć kryjówki dla " + player.getName(), e);
			player.sendPrivateText("Nie udało się wejść do kryjówki. Spróbuj ponownie później.");
			return false;
		}
	}
}
