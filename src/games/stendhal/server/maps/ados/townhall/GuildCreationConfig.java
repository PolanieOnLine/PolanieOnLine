package games.stendhal.server.maps.ados.townhall;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import games.stendhal.server.core.engine.guild.GuildService.RequiredItem;

/**
 * Tunable guild creation costs for Guild Master NPC.
 */
public final class GuildCreationConfig {
	public static final int GOLD_FEE = 25000;
	public static final List<RequiredItem> REQUIRED_ITEMS = Collections.unmodifiableList(Arrays.asList(
			new RequiredItem("sztabka złota", 5),
			new RequiredItem("zbroja elficka", 1)));

	private GuildCreationConfig() {
		// utility class
	}
}
