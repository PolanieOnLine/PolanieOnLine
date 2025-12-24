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

import android.content.res.Configuration;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

public class SplashUtil {

	/** Image used as title page background. */
	private final ImageView splashForeground;
	/** Image used as title page background. */
	private final ImageView splashBackground;
	/** Overlay view to dim the background. */
	private final View splashDim;

	/** Singleton instance. */
	private static SplashUtil instance;


	/**
	 * Retrieves singleton instance.
	 */
	public static SplashUtil get() {
		if (SplashUtil.instance == null) {
			SplashUtil.instance = new SplashUtil();
		}
		return SplashUtil.instance;
	}

	/**
	 * Hidden singleton constructor.
	 */
	private SplashUtil() {
		final MainActivity activity = MainActivity.get();
		splashForeground = (ImageView) activity.findViewById(R.id.splash_foreground);
		splashBackground = (ImageView) activity.findViewById(R.id.splash_background);
		splashDim = activity.findViewById(R.id.splash_dim);
		setVisible(false);
	}

	/**
	 * Sets the background splash image.
	 *
	 * @param resId
	 *   Resource ID.
	 */
	private void setImage(final int resId) {
		splashForeground.setImageResource(resId);
		splashBackground.setImageResource(resId);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
			splashBackground.setRenderEffect(RenderEffect.createBlurEffect(24.0f, 24.0f, Shader.TileMode.CLAMP));
		}
	}

	/**
	 * Sets splash image dependent on device orientation.
	 */
	public void update() {
		if (!isVisible()) {
			return;
		}
		int resId = R.drawable.splash;
		if (MainActivity.get().getOrientation() == Configuration.ORIENTATION_PORTRAIT) {
			resId = R.drawable.splash_portrait;
		}
		setImage(resId);
	}

	/**
	 * Shows or hides background splash image.
	 *
	 * @param visible
	 *   `true` if image should visible, `false` if not.
	 */
	public void setVisible(final boolean visible) {
		final int visibility = visible ? ImageView.VISIBLE : ImageView.GONE;
		splashForeground.setVisibility(visibility);
		splashBackground.setVisibility(visibility);
		splashDim.setVisibility(visibility);
		update();
	}

	/**
	 * Checks if splash is visible.
	 */
	public boolean isVisible() {
		return ImageView.VISIBLE == splashForeground.getVisibility();
	}
}
