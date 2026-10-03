/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.content.Context;
import android.content.pm.*;
import android.os.Build;
import java.io.*;
import java.util.*;

/** A website checksum alone is insufficient: the APK must match the installed app signer. */
final class UpdateVerifier {
	static void verify(Context context,File apk,AndroidUpdate update) throws Exception {
		PackageManager pm=context.getPackageManager();
		int flags=Build.VERSION.SDK_INT>=28 ? PackageManager.GET_SIGNING_CERTIFICATES : PackageManager.GET_SIGNATURES;
		PackageInfo installed=pm.getPackageInfo(context.getPackageName(),flags);
		PackageInfo candidate=pm.getPackageArchiveInfo(apk.getAbsolutePath(),flags);
		validate(installed,candidate,update);
	}
	static void validate(PackageInfo installed,PackageInfo candidate,AndroidUpdate update) throws IOException {
		if(candidate==null || !"eu.polanieonline.client".equals(candidate.packageName) || !installed.packageName.equals(candidate.packageName)
			|| code(candidate)!=update.versionCode || code(candidate)<=code(installed)
			|| !update.versionName.equals(candidate.versionName) || candidate.applicationInfo==null
			|| (candidate.applicationInfo.flags&ApplicationInfo.FLAG_DEBUGGABLE)!=0
			|| (Build.VERSION.SDK_INT>=24 && candidate.applicationInfo.minSdkVersion>Build.VERSION.SDK_INT)) { throw new IOException("Wrong APK"); }
		Set<String> current=signers(installed),next=signers(candidate);
		if(current.isEmpty() || !current.equals(next)) { throw new IOException("Wrong signer"); }
	}
	private static Set<String> signers(PackageInfo info) {
		Signature[] signatures=Build.VERSION.SDK_INT>=28 ? (info.signingInfo==null ? null : info.signingInfo.getApkContentsSigners()) : info.signatures;
		Set<String> result=new HashSet<>(); if(signatures!=null) { for(Signature signature:signatures) { result.add(signature.toCharsString()); } } return result;
	}
	static long code(PackageInfo info) { return Build.VERSION.SDK_INT>=28 ? info.getLongVersionCode() : info.versionCode; }
}
