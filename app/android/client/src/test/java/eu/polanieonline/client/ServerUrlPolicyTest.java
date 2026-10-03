package eu.polanieonline.client;

import org.junit.Test;
import static org.junit.Assert.*;

public class ServerUrlPolicyTest {
	private final String production = "https://polanieonline.eu/";
	private boolean allowed(String url) { return ServerUrlPolicy.isInternal(url, production, "", false); }

	@Test public void canonicalAndLegacyProductionOriginsAreInternal() {
		assertTrue(allowed("https://polanieonline.eu/client/polanieonline.html"));
		assertTrue(allowed("https://s1.polanieonline.eu/client/polanieonline.html"));
		assertTrue(allowed("https://www.polanieonline.eu/profile"));
		assertTrue(allowed("https://POLANIEONLINE.EU:443/profile"));
	}
	@Test public void hostSubstringsCannotImpersonateTheServer() {
		assertFalse(allowed("https://polanieonline.eu.evil.example/client/polanieonline.html"));
		assertFalse(allowed("https://evil.example/?url=https://polanieonline.eu/client/polanieonline.html"));
		assertFalse(allowed("https://polanieonline.eu@evil.example/"));
		assertFalse(allowed("https://user@polanieonline.eu/"));
	}
	@Test public void productionDoesNotTrustInsecureOrDifferentPorts() {
		assertFalse(allowed("http://polanieonline.eu/client/polanieonline.html"));
		assertFalse(allowed("https://polanieonline.eu:8443/"));
		assertFalse(allowed("https://stage.polanieonline.eu/"));
	}
	@Test public void onlyTheExactOfflineAssetIsAllowed() {
		assertTrue(allowed("file:///android_asset/server_unavailable.html"));
		assertFalse(allowed("file:///sdcard/foreign.html"));
		assertFalse(allowed("javascript:alert(1)"));
		assertFalse(allowed("data:text/html,hello"));
	}
	@Test public void customServersMatchTheirWholeOrigin() {
		String custom = "https://example.test:8443/client/polanieonline.html";
		assertTrue(ServerUrlPolicy.isInternal(custom + "?char=A", production, custom, false));
		assertFalse(ServerUrlPolicy.isInternal("https://example.test/client/polanieonline.html", production, custom, false));
	}
	@Test public void customClientPageIgnoresOnlyQueryAndFragment() {
		String page = "https://example.test/client/game.html";
		assertTrue(ServerUrlPolicy.samePage(page + "?char=A#A", page));
		assertFalse(ServerUrlPolicy.samePage(page + "/extra", page));
		assertFalse(ServerUrlPolicy.samePage("https://evil.example/" + page, page));
	}
	@Test public void profilesDoNotAutomaticallyTrustEachOther() {
		assertFalse(ServerUrlPolicy.isInternal(production, "https://stage.polanieonline.eu/", "", false));
		assertTrue(ServerUrlPolicy.isInternal("https://stage.polanieonline.eu/client/polanieonline.html",
				"https://stage.polanieonline.eu/", "", false));
	}
	@Test public void localhostRequiresDebugOrExplicitConfiguration() {
		assertFalse(allowed("http://localhost:8080/"));
		assertTrue(ServerUrlPolicy.isInternal("http://localhost:8080/", production, "", true));
		assertTrue(ServerUrlPolicy.isInternal("http://localhost:8080/", production, "http://localhost:8080/client/game.html", false));
	}
	@Test public void invalidUrlsFailClosed() {
		assertFalse(allowed(null));
		assertFalse(allowed("https://bad host/"));
		assertFalse(ServerUrlPolicy.samePage(null, production));
	}
}
