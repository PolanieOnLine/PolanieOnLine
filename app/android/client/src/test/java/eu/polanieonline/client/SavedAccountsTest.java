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
