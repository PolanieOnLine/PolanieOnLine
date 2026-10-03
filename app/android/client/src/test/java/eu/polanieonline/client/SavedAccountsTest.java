package eu.polanieonline.client;
import static org.junit.Assert.*;
import android.content.SharedPreferences;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class) @Config(sdk=29,manifest=Config.NONE)
public class SavedAccountsTest {
	@Test public void checkedRememberPersistsAndUpdatesOneAccountWithoutDuplicates() throws Exception {
		SharedPreferences prefs=RuntimeEnvironment.getApplication().getSharedPreferences("remember-save",0);
		assertTrue(CredentialsStore.save(prefs," First ","synthetic-one"));
		assertTrue(CredentialsStore.save(prefs,"Second","synthetic-two"));
		assertTrue(CredentialsStore.save(prefs,"FIRST ","synthetic-updated"));
		JSONArray accounts=new JSONArray(prefs.getString("credentials",""));
		assertEquals(2,accounts.length()); assertEquals("FIRST",accounts.getJSONObject(0).getString("username"));
		assertEquals("synthetic-updated",accounts.getJSONObject(0).getString("password"));
		assertEquals("Second",accounts.getJSONObject(1).getString("username"));
	}
	@Test public void blankFieldsDoNotEraseExistingAccounts() throws Exception {
		SharedPreferences prefs=RuntimeEnvironment.getApplication().getSharedPreferences("remember-empty",0);
		assertTrue(CredentialsStore.save(prefs,"First","synthetic-one"));
		String existing=prefs.getString("credentials","");
		assertFalse(CredentialsStore.save(prefs," ","synthetic-two"));
		assertFalse(CredentialsStore.save(prefs,"Second",""));
		assertEquals(existing,prefs.getString("credentials",""));
	}
	@Test public void failedDiskWriteIsNotReportedAsSuccessfulSave() {
		android.content.SharedPreferences.Editor editor=(android.content.SharedPreferences.Editor)java.lang.reflect.Proxy.newProxyInstance(
			getClass().getClassLoader(),new Class[]{android.content.SharedPreferences.Editor.class},(proxy,method,args)->{
				if("commit".equals(method.getName())) { return false; }
				if("apply".equals(method.getName())) { fail("Remember must confirm the disk write"); }
				return proxy;
			});
		SharedPreferences prefs=(SharedPreferences)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
			new Class[]{SharedPreferences.class},(proxy,method,args)->"edit".equals(method.getName()) ? editor : args[1]);
		assertFalse(CredentialsStore.save(prefs,"First","synthetic-one"));
	}
	@Test public void encryptionFailureIsNotReportedAsSuccessfulSave() {
		SharedPreferences prefs=(SharedPreferences)java.lang.reflect.Proxy.newProxyInstance(getClass().getClassLoader(),
			new Class[]{SharedPreferences.class},(proxy,method,args)->{ throw new SecurityException("Synthetic key failure"); });
		assertFalse(CredentialsStore.save(prefs,"First","synthetic-one"));
	}
	@Test public void malformedExistingStoreIsNotOverwritten() {
		SharedPreferences prefs=RuntimeEnvironment.getApplication().getSharedPreferences("remember-corrupt",0);
		prefs.edit().putString("credentials","not-a-json-array").commit();
		assertFalse(CredentialsStore.save(prefs,"First","synthetic-one"));
		assertEquals("not-a-json-array",prefs.getString("credentials",""));
	}
	@Test public void rememberingAnotherAccountKeepsFiveMostRecent() throws Exception {
		SharedPreferences prefs=RuntimeEnvironment.getApplication().getSharedPreferences("remember-limit",0);
		for(int i=0;i<6;i++) { assertTrue(CredentialsStore.save(prefs,"Account"+i,"synthetic")); }
		JSONArray accounts=new JSONArray(prefs.getString("credentials",""));
		assertEquals(5,accounts.length()); assertEquals("Account5",accounts.getJSONObject(0).getString("username"));
		for(int i=0;i<accounts.length();i++) { assertNotEquals("Account0",accounts.getJSONObject(i).getString("username")); }
	}
	@Test public void removeOnlyOneLocalLoginCaseInsensitively() throws Exception {
		SharedPreferences prefs=RuntimeEnvironment.getApplication().getSharedPreferences("test-credentials",0);
		JSONArray accounts=new JSONArray().put(new JSONObject().put("username","First").put("password","synthetic-one").put("lastUsed",1))
			.put(new JSONObject().put("username","Second").put("password","synthetic-two").put("lastUsed",2));
		prefs.edit().putString("credentials",accounts.toString()).commit();
		assertTrue(CredentialsStore.remove(prefs,"FIRST"));
		JSONArray left=new JSONArray(prefs.getString("credentials",""));
		assertEquals(1,left.length()); assertEquals("Second",left.getJSONObject(0).getString("username"));
		assertEquals("synthetic-two",left.getJSONObject(0).getString("password"));
		assertFalse(CredentialsStore.remove(prefs,"missing"));
	}
	@Test public void legacyLoginIsMigratedAndThenRemoved() throws Exception {
		SharedPreferences prefs=RuntimeEnvironment.getApplication().getSharedPreferences("test-legacy-credentials",0);
		prefs.edit().putString("username","Old").putString("password","synthetic").commit();
		assertTrue(CredentialsStore.remove(prefs,"old"));
		assertEquals("[]",prefs.getString("credentials","")); assertFalse(prefs.contains("password")); assertFalse(prefs.contains("username"));
	}
}
