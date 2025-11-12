/***************************************************************************
 *                    Copyright © 2003-2024 - Stendhal                     *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

declare var stendhal: any;

import { Point } from "../util/Point";


/**
 * Manages touch events.
 */
export class TouchHandler {

	/** Property denoting a touch is engaged. */
	private touchEngaged = false;

	/** Threshold determining if time between touch start & touch end represents a "long" touch. */
	private readonly longTouchDuration = 300;
	/** Time at which touch was engaged. */
	private timestampTouchStart = 0;
	/** Time at which touch was released. */
	private timestampTouchEnd = 0;
        /** Property denoting an object is being "held". */
        private held = false;

        /** Position on page when touch event began. */
        private origin?: Point;
        /** Last tap position registered for double tap detection. */
        private lastTapPoint?: Point;
        /** Number of pixels touch can move before being vetoed as a long touch or double tap. */
        private readonly moveThreshold = 32;
        /** Maximum time between taps before they stop counting as a double tap. */
        private readonly doubleTapThreshold = 375;
        /** Timestamp when the last tap was recorded. */
        private lastTapTime = 0;
        /** Target element that received the previous tap. */
        private lastTapTarget: EventTarget|null = null;

	/** Singleton instance. */
	private static instance: TouchHandler;


	/**
	 * Retrieves singleton instance.
	 */
	static get(): TouchHandler {
		if (!TouchHandler.instance) {
			TouchHandler.instance = new TouchHandler();
		}
		return TouchHandler.instance;
	}

	/**
	 * Hidden singleton constructor.
	 */
	private constructor() {
		// do nothing
	}

	/**
	 * Checks for touch event.
	 *
	 * @param evt {Event}
	 *   Event to be checked.
	 * @return {boolean}
	 *   `true` if "evt" represents a `TouchEvent`.
	 */
	public isTouchEvent(evt: Event): boolean {
		return window["TouchEvent"] && evt instanceof TouchEvent;
	}

	/**
	 * Sets timestamp when touch applied.
	 *
	 * @param x {number}
	 *   Touch position relative to page on X axis.
	 * @param y {number}
	 *   Touch position relative to page on Y axis.
	 */
	onTouchStart(x: number, y: number) {
		this.timestampTouchStart = +new Date();
		this.touchEngaged = true;
		// TODO: handle object origin in `ui.HeldObject.HeldObject`
		this.origin = new Point(x, y);
	}

	/**
	 * Sets timestamp when touch released.
	 */
        onTouchEnd(_evt?: TouchEvent) {
                this.timestampTouchEnd = +new Date();
                this.touchEngaged = false;
        }

        /**
         * Register a tap and determine if it should be interpreted as a double tap.
         *
         * @param x {number}
         *   Tap position relative to page on X axis.
         * @param y {number}
         *   Tap position relative to page on Y axis.
         * @return {boolean}
         *   `true` if the tap completed a double tap gesture.
         */
        registerTap(x: number, y: number, target?: EventTarget): boolean {
                const now = +new Date();
                let isDoubleTap = false;

                if (this.lastTapTime) {
                        const timeDelta = now - this.lastTapTime;
                        const sameTarget = !target || !this.lastTapTarget || this.lastTapTarget === target;
                        if (sameTarget && timeDelta <= this.doubleTapThreshold && this.lastTapPoint) {
                                const withinX = Math.abs(this.lastTapPoint.x - x) <= this.moveThreshold;
                                const withinY = Math.abs(this.lastTapPoint.y - y) <= this.moveThreshold;
                                isDoubleTap = withinX && withinY;
                        }
                }

                if (isDoubleTap) {
                        this.lastTapTime = 0;
                        this.lastTapPoint = undefined;
                        this.lastTapTarget = null;
                } else {
                        this.lastTapTime = now;
                        this.lastTapPoint = new Point(x, y);
                        this.lastTapTarget = target || null;
                }

                return isDoubleTap;
        }

	/**
	 * Can be used to detect if a mouse event was triggered by touch.
	 *
	 * @return {boolean}
	 *   Value of `ui.TouchHandler.TouchHandler.touchEngaged` property.
	 */
	isTouchEngaged(): boolean {
		return this.touchEngaged;
	}

	/**
	 * Checks if a touch event represents a long touch after release.
	 *
	 * @param evt {Event}
	 *   Event object to be checked.
	 * @return {boolean}
	 *   `true` if touch was released after duration threshold.
	 */
	isLongTouch(evt?: Event): boolean {
		if (evt && !this.isTouchEvent(evt)) {
			return false;
		}
		const durationMatch = (this.timestampTouchEnd - this.timestampTouchStart > this.longTouchDuration);
		let positionMatch = true;
		if (evt && this.origin) {
			const pos = stendhal.ui.html.extractPosition(evt);
			// if position has moved too much it's not a long touch
			positionMatch = (Math.abs(pos.pageX - this.origin.x) <= this.moveThreshold)
					&& (Math.abs(pos.pageY - this.origin.y) <= this.moveThreshold);
		}
		return durationMatch && positionMatch;
	}

	/**
	 * Unsets `ui.TouchHandler.TouchHandler.origin` property.
	 */
	unsetOrigin() {
		this.origin = undefined;
	}

	/**
	 * Sets `ui.TouchHandler.TouchHandler.held` property.
	 */
	setHolding(held: boolean) {
		this.held = held;
	}

	/**
	 * Checks if an held object was initiated using touch.
	 */
	holding(): boolean {
		return this.held;
	}
}
