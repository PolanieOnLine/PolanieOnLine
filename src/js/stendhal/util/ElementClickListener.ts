/***************************************************************************
 *                    Copyright © 2024 - Faiumoni e. V.                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

declare var stendhal: any;


/**
 * Adds event listeners to handle clicks & multiple touches.
 *
 * TODO: add support long click/touch
 */
export class ElementClickListener {

	/** Function executed when click is detected. */
	public onClick?: Function;
	/** Function executed when double click is detected. */
	public onDoubleClick?: Function;
	/** Property denoting button was pressed with mouse click. */
	private clickEngaged = false;
	/** Property denoting button was pressed with tap/touch. */
	private touchEngaged = 0;
	private lastActivationTimestamp = 0;
	private singleClickTimeoutId?: number;
	private readonly doubleClickThreshold = 300;


	constructor(element: HTMLElement) {
		// add supported listeners
		element.addEventListener("mousedown", (evt: MouseEvent) => {
			evt.preventDefault();
			if (evt.button == 0) {
				this.clickEngaged = true;
			}
		});
		element.addEventListener("touchstart", (evt: TouchEvent) => {
			evt.preventDefault();
			this.touchEngaged = evt.changedTouches.length;
		}, {passive: false});
		element.addEventListener("mouseup", (evt: MouseEvent) => {
			evt.preventDefault();
			if (this.clickEngaged && evt.button == 0 && this.onClick) {
				// FIXME: should veto if moved too much before release
				this.handleActivation(evt);
			}
			this.clickEngaged = false;
		});
		element.addEventListener("touchend", (evt: TouchEvent) => {
			evt.preventDefault();
			const target = stendhal.ui.html.extractTarget(evt);
			if (this.touchEngaged == evt.changedTouches.length && target == element && this.onClick) {
				// FIXME: should veto if moved too much before release
				this.handleActivation(evt);
			}
			this.touchEngaged = 0;
		});
	}

	private handleActivation(evt: Event) {
		if (!this.onDoubleClick) {
			this.onClick?.(evt);
			return;
		}

		if (this.singleClickTimeoutId) {
			window.clearTimeout(this.singleClickTimeoutId);
			this.singleClickTimeoutId = undefined;
		}

		const now = Date.now();
		if (now - this.lastActivationTimestamp <= this.doubleClickThreshold) {
			this.lastActivationTimestamp = 0;
			this.onDoubleClick?.(evt);
			return;
		}

		this.lastActivationTimestamp = now;
		this.singleClickTimeoutId = window.setTimeout(() => {
			this.singleClickTimeoutId = undefined;
			this.lastActivationTimestamp = 0;
			this.onClick?.(evt);
		}, this.doubleClickThreshold);
	}
}
