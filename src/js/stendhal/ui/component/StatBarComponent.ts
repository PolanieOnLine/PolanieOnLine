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

import { Component } from "../toolkit/Component";
import { Color } from "../../data/color/Color";
import { RenderingContext2D } from "util/Types";
import { drawLifeTideOverlay } from "../../util/LifeTideVisuals";

/**
 * TODO: move this to `ui.toolkit`.
 */
export class StatBarComponent extends Component {

	readonly canvas: HTMLCanvasElement;
	protected readonly ctx: RenderingContext2D;
	private ratio = 0;
	private surplusRatio = 0;
	private pulseFrame?: number;


	constructor(id?: string) {
		if (!id) {
			id = "statbar-template";
		}
		super(id);
		this.canvas = this.componentElement as HTMLCanvasElement;
		this.ctx = this.canvas.getContext("2d")!;
	}

	override setVisible(visible = true) {
		super.setVisible(visible);
		if (this.pulseFrame !== undefined) {
			cancelAnimationFrame(this.pulseFrame);
			this.pulseFrame = undefined;
		}
		if (visible && this.surplusRatio > 0) {
			this.paint();
		}
	}

	drawBase() {
		this.ctx.beginPath();
		this.ctx.fillStyle = Color.GRAY;
		this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
	}

	draw(ratio: number, surplusRatio = 0) {
		this.ratio = Number.isFinite(ratio) ? Math.max(0, Math.min(1, ratio)) : 0;
		this.surplusRatio = Number.isFinite(surplusRatio) ? Math.max(0, Math.min(1, surplusRatio)) : 0;
		if (this.pulseFrame !== undefined) {
			cancelAnimationFrame(this.pulseFrame);
			this.pulseFrame = undefined;
		}
		this.paint();
	}

	private paint() {
		this.ctx.beginPath();
		this.ctx.fillStyle = Color.BLACK;
		this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
		this.ctx.fillStyle = Color.getStatBarColor(this.ratio);
		this.ctx.fillRect(0, 0, this.canvas.width * this.ratio, this.canvas.height);
		drawLifeTideOverlay(this.ctx, 0, 0, this.canvas.width, this.canvas.height, this.surplusRatio);
		// The browser pauses these callbacks in background tabs and resumes them
		// when visible. Keep a pending frame so unchanged HP can breathe again.
		if (this.surplusRatio > 0 && this.canvas.isConnected
				&& this.canvas.getClientRects().length > 0) {
			this.pulseFrame = requestAnimationFrame(() => {
				this.pulseFrame = undefined;
				this.paint();
			});
		}
	}

}

