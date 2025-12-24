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
import android.net.Uri;
import android.os.Bundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OpenAppByUrl extends Activity {

	private static final Logger LOG = LogManager.getLogger(OpenAppByUrl.class);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		// String action = intent.getAction();
		Uri data = intent.getData();
		LOG.debug("URL: {}", data);
		MainActivity.get().getActiveClientView().checkLoginIntent(intent);
		finish();
	 }

}
