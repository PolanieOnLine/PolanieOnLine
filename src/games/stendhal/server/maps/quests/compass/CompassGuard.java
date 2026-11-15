/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.compass;

import games.stendhal.common.constants.Nature;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.pathfinder.FixedPath;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.Killer;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.PoisonStatus;
import games.stendhal.server.events.AttackEvent;
import games.stendhal.server.maps.quests.compass.CompassProgress.Stage;
import games.stendhal.server.maps.quests.trail.TrailSceneRegistry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/** Private native-art encounters; no global creature or repeatable drops. */
final class CompassGuard extends Creature {
	static final int[] X = {41, 90, 82, 87}, Y = {81, 70, 48, 51};
	private final int role;
	private boolean recorded;
	private boolean engaged;
	private int sceneTurns;
	private SpeakerNPC sceneOpponent;

	private CompassGuard(Creature nativeCreature, int role) {
		super(nativeCreature);
		this.role = role;
		setXP(0);
		clearDropItemList();
		setName(role < 2 ? "wilk przy rannym" : "błędne światło");
		setDescription(
				role < 2
						? "Wilk osaczył rannego wędrowca."
						: "Błędne światło tańczy wokół zagubionej obudowy kompasu.");
		setLevel(role < 2 ? 12 : 18);
		setAtk(role < 2 ? 55 : 70);
		setRatk(0);
		setDef(role < 2 ? 12 : 17);
		initHP(role < 2 ? 180 : 240);
		initArmorHP(0);
		setArmorType("none");
		setResistance(0);
		setVisibility(100);
		setBaseSpeed(0.4);
		LinkedHashMap<String, String> profiles = new LinkedHashMap<>();
		profiles.put("animal", "");
		profiles.put("offensive", "");
		profiles.put("no_elite", "");
		setAIProfiles(profiles);
	}

	static CompassGuard create(int role) {
		Creature source =
				SingletonRepository.getEntityManager().getCreature(role < 2 ? "wilk" : "błędny ognik");
		if (source == null) throw new IllegalStateException("Missing compass encounter creature");
		return new CompassGuard(source, role);
	}

	static boolean needed(Player p, int role) {
		return CompassProgress.stage(p) == (role < 2 ? Stage.WOLVES : Stage.CASE)
				&& (CompassProgress.detail(p) & (1 << (role % 2))) == 0;
	}

	int role() {
		return role;
	}

	private boolean eligible(RPEntity enemy) {
		return enemy instanceof Player
				&& PlayerPrivateScene.isOwnedBy(this, (Player) enemy)
				&& needed((Player) enemy, role)
				&& enemy.getZone() == getZone()
				&& (role >= 2 || engaged)
				&& Math.abs(enemy.getX() - (role < 2 ? getX() : X[role])) <= (role < 2 ? 16 : 8)
				&& Math.abs(enemy.getY() - (role < 2 ? getY() : Y[role])) <= (role < 2 ? 16 : 8);
	}

	@Override
	public List<RPEntity> getEnemyList() {
		List<RPEntity> enemies = new ArrayList<>();
		for (RPEntity e : super.getEnemyList()) if (eligible(e)) enemies.add(e);
		return enemies;
	}

	@Override
	public void logic() {
		if (role < 2 && !engaged) {
			Player owner = null;
			if (getZone() != null) {
				for (Player player : getZone().getPlayers()) {
					if (PlayerPrivateScene.isOwnedBy(this, player)) {
						owner = player;
						break;
					}
				}
			}
			RPEntity target = owner == null ? null : owner.getAttackTarget();
			if (owner != null && ((target instanceof CompassGuard
					&& ((CompassGuard) target).role < 2 && PlayerPrivateScene.isOwnedBy(target, owner))
					|| getAttackingRPEntities().contains(owner))) {
				engaged = true;
				clearSceneAttack();
				clearPath();
				stop();
			} else {
				rescueScene(owner);
				return;
			}
		}
		if (getAttackTarget() != null && !eligible(getAttackTarget())) {
			stopAttack();
			clearPath();
			stop();
		}
		super.logic();
	}

	/** Native movement and attack perceptions only: Dobromir is never a combat target. */
	private void rescueScene(Player owner) {
		SpeakerNPC wounded = null;
		if (owner != null && needed(owner, role)) {
			for (Entity entity : getZone().getEntitiesOfClass(SpeakerNPC.class)) {
				if (PlayerPrivateScene.isOwnedBy(entity, owner)
						&& CompassProgress.WANDERER.equals(TrailSceneRegistry.speakerName(entity))) {
					wounded = (SpeakerNPC) entity;
					break;
				}
			}
		}
		if (wounded == null) {
			clearSceneAttack();
			clearPath();
			stop();
			notifyWorldAboutChanges();
			return;
		}
		sceneOpponent = wounded;
		if (!nextTo(wounded, 3)) {
			if (!hasPath()) setMovement(wounded, 2, 9, 48);
		} else if (!hasPath()) {
			// The close points are short lunges; the outer points keep the pack circling.
			int[][] offsets = {{-2, -1}, {-1, -1}, {-2, -2}, {2, -2},
					{2, 1}, {1, 1}, {2, 2}, {-2, 2}};
			List<Node> nodes = new ArrayList<>();
			for (int i = 0; i < offsets.length; i++) {
				int[] point = offsets[(i + role * 4) % offsets.length];
				nodes.add(new Node(wounded.getX() + point[0], wounded.getY() + point[1]));
			}
			setPath(new FixedPath(nodes, true));
		}
		checkPause();
		if (nextTo(wounded, 2) && ++sceneTurns % 6 == 0) {
			faceToward(wounded);
			wounded.faceToward(this);
			// Clients use this attribute to resolve native attack events. Deliberately
			// do not call setTarget: neither actor enters damaging server combat.
			put("target", wounded.getID().getObjectID());
			wounded.put("target", getID().getObjectID());
			boolean parried = sceneTurns % 12 == 0;
			addEvent(new AttackEvent(parried, 0, Nature.CUT, null, false, parried));
			wounded.addEvent(new AttackEvent(!parried, 0, Nature.CUT, null, false, !parried));
			pauseTurnsRemaining = 1;
			wounded.notifyWorldAboutChanges();
		}
		notifyWorldAboutChanges();
	}

	private void clearSceneAttack() {
		if (sceneOpponent != null && sceneOpponent.has("target")
				&& sceneOpponent.getInt("target") == getID().getObjectID()) {
			sceneOpponent.remove("target");
			sceneOpponent.notifyWorldAboutChanges();
		}
		sceneOpponent = null;
		if (getAttackTarget() == null && has("target")) remove("target");
	}

	@Override
	protected void dropItemsOn(Corpse corpse) {}

	@Override
	public Creature getNewInstance() {
		return new CompassGuard(this, role);
	}

	@Override
	public void onDead(Killer killer, boolean remove) {
		clearSceneAttack();
		Entity source = killer instanceof PoisonStatus
				? ((PoisonStatus) killer).getSource()
				: killer instanceof RPEntity ? (RPEntity) killer : null;
		if (!recorded
				&& getZone() != null
				&& source != null
				&& PlayerPrivateScene.canInteract(this, source)) {
			recorded = true;
			for (Player owner : getZone().getPlayers())
				if (PlayerPrivateScene.isOwnedBy(this, owner)
						&& needed(owner, role)
						&& owner.nextTo(this, 16)) {
					CompassProgress.set(
							owner,
							CompassProgress.stage(owner),
							CompassProgress.detail(owner) | (1 << (role % 2)),
							0);
					owner.sendPrivateText(CompassProgress.objective(owner));
					break;
				}
		}
		super.onDead(killer, remove);
	}
}
