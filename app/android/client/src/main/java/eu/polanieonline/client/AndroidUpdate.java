/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import java.net.URI;
import java.io.*;
import java.security.MessageDigest;
import java.util.function.*;
import org.json.*;

/** Strict public release metadata. It never carries accounts, sessions or arbitrary download URLs. */
final class AndroidUpdate {
	static final String MANIFEST_URL="https://polanieonline.eu/client/android/update.json";
	static final long MAX_BYTES=150L*1024*1024;
	final int versionCode,minSdk;
	final long size;
	final String versionName,url,sha256,notes;
	private AndroidUpdate(JSONObject json) throws Exception {
		if(json.getInt("schema")!=1 || !"eu.polanieonline.client".equals(json.getString("package_name"))) { throw new IOException("Invalid manifest"); }
		versionCode=json.getInt("version_code"); minSdk=json.getInt("min_sdk"); size=json.getLong("size_bytes");
		versionName=json.getString("version_name"); url=json.getString("apk_url"); sha256=json.getString("sha256"); notes=json.optString("notes","");
		if(versionCode<=0 || versionCode>2100000000 || minSdk<21 || minSdk>100 || size<=0 || size>MAX_BYTES || versionName.isEmpty() || versionName.length()>60
				|| notes.length()>12000 || !sha256.matches("[a-f0-9]{64}") || !safeUrl(url)) { throw new IOException("Invalid manifest values"); }
	}
	static AndroidUpdate parse(JSONObject json) throws Exception {
		if(!json.getBoolean("enabled")) { return null; }
		return new AndroidUpdate(json);
	}
	boolean newerThan(long installed,int sdk) { return versionCode>installed && minSdk<=sdk; }
	static boolean safeUrl(String value) {
		try {
			URI uri=new URI(value);
			return "https".equals(uri.getScheme()) && "polanieonline.eu".equals(uri.getHost()) && uri.getPort()==-1
				&& uri.getRawUserInfo()==null && uri.getRawQuery()==null && uri.getRawFragment()==null
				&& uri.getRawPath().matches("/client/android/[A-Za-z0-9_-]+\\.apk");
		} catch(Exception e) { return false; }
	}
	/** Streaming validation also handles unknown HTTP length and cancellation. */
	static void copyVerified(InputStream input,OutputStream output,AndroidUpdate update,BooleanSupplier cancelled,LongConsumer progress) throws Exception {
		MessageDigest digest=MessageDigest.getInstance("SHA-256"); byte[] buffer=new byte[32768]; long total=0; int count;
		while((count=input.read(buffer))!=-1) {
			if(cancelled.getAsBoolean() || Thread.currentThread().isInterrupted()) { throw new InterruptedIOException("Cancelled"); }
			total+=count; if(total>update.size || total>MAX_BYTES) { throw new IOException("Unexpected size"); }
			digest.update(buffer,0,count); output.write(buffer,0,count); progress.accept(total);
		}
		if(cancelled.getAsBoolean() || total!=update.size || !hex(digest.digest()).equals(update.sha256)) { throw new IOException("Invalid download"); }
	}
	static String hex(byte[] bytes) { StringBuilder value=new StringBuilder(); for(byte b:bytes) { value.append(String.format(java.util.Locale.ROOT,"%02x",b&255)); } return value.toString(); }
}
