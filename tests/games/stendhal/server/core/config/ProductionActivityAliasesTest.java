package games.stendhal.server.core.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.not;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ProductionActivityAliasesTest {
	@Test
	public void brewingGetsBrewingWordsOnly() {
		final List<String> activities = ProductionActivityAliases.expand(Arrays.asList("brew", "nawarz"));

		assertThat(activities, hasItems("brew", "nawarz", "warz", "wywarz", "uwarz"));
		assertThat(activities, not(hasItems("szyj", "wytop")));
	}

	@Test
	public void mixingGetsNaturalPolishCommands() {
		final List<String> activities = ProductionActivityAliases.expand(Arrays.asList("mix", "wymieszaj", "zrób"));

		assertThat(activities, hasItems("mix", "wymieszaj", "mieszaj", "zmieszaj"));
	}

	@Test
	public void productionKindsDoNotShareUnrelatedAliases() {
		final List<String> smith = ProductionActivityAliases.expand(Arrays.asList("cast", "odlej"));
		final List<String> tailor = ProductionActivityAliases.expand(Arrays.asList("sew", "uszyj"));
		final List<String> baker = ProductionActivityAliases.expand(Arrays.asList("bake", "upiecz"));

		assertThat(smith, hasItems("wytop", "przetop"));
		assertThat(smith, not(hasItems("szyj", "piecz")));
		assertThat(tailor, hasItems("szyj"));
		assertThat(baker, hasItems("piecz"));
	}

	@Test
	public void genericMakeKeepsUsefulButNeutralAliases() {
		final List<String> activities = ProductionActivityAliases.expand(Arrays.asList("make", "zrób"));

		assertThat(activities, hasItems("make", "zrób", "wykonaj", "przygotuj"));
		assertThat(activities, not(hasItems("warz", "szyj", "wytop")));
	}
}
