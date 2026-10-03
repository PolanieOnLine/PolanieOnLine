/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.app.AlertDialog;
import android.widget.*;
import java.util.List;

/** Passwords remain in encrypted storage and are never displayed by this manager. */
final class SavedAccounts {
	private SavedAccounts() { }
	static void show(MainActivity activity) {
		LinearLayout content=NativeUi.column(activity);
		int pad=NativeUi.dp(activity,18); content.setPadding(pad,pad,pad,pad);
		List<CredentialsStore.Credentials> accounts=CredentialsStore.loadAll(activity);
		if(accounts.isEmpty()) { content.addView(NativeUi.text(activity,"Nie masz zapisanych kont. Możesz zapisać dane podczas logowania do gry.",15,NativeUi.MUTED)); }
		ScrollView scroll=new ScrollView(activity); scroll.addView(content);
		AlertDialog dialog=new AlertDialog.Builder(activity).setTitle("Zapisane konta").setView(scroll).setNegativeButton("Zamknij",null).create();
		for(CredentialsStore.Credentials account:accounts) {
			LinearLayout row=NativeUi.card(activity);
			row.addView(NativeUi.text(activity,account.getUsername(),18,NativeUi.TEXT));
			row.addView(NativeUi.button(activity,"Zaloguj",()->{
				dialog.dismiss();
				Runnable login=()->activity.getActiveClientView().showLoginDialogWithCredentials(account,false);
				if(activity.getActiveClientView().onTitleScreen()) { login.run(); }
				else { new AlertDialog.Builder(activity).setMessage("Zakończyć obecną sesję i zalogować na wybrane konto?")
					.setPositiveButton("Zaloguj",(d,w)->login.run()).setNegativeButton("Anuluj",null).show(); }
			}));
			row.addView(NativeUi.button(activity,"Usuń z telefonu",()->new AlertDialog.Builder(activity)
				.setMessage("Usunąć zapisane dane konta "+account.getUsername()+" z tego telefonu? Konto w grze pozostanie bez zmian.")
				.setNegativeButton("Anuluj",null).setPositiveButton("Usuń",(d,w)->{
					if(CredentialsStore.remove(activity,account.getUsername())) { dialog.dismiss(); show(activity); }
					else { Toast.makeText(activity,"Nie udało się usunąć zapisanych danych.",Toast.LENGTH_LONG).show(); }
				}).show()));
			content.addView(row);
		}
		dialog.show();
	}
}
