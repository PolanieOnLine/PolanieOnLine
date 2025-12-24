/***************************************************************************
 *                     Copyright © 2025 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package eu.polanieonline.client;

/**
 * Represents a target server profile.
 */
public enum ServerProfile {
	/** Production environment. */
	PROD("Prod", "https://s1.polanieonline.eu/", "client", false, false),
	/** Staging environment. */
	STAGE("Stage", "https://stage.polanieonline.eu/", "client", false, true),
	/** Test environment. */
	TEST("Test", "https://test.polanieonline.eu/", "testclient", true, true);

	private final String label;
	private final String serverBase;
	private final String clientSuffix;
	private final boolean testClient;
	private final boolean testServer;

	ServerProfile(final String label, final String serverBase, final String clientSuffix, final boolean testClient,
			final boolean testServer) {
		this.label = label;
		this.serverBase = serverBase;
		this.clientSuffix = clientSuffix;
		this.testClient = testClient;
		this.testServer = testServer;
	}

	public String getLabel() {
		return label;
	}

	public String getServerBase() {
		return serverBase;
	}

	public String getClientSuffix() {
		return clientSuffix;
	}

	public boolean isTestClient() {
		return testClient;
	}

	public boolean isTestServer() {
		return testServer;
	}
}