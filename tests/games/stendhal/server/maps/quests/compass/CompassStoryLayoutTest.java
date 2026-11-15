package games.stendhal.server.maps.quests.compass;

import static org.junit.Assert.*;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.common.tiled.StendhalMapStructure;
import games.stendhal.server.core.config.zone.TMXLoader;
import games.stendhal.server.maps.quests.compass.CompassScenes.Clue;

/** Story props must fit the real terrain and require exploration rather than one row of clicks. */
public class CompassStoryLayoutTest {
	@Test
	public void theInvestigationAndCalibrationAreSpreadAcrossTheClearing() {
		assertSpread(new Clue[] {Clue.TRACKS, Clue.BAG, Clue.MARKS, Clue.NOTES});
		assertSpread(new Clue[] {Clue.CROSS_MOSS, Clue.CROSS_STUMP, Clue.CROSS_WATER});
		Clue a = Clue.CROSS_MOSS, b = Clue.CROSS_STUMP, c = Clue.CROSS_WATER;
		assertNotEquals("Calibration must not be a straight row of adjacent props",
				0, (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x));
	}

	private void assertSpread(Clue[] clues) {
		for (int i = 1; i < clues.length; i++) {
			int distance = Math.abs(clues[i].x - clues[i - 1].x) + Math.abs(clues[i].y - clues[i - 1].y);
			assertTrue("The next clue should require following the trail: " + clues[i], distance >= 30);
		}
	}

	@Test
	public void theRescueBeginsOutsideTheViewAroundTheLastClue() {
		for (int role = 0; role < 2; role++) {
			assertTrue("A wolf must approach from beyond the preceding clue",
					Math.max(Math.abs(CompassGuard.X[role] - Clue.STUMP.x),
							Math.abs(CompassGuard.Y[role] - Clue.STUMP.y)) >= 20);
		}
		assertTrue(Math.max(Math.abs(68 - Clue.STUMP.x), Math.abs(86 - Clue.STUMP.y)) >= 20);
	}

	@Test
	public void everyPropIsWalkableAndVisibleAndForestCluesShareTheReachableClearing() throws Exception {
		Map<String, StendhalMapStructure> maps = new HashMap<>();
		for (Clue clue : Clue.values()) {
			StendhalMapStructure map = maps.get(clue.zone);
			if (map == null) {
				String path = "data/maps/Level 0/" + clue.zone.substring(2).replaceFirst("_", "/") + ".tmx";
				map = TMXLoader.load(getClass().getClassLoader().getResource(path).toExternalForm());
				maps.put(clue.zone, map);
			}
			for (String name : new String[] {"collision", "3_roof", "4_roof_add"}) {
				LayerDefinition layer = map.getLayer(name);
				layer.build();
				assertEquals(clue + " must not be hidden under trees or placed in a wall (" + name + ")",
						0, layer.getTileAt(clue.x, clue.y));
			}
		}
		LayerDefinition collision = maps.get(CompassProgress.FOREST).getLayer("collision");
		int width = collision.getWidth(), height = collision.getHeight();
		boolean[] reached = new boolean[width * height];
		ArrayDeque<Integer> queue = new ArrayDeque<>();
		reached[86 * width + 68] = true;
		queue.add(86 * width + 68);
		while (!queue.isEmpty()) {
			int position = queue.remove();
			int x = position % width, y = position / width;
			for (int[] delta : new int[][] {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}) {
				int nextX = x + delta[0], nextY = y + delta[1];
				if (nextX < 0 || nextY < 0 || nextX >= width || nextY >= height) continue;
				int next = nextY * width + nextX;
				if (!reached[next] && collision.getTileAt(nextX, nextY) == 0) {
					reached[next] = true;
					queue.add(next);
				}
			}
		}
		for (Clue clue : Clue.values()) {
			if (CompassProgress.FOREST.equals(clue.zone)) {
				assertTrue(clue + " must be reachable without crossing the forest's blocked southern band",
						reached[clue.y * width + clue.x]);
			}
		}
	}
}
