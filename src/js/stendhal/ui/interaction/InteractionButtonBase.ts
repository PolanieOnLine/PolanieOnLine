/***************************************************************************
 *                     Copyright © 2025 - PolanieOnLine                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { Component } from "../toolkit/Component";


export interface InteractionButtonOptions {
	id: string;
	classes?: string[];
	ariaLabel: string;
	title: string;
	ariaPressed?: string;
}

export abstract class InteractionButtonBase extends Component {

	protected readonly boundUpdate: () => void;
	protected resizeObserver?: ResizeObserver;
	protected mutationObserver?: MutationObserver;
	private readonly containerId: string;

	constructor(element: HTMLButtonElement, containerId = "attack-button-container") {
		super(element);
		this.containerId = containerId;
		this.boundUpdate = this.update.bind(this);
	}

	protected static createButton(options: InteractionButtonOptions): HTMLButtonElement {
		const element = document.createElement("button");
		element.id = options.id;
		element.classList.add("unclickable", "hidden");
		if (options.classes?.length) {
			element.classList.add(...options.classes);
		}
		element.setAttribute("aria-label", options.ariaLabel);
		element.title = options.title;
		if (options.ariaPressed !== undefined) {
			element.setAttribute("aria-pressed", options.ariaPressed);
		}
		return element;
	}

	public mount() {
		const container = document.getElementById(this.containerId) || document.body;
		if (!container.contains(this.componentElement)) {
			container.appendChild(this.componentElement);
		}

		this.componentElement.classList.remove("hidden");

		this.update();
		window.addEventListener("resize", this.boundUpdate);
		window.addEventListener("scroll", this.boundUpdate);
		this.observeLayoutChanges();
	}

	public unmount() {
		if (this.componentElement.parentElement) {
			this.componentElement.remove();
		}
		this.componentElement.classList.add("hidden");
		window.removeEventListener("resize", this.boundUpdate);
		window.removeEventListener("scroll", this.boundUpdate);
		this.resizeObserver?.disconnect();
		this.resizeObserver = undefined;
		this.mutationObserver?.disconnect();
		this.mutationObserver = undefined;
	}

	protected observeLayoutChanges() {
		if (this.resizeObserver || typeof ResizeObserver === "undefined") {
			return;
		}
		const viewport = document.getElementById("viewport");
		const clientRoot = document.getElementById("client");
		if (!viewport && !clientRoot) {
			return;
		}
		if (viewport) {
			this.resizeObserver = new ResizeObserver(() => this.update());
			this.resizeObserver.observe(viewport);
		}
		if (clientRoot) {
			this.mutationObserver = new MutationObserver(() => this.update());
			this.mutationObserver.observe(clientRoot, { attributes: true, attributeFilter: ["class"] });
		}
	}

	public abstract update(): void;
}
