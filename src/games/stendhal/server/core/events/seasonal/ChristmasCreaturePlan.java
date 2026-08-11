/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import java.util.List;

import games.stendhal.common.parser.ExpressionType;
import games.stendhal.common.parser.WordList;
import games.stendhal.server.core.config.CreatureGroupsXMLLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.defaultruleset.DefaultCreature;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPointUpdater;
import marauroa.common.game.IRPZone;

/**
 * Prepared Christmas-specific creature definition change.
 *
 * Current data uses the same tile id for the normal deer and its Christmas
 * reindeer replacement. Existing live creatures are not killed; spawn points
 * switch prototype so their next respawns use the selected event variant.
 */
final class ChristmasCreaturePlan {
	private static final String CREATURE_GROUPS = "/data/conf/creatures.xml";
	private static final String NORMAL_NAME = "jeleń";
	private static final String CHRISTMAS_NAME = "renifer";

	private final DefaultCreature activeDefinition;

	private ChristmasCreaturePlan(final DefaultCreature activeDefinition) {
		this.activeDefinition = activeDefinition;
	}

	static ChristmasCreaturePlan prepare(final boolean enabled) {
		final String target = enabled ? CHRISTMAS_NAME : NORMAL_NAME;
		final List<DefaultCreature> creatures =
				new CreatureGroupsXMLLoader(CREATURE_GROUPS).load();
		DefaultCreature found = null;
		for (final DefaultCreature creature : creatures) {
			if (target.equals(creature.getCreatureName())) {
				if (found != null) {
					throw new IllegalStateException("Duplicate creature definition: " + target);
				}
				found = creature;
			}
		}
		if (found == null) {
			throw new IllegalStateException("Missing creature definition for " + target);
		}
		if (!found.verifyItems(SingletonRepository.getEntityManager())) {
			throw new IllegalStateException("Invalid item references for creature " + target);
		}
		return new ChristmasCreaturePlan(found);
	}

	void apply() {
		final EntityManager manager = SingletonRepository.getEntityManager();
		removeDefinition(manager, NORMAL_NAME);
		removeDefinition(manager, CHRISTMAS_NAME);

		final String target = activeDefinition.getCreatureName();
		if (!manager.addCreature(activeDefinition)) {
			throw new IllegalStateException("Unable to activate creature definition " + target);
		}
		WordList.getInstance().registerName(target, ExpressionType.SUBJECT);

		for (final IRPZone irpZone : SingletonRepository.getRPWorld()) {
			final StendhalRPZone zone = (StendhalRPZone) irpZone;
			for (final CreatureRespawnPoint point : zone.getRespawnPointList()) {
				final String prototypeName = point.getPrototypeCreature().getName();
				if (NORMAL_NAME.equals(prototypeName) || CHRISTMAS_NAME.equals(prototypeName)) {
					CreatureRespawnPointUpdater.replacePrototype(
							point, activeDefinition.getCreature());
				}
			}
		}
	}

	private static void removeDefinition(final EntityManager manager,
			final String name) {
		final DefaultCreature definition = manager.getDefaultCreature(name);
		if (definition != null) {
			manager.getDefaultCreatures().remove(definition);
		}
	}
}
