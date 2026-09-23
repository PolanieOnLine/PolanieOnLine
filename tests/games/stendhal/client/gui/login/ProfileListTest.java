package games.stendhal.client.gui.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class ProfileListTest {
	@Test
	public void savedProfilesDoNotContainPasswords() throws Exception {
		Profile profile = new Profile();
		profile.setHost("example.org");
		profile.setPort(32160);
		profile.setUser("hero");
		profile.setPassword("private-password");
		ProfileList profiles = new ProfileList();
		profiles.add(profile);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		profiles.save(output);

		ProfileList restored = new ProfileList();
		assertFalse(restored.load(new ByteArrayInputStream(output.toByteArray())));
		Profile saved = restored.iterator().next();
		assertEquals("hero", saved.getUser());
		assertEquals("example.org", saved.getHost());
		assertEquals(32160, saved.getPort());
		assertEquals("", saved.getPassword());
	}

	@Test
	public void legacyPasswordIsRemovedOnLoad() throws Exception {
		String legacy = new Encoder().encode("example.org\nhero\nprivate-password\n32160\ntrue") + "\n";
		ProfileList profiles = new ProfileList();

		assertTrue(profiles.load(new ByteArrayInputStream(legacy.getBytes(StandardCharsets.UTF_8))));
		assertEquals("", profiles.iterator().next().getPassword());
	}
}
