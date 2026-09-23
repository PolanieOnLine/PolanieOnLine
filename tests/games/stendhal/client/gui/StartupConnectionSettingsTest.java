package games.stendhal.client.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StartupConnectionSettingsTest {
	@Test
	public void acceptsValidPorts() {
		assertEquals(1, StartupConnectionSettings.parsePort("1"));
		assertEquals(32160, StartupConnectionSettings.parsePort(" 32160 "));
		assertEquals(65535, StartupConnectionSettings.parsePort("65535"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsOutOfRangePort() {
		StartupConnectionSettings.parsePort("65536");
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNonnumericPort() {
		StartupConnectionSettings.parsePort("abc");
	}
}
