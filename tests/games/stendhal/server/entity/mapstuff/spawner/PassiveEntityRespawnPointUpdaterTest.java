/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.spawner;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.RPClass.PassiveEntityRespawnPointTestHelper;

/**
 * Tests runtime updates of passive item objects originating from TMX maps.
 */
public class PassiveEntityRespawnPointUpdaterTest {
	private static final String HERB = "../../tileset/logic/item/herb.png";
	private static final String SIGN = "../../tileset/logic/item/sign.png";

	@BeforeClass
	public static void beforeClass() {
		MockStendlRPWorld.get();
		PassiveEntityRespawnPointTestHelper.generateRPClasses();
	}

	@Test
	public void signIsIgnoredLikeDuringNormalZonePopulation() {
		final StendhalRPZone zone = new StendhalRPZone("test_sign", 100, 100);

		PassiveEntityRespawnPointUpdater.validateMapObject(SIGN, 0, 61, 43);
		PassiveEntityRespawnPointUpdater.addMapSpawner(zone, SIGN, 0, 61, 43);
		PassiveEntityRespawnPointUpdater.removeMapSpawner(zone, SIGN, 0, 61, 43);

		assertEquals(0, countGrowers(zone, 61, 43));
	}

	@Test
	public void addAndRemoveAreIdempotentForReindeerMoss() {
		final StendhalRPZone zone = new StendhalRPZone("test_moss", 100, 100);

		PassiveEntityRespawnPointUpdater.addMapSpawner(zone, HERB, 4, 40, 1);
		PassiveEntityRespawnPointUpdater.addMapSpawner(zone, HERB, 4, 40, 1);
		assertEquals(1, countGrowers(zone, 40, 1));

		PassiveEntityRespawnPointUpdater.removeMapSpawner(zone, HERB, 4, 40, 1);
		PassiveEntityRespawnPointUpdater.removeMapSpawner(zone, HERB, 4, 40, 1);
		assertEquals(0, countGrowers(zone, 40, 1));
	}

	private static int countGrowers(final StendhalRPZone zone,
			final int x, final int y) {
		int count = 0;
		for (final Entity entity : zone.getEntitiesAt(x, y)) {
			if (entity instanceof PassiveEntityRespawnPoint) {
				count++;
			}
		}
		return count;
	}
}
