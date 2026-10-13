/* Copyright (C) 2026 - PolanieOnLine; AGPL version 3 or later. */

/** Uses the game's bundled dictionaries, without any translation service. */
export class GameLanguage {
	private static language = "pl";
	private static dictionaries: Record<string, Map<string, string>> = {pl: new Map(), en: new Map()};
	private static loading?: Promise<void>;
	private static originals = new WeakMap<Node, {original: string, rendered: string}>();
	private static attributes = new WeakMap<Element, Map<string, {original: string, rendered: string}>>();

	public static normalize(language: unknown): string {
		return typeof language === "string" && language.trim().toLowerCase() === "en" ? "en" : "pl";
	}

	public static getLanguage(): string { return this.language; }

	public static getText(text: string): string {
		return this.dictionaries[this.language].get(text) || text;
	}

	public static parse(content: string): Map<string, string> {
		const entries = new Map<string, string>();
		for (const raw of content.replace(/^\uFEFF/, "").split(/\r?\n/)) {
			if (!raw.trim() || raw.trim().startsWith("#")) continue;
			const separator = raw.indexOf("=");
			if (separator < 1) continue;
			const value = raw.substring(separator + 1);
			if (value.trim()) entries.set(raw.substring(0, separator), value);
		}
		return entries;
	}

	public static load(dataPath: string): Promise<void> {
		if (!this.loading) {
			this.loading = Promise.all(["pl", "en"].map(async language => {
				try {
					const response = await fetch(dataPath + "/languages/" + language + ".txt");
					if (!response.ok) throw new Error("HTTP " + response.status);
					this.dictionaries[language] = this.parse(await response.text());
				} catch (error) { console.warn("Cannot load game language " + language, error); }
			})).then(() => { this.refreshSettings(); });
		}
		return this.loading;
	}

	public static synchronize(language: unknown): void {
		const selected = this.normalize(language);
		if (selected === this.language) return;
		this.language = selected;
		this.refreshSettings();
	}

	private static refreshSettings(): void {
		if (typeof document === "undefined") return;
		for (const root of Array.from(document.querySelectorAll<HTMLElement>(".settings-dialog"))) this.translateSettings(root);
	}

	public static translateSettings(root: HTMLElement): void {
		const caption = root.closest(".windowcontent")?.parentElement?.querySelector(".windowtitle");
		if (caption) caption.textContent = this.getText("Ustawienia");
		// Only this dialog's display text is translated. Chat and input values are excluded.
		const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT);
		let node: Node | null;
		while ((node = walker.nextNode())) {
			const parent = node.parentElement;
			if (!parent || parent.closest("textarea, input, script, style")) continue;
			const current = node.textContent || "";
			let saved = this.originals.get(node);
			if (!saved || saved.rendered !== current) saved = {original: current, rendered: current};
			saved.rendered = saved.original.replace(/\S[\s\S]*\S|\S/, value => this.getText(value.trim().replace(/\s+/g, " ")));
			node.textContent = saved.rendered;
			this.originals.set(node, saved);
		}
		for (const element of Array.from(root.querySelectorAll("[title], [aria-label], [placeholder]"))) {
			let saved = this.attributes.get(element);
			if (!saved) { saved = new Map(); this.attributes.set(element, saved); }
			for (const attribute of ["title", "aria-label", "placeholder"]) {
				const current = element.getAttribute(attribute);
				if (current === null) continue;
				let text = saved.get(attribute);
				if (!text || text.rendered !== current) text = {original: current, rendered: current};
				text.rendered = this.getText(text.original);
				element.setAttribute(attribute, text.rendered);
				saved.set(attribute, text);
			}
		}
		const selector = root.querySelector<HTMLSelectElement>("#selectlanguage");
		if (selector) selector.value = this.language;
	}
}
