package eu.polanieonline.client;
import static org.junit.Assert.*;
import android.view.ViewGroup;
import androidx.preference.PreferenceManager;
import java.lang.reflect.Field;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

@RunWith(RobolectricTestRunner.class) @Config(sdk=29) @LooperMode(LooperMode.Mode.PAUSED)
public class RendererRecoveryTest {
	private MainActivity activity;
	private ViewGroup clients;
	@Before public void setup() throws Exception {
		activity=Robolectric.buildActivity(MainActivity.class).get();
		activity.setTheme(R.style.Theme_Stendhal_FullScreen);
		set(MainActivity.class,"instance",null,activity);
		Menu.reset(); SplashUtil.reset();
		activity.setContentView(R.layout.activity_main);
		clients=activity.findViewById(R.id.clientList); set(MainActivity.class,"clientList",activity,clients);
		PreferenceManager.getDefaultSharedPreferences(activity).edit().putBoolean("title_music",false).apply();
	}
	@After public void cleanup() throws Exception {
		for(ClientView view:activity.getClientViewList()) { clients.removeView(view); view.destroy(); }
		Menu.reset(); SplashUtil.reset(); MusicPlayer.stopMusic(); set(MainActivity.class,"instance",null,null);
	}
	@Test public void reportedViewIsDestroyedAndReplacedWithANewTitleView() throws Exception {
		ClientView failed=addClient();
		assertTrue(failed.getWebViewClient().onRenderProcessGone(failed,null));
		assertEquals(0,clients.getChildCount()); assertTrue((boolean)get(ClientView.class,"viewDestroyed",failed));
		Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
		assertEquals(1,clients.getChildCount());
		assertNotSame(failed,activity.getActiveClientView()); assertTrue(activity.getActiveClientView().onTitleScreen());
		failed.getWebViewClient().onPageFinished(failed,"https://polanieonline.eu/client/polanieonline.html");
		assertTrue(activity.getActiveClientView().onTitleScreen());
	}
	@Test public void sharedRendererCallbacksCreateOnlyOneReplacement() {
		ClientView first=addClient(),second=addClient();
		first.getWebViewClient().onRenderProcessGone(first,null);
		second.getWebViewClient().onRenderProcessGone(second,null);
		Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
		assertEquals(1,clients.getChildCount()); assertTrue(activity.getActiveClientView().onTitleScreen());
	}
	@Test public void unaffectedViewIsNotDestroyedOrReloaded() throws Exception {
		ClientView unaffected=addClient(),failed=addClient();
		failed.getWebViewClient().onRenderProcessGone(failed,null);
		Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
		assertSame(unaffected,activity.getActiveClientView());
		assertFalse((boolean)get(ClientView.class,"viewDestroyed",unaffected));
		assertNull(unaffected.getUrl());
	}
	private ClientView addClient() { ClientView view=new ClientView(activity); clients.addView(view); view.setActive(true); return view; }
	private static void set(Class<?> type,String name,Object receiver,Object value) throws Exception {
		Field field=type.getDeclaredField(name); field.setAccessible(true); field.set(receiver,value);
	}
	private static Object get(Class<?> type,String name,Object receiver) throws Exception {
		Field field=type.getDeclaredField(name); field.setAccessible(true); return field.get(receiver);
	}
}
