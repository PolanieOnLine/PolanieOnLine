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

import { RenderingContext2D } from "util/Types";
import { Color } from "../data/color/Color";
import { Pair } from "../util/Pair";


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
	 * Handles drawing the sprite on the screen.
	 *
	 * @param ctx
	 *     Drawing canvas.
	 * @return
	 *     <code>true</code> if the sprites duration time has expired
	 *     & should be removed from the screen.
	 */
	abstract draw(ctx: RenderingContext2D): boolean;

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
	onAdded(ctx: RenderingContext2D) {
		// TODO: Reference the Viewport directly instead of deriving the canvas from the context
		if (!(ctx instanceof CanvasRenderingContext2D)) {
			console.error("Cannot add event listeners because HTMLCanvasElement is required.");
			return;
		}

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
	protected segregate(parts: Pair<string, string>[], defaultColor = Color.CHAT_NORMALBLACK) {
		const formatted = TextBubble.buildFormattedParts(this.text, defaultColor);
		for (const part of formatted.parts) {
			parts.push(part);
		}
		this.text = formatted.plainText;
	}

	protected static buildFormattedParts(text: string, defaultColor = Color.CHAT_NORMALBLACK)
			: { plainText: string; parts: Pair<string, string>[] } {
		const styleColors: Record<string, string|undefined> = {
			"#": Color.CHAT_HIGHLIGHT,
			"§": undefined,
			"~": Color.CHAT_UNDERLINE,
			"¡": Color.CHAT_ADMIN
		};
		const delims = [" ", ",", ".", "!", "?", ":", ";"];
		const parts: Pair<string, string>[] = [];
		const styleStack: { type: string; color?: string }[] = [];
		let currentText = "";

		const pushSegment = (color: string) => {
			if (currentText.length > 0) {
				parts.push(new Pair(color, currentText));
				currentText = "";
			}
		};

		const currentColor = () => {
			for (let i = styleStack.length - 1; i >= 0; i--) {
				if (styleStack[i].color) {
					return styleStack[i].color as string;
				}
			}
			return defaultColor;
		};

		const removeStyle = (type: string) => {
			for (let i = styleStack.length - 1; i >= 0; i--) {
				if (styleStack[i].type === type) {
					styleStack.splice(i, 1);
					break;
				}
			}
		};

		let inHighlight = false, inUnderline = false, inUnderlineColor = false, inAdmin = false;
		let inHighlightQuote = false, inUnderlineQuote = false, inUnderlineColorQuote = false, inAdminQuote = false;
		let color = defaultColor;

		for (let idx = 0; idx < text.length; idx++) {
			const char = text[idx];

			if (char === "\\") {
				const next = text[idx + 1];
				currentText += next;
				idx++;
				continue;
			}

			if (char === "#") {
				if (inHighlight) {
					currentText += char;
					continue;
				}
				const next = text[idx + 1];
				if (next === "#") {
					currentText += char;
					idx++;
					continue;
				}
				if (next === "'") {
					inHighlightQuote = true;
					idx++;
				}
				pushSegment(color);
				inHighlight = true;
				styleStack.push({ type: "#", color: styleColors["#"] });
				color = currentColor();
				continue;
			}

			if (char === "§") {
				if (inUnderline) {
					currentText += char;
					continue;
				}
				const next = text[idx + 1];
				if (next === "§") {
					currentText += char;
					idx++;
					continue;
				}
				if (next === "'") {
					inUnderlineQuote = true;
					idx++;
				}
				pushSegment(color);
				inUnderline = true;
				styleStack.push({ type: "§", color: styleColors["§"] });
				color = currentColor();
				continue;
			}

			if (char === "~") {
				if (inUnderlineColor) {
					currentText += char;
					continue;
				}
				const next = text[idx + 1];
				if (next === "~") {
					currentText += char;
					idx++;
					continue;
				}
				if (next === "'") {
					inUnderlineColorQuote = true;
					idx++;
				}
				pushSegment(color);
				inUnderlineColor = true;
				styleStack.push({ type: "~", color: styleColors["~"] });
				color = currentColor();
				continue;
			}

			if (char === "¡") {
				if (inAdmin) {
					currentText += char;
					continue;
				}
				const next = text[idx + 1];
				if (next === "¡") {
					currentText += char;
					idx++;
					continue;
				}
				if (next === "'") {
					inAdminQuote = true;
					idx++;
				}
				pushSegment(color);
				inAdmin = true;
				styleStack.push({ type: "¡", color: styleColors["¡"] });
				color = currentColor();
				continue;
			}

			if (char === "'") {
				if (inAdminQuote) {
					pushSegment(color);
					inAdmin = false;
					inAdminQuote = false;
					removeStyle("¡");
					color = currentColor();
					continue;
				}
				if (inUnderlineColorQuote) {
					pushSegment(color);
					inUnderlineColor = false;
					inUnderlineColorQuote = false;
					removeStyle("~");
					color = currentColor();
					continue;
				}
				if (inUnderlineQuote) {
					pushSegment(color);
					inUnderline = false;
					inUnderlineQuote = false;
					removeStyle("§");
					color = currentColor();
					continue;
				}
				if (inHighlightQuote) {
					pushSegment(color);
					inHighlight = false;
					inHighlightQuote = false;
					removeStyle("#");
					color = currentColor();
					continue;
				}
				currentText += char;
				continue;
			}

			if (delims.indexOf(char) > -1) {
				const next = text[idx + 1];
				if (char === " " || next === " " || typeof(next) === "undefined") {
					if (inAdmin && !inAdminQuote && !inHighlightQuote && !inUnderlineQuote && !inUnderlineColorQuote) {
						pushSegment(color);
						inAdmin = false;
						removeStyle("¡");
						color = currentColor();
						currentText += char;
						continue;
					}
					if (inUnderlineColor && !inUnderlineColorQuote && !inHighlightQuote && !inUnderlineQuote && !inAdminQuote) {
						pushSegment(color);
						inUnderlineColor = false;
						removeStyle("~");
						color = currentColor();
						currentText += char;
						continue;
					}
					if (inUnderline && !inUnderlineQuote && !inHighlightQuote) {
						pushSegment(color);
						inUnderline = false;
						removeStyle("§");
						color = currentColor();
						currentText += char;
						continue;
					}
					if (inHighlight && !inUnderlineQuote && !inHighlightQuote && !inUnderlineColorQuote && !inAdminQuote) {
						pushSegment(color);
						inHighlight = false;
						removeStyle("#");
						color = currentColor();
						currentText += char;
						continue;
					}
				}
				currentText += char;
				continue;
			}

			currentText += char;
		}

		pushSegment(color);

		return {
			plainText: parts.map((p) => p.second).join(""),
			parts
		};
	}
}
