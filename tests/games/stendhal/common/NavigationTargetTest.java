/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.common;

import static org.junit.Assert.*;

import org.junit.Test;

public class NavigationTargetTest {
	@Test
	public void completeRouteDistanceIsSeparateFromTheImmediateWaypoint() {
		NavigationTarget route = NavigationTarget.parse("goal", "5\t0\tmain\tCel\t145");
		assertNotNull(route);
		assertEquals(145, route.distance(0, 0), .00001);
		assertEquals(145, route.distance(1, 0), .00001);
		assertEquals("Idź na wschód", route.instruction(0, 0));
		assertEquals("Idź dalej", route.instruction(5, 0));
		assertEquals("Jesteś na miejscu", NavigationTarget.parse("goal", "5\t0\tmain\tCel\t0").instruction(5, 0));
		for (String invalid : new String[] {"", "-1", "+1", "NaN", "1.5", "2147483648"})
			assertNull(NavigationTarget.parse("goal", "5\t0\tmain\tCel\t" + invalid));
	}

	@Test
	public void instructionsExplainAllEightDirections() {
		assertEquals("Idź na północ", target(0, -10).instruction(0, 0));
		assertEquals("Idź na północny wschód", target(10, -10).instruction(0, 0));
		assertEquals("Idź na wschód", target(10, 0).instruction(0, 0));
		assertEquals("Idź na południowy wschód", target(10, 10).instruction(0, 0));
		assertEquals("Idź na południe", target(0, 10).instruction(0, 0));
		assertEquals("Idź na południowy zachód", target(-10, 10).instruction(0, 0));
		assertEquals("Idź na zachód", target(-10, 0).instruction(0, 0));
		assertEquals("Idź na północny zachód", target(-10, -10).instruction(0, 0));
	}
	@Test
	public void cardinalPositionsAndContinuousDistance() {
		assertEquals(.5, target(0, -10).position(0, 0), .00001);
		assertEquals(.25, target(-10, 0).position(0, 0), .00001);
		assertEquals(.75, target(10, 0).position(0, 0), .00001);
		assertEquals(1, target(0, 10).position(0, 0), .00001);
		assertEquals(.5, target(0, 0).position(0, 0), .00001);
		assertEquals(4.5, target(5, 0).distance(.5, 0), .00001);
		assertEquals(5, target(3, 4).distance(0, 0), .00001);
	}

	@Test
	public void invalidWireEntriesAreIgnored() {
		for (String wire :
				new String[] {
					"",
					"1\t2\tmain",
					"NaN\t2\tmain\tx",
					"1\tInfinity\tside\tx",
					"1\t2\tother\tx",
					"1\t2\tmain\t",
					"1\t2\tmain\tx\ty",
					"\t2\tmain\tx"
				}) {
			assertNull(wire, NavigationTarget.parse("goal", wire));
		}
		assertNull(NavigationTarget.parse("", "1\t2\tmain\tx"));
		assertNull(NavigationTarget.parse("goal", null));
		NavigationTarget side = NavigationTarget.parse("goal", "1\t2\tside\tŚlady");
		assertNotNull(side);
		assertFalse(side.main);
		assertEquals("Ślady", side.label);
	}

	private NavigationTarget target(double x, double y) {
		return NavigationTarget.parse("goal", x + "\t" + y + "\tmain\tCel");
	}
}
