import { marauroa } from "marauroa";
import { ui } from "../UI";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";
import { stendhal } from "../../stendhal";

/** A private server-controlled chooser, separate from NPC chat shortcuts. */
export class ArenaObservationDialog extends DialogContentComponent {
	private static active?: ArenaObservationDialog;
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
			if (this.players.value) { ArenaObservationDialog.send(this.players.value); }
		});
		this.addButton("Odśwież", () => ArenaObservationDialog.send("lista"));
		this.leave = this.addButton("Powrót do lobby", () => ArenaObservationDialog.send("powrot"));
	}

	private static send(choice: string): void {
		marauroa.clientFramework.sendAction({type: "chat", text: "obserwuj " + choice} as any);
	}

	public static show(event: {[key: string]: any}): void {
		if (event.arena_phase === "close") {
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
		for (const entry of String(event.options || "").split("\t")) {
			const parts = entry.split("|~|");
			if (parts.length >= 2) {
				const option = document.createElement("option");
				option.value = parts[1];
				option.textContent = parts[1];
				dialog.players.appendChild(option);
			}
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
		if (!this.serverClosing) { ArenaObservationDialog.send("zamknij"); }
		ArenaObservationDialog.active = undefined;
	}
}
