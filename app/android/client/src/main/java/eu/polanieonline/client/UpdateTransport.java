/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

final class UpdateTransport {
	private UpdateTransport() { }
	static HttpURLConnection open(String url) throws IOException {
		if(!AndroidUpdate.MANIFEST_URL.equals(url) && !AndroidUpdate.safeUrl(url)) { throw new IOException("Untrusted URL"); }
		HttpURLConnection connection=(HttpURLConnection)new URL(url).openConnection();
		connection.setInstanceFollowRedirects(false); connection.setConnectTimeout(10000); connection.setReadTimeout(15000);
		connection.setUseCaches(false);
		connection.setRequestProperty("Accept-Encoding","identity"); connection.setRequestProperty("User-Agent","PolanieOnLine-Android/"+BuildConfig.VERSION_NAME);
		return connection;
	}
	static AndroidUpdate check() throws Exception {
		HttpURLConnection connection=open(AndroidUpdate.MANIFEST_URL);
		try {
			int status=connection.getResponseCode(); if(status==404) { return null; } if(status!=200) { throw new IOException("Unavailable"); }
			try(InputStream input=connection.getInputStream(); ByteArrayOutputStream output=new ByteArrayOutputStream()) {
				byte[] bytes=new byte[4096]; int n; while((n=input.read(bytes))!=-1) {
					if(output.size()+n>32768) { throw new IOException("Manifest too large"); } output.write(bytes,0,n);
				}
				return AndroidUpdate.parse(new JSONObject(new String(output.toByteArray(),StandardCharsets.UTF_8)));
			}
		} finally { connection.disconnect(); }
	}
}
