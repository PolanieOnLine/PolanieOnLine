/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.config.zone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import org.junit.Test;

import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.common.tiled.StendhalMapStructure;
import games.stendhal.server.core.config.zone.ZoneMapUpdater.MapUpdatePlan;

public class ZoneMapUpdaterTest {
	@Test
	public void testEmptyClientLayerCanBeSerialized() throws Exception {
		final LayerDefinition layer = ZoneMapUpdater.createEmptyClientLayer(
				"4_roof_add", 64, 32);

		assertEquals("4_roof_add", layer.getName());
		assertEquals(64, layer.getWidth());
		assertEquals(32, layer.getHeight());

		final byte[] encoded = layer.encode();
		assertTrue(encoded.length > 0);

		final LayerDefinition decoded = LayerDefinition.decode(
				new ByteArrayInputStream(encoded));
		assertEquals("4_roof_add", decoded.getName());
		assertEquals(64, decoded.getWidth());
		assertEquals(32, decoded.getHeight());
	}

	@Test
	public void testIdenticalMapsProduceEmptyUpdatePlan() throws Exception {
		final MapUpdatePlan plan = ZoneMapUpdater.prepareMapUpdate(
				createMap(0), createMap(0), "normal.tmx", "christmas.tmx");
		assertTrue(plan.isEmpty());
	}

	@Test
	public void testChangedFloorProducesUpdatePlan() throws Exception {
		final MapUpdatePlan plan = ZoneMapUpdater.prepareMapUpdate(
				createMap(0), createMap(1), "normal.tmx", "christmas.tmx");
		assertFalse(plan.isEmpty());
	}

	private static StendhalMapStructure createMap(final int floorTile) {
		final StendhalMapStructure map = new StendhalMapStructure(2, 2);
		for (final String name : new String[] {
				"0_floor", "1_terrain", "2_object", "objects", "collision", "protection"}) {
			final LayerDefinition layer = new LayerDefinition(2, 2);
			layer.setName(name);
			if ("0_floor".equals(name)) {
				layer.set(0, 0, floorTile);
			}
			map.addLayer(layer);
		}
		return map;
	}
}
