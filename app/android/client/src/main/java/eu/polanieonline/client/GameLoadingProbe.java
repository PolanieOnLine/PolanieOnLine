/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

/** Only checks public UI visibility; never reads fields or modifies the webclient. */
final class GameLoadingProbe {
	static final String SCRIPT="(function(){try{function visible(e){if(!e)return false;"
		+"var s=window.getComputedStyle(e);return s.display!=='none'&&s.visibility!=='hidden'&&e.getClientRects().length>0;}"
		+"if(visible(document.getElementById('client')))return 1;"
		+"if(visible(document.getElementById('loginpopup'))||visible(document.querySelector('form.credential-dialog')))return 2;"
		+"return 0;}catch(e){return 0;}})()";
	private GameLoadingProbe() { }
}
