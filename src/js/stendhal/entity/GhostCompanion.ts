import { DomesticAnimal } from "./DomesticAnimal";
import { RenderingContext2D } from "util/Types";

/** A translucent companion whose hover continues while standing still. */
export class GhostCompanion extends DomesticAnimal {

	protected override getAnimationFrame(timeMillis: number): number {
		return [0, 1, 2, 1][Math.floor(timeMillis / 200) % 4];
	}

	override drawMain(ctx: RenderingContext2D) {
		const timeMillis = Date.now();
		const hoverOffset = -3 + Math.round(2 * Math.sin((timeMillis % 2400) * Math.PI * 2 / 2400));
		const visibility = Number(this["visibility"] ?? 100);
		ctx.save();
		try {
			ctx.globalAlpha *= Math.max(0, Math.min(100, visibility)) / 100;
			ctx.translate(0, hoverOffset);
			super.drawMain(ctx);
		} finally {
			ctx.restore();
		}
	}
}
