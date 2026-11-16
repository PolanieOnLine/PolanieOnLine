/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.krakow.ship.KrakowFerry;
import games.stendhal.server.maps.gdansk.ship.GdanskFerry;
import utilities.PlayerTestHelper;
import utilities.QuestHelper;

/** Exercise disembarking against the actual port maps, including collision. */
public class WarsawFerryArrivalTest {
	private static StendhalRPZone warsaw;
	private static StendhalRPZone krakow;
	private static StendhalRPZone gdansk;

	@BeforeClass
	public static void initialize() throws Exception {
		QuestHelper.setUpBeforeClass();
		warsaw = load("0_warszawa_c", "Level 0/warszawa/c.tmx");
		load("0_warszawa_ne", "Level 0/warszawa/ne.tmx");
		krakow = load("0_krakow_wisla_e", "Level 0/krakow/wisla_e.tmx");
		gdansk = load("0_gdansk_city_nw", "Level 0/gdansk/city_nw.tmx");
	}

	@Test
	public void krakowFerryDisembarksAtBothPorts() {
		final StendhalRPZone ship = new StendhalRPZone("krakow_arrival_test", 128, 128);
		MockStendlRPWorld.get().addRPZone(ship);
		SingletonRepository.getNPCList().remove("Maryna");
		new games.stendhal.server.maps.krakow.ship.CoastConveyerNPC().configureZone(ship, Collections.emptyMap());
		final SpeakerNPC npc = SingletonRepository.getNPCList().get("Maryna");
		final KrakowFerry ferry = SingletonRepository.getKrakowFerry();
		do {
			ferry.onTurnReached(0);
		} while (ferry.getState() != KrakowFerry.Status.ANCHORED_AT_WARSZAWA);
		arrive(ship, npc, warsaw, 3, 63, "krakow_to_warsaw");
		do {
			ferry.onTurnReached(0);
		} while (ferry.getState() != KrakowFerry.Status.ANCHORED_AT_KRAKOW);
		arrive(ship, npc, krakow, 12, 94, "warsaw_to_krakow");
		SingletonRepository.getNPCList().remove("Maryna");
	}

	@Test
	public void gdanskFerryDisembarksAtBothPorts() {
		final StendhalRPZone ship = new StendhalRPZone("gdansk_arrival_test", 128, 128);
		MockStendlRPWorld.get().addRPZone(ship);
		SingletonRepository.getNPCList().remove("Basia");
		new games.stendhal.server.maps.gdansk.ship.CoastConveyerNPC().configureZone(ship, Collections.emptyMap());
		final SpeakerNPC npc = SingletonRepository.getNPCList().get("Basia");
		final GdanskFerry ferry = SingletonRepository.getGdanskFerry();
		do {
			ferry.onTurnReached(0);
		} while (ferry.getState() != GdanskFerry.Status.ANCHORED_AT_WARSZAWA);
		arrive(ship, npc, warsaw, 24, 92, "gdansk_to_warsaw");
		do {
			ferry.onTurnReached(0);
		} while (ferry.getState() != GdanskFerry.Status.ANCHORED_AT_GDANSK);
		arrive(ship, npc, gdansk, 71, 37, "warsaw_to_gdansk");
		SingletonRepository.getNPCList().remove("Basia");
	}

	private void arrive(final StendhalRPZone ship, final SpeakerNPC npc,
			final StendhalRPZone destination, final int x, final int y, final String name) {
		final Player player = PlayerTestHelper.createPlayer(name);
		ship.add(player);
		player.setPosition(27, 34);
		try {
			npc.getEngine().stepTest(player, "hi");
			npc.getEngine().stepTest(player, "zejdź");
			npc.getEngine().stepTest(player, "tak");
			assertSame("Disembarking must reach the map containing the port", destination, player.getZone());
			assertEquals(x, player.getX());
			assertEquals(y, player.getY());
		} finally {
			player.getZone().remove(player);
		}
	}

	private static StendhalRPZone load(final String id, final String map) throws Exception {
		final StendhalRPZone zone = new StendhalRPZone(id);
		zone.addCollisionLayer(id + ".collision", TMXLoader.load("data/maps/" + map).getLayer("collision"));
		MockStendlRPWorld.get().addRPZone(zone);
		return zone;
	}
}
