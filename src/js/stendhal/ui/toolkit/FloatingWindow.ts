/***************************************************************************
 *                (C) Copyright 2015-2024 - Faiumoni e. V.                 *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

declare var stendhal: any;

import { Component } from "./Component";
import { DialogContentComponent } from "./DialogContentComponent";

import { Point } from "../../util/Point";


export class FloatingWindow extends Component {

	private readonly closeSound = "click-1";
	private readonly toggleSound = "ui/window_fold";
	private opened = true;

	private onMouseMovedDuringDragListener: EventListener;
	private onMouseUpDuringDragListener: EventListener;
	private offsetX = 0;
	private offsetY = 0;

	private content: Component;
	private contentWrapper!: HTMLElement;
	private minimizeButton: HTMLButtonElement | null = null;
	private minimizeEnabled = false;
	private minimized = false;
	private preferredWidth?: number;
	private fixedWidth?: number;

	private windowId?: string;


	constructor(title: string, protected contentComponent: Component, x: number, y: number) {
		super("window-template");

		this.content = contentComponent;

		// create HTML code for window
		this.componentElement.style.position = "absolute";
		this.componentElement.style.left = x + "px";
		this.componentElement.style.top = y + "px";

		const titleBar = this.child(".windowtitlebar")!;

		// apply theme
		stendhal.config.applyTheme(titleBar);

		if (title) {
			this.child(".windowtitle")!.textContent = title;
		} else {
			titleBar.classList.add("hidden");
		}
		this.contentWrapper = this.child(".windowcontent")! as HTMLElement;
		this.contentWrapper.append(contentComponent.componentElement);

		// register and prepare event listeners
		titleBar.addEventListener("mousedown", (event) => {
			this.onMouseDown(event as MouseEvent)
		});
		titleBar.addEventListener("touchstart", (event) => {
			this.onTouchStart(event as TouchEvent)
		});
		const closeButton = this.child(".windowtitleclose")!;
		closeButton.addEventListener("click", (event) => {
			this.onClose(event);
			stendhal.sound.playGlobalizedEffect(this.closeSound);
		});
		closeButton.addEventListener("touchend", (event) => {
			this.onClose(event);
			stendhal.sound.playGlobalizedEffect(this.closeSound);
		});
		this.minimizeButton = this.child(".windowtitleminimize") as HTMLButtonElement | null;
		if (this.minimizeButton) {
			this.minimizeButton.style.display = "none";
			this.minimizeButton.setAttribute("aria-hidden", "true");
			this.minimizeButton.tabIndex = -1;
			this.minimizeButton.addEventListener("click", (event) => {
				event.preventDefault();
				this.toggleMinimized();
			});
		}
		this.onMouseMovedDuringDragListener = (event: Event) => {
			if (event.type === "mousemove") {
				this.onMouseMovedDuringDrag(event as MouseEvent);
			} else {
				this.onTouchMovedDuringDrag(event as TouchEvent);
			}
		}
		this.onMouseUpDuringDragListener = () => {
			this.onMouseUpDuringDrag();
		}
		contentComponent.componentElement.addEventListener("close", (event) => {
			this.onClose(event);
		})
		this.contentComponent.parentComponent = this;

		// add window to DOM
		let popupcontainer = document.getElementById("popupcontainer")!;
		popupcontainer.appendChild(this.componentElement);

		this.deferPreferredWidthCapture();
	}


	public close() {
		this.componentElement.remove();
		this.contentComponent.onParentClose();
		this.opened = false;
		this.contentComponent.parentComponent = undefined;
	}

	private onClose(event: Event) {
		this.close();
		event.preventDefault();
	}

	public isOpen() {
		return this.opened;
	}

	public isMinimized() {
		return this.minimized;
	}

	/**
	 * start draging of popup window
	 */
	private onMouseDown(event: MouseEvent) {
		window.addEventListener("mousemove", this.onMouseMovedDuringDragListener, true);
		window.addEventListener("mouseup", this.onMouseUpDuringDragListener, true);
		window.addEventListener("touchmove", this.onMouseMovedDuringDragListener, true);
		window.addEventListener("touchend", this.onMouseUpDuringDragListener, true);

		event.preventDefault();
		let box = this.componentElement.getBoundingClientRect();
		this.offsetX = event.clientX - box.left - window.pageXOffset;
		this.offsetY = event.clientY - box.top - window.pageYOffset;
	}

	private onTouchStart(event: TouchEvent) {
		const firstT = event.changedTouches[0];
		const simulated = new MouseEvent("mousedown", {
			screenX: firstT.screenX, screenY: firstT.screenY,
			clientX: firstT.clientX, clientY: firstT.clientY
		})
		firstT.target.dispatchEvent(simulated);

		event.preventDefault();
	}

	/**
	 * updates position of popup window during drag
	 */
	private onMouseMovedDuringDrag(event: MouseEvent) {
		this.componentElement.style.left = event.clientX - this.offsetX + 'px';
		this.componentElement.style.top = event.clientY - this.offsetY + 'px';
		this.onMoved();
	}

	private onTouchMovedDuringDrag(event: TouchEvent) {
		const firstT = event.changedTouches[0];
		const simulated = new MouseEvent("mousemove", {
			screenX: firstT.screenX, screenY: firstT.screenY,
			clientX: firstT.clientX, clientY: firstT.clientY
		})
		firstT.target.dispatchEvent(simulated);

		// FIXME: how to disable scrolling
		//event.preventDefault();
	}

	/**
	 * deregister global event listeners used for dragging popup window
	 */
	private onMouseUpDuringDrag() {
		window.removeEventListener("mousemove", this.onMouseMovedDuringDragListener, true);
		window.removeEventListener("mouseup", this.onMouseUpDuringDragListener, true);
		window.removeEventListener("touchmove", this.onMouseMovedDuringDragListener, true);
		window.removeEventListener("touchend", this.onMouseUpDuringDragListener, true);
		this.onMoved();
	}

	/**
	 * Keeps dialog window within browser page.
	 */
	private checkPos(): Point {
		if (this.content) {
			this.content.onMoved();
		}

		const dialogArea = this.componentElement.getBoundingClientRect();
		const clientArea = document.documentElement.getBoundingClientRect();

		// clientArea.height is 0, if there are now child elements (e. g. on login / choose character dialogs)
		let clientAreaHeight = clientArea.height;
		if (clientAreaHeight == 0) {
			clientAreaHeight = window.visualViewport?.height || 200;
		}

		const offset = stendhal.ui.getPageOffset();

		let newX = dialogArea.x;
		let newY = dialogArea.y;

		if (newX < 0) {
			newX = 0;
			this.componentElement.style.left = (offset.x + newX) + "px";
		} else if (dialogArea.x + dialogArea.width > clientArea.right + offset.x) {
			newX = clientArea.right - dialogArea.width;
			this.componentElement.style.left = (offset.x + newX) + "px";
		}
		if (newY < 0) {
			newY = 0;
			this.componentElement.style.top = (offset.y + newY) + "px";
		} else if (dialogArea.y + dialogArea.height > clientAreaHeight) {
			newY = clientAreaHeight - dialogArea.height;
			this.componentElement.style.top = (offset.y + newY) + "px";
		}

		return new Point(newX + offset.x, newY + offset.y);
	}

	public override onMoved() {
		const pos = this.checkPos();
		if (typeof (this.windowId) !== "undefined") {
			stendhal.config.setWindowState(this.windowId, pos.x, pos.y);
		}
	}

	public setId(id: string | undefined) {
		this.windowId = id;
	}

	/**
	 * Sets visibility of close button.
	 *
	 * Default is shown.
	 *
	 * @param {boolean} enable
	 *   Set to `false` to hide.
	 */
	enableCloseButton(enable: boolean) {
		this.child(".windowtitleclose")!.style.display = enable ? "" : "none";
	}

	public enableMinimizeButton(enable: boolean) {
		if (!this.minimizeButton) {
			return;
		}

		this.minimizeEnabled = enable;
		if (enable) {
			this.minimizeButton.style.display = "";
			this.minimizeButton.setAttribute("aria-hidden", "false");
			this.minimizeButton.tabIndex = 0;
			this.updateMinimizeButtonState();
		} else {
			this.setMinimized(false);
			this.minimizeButton.style.display = "none";
			this.minimizeButton.setAttribute("aria-hidden", "true");
			this.minimizeButton.tabIndex = -1;
		}
	}

	public setMinimized(minimized: boolean) {
		const nextState = minimized && this.minimizeEnabled;
		if (this.minimized === nextState) {
			return;
		}

		this.ensurePreferredWidth();

		this.minimized = nextState;
		if (this.contentWrapper) {
			this.contentWrapper.style.display = this.minimized ? "none" : "";
		}
		this.componentElement.classList.toggle("windowdiv--minimized", this.minimized);
		this.updateMinimizeButtonState();
		this.playToggleSound();

		if (!this.minimized && this.fixedWidth === undefined) {
			requestAnimationFrame(() => this.capturePreferredWidth());
		}
	}

	private toggleMinimized() {
		this.setMinimized(!this.minimized);
	}

	private updateMinimizeButtonState() {
		if (!this.minimizeButton) {
			return;
		}

		const label = this.minimized ? "Przywróć okno" : "Zminimalizuj okno";
		this.minimizeButton.setAttribute("aria-expanded", (!this.minimized).toString());
		this.minimizeButton.setAttribute("aria-label", label);
	}

	private playToggleSound() {
		if (!this.minimizeEnabled) {
			return;
		}

		const soundService = stendhal?.sound;
		const play = soundService?.playGlobalizedEffect;
		if (typeof play !== "function") {
			return;
		}

		try {
			play.call(soundService, this.toggleSound);
		} catch (err) {
			console.warn("Unable to play window toggle sound", err);
		}
	}

	private deferPreferredWidthCapture() {
		if (this.fixedWidth !== undefined) {
			this.applyPreferredWidth();
			return;
		}

		if (typeof queueMicrotask === "function") {
			queueMicrotask(() => this.capturePreferredWidth());
			return;
		}

		setTimeout(() => this.capturePreferredWidth(), 0);
	}

	private ensurePreferredWidth() {
		if (this.fixedWidth !== undefined) {
			this.preferredWidth = this.fixedWidth;
			this.applyPreferredWidth();
			return;
		}

		if (this.preferredWidth !== undefined) {
			this.applyPreferredWidth();
			return;
		}

		this.capturePreferredWidth();
	}

	private capturePreferredWidth() {
		if (this.fixedWidth !== undefined) {
			this.preferredWidth = this.fixedWidth;
			this.applyPreferredWidth();
			return;
		}

		if (!this.componentElement.isConnected) {
			return;
		}

		const rect = this.componentElement.getBoundingClientRect();
		const width = Math.ceil(rect.width);
		if (!width) {
			return;
		}

		this.preferredWidth = width;
		this.applyPreferredWidth();
	}

	private applyPreferredWidth() {
		if (this.preferredWidth === undefined) {
			return;
		}

		const widthValue = `${this.preferredWidth}px`;
		this.componentElement.style.minWidth = widthValue;

		if (this.fixedWidth !== undefined || this.minimized) {
			this.componentElement.style.width = widthValue;
		} else {
			this.componentElement.style.removeProperty("width");
		}
	}

	public setFixedWidth(width: number) {
		if (!Number.isFinite(width)) {
			return;
		}

		const sanitized = Math.max(1, Math.round(width));
		this.fixedWidth = sanitized;
		this.preferredWidth = sanitized;
		this.applyPreferredWidth();
	}
}
