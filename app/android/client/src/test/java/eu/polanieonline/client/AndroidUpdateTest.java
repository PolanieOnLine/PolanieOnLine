package eu.polanieonline.client;

import android.content.pm.*;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.io.*;
import java.security.MessageDigest;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=23)
public class AndroidUpdateTest {
	private JSONObject manifest() throws Exception {
		return new JSONObject().put("enabled",true).put("schema",1).put("package_name","eu.polanieonline.client")
			.put("version_code",1042002).put("version_name","1.42").put("min_sdk",21).put("size_bytes",3)
			.put("apk_url","https://polanieonline.eu/client/android/polanieonline-1042002.apk")
			.put("sha256",AndroidUpdate.hex(MessageDigest.getInstance("SHA-256").digest(new byte[]{1,2,3}))).put("notes","Zmiany");
	}
	private void invalid(String key,Object value) throws Exception {
		try { AndroidUpdate.parse(manifest().put(key,value)); fail("Accepted "+key); } catch(IOException expected) { }
	}
	@Test public void acceptsNewerCompatibleRelease() throws Exception {
		AndroidUpdate update=AndroidUpdate.parse(manifest()); assertTrue(update.newerThan(1042001,23));
		assertFalse(update.newerThan(1042002,23)); assertFalse(update.newerThan(1042003,23)); assertFalse(update.newerThan(1042001,19));
	}
	@Test public void publicationMayBeDisabled() throws Exception { assertNull(AndroidUpdate.parse(new JSONObject().put("enabled",false))); }
	@Test public void rejectsWrongSchemaAndPackage() throws Exception { invalid("schema",2); invalid("package_name","eu.other.client"); }
	@Test public void rejectsInvalidVersionsAndSize() throws Exception { invalid("version_code",0); invalid("version_code",2147483647); invalid("size_bytes",0); invalid("size_bytes",AndroidUpdate.MAX_BYTES+1); invalid("min_sdk",1); }
	@Test public void rejectsUntrustedUrls() throws Exception {
		for(String url:new String[]{"http://polanieonline.eu/client/android/a.apk","https://evil.eu/client/android/a.apk",
			"https://polanieonline.eu:443/client/android/a.apk","https://x@polanieonline.eu/client/android/a.apk",
			"https://polanieonline.eu/client/android/a.apk?redirect=x","https://polanieonline.eu/client/android/a.apk#x",
			"https://polanieonline.eu/client/android/../a.apk","https://polanieonline.eu/client/android/%61.apk"}) { invalid("apk_url",url); }
	}
	@Test public void rejectsMalformedHash() throws Exception { invalid("sha256","bad"); invalid("sha256","A".repeat(64)); }
	@Test public void verifiesStreamAndProgress() throws Exception {
		ByteArrayOutputStream out=new ByteArrayOutputStream(); long[] progress={0};
		AndroidUpdate.copyVerified(new ByteArrayInputStream(new byte[]{1,2,3}),out,AndroidUpdate.parse(manifest()),()->false,count->progress[0]=count);
		assertArrayEquals(new byte[]{1,2,3},out.toByteArray()); assertEquals(3,progress[0]);
	}
	private void rejectsStream(byte[] bytes,boolean cancel) throws Exception {
		try { AndroidUpdate.copyVerified(new ByteArrayInputStream(bytes),new ByteArrayOutputStream(),AndroidUpdate.parse(manifest()),()->cancel,count->{}); fail("Accepted stream"); }
		catch(IOException expected) { }
	}
	@Test public void rejectsTruncatedFile() throws Exception { rejectsStream(new byte[]{1,2},false); }
	@Test public void rejectsOversizedFile() throws Exception { rejectsStream(new byte[]{1,2,3,4},false); }
	@Test public void rejectsBadChecksum() throws Exception { rejectsStream(new byte[]{3,2,1},false); }
	@Test public void cancellationStopsCopy() throws Exception { rejectsStream(new byte[]{1,2,3},true); }
	private PackageInfo apk(int code,String cert) {
		PackageInfo info=new PackageInfo(); info.packageName="eu.polanieonline.client"; info.versionCode=code; info.versionName="1.42";
		info.applicationInfo=new ApplicationInfo(); info.signatures=new Signature[]{new Signature(cert)}; return info;
	}
	private void rejectsApk(PackageInfo candidate) throws Exception {
		try { UpdateVerifier.validate(apk(1042001,"abcd"),candidate,AndroidUpdate.parse(manifest())); fail("Accepted APK"); } catch(IOException expected) { }
	}
	@Test public void acceptsMatchingSignerAndVersion() throws Exception { UpdateVerifier.validate(apk(1042001,"abcd"),apk(1042002,"abcd"),AndroidUpdate.parse(manifest())); }
	@Test public void rejectsDifferentSigningKey() throws Exception { rejectsApk(apk(1042002,"1234")); }
	@Test public void rejectsMissingSignature() throws Exception { PackageInfo info=apk(1042002,"abcd"); info.signatures=null; rejectsApk(info); }
	@Test public void rejectsWrongPackage() throws Exception { PackageInfo info=apk(1042002,"abcd"); info.packageName="evil.app"; rejectsApk(info); }
	@Test public void rejectsDebugApk() throws Exception { PackageInfo info=apk(1042002,"abcd"); info.applicationInfo.flags=ApplicationInfo.FLAG_DEBUGGABLE; rejectsApk(info); }
	@Test public void rejectsUnexpectedVersion() throws Exception { rejectsApk(apk(1042001,"abcd")); rejectsApk(apk(1042003,"abcd")); }
	@Test public void rejectsCorruptArchive() throws Exception { rejectsApk(null); }
	@Test @Config(sdk=28) public void modernAndroidChecksCurrentSigner() throws Exception {
		PackageInfo installed=apk(1042001,"abcd"),candidate=apk(1042002,"abcd");
		for(PackageInfo info:new PackageInfo[]{installed,candidate}) {
			info.signingInfo=org.robolectric.util.ReflectionHelpers.callConstructor(SigningInfo.class);
			org.robolectric.Shadows.shadowOf(info.signingInfo).setSignatures(info.signatures);
		}
		UpdateVerifier.validate(installed,candidate,AndroidUpdate.parse(manifest()));
		org.robolectric.Shadows.shadowOf(candidate.signingInfo).setSignatures(new Signature[]{new Signature("1234")});
		try { UpdateVerifier.validate(installed,candidate,AndroidUpdate.parse(manifest())); fail("Accepted new signer"); } catch(IOException expected) { }
	}
}
