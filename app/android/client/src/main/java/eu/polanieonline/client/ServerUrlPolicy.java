/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import java.net.URI;
import java.net.URISyntaxException;

/** Exact origin checks before navigation or credential submission. */
final class ServerUrlPolicy {
	private ServerUrlPolicy() { }

	static boolean isInternal(String url, String server, String customClient, boolean debug) {
		if ("file:///android_asset/server_unavailable.html".equals(url)) {
			return true;
		}
		URI target = parse(url);
		URI base = parse(server);
		if (!isWeb(target)) {
			return false;
		}
		if (sameOrigin(target, base) || sameOrigin(target, parse(customClient))) {
			return true;
		}
		if (isProduction(base) && isProduction(target)) {
			return true;
		}
		return debug && ("localhost".equals(target.getHost()) || "127.0.0.1".equals(target.getHost()));
	}

	static boolean samePage(String url, String configured) {
		URI target = parse(url);
		URI base = parse(configured);
		return sameOrigin(target, base) && target.getPath().equals(base.getPath());
	}

	private static boolean isProduction(URI uri) {
		if (!isWeb(uri) || !"https".equalsIgnoreCase(uri.getScheme()) || port(uri) != 443) {
			return false;
		}
		String host = uri.getHost();
		return "polanieonline.eu".equalsIgnoreCase(host) || "s1.polanieonline.eu".equalsIgnoreCase(host)
				|| "www.polanieonline.eu".equalsIgnoreCase(host);
	}

	private static boolean sameOrigin(URI a, URI b) {
		return isWeb(a) && isWeb(b) && a.getScheme().equalsIgnoreCase(b.getScheme())
				&& a.getHost().equalsIgnoreCase(b.getHost()) && port(a) == port(b);
	}

	private static boolean isWeb(URI uri) {
		return uri != null && uri.getHost() != null && uri.getUserInfo() == null
				&& ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()));
	}

	private static int port(URI uri) {
		return uri.getPort() >= 0 ? uri.getPort() : ("https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80);
	}

	private static URI parse(String value) {
		try {
			return value == null || value.isEmpty() ? null : new URI(value);
		} catch (URISyntaxException e) {
			return null;
		}
	}
}
