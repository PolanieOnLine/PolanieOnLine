package games.stendhal.client.gui.login;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/** Exercises both desktop protocols without accessing real accounts or networks. */
public class ExternalGameLoginClientTest {
	private static final String ID = "11111111-2222-3333-4444-555555555555";
	private static final String SECRET = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

	private String startResponse(final String verificationUrl) {
		return "{\"id\":\"" + ID + "\",\"secret\":\"" + SECRET
				+ "\",\"code\":\"0123456789\",\"expires_in\":600,\"verification_url\":\"" + verificationUrl + "\"}";
	}

	@Test
	public void bothProvidersStartPollAndReceiveCredentialsThroughTheirOwnRoutes() throws Exception {
		for (final ExternalLoginProvider provider : ExternalLoginProvider.values()) {
			final FakeConnections factory = new FakeConnections();
			factory.add(201, startResponse("https://login.example/auth/external/" + provider.id + "/redirect?intent=game"));
			factory.add(202, "{\"status\":\"pending\"}");
			factory.add(200, "{\"username\":\"wojownik\",\"seed\":\"S_12345678901234\"}");
			final ExternalGameLoginClient client = new ExternalGameLoginClient("https://login.example", provider, factory);
			final ExternalGameLoginClient.StartResult request = client.start();
			assertEquals("0123456789", request.code);
			assertNull(client.poll(request));
			final ExternalGameLoginClient.Credentials credentials = client.poll(request);
			assertEquals("wojownik", credentials.username);
			assertEquals("S_12345678901234", credentials.seed);
			assertEquals(provider.apiPath() + "/start", factory.opened.get(0).getURL().getPath());
			assertEquals(provider.apiPath() + "/" + ID + "/poll", factory.opened.get(1).getURL().getPath());
			assertEquals("{\"secret\":\"" + SECRET + "\"}", factory.opened.get(1).output.toString("UTF-8"));
			for (final FakeConnection connection : factory.opened) {
				assertEquals("POST", connection.getRequestMethod());
				assertFalse(connection.getInstanceFollowRedirects());
				assertTrue(connection.disconnected);
			}
		}
	}

	@Test
	public void unsafeSitesAreRejectedBeforeAnyRequest() throws Exception {
		for (final String site : new String[] { "http://login.example", "https://user@login.example", "https://login.example?x=1", "https://login.example#x" }) {
			try {
				new ExternalGameLoginClient(site, ExternalLoginProvider.GOOGLE);
				fail(site);
			} catch (final IOException expected) { }
		}
	}

	@Test
	public void verificationCannotSwitchOriginPortOrProvider() throws Exception {
		for (final String address : new String[] { "https://other.example/auth/external/google/redirect",
				"https://login.example:444/auth/external/google/redirect",
				"https://login.example/auth/external/steam/redirect", "http://login.example/auth/external/google/redirect" }) {
			final FakeConnections factory = new FakeConnections();
			factory.add(201, startResponse(address));
			try {
				new ExternalGameLoginClient("https://login.example", ExternalLoginProvider.GOOGLE, factory).start();
				fail(address);
			} catch (final IOException expected) { }
		}
	}

	@Test
	public void unavailableGoogleShowsTheWebsiteMessage() throws Exception {
		final FakeConnections factory = new FakeConnections();
		factory.add(503, "{\"message\":\"Logowanie przez Google jest niedostępne.\"}");
		try {
			new ExternalGameLoginClient("https://login.example", ExternalLoginProvider.GOOGLE, factory).start();
			fail();
		} catch (final IOException expected) {
			assertEquals("Logowanie przez Google jest niedostępne.", expected.getMessage());
		}
	}

	@Test
	public void expiryDenialAndInvalidSeedsDoNotReturnCredentials() throws Exception {
		for (final int status : new int[] { 410, 403, 200 }) {
			final FakeConnections factory = new FakeConnections();
			factory.add(status, status == 200 ? "{\"username\":\"player\",\"seed\":\"short\"}" : "{}");
			final ExternalGameLoginClient client = new ExternalGameLoginClient("https://login.example", ExternalLoginProvider.GOOGLE, factory);
			try {
				client.poll(new ExternalGameLoginClient.StartResult(ID, SECRET, "0123456789", "", 600));
				fail();
			} catch (final IOException expected) { }
		}
	}

	private static final class FakeConnections implements ExternalGameLoginClient.ConnectionFactory {
		final List<Integer> statuses = new ArrayList<Integer>();
		final List<String> bodies = new ArrayList<String>();
		final List<FakeConnection> opened = new ArrayList<FakeConnection>();
		void add(final int status, final String body) { statuses.add(status); bodies.add(body); }
		public HttpURLConnection open(final URL url) {
			final int index = opened.size();
			final FakeConnection connection = new FakeConnection(url, statuses.get(index), bodies.get(index));
			opened.add(connection);
			return connection;
		}
	}

	private static final class FakeConnection extends HttpURLConnection {
		final ByteArrayOutputStream output = new ByteArrayOutputStream();
		final int status;
		final String body;
		boolean disconnected;
		FakeConnection(final URL url, final int status, final String body) {
			super(url); this.status = status; this.body = body;
		}
		public void connect() { }
		public void disconnect() { disconnected = true; }
		public boolean usingProxy() { return false; }
		public int getResponseCode() { return status; }
		public OutputStream getOutputStream() { return output; }
		public InputStream getInputStream() { return new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)); }
		public InputStream getErrorStream() { return getInputStream(); }
	}
}
