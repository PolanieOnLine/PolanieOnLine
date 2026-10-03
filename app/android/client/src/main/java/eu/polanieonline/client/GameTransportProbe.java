/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

/** Read-only inspection. No patched WebSocket, handlers, controls or game source. */
final class GameTransportProbe {
	static final String SCRIPT="(function(){try{var roots=[window.marauroa,window.$v];"
		+"var keys=Object.getOwnPropertyNames(window);for(var k=0;k<keys.length&&k<2048;k++){"
		+"var d=Object.getOwnPropertyDescriptor(window,keys[k]);var r=d&&d.value;"
		+"if(r&&typeof r==='object'&&(Object.getOwnPropertyDescriptor(r,'clientFramework')"
		+"||Object.getOwnPropertyDescriptor(r,'$clientFramework$')))roots.push(r);}"
		+"for(var i=0;i<roots.length;i++){var r=roots[i];if(!r)continue;"
		+"var f=r.clientFramework||r.$clientFramework$;var s=f&&(f.socket||f.$socket$);"
		+"if(s&&typeof s.readyState==='number')return s.readyState;}return -1;}catch(e){return -1;}})()";
	private GameTransportProbe() { }
}
