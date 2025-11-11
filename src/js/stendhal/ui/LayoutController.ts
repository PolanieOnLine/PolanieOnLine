import { singletons } from "../SingletonRepo";

/**
 * Controls responsive side panel layout for the web client.
 */
export class LayoutController {

	private static instance?: LayoutController;

	public static get(): LayoutController {
		if (!LayoutController.instance) {
			LayoutController.instance = new LayoutController();
		}
		return LayoutController.instance;
	}

	private readonly mediaQuery: MediaQueryList;
	private readonly client: HTMLElement;
	private readonly leftToggle?: HTMLButtonElement;
	private readonly rightToggle?: HTMLButtonElement;
	private readonly minimapFrame?: HTMLElement;
	private readonly minimapOverlay?: HTMLElement;
	private readonly minimapOriginalParent?: HTMLElement;
	private readonly minimapNextSibling?: Node | null;

	private requestViewportResize() {
		const viewport = singletons.getViewPort();
		viewport.refreshBounds();
	}

	private constructor() {
		this.mediaQuery = window.matchMedia("(max-width: 1024px)");

		const client = document.getElementById("client");
		if (!client) {
			throw new Error("Missing client container element");
		}
		this.client = client;

		this.leftToggle = document.getElementById("left-panel-toggle") as HTMLButtonElement | null || undefined;
		this.rightToggle = document.getElementById("right-panel-toggle") as HTMLButtonElement | null || undefined;

		const minimapFrame = document.getElementById("minimap-frame");
		this.minimapFrame = minimapFrame || undefined;
		this.minimapOverlay = document.getElementById("minimap-overlay") || undefined;
		this.minimapOriginalParent = minimapFrame?.parentElement || undefined;
		this.minimapNextSibling = minimapFrame?.nextSibling || null;

		this.handleMediaChange = this.handleMediaChange.bind(this);
	}

	/**
	 * Initializes responsive listeners.
	 */
	public init() {
		this.leftToggle?.addEventListener("click", () => {
			if (!this.mediaQuery.matches) {
				return;
			}
			this.togglePanel("left-panel-collapsed", this.leftToggle!, {
				expandedLabel: "Schowaj lewy panel",
				collapsedLabel: "Pokaż lewy panel",
				expandedIcon: "◀",
				collapsedIcon: "▶"
			});
			this.updateMinimapPosition();
			this.requestViewportResize();
		});

		this.rightToggle?.addEventListener("click", () => {
			if (!this.mediaQuery.matches) {
				return;
			}
			this.togglePanel("right-panel-collapsed", this.rightToggle!, {
				expandedLabel: "Schowaj prawy panel",
				collapsedLabel: "Pokaż prawy panel",
				expandedIcon: "▶",
				collapsedIcon: "◀"
			});
			this.requestViewportResize();
		});

		if (typeof this.mediaQuery.addEventListener === "function") {
			this.mediaQuery.addEventListener("change", this.handleMediaChange);
		} else if (typeof (this.mediaQuery as any).addListener === "function") {
			(this.mediaQuery as any).addListener(this.handleMediaChange);
		}

		this.handleMediaChange(this.mediaQuery);
	}

	private togglePanel(className: string, toggle: HTMLButtonElement, labels: {
		expandedLabel: string;
		collapsedLabel: string;
		expandedIcon: string;
		collapsedIcon: string;
	}) {
		const collapsed = this.client.classList.toggle(className);
		toggle.textContent = collapsed ? labels.collapsedIcon : labels.expandedIcon;
		toggle.setAttribute("aria-expanded", (!collapsed).toString());
		toggle.setAttribute("aria-label", collapsed ? labels.collapsedLabel : labels.expandedLabel);
	}

	private handleMediaChange(event: MediaQueryList | MediaQueryListEvent) {
		const matches = "matches" in event ? event.matches : (event as MediaQueryList).matches;
		if (!matches) {
			this.client.classList.remove("left-panel-collapsed", "right-panel-collapsed");
			this.resetToggle(this.leftToggle, "◀", "Schowaj lewy panel");
			this.resetToggle(this.rightToggle, "▶", "Schowaj prawy panel");
			this.restoreMinimap();
			this.requestViewportResize();
			return;
		}

		this.updateToggle(this.leftToggle, this.client.classList.contains("left-panel-collapsed"), {
			expandedLabel: "Schowaj lewy panel",
			collapsedLabel: "Pokaż lewy panel",
			expandedIcon: "◀",
			collapsedIcon: "▶"
		});
		this.updateToggle(this.rightToggle, this.client.classList.contains("right-panel-collapsed"), {
			expandedLabel: "Schowaj prawy panel",
			collapsedLabel: "Pokaż prawy panel",
			expandedIcon: "▶",
			collapsedIcon: "◀"
		});
		this.updateMinimapPosition();
		this.requestViewportResize();
	}

	private resetToggle(toggle: HTMLButtonElement | undefined, icon: string, label: string) {
		if (!toggle) {
			return;
		}
		toggle.textContent = icon;
		toggle.setAttribute("aria-expanded", "true");
		toggle.setAttribute("aria-label", label);
	}

	private updateToggle(toggle: HTMLButtonElement | undefined, collapsed: boolean, labels: {
		expandedLabel: string;
		collapsedLabel: string;
		expandedIcon: string;
		collapsedIcon: string;
	}) {
		if (!toggle) {
			return;
		}
		toggle.textContent = collapsed ? labels.collapsedIcon : labels.expandedIcon;
		toggle.setAttribute("aria-expanded", (!collapsed).toString());
		toggle.setAttribute("aria-label", collapsed ? labels.collapsedLabel : labels.expandedLabel);
	}

	private updateMinimapPosition() {
		if (!this.minimapFrame || !this.minimapOverlay || !this.minimapOriginalParent) {
			return;
		}

		const shouldOverlay = this.mediaQuery.matches && this.client.classList.contains("left-panel-collapsed");
		if (shouldOverlay) {
			if (this.minimapFrame.parentElement !== this.minimapOverlay) {
				this.minimapOverlay.appendChild(this.minimapFrame);
			}
			this.minimapOverlay.hidden = false;
		} else {
			this.restoreMinimap();
		}
	}

	private restoreMinimap() {
		if (!this.minimapFrame || !this.minimapOriginalParent || !this.minimapOverlay) {
			return;
		}
		if (this.minimapFrame.parentElement !== this.minimapOriginalParent) {
			if (this.minimapNextSibling && this.minimapNextSibling.parentNode === this.minimapOriginalParent) {
				this.minimapOriginalParent.insertBefore(this.minimapFrame, this.minimapNextSibling);
			} else {
				this.minimapOriginalParent.insertBefore(this.minimapFrame, this.minimapOriginalParent.firstChild);
			}
		}
		this.minimapOverlay.hidden = true;
	}
}
