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

declare var stendhal: any;

import { Color } from "../data/color/Color";

export interface TextSegment {
	text: string;
	color: string;
	underline?: boolean;
	italic?: boolean;
}

export abstract class TextBubble {

	protected static readonly STANDARD_DUR = 5000;
	protected static readonly adjustY = 15;

	protected text: string;
	protected timeStamp: number;
	protected x = -1;
	protected y = -1;
	protected width = -1;
	protected height = -1;
	protected duration = TextBubble.STANDARD_DUR;

	protected onRemovedAction?: Function;


	constructor(text: string) {
		this.text = text;
		this.timeStamp = Date.now();
	}

	/**
	 * Reinitialises the bubble's timing and layout metrics so an instance
	 * can be reused without allocating a new object.
	 *
	 * @param text
	 *   Text to display.
	 * @param duration
	 *   Optional duration override.
	 */
	protected resetBubble(text: string, duration = TextBubble.STANDARD_DUR) {
		this.text = text;
		this.timeStamp = Date.now();
		this.width = -1;
		this.height = -1;
		this.duration = duration;
	}

	/**
	 * Handles drawing the sprite on the screen.
	 *
	 * @param ctx
	 *     Drawing canvas.
	 * @return
	 *     <code>true</code> if the sprites duration time has expired
	 *     & should be removed from the screen.
	 */
	abstract draw(ctx: CanvasRenderingContext2D): boolean;

	getX(): number {
		return this.x;
	}

	getY(): number {
		return this.y;
	}

	getWidth(): number {
		return this.width;
	}

	getHeight(): number {
		return this.height;
	}

	/**
	 * Checks if a point or area clips the boundries of this sprite.
	 *
	 * @param x1
	 *     Left-most position of area to check.
	 * @param x2
	 *     Right-most position of area to check.
	 * @param y1
	 *     Top-most position of area to check.
	 * @param y2
	 *     Bottom-most position of area to check.
	 * @return
	 *     <code>true</code> if any point is within the area of the
	 *     sprite.
	 */
	clips(x1: number, x2: number, y1: number, y2: number): boolean {
		const x = this.getX(), y = this.getY();
		const r = x + this.getWidth();
		const b = y + this.getHeight();

		return (x <= x1 && x1 <= r && x <= x2 && x2 <= r)
			&& (y <= y1 && y1 <= b && y <= y2 && y2 <= b);
	}

	/**
	 * Checks if a point clips the boundaries of this sprite.
	 *
	 * @param x
	 *     Horizonal coordinate.
	 * @param y
	 *     Vertical coordinate.
	 * @return
	 *     <code>true</code> if the point is within the area of the
	 *     sprite.
	 */
	clipsPoint(x: number, y: number) {
		return this.clips(x, x, y, y);
	}

	/**
	 * Executed when viewport is clicked/tapped to detect text bubble boundaries & remove.
	 *
	 * @param evt
	 *   Mouse or touch event executed on canvas context.
	 */
	onClick(evt: MouseEvent | TouchEvent) {
		const pos = stendhal.ui.html.extractPosition(evt);
		const screenRect = document.getElementById("viewport")!.getBoundingClientRect();
		const pointX = pos.clientX - screenRect.x + stendhal.ui.gamewindow.offsetX;
		const pointY = pos.clientY - screenRect.y + stendhal.ui.gamewindow.offsetY + TextBubble.adjustY;
		if (this.clipsPoint(pointX, pointY)) {
			evt.stopPropagation();
			// remove when clicked or tapped
			stendhal.ui.gamewindow.removeTextBubble(this, pointX, pointY);
		}
	}

	/**
	 * Action to execute when sprite is added to viewport.
	 *
	 * Adds a listener to remove sprite with mouse click or touch.
	 *
	 * @param ctx
	 *   Canvas context on which text bubble is drawn.
	 */
	onAdded(ctx: CanvasRenderingContext2D) {
		// prevent multiple listeners from being added
		if (typeof (this.onRemovedAction) === "undefined") {
			// add click listener to remove chat bubble
			const listener = (e: MouseEvent | TouchEvent) => {
				// FIXME: should only execute if click/touch hasn't moved too far
				this.onClick(e);
			};
			ctx.canvas.addEventListener("click", listener);
			ctx.canvas.addEventListener("touchend", listener);
			this.onRemovedAction = function() {
				ctx.canvas.removeEventListener("click", listener);
				ctx.canvas.removeEventListener("touchend", listener);
			};
		}
	}

	/**
	 * Action to execute when sprite is removed from viewport.
	 *
	 * Removes the listener added with TextBubble.onAdded.
	 */
	onRemoved() {
		if (typeof (this.onRemovedAction) !== "undefined") {
			this.onRemovedAction();
			this.onRemovedAction = undefined;
		}
	}

	/**
	 * Sets the duration that the sprite should be displayed on the
	 * screen.
	 *
	 * @param dur
	 *     New duration time.
	 */
	setDuration(dur: number) {
		if (dur == 0) {
			this.duration = TextBubble.STANDARD_DUR;
		} else {
			this.duration = dur;
		}
	}

	/**
	 * Checks if the sprite duration time has expired & should be
	 * removed from the screen.
	 *
	 * @return
	 *     <code>true</code> if the duration time has been exceeded.
	 */
	expired(): boolean {
		return Date.now() >= this.timeStamp + this.duration;
	}

	/**
	 * Identifies indexes for formatting & removes symbols from text.
	 *
	 * @param parts {util.Pair.Pair[]}
	 *   Array to be populated.
	 * @param defaultColor {string}
	 *   Unformatted text color (default: `util.Color.Color.CHAT_NORMALBLACK`).
	 */
	protected segregate(parts: TextSegment[], defaultColor = Color.CHAT_NORMALBLACK) {
		const delims = [" ", ",", ".", "!", "?", ":", ";"];
		let highlight = false;
		let underline = false;
		let underlineColor = false;
		let admin = false;
		let highlightQuote = false;
		let underlineQuote = false;
		let underlineColorQuote = false;
		let adminQuote = false;
		let buffer = "";

		const currentStyle = () => {
			let color = defaultColor;
			if (underlineColor) {
				color = Color.CHAT_UNDERLINE;
			} else if (highlight) {
				color = Color.CHAT_HIGHLIGHT;
			} else if (admin) {
				color = Color.CHAT_ADMIN;
			}
			return {
				color,
				underline: underline || underlineColor,
				italic: admin
			};
		};
		let style = currentStyle();

		const flush = () => {
			if (!buffer) {
				return;
			}
			parts.push({
				text: buffer,
				color: style.color,
				underline: style.underline,
				italic: style.italic
			});
			buffer = "";
		};

		for (let idx = 0; idx < this.text.length; idx++) {
			const c = this.text[idx];

			if (c === "\\") {
				const next = this.text[idx + 1];
				if (typeof next !== "undefined") {
					buffer += next;
					idx++;
				}
				continue;
			} else if (c === "#") {
				if (highlight) {
					buffer += c;
					continue;
				}
				const next = this.text[idx + 1];
				if (next === "#") {
					buffer += c;
					idx++;
					continue;
				}
				if (next === "'") {
					highlightQuote = true;
					idx++;
				}
				flush();
				highlight = true;
				style = currentStyle();
				continue;
			} else if (c === "§") {
				if (underline) {
					buffer += c;
					continue;
				}
				const next = this.text[idx + 1];
				if (next === "§") {
					buffer += c;
					idx++;
					continue;
				}
				if (next === "'") {
					underlineQuote = true;
					idx++;
				}
				flush();
				underline = true;
				style = currentStyle();
				continue;
			} else if (c === "~") {
				if (underlineColor) {
					buffer += c;
					continue;
				}
				const next = this.text[idx + 1];
				if (next === "~") {
					buffer += c;
					idx++;
					continue;
				}
				if (next === "'") {
					underlineColorQuote = true;
					idx++;
				}
				flush();
				underlineColor = true;
				style = currentStyle();
				continue;
			} else if (c === "¡") {
				if (admin) {
					buffer += c;
					continue;
				}
				const next = this.text[idx + 1];
				if (next === "¡") {
					buffer += c;
					idx++;
					continue;
				}
				if (next === "'") {
					adminQuote = true;
					idx++;
				}
				flush();
				admin = true;
				style = currentStyle();
				continue;
			} else if (c === "'") {
				if (adminQuote) {
					flush();
					admin = false;
					adminQuote = false;
					style = currentStyle();
					continue;
				}
				if (underlineColorQuote) {
					flush();
					underlineColor = false;
					underlineColorQuote = false;
					style = currentStyle();
					continue;
				}
				if (underlineQuote) {
					flush();
					underline = false;
					underlineQuote = false;
					style = currentStyle();
					continue;
				}
				if (highlightQuote) {
					flush();
					highlight = false;
					highlightQuote = false;
					style = currentStyle();
					continue;
				}
				buffer += c;
				continue;
			} else if (delims.indexOf(c) > -1) {
				const next = this.text[idx + 1];
				if (c === " " || next === " " || typeof next === "undefined") {
					if (admin && !adminQuote && !highlightQuote && !underlineQuote && !underlineColorQuote) {
						flush();
						admin = false;
						style = currentStyle();
					}
					if (underlineColor && !underlineColorQuote && !highlightQuote && !underlineQuote && !adminQuote) {
						flush();
						underlineColor = false;
						style = currentStyle();
					}
					if (underline && !underlineQuote && !highlightQuote) {
						flush();
						underline = false;
						style = currentStyle();
					}
					if (highlight && !underlineQuote && !highlightQuote && !underlineColorQuote && !adminQuote) {
						flush();
						highlight = false;
						style = currentStyle();
					}
				}
				buffer += c;
				continue;
			}

			buffer += c;
		}

		flush();

		this.text = parts.map((part) => part.text).join("");
	}
}
