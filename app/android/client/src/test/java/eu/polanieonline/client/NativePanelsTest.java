package eu.polanieonline.client;
import static org.junit.Assert.*;
import android.content.Context;
import android.content.Intent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class) @Config(sdk=29)
public class NativePanelsTest {
	@Test public void enlargedLogoKeepsProportionsInBothHeaderWidths() {
		CalendarActivity a=Robolectric.buildActivity(CalendarActivity.class).get();
		android.widget.ImageView logo=NativeUi.brand(a);
		assertEquals(android.widget.ImageView.ScaleType.FIT_CENTER,logo.getScaleType());
		assertEquals(400,logo.getDrawable().getIntrinsicWidth());
		for(int width:new int[]{200,250}) {
			int w=NativeUi.dp(a,width),h=NativeUi.dp(a,60);
			logo.measure(android.view.View.MeasureSpec.makeMeasureSpec(w,android.view.View.MeasureSpec.EXACTLY),
				android.view.View.MeasureSpec.makeMeasureSpec(h,android.view.View.MeasureSpec.EXACTLY));
			logo.layout(0,0,w,h);
			float[] values=new float[9]; logo.getImageMatrix().getValues(values);
			assertEquals(values[android.graphics.Matrix.MSCALE_X],values[android.graphics.Matrix.MSCALE_Y],0.001f);
			android.graphics.RectF bounds=new android.graphics.RectF(logo.getDrawable().getBounds());
			logo.getImageMatrix().mapRect(bounds);
			assertTrue(bounds.width()<=w+0.01f); assertTrue(bounds.height()<=h+0.01f);
		}
	}
	@Test public void woodBitmapDoesNotEnlargeButtonsOrRows() {
		CalendarActivity a=Robolectric.buildActivity(CalendarActivity.class).get();
		a.setTheme(R.style.Theme_Stendhal_FullScreen);
		android.widget.Button left=NativeUi.woodButton(a,"Konta",()->{});
		android.widget.Button right=NativeUi.woodButton(a,"Kalendarz",()->{});
		int height=NativeUi.dp(a,48);
		assertEquals(height,left.getLayoutParams().height);
		NativeUi.row(a,left,right);
		assertEquals(height,left.getLayoutParams().height);
		assertEquals(height,right.getLayoutParams().height);
	}
	@Test public void reportOmitsSavedCredentialsAndPrivateData() {
		Context c=RuntimeEnvironment.getApplication();
		c.getSharedPreferences("eu.polanieonline.client.credentials",0).edit()
			.putString("username","SECRET-NICK").putString("password","SECRET-PASSWORD").apply();
		String report=Diagnostics.report(c);
		assertTrue(report.contains("Android:")); assertTrue(report.contains("WebView:"));
		assertFalse(report.contains("SECRET")); assertFalse(report.contains("seed")); assertFalse(report.contains("cookie"));
	}
	@Test public void websiteShortcutOpensExternalBrowserAtCanonicalOrigin() {
		CalendarActivity a=Robolectric.buildActivity(CalendarActivity.class).get();
		NativeUi.openSite(a,"/profile");
		Intent intent=Shadows.shadowOf(a).getNextStartedActivity();
		assertEquals(Intent.ACTION_VIEW,intent.getAction());
		assertEquals("https://polanieonline.eu/profile",intent.getDataString());
	}
}
