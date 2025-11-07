export interface ToggleSwitchOptions {
	id?: string;
	checked: boolean;
	label: string;
	onChange: (next: boolean) => void;
	disabled?: boolean;
	testId?: string;
}

export class ToggleSwitch {
	public readonly el: HTMLButtonElement;

	private readonly trackEl: HTMLSpanElement;
	private readonly thumbEl: HTMLSpanElement;
	private _checked: boolean;

	constructor(private readonly options: ToggleSwitchOptions) {
		this._checked = !!options.checked;
		this.el = document.createElement("button");
		this.el.type = "button";
		this.el.className = this._checked ? "toggle" : "toggle toggle--off";
		this.el.setAttribute("role", "switch");
		this.el.setAttribute("aria-checked", String(this._checked));
		this.el.setAttribute("aria-label", options.label);
		this.el.tabIndex = 0;
		this.el.title = "";
		this.el.addEventListener("click", () => this.handleToggle());
		this.el.addEventListener("keydown", (event) => this.handleKeyDown(event));

		if (options.id) {
			this.el.id = options.id;
		}
		if (options.testId) {
			this.el.dataset.testid = options.testId;
		}

		this.trackEl = document.createElement("span");
		this.trackEl.className = "toggle__track";

		this.thumbEl = document.createElement("span");
		this.thumbEl.className = "toggle__thumb";

		this.el.append(this.trackEl, this.thumbEl);

		if (options.disabled) {
			this.setDisabled(true);
		}
	}

	public mount(container: HTMLElement): void {
		container.textContent = "";
		container.appendChild(this.el);
	}

	public destroy(): void {
		this.el.remove();
	}

	public getChecked(): boolean {
		return this._checked;
	}

	public setChecked(next: boolean): void {
		this._checked = next;
		this.el.classList.toggle("toggle--off", !next);
		this.el.setAttribute("aria-checked", String(next));
	}

	public setDisabled(disabled: boolean): void {
		this.el.disabled = disabled;
		this.el.setAttribute("aria-disabled", disabled ? "true" : "false");
	}

	private handleToggle(): void {
		if (this.el.disabled) {
			return;
		}
		const next = !this._checked;
		this.setChecked(next);
		this.emitChange(next);
	}

	private handleKeyDown(event: KeyboardEvent): void {
		if (this.el.disabled) {
			return;
		}
		switch (event.key) {
		case " ":
		case "Spacebar":
		case "Enter":
			event.preventDefault();
			this.handleToggle();
			break;
		case "ArrowLeft":
			event.preventDefault();
			if (this._checked) {
				this.setChecked(false);
				this.emitChange(false);
			}
			break;
		case "ArrowRight":
			event.preventDefault();
			if (!this._checked) {
				this.setChecked(true);
				this.emitChange(true);
			}
			break;
		default:
			break;
		}
	}

	private emitChange(next: boolean): void {
		this.options.onChange(next);
		this.el.dispatchEvent(new Event("change", {bubbles: true}));
	}
}
