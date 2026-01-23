export const BASE_TILE_EDGE_TRIM = 0.02;

export interface TileOverlapMetrics {
	tileOverlap: number;
	overlapOffset: number;
	edgeTrim: number;
}

export function resolveTileScale(tileScale: number): number {
	return tileScale > 0 ? tileScale : 1;
}

export function getTileOverlapMetrics(tileScale: number, baseEdgeTrim = BASE_TILE_EDGE_TRIM, pixelRatio = 1, tileSize = 32): TileOverlapMetrics {
	const clampedScale = resolveTileScale(tileScale);
	const safePixelRatio = pixelRatio > 0 ? pixelRatio : 1;
	const effectivePixelRatio = safePixelRatio * clampedScale;
	const scaledTilePixels = tileSize * effectivePixelRatio;
	const alignmentPadding = clampedScale < 1 && Math.abs(scaledTilePixels - Math.round(scaledTilePixels)) > 0.001
		? 1
		: 0;
	const minOverlap = clampedScale < 1 ? Math.max(1, Math.ceil(2 / Math.max(1, effectivePixelRatio))) : 0;
	// Align overlap to avoid seams when downscaling on mobile devices.
	const tileOverlap = clampedScale < 1 ? Math.max(minOverlap, Math.ceil(2 / clampedScale)) + alignmentPadding : 0;
	const overlapOffset = tileOverlap ? Math.floor(tileOverlap / 2) : 0;
	const edgeTrim = clampedScale < 1 ? Math.max(baseEdgeTrim, baseEdgeTrim / clampedScale) : baseEdgeTrim;

	return {
		tileOverlap,
		overlapOffset,
		edgeTrim
	};
}
