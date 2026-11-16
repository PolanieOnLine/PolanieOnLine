/***************************************************************************
 *                    Copyright © 2024 - Faiumoni e. V.                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { singletons } from "../SingletonRepo";
import { ui } from "./UI";


/**
 * Listens for updates to user interface.
 */
export class UIUpdateObserver {

	private vpRectObserver?: MutationObserver;
	private vpScaleObserver?: ResizeObserver;
	private browserResizeScheduled = false;

	private static instance: UIUpdateObserver;


	/**
	 * Retrieves the singleton instance.
	 */
	public static get(): UIUpdateObserver {
		if (!UIUpdateObserver.instance) {
			UIUpdateObserver.instance = new UIUpdateObserver();
		}
		return UIUpdateObserver.instance;
	}

	/**
	 * Hidden singleton constructor.
	 */
	private constructor() {
		// do nothing
	}

	/**
	 * Initializes observers for display state & changes to viewport attributes.
	 */
	public init() {
		if (this.vpRectObserver) {
			console.warn("cannot re-initialize UIUpdateObserver");
			return;
		}

		this.vpRectObserver = new MutationObserver((mutations: MutationRecord[]) => {
			this.onViewPortUpdate();
		});
		this.vpRectObserver.observe(document.getElementById("viewport")!,
				{attributes: true, attributeFilter: ["left", "right", "top", "bottom", "width", "height"]});
		if (typeof ResizeObserver !== "undefined") {
			this.vpScaleObserver = new ResizeObserver(() => this.onViewPortUpdate());
			this.vpScaleObserver.observe(document.getElementById("viewport")!);
		}

		// Some mobile browsers expose rotation only as a viewport resize.
		const resize = () => this.onBrowserResize();
		window.addEventListener("resize", resize);
		window.addEventListener("orientationchange", resize);
		window.visualViewport?.addEventListener("resize", resize);
		if (typeof screen !== "undefined") screen.orientation?.addEventListener("change", resize);
	}

	/**
	 * Retrieves the display style of the client.
	 *
	 * @return
	 *   String representing the client's display state.
	 */
	private getClientDisplay(): string {
		return document.getElementById("client")!.style["display"] || "none";
	}

	/**
	 * Checks if the client's display state has changed.
	 *
	 * @param oldDisplay
	 *   The previous display state of the client.
	 * @return
	 *   `true` if new state is not "none".
	 */
	private isClientDisplayChanged(oldDisplay: string): boolean {
		return this.getClientDisplay() !== oldDisplay;
	}

	/**
	 * Called when the client's "display" attribute changes.
	 */
	private onClientDisplayUpdate() {
	}

	/**
	 * Called when an attribute of the viewport has changed.
	 */
	private onViewPortUpdate() {
		singletons.getViewPort().updateCanvasSize();
		ui.onDisplayUpdate();
	}

	/**
	 * Wait for the browser to apply its new dimensions, then refresh the UI.
	 */
	private onBrowserResize() {
		if (this.browserResizeScheduled) return;
		this.browserResizeScheduled = true;
		requestAnimationFrame(() => {
			singletons.getViewPort().scheduleResize();
			requestAnimationFrame(() => {
				this.browserResizeScheduled = false;
				ui.onDisplayUpdate();
			});
		});
	}
}

