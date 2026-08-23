/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import java.awt.geom.Rectangle2D;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.MovementListener;
import games.stendhal.server.entity.ActiveEntity;
import games.stendhal.server.entity.player.Player;

/** Ensures the repair prop appears even when an older transition entered repair. */
public final class MieszczaninRepairMovementSync {
	private MieszczaninRepairMovementSync() {
		// utility class
	}

	/** Attach a lightweight recovery sync to the settlement zone. */
	public static void attach(final StendhalRPZone zone) {
		zone.addMovementListener(new MovementListener() {
			private final Rectangle2D area = new Rectangle2D.Double(0, 0, 128, 128);

			@Override
			public Rectangle2D getArea() {
				return area;
			}

			@Override
			public void onMoved(final ActiveEntity entity, final StendhalRPZone currentZone,
					final int oldX, final int oldY, final int newX, final int newY) {
				if (entity instanceof Player) {
					final Player player = (Player) entity;
					if (player.isQuestInState(PierscienMieszczanina.QUEST_SLOT,
							PierscienMieszczanina.STATE_REPAIR)
							&& !MieszczaninRepairProgress.isRepaired(player)) {
						MieszczaninRepairStage.syncRepairSite(currentZone, player);
					}
				}
			}

			@Override
			public void onEntered(final ActiveEntity entity, final StendhalRPZone currentZone,
					final int newX, final int newY) {
				// The zone-enter listener in MieszczaninRepairStage handles this.
			}

			@Override
			public void onExited(final ActiveEntity entity, final StendhalRPZone currentZone,
					final int oldX, final int oldY) {
				// The zone-exit listener in MieszczaninRepairStage handles this.
			}

			@Override
			public void beforeMove(final ActiveEntity entity, final StendhalRPZone currentZone,
					final int oldX, final int oldY, final int newX, final int newY) {
				// nothing
			}
		});
	}
}
