/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { stendhal } from "../stendhal";

/**
 * HTML code manipulation.
 */
export class HTMLManager {

	/** Singleton instance. */
	private static instance: HTMLManager;


	/**
	 * Retrieves singleton instance.
	 */
	static get(): HTMLManager {
		if (!HTMLManager.instance) {
			HTMLManager.instance = new HTMLManager();
		}
		return HTMLManager.instance;
	}

	/**
	 * Hidden singleton constructor.
	 */
	private constructor() {
		// do nothing
	}

	esc(msg: string, filter=[]) {
		msg = msg.replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/\n/g, "<br>");
		// restore filtered tags
		for (const tag of filter) {
			msg = msg.replace("&lt;" + tag + "&gt;", "<" + tag + ">")
					.replace("&lt;/" + tag + "&gt;", "</" + tag + ">");
		}

		return msg;
	}

	niceName(s: string): string {
		if (!s) {
			return "";
		}
		let temp = s.replace(/_/g, " ").trim();
		return temp.charAt(0).toUpperCase() + temp.slice(1);
	}

	/**
	 * Retrieves target element from event.
	 *
	 * @param event {any}
	 *   Executed event.
	 * @return {EventTarget}
	 *   Translated event target.
	 */
	extractTarget(event: any): EventTarget | null {
		if (event.changedTouches && event.changedTouches.length > 0) {
			// FIXME: Always uses last index. Any way to detect which touch index was engaged?
			const tidx = event.changedTouches.length - 1;
			const touch = event.changedTouches[tidx];
			if (["touchmove", "touchend"].indexOf(event.type) > -1) {
				// touch events target source element
				for (const el of document.elementsFromPoint(touch.clientX, touch.clientY)) {
					if (!el.classList.contains("notarget")) {
						return el;
					}
				}
			}
			return touch.target;
		}
		return event.target ?? null;
	}

	/**
	 * Normalizes an event object without mutating the native browser event.
	 *
	 * @param event {any}
	 *   Executed event.
	 * @return {any}
	 *   Normalized event data.
	 */
	extractPosition(event: any): any {
		const target = this.extractTarget(event);
		if (!(target instanceof HTMLElement)) {
			return event;
		}
		const element = target as HTMLElement;
		const pos: any = {
			type: event.type,
			target: target,
			button: event.button,
			buttons: event.buttons,
			which: event.which,
			ctrlKey: event.ctrlKey,
			shiftKey: event.shiftKey,
			altKey: event.altKey,
			metaKey: event.metaKey
		};
		let clientX: number|undefined;
		let clientY: number|undefined;
		let pageX: number|undefined;
		let pageY: number|undefined;
		if (event.changedTouches && event.changedTouches.length > 0) {
			// FIXME: Always uses last index. Any way to detect which touch index was engaged?
			const tidx = event.changedTouches.length - 1;
			const touch = event.changedTouches[tidx];
			clientX = Math.round(touch.clientX);
			clientY = Math.round(touch.clientY);
			pageX = Math.round(touch.pageX);
			pageY = Math.round(touch.pageY);
		} else {
			clientX = typeof event.clientX === "number" ? event.clientX : undefined;
			clientY = typeof event.clientY === "number" ? event.clientY : undefined;
			pageX = typeof event.pageX === "number" ? event.pageX : undefined;
			pageY = typeof event.pageY === "number" ? event.pageY : undefined;
		}

		pos.clientX = clientX;
		pos.clientY = clientY;
		pos.pageX = pageX;
		pos.pageY = pageY;

		const rect = element.getBoundingClientRect();
		if (typeof clientX === "number" && typeof clientY === "number") {
			pos.offsetX = Math.round(clientX - rect.left);
			pos.offsetY = Math.round(clientY - rect.top);
		} else if (typeof pageX === "number" && typeof pageY === "number") {
			const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft || 0;
			const scrollTop = window.pageYOffset || document.documentElement.scrollTop || 0;
			pos.offsetX = Math.round(pageX - (rect.left + scrollLeft));
			pos.offsetY = Math.round(pageY - (rect.top + scrollTop));
		} else {
			pos.offsetX = typeof event.offsetX === "number" ? event.offsetX : 0;
			pos.offsetY = typeof event.offsetY === "number" ? event.offsetY : 0;
		}
		const canvas = element instanceof HTMLCanvasElement ? element : null;
		if (canvas && rect.width && rect.height) {
			const gamewindow = stendhal.ui?.gamewindow;
			const viewportCanvas = gamewindow?.getElement?.();
			const isViewportCanvas = Boolean(viewportCanvas && canvas === viewportCanvas);
			const inputScale = isViewportCanvas ? gamewindow?.getInputScale?.() : undefined;
			const rectWidth = typeof inputScale?.rectWidth === "number" && inputScale.rectWidth > 0
				? inputScale.rectWidth
				: rect.width;
			const rectHeight = typeof inputScale?.rectHeight === "number" && inputScale.rectHeight > 0
				? inputScale.rectHeight
				: rect.height;
			const scaleX = canvas.width / rectWidth;
			const scaleY = canvas.height / rectHeight;
			const devicePixelRatio = isViewportCanvas
				? (typeof inputScale?.devicePixelRatio === "number" && inputScale.devicePixelRatio > 0
					? inputScale.devicePixelRatio
					: window.devicePixelRatio || 1)
				: 1;
			pos.canvasRelativeX = Math.round(pos.offsetX * (scaleX / devicePixelRatio));
			pos.canvasRelativeY = Math.round(pos.offsetY * (scaleY / devicePixelRatio));
		} else {
			pos.canvasRelativeX = Math.round(pos.offsetX);
			pos.canvasRelativeY = Math.round(pos.offsetY);
		}
		return pos;
	}

	formatTallyMarks(line: string): any {
		let tmp = line.split("<tally>");
		const pre = tmp[0];
		tmp = tmp[1].split("</tally>");
		const post = tmp[1];
		const count = parseInt(tmp[0].trim(), 10);

		let tallyString = "";
		if (count > 0) {
			let t = 0
			for (let idx = 0; idx < count; idx++) {
				t++
				if (t == 5) {
					tallyString += "5";
					t = 0;
				}
			}

			if (t > 0) {
				tallyString += t;
			}
		} else {
			tallyString = "0";
		}

		const tally = document.createElement("span");
		tally.className = "tally";
		tally.textContent = tallyString;

		return [pre, tally, post];
	}

	/**
	 * Extracts slot name from element ID.
	 *
	 * @param id {string}
	 *   Element's ID string.
	 * @return {string}
	 *   Slot name.
	 */
	parseSlotName(id: string): string {
		if (id.includes("-")) {
			return id.split("-")[0];
		}
		return id.replace(/[0-9]$/, "");
	}
}
