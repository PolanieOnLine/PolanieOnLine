import { marauroa } from "marauroa";
import { Paths } from "../../data/Paths";
import { singletons } from "../../SingletonRepo";
import { GameLanguage } from "../../util/GameLanguage";
import { getItemSlotLayers } from "../component/ItemSlotPresentation";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";
import { FloatingWindow } from "../toolkit/FloatingWindow";

type CalendarData = {[key: string]: any};
const text = (value: string): string => GameLanguage.getText(value);
// Zero-based columns/rows; spans remain the server's original door sizes.
const landscapePositions: Record<number, [number, number]> = {
	1: [7, 0], 2: [5, 1], 3: [2, 0], 4: [2, 1], 5: [0, 1], 6: [0, 2],
	7: [5, 0], 8: [2, 2], 9: [4, 0], 10: [6, 1], 11: [5, 2], 12: [0, 0],
	13: [3, 1], 14: [3, 2], 15: [8, 0], 16: [1, 2], 17: [4, 2], 18: [1, 1],
	19: [3, 0], 20: [6, 2], 21: [6, 0], 22: [9, 0], 23: [7, 1], 24: [8, 1]
};

/** Opened locally; asynchronous replies can only update the matching live window. */
export class AdventCalendarDialog extends DialogContentComponent {
	private static active?: AdventCalendarDialog;
	private static openingSequence = 0;
	private readonly owner = marauroa.me;
	private readonly windowId = `${Date.now()}:${++AdventCalendarDialog.openingSequence}`;
	private readonly board = document.createElement("div");
	private readonly date = document.createElement("span");
	private readonly status = document.createElement("div");
	private readonly doors = new Map<number, HTMLButtonElement>();
	private readonly previews = new Map<number, HTMLElement>();
	private readonly sprites = new Map<HTMLElement, string>();
	private readonly timer: ReturnType<typeof setInterval>;
	private data?: CalendarData;
	private sequence = 0;
	private requestId = "";
	private processedRequestId = "";
	private requestCommand = "";
	private requestDay = 0;
	private sentAt = 0;
	private waiting = false;

	private constructor() {
		super("empty-div-template");
		this.componentElement.classList.add("advent-calendar-dialog");
		const heading = document.createElement("div");
		heading.className = "advent-calendar-heading";
		const title = document.createElement("strong");
		title.textContent = text("Świąteczne podarunki");
		heading.append(title, this.date);
		this.board.className = "advent-calendar-board";
		this.board.setAttribute("aria-label", text("Kalendarz adwentowy"));
		this.status.className = "advent-calendar-status";
		this.status.setAttribute("aria-live", "polite");
		this.status.textContent = text("Wczytywanie kalendarza…");
		this.componentElement.append(heading, this.board, this.status);
		this.addCloseButton();
		this.timer = setInterval(() => {
			if (marauroa.me !== this.owner) {
				this.close();
				return;
			}
			const elapsed = Date.now() - this.sentAt;
			if (this.waiting && elapsed > 8000) {
				this.waiting = false;
				this.status.textContent = text("Nie udało się odświeżyć kalendarza. Spróbuj ponownie.");
				this.updateDoors();
			} else if (!this.waiting && elapsed > 30000) {
				this.send("refresh");
			}
			for (const [icon, filename] of this.sprites) {
				const sheet = singletons.getSpriteStore().get(filename);
				const frames = Math.max(1, Math.floor(sheet.width / 32) || 1);
				icon.style.backgroundPosition = `${-(Math.floor(Date.now() / 100) % frames) * 32}px 0px`;
			}
		}, 100);
	}

	public static open(): void {
		if (!marauroa.me) return;
		if (this.active?.owner !== marauroa.me) this.active?.close();
		if (!this.active) {
			const dialog = new AdventCalendarDialog();
			const frame = new FloatingWindow(text("Kalendarz adwentowy"), dialog, 24, 24);
			frame.setResponsiveWidth(true);
			frame.setId("advent-calendar");
			dialog.setFrame(frame);
			this.active = dialog;
		}
		this.active.send("open");
	}

	public static show(data: CalendarData): void {
		const dialog = this.active;
		if (!dialog || dialog.owner !== marauroa.me || data.request_id !== dialog.requestId
				|| data.request_id === dialog.processedRequestId) return;
		dialog.processedRequestId = dialog.requestId;
		dialog.waiting = false;
		if (data.status === "inactive" || (dialog.data && data.year !== dialog.data.year)) dialog.clearPreviews();
		dialog.data = data;
		const date = new Date(`${data.date}T12:00:00`);
		dialog.date.textContent = Number.isFinite(date.getTime())
			? new Intl.DateTimeFormat(GameLanguage.getLanguage(), {day: "numeric", month: "long", year: "numeric"}).format(date)
			: String(data.date || "");
		if (!dialog.doors.size) dialog.buildDoors(dialog.list(data.layout));
		dialog.status.textContent = text(data.message || "Otwórz dzisiejsze okienko lub odbierz zaległy prezent.");
		dialog.updateDoors();
		const revealedDay = Number(data.revealed_day);
		if ((data.status === "received" || data.status === "claimed") && revealedDay === dialog.requestDay
				&& (dialog.requestCommand === "claim" || dialog.requestCommand === "view")
				&& dialog.list(data.states)[revealedDay - 1] === "claimed" && data.reward_names) {
			dialog.showRewards(revealedDay, data);
		}
	}

	public override onParentClose(): void {
		clearInterval(this.timer);
		this.clearPreviews();
		if (AdventCalendarDialog.active === this) AdventCalendarDialog.active = undefined;
	}

	private buildDoors(layout: string[]): void {
		for (const value of layout) {
			const [day, column, row, width, height] = value.split(",").map(Number);
			if (![day, column, row, width, height].every(Number.isFinite) || day < 1 || day > 24) continue;
			const door = document.createElement("button");
			door.type = "button";
			door.className = "advent-calendar-door";
			door.style.setProperty("--advent-column", `${column + 1} / span ${width}`);
			door.style.setProperty("--advent-row", `${row + 1} / span ${height}`);
			const [landscapeColumn, landscapeRow] = landscapePositions[day];
			door.style.setProperty("--advent-landscape-column", `${landscapeColumn + 1} / span ${width}`);
			door.style.setProperty("--advent-landscape-row", `${landscapeRow + 1} / span ${height}`);
			door.dataset.day = String(day);
			door.classList.toggle("tone-green", day % 3 === 0);
			const preview = document.createElement("span");
			preview.className = "advent-calendar-rewards";
			this.previews.set(day, preview);
			const leftLeaf = document.createElement("span");
			leftLeaf.className = "advent-calendar-leaf advent-calendar-leaf-left";
			leftLeaf.setAttribute("aria-hidden", "true");
			const rightLeaf = document.createElement("span");
			rightLeaf.className = "advent-calendar-leaf advent-calendar-leaf-right";
			rightLeaf.setAttribute("aria-hidden", "true");
			const number = document.createElement("span");
			number.className = "advent-calendar-number";
			number.textContent = String(day);
			const received = document.createElement("span");
			received.className = "advent-calendar-received";
			received.textContent = "✓";
			received.setAttribute("aria-hidden", "true");
			door.append(preview, leftLeaf, rightLeaf, number, received);
			door.addEventListener("click", () => {
				const state = this.list(this.data?.states)[day - 1];
				if (this.data?.status !== "inactive" && (state === "available" || state === "claimed")) {
					this.send(state === "claimed" ? "view" : "claim", day);
				}
			});
			door.addEventListener("keydown", event => {
				if ((event.key === "ArrowDown" || event.key === "ArrowUp") && preview.scrollHeight > preview.clientHeight) {
					event.preventDefault();
					preview.scrollBy({top: event.key === "ArrowDown" ? 28 : -28});
				}
			});
			this.doors.set(day, door);
			this.board.appendChild(door);
		}
	}

	private updateDoors(): void {
		const states = this.list(this.data?.states);
		for (const [day, door] of this.doors) {
			const state = this.data?.status === "inactive" ? "inactive" : states[day - 1] || "future";
			if (state !== "claimed") this.clearPreview(day);
			door.classList.toggle("is-today", day === Number(this.data?.today));
			door.classList.toggle("is-claimed", state === "claimed");
			door.classList.toggle("is-open", state === "claimed");
			door.classList.toggle("is-expired", state === "expired");
			door.disabled = this.waiting || (state !== "available" && state !== "claimed");
			const label = state === "claimed" ? "Prezent odebrany" : state === "available" ? "Odbierz prezent"
				: state === "expired" ? "Termin odbioru minął" : "Prezent jeszcze niedostępny";
			const rewards = this.previews.get(day)?.getAttribute("aria-label");
			door.setAttribute("aria-label", `${day} ${text("grudnia")}: ${text(label)}${rewards ? `. ${rewards}` : ""}`);
		}
	}

	private send(command: string, day = 0): void {
		if (this.waiting) return;
		this.requestId = `${this.windowId}:${++this.sequence}`;
		this.requestCommand = command;
		this.requestDay = day;
		this.waiting = true;
		this.sentAt = Date.now();
		this.updateDoors();
		marauroa.clientFramework.sendAction({type: "advent_calendar_action", command, day: String(day), request_id: this.requestId});
	}

	private showRewards(day: number, data: CalendarData): void {
		const preview = this.previews.get(day);
		if (!preview) return;
		this.clearPreview(day);
		const names = this.list(data.reward_names);
		const classes = this.list(data.reward_classes);
		const subclasses = this.list(data.reward_subclasses);
		const quantities = this.list(data.reward_quantities);
		const labels: string[] = [];
		names.forEach((name, index) => {
			if (!classes[index] || !subclasses[index]) return;
			const reward = document.createElement("span");
			reward.className = "advent-calendar-reward";
			const slot = document.createElement("div");
			slot.className = "itemSlot";
			const layers = getItemSlotLayers(slot);
			const filename = `${Paths.sprites}/items/${classes[index]}/${subclasses[index]}.png`;
			layers.icon.style.backgroundImage = `url(${singletons.getSpriteStore().checkPath(filename)})`;
			const quantity = Number(quantities[index]);
			layers.quantityLabel.textContent = quantity > 9999999 ? `${Math.floor(quantity / 1000000)}M`
				: quantity > 9999 ? `${Math.floor(quantity / 1000)}K` : quantity > 1 ? quantities[index] : "";
			this.sprites.set(layers.icon, filename);
			const label = `${text(name === "money" ? "monety" : name)} × ${quantities[index] || "1"}`;
			reward.title = label;
			reward.setAttribute("aria-label", label);
			labels.push(label);
			reward.appendChild(slot);
			preview.appendChild(reward);
		});
		preview.setAttribute("aria-label", labels.join(", "));
		this.doors.get(day)!.title = labels.join("\n");
		this.doors.get(day)?.classList.toggle("has-preview", labels.length > 0);
		this.updateDoors();
	}

	private clearPreview(day: number): void {
		const preview = this.previews.get(day);
		if (!preview) return;
		for (const icon of this.sprites.keys()) {
			if (preview.contains(icon)) this.sprites.delete(icon);
		}
		preview.replaceChildren();
		preview.removeAttribute("aria-label");
		this.doors.get(day)?.classList.toggle("has-preview", false);
		this.doors.get(day)?.removeAttribute("title");
	}

	private clearPreviews(): void {
		for (const day of this.previews.keys()) this.clearPreview(day);
		this.sprites.clear();
	}

	private list(value: unknown): string[] {
		return Array.isArray(value) ? value.map(String)
			: typeof value === "string" ? value.substring(1, value.length - 1).split(/\t/).filter(Boolean) : [];
	}
}
