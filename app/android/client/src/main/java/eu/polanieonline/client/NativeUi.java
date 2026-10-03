/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.*;
import androidx.core.graphics.PathParser;

/** Native surfaces using the same colours and ornament paths as the website. */
final class NativeUi {
	static final int INK = Color.rgb(12, 21, 29), TEAL = Color.rgb(104, 204, 212), GOLD = Color.rgb(181, 150, 96);
	static final int TEXT = Color.rgb(242, 245, 247), MUTED = Color.rgb(171, 189, 199);
	private NativeUi() { }
	static int dp(android.content.Context c, float value) { return Math.round(c.getResources().getDisplayMetrics().density * value); }
	static LinearLayout column(android.content.Context c) {
		LinearLayout view = new LinearLayout(c); view.setOrientation(LinearLayout.VERTICAL); return view;
	}
	static TextView text(android.content.Context c, String value, int size, int colour) {
		TextView view = new TextView(c); view.setText(value); view.setTextSize(size); view.setTextColor(colour);
		view.setPadding(0, dp(c, 4), 0, dp(c, 4)); return view;
	}
	static Button button(Activity a, String label, Runnable action) {
		Button button = new Button(a); button.setText(label); button.setTextColor(TEXT); button.setTextSize(14);
		button.setAllCaps(false); button.setMinHeight(dp(a, 48));
		GradientDrawable bg = new GradientDrawable(); bg.setColor(0xff122d36); bg.setCornerRadius(dp(a, 7)); bg.setStroke(dp(a, 1), 0xff357885);
		button.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x445eaaac),bg,null));
		button.setPadding(dp(a, 14), dp(a, 8), dp(a, 14), dp(a, 8));
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.topMargin = dp(a, 8); button.setLayoutParams(p);
		button.setOnClickListener(v -> action.run()); return button;
	}
	static LinearLayout card(android.content.Context c) {
		LinearLayout card = column(c); card.setBackground(new Ornament(c));
		card.setPadding(dp(c, 24), dp(c, 20), dp(c, 24), dp(c, 28));
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2); p.topMargin = dp(c, 12); card.setLayoutParams(p); return card;
	}
	static Button woodButton(Activity a,String label,Runnable action) {
		Button button=button(a,label,action);
		button.setBackground(new android.graphics.drawable.InsetDrawable(androidx.core.content.ContextCompat.getDrawable(a,R.drawable.btn_wood),0,dp(a,5),0,dp(a,5)));
		button.setBackgroundTintList(null); button.setTextColor(0xffffeed3); button.setTextSize(13);
		button.setPadding(dp(a,10),dp(a,6),dp(a,10),dp(a,6));
		button.setGravity(android.view.Gravity.CENTER); button.setIncludeFontPadding(false);
		button.setMaxLines(2); button.setEllipsize(android.text.TextUtils.TruncateAt.END);
		// The tiled bitmap has an 80dp intrinsic height. It must not determine button height.
		// Reserve two text lines at the current font scale, even when the neighbouring label is shorter.
		button.getLayoutParams().height=Math.max(dp(a,48),2*button.getLineHeight()+button.getCompoundPaddingTop()+button.getCompoundPaddingBottom()); return button;
	}
	static Button link(Activity a, String label, Runnable action) {
		Button button=new Button(a); button.setText(label); button.setTextSize(13); button.setTextColor(TEAL); button.setAllCaps(false);
		button.setMinHeight(dp(a,44)); button.setMinimumHeight(dp(a,44)); button.setMinWidth(0); button.setMinimumWidth(0);
		button.setGravity(android.view.Gravity.START|android.view.Gravity.CENTER_VERTICAL);
		button.setPadding(0,dp(a,4),0,dp(a,4));
		button.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x335eaaac),null,new android.graphics.drawable.ColorDrawable(Color.WHITE)));
		button.setLayoutParams(new LinearLayout.LayoutParams(-1,-2)); button.setOnClickListener(v->action.run()); return button;
	}
	static ImageView brand(Activity a) {
		ImageView logo=new ImageView(a); logo.setImageResource(R.drawable.logo2x2);
		logo.setContentDescription("PolanieOnLine"); logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
		return logo;
	}
	static LinearLayout row(Activity a, Button left, Button right) {
		LinearLayout row=new LinearLayout(a);
		row.setBaselineAligned(false); row.setGravity(android.view.Gravity.TOP);
		int leftHeight=left.getLayoutParams().height, rightHeight=right.getLayoutParams().height;
		left.setLayoutParams(new LinearLayout.LayoutParams(0,leftHeight,1));
		LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,rightHeight,1); p.leftMargin=dp(a,8); right.setLayoutParams(p);
		LinearLayout.LayoutParams outer=new LinearLayout.LayoutParams(-1,-2); outer.topMargin=dp(a,8); row.setLayoutParams(outer);
		row.addView(left); row.addView(right); return row;
	}
	static void openSite(Activity a, String path) {
		try { a.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://polanieonline.eu" + path))); }
		catch (ActivityNotFoundException e) { Toast.makeText(a, "Nie znaleziono przeglądarki w telefonie.", Toast.LENGTH_LONG).show(); }
	}
	static ScrollView screen(Activity a, LinearLayout content) {
		return screen(a,content,false);
	}
	static ScrollView screen(Activity a, LinearLayout content, boolean winter) {
		ScrollView scroll = new ScrollView(a); scroll.setFillViewport(true);
		content.setPadding(dp(a, 18), dp(a, 16), dp(a, 18), dp(a, 24)); scroll.addView(content);
		FrameLayout stage=new FrameLayout(a); stage.setBackgroundColor(INK);
		if(winter) {
			ImageView background=new ImageView(a); background.setImageResource(R.drawable.title_winter_v2);
			background.setScaleType(ImageView.ScaleType.CENTER_CROP); background.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
			stage.addView(background,new FrameLayout.LayoutParams(-1,-1));
			View dim=new View(a); dim.setBackgroundColor(0xaa040c12); stage.addView(dim,new FrameLayout.LayoutParams(-1,-1));
			// Limit reading width on tablets and landscape phones, while leaving the background full width.
			scroll.removeView(content); FrameLayout holder=new FrameLayout(a) {
				@Override protected void onMeasure(int width,int height) {
					content.getLayoutParams().width=Math.min(dp(a,840),View.MeasureSpec.getSize(width));
					super.onMeasure(width,height);
				}
			};
			holder.addView(content,new FrameLayout.LayoutParams(-1,-2,android.view.Gravity.CENTER_HORIZONTAL));
			scroll.addView(holder);
		}
		stage.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
		androidx.core.view.WindowCompat.setDecorFitsSystemWindows(a.getWindow(), false);
		darkBars(a);
		androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(stage, (v, insets) -> {
			androidx.core.graphics.Insets safe = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()
					| androidx.core.view.WindowInsetsCompat.Type.displayCutout() | androidx.core.view.WindowInsetsCompat.Type.ime());
			v.setPadding(safe.left, safe.top, safe.right, safe.bottom); return insets;
		});
		a.setContentView(stage); return scroll;
	}
	static void darkBars(Activity a) {
		a.getWindow().setNavigationBarColor(INK);
		if(android.os.Build.VERSION.SDK_INT>=29) { a.getWindow().setNavigationBarContrastEnforced(false); }
		androidx.core.view.WindowCompat.getInsetsController(a.getWindow(),a.getWindow().getDecorView()).setAppearanceLightNavigationBars(false);
	}
	static final class Ornament extends Drawable {
		private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		private final float density;
		private final boolean compact;
		private final Shader woodShader;
		private final Path corner = PathParser.createPathFromPathData("M6 58V6h52M14 42V14h28M6 30 30 6M6 46 46 6M14 30l8-8 8 8-8 8-8-8Zm8-24 8 8-8 8-8-8 8-8Z");
		private final Path accent = PathParser.createPathFromPathData("m30 30 12 12m-7-7 10-2-2 10m-13-13 9-2-2 9");
		private final Path knot = PathParser.createPathFromPathData("m44 12 12-10 12 10-12 10-12-10Zm8 0L64 2l12 10-12 10-12-10Zm-16 0 4-4 4 4-4 4-4-4Zm40 0 4-4 4 4-4 4-4-4ZM0 12h32m56 0h32");
		private final Path knotAccent = PathParser.createPathFromPathData("m10 12 8-7 8 7-8 7-8-7Zm84 0 8-7 8 7-8 7-8-7Z");
		Ornament(android.content.Context c) { this(c,false); }
		Ornament(android.content.Context c, boolean compact) { this(c,compact,false); }
		Ornament(android.content.Context c, boolean compact, boolean wood) {
			density = c.getResources().getDisplayMetrics().density; this.compact=compact;
			woodShader=wood ? new BitmapShader(BitmapFactory.decodeResource(c.getResources(),R.drawable.panel_wood),Shader.TileMode.REPEAT,Shader.TileMode.REPEAT) : null;
		}
		@Override public void draw(Canvas canvas) {
			Rect bounds = getBounds(); float l = bounds.left + 8*density, t = bounds.top + 8*density;
			float r = bounds.right - 8*density, bottom = bounds.bottom - 10*density;
			paint.setStyle(Paint.Style.FILL);
			if(woodShader!=null) {
				paint.setShader(woodShader); canvas.drawRoundRect(bounds.left,bounds.top,bounds.right,bounds.bottom,6*density,6*density,paint);
				paint.setShader(null); paint.setColor(0xc2120b07);
			} else { paint.setColor(0xf20c151d); }
			canvas.drawRoundRect(bounds.left,bounds.top,bounds.right,bounds.bottom,6*density,6*density,paint);
			paint.setStyle(Paint.Style.STROKE); paint.setStrokeWidth(density); paint.setColor(0x88b59660);
			float middle = (l+r)/2, width = Math.min((compact ? 56 : 76)*density, (r-l)*.45f);
			canvas.drawLine(l,t,r,t,paint); canvas.drawLine(l,t,l,bottom,paint); canvas.drawLine(r,t,r,bottom,paint);
			canvas.drawLine(l,bottom,middle-width/2,bottom,paint); canvas.drawLine(middle+width/2,bottom,r,bottom,paint);
			paint.setColor(0x33b59660); canvas.drawLine(l+4*density,t+4*density,r-4*density,t+4*density,paint);
			canvas.drawLine(l+4*density,t+4*density,l+4*density,bottom-4*density,paint); canvas.drawLine(r-4*density,t+4*density,r-4*density,bottom-4*density,paint);
			float s = (compact ? 18 : 24)*density/64;
			for (int i=0;i<4;i++) { canvas.save(); canvas.translate(i==0||i==3?l:r, i<2?t:bottom); canvas.rotate(i*90); canvas.scale(s,s);
				paint.setStrokeWidth(1.2f); paint.setColor(GOLD); canvas.drawPath(corner,paint); paint.setColor(0xff5eaaac); canvas.drawPath(accent,paint); canvas.restore(); }
			canvas.save(); canvas.translate(middle-width/2, bottom-width/120*12); canvas.scale(width/120,width/120);
			paint.setStrokeWidth(1.1f); paint.setColor(GOLD); canvas.drawPath(knot,paint); paint.setColor(0xff5eaaac); canvas.drawPath(knotAccent,paint); canvas.restore();
		}
		@Override public void setAlpha(int alpha) { }
		@Override public void setColorFilter(ColorFilter filter) { paint.setColorFilter(filter); }
		@Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
	}
}
