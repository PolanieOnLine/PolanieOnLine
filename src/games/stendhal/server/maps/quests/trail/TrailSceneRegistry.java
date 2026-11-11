/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.events.ZoneEnterExitListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.PlayerPrivateScene;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.npc.PlayerPrivateSpeakerNPC;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;

/** Reconstructs personal story actors from the player's saved quest checkpoints. */
public final class TrailSceneRegistry {
	private static final Logger LOGGER = Logger.getLogger(TrailSceneRegistry.class);
	private static final Map<String, SceneZone> ZONES = new HashMap<>();
	private static final Set<Player> PENDING = Collections.synchronizedSet(
			Collections.newSetFromMap(new IdentityHashMap<Player, Boolean>()));

	private TrailSceneRegistry() {
	}

	public static void prop(final StendhalRPZone zone, final String key, final Predicate<Player> needed,
			final Supplier<? extends Entity> factory) {
		register(zone, key, needed, owner -> factory.get());
	}

	public static void encounter(final StendhalRPZone zone, final String key, final int x, final int y,
			final Predicate<Player> needed, final Supplier<? extends Creature> factory) {
		register(zone, key, needed, owner -> {
			Creature creature = factory.get();
			if (creature.getClass() == Creature.class) {
				creature = new SceneFighter(creature);
			}
			creature.setXP(0);
			creature.clearDropItemList();
			creature.setPosition(x, y);
			final CreatureRespawnPoint origin = new CreatureRespawnPoint(zone, x, y, creature, 1) {
				@Override
				public void onTurnReached(final int turn) {
					// Only persisted quest state may create this encounter.
				}

				@Override
				public void notifyDead(final Creature victim) {
					// Preserve origin metadata without scheduling a shared respawn.
				}
			};
			TurnNotifier.get().dontNotify(origin);
			creature.setRespawnPoint(origin);
			return creature;
		});
	}

	public static void actor(final StendhalRPZone zone, final SpeakerNPC template,
			final Predicate<Player> needed) {
		state(zone);
		// Keep the canonical dialogue binding available during quest startup.
		// The template never appears to a real player or starts conversations.
		template.put("#perception_key", "name");
		template.put("#perception_value", "!trail-dialogue:" + template.getName());
		template.put("owner_collision_only", "");
		template.setResistance(0);
		template.hideLocation();
		zone.add(template);
		register(zone, "npc:" + template.getName(), needed, owner -> copyActor(owner, template));
	}

	private static SpeakerNPC copyActor(final Player owner, final SpeakerNPC template) {
		final PlayerPrivateSpeakerNPC actor = new PlayerPrivateSpeakerNPC(owner, template.getName());
		for (final String attribute : template) {
			if (!attribute.equals("id") && !attribute.equals("zoneid") && !attribute.equals("name")
					&& !attribute.equals("#perception_key") && !attribute.equals("#perception_value")
					&& !attribute.equals("text") && !attribute.equals("conversation_player")) {
				actor.put(attribute, template.get(attribute));
			}
		}
		actor.setTitle(template.getName());
		actor.setPosition(template.getX(), template.getY());
		actor.setDirection(template.getDirection());
		actor.setGender(template.getGender());
		actor.setDescription(template.getDescription());
		actor.setResistance(100);
		actor.copyDialogueFrom(template);
		return actor;
	}

	/** Resolves private actors to the canonical role used by existing dialogue. */
	public static String speakerName(final Entity speaker) {
		return speaker instanceof PlayerPrivateSpeakerNPC ? ((PlayerPrivateSpeakerNPC) speaker).getTitle()
				: speaker.get("name");
	}

	private static void register(final StendhalRPZone zone, final String key, final Predicate<Player> needed,
			final ActorFactory factory) {
		final SceneZone scene = state(zone);
		scene.definitions.put(key, new Definition(needed, factory));
		for (final Player player : zone.getPlayers()) {
			refreshLater(player);
		}
	}

	/** Quest mutation callbacks only enqueue work; they never remove the active speaker mid-dialogue. */
	public static void questChanged(final Player player, final String quest) {
		if (quest != null && (quest.startsWith("trail_")
				|| "wanderers_compass".equals(quest))) {
			refreshLater(player);
		}
	}

	public static void refreshLater(final Player player) {
		if (player == null || !PENDING.add(player)) {
			return;
		}
		TurnNotifier.get().notifyInTurns(1, turn -> {
			PENDING.remove(player);
			final StendhalRPZone zone = player.getZone();
			final SceneZone scene = zone == null ? null : ZONES.get(zone.getName());
			if (scene != null && scene.zone == zone && zone.has(player.getID())
					&& zone.getPlayers().contains(player) && player.getHP() > 0) {
				scene.refresh(player);
			}
		});
	}

	/** Level requirements can become satisfied without leaving the map. */
	public static void checkAvailability(final Player player) {
		final StendhalRPZone zone = player.getZone();
		final SceneZone scene = zone == null ? null : ZONES.get(zone.getName());
		if (scene != null && scene.zone == zone
				&& !Integer.valueOf(player.getLevel()).equals(scene.levels.get(player))) {
			refreshLater(player);
		}
	}

	private static SceneZone state(final StendhalRPZone zone) {
		SceneZone scene = ZONES.get(zone.getName());
		if (scene == null || scene.zone != zone) {
			if (scene != null) {
				for (final Player player : new ArrayList<>(scene.actors.keySet())) {
					scene.clear(player);
				}
			}
			scene = new SceneZone(zone);
			ZONES.put(zone.getName(), scene);
			zone.addZoneEnterExitListener(scene);
		}
		return scene;
	}

	private interface ActorFactory {
		Entity create(Player owner);
	}

	private static final class Definition {
		final Predicate<Player> needed;
		final ActorFactory factory;

		Definition(final Predicate<Player> needed, final ActorFactory factory) {
			this.needed = needed;
			this.factory = factory;
		}
	}

	private static final class SceneZone implements ZoneEnterExitListener {
		final StendhalRPZone zone;
		final Map<String, Definition> definitions = new LinkedHashMap<>();
		final Map<Player, Map<String, Entity>> actors = new IdentityHashMap<>();
		final Map<Player, Integer> levels = new IdentityHashMap<>();

		SceneZone(final StendhalRPZone zone) {
			this.zone = zone;
		}

		@Override
		public void onEntered(final RPObject object, final StendhalRPZone current) {
			if (object instanceof Player) {
				refreshLater((Player) object);
			}
		}

		@Override
		public void onExited(final RPObject object, final StendhalRPZone current) {
			if (object instanceof Player) {
				clear((Player) object);
			}
		}

		void refresh(final Player player) {
			levels.put(player, player.getLevel());
			final Map<String, Entity> owned = actors.computeIfAbsent(player, key -> new LinkedHashMap<>());
			final Iterator<Map.Entry<String, Entity>> iterator = owned.entrySet().iterator();
			while (iterator.hasNext()) {
				final Map.Entry<String, Entity> entry = iterator.next();
				final Definition definition = definitions.get(entry.getKey());
				if (definition == null || !definition.needed.test(player)
						|| !zone.has(entry.getValue().getID())) {
					remove(entry.getValue());
					iterator.remove();
				}
			}
			clearCorpses(player);
			for (final Map.Entry<String, Definition> entry : definitions.entrySet()) {
				if (!owned.containsKey(entry.getKey()) && entry.getValue().needed.test(player)) {
					try {
						final Entity actor = entry.getValue().factory.create(player);
						PlayerPrivateScene.assign(actor, player);
						zone.add(actor);
						owned.put(entry.getKey(), actor);
					} catch (final RuntimeException exception) {
						LOGGER.error("Cannot create private story actor " + entry.getKey(), exception);
					}
				}
			}
			if (owned.isEmpty()) {
				actors.remove(player);
			}
			if (BellTrailProgress.ISLANDS.equals(zone.getName())
					&& BellTrailProgress.active(player, TrailQuestLine.Step.BELL_ESCAPE)
					&& player.isQuestInState(TrailQuestLine.Step.BELL_ESCAPE.slot, 0, "escort", "ambush")
					&& BellTrailEscort.group(player).size() != 2) {
				BellTrailEscort.start(player);
			}
		}

		void clear(final Player player) {
			levels.remove(player);
			final Map<String, Entity> owned = actors.remove(player);
			if (owned != null) {
				for (final Entity actor : owned.values()) {
					remove(actor);
				}
			}
			clearCorpses(player);
			for (final Entity escort : new ArrayList<>(zone.getEntitiesOfClass(BellTrailEscort.class))) {
				if (PlayerPrivateScene.isOwnedBy(escort, player)) {
					remove(escort);
				}
			}
		}

		void clearCorpses(final Player player) {
			for (final Entity corpse : new ArrayList<>(zone.getEntitiesOfClass(Corpse.class))) {
				if (PlayerPrivateScene.isOwnedBy(corpse, player)) {
					remove(corpse);
				}
			}
		}

		void remove(final Entity actor) {
			if (zone.has(actor.getID())) {
				if (actor instanceof RPEntity) {
					final RPEntity being = (RPEntity) actor;
					for (final RPEntity attacker : new ArrayList<>(being.getAttackingRPEntities())) {
						attacker.stopAttack();
					}
					being.stopAttack();
					being.getStatusList().removeAll();
					being.stop();
				}
				zone.remove(actor);
			}
		}
	}

	private static final class SceneFighter extends Creature {
		SceneFighter(final Creature template) {
			super(template);
		}

		@Override
		protected void dropItemsOn(final Corpse corpse) {
			// All rewards belong to the persisted quest, never a repeatable spawn.
		}
	}
}
