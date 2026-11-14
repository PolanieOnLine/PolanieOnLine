package games.stendhal.common;

/** Stable door placement: day, column, row, column span, row span. */
public final class AdventCalendarLayout {

	private static final int[][] DOORS = {
		{12, 0, 0, 2, 1}, {3, 2, 0, 1, 1}, {19, 3, 0, 1, 1}, {7, 4, 0, 1, 1}, {21, 5, 0, 1, 1},
		{1, 0, 1, 1, 1}, {15, 1, 1, 1, 1}, {9, 2, 1, 1, 2}, {22, 3, 1, 1, 1}, {5, 4, 1, 1, 1}, {18, 5, 1, 1, 1},
		{4, 0, 2, 1, 1}, {13, 1, 2, 1, 1}, {2, 3, 2, 1, 1}, {20, 4, 2, 2, 1},
		{10, 0, 3, 1, 1}, {23, 1, 3, 1, 1}, {6, 2, 3, 1, 1}, {16, 3, 3, 1, 1}, {24, 4, 3, 2, 2},
		{8, 0, 4, 1, 1}, {14, 1, 4, 1, 1}, {17, 2, 4, 1, 1}, {11, 3, 4, 1, 1}
	};

	private AdventCalendarLayout() {
	}

	public static int[][] doors() {
		final int[][] copy = new int[DOORS.length][];
		for (int i = 0; i < DOORS.length; i++) {
			copy[i] = DOORS[i].clone();
		}
		return copy;
	}
}
