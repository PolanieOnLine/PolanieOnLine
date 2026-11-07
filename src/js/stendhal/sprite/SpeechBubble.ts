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

import { TextBubble } from "./TextBubble";

import { Color } from "../data/color/Color";

import { RPEntity } from "../entity/RPEntity";

import { Pair } from "../util/Pair";
import { Speech } from "../util/Speech";
import { RenderingContext2D } from "util/Types";


export class SpeechBubble extends TextBubble {

	private entity: RPEntity;
	private offsetY: number;
	private laidOut = false;
	private static readonly TAIL_WIDTH = 8;

	/** Formatted sections. */
	private parts: Pair<string, string>[];


	constructor(text: string, entity: RPEntity) {
		text = text.replace(/\\\\/g, "\\");
		super((text.length > 30) ? (text.substring(0, 30) + "...") : text);
		this.entity = entity;

		this.parts = [];
		this.segregate(this.parts, Color.CHAT_NORMALBLACK);

		this.offsetY = 0;

		this.duration = Math.max(
			TextBubble.STANDARD_DUR,
			this.text.length * TextBubble.STANDARD_DUR / 50);
	}

	override draw(ctx: RenderingContext2D): boolean {
		ctx.lineWidth = 2;
		ctx.font = "14px Arial";
		ctx.fillStyle = Color.WHITE;
		ctx.strokeStyle = Color.BLACK;

		if (this.width < 0 || this.height < 0) {
			// get width of text
			this.width = ctx.measureText(this.text).width + 8;
			this.height = 20;
		}
		if (!this.laidOut) {
			this.findFreePosition();
			this.laidOut = true;
		}

		let x = this.getX(), y = this.getY();
		Speech.drawBubbleRounded(ctx, x, y, this.width, this.height);

		x += 4;
		ctx.save();
		for (const p of this.parts) {
			ctx.fillStyle = p.first;
			ctx.fillText(p.second, x, y + TextBubble.adjustY);
			x += ctx.measureText(p.second).width;
		}
		ctx.restore();

		return this.expired();
	}

	override getX(): number {
		// Java anchors the bubble's tail, rather than its body, to the entity's right edge.
		let x = (this.entity["_x"] + this.entity["width"]) * 32 + SpeechBubble.TAIL_WIDTH;
		if (this.entity.inView()) {
			const view = stendhal.ui.gamewindow;
			const size = view.getWorldViewportSize();
			x = Math.max(view.offsetX + SpeechBubble.TAIL_WIDTH + 1,
				Math.min(x, view.offsetX + size.width - this.width - 1));
		}
		return x;
	}

	override getY(): number {
		// Match Java's left/bottom anchor; the text baseline is not a positioning offset.
		let y = this.entity["_y"] * 32 - this.height + this.offsetY;
		if (this.entity.inView()) {
			const view = stendhal.ui.gamewindow;
			const size = view.getWorldViewportSize();
			y = Math.max(view.offsetY + 1, Math.min(y, view.offsetY + size.height - this.height - 1));
		}
		return y;
	}

	protected override getBoundsY(): number {
		return this.getY();
	}

	/** Measure first, then reserve the nearest free row, including the bubble's tail. */
	private findFreePosition() {
		const x = this.getX();
		const gap = 2;
		const bubbles = stendhal.ui.gamewindow.textSprites as SpeechBubble[];
		for (let row = 0; row <= 20; row++) {
			for (const direction of row === 0 ? [1] : [1, -1]) {
				this.offsetY = direction * row * (this.height + gap);
				const y = this.getY();
				const overlaps = bubbles.some(bubble => bubble !== this && !bubble.expired()
					&& bubble.getWidth() >= 0 && bubble.getHeight() >= 0
					&& x - SpeechBubble.TAIL_WIDTH < bubble.getX() + bubble.getWidth() + gap
					&& x + this.width + gap > bubble.getX() - SpeechBubble.TAIL_WIDTH
					&& y < bubble.getY() + bubble.getHeight() + gap
					&& y + this.height + gap > bubble.getY());
				if (!overlaps) {
					return;
				}
			}
		}
		this.offsetY = 0;
	}
}

