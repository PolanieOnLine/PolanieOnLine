package eu.polanieonline.client;

import static org.junit.Assert.*;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import java.lang.reflect.*;
import java.time.YearMonth;
import java.util.*;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class) @Config(sdk=29)
public class CalendarDesignTest {
	private CalendarActivity activity() { CalendarActivity a=Robolectric.buildActivity(CalendarActivity.class).get(); a.setTheme(R.style.Theme_Stendhal_FullScreen); return a; }
	private List<View> all(View root) {
		List<View> found=new ArrayList<>(); found.add(root);
		if(root instanceof ViewGroup) { ViewGroup group=(ViewGroup)root; for(int i=0;i<group.getChildCount();i++) { found.addAll(all(group.getChildAt(i))); } }
		return found;
	}
	@Test public void calendarUsesSharedLogoOrnamentAndFixedWoodenControls() {
		CalendarActivity a=activity(); LinearLayout content=a.createContent();
		assertTrue(((ViewGroup)content.getChildAt(0)).getBackground() instanceof NativeUi.Ornament);
		int buttons=0,logos=0;
		for(View view:all(content)) {
			if(view instanceof Button) { buttons++; assertEquals(NativeUi.dp(a,48),view.getLayoutParams().height); }
			if(view instanceof ImageView) { logos++; assertEquals(ImageView.ScaleType.FIT_CENTER,((ImageView)view).getScaleType()); }
		}
		assertEquals(5,buttons); assertEquals(1,logos);
	}
	@Test public void winterContentFitsSafeWidthAndIsBoundedOnWideScreens() {
		CalendarActivity a=activity(); LinearLayout content=a.createContent(); ScrollView scroll=NativeUi.screen(a,content,true);
		for(int width:new int[]{320,760,1200}) {
			scroll.measure(View.MeasureSpec.makeMeasureSpec(NativeUi.dp(a,width),View.MeasureSpec.EXACTLY),
				View.MeasureSpec.makeMeasureSpec(NativeUi.dp(a,360),View.MeasureSpec.EXACTLY));
			assertEquals(NativeUi.dp(a,Math.min(width,840)),content.getMeasuredWidth());
		}
	}
	@Test @Config(qualifiers="w900dp-h400dp-land") public void landscapeKeepsEventsBesideCompactWoodenControls() {
		CalendarActivity a=activity(); LinearLayout content=a.createContent();
		assertEquals(LinearLayout.HORIZONTAL,content.getOrientation());
		assertEquals(NativeUi.dp(a,260),content.getChildAt(1).getLayoutParams().width);
		assertTrue(content.getChildAt(1).getBackground() instanceof NativeUi.Ornament);
	}
	@Test public void eventSummaryIsCompactButFullDescriptionRemainsInDetails() throws Exception {
		CalendarActivity a=activity(); LinearLayout content=a.createContent();
		Field month=CalendarActivity.class.getDeclaredField("month"); month.setAccessible(true); month.set(a,YearMonth.of(2050,10));
		JSONObject event=new JSONObject().put("id",1).put("title","Wydarzenie QA").put("description","Pełny opis wydarzenia")
			.put("starts_at","2050-10-10T00:00:00+02:00").put("ends_at","2050-10-11T00:00:00+02:00").put("all_day",true).put("state","published");
		Method render=CalendarActivity.class.getDeclaredMethod("render",JSONObject.class,boolean.class); render.setAccessible(true);
		render.invoke(a,new JSONObject().put("events",new JSONArray().put(event)),false);
		boolean description=false,details=false;
		for(View view:all(content)) {
			if(view instanceof TextView && "Pełny opis wydarzenia".contentEquals(((TextView)view).getText())) { description=true; assertEquals(3,((TextView)view).getMaxLines()); }
			if(view instanceof Button && "Szczegóły i przypomnienie".contentEquals(((Button)view).getText())) { details=true; }
		}
		assertTrue(description); assertTrue(details);
	}
}
