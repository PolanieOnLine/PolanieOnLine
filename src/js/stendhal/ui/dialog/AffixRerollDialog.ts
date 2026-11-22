import { marauroa } from "marauroa";
import { ItemRarity } from "../../data/ItemRarity";
import { Paths } from "../../data/Paths";
import { singletons } from "../../SingletonRepo";
import { GameLanguage } from "../../util/GameLanguage";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";
import { FloatingWindow } from "../toolkit/FloatingWindow";

type RerollData = {[key: string]: any};
type Command = "select" | "roll" | "accept" | "keep" | "refresh" | "close";
const text = (value: string): string => GameLanguage.getText(value);

/** A server-owned quote and paid offer; closing never resolves an offer. */
export class AffixRerollDialog extends DialogContentComponent {
	private static active?: AffixRerollDialog;
	private static readonly seenWindows = new WeakMap<object, Set<string>>();
	private readonly owner = marauroa.me;
	private readonly zone = marauroa.currentZoneName;
	private readonly items = document.createElement("div");
	private readonly affixes = document.createElement("div");
	private readonly identity = document.createElement("div");
	private readonly comparison = document.createElement("div");
	private readonly currentLabel = document.createElement("p");
	private readonly newLabel = document.createElement("p");
	private readonly costCount = document.createElement("span");
	private readonly status = document.createElement("div");
	private readonly confirmation = document.createElement("div");
	private readonly warning = document.createElement("p");
	private readonly roll: HTMLButtonElement;
	private readonly confirmRoll: HTMLButtonElement;
	private readonly cancelRoll: HTMLButtonElement;
	private readonly keep: HTMLButtonElement;
	private readonly accept: HTMLButtonElement;
	private readonly refreshButton: HTMLButtonElement;
	private readonly sprites = new Map<HTMLElement, string>();
	private readonly timer: ReturnType<typeof setInterval>;
	private data: RerollData = {};
	private sequence = 0;
	private requestId = "";
	private processedRequest = "";
	private sentAt = 0;
	private waiting = false;
	private recoveryRequired = false;
	private confirming = false;
	private closed = false;

	private constructor(private readonly windowId: string, private readonly npcId: string) {
		super("empty-div-template");
		this.componentElement.classList.add("affix-reroll-dialog");
		const intro = document.createElement("p");
		intro.className = "affix-reroll-intro";
		intro.textContent = text("Wybierz przedmiot i jedną cechę. Pozostałe cechy przedmiotu nie zmienią się.");
		const layout = document.createElement("div");
		layout.className = "affix-reroll-layout";
		this.items.className = "affix-reroll-items";
		this.items.setAttribute("aria-label", text("Przedmioty do przemiany"));
		const catalogue = this.section("1. Przedmiot", this.items);
		const detail = document.createElement("div");
		detail.className = "affix-reroll-detail";
		this.identity.className = "affix-reroll-identity";
		this.affixes.className = "affix-reroll-affixes";
		this.affixes.setAttribute("aria-label", text("Cecha do przemiany"));
		detail.append(this.identity, this.section("2. Cecha do przemiany", this.affixes));
		this.comparison.className = "affix-reroll-comparison";
		this.comparison.append(this.section("Obecna cecha", this.currentLabel), this.section("Nowa cecha", this.newLabel));
		detail.appendChild(this.comparison);
		layout.append(catalogue, detail);
		const cost = document.createElement("div");
		cost.className = "affix-reroll-cost";
		cost.append(this.sprite("resource", "transformation_stone"), this.costCount);
		this.confirmation.className = "affix-reroll-confirmation";
		const warning = this.warning;
		const confirmationButtons = document.createElement("div");
		confirmationButtons.className = "affix-reroll-actions";
		this.confirmRoll = this.button("Zużyj kamienie i losuj", () => this.send("roll"));
		this.cancelRoll = this.button("Wróć", () => { this.confirming = false; this.updateControls(); });
		confirmationButtons.append(this.cancelRoll, this.confirmRoll);
		this.confirmation.append(warning, confirmationButtons);
		this.status.className = "affix-reroll-status";
		this.status.setAttribute("role", "status");
		this.status.setAttribute("aria-live", "polite");
		const actions = document.createElement("div");
		actions.className = "affix-reroll-actions";
		this.refreshButton = this.button("Odśwież", () => this.send("refresh"));
		this.roll = this.button("Losuj nową cechę", () => {
			if (!this.canRoll()) return;
			this.confirming = true;
			this.updateControls();
			this.confirmRoll.focus();
		});
		this.keep = this.button("Zachowaj obecny", () => this.send("keep"));
		this.accept = this.button("Wybierz nowy", () => this.send("accept"));
		this.accept.classList.add("affix-reroll-primary");
		this.confirmRoll.classList.add("affix-reroll-primary");
		actions.append(this.refreshButton, this.roll, this.keep, this.accept, this.button("Zamknij", () => this.close()));
		this.componentElement.append(intro, layout, cost, this.confirmation, this.status, actions);
		this.timer = setInterval(() => this.tick(), 100);
	}

	public static show(data: RerollData): void {
		if (!marauroa.me || !data.window_id || !data.npc_id) return;
		if (this.active?.owner !== marauroa.me) this.active?.close();
		if (data.phase === "open") {
			let seen = this.seenWindows.get(marauroa.me);
			if (!seen) { seen = new Set(); this.seenWindows.set(marauroa.me, seen); }
			const windowId = String(data.window_id);
			if (seen.has(windowId)) return;
			seen.add(windowId);
			this.active?.close();
			const dialog = new AffixRerollDialog(windowId, String(data.npc_id));
			const frame = new FloatingWindow(text("Przemiana afiksów u Przemira"), dialog, 24, 24);
			frame.setResponsiveWidth(true);
			frame.setId("affix-reroll");
			dialog.setFrame(frame);
			this.active = dialog;
			dialog.apply(data);
			return;
		}
		const dialog = this.active;
		if (!dialog || dialog.owner !== marauroa.me || dialog.zone !== marauroa.currentZoneName || String(data.window_id) !== dialog.windowId
				|| String(data.npc_id) !== dialog.npcId || !dialog.requestId
				|| String(data.request_id) !== dialog.requestId || dialog.processedRequest === dialog.requestId) return;
		if (!["select", "offer", "result"].includes(data.phase)) return;
		dialog.processedRequest = dialog.requestId;
		dialog.apply(data);
	}

	public override onParentClose(): void {
		if (this.closed) return;
		this.closed = true;
		clearInterval(this.timer);
		if (this.owner === marauroa.me) {
			marauroa.clientFramework.sendAction({type: "affix_reroll_action", command: "close", npc_id: this.npcId,
				window_id: this.windowId, request_id: String(++this.sequence)});
		}
		this.sprites.clear();
		this.data = {};
		this.componentElement.replaceChildren();
		if (AffixRerollDialog.active === this) AffixRerollDialog.active = undefined;
	}

	private apply(data: RerollData): void {
		this.data = data;
		this.waiting = false;
		this.recoveryRequired = false;
		this.confirming = false;
		this.status.textContent = String(data.message || text(this.hasOffer()
			? "Wybierz cechę, którą chcesz zachować."
			: "Wybierz cechę, którą chcesz wylosować ponownie."));
		this.draw();
	}

	private draw(): void {
		// Remove detached item icons while keeping the permanent cost icon animated.
		for (const icon of this.sprites.keys()) if (this.items.contains(icon) || this.identity.contains(icon)) this.sprites.delete(icon);
		this.items.replaceChildren();
		this.identity.replaceChildren();
		this.affixes.replaceChildren();
		const paths = this.list(this.data.candidate_paths);
		const names = this.list(this.data.candidate_names);
		const classes = this.list(this.data.candidate_classes);
		const subclasses = this.list(this.data.candidate_subclasses);
		const rarities = this.list(this.data.candidate_rarities);
		const levels = this.list(this.data.candidate_levels);
		paths.forEach((path, index) => {
			const button = this.button("", () => this.send("select", path, ""));
			button.className = "affix-reroll-item";
			button.setAttribute("aria-pressed", String(path === this.data.selected_path));
			const name = document.createElement("span");
			name.className = "affix-reroll-item-name";
			name.textContent = `${names[index] || text("Przedmiot")}${Number(levels[index]) > 0 ? ` +${Number(levels[index])}` : ""}`;
			const metadata = document.createElement("small");
			const rarity = ItemRarity.fromId(rarities[index]);
			metadata.textContent = `${rarity ? text(rarity.polishDisplayName) : rarities[index] || ""} · #${index + 1}`;
			if (rarity) name.style.color = rarity.colorHex;
			const label = document.createElement("span");
			label.append(name, metadata);
			button.append(this.sprite(classes[index], subclasses[index]), label);
			this.items.appendChild(button);
		});
		if (!paths.length) this.items.textContent = text("Brak przedmiotów z cechami do przemiany.");
		if (this.data.selected_path) {
			const name = document.createElement("strong");
			name.textContent = String(this.data.item_name || text("Przedmiot"));
			this.identity.append(this.sprite(this.data.item_class, this.data.item_subclass), name);
		}
		const ids = this.list(this.data.affix_ids);
		const labels = this.list(this.data.affix_labels);
		ids.forEach((id, index) => {
			const button = this.button(labels[index] || id, () => this.send("select", this.data.selected_path, id));
			button.className = "affix-reroll-affix";
			if (id === this.data.rerolled_affix) this.markLabel(button, labels[index] || id);
			button.setAttribute("aria-pressed", String(id === this.data.selected_affix));
			this.affixes.appendChild(button);
		});
		if (!ids.length) this.affixes.textContent = text(this.data.selected_path ? "Brak cech do przemiany." : "Najpierw wybierz przedmiot.");
		this.currentLabel.textContent = String(this.data.current_label || "");
		if (this.data.rerolled_affix === this.data.selected_affix && this.data.rerolled_affix) this.markLabel(this.currentLabel, String(this.data.current_label || ""));
		this.newLabel.textContent = String(this.data.new_label || "");
		const price = Number(this.data.stone_cost) === 3 ? 3 : 1;
		this.warning.textContent = text(price === 3
				? "Losowanie zużyje 3 kamienie przemiany, nawet jeśli zachowasz obecną cechę."
				: "Losowanie zużyje 1 kamień przemiany, nawet jeśli zachowasz obecną cechę.");
		this.costCount.textContent = `${text("Koszt losowania")}: ${Number(this.data.stone_cost) ? price : "1 / 3"} × ${text("kamień przemiany")} · ${text("Posiadasz")}: ${Math.max(0, Number(this.data.stone_count) || 0)}`;
		this.updateControls();
	}

	private markLabel(target: HTMLElement, label: string): void {
		target.textContent = "";
		const icon = document.createElement("span");
		icon.className = "affix-rerolled-icon";
		icon.title = text("Afiks wybrany do przemiany");
		const name = document.createElement("span"); name.textContent = label;
		target.append(icon, name);
	}

	private hasOffer(): boolean { return !!this.data.offer_token && !!this.data.selected_path; }

	private canRoll(): boolean {
		return !this.closed && !this.waiting && !this.recoveryRequired && !this.hasOffer()
			&& String(this.data.can_roll) === "1" && !!this.data.request_token && !!this.data.selected_path && !!this.data.selected_affix;
	}

	private updateControls(): void {
		const blocked = this.closed || this.waiting || this.recoveryRequired;
		this.componentElement.setAttribute("aria-busy", String(this.waiting));
		for (const button of Array.from(this.items.children) as HTMLButtonElement[]) button.disabled = blocked || this.hasOffer() || this.confirming;
		for (const button of Array.from(this.affixes.children) as HTMLButtonElement[]) button.disabled = blocked || this.hasOffer() || this.confirming;
		this.refreshButton.disabled = this.closed || this.waiting;
		this.roll.hidden = this.hasOffer() || this.confirming;
		this.roll.disabled = !this.canRoll();
		this.confirmation.hidden = !this.confirming;
		this.confirmRoll.disabled = !this.canRoll();
		this.cancelRoll.disabled = this.waiting;
		this.comparison.hidden = !this.hasOffer();
		this.keep.hidden = this.accept.hidden = !this.hasOffer();
		this.keep.disabled = blocked || !this.hasOffer();
		this.accept.disabled = blocked || !this.hasOffer() || String(this.data.can_accept ?? "1") !== "1";
	}

	private send(command: Command, path = String(this.data.selected_path || ""), affix = String(this.data.selected_affix || "")): void {
		if (this.closed || this.owner !== marauroa.me || AffixRerollDialog.active !== this || this.waiting) return;
		if (this.zone !== marauroa.currentZoneName) { this.close(); return; }
		if (this.recoveryRequired && command !== "refresh") return;
		if (command === "roll" && (!this.confirming || !this.canRoll())) return;
		if ((command === "accept" || command === "keep") && !this.hasOffer()) return;
		if (command === "accept" && String(this.data.can_accept ?? "1") !== "1") return;
		if (command === "select" && (this.hasOffer() || this.confirming || !this.list(this.data.candidate_paths).includes(path))) return;
		if (command === "select" && path !== this.data.selected_path) affix = "";
		if (command === "select" && affix && !this.list(this.data.affix_ids).includes(affix)) return;
		const action: {[key: string]: any} = {type: "affix_reroll_action", command, npc_id: this.npcId,
			window_id: this.windowId, request_id: String(++this.sequence)};
		if (path) action.target_path = path.split("/");
		if ((command === "select" || command === "roll") && affix) action.affix_id = affix;
		if (command === "roll") action.request_token = this.data.request_token;
		if (command === "accept" || command === "keep") action.offer_token = this.data.offer_token;
		this.requestId = action.request_id;
		this.waiting = true;
		this.sentAt = Date.now();
		this.status.textContent = text("Oczekiwanie na odpowiedź…");
		this.updateControls();
		marauroa.clientFramework.sendAction(action);
	}

	private tick(): void {
		if (this.owner !== marauroa.me || this.zone !== marauroa.currentZoneName) { this.close(); return; }
		if (this.waiting && Date.now() - this.sentAt > 10000) {
			this.waiting = false;
			this.recoveryRequired = true;
			this.confirming = false;
			this.status.textContent = text("Brak odpowiedzi. Odśwież, aby sprawdzić wynik. Nie ponawiaj losowania.");
			this.updateControls();
		}
		for (const [icon, filename] of this.sprites) {
			const sheet = singletons.getSpriteStore().get(filename);
			const frames = Math.max(1, Math.floor(sheet.width / 32) || 1);
			icon.style.backgroundPosition = `${-(Math.floor(Date.now() / 100) % frames) * 32}px 0px`;
		}
	}

	private sprite(itemClass: unknown, subclass: unknown): HTMLElement {
		const icon = document.createElement("span");
		icon.className = "affix-reroll-sprite";
		icon.setAttribute("aria-hidden", "true");
		// Sprite identifiers never become arbitrary CSS URLs or filesystem paths.
		if (/^[a-zA-Z0-9_-]+$/.test(String(itemClass)) && /^[a-zA-Z0-9_-]+$/.test(String(subclass))) {
			const filename = `${Paths.sprites}/items/${itemClass}/${subclass}.png`;
			icon.style.backgroundImage = `url(${singletons.getSpriteStore().checkPath(filename)})`;
			this.sprites.set(icon, filename);
		}
		return icon;
	}

	private section(title: string, body: HTMLElement): HTMLElement {
		const section = document.createElement("fieldset");
		const legend = document.createElement("legend");
		legend.textContent = text(title);
		section.append(legend, body);
		return section;
	}

	private button(label: string, action: () => void): HTMLButtonElement {
		const button = document.createElement("button");
		button.type = "button";
		button.className = "dialogbutton";
		button.textContent = text(label);
		button.addEventListener("click", action);
		return button;
	}

	private list(value: unknown): string[] {
		if (Array.isArray(value)) return value.map(String);
		if (typeof value !== "string") return [];
		const wrapped = (value.startsWith("[") && value.endsWith("]")) || (value.startsWith("\t") && value.endsWith("\t"));
		const content = wrapped ? value.slice(1, -1) : value;
		return content ? content.split("\t") : [];
	}
}
