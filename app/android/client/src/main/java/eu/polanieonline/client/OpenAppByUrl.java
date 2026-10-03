/***************************************************************************
 *                    Copyright © 2024 - Faiumoni e. V.                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package eu.polanieonline.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class OpenAppByUrl extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		MainActivity main = MainActivity.get();
		if (main == null || main.isFinishing() || main.isDestroyed() || main.getActiveClientView() == null) {
			// A callback cannot resume a login state lost when the process was closed.
			startActivity(new Intent(this, MainActivity.class));
			Toast.makeText(this, "Otwórz logowanie ponownie w aplikacji.", Toast.LENGTH_LONG).show();
		} else {
			main.getActiveClientView().checkLoginIntent(intent);
		}
		finish();
	 }

}
