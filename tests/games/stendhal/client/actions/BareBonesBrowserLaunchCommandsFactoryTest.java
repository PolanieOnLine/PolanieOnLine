package games.stendhal.client.actions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.Test;

public class BareBonesBrowserLaunchCommandsFactoryTest {
	@Test
	public void commandsUseCurrentWebsiteSections() throws Exception {
		final Map<String, SlashAction> commands = BareBonesBrowserLaunchCommandsFactory.createBrowserCommands();
		assertUrl(commands, "beginnersguide", "/forum/wiedza?type=guide");
		assertUrl(commands, "manual", "/forum/wiedza?type=guide");
		assertUrl(commands, "faq", "/faq");
		assertUrl(commands, "changepassword", "/profile?section=settings");
		assertUrl(commands, "loginhistory", "/profile?section=settings");
		assertUrl(commands, "merge", "/profile?section=settings");
		assertUrl(commands, "halloffame", "/aleja-slaw");
	}

	private void assertUrl(final Map<String, SlashAction> commands, final String command,
			final String path) throws Exception {
		assertTrue(commands.containsKey(command));
		final Field url = BareBonesBrowserLaunchCommand.class.getDeclaredField("urlToOpen");
		url.setAccessible(true);
		assertEquals("https://polanieonline.eu" + path, url.get(commands.get(command)));
	}
}
