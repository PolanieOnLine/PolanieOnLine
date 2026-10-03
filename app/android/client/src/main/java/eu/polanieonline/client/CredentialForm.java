/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.app.AlertDialog;
import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.view.View;

/** Keep native forms visible in landscape and explicitly advance between fields. */
final class CredentialForm {
	private CredentialForm() { }
	static void connect(EditText... fields) {
		for(int i=0;i<fields.length;i++) {
			EditText field=fields[i],next=i+1<fields.length ? fields[i+1] : null;
			field.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN
				| (next==null ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT));
			if(next!=null) { field.setNextFocusForwardId(next.getId()); field.setNextFocusDownId(next.getId()); }
			field.setOnEditorActionListener((view,action,event)->{
				boolean enter=event!=null && event.getKeyCode()==KeyEvent.KEYCODE_ENTER;
				if(!enter && action!=EditorInfo.IME_ACTION_NEXT && action!=EditorInfo.IME_ACTION_DONE) { return false; }
				if(enter && event.getAction()!=KeyEvent.ACTION_UP) { return true; }
				InputMethodManager keyboard=(InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
				if(next!=null) {
					next.requestFocus(); next.setSelection(next.length());
					if(keyboard!=null) { keyboard.showSoftInput(next,InputMethodManager.SHOW_IMPLICIT); }
				} else if(keyboard!=null) { keyboard.hideSoftInputFromWindow(view.getWindowToken(),0); }
				// Done only closes the keyboard. Account creation still requires the explicit button.
				return true;
			});
		}
	}
	static void resizeWithKeyboard(AlertDialog dialog) {
		if(dialog.getWindow()!=null) { dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); }
	}
	static AlertDialog dialog(Activity activity,String title,View form,String confirmLabel,Runnable confirm,
			String secondaryLabel,Runnable secondary,String extraLabel,Runnable extra) {
		LinearLayout body=NativeUi.card(activity);
		body.addView(NativeUi.text(activity,title,20,NativeUi.TEXT));
		// The title and actions must scroll too. Fixed alert chrome leaves no room above a landscape IME.
		if(form instanceof ScrollView) {
			ScrollView wrapper=(ScrollView)form; View fields=wrapper.getChildAt(0); wrapper.removeView(fields);
			fields.setPadding(0,NativeUi.dp(activity,8),0,0); body.addView(fields);
		} else { body.addView(form); }
		ScrollView scroll=new ScrollView(activity); scroll.addView(body);
		AlertDialog dialog=new AlertDialog.Builder(activity,R.style.NativeDialog).create();
		dialog.setView(scroll,0,0,0,0);
		body.addView(NativeUi.row(activity,NativeUi.woodButton(activity,confirmLabel,()->{ dialog.dismiss(); confirm.run(); }),
			NativeUi.woodButton(activity,secondaryLabel,()->{ dialog.dismiss(); if(secondary!=null) { secondary.run(); } })));
		if(extraLabel!=null) { body.addView(NativeUi.link(activity,extraLabel,()->{ dialog.dismiss(); extra.run(); })); }
		resizeWithKeyboard(dialog); return dialog;
	}
	static void fitDialog(AlertDialog dialog,Activity activity) {
		if(dialog.getWindow()!=null) { dialog.getWindow().setLayout(NativeUi.dp(activity,Math.min(560,activity.getResources().getConfiguration().screenWidthDp-32)),WindowManager.LayoutParams.WRAP_CONTENT); }
	}
}
