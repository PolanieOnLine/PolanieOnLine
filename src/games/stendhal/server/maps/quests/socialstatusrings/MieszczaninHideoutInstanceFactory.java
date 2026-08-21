/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import java.awt.geom.Rectangle2D;
import java.io.IOException;

import org.apache.log4j.Logger;

import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.common.tiled.StendhalMapStructure;
import games.stendhal.common.tiled.TileSetDefinition;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.Spot;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.MovementListener;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.ActiveEntity;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.mapstuff.portal.Teleporter;
import games.stendhal.server.entity.mapstuff.quest.PlayerPrivateQuestProp;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.server.game.rp.InstanceZoneDescriptor;
import marauroa.server.game.rp.InstanceZoneFactory;
import marauroa.server.game.rp.InstanceZoneManager;

/** Creates the fixed private hideout used by the Mieszczanin tracking stage. */
final class MieszczaninHideoutInstanceFactory implements InstanceZoneFactory {
	static final String BASE_ZONE_ID = "mieszczanin_hideout";
	static final String VARIANT_ID = "bandits";
	static final int WIDTH = 24;
	static final int HEIGHT = 18;
	static final int START_X = 12;
	static final int START_Y = 15;
	static final int EXIT_X = 12;
	static final int EXIT_Y = 16;

	private static final String DISPLAY_NAME = "Leśna kryjówka";
	private static final int FLOOR_TILE = 42;
	private static final int EXIT_TILE = 59;
	private static final int TOOLS_X = 6;
	private static final int TOOLS_Y = 3;
	private static final Logger logger = Logger.getLogger(MieszczaninHideoutInstanceFactory.class);

	private final Player owner;
	private final String returnZoneName;
	private final int returnX;
	private final int returnY;

	MieszczaninHideoutInstanceFactory(final Player owner, final String returnZoneName,
			final int returnX, final int returnY) {
		this.owner = owner;
		this.returnZoneName = returnZoneName;
		this.returnX = returnX;
		this.returnY = returnY;
	}

	@Override
	public IRPZone create(final InstanceZoneDescriptor descriptor) {
		final StendhalRPZone zone = buildZone(descriptor.getRuntimeZoneIdString());
		zone.getAttributes().put("readable_name", DISPLAY_NAME);
		return zone;
	}

	@Override
	public void destroy(final InstanceZoneDescriptor descriptor, final IRPZone zone) {
		cleanupTransientEntities((StendhalRPZone) zone);
	}

	private StendhalRPZone buildZone(final String runtimeZoneId) {
		final StendhalMapStructure map = createMapStructure();
		map.build();

		final StendhalRPZone zone = new StendhalRPZone(runtimeZoneId, WIDTH, HEIGHT);
		try {
			zone.addTilesets(runtimeZoneId + ".tilesets", map.getTilesets());
			zone.addLayer(runtimeZoneId + ".0_floor", map.getLayer("0_floor"));
			zone.addLayer(runtimeZoneId + ".1_terrain", map.getLayer("1_terrain"));
			zone.addLayer(runtimeZoneId + ".2_object", map.getLayer("2_object"));
			zone.addLayer(runtimeZoneId + ".3_roof", map.getLayer("3_roof"));
			zone.addCollisionLayer(runtimeZoneId + ".collision", map.getLayer("collision"));
			zone.addProtectionLayer(runtimeZoneId + ".protection", map.getLayer("protection"));
		} catch (final IOException e) {
			throw new IllegalStateException("Nie udało się zbudować mapy kryjówki", e);
		}

		final StendhalRPZone returnZone = SingletonRepository.getRPWorld().getZone(returnZoneName);
		if (returnZone == null) {
			throw new IllegalStateException("Brak strefy powrotnej dla kryjówki: " + returnZoneName);
		}

		final Teleporter exit = new Teleporter(new Spot(returnZone, returnX, returnY));
		exit.setPosition(EXIT_X, EXIT_Y);
		zone.add(exit);

		zone.setMoveToAllowed(false);
		zone.disallowIn();
		zone.addMovementListener(new HideoutMovementListener());
		populateQuestScene(zone);
		return zone;
	}

	private void populateQuestScene(final StendhalRPZone zone) {
		addDecoration(zone, "item/pot/barrels_1", 0, 4, true, 3, 4,
				"Oto ciężka beczka zabrana z jednego z napadniętych wozów. Na obręczach zaschło błoto z traktu.");
		addDecoration(zone, "item/pot/crate_small", 0, 1, true, 4, 4,
				"Oto skrzynia z wyrwanym znakiem kupieckim. Napastnicy najwyraźniej sortowali tutaj zdobyty ładunek.");
		addSackPair(zone, 3, 6,
				"Oto dwa worki z zapasami zabranymi z wozów. Część zawartości wysypała się na podłogę.");
		addDecoration(zone, "item/logs", 0, 1, true, 8, 3,
				"Oto stos drewna i połamanych elementów wozu. Zbóje odkładali tu wszystko, co mogło się jeszcze przydać.");

		addSackPair(zone, 19, 4,
				"Oto dwa ciężkie worki z różnych dostaw. Sznury i oznaczenia nie pasują do siebie, więc łupy muszą pochodzić z kilku napadów.");
		addDecoration(zone, "item/pot/crate_small", 0, 1, true, 20, 5,
				"Oto kolejna skrzynia ze skradzionego ładunku. Wieko zostało podważone w pośpiechu.");
		addDecoration(zone, "item/bazaar_produce", 2, 2, false, 18, 5,
				"Oto rozsypana żywność z napadniętych dostaw. Część zdążyła już zwiędnąć.");
		addDecoration(zone, "item/pot/barrels_1", 5, 4, true, 21, 7,
				"Oto przewrócona beczka. Wokół niej leżą ślady po przesuwaniu ciężkich skrzyń.");

		addDecoration(zone, "item/pot/barrels_1", 1, 4, true, 4, 13,
				"Oto beczka ustawiona przy południowej części kryjówki. Wygląda na zapas przygotowany do dalszego wywozu.");
		addDecoration(zone, "item/pot/crate_small", 0, 1, true, 5, 13,
				"Oto zamknięta skrzynia z cudzym znakiem przewozowym. Zbóje nie zdążyli jeszcze jej otworzyć.");
		addDecoration(zone, "item/logs", 0, 1, true, 7, 13,
				"Oto odłożone deski i kawałki drewna. Niektóre wyglądają jak części rozebranych wozów.");

		final MieszczaninMessengerNPC messenger = new MieszczaninMessengerNPC(owner);
		messenger.setPosition(18, 3);
		zone.add(messenger);

		if (!MieszczaninHideoutProgress.isCleared(owner)) {
			spawnAttackers(zone);
		}
		if (MieszczaninHideoutProgress.isMessengerFreed(owner)
				&& !MieszczaninHideoutProgress.areToolsRecovered(owner)) {
			ensureStolenTools(zone, owner);
		}
	}

	private void spawnAttackers(final StendhalRPZone zone) {
		final EntityManager manager = SingletonRepository.getEntityManager();
		addBandit(zone, new MieszczaninQuestBandit(
				manager.getCreature("zbójnik leśny"), owner,
				"napastnik z traktu",
				"Oto jeden z ludzi napadających na wozy kierowane na leśny objazd.",
				130, 740, 27, 800), 7, 11);
		addBandit(zone, new MieszczaninQuestBandit(
				manager.getCreature("zbójnik leśny zwiadowca"), owner,
				"strażnik kryjówki",
				"Oto czujny strażnik kryjówki. Obserwuje przejście i pilnuje skradzionego ładunku.",
				135, 500, 32, 1200), 16, 8);
		addBandit(zone, new MieszczaninQuestBandit(
				manager.getCreature("zbójnik leśny starszy"), owner,
				"herszt napastników",
				"Oto człowiek dowodzący napadami na trakt. Nie wygląda na kogoś, kto zamierza oddać łup po dobroci.",
				140, 800, 57, 1100), 11, 4);
	}

	private void addBandit(final StendhalRPZone zone, final Creature bandit,
			final int x, final int y) {
		bandit.setPosition(x, y);
		zone.add(bandit);
	}

	static void ensureStolenTools(final StendhalRPZone zone, final Player owner) {
		if (MieszczaninHideoutProgress.areToolsRecovered(owner)) {
			return;
		}
		for (final Entity entity : zone.getEntitiesOfClass(MieszczaninStolenTools.class)) {
			if (((MieszczaninStolenTools) entity).isOwnedBy(owner)) {
				return;
			}
		}
		final MieszczaninStolenTools tools = new MieszczaninStolenTools(owner);
		tools.setPosition(TOOLS_X, TOOLS_Y);
		zone.add(tools);
	}

	private void addSackPair(final StendhalRPZone zone, final int x, final int y,
			final String description) {
		// sacks.png is a two-tile horizontal composition. Keep the left and
		// right halves in their source order instead of showing isolated halves.
		addDecoration(zone, "item/sacks", 0, 2, true, x, y, description);
		addDecoration(zone, "item/sacks", 1, 2, true, x + 1, y, description);
	}

	private void addDecoration(final StendhalRPZone zone, final String tileset,
			final int tileIndex, final int columns, final boolean solid,
			final int x, final int y, final String description) {
		final PlayerPrivateQuestProp prop = new PlayerPrivateQuestProp(
				owner, tileset, tileIndex, columns, solid, true);
		prop.setDescription(description);
		prop.setPosition(x, y);
		zone.add(prop);
	}

	static StendhalMapStructure createMapStructure() {
		final LayerDefinition floor = createLayer("0_floor");
		final LayerDefinition terrain = createLayer("1_terrain");
		final LayerDefinition object = createLayer("2_object");
		final LayerDefinition roof = createLayer("3_roof");
		final LayerDefinition collision = createLayer("collision");
		final LayerDefinition protection = createLayer("protection");

		final StendhalMapStructure map = new StendhalMapStructure(WIDTH, HEIGHT);
		map.addLayer(floor);
		map.addLayer(terrain);
		map.addLayer(object);
		map.addLayer(roof);
		map.addLayer(collision);
		map.addLayer(protection);

		map.addTileset(new TileSetDefinition("filler", "../../tileset/ground/gravel.png", 1));
		map.addTileset(new TileSetDefinition("outercorners", "../../tileset/building/wall/int_wall_dark_red_corners_2.png", 2));
		map.addTileset(new TileSetDefinition("wall", "../../tileset/building/wall/int_wall_dark_red.png", 10));
		map.addTileset(new TileSetDefinition("innercorners", "../../tileset/building/wall/int_wall_dark_red_corners.png", 26));
		map.addTileset(new TileSetDefinition("paving", "../../tileset/ground/brown_paving.png", 42));
		map.addTileset(new TileSetDefinition("portal", "../../tileset/building/decoration/floor_sparkle.png", 44));

		for (int x = 0; x < WIDTH; x++) {
			for (int y = 0; y < HEIGHT; y++) {
				floor.set(x, y, FLOOR_TILE);
				if (x == 0 || y == 0 || x == WIDTH - 1 || y == HEIGHT - 1) {
					collision.set(x, y, 1);
					terrain.set(x, y, wallTileFor(x, y));
				}
			}
		}
		floor.set(EXIT_X, EXIT_Y, EXIT_TILE);
		return map;
	}

	private static LayerDefinition createLayer(final String name) {
		final LayerDefinition layer = new LayerDefinition(WIDTH, HEIGHT);
		layer.setName(name);
		layer.build();
		return layer;
	}

	private static int wallTileFor(final int x, final int y) {
		if (x == 0 && y == 0) {
			return 2;
		}
		if (x == WIDTH - 1 && y == 0) {
			return 3;
		}
		if (x == 0 && y == HEIGHT - 1) {
			return 4;
		}
		if (x == WIDTH - 1 && y == HEIGHT - 1) {
			return 5;
		}
		/* The four wall faces in this sheet point into the room. */
		if (x == 0) {
			return 15;
		}
		if (x == WIDTH - 1) {
			return 20;
		}
		if (y == 0) {
			return 16;
		}
		return 19;
	}

	private static void cleanupTransientEntities(final StendhalRPZone zone) {
		for (final RPObject object : zone) {
			if (object instanceof Corpse) {
				((Corpse) object).onRemoved(zone);
			}
		}
	}

	private final class HideoutMovementListener implements MovementListener {
		private final Rectangle2D area = new Rectangle2D.Double(0, 0, WIDTH, HEIGHT);

		@Override
		public Rectangle2D getArea() {
			return area;
		}

		@Override
		public void onEntered(final ActiveEntity entity, final StendhalRPZone zone,
				final int newX, final int newY) {
			// Scene is reconstructed from persistent hideout progress when created.
		}

		@Override
		public void onExited(final ActiveEntity entity, final StendhalRPZone zone,
				final int oldX, final int oldY) {
			if (!(entity instanceof Player) || zone.getPlayers().size() != 1) {
				return;
			}

			entity.put("zoneid", returnZoneName);
			entity.put("x", returnX);
			entity.put("y", returnY);

			final InstanceZoneManager manager = SingletonRepository.getRPWorld().getInstanceZoneManager();
			if (manager.isInstanceZone(zone.getID())) {
				try {
					manager.release(zone.getID(), ((Player) entity).getName());
				} catch (final Exception e) {
					logger.error("Nie udało się zwolnić instancji kryjówki " + zone.getName(), e);
				}
			} else {
				cleanupTransientEntities(zone);
				SingletonRepository.getRPWorld().removeZone(zone);
			}
		}

		@Override
		public void onMoved(final ActiveEntity entity, final StendhalRPZone zone,
				final int oldX, final int oldY, final int newX, final int newY) {
			// nothing
		}

		@Override
		public void beforeMove(final ActiveEntity entity, final StendhalRPZone zone,
				final int oldX, final int oldY, final int newX, final int newY) {
			// nothing
		}
	}
}
