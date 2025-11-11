declare var stendhal: any;

const TILE_EDGE_TRIM = 0.02;

export interface TileLayerOptions {
	composite?: GlobalCompositeOperation;
}

export default class TileLayerPainter {

	public static drawLayerByName(ctx: CanvasRenderingContext2D, name: string, tileOffsetX: number,
			tileOffsetY: number, targetTileWidth: number, targetTileHeight: number, options?: TileLayerOptions): boolean {
		const index = stendhal.data.map.layerNames.indexOf(name);
		if (index < 0) {
			return false;
		}
		const layer = stendhal.data.map.layers[index];
		if (!layer) {
			return false;
		}
		ctx.save();
		const composite = options?.composite;
		if (typeof(composite) !== "undefined") {
			ctx.globalCompositeOperation = composite;
		}
		TileLayerPainter.drawLayerTiles(ctx, layer, tileOffsetX, tileOffsetY, targetTileWidth, targetTileHeight);
		ctx.restore();
		return true;
	}

	private static drawLayerTiles(ctx: CanvasRenderingContext2D, layer: number[], tileOffsetX: number,
			tileOffsetY: number, targetTileWidth: number, targetTileHeight: number): void {
		const map = stendhal.data.map;
		const canvas = ctx.canvas;
		const yMax = Math.min(tileOffsetY + canvas.height / targetTileHeight + 1, map.zoneSizeY);
		const xMax = Math.min(tileOffsetX + canvas.width / targetTileWidth + 1, map.zoneSizeX);

		for (let y = tileOffsetY; y < yMax; y++) {
			for (let x = tileOffsetX; x < xMax; x++) {
				let gid = layer[y * map.zoneSizeX + x];
				const flip = gid & 0xE0000000;
				gid &= 0x1FFFFFFF;
				if (gid <= 0) {
					continue;
				}
				const tilesetIndex = map.getTilesetForGid(gid);
				const tileset = map.aImages[tilesetIndex];
				if (!tileset || tileset.height <= 0) {
					continue;
				}
				const base = map.firstgids[tilesetIndex];
				const idx = gid - base;
				TileLayerPainter.drawTile(
				ctx,
				tileset,
				idx,
				x * targetTileWidth,
				y * targetTileHeight,
				targetTileWidth,
				targetTileHeight,
				flip
				);
			}
		}
	}

	private static drawTile(ctx: CanvasRenderingContext2D, tileset: HTMLImageElement, idx: number, screenX: number, screenY: number,
			destWidth: number, destHeight: number, flip: number): void {
		const tilesetWidth = tileset.width;
		const tilesPerRow = Math.floor(tilesetWidth / stendhal.data.map.tileWidth);
		const sourceX = (idx % tilesPerRow) * stendhal.data.map.tileWidth + TILE_EDGE_TRIM;
		const sourceY = Math.floor(idx / tilesPerRow) * stendhal.data.map.tileHeight + TILE_EDGE_TRIM;
		const sourceWidth = stendhal.data.map.tileWidth - TILE_EDGE_TRIM * 2;
		const sourceHeight = stendhal.data.map.tileHeight - TILE_EDGE_TRIM * 2;

		if (flip === 0) {
			ctx.drawImage(tileset, sourceX, sourceY, sourceWidth, sourceHeight, screenX, screenY, destWidth, destHeight);
			return;
		}

		ctx.save();
		ctx.translate(screenX, screenY);

		if ((flip & 0x80000000) !== 0) {
			ctx.scale(-1, 1);
			ctx.translate(-destWidth, 0);
		}
		if ((flip & 0x40000000) !== 0) {
			ctx.scale(1, -1);
			ctx.translate(0, -destHeight);
		}
		if ((flip & 0x20000000) !== 0) {
			ctx.transform(0, 1, 1, 0, 0, 0);
		}

		ctx.drawImage(tileset, sourceX, sourceY, sourceWidth, sourceHeight, 0, 0, destWidth, destHeight);

		ctx.restore();
	}
}

export const drawLayerByName = TileLayerPainter.drawLayerByName;