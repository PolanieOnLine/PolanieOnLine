/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.content.Context;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import org.json.*;

/** Only public website endpoints. No cookies or game credentials are sent. */
final class SiteData {
	static final ExecutorService IO = Executors.newFixedThreadPool(2);
	private SiteData() { }
	static JSONObject fetch(String path) throws IOException, JSONException {
		if (!path.matches("/api/v1/site/(home|calendar(?:\\?month=20[0-9]{2}-(?:0[1-9]|1[0-2]))?)")) { throw new IOException("Invalid endpoint"); }
		HttpURLConnection connection = (HttpURLConnection)new URL("https://polanieonline.eu"+path).openConnection();
		connection.setConnectTimeout(6000); connection.setReadTimeout(6000); connection.setInstanceFollowRedirects(false);
		connection.setRequestProperty("Accept", "application/json"); connection.setRequestProperty("User-Agent", "PolanieOnLine-Android/"+BuildConfig.VERSION_NAME);
		try {
			if (connection.getResponseCode()!=200) { throw new IOException("Site unavailable"); }
			try (InputStream input=connection.getInputStream(); ByteArrayOutputStream output=new ByteArrayOutputStream()) {
				byte[] buffer=new byte[8192]; int count;
				while ((count=input.read(buffer))!=-1) { if (output.size()+count>1024*1024) { throw new IOException("Response too large"); } output.write(buffer,0,count); }
				return new JSONObject(new String(output.toByteArray(),StandardCharsets.UTF_8));
			}
		} finally { connection.disconnect(); }
	}
	static JSONObject cached(Context context, String key) {
		try { return new JSONObject(context.getSharedPreferences("public_site_cache",0).getString(key,"{}")); }
		catch (JSONException e) { return new JSONObject(); }
	}
	static void cache(Context context,String key,JSONObject value) { context.getSharedPreferences("public_site_cache",0).edit().putString(key,value.toString()).apply(); }
	static List<CalendarEntry> events(JSONObject value, boolean includeUpcoming) {
		Map<Integer,CalendarEntry> entries=new LinkedHashMap<>();
		for(String key: includeUpcoming ? new String[]{"events","upcoming"} : new String[]{"events"}) {
			JSONArray list=value.optJSONArray(key); if(list==null) { continue; }
			for(int i=0;i<list.length();i++) { try { CalendarEntry event=new CalendarEntry(list.getJSONObject(i));
				if ("published".equals(event.state)||"cancelled".equals(event.state)) { entries.put(event.id,event); }
			} catch(JSONException|RuntimeException ignored) { } }
		}
		List<CalendarEntry> result=new ArrayList<>(entries.values()); result.sort(Comparator.comparing(e->e.start)); return result;
	}
}
