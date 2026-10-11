/* Copyright (C) 2026 - PolanieOnLine; AGPL version 3 or later. */
import { RenderingContext2D } from "util/Types";
import { Paths } from "../../data/Paths";
import { singletons } from "../../SingletonRepo";

export type CreatureArmor = "none" | "light" | "medium" | "heavy";
const LABELS: Record<CreatureArmor, string> = {
	none: "Brak pancerza", light: "Pancerz lekki", medium: "Pancerz średni", heavy: "Pancerz ciężki"
};

export function readCreatureArmor(value: unknown): CreatureArmor | undefined {
	return typeof value === "string" && Object.prototype.hasOwnProperty.call(LABELS, value)
		? value as CreatureArmor : undefined;
}

export function getCreatureArmorLabel(value: unknown): string | undefined {
	const armor = readCreatureArmor(value);
	return armor ? LABELS[armor] : undefined;
}

/** No animation, filters or image generation in the render loop. */
export function drawCreatureArmor(ctx: RenderingContext2D, value: unknown,
		barX: number, barY: number, barWidth: number, barHeight: number) {
	const armor = readCreatureArmor(value);
	if (!armor || armor === "none") {
		return;
	}
	const image = singletons.getSpriteStore().get(Paths.sprites + "/status/armor/" + armor + ".png");
	ctx.save();
	try {
		ctx.imageSmoothingEnabled = false;
		if (image.complete && image.width === 16 && image.height === 16) {
			ctx.drawImage(image, Math.round(barX) - 18, Math.round(barY));
		}
		ctx.strokeStyle = "#687483";
		ctx.lineWidth = 1;
		ctx.strokeRect(Math.round(barX) - 1.5, Math.round(barY) - 1.5,
				barWidth + 3, barHeight + 3);
	} finally {
		ctx.restore();
	}
}
