package eu.polanieonline.client;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=29,qualifiers="w800dp-h360dp-land")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class SavedAccountsDesignTest {
	private CalendarActivity activity() {
		CalendarActivity activity=Robolectric.buildActivity(CalendarActivity.class).get();
		activity.setTheme(R.style.Theme_Stendhal_FullScreen); return activity;
	}
	private LinearLayout panel(Dialog dialog) {
		return (LinearLayout)((android.view.ViewGroup)dialog.findViewById(android.R.id.content)).getChildAt(0);
	}
	private void measure(View view,CalendarActivity activity,int width,int height) {
		view.measure(View.MeasureSpec.makeMeasureSpec(NativeUi.dp(activity,width),View.MeasureSpec.EXACTLY),
			View.MeasureSpec.makeMeasureSpec(NativeUi.dp(activity,height),View.MeasureSpec.AT_MOST));
		view.layout(0,0,view.getMeasuredWidth(),view.getMeasuredHeight());
	}
	@Test public void oneWoodenPanelReplacesTheSystemAlertBackground() throws Exception {
		CalendarActivity activity=activity(); LinearLayout list=NativeUi.column(activity);
		list.addView(SavedAccounts.accountRow(activity,"Przykładowe konto",()->{},()->{}));
		Dialog dialog=SavedAccounts.panel(activity,"Zapisane konta",list); dialog.show(); Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
		assertTrue(dialog.getWindow().hasFeature(Window.FEATURE_NO_TITLE));
		assertEquals(Color.TRANSPARENT,((ColorDrawable)dialog.getWindow().getDecorView().getBackground()).getColor());
		assertTrue(panel(dialog).getBackground() instanceof NativeUi.Ornament);
		assertTrue(list.getChildAt(0).getBackground() instanceof GradientDrawable);
		assertTrue(dialog.getWindow().getAttributes().width<=NativeUi.dp(activity,460));
		LinearLayout panel=panel(dialog); measure(panel,activity,460,336);
		assertTrue(panel.getMeasuredHeight()<NativeUi.dp(activity,280));
		if(Boolean.getBoolean("saved.accounts.preview")) {
			android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(panel.getWidth(),panel.getHeight(),android.graphics.Bitmap.Config.ARGB_8888);
			panel.draw(new android.graphics.Canvas(bitmap));
			try(java.io.FileOutputStream out=new java.io.FileOutputStream(System.getProperty("saved.accounts.preview.path"))) {
				bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);
			}
			bitmap.recycle();
		}
		dialog.dismiss();
	}
	@Test public void actionsAreCompactAndEachCallsOnlyItsOwnHandler() {
		CalendarActivity activity=activity(); int[] clicks={0,0};
		LinearLayout card=SavedAccounts.accountRow(activity,"Przykładowe konto",()->clicks[0]++,()->clicks[1]++);
		LinearLayout actions=(LinearLayout)card.getChildAt(1);
		Button login=(Button)actions.getChildAt(0),delete=(Button)actions.getChildAt(1);
		assertEquals(login.getLayoutParams().height,delete.getLayoutParams().height);
		assertTrue(login.getLayoutParams().height>=NativeUi.dp(activity,48));
		assertTrue(login.getLayoutParams().height<NativeUi.dp(activity,64));
		assertFalse(actions.isBaselineAligned()); login.performClick(); assertArrayEquals(new int[]{1,0},clicks);
		delete.performClick(); assertArrayEquals(new int[]{1,1},clicks);
	}
	@Test public void landscapeListScrollsWithoutGrowingPastTheScreen() {
		CalendarActivity activity=activity(); LinearLayout list=NativeUi.column(activity);
		for(int i=0;i<5;i++) { list.addView(SavedAccounts.accountRow(activity,"Przykładowe konto "+i,()->{},()->{})); }
		Dialog dialog=SavedAccounts.panel(activity,"Zapisane konta",list); dialog.show(); Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
		LinearLayout panel=panel(dialog); measure(panel,activity,460,336);
		ScrollView scroll=(ScrollView)panel.getChildAt(1);
		assertTrue(panel.getMeasuredHeight()<=NativeUi.dp(activity,336));
		assertTrue(scroll.getMeasuredHeight()>0); assertTrue(list.getMeasuredHeight()>scroll.getMeasuredHeight());
		assertTrue(panel.getChildAt(0).getMeasuredHeight()>0); dialog.dismiss();
	}
	@Test public void narrowScreenAndLargerFontKeepBothActionsReadable() {
		RuntimeEnvironment.setFontScale(1.6f); CalendarActivity activity=activity();
		LinearLayout card=SavedAccounts.accountRow(activity,"Długa nazwa przykładowego konta",()->{},()->{});
		measure(card,activity,280,600); LinearLayout actions=(LinearLayout)card.getChildAt(1);
		for(int i=0;i<2;i++) {
			Button button=(Button)actions.getChildAt(i);
			assertTrue(button.getLineCount()<=2);
			assertTrue(button.getLayout().getHeight()<=button.getHeight()-button.getCompoundPaddingTop()-button.getCompoundPaddingBottom());
		}
	}
}
