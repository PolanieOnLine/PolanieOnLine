/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.engine;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Test;

import marauroa.common.Configuration;
import marauroa.common.ConfigurationParams;

public class StendhalRPRuleProcessorWelcomeTest {
	@Test
	public void literalWelcomeIsResolvedWithoutExternalIo() throws Exception {
		final ConfigurationParams params = new ConfigurationParams();
		params.setPersistence(false);
		final Configuration config = new Configuration(params);
		config.set("server_welcome", "Wiadomość testowa");

		final Method method = StendhalRPRuleProcessor.class.getDeclaredMethod(
				"resolveWelcomeMessage", Configuration.class);
		method.setAccessible(true);

		assertEquals("Wiadomość testowa", method.invoke(null, config));
	}
}
