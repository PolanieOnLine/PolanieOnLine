/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.NPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/** Each player guides their own passable group; saved stops survive logout. */
final class BellTrailEscort extends NPC {
	private final Player owner;
	private int turns;

	private BellTrailEscort(Player owner, int number, int[] start) {
		this.owner = owner;
		setName(number == 0 ? "ranny badacz" : "ranny zwiadowca");
		setEntityClass(number == 0 ? "woodcutternpc" : "youngsoldiernpc");
		setDescription("Członek wyprawy pod opieką " + owner.getName()
				+ ". Idzie powoli i czeka, gdy przewodnik walczy.");
		setPosition(start[0], start[1] + number);
		setResistance(0);
		setBaseSpeed(0.35);
	}

	static boolean start(Player player) {
		if (!BellTrailProgress.active(player, Step.BELL_ESCAPE)
				|| (!player.isQuestInState(Step.BELL_ESCAPE.slot, 0, "escort")
						&& !player.isQuestInState(Step.BELL_ESCAPE.slot, 0,
								"ambush"))) {
			return false;
		}
		StendhalRPZone zone = player.getZone();
		if (zone == null || !BellTrailProgress.ISLANDS.equals(zone.getName())) {
			return false;
		}
		if (group(player).size() == 2) {
			player.sendPrivateText(
					"Twoi ranni już czekają na trasie. Wróć do nich, nie nadążą, jeśli pobiegniesz sam.");
			return true;
		}
		for (BellTrailEscort escort : group(player)) {
			zone.remove(escort);
		}
		int checkpoint = Math.max(0, Math.min(2,
				BellTrailProgress.counter(player, Step.BELL_ESCAPE)));
		for (int i = 0; i < 2; i++) {
			zone.add(new BellTrailEscort(player, i,
					BellTrailProgress.ROUTE[checkpoint]));
		}
		player.sendPrivateText(
				"Ranni czekają przy ostatnim bezpiecznym postoju. Idź powoli. Zatrzymają się, gdy zaczniesz walczyć lub znikniesz im z oczu.");
		return true;
	}

	static List<BellTrailEscort> group(Player player) {
		List<BellTrailEscort> result = new ArrayList<>();
		if (player.getZone() != null) {
			for (Entity entity : player.getZone().getEntitiesOfClass(
					BellTrailEscort.class)) {
				BellTrailEscort escort = (BellTrailEscort) entity;
				if (escort.owner == player) {
					result.add(escort);
				}
			}
		}
		return result;
	}

	static boolean arrive(Player player, Entity stop, int checkpoint) {
		if (!BellTrailProgress.active(player, Step.BELL_ESCAPE)
				|| !player.isQuestInState(Step.BELL_ESCAPE.slot,
						"escort;" + (checkpoint - 1))) {
			return false;
		}
		List<BellTrailEscort> group = group(player);
		if (group.size() != 2
				|| group.stream().anyMatch(npc -> !npc.nextTo(stop, 4))) {
			player.sendPrivateText(
					"Zaczekaj na obu rannych. Samo dotarcie do postoju nie wystarczy.");
			return false;
		}
		player.setQuest(Step.BELL_ESCAPE.slot, checkpoint == 3 ? "return"
				: checkpoint == 1 ? "ambush;1" : "escort;2");
		if (checkpoint == 3) {
			for (BellTrailEscort npc : group) {
				player.getZone().remove(npc);
			}
		}
		return true;
	}

	@Override
	public void logic() {
		if (getZone() == null) {
			return;
		}
		if (owner.isDisconnected() || owner.getZone() != getZone()
				|| !getZone().has(owner.getID())
				|| !BellTrailProgress.active(owner, Step.BELL_ESCAPE)
				|| owner.isQuestInState(Step.BELL_ESCAPE.slot, "return")) {
			getZone().remove(this);
			return;
		}
		if (owner.isQuestInState(Step.BELL_ESCAPE.slot, 0, "ambush")
				|| owner.getAttackTarget() != null
				|| !owner.getAttackingRPEntities().isEmpty()
				|| !nextTo(owner, 12)) {
			clearPath();
			stop();
			return;
		}
		if (turns++ % 4 == 0) {
			setMovement(owner, 1, 4, 12);
		}
		super.logic();
	}
}
