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
	extractTarget(event: any): EventTarget {
		if (event.changedTouches) {
			// FIXME: Always uses last index. Any way to detect which touch index was engaged?
			const tidx = event.changedTouches.length - 1;
			if (["touchmove", "touchend"].indexOf(event.type) > -1) {
				// touch events target source element
				for (const el of document.elementsFromPoint(event.changedTouches[tidx].pageX, event.changedTouches[tidx].pageY)) {
					if (!el.classList.contains("notarget")) {
						return el;
					}
				}
			}
			return event.changedTouches[tidx].target;
		}
		return event.target;
	}

	/**
	 * Normalizes an event object.
	 *
	 * @param event {any}
	 *   Executed event.
	 * @return {any}
	 *   Normalized event.
	 */
	extractPosition(event: any): any {
		let pos = event;
		const target = this.extractTarget(event);
		if (!(target instanceof HTMLElement)) {
			return pos;
		}
		const element = target as HTMLElement;
		let clientX: number|undefined;
		let clientY: number|undefined;
		let pageX: number|undefined;
		let pageY: number|undefined;
		if (event.changedTouches) {
			// FIXME: Always uses last index. Any way to detect which touch index was engaged?
			const tidx = event.changedTouches.length - 1;
			const touch = event.changedTouches[tidx];
			pos = {
				pageX: Math.round(touch.pageX),
				pageY: Math.round(touch.pageY),
				clientX: Math.round(touch.clientX),
				clientY: Math.round(touch.clientY),
				target: target
			};
			clientX = pos.clientX;
			clientY = pos.clientY;
			pageX = pos.pageX;
			pageY = pos.pageY;
		} else {
			clientX = typeof pos.clientX === "number" ? pos.clientX : undefined;
			clientY = typeof pos.clientY === "number" ? pos.clientY : undefined;
			pageX = typeof pos.pageX === "number" ? pos.pageX : undefined;
			pageY = typeof pos.pageY === "number" ? pos.pageY : undefined;
			if (!pos.target) {
				pos.target = target;
			}
		}

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
			if (typeof pos.offsetX !== "number") {
				pos.offsetX = 0;
			}
			if (typeof pos.offsetY !== "number") {
				pos.offsetY = 0;
			}
		}
		const canvas = element instanceof HTMLCanvasElement ? element : null;
		if (canvas && canvas.clientWidth && canvas.clientHeight) {
			pos.canvasRelativeX = Math.round(pos.offsetX * canvas.width / canvas.clientWidth);
			pos.canvasRelativeY = Math.round(pos.offsetY * canvas.height / canvas.clientHeight);
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
