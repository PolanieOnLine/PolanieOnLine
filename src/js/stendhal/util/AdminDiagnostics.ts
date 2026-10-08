import { marauroa } from "marauroa";

export type DiagnosticSwitch = "enabled" | "freezeIcons" | "simpleSlots" | "hideRarity";
type Switches = Record<DiagnosticSwitch, boolean>;
export interface DiagnosticSample {
	fps: number;
	frameMs: number;
	p95Ms: number;
	maxMs: number;
	slowFrames: number;
	drawMs: number;
	visibleItems: number;
	animatedItems: number;
	frames: number;
}

/** Local, session-only rendering experiments. Never sends actions or stores preferences. */
export class AdminDiagnostics {
	private static switches: Switches = { enabled: false, freezeIcons: false, simpleSlots: false, hideRarity: false };
	private static owner?: string;
	private static reportsOwner?: string;
	private static reports: string[] = [];
	private static revision = 0;
	private static intervals: number[] = [];
	private static drawTimes: number[] = [];
	private static inventories = new Map<object, { visible: number; animated: number }>();
	private static lastFrame?: number;
	private static lastHud = 0;
	private static hud?: HTMLElement;
	private static listening = false;
	private static visibilityChanged = () => { this.lastFrame = undefined; };

	public static isAdministrator(): boolean {
		const level = Number(marauroa.me?.["adminlevel"]);
		return Number.isFinite(level) && level > 600;
	}

	private static checkSession() {
		const name = this.playerIdentity();
		if ((this.owner !== undefined && (!this.isAdministrator() || name !== this.owner))
				|| (this.reportsOwner !== undefined && (!this.isAdministrator() || name !== this.reportsOwner))) {
			this.reset(true);
		}
	}

	private static playerIdentity(): string {
		return String(marauroa.me?.["name"] ?? marauroa.me?.["_name"] ?? marauroa.me?.["id"] ?? "");
	}

	public static get(key: DiagnosticSwitch): boolean {
		this.checkSession();
		return this.isAdministrator() && this.switches.enabled && this.switches[key];
	}

	public static getRevision(): number {
		this.checkSession();
		return this.revision;
	}

	public static set(key: DiagnosticSwitch, value: boolean): boolean {
		this.checkSession();
		if (!this.isAdministrator() || (key !== "enabled" && !this.switches.enabled)) {
			return false;
		}
		if (key === "enabled" && !value) {
			this.reset();
			return true;
		}
		this.owner = this.playerIdentity();
		this.switches[key] = value;
		this.revision++;
		this.clearMeasurements();
		this.applyStyles();
		return true;
	}

	public static reset(clearReports = false) {
		this.switches = { enabled: false, freezeIcons: false, simpleSlots: false, hideRarity: false };
		this.owner = undefined;
		if (clearReports) { this.reports = []; this.reportsOwner = undefined; }
		this.revision++;
		this.clearMeasurements();
		this.applyStyles();
	}

	private static applyStyles() {
		document.documentElement.classList.toggle("admin-diagnostic-simple-slots", this.get("simpleSlots"));
		if (this.get("enabled") && !this.listening) {
			document.addEventListener("visibilitychange", this.visibilityChanged);
			this.listening = true;
		}
		if (!this.get("enabled")) {
			if (this.listening) { document.removeEventListener("visibilitychange", this.visibilityChanged); }
			this.listening = false;
			this.hud?.remove();
			this.hud = undefined;
		}
	}

	public static clearMeasurements() {
		this.intervals = [];
		this.drawTimes = [];
		this.inventories.clear();
		this.lastFrame = undefined;
		this.lastHud = 0;
		if (this.hud) { this.hud.textContent = "Diagnostyka: zbieranie nowej próbki…"; }
	}

	public static inventory(owner: object, visible: number, animated: number) {
		if (this.get("enabled")) { this.inventories.set(owner, { visible, animated }); }
	}

	public static removeInventory(owner: object) { this.inventories.delete(owner); }

	/** Actual viewport draws, not video FPS or server ticks. Keeps at most 300 intervals. */
	public static frame(start: number, end: number) {
		this.checkSession();
		if (!this.get("enabled")) { return; }
		if (document.visibilityState === "hidden") {
			this.lastFrame = undefined;
			return;
		}
		if (this.lastFrame !== undefined) {
			const interval = start - this.lastFrame;
			if (interval > 0 && Number.isFinite(interval)) {
				this.intervals.push(interval);
				this.drawTimes.push(Math.max(0, end - start));
				if (this.intervals.length > 300) { this.intervals.shift(); this.drawTimes.shift(); }
			}
		}
		this.lastFrame = start;
		if (end - this.lastHud >= 1000) {
			this.lastHud = end;
			if (!this.hud) {
				this.hud = document.createElement("div");
				this.hud.className = "admin-diagnostic-hud";
				document.body.appendChild(this.hud);
			}
			const s = this.sample();
			this.hud.textContent = `Diagnostyka · ${s.fps.toFixed(1)} FPS · p95 ${s.p95Ms.toFixed(1)} ms · `
				+ `>50 ms: ${s.slowFrames} · ikony ${s.visibleItems} (${s.animatedItems} anim.)`;
		}
	}

	public static sample(): DiagnosticSample {
		this.checkSession();
		const sorted = [...this.intervals].sort((a, b) => a - b);
		const frames = sorted.length;
		const frameMs = frames ? sorted.reduce((a, b) => a + b, 0) / frames : 0;
		let visibleItems = 0, animatedItems = 0;
		for (const counts of this.inventories.values()) {
			visibleItems += counts.visible; animatedItems += counts.animated;
		}
		return {
			fps: frameMs ? 1000 / frameMs : 0, frameMs,
			p95Ms: sorted[Math.max(0, Math.ceil(frames * 0.95) - 1)] || 0,
			maxMs: sorted[frames - 1] || 0,
			slowFrames: sorted.filter(ms => ms > 50).length,
			drawMs: frames ? this.drawTimes.reduce((a, b) => a + b, 0) / frames : 0,
			visibleItems, animatedItems, frames
		};
	}

	public static report(build: { version?: string; build?: string } = {}): string {
		if (!this.get("enabled")) { return "Diagnostyka jest wyłączona."; }
		const s = this.sample();
		return [
			`Webklient ${build.version || "?"}, build ${build.build || "?"}`,
			`Animacje ikon: ${this.get("freezeIcons") ? "wyłączone" : "włączone"}; dekoracje slotów: ${this.get("simpleSlots") ? "wyłączone" : "włączone"}; obwódki rzadkości: ${this.get("hideRarity") ? "wyłączone" : "włączone"}`,
			`Próbka: ${s.frames} odstępów między klatkami (maks. 300)`,
			`FPS renderera: ${s.fps.toFixed(1)}; średnia klatka: ${s.frameMs.toFixed(1)} ms; p95: ${s.p95Ms.toFixed(1)} ms; maks.: ${s.maxMs.toFixed(1)} ms; klatki >50 ms: ${s.slowFrames}`,
			`Średni czas kodu rysowania: ${s.drawMs.toFixed(1)} ms (bez asynchronicznego malowania GPU)`,
			`Widoczne przedmioty: ${s.visibleItems}; animowane: ${s.animatedItems}`,
			`Środowisko: ${navigator.userAgent}`
		].join("\n");
	}

	/** Retain a small report when the settings window is closed between experiments. */
	public static saveReport(label: string, build: { version?: string; build?: string } = {}) {
		if (!this.get("enabled")) { return; }
		this.reportsOwner = this.playerIdentity();
		this.reports.push((label.trim() ? `Próba: ${label.trim().slice(0, 80)}\n` : "") + this.report(build));
		if (this.reports.length > 20) { this.reports.shift(); }
	}

	public static getReports(): string {
		this.checkSession();
		return this.reports.join("\n\n---\n");
	}
}
