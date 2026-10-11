/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.ZoneEnterExitListener;
import games.stendhal.server.entity.mapstuff.portal.Portal;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.server.game.rp.InstanceZoneDescriptor;
import marauroa.server.game.rp.InstanceZoneFactory;

/** Copies map layers only: monsters, drops and timers belong to one player. */
final class ChallengeArenaInstanceFactory implements InstanceZoneFactory {
	// Last walkable row in front of the closed south gate in the real map.
	static final int EXIT_Y = 31;
	private final Player owner;
	private final ChallengeArenaInfo template;
	private ChallengeArenaInfo info;

	ChallengeArenaInstanceFactory(final Player owner, final ChallengeArenaInfo template) {
		this.owner = owner;
		this.template = template;
	}

	ChallengeArenaInfo getArenaInfo() {
		return info;
	}

	@Override
	public IRPZone create(final InstanceZoneDescriptor descriptor) {
		final StendhalRPZone zone = new StendhalRPZone(
				descriptor.getRuntimeZoneIdString(), template.getZone());
		zone.getAttributes().put("readable_name", "Arena Wyzwań w Tarnowie");
		zone.setPublicAccessible(false);
		zone.setMoveToAllowed(false);
		zone.disallowIn();
		info = template.forInstance(zone);
		final ChallengeArenaPlayerGate gate = new ChallengeArenaPlayerGate(
				zone.getWidth(), zone.getHeight(), owner.getName());
		gate.setPosition(0, 0);
		zone.add(gate);
		// The south gate is also an early exit after victory (no reward lost).
		for (int x = 31; x <= 33; x++) {
			final Portal exit = new Portal() {
				@Override protected boolean usePortal(final Player player) {
					if (!nextTo(player)) {
						return false;
					}
					if (player != owner) {
						// An administrative visit must never forfeit the fighter's run.
						return ChallengeArenaPlayerGate.hasAdministrativeAccess(player) && info.teleportToLobby(player);
					}
					final ChallengeArenaEngine engine = info.getEngine();
					return engine != null && engine.forfeit(player);
				}
			};
			exit.setIdentifier("challenge_arena_private_exit_" + x);
			exit.setIgnoreNoDestination(true); // Destination is resolved by the owning run.
			exit.setPosition(x, EXIT_Y);
			zone.add(exit);
		}
		zone.addZoneEnterExitListener(new ZoneEnterExitListener() {
			@Override public void onEntered(final RPObject object, final StendhalRPZone entered) {
				// Access is controlled by the owner gate and teleport restrictions.
			}

			@Override public void onExited(final RPObject object, final StendhalRPZone exited) {
				if (object != owner) {
					return;
				}
				info.saveLobbyPosition(owner);
				stopRun();
				// Zone listeners run before the player is actually removed. Defer
				// destruction until the move has completed; logout also releases membership.
				SingletonRepository.getTurnNotifier().notifyInTurns(0,
						turn -> ChallengeArenaManager.release(owner.getName(), info));
			}
		});
		return zone;
	}

	private void stopRun() {
		final ChallengeArenaState state = ChallengeArenaState.parse(
				owner.getQuest(ChallengeArenaState.QUEST_SLOT));
		if (state != null && state.getLifecycle() == ChallengeArenaState.Lifecycle.ACTIVE) {
			owner.setQuest(ChallengeArenaState.QUEST_SLOT,
					state.withLifecycle(ChallengeArenaState.Lifecycle.FAILED).serialize());
		}
		final ChallengeArenaEngine engine = info.getEngine();
		if (engine != null) {
			engine.stop();
		}
	}

	@Override
	public void destroy(final InstanceZoneDescriptor descriptor, final IRPZone destroyed) {
		if (info == null) {
			return;
		}
		stopRun();
		final StendhalRPZone zone = (StendhalRPZone) destroyed;
		// Administrative visitors must not remain in a detached zone.
		for (final Player visitor : new ArrayList<>(zone.getPlayers())) {
			info.teleportToLobby(visitor);
		}
		final List<RPObject> objects = new ArrayList<>();
		for (final RPObject object : zone) {
			objects.add(object);
		}
		for (final RPObject object : objects) {
			zone.remove(object); // Cancels corpse/item decay and entity listeners, too.
		}
		ChallengeArenaManager.forgetSession(owner.getName(), info);
	}
}
