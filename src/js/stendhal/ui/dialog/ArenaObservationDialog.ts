import { marauroa } from "marauroa";
import { ui } from "../UI";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";
import { stendhal } from "../../stendhal";

/** A private server-controlled chooser, separate from NPC chat shortcuts. */
export class ArenaObservationDialog extends DialogContentComponent {
	private static active?: ArenaObservationDialog;
	private static watching = false;

	public static isWatching(): boolean { return this.watching; }
	private readonly status = document.createElement("p");
	private readonly players = document.createElement("select");
	private readonly observe: HTMLButtonElement;
	private readonly leave: HTMLButtonElement;
	private serverClosing = false;

	private constructor() {
		super("empty-div-template");
		this.componentElement.style.minWidth = "260px";
		this.componentElement.appendChild(this.status);
		this.players.setAttribute("aria-label", "Gracz do obserwacji");
		this.players.style.width = "100%";
		this.componentElement.appendChild(this.players);
		this.observe = this.addButton("Obserwuj", () => {
			if (this.players.value) { ArenaObservationDialog.send("watch", this.players.value); }
		});
		this.addButton("Odśwież", () => ArenaObservationDialog.send("list"));
		this.leave = this.addButton("Powrót do lobby", () => ArenaObservationDialog.send("return"));
	}

	private static send(operation: string, target?: string): void {
		const action: {[key: string]: string} = {type: "arena_observation", operation};
		if (target !== undefined) { action.target = target; }
		marauroa.clientFramework.sendAction(action as any);
	}

	public static show(event: {[key: string]: any}): void {
		this.watching = event.arena_phase === "watch";
		const names = String(event.options || "").split("\t")
			.map(entry => entry.split("|~|"))
			.filter(parts => parts.length >= 2 && parts[1] !== "")
			.map(parts => parts[1]);
		if (event.arena_phase === "close" || (event.arena_phase === "open" && names.length === 0)) {
			// Run after any earlier view-change event's deferred camera update.
			queueMicrotask(() => { stendhal.ui.viewport.freeze = false; });
			if (this.active) {
				this.active.serverClosing = true;
				this.active.close();
			}
			return;
		}
		if (!this.active) {
			this.active = new ArenaObservationDialog();
			this.active.setFrame(ui.createSingletonFloatingWindow("Obserwacja areny", this.active, 24, 24));
		}
		const dialog = this.active;
		const selected = dialog.players.value;
		dialog.players.replaceChildren();
		for (const name of names) {
			const option = document.createElement("option");
			option.value = name;
			option.setAttribute("data-no-translate", "");
			option.textContent = name;
			dialog.players.appendChild(option);
		}
		if (Array.from(dialog.players.options).some(option => option.value === selected)) {
			dialog.players.value = selected;
		}
		const empty = dialog.players.options.length === 0;
		dialog.status.textContent = empty ? "Nikt teraz nie walczy na arenie." : String(event.title || "");
		dialog.observe.disabled = empty;
		dialog.leave.hidden = event.arena_phase !== "watch";
	}

	public override onParentClose(): void {
		if (!this.serverClosing) { ArenaObservationDialog.send("close"); }
		ArenaObservationDialog.active = undefined;
	}
}
