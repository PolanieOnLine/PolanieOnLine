/***************************************************************************
 *                     Copyright © 2025 - PolanieOnLine                    *
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

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Popup anchored to the toolbar offering quick login and profile switching.
 */
public class QuickActionsPopup {

	private final Context context;
	private PopupWindow popupWindow;
	private AutoCompleteTextView loginInput;
	private RadioGroup profileGroup;
	private final List<String> usernames = new ArrayList<>();
	private ArrayAdapter<String> loginAdapter;
	private boolean bindingProfileSelection = false;

	public QuickActionsPopup(final Context context) {
		this.context = context;
	}

	/**
	 * Displays the popup anchored to the provided view.
	 *
	 * @param anchor Anchor view for the popup.
	 */
	public void show(final View anchor) {
		if (popupWindow == null) {
			createPopup(anchor);
		}
		refreshProfiles();
		refreshLogins();
		popupWindow.showAsDropDown(anchor, -24, 0, Gravity.END);
		loginInput.post(new Runnable() {
			@Override
			public void run() {
				loginInput.showDropDown();
			}
		});
	}

	private void createPopup(final View anchor) {
		final View content = LayoutInflater.from(context).inflate(R.layout.popup_quick_actions, null);
		popupWindow = new PopupWindow(content, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
				true);
		popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		popupWindow.setOutsideTouchable(true);

		loginInput = content.findViewById(R.id.quick_login_input);
		loginAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, usernames);
		loginInput.setAdapter(loginAdapter);
		loginInput.setThreshold(0);
		loginInput.setOnItemClickListener((parent, view, position, id) -> {
			final String username = loginAdapter.getItem(position);
			if (username == null) {
				return;
			}
			handleLoginSelection(username);
		});

		content.findViewById(R.id.quick_close_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(final View v) {
				popupWindow.dismiss();
			}
		});

		profileGroup = content.findViewById(R.id.quick_profile_group);
		profileGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(final RadioGroup group, final int checkedId) {
				if (bindingProfileSelection) {
					return;
				}
				final ServerProfile selected = mapProfileId(checkedId);
				if (selected == null) {
					return;
				}
				popupWindow.dismiss();
				MainActivity.get().getActiveClientView().applyServerProfile(selected);
			}
		});
	}

	private void refreshLogins() {
		usernames.clear();
		final List<CredentialsStore.Credentials> savedCredentials = CredentialsStore.loadAll(context);
		for (final CredentialsStore.Credentials credentials : savedCredentials) {
			usernames.add(credentials.getUsername());
		}
		loginAdapter.notifyDataSetChanged();
	}

	private void refreshProfiles() {
		bindingProfileSelection = true;
		final ServerProfile profile = MainActivity.get().getActiveClientView().getCurrentProfile();
		if (profile != null) {
			switch (profile) {
			case PROD:
				profileGroup.check(R.id.profile_prod);
				break;
			case STAGE:
				profileGroup.check(R.id.profile_stage);
				break;
			case TEST:
				profileGroup.check(R.id.profile_test);
				break;
			default:
				break;
			}
		}
		bindingProfileSelection = false;
	}

	private ServerProfile mapProfileId(final int checkedId) {
		if (checkedId == R.id.profile_test) {
			return ServerProfile.TEST;
		}
		if (checkedId == R.id.profile_stage) {
			return ServerProfile.STAGE;
		}
		if (checkedId == R.id.profile_prod) {
			return ServerProfile.PROD;
		}
		return null;
	}

	private void handleLoginSelection(final String username) {
		final List<CredentialsStore.Credentials> savedCredentials = CredentialsStore.loadAll(context);
		final CredentialsStore.Credentials credentials = CredentialsStore.findByUsername(savedCredentials, username);
		if (credentials == null) {
			Notifier.toast("Brak zapisanych danych dla \"" + username + "\"");
			return;
		}
		popupWindow.dismiss();
		MainActivity.get().getActiveClientView().showLoginDialogWithCredentials(credentials, true);
	}
}