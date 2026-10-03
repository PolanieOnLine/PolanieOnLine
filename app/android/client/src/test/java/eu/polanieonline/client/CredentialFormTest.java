package eu.polanieonline.client;

import static org.junit.Assert.*;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class) @Config(sdk=29)
public class CredentialFormTest {
	private Activity activity() {
		Activity a=Robolectric.buildActivity(Activity.class).setup().get(); a.setTheme(R.style.NativeDialog); return a;
	}
	@Test public void loginNextMovesFromUsernameToPasswordWithoutSubmitting() {
		Activity a=activity(); View form=LayoutInflater.from(a).inflate(R.layout.dialog_login,null); a.setContentView(form);
		EditText username=form.findViewById(R.id.loginUsername),password=form.findViewById(R.id.loginPassword);
		CredentialForm.connect(username,password); username.requestFocus(); username.setText("qa-local-only");
		username.onEditorAction(EditorInfo.IME_ACTION_NEXT);
		assertTrue(password.hasFocus()); assertEquals(R.id.loginPassword,username.getNextFocusForwardId());
		password.onEditorAction(EditorInfo.IME_ACTION_DONE); assertTrue(password.hasFocus());
		assertTrue(form instanceof ScrollView);
	}
	@Test public void registrationAdvancesThroughAllFourFieldsAndNeverExtractsFullscreen() {
		Activity a=activity(); View form=LayoutInflater.from(a).inflate(R.layout.dialog_register,null); a.setContentView(form);
		EditText[] fields={form.findViewById(R.id.registerUsername),form.findViewById(R.id.registerPassword),
			form.findViewById(R.id.registerPasswordRepeat),form.findViewById(R.id.registerEmail)};
		CredentialForm.connect(fields); fields[0].requestFocus();
		for(int i=0;i<fields.length;i++) {
			assertTrue(fields[i].hasFocus());
			assertNotEquals(0,fields[i].getImeOptions() & EditorInfo.IME_FLAG_NO_EXTRACT_UI);
			assertNotEquals(0,fields[i].getImeOptions() & EditorInfo.IME_FLAG_NO_FULLSCREEN);
			assertEquals(i==fields.length-1 ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT,fields[i].getImeOptions() & EditorInfo.IME_MASK_ACTION);
			fields[i].onEditorAction(i==fields.length-1 ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT);
		}
		assertTrue(fields[3].hasFocus()); assertTrue(form instanceof ScrollView);
	}
	@Test public void keyboardResizeDoesNotTriggerAccountCreation() {
		Activity a=activity(); final int[] submissions={0};
		View form=LayoutInflater.from(a).inflate(R.layout.dialog_register,null);
		EditText email=form.findViewById(R.id.registerEmail); CredentialForm.connect(email);
		AlertDialog dialog=CredentialForm.dialog(a,"Rejestracja",form,"Utwórz konto",()->submissions[0]++,"Zamknij",null,null,null);
		dialog.show(); CredentialForm.fitDialog(dialog,a);
		email.onEditorAction(EditorInfo.IME_ACTION_DONE); assertEquals(0,submissions[0]);
		android.view.ViewParent parent=email.getParent();
		while(parent!=null && !(parent instanceof ScrollView)) { parent=parent.getParent(); }
		assertTrue(parent instanceof ScrollView);
		assertEquals(3,((android.view.ViewGroup)((ScrollView)parent).getChildAt(0)).getChildCount());
		assertEquals(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE,dialog.getWindow().getAttributes().softInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST);
		dialog.dismiss();
	}
}
