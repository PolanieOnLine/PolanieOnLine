/***************************************************************************
 *                    (C) Copyright 2003-2026 - Marauroa                   *
 ***************************************************************************/
package games.stendhal.server.core.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import org.junit.Test;

public class EndTurnDiagnosticsTest {
	@Test
	public void testFormatsTopListenersAndZones() {
		final EndTurnDiagnostics diagnostics = new EndTurnDiagnostics();
		diagnostics.reset();
		diagnostics.recordTurnListener("listener-a", 2000000L, false);
		diagnostics.recordTurnListener("listener-b", 5000000L, true);
		diagnostics.recordTurnListener("listener-c", 3000000L, false);
		diagnostics.recordTurnListener("listener-d", 4000000L, false);
		diagnostics.recordTurnNotifierDuration(16000000L);
		diagnostics.recordZone("zone-a", 7000000L);
		diagnostics.recordZone("zone-b", 11000000L);
		diagnostics.recordZone("zone-c", 9000000L);
		diagnostics.recordZone("zone-d", 8000000L);
		diagnostics.finish(60000000L);

		final String text = diagnostics.format(42);
		assertThat(text, containsString("turn=42, elapsedMs=60, turnNotifierMs=16"));
		assertThat(text, containsString("listeners={count=4,failures=1,totalMs=14,top=[listener-b:5000us,listener-d:4000us,listener-c:3000us]}"));
		assertThat(text, containsString("zones={count=4,totalMs=35,top=[zone-b:11000us,zone-c:9000us,zone-d:8000us]}"));
	}

	@Test
	public void testResetClearsPreviousTopEntries() {
		final EndTurnDiagnostics diagnostics = new EndTurnDiagnostics();
		diagnostics.recordZone("old-zone", 9000000L);
		diagnostics.recordTurnListener("old-listener", 7000000L, false);
		diagnostics.reset();
		diagnostics.finish(1000000L);

		final String text = diagnostics.format(7);
		assertThat(text, containsString("listeners={count=0,failures=0,totalMs=0,top=[]}"));
		assertThat(text, containsString("zones={count=0,totalMs=0,top=[]}"));
		assertThat(text, not(containsString("old-zone")));
		assertThat(text, not(containsString("old-listener")));
	}
}
