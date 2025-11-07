import { Component } from "../toolkit/Component";
import { InventoryWindowOrderManager } from "./InventoryWindowOrderManager";

interface InventoryWindowElements {
	root: HTMLElement;
	body?: HTMLElement;
	toggle?: HTMLButtonElement;
	title?: HTMLElement;
}

export class InventoryWindowController {

	private static windows: Map<string, InventoryWindowElements> = new Map();

	public static register(id: string, options?: { title?: string; collapsed?: boolean }) {
		const root = document.getElementById(id);
		if (!root) {
			return;
		}

		const elements: InventoryWindowElements = {
			root,
			body: root.querySelector<HTMLElement>("[data-window-body]") || undefined,
			toggle: root.querySelector<HTMLButtonElement>("[data-window-toggle]") || undefined,
			title: root.querySelector<HTMLElement>("[data-window-title]") || undefined
		};

		this.windows.set(id, elements);

		try {
			InventoryWindowOrderManager.get().registerWindow(id, root);
		} catch (err) {
			console.warn("Unable to register inventory window for personalization", err);
		}

		if (options?.title && elements.title) {
			elements.title.textContent = options.title;
		}

		if (elements.toggle) {
			elements.toggle.addEventListener("click", () => this.toggle(id));
		}

		this.applyCollapsed(elements, !!options?.collapsed);

		const initiallyHidden = root.classList.contains("inventory-window--hidden")
				|| root.hasAttribute("hidden")
				|| getComputedStyle(root).display === "none";
		this.setWindowVisibility(id, !initiallyHidden);
	}

	public static attachComponent(id: string, component: Component) {
		const elements = this.windows.get(id);
		if (!elements || !elements.body) {
			return;
		}

		if (!elements.body.contains(component.componentElement)) {
			elements.body.append(component.componentElement);
		}

		const originalSetVisible = component.setVisible.bind(component);
		(component as any).setVisible = (visible=true) => {
			originalSetVisible(visible);
			InventoryWindowController.setWindowVisibility(id, visible);
		};

		const originalToggle = component.toggleVisibility.bind(component);
		(component as any).toggleVisibility = () => {
			originalToggle();
			InventoryWindowController.setWindowVisibility(id, component.isVisible());
		};

		this.setWindowVisibility(id, component.isVisible());
	}

	public static setTitle(id: string, title: string) {
		const elements = this.windows.get(id);
		if (elements?.title) {
			elements.title.textContent = title;
		}
	}

	public static setWindowVisibility(id: string, visible: boolean) {
		const elements = this.windows.get(id);
		if (!elements) {
			return;
		}
		elements.root.classList.toggle("inventory-window--hidden", !visible);
		elements.root.setAttribute("aria-hidden", (!visible).toString());
	}

	private static toggle(id: string) {
		const elements = this.windows.get(id);
		if (!elements) {
			return;
		}
		const collapsed = !elements.root.classList.contains("inventory-window--collapsed");
		this.applyCollapsed(elements, collapsed);
	}

	private static applyCollapsed(elements: InventoryWindowElements, collapsed: boolean) {
		elements.root.classList.toggle("inventory-window--collapsed", collapsed);
		if (elements.body) {
			elements.body.setAttribute("aria-hidden", collapsed.toString());
		}
		if (elements.toggle) {
			elements.toggle.setAttribute("aria-expanded", (!collapsed).toString());
			elements.toggle.textContent = collapsed ? "+" : "−";
			elements.toggle.setAttribute("aria-label", collapsed ? "Rozwiń okno" : "Zwiń okno");
		}
	}
}
