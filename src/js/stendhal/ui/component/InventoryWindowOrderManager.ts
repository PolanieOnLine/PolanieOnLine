declare const stendhal: any;

interface ManagedWindow {
	id: string;
	element: HTMLElement;
	titleBar: HTMLElement;
	titleElement?: HTMLElement;
	toggleButton?: HTMLButtonElement;
	upButton?: HTMLButtonElement;
	downButton?: HTMLButtonElement;
	pointerDownListener: (event: PointerEvent) => void;
	keyDownListener: (event: KeyboardEvent) => void;
}

interface DragSnapshot {
	windowId: string;
	pointerId: number;
	orderWithoutDragged: string[];
	positions: { id: string; mid: number; element: HTMLElement }[];
	dropIndex: number;
}

export class InventoryWindowOrderManager {

	private static instance?: InventoryWindowOrderManager;

	private readonly orderStorageKey = "client.ui.right-panel.order";
	private readonly enabledStorageKey = "client.ui.right-panel.personalize";

	private readonly windows: Map<string, ManagedWindow> = new Map();
	private readonly defaultOrder: string[] = [];
	private order: string[] = [];
	private personalizationEnabled = false;
	private readonly liveRegion: HTMLElement;

	private dropIndicator?: HTMLElement;
	private dragState?: DragSnapshot;
	private pointerMoveRaf = 0;
	private pendingPointerY = 0;

	private readonly onPointerMove = (event: PointerEvent) => this.handlePointerMove(event);
	private readonly onPointerUp = (event: PointerEvent) => this.handlePointerUp(event);

	private constructor(private readonly container: HTMLElement) {
		this.order = this.loadOrder();
		this.personalizationEnabled = this.loadPersonalizationEnabled();
		this.liveRegion = this.createLiveRegion();
	}

	public static get(): InventoryWindowOrderManager {
		if (!this.instance) {
			const container = document.getElementById("rightColumn");
			if (!container) {
				throw new Error("Cannot initialize InventoryWindowOrderManager because #rightColumn was not found.");
			}
			this.instance = new InventoryWindowOrderManager(container);
		}
		return this.instance;
	}

	public registerWindow(id: string, root: HTMLElement) {
		if (this.windows.has(id)) {
			return;
		}

		const titleBar = root.querySelector<HTMLElement>(".inventory-window__titlebar");
		if (!titleBar) {
			return;
		}

		const titleElement = root.querySelector<HTMLElement>("[data-window-title]") || undefined;
		const toggleButton = root.querySelector<HTMLButtonElement>("[data-window-toggle]") || undefined;

		titleBar.tabIndex = 0;
		root.setAttribute("aria-grabbed", "false");

		const pointerDownListener = (event: PointerEvent) => this.onPointerDown(event, id);
		const keyDownListener = (event: KeyboardEvent) => this.onKeyDown(event, id);

		titleBar.addEventListener("pointerdown", pointerDownListener);
		titleBar.addEventListener("keydown", keyDownListener);

		const info: ManagedWindow = {
			id,
			element: root,
			titleBar,
			titleElement,
			toggleButton,
			pointerDownListener,
			keyDownListener
		};
		this.windows.set(id, info);

		if (!this.defaultOrder.includes(id)) {
			this.defaultOrder.push(id);
		}

		this.ensureOrderCompleteness();
		this.applyCurrentOrder();
		this.updatePersonalizationControls(info);
		this.updateArrowsState();
	}

	public isPersonalizationEnabled(): boolean {
		return this.personalizationEnabled;
	}

	public setPersonalizationEnabled(enabled: boolean) {
		if (this.personalizationEnabled === enabled) {
			return;
		}

		this.personalizationEnabled = enabled;
		this.savePersonalizationEnabled(enabled);

		if (!enabled) {
			this.cancelDrag();
		}

		for (const info of this.windows.values()) {
			this.updatePersonalizationControls(info);
		}
		this.updateArrowsState();
	}

	public resetLayout() {
		if (this.defaultOrder.length === 0) {
			return;
		}

		this.order = [...this.defaultOrder];
		this.saveOrder();
		this.applyCurrentOrder();
		this.announce("Przywrócono domyślny układ prawego panelu.");
	}

	public moveWindow(id: string, direction: number) {
		if (!this.personalizationEnabled || direction === 0) {
			return;
		}
		const knownOrder = this.getKnownOrder();
		const index = knownOrder.indexOf(id);
		if (index === -1) {
			return;
		}

		const targetIndex = index + direction;
		if (targetIndex < 0 || targetIndex >= knownOrder.length) {
			return;
		}

		const previousOrder = [...this.order];
		const updatedOrder = [...knownOrder];
		const [moved] = updatedOrder.splice(index, 1);
		updatedOrder.splice(targetIndex, 0, moved);

		this.order = this.mergeWithUnknown(updatedOrder);
		if (!this.ordersEqual(previousOrder, this.order)) {
			this.saveOrder();
			this.applyCurrentOrder();
			this.announceMove(id);
		} else {
			this.applyCurrentOrder();
		}
	}

	private onPointerDown(event: PointerEvent, id: string) {
		if (!this.personalizationEnabled) {
			return;
		}

		if (event.button !== 0) {
			return;
		}

		if (event.pointerType === "touch" || (event.pointerType === "pen" && this.isMobileMode())) {
			return;
		}

		const target = event.target as HTMLElement;
		if (target.closest("button")) {
			return;
		}

		const info = this.windows.get(id);
		if (!info) {
			return;
		}

		event.preventDefault();

		const orderWithoutDragged = this.getKnownOrder().filter((wid) => wid !== id);
		const positions = this.computePositions(id);
		const dropIndicator = this.ensureDropIndicator();
		dropIndicator.style.height = info.titleBar.offsetHeight + "px";

		this.dragState = {
			windowId: id,
			pointerId: event.pointerId,
			orderWithoutDragged,
			positions,
			dropIndex: this.getKnownOrder().indexOf(id)
		};

		info.element.classList.add("dragging");
		info.element.setAttribute("aria-grabbed", "true");
		info.titleBar.style.cursor = "grabbing";
		this.setDropEffect(id, true);
		this.updateDropIndicator(event.clientY);

		info.titleBar.setPointerCapture(event.pointerId);
		window.addEventListener("pointermove", this.onPointerMove);
		window.addEventListener("pointerup", this.onPointerUp);
		window.addEventListener("pointercancel", this.onPointerUp);
	}

	private handlePointerMove(event: PointerEvent) {
		if (!this.dragState || event.pointerId !== this.dragState.pointerId) {
			return;
		}
		this.pendingPointerY = event.clientY;
		if (this.pointerMoveRaf) {
			return;
		}
		this.pointerMoveRaf = window.requestAnimationFrame(() => {
			this.pointerMoveRaf = 0;
			if (this.dragState) {
				this.updateDropIndicator(this.pendingPointerY);
			}
		});
	}

	private handlePointerUp(event: PointerEvent) {
		if (!this.dragState || event.pointerId !== this.dragState.pointerId) {
			return;
		}

		const state = this.dragState;
		this.dragState = undefined;

		window.removeEventListener("pointermove", this.onPointerMove);
		window.removeEventListener("pointerup", this.onPointerUp);
		window.removeEventListener("pointercancel", this.onPointerUp);
		if (this.pointerMoveRaf) {
			window.cancelAnimationFrame(this.pointerMoveRaf);
			this.pointerMoveRaf = 0;
		}

		const info = this.windows.get(state.windowId);
		if (info) {
			info.element.classList.remove("dragging");
			info.element.setAttribute("aria-grabbed", "false");
			info.titleBar.style.cursor = this.personalizationEnabled && !this.isMobileMode() ? "grab" : "";
			if (info.titleBar.hasPointerCapture(event.pointerId)) {
				info.titleBar.releasePointerCapture(event.pointerId);
			}
		}

		this.setDropEffect(state.windowId, false);
		this.removeDropIndicator();

		const previousOrder = [...this.order];
		const newOrder = [...state.orderWithoutDragged];
		newOrder.splice(state.dropIndex, 0, state.windowId);
		this.order = this.mergeWithUnknown(newOrder);

		if (!this.ordersEqual(previousOrder, this.order)) {
			this.saveOrder();
			this.applyCurrentOrder();
			this.announceMove(state.windowId);
		} else {
			this.applyCurrentOrder();
		}
	}

	private onKeyDown(event: KeyboardEvent, id: string) {
		if (!this.personalizationEnabled) {
			return;
		}

		if (!event.altKey) {
			return;
		}

		if (event.key === "ArrowUp" || event.key === "Up") {
			event.preventDefault();
			this.moveWindow(id, -1);
		} else if (event.key === "ArrowDown" || event.key === "Down") {
			event.preventDefault();
			this.moveWindow(id, 1);
		}
	}

	private computePositions(excludeId: string) {
		const positions: { id: string; mid: number; element: HTMLElement }[] = [];
		for (const id of this.getKnownOrder()) {
			const info = this.windows.get(id);
			if (!info || id === excludeId) {
				continue;
			}
			const rect = info.element.getBoundingClientRect();
			positions.push({
				id,
				mid: rect.top + (rect.height / 2),
				element: info.element
			});
		}
		return positions;
	}

	private updateDropIndicator(pointerY: number) {
		if (!this.dragState) {
			return;
		}
		this.dragState.positions = this.computePositions(this.dragState.windowId);
		let dropIndex = 0;
		for (const pos of this.dragState.positions) {
			if (pointerY > pos.mid) {
				dropIndex++;
			} else {
				break;
			}
		}
		this.dragState.dropIndex = dropIndex;

		const indicator = this.ensureDropIndicator();
		const beforeId = this.dragState.orderWithoutDragged[dropIndex];
		if (beforeId) {
			const beforeInfo = this.windows.get(beforeId);
			if (beforeInfo) {
				this.container.insertBefore(indicator, beforeInfo.element);
				return;
			}
		}
		this.container.appendChild(indicator);
	}

	private ensureDropIndicator(): HTMLElement {
		if (!this.dropIndicator) {
			const indicator = document.createElement("div");
			indicator.className = "inventory-window__drop-indicator";
			indicator.setAttribute("aria-hidden", "true");
			this.dropIndicator = indicator;
		}
		return this.dropIndicator;
	}

	private removeDropIndicator() {
		if (this.dropIndicator && this.dropIndicator.parentElement) {
			this.dropIndicator.remove();
		}
	}

	private ensureOrderCompleteness() {
		const seen = new Set<string>();
		const merged: string[] = [];

		for (const id of this.order) {
			if (!seen.has(id)) {
				merged.push(id);
				seen.add(id);
			}
		}

		for (const id of this.defaultOrder) {
			if (!seen.has(id)) {
				merged.push(id);
				seen.add(id);
			}
		}

		this.order = merged;
	}

	private applyCurrentOrder() {
		const order = this.getKnownOrder();
		for (const id of order) {
			const info = this.windows.get(id);
			if (info) {
				this.container.appendChild(info.element);
			}
		}
		this.updateArrowsState();
	}

	private getKnownOrder(): string[] {
		return this.order.filter((id) => this.windows.has(id));
	}

	private mergeWithUnknown(knownOrder: string[]): string[] {
		const unknown = this.order.filter((id) => !this.windows.has(id));
		return [...knownOrder, ...unknown];
	}

	private updatePersonalizationControls(info: ManagedWindow) {
		if (this.personalizationEnabled && !this.isMobileMode()) {
			info.titleBar.style.cursor = "grab";
		} else {
			info.titleBar.style.cursor = "";
		}
		info.element.setAttribute("aria-grabbed", "false");
		this.ensureArrowButtons(info);
	}

	private ensureArrowButtons(info: ManagedWindow) {
		if (!info.upButton || !info.downButton) {
			const up = this.createArrowButton("▲", "Przenieś okno w górę", () => this.moveWindow(info.id, -1));
			const down = this.createArrowButton("▼", "Przenieś okno w dół", () => this.moveWindow(info.id, 1));

			if (info.toggleButton && info.toggleButton.parentElement === info.titleBar) {
				info.titleBar.insertBefore(up, info.toggleButton);
				info.titleBar.insertBefore(down, info.toggleButton);
			} else {
				info.titleBar.append(up, down);
			}

			info.upButton = up;
			info.downButton = down;
		}

		const showMobileControls = this.personalizationEnabled && this.isMobileMode();
		if (info.upButton) {
			info.upButton.hidden = !showMobileControls;
		}
		if (info.downButton) {
			info.downButton.hidden = !showMobileControls;
		}
	}

	private createArrowButton(label: string, ariaLabel: string, action: () => void): HTMLButtonElement {
		const button = document.createElement("button");
		button.type = "button";
		button.className = "inventory-window__reorder";
		button.textContent = label;
		button.setAttribute("aria-label", ariaLabel);
		button.hidden = true;
		button.addEventListener("click", (event) => {
			event.preventDefault();
			action();
		});
		return button;
	}

	private updateArrowsState() {
		const order = this.getKnownOrder();
		const show = this.personalizationEnabled && this.isMobileMode();
		order.forEach((id, index) => {
			const info = this.windows.get(id);
			if (!info) {
				return;
			}
			if (info.upButton) {
				info.upButton.hidden = !show;
				info.upButton.disabled = index === 0;
			}
			if (info.downButton) {
				info.downButton.hidden = !show;
				info.downButton.disabled = index === order.length - 1;
			}
		});
	}

	private setDropEffect(draggedId: string, active: boolean) {
		for (const info of this.windows.values()) {
			if (info.id === draggedId) {
				info.element.setAttribute("aria-grabbed", active ? "true" : "false");
			} else if (active) {
				info.element.setAttribute("aria-dropeffect", "move");
			} else {
				info.element.removeAttribute("aria-dropeffect");
				info.element.setAttribute("aria-grabbed", "false");
			}
		}
	}

	private loadOrder(): string[] {
		try {
			const stored = window.localStorage.getItem(this.orderStorageKey);
			if (stored) {
				const parsed = JSON.parse(stored);
				if (Array.isArray(parsed)) {
					return parsed.filter((id) => typeof id === "string");
				}
			}
		} catch (err) {
			console.warn("Unable to load right panel layout from localStorage", err);
		}
		return [];
	}

	private saveOrder() {
		try {
			window.localStorage.setItem(this.orderStorageKey, JSON.stringify(this.order));
		} catch (err) {
			console.warn("Unable to save right panel layout", err);
		}
	}

	private loadPersonalizationEnabled(): boolean {
		try {
			return window.localStorage.getItem(this.enabledStorageKey) === "true";
		} catch (err) {
			return false;
		}
	}

	private savePersonalizationEnabled(enabled: boolean) {
		try {
			window.localStorage.setItem(this.enabledStorageKey, enabled ? "true" : "false");
		} catch (err) {
			console.warn("Unable to persist right panel personalization preference", err);
		}
	}

	private createLiveRegion(): HTMLElement {
		const region = document.createElement("div");
		region.setAttribute("role", "status");
		region.setAttribute("aria-live", "polite");
		region.style.position = "absolute";
		region.style.width = "1px";
		region.style.height = "1px";
		region.style.margin = "-1px";
		region.style.border = "0";
		region.style.padding = "0";
		region.style.clip = "rect(0 0 0 0)";
		region.style.overflow = "hidden";
		region.style.whiteSpace = "nowrap";
		this.container.appendChild(region);
		return region;
	}

	private announceMove(id: string) {
		const info = this.windows.get(id);
		if (!info) {
			return;
		}
		const order = this.getKnownOrder();
		const index = order.indexOf(id);
		if (index === -1) {
			return;
		}

		let message: string;
		const title = this.getWindowTitle(info);
		if (index === 0) {
			message = `Przeniesiono „${title}” na początek listy.`;
		} else if (index === order.length - 1) {
			message = `Przeniesiono „${title}” na koniec listy.`;
		} else {
			const belowId = order[index + 1];
			const belowInfo = belowId ? this.windows.get(belowId) : undefined;
			const belowTitle = belowInfo ? this.getWindowTitle(belowInfo) : "";
			message = `Przeniesiono „${title}” nad „${belowTitle}”.`;
		}
		this.announce(message);
	}

	private announce(message: string) {
		this.liveRegion.textContent = "";
		window.setTimeout(() => {
			this.liveRegion.textContent = message;
		}, 50);
	}

	private getWindowTitle(info: ManagedWindow): string {
		const text = info.titleElement?.textContent?.trim();
		return text && text.length > 0 ? text : info.id;
	}

	private ordersEqual(a: string[], b: string[]): boolean {
		if (a.length !== b.length) {
			return false;
		}
		for (let i = 0; i < a.length; i++) {
			if (a[i] !== b[i]) {
				return false;
			}
		}
		return true;
	}

	private isMobileMode(): boolean {
		try {
			if (typeof stendhal !== "undefined" && stendhal.session && typeof stendhal.session.touchOnly === "function") {
				return !!stendhal.session.touchOnly();
			}
		} catch (err) {
			// ignore
		}
		if (window.matchMedia) {
			const coarse = window.matchMedia("(pointer: coarse)");
			const fine = window.matchMedia("(pointer: fine)");
			return coarse.matches && !fine.matches;
		}
		return false;
	}

	private cancelDrag() {
		if (!this.dragState) {
			return;
		}
		const state = this.dragState;
		this.dragState = undefined;
		if (this.pointerMoveRaf) {
			window.cancelAnimationFrame(this.pointerMoveRaf);
			this.pointerMoveRaf = 0;
		}
		window.removeEventListener("pointermove", this.onPointerMove);
		window.removeEventListener("pointerup", this.onPointerUp);
		window.removeEventListener("pointercancel", this.onPointerUp);
		const info = this.windows.get(state.windowId);
		if (info) {
			info.element.classList.remove("dragging");
			info.element.setAttribute("aria-grabbed", "false");
			info.titleBar.style.cursor = this.personalizationEnabled && !this.isMobileMode() ? "grab" : "";
			if (info.titleBar.hasPointerCapture(state.pointerId)) {
				info.titleBar.releasePointerCapture(state.pointerId);
			}
		}
		this.setDropEffect(state.windowId, false);
		this.removeDropIndicator();
	}
}