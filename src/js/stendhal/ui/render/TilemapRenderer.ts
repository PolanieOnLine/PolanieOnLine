import { drawLayerByName, TileLayerOptions } from "../../landscape/TileLayerPainter";

const TILE_EDGE_TRIM = 0.02;
const PARALLAX_SCROLL = 0.25;

interface WorkerFrameMessage {
        base?: ImageBitmap;
        roof?: ImageBitmap;
}

interface WorkerInstance {
        worker: Worker;
        awaitingFrame: boolean;
        atlasReady: boolean;
        configured: boolean;
}

const workerSource = `
const TILE_EDGE_TRIM = ${TILE_EDGE_TRIM};
const PARALLAX_SCROLL = ${PARALLAX_SCROLL};

let atlas = null;
let baseLayer = null;
let roofLayer = null;
let zoneWidth = 0;
let zoneHeight = 0;
let tileWidth = 32;
let tileHeight = 32;
let targetTileWidth = 32;
let targetTileHeight = 32;
let tilesPerRow = 0;
let parallax = null;
let parallaxWidth = 0;
let parallaxHeight = 0;
let pixelX = null;
let pixelY = null;
let baseCanvas = null;
let roofCanvas = null;
let baseCtx = null;
let roofCtx = null;

function ensureCaches() {
        if (!zoneWidth || !zoneHeight) {
                pixelX = null;
                pixelY = null;
                return;
        }
        if (!pixelX || pixelX.length !== zoneWidth) {
                pixelX = new Float32Array(zoneWidth);
                for (let i = 0; i < zoneWidth; i++) {
                        pixelX[i] = i * targetTileWidth;
                }
        }
        if (!pixelY || pixelY.length !== zoneHeight) {
                pixelY = new Float32Array(zoneHeight);
                for (let i = 0; i < zoneHeight; i++) {
                        pixelY[i] = i * targetTileHeight;
                }
        }
}

function ensureCanvas(width, height) {
        if (!baseCanvas) {
                baseCanvas = new OffscreenCanvas(width, height);
                baseCtx = baseCanvas.getContext("2d");
        } else if (baseCanvas.width !== width || baseCanvas.height !== height) {
                baseCanvas.width = width;
                baseCanvas.height = height;
                baseCtx = baseCanvas.getContext("2d");
        }
        if (!roofCanvas) {
                roofCanvas = new OffscreenCanvas(width, height);
                roofCtx = roofCanvas.getContext("2d");
        } else if (roofCanvas.width !== width || roofCanvas.height !== height) {
                roofCanvas.width = width;
                roofCanvas.height = height;
                roofCtx = roofCanvas.getContext("2d");
        }
}

function drawParallax(ctx, offsetX, offsetY, width, height) {
        if (!parallax || !parallaxWidth || !parallaxHeight) {
                return;
        }
        const startY = offsetY - ((offsetY * PARALLAX_SCROLL) % parallaxHeight);
        for (let dy = startY; dy < offsetY + height + parallaxHeight; dy += parallaxHeight) {
                const startX = offsetX - ((offsetX * PARALLAX_SCROLL) % parallaxWidth);
                for (let dx = startX; dx < offsetX + width + parallaxWidth; dx += parallaxWidth) {
                        ctx.drawImage(parallax, dx, dy);
                }
        }
}

function drawLayer(ctx, layer, offsetX, offsetY, width, height) {
        if (!ctx || !layer || !atlas || !pixelX || !pixelY || !tilesPerRow) {
                return;
        }
        const startX = Math.max(0, Math.floor(offsetX / targetTileWidth));
        const startY = Math.max(0, Math.floor(offsetY / targetTileHeight));
        const cols = Math.ceil((width + targetTileWidth) / targetTileWidth) + 1;
        const rows = Math.ceil((height + targetTileHeight) / targetTileHeight) + 1;
        const endX = Math.min(zoneWidth, startX + cols);
        const endY = Math.min(zoneHeight, startY + rows);
        const sourceWidth = tileWidth - TILE_EDGE_TRIM * 2;
        const sourceHeight = tileHeight - TILE_EDGE_TRIM * 2;
        for (let y = startY; y < endY; y++) {
                const destY = pixelY[y];
                const rowIndex = y * zoneWidth;
                for (let x = startX; x < endX; x++) {
                        const index = layer[rowIndex + x];
                        if (index < 0) {
                                continue;
                        }
                        const srcX = (index % tilesPerRow) * tileWidth + TILE_EDGE_TRIM;
                        const srcY = Math.floor(index / tilesPerRow) * tileHeight + TILE_EDGE_TRIM;
                        ctx.drawImage(atlas, srcX, srcY, sourceWidth, sourceHeight, pixelX[x], destY,
                                targetTileWidth, targetTileHeight);
                }
        }
}

self.onmessage = (event) => {
        const data = event.data;
        switch (data.type) {
                case "configure": {
                        zoneWidth = data.zoneWidth | 0;
                        zoneHeight = data.zoneHeight | 0;
                        tileWidth = data.tileWidth;
                        tileHeight = data.tileHeight;
                        targetTileWidth = data.targetTileWidth;
                        targetTileHeight = data.targetTileHeight;
                        tilesPerRow = data.tilesPerRow | 0;
                        ensureCaches();
                        break;
                }
                case "layers": {
                        baseLayer = data.base ? new Int32Array(data.base) : null;
                        roofLayer = data.roof ? new Int32Array(data.roof) : null;
                        break;
                }
                case "atlas": {
                        atlas = data.atlas || null;
                        break;
                }
                case "parallax": {
                        parallax = data.image || null;
                        parallaxWidth = data.width || 0;
                        parallaxHeight = data.height || 0;
                        break;
                }
                case "frame": {
                        if (!atlas || !baseLayer) {
                                self.postMessage({type: "frame"});
                                break;
                        }
                        ensureCaches();
                        ensureCanvas(data.width, data.height);
                        if (!baseCtx || !roofCtx) {
                                self.postMessage({type: "frame"});
                                break;
                        }
                        baseCtx.setTransform(1, 0, 0, 1, 0, 0);
                        baseCtx.clearRect(0, 0, baseCanvas.width, baseCanvas.height);
                        baseCtx.imageSmoothingEnabled = false;
                        if (parallax) {
                                drawParallax(baseCtx, data.offsetX, data.offsetY, baseCanvas.width, baseCanvas.height);
                        }
                        drawLayer(baseCtx, baseLayer, data.offsetX, data.offsetY, baseCanvas.width, baseCanvas.height);
                        let baseBitmap = null;
                        try {
                                baseBitmap = baseCanvas.transferToImageBitmap();
                        } catch (error) {
                                baseBitmap = null;
                        }
                        let roofBitmap = null;
                        if (roofLayer) {
                                roofCtx.setTransform(1, 0, 0, 1, 0, 0);
                                roofCtx.clearRect(0, 0, roofCanvas.width, roofCanvas.height);
                                roofCtx.imageSmoothingEnabled = false;
                                drawLayer(roofCtx, roofLayer, data.offsetX, data.offsetY, roofCanvas.width, roofCanvas.height);
                                try {
                                        roofBitmap = roofCanvas.transferToImageBitmap();
                                } catch (error) {
                                        roofBitmap = null;
                                }
                        }
                        const transferables = [];
                        if (baseBitmap) {
                                transferables.push(baseBitmap);
                        }
                        if (roofBitmap) {
                                transferables.push(roofBitmap);
                        }
                        self.postMessage({type: "frame", base: baseBitmap, roof: roofBitmap}, transferables);
                        break;
                }
                case "dispose": {
                        atlas = null;
                        baseLayer = null;
                        roofLayer = null;
                        zoneWidth = 0;
                        zoneHeight = 0;
                        pixelX = null;
                        pixelY = null;
                        parallax = null;
                        parallaxWidth = 0;
                        parallaxHeight = 0;
                        baseCanvas = null;
                        roofCanvas = null;
                        baseCtx = null;
                        roofCtx = null;
                        break;
                }
        }
};
`;

let cspBlocksBlobWorker: boolean | undefined;
let workerSupportLogged = false;

function directiveAllowsBlob(source: string | undefined): boolean {
        if (!source) {
                return false;
        }
        return /(^|\s)blob:/.test(source);
}

function detectCspBlobRestriction(): boolean {
        if (cspBlocksBlobWorker !== undefined) {
                return cspBlocksBlobWorker;
        }
        if (typeof document === "undefined") {
                cspBlocksBlobWorker = false;
                return cspBlocksBlobWorker;
        }
        const policies = Array.from(document.querySelectorAll('meta[http-equiv="Content-Security-Policy"]'));
        for (const policy of policies) {
                const content = policy.getAttribute("content") || "";
                if (!content) {
                        continue;
                }
                const directives = content.split(";");
                let workerSrc: string | undefined;
                let scriptSrc: string | undefined;
                for (const directive of directives) {
                        const trimmed = directive.trim();
                        if (!trimmed) {
                                continue;
                        }
                        if (trimmed.startsWith("worker-src")) {
                                workerSrc = trimmed;
                        } else if (trimmed.startsWith("script-src")) {
                                scriptSrc = trimmed;
                        }
                }
                if (workerSrc && !directiveAllowsBlob(workerSrc)) {
                        cspBlocksBlobWorker = true;
                        return cspBlocksBlobWorker;
                }
                if (!workerSrc && scriptSrc && !directiveAllowsBlob(scriptSrc)) {
                        cspBlocksBlobWorker = true;
                        return cspBlocksBlobWorker;
                }
        }
        cspBlocksBlobWorker = false;
        return cspBlocksBlobWorker;
}

function createWorker(): Worker | undefined {
        if (typeof Worker === "undefined" || typeof OffscreenCanvas === "undefined") {
                cspBlocksBlobWorker = true;
                return undefined;
        }
        if (detectCspBlobRestriction()) {
                return undefined;
        }
        let url: string | undefined;
        try {
                const blob = new Blob([workerSource], {type: "application/javascript"});
                url = URL.createObjectURL(blob);
                const worker = new Worker(url);
                cspBlocksBlobWorker = false;
                return worker;
        } catch (error) {
                if (!workerSupportLogged) {
                        console.info("Tilemap renderer worker disabled", error);
                        workerSupportLogged = true;
                }
                cspBlocksBlobWorker = true;
                return undefined;
        } finally {
                if (url) {
                        URL.revokeObjectURL(url);
                }
        }
}

export class TilemapRenderer {

        private workerInstance?: WorkerInstance;
        private displayBase?: ImageBitmap;
        private displayRoof?: ImageBitmap;
        private baseLayer?: Int32Array;
        private roofLayer?: Int32Array;
        private zoneWidth = 0;
        private zoneHeight = 0;
        private tileWidth = 32;
        private tileHeight = 32;
        private targetTileWidth = 32;
        private targetTileHeight = 32;
        private tilesPerRow = 0;
        private pixelX?: Float32Array;
        private pixelY?: Float32Array;
        private parallaxImage?: HTMLImageElement;
        private lastTilesetToken?: unknown;
        private atlasCanvas?: HTMLCanvasElement;

        configure(map: any, targetTileWidth: number, targetTileHeight: number) {
                if (!map || !map.combinedTileset) {
                        return;
                }
                const tileset = map.combinedTileset;
                const tokenChanged = this.lastTilesetToken !== tileset;
                const targetChanged = this.targetTileWidth !== targetTileWidth || this.targetTileHeight !== targetTileHeight;
                if (!tokenChanged && !targetChanged) {
                        return;
                }
                this.lastTilesetToken = tileset;
                this.targetTileWidth = targetTileWidth;
                this.targetTileHeight = targetTileHeight;
                this.zoneWidth = map.zoneSizeX;
                this.zoneHeight = map.zoneSizeY;
                this.tileWidth = map.tileWidth;
                this.tileHeight = map.tileHeight;
                this.tilesPerRow = tileset.tilesPerRow;
                const baseLayer = tileset.combinedLayers[0];
                const roofLayer = tileset.combinedLayers[1];
                this.baseLayer = baseLayer ? new Int32Array(baseLayer) : undefined;
                this.roofLayer = roofLayer ? new Int32Array(roofLayer) : undefined;
                this.atlasCanvas = tileset.canvas as HTMLCanvasElement | undefined;
                this.pixelX = new Float32Array(this.zoneWidth);
                for (let i = 0; i < this.zoneWidth; i++) {
                        this.pixelX[i] = i * this.targetTileWidth;
                }
                this.pixelY = new Float32Array(this.zoneHeight);
                for (let i = 0; i < this.zoneHeight; i++) {
                        this.pixelY[i] = i * this.targetTileHeight;
                }
                this.resetDisplayBuffers();
                this.setupWorker(tileset);
        }

        updateParallax(image?: HTMLImageElement) {
                if (this.parallaxImage === image) {
                        return;
                }
                this.parallaxImage = image;
                if (!this.workerInstance || !this.workerInstance.worker) {
                        return;
                }
                if (!image) {
                        this.workerInstance.worker.postMessage({type: "parallax", image: null, width: 0, height: 0});
                        return;
                }
                if (typeof createImageBitmap !== "function" || !image.complete || image.naturalWidth <= 0) {
                        return;
                }
                createImageBitmap(image).then((bitmap) => {
                        if (!this.workerInstance) {
                                bitmap.close();
                                return;
                        }
                        this.workerInstance.worker.postMessage({
                                type: "parallax",
                                image: bitmap,
                                width: image.naturalWidth || image.width,
                                height: image.naturalHeight || image.height
                        }, [bitmap]);
                }).catch(() => {
                        // ignore
                });
        }

        prepareFrame(offsetX: number, offsetY: number, width: number, height: number) {
                if (!this.workerInstance || !this.workerInstance.worker) {
                        return;
                }
                if (this.workerInstance.awaitingFrame) {
                        return;
                }
                if (!this.workerInstance.atlasReady || !this.workerInstance.configured) {
                        return;
                }
                this.workerInstance.awaitingFrame = true;
                this.workerInstance.worker.postMessage({
                        type: "frame",
                        offsetX,
                        offsetY,
                        width,
                        height
                });
        }

        drawBaseLayer(ctx: CanvasRenderingContext2D, offsetX: number, offsetY: number, width: number, height: number) {
                const drewWorkerFrame = this.drawWorkerFrame(ctx, this.displayBase, offsetX, offsetY);
                if (drewWorkerFrame) {
                        return;
                }
                this.drawParallax(ctx, offsetX, offsetY, width, height);
                this.drawLayerDirect(ctx, this.baseLayer, offsetX, offsetY, width, height);
        }

        drawRoofLayer(ctx: CanvasRenderingContext2D, offsetX: number, offsetY: number, width: number, height: number) {
                const drewWorkerFrame = this.drawWorkerFrame(ctx, this.displayRoof, offsetX, offsetY);
                if (drewWorkerFrame) {
                        return;
                }
                this.drawLayerDirect(ctx, this.roofLayer, offsetX, offsetY, width, height);
        }

        drawBlendLayer(name: string, ctx: CanvasRenderingContext2D, tileOffsetX: number, tileOffsetY: number,
                        options?: TileLayerOptions) {
                drawLayerByName(ctx, name, tileOffsetX, tileOffsetY, this.targetTileWidth, this.targetTileHeight, options);
        }

        private resetDisplayBuffers() {
                if (this.displayBase) {
                        this.displayBase.close();
                        this.displayBase = undefined;
                }
                if (this.displayRoof) {
                        this.displayRoof.close();
                        this.displayRoof = undefined;
                }
        }

        private setupWorker(tileset: any) {
                if (this.workerInstance) {
                        this.workerInstance.worker.terminate();
                        this.workerInstance = undefined;
                }
                const worker = createWorker();
                if (!worker) {
                        this.workerInstance = undefined;
                        return;
                }
                const instance: WorkerInstance = {
                        worker,
                        awaitingFrame: false,
                        atlasReady: false,
                        configured: false
                };
                worker.onmessage = (event: MessageEvent<WorkerFrameMessage & {type: string}>) => {
                        const data = event.data;
                        if (!data) {
                                instance.awaitingFrame = false;
                                return;
                        }
                        if (data.type === "frame") {
                                if (data.base) {
                                        if (this.displayBase) {
                                                this.displayBase.close();
                                        }
                                        this.displayBase = data.base;
                                }
                                if (data.roof) {
                                        if (this.displayRoof) {
                                                this.displayRoof.close();
                                        }
                                        this.displayRoof = data.roof;
                                }
                                instance.awaitingFrame = false;
                        }
                };
                worker.onerror = () => {
                        this.workerInstance = undefined;
                };
                const configureMessage = {
                        type: "configure",
                        zoneWidth: this.zoneWidth,
                        zoneHeight: this.zoneHeight,
                        tileWidth: this.tileWidth,
                        tileHeight: this.tileHeight,
                        targetTileWidth: this.targetTileWidth,
                        targetTileHeight: this.targetTileHeight,
                        tilesPerRow: this.tilesPerRow
                };
                worker.postMessage(configureMessage);
                const baseCopy = this.baseLayer ? this.baseLayer.slice() : undefined;
                const roofCopy = this.roofLayer ? this.roofLayer.slice() : undefined;
                const baseBuffer = baseCopy ? baseCopy.buffer : undefined;
                const roofBuffer = roofCopy ? roofCopy.buffer : undefined;
                const transferables: ArrayBuffer[] = [];
                if (baseBuffer) {
                        transferables.push(baseBuffer);
                }
                if (roofBuffer) {
                        transferables.push(roofBuffer);
                }
                worker.postMessage({
                        type: "layers",
                        base: baseBuffer ?? null,
                        roof: roofBuffer ?? null
                }, transferables);
                instance.configured = true;
                if (typeof createImageBitmap === "function" && tileset.canvas) {
                        createImageBitmap(tileset.canvas).then((bitmap) => {
                                if (!this.workerInstance || this.workerInstance.worker !== worker) {
                                        bitmap.close();
                                        return;
                                }
                                worker.postMessage({type: "atlas", atlas: bitmap}, [bitmap]);
                                instance.atlasReady = true;
                        }).catch(() => {
                                instance.atlasReady = false;
                        });
                }
                this.workerInstance = instance;
        }

        private drawWorkerFrame(ctx: CanvasRenderingContext2D, bitmap: ImageBitmap|undefined, offsetX: number, offsetY: number): boolean {
                if (!bitmap) {
                        return false;
                }
                ctx.drawImage(bitmap, offsetX, offsetY);
                return true;
        }

        private drawParallax(ctx: CanvasRenderingContext2D, offsetX: number, offsetY: number, width: number, height: number) {
                const image = this.parallaxImage;
                if (!image || !image.width || !image.height) {
                        return;
                }
                const imgWidth = image.width;
                const imgHeight = image.height;
                const startY = offsetY - ((offsetY * PARALLAX_SCROLL) % imgHeight);
                for (let dy = startY; dy < offsetY + height + imgHeight; dy += imgHeight) {
                        const startX = offsetX - ((offsetX * PARALLAX_SCROLL) % imgWidth);
                        for (let dx = startX; dx < offsetX + width + imgWidth; dx += imgWidth) {
                                ctx.drawImage(image, dx, dy);
                        }
                }
        }

        private drawLayerDirect(ctx: CanvasRenderingContext2D, layer: Int32Array|undefined, offsetX: number, offsetY: number,
                        width: number, height: number) {
                if (!layer || !this.pixelX || !this.pixelY || !this.tilesPerRow) {
                        return;
                }
                const startX = Math.max(0, Math.floor(offsetX / this.targetTileWidth));
                const startY = Math.max(0, Math.floor(offsetY / this.targetTileHeight));
                const cols = Math.ceil((width + this.targetTileWidth) / this.targetTileWidth) + 1;
                const rows = Math.ceil((height + this.targetTileHeight) / this.targetTileHeight) + 1;
                const endX = Math.min(this.zoneWidth, startX + cols);
                const endY = Math.min(this.zoneHeight, startY + rows);
                const sourceWidth = this.tileWidth - TILE_EDGE_TRIM * 2;
                const sourceHeight = this.tileHeight - TILE_EDGE_TRIM * 2;
                const atlasCanvas = this.atlasCanvas;
                if (!atlasCanvas) {
                        return;
                }
                for (let y = startY; y < endY; y++) {
                        const destY = this.pixelY[y];
                        const rowIndex = y * this.zoneWidth;
                        for (let x = startX; x < endX; x++) {
                                const index = layer[rowIndex + x];
                                if (index < 0) {
                                        continue;
                                }
                                const srcX = (index % this.tilesPerRow) * this.tileWidth + TILE_EDGE_TRIM;
                                const srcY = Math.floor(index / this.tilesPerRow) * this.tileHeight + TILE_EDGE_TRIM;
                                ctx.drawImage(atlasCanvas, srcX, srcY, sourceWidth, sourceHeight,
                                        this.pixelX[x], destY, this.targetTileWidth, this.targetTileHeight);
                        }
                }
        }
}

