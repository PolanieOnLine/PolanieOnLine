package eu.polanieonline.client;
import static org.junit.Assert.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

@RunWith(RobolectricTestRunner.class) @Config(sdk=29) @LooperMode(LooperMode.Mode.PAUSED)
public class GameLoadingPanelTest {
	private FrameLayout host;
	private GameLoadingPanel panel;
	private final Object first=new Object(),second=new Object();
	private int cancellations;
	@Before public void setup() {
		CalendarActivity activity=Robolectric.buildActivity(CalendarActivity.class).get();
		activity.setTheme(R.style.Theme_Stendhal_FullScreen);
		host=new FrameLayout(activity); panel=new GameLoadingPanel(activity,host);
	}
	@Test public void slowLoadingDoesNotCancelOrRetryAnything() {
		panel.begin(first,()->cancellations++);
		assertTrue(text(host).contains("Łączenie z grą"));
		Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofMillis(GameLoadingPanel.SLOW_AFTER_MS));
		assertTrue(text(host).contains("Ładowanie trwa dłużej niż zwykle"));
		assertTrue(panel.visible()); assertEquals(0,cancellations); panel.dispose();
	}
	@Test public void cancellationRunsOnceAndRemovedTimerCannotReopenPanel() {
		panel.begin(first,()->cancellations++); panel.cancel(); panel.cancel();
		Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofSeconds(60));
		assertEquals(1,cancellations); assertFalse(panel.visible()); assertEquals(View.GONE,host.getVisibility());
	}
	@Test public void previousViewCannotFinishTheNewAttempt() {
		panel.begin(first,()->cancellations++); panel.begin(second,()->cancellations+=10);
		panel.finish(first); assertTrue(panel.owns(second));
		panel.finish(second); assertFalse(panel.visible()); assertEquals(0,cancellations);
	}
	@Test public void layoutRefreshDoesNotResetTheDeadline() {
		panel.begin(first,()->cancellations++);
		Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofSeconds(30));
		panel.refresh();
		Shadows.shadowOf(android.os.Looper.getMainLooper()).idleFor(Duration.ofSeconds(16));
		assertTrue(text(host).contains("Ładowanie trwa dłużej niż zwykle")); panel.dispose();
	}
	private static String text(View view) {
		String result=view instanceof TextView ? ((TextView)view).getText().toString() : "";
		if(view instanceof ViewGroup) { ViewGroup group=(ViewGroup)view; for(int i=0;i<group.getChildCount();i++) { result+=" "+text(group.getChildAt(i)); } }
		return result;
	}
}
