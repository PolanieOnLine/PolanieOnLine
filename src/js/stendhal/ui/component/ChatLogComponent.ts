/***************************************************************************
 *                (C) Copyright 2003-2024 - Faiumoni e. V.                 *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU Affero General Public License as        *
 *   published by the Free Software Foundation; either version 3 of the    *
 *   License, or (at your option) any later version.                       *
 *                                                                         *
 ***************************************************************************/

import { ui } from "../UI";
import { Component } from "../toolkit/Component";
import { Panel } from "../toolkit/Panel";
import { TabPanelComponent } from "../toolkit/TabPanelComponent";
import { Layout } from "../../data/enum/Layout";
import { MenuItem } from "../../action/MenuItem";
import { UIComponentEnum } from "../UIComponentEnum";
import { singletons } from "../../SingletonRepo";
import { Chat } from "../../util/Chat";
import { GameLanguage } from "../../util/GameLanguage";

import { stendhal } from "../../stendhal";

/**
 * Chat Log
 */
export class ChatLogComponent extends Component {
	private readonly tabs: TabPanelComponent;
	private readonly tabButtons: HTMLElement;
	private readonly logs: HTMLElement[] = [];
	private readonly scrollStateBottom = [true, true];
	private readonly scrollPositions = [0, 0];
	private readonly unreadChannels = new Set<number>();
	/** Event listener for managing scroll position when component properties change such as changes to visibility. */
	private scrollListener: EventListenerOrEventListenerObject;

	constructor() {
		super("chat");
		this.tabs = new TabPanelComponent(Layout.BOTTOM);
		this.tabs.componentElement.classList.add("chat-tabs-panel");
		for (const [index, label] of ["Główny", "Prywatny"].entries()) {
			const log = document.createElement("div");
			log.className = "chat-log-pane";
			log.id = "chat-channel-" + index;
			log.setAttribute("role", "tabpanel");
			log.setAttribute("data-no-translate", "");
			this.logs.push(log);
			this.tabs.addTab(label, new Panel(log));
		}
		this.tabs.onTabChanged = () => this.onChannelChanged();
		const buttons = this.tabs.componentElement.querySelector<HTMLElement>(".tabpanel-tabs")!;
		this.tabButtons = buttons;
		buttons.setAttribute("role", "tablist");
		for (let index = 0; index < buttons.children.length; index++) {
			const button = buttons.children[index];
			button.setAttribute("role", "tab");
			button.setAttribute("aria-controls", this.logs[index].id);
		}
		this.updateTabMarkers();
		this.componentElement.appendChild(this.tabs.componentElement);
		GameLanguage.registerUI(buttons as HTMLElement);
		this.refresh();

		for (const log of this.logs) {
			log.addEventListener("mouseup", (evt: MouseEvent) => {
				this.onContextMenu(evt);
			});
			log.addEventListener("touchstart", (evt: TouchEvent) => {
				const pos = stendhal.ui.html.extractPosition(evt);
				stendhal.ui.touch.onTouchStart(pos.pageX, pos.pageY);
			}, {passive: true});
			log.addEventListener("touchend", (evt: TouchEvent) => {
				stendhal.ui.touch.onTouchEnd();
				this.onContextMenu(evt);
				stendhal.ui.touch.unsetOrigin();
			});
		}

		this.scrollListener = (evt: Event) => {
			if (evt.currentTarget === this.currentLog()) {
				this.onScroll();
			}
		};
		if (this.isVisible()) {
			for (const log of this.logs) {
				log.addEventListener("scroll", this.scrollListener);
			}
		}
	}

	/**
	 * Updates font from config.
	 */
	public override refresh() {
		this.componentElement.style.setProperty("font-family", stendhal.config.get("font.chat"));
		GameLanguage.translateUI(this.tabButtons);
	}

	/**
	 * Generates a timestamp.
	 *
	 * @return {HTMLSpanElement}
	 *   `HTMLSpanElement` element with inner text formatted with timestamp.
	 */
	private createTimestamp(): HTMLSpanElement {
		const date = new Date();

		const hours = date.getHours().toString().padStart(2, "0");
		const minutes = date.getMinutes().toString().padStart(2, "0");
		const seconds = date.getSeconds().toString().padStart(2, "0");

		const time = `${hours}:${minutes}:${seconds}`;
	
		const timestamp = document.createElement("span");
		timestamp.className = "logtimestamp";
		timestamp.textContent = `[${time}]`;
	
		return timestamp;
	}

	/**
	 * Adds a line of text.
	 *
	 * @param row {HTMLDivElement}
	 *   Div containing text to be added.
	 */
	private add(row: HTMLDivElement, type: string) {
		const selected = this.tabs.getCurrentIndex();
		// Java sends local client responses only to the channel being read.
		const destinations = type === "client" ? [selected]
			: ["privmsg", "group", "support", "tutorial"].includes(type) ? [0, 1] : [0];
		for (const [position, index] of destinations.entries()) {
			const wasAtBottom = index === selected ? this.isAtBottom() : this.scrollStateBottom[index];
			this.logs[index].appendChild(position === 0 ? row : row.cloneNode(true));
			if (index === selected && wasAtBottom) {
				this.scrollToBottom();
			} else if (index !== selected) {
				this.unreadChannels.add(index);
			}
		}
		this.updateTabMarkers();
	}

	private currentLog(): HTMLElement {
		return this.logs[this.tabs.getCurrentIndex()];
	}

	private onChannelChanged() {
		const index = this.tabs.getCurrentIndex();
		this.unreadChannels.delete(index);
		if (this.scrollStateBottom[index]) {
			this.scrollToBottom();
		} else {
			this.setScroll(this.scrollPositions[index]);
		}
		this.updateTabMarkers();
	}

	private updateTabMarkers() {
		const buttons = this.tabButtons.children;
		for (let index = 0; index < buttons.length; index++) {
			buttons[index].classList.toggle("chat-unread", this.unreadChannels.has(index));
			buttons[index].setAttribute("aria-selected", String(index === this.tabs.getCurrentIndex()));
		}
	}

	/**
	 * Adds a line of text.
	 *
	 * @param type
	 *   Message type.
	 * @param message
	 *   Text to be added.
	 * @param orator
	 *   Name of entity making the expression (default: `undefined`).
	 * @param timestamp
	 *   If `false`, suppresses prepending message with timestamp (default: `true`).
	 */
	public addLine(type: string, message: string, orator?: string, timestamp = true) {
		if (type === "client" && !orator) message = GameLanguage.getText(message);
		const lcol = document.createElement("div");
		lcol.className = "logcolL";
		lcol.appendChild(timestamp ? this.createTimestamp() : document.createTextNode(" "));

		const rcol = document.createElement("div");
		rcol.className = "logcolR log" + type;

		if (orator) {
			rcol.appendChild(this.makeSpan("lognick", `<${orator}>`));
			rcol.appendChild(document.createTextNode(" "));
		}

		// bez logmsg, bez dodatkowego spana
		rcol.innerHTML += Chat.formatLogEntry(message);

		const row = document.createElement("div");
		row.className = "logrow";
		row.appendChild(lcol);
		row.appendChild(rcol);

		this.add(row, type);
	}

	/**
	 * Adds multiple lines of text.
	 *
	 * @param type {string}
	 *   Message type.
	 * @param messages {string[]}
	 *   Array of texts to be added.
	 * @param orator
	 *   Name of entity making the expression (default: `undefined`).
	 */
	public addLines(type: string, messages: string[], orator?: string) {
		let stamped = false;
		for (const line of messages) {
			if (!stamped) {
				this.addLine(type, line, orator);
				stamped = true;
			} else {
				this.addLine(type, line, orator, false);
			}
		}
	}

	/**
	 * Adds a line displaying an emoji image.
	 *
	 * @param emoji {HTMLImageElement}
	 *   Emoji image sprite.
	 * @param orator {string}
	 *   Name of entity making the expression (default: `undefined`).
	 */
	public addEmojiLine(emoji: HTMLImageElement, orator?: string) {
		const lcol = document.createElement("div");
		lcol.className = "logcolL";
		lcol.appendChild(this.createTimestamp());

		const rcol = document.createElement("div");
		rcol.className = "logcolR lognormal";

		if (orator) {
			rcol.appendChild(this.makeSpan("lognick", `<${orator}>`));
			rcol.appendChild(document.createTextNode(" "));
		}

		rcol.appendChild(emoji.cloneNode() as HTMLImageElement);

		const row = document.createElement("div");
		row.className = "logrow";
		row.appendChild(lcol);
		row.appendChild(rcol);

		this.add(row, "normal");
	}

	private makeSpan(className: string, text: string): HTMLSpanElement {
		const s = document.createElement("span");
		s.className = className;
		s.textContent = text;
		return s;
	}

	/**
	 * Removes all text from log.
	 */
	public clear() {
		this.currentLog().innerHTML = "";
		const index = this.tabs.getCurrentIndex();
		this.scrollPositions[index] = 0;
		this.scrollStateBottom[index] = true;
	}

	/**
	 * Sets scrolled position.
	 *
	 * @param scroll {number}
	 *   New scrolled position.
	 */
	private setScroll(scroll: number) {
		this.currentLog().scrollTop = scroll;
		this.onScroll();
	}

	/**
	 * Sets scrolled position to end of log.
	 */
	private scrollToBottom() {
		this.setScroll(this.currentLog().scrollHeight);
	}

	/**
	 * Checks if scrolled state represents end of log.
	 *
	 * @return
	 *   `true` if recent state is less than 0 or scroll position is same as element height.
	 */
	private isAtBottom(): boolean {
		const log = this.currentLog();
		return log.scrollHeight - log.clientHeight <= log.scrollTop + 5;
	}

	/**
	 * Called when a scroll event occurs.
	 */
	private onScroll() {
		// remember scrolled state
		const index = this.tabs.getCurrentIndex();
		this.scrollStateBottom[index] = this.isAtBottom();
		this.scrollPositions[index] = this.currentLog().scrollTop;
	}

	/**
	 * Doesn't listen for scroll events while chat panel/log is hidden.
	 */
	public onHide() {
		// stop listening for scroll events when chat panel is hidden
		for (const log of this.logs) {
			log.removeEventListener("scroll", this.scrollListener);
		}
	}

	/**
	 * Re-activates scroll event listener when chat panel/log visibility is restored.
	 */
	public onUnhide() {
		if (this.scrollStateBottom[this.tabs.getCurrentIndex()]) {
			this.scrollToBottom();
		}
		// don't listen for scroll events until AFTER scroll state has been updated
		for (const log of this.logs) {
			log.addEventListener("scroll", this.scrollListener);
		}
	}

	/**
	 * Copies log text to clipboard or exports to file.
	 *
	 * @param clipboard {boolean}
	 *   If `true` text is copied to clipboard.
	 * @fixme
	 *   File export currently not supported.
	 */
	public exportContents(clipboard=true) {
		if (clipboard && (!navigator || !navigator.clipboard)) {
			console.warn("copying to clipboard not supported by this browser");
			return;
		}

		const lines = [];
		// try to get highlightext text first
		if (window.getSelection) {
			const sel = window.getSelection();
			let value;
			if (sel && sel.type == "Range") {
				// FIXME: how to get only text from log when text from multiple elements is selected?
				if (sel.rangeCount == 1) {
					value = sel.toString()
				}
			}
			if (value && value !== "") {
				lines.push(value);
			}
		//~ } else if (document.selection && document.selection.type == "Text") {
			//~ lines.push(document.selection.createRange().text);
		}
		// if no highlighted text, copy the entire log
		if (lines.length == 0) {
			const children = this.currentLog().children;
			for (let idx = 0; idx < children.length; idx++) {
				const row = children[idx];
				let text = row.children[0].innerHTML.trim() + " ";
				if (text.trim() === "") {
					text = "    ";
				}
				text = this.plainText(text + row.children[1].innerHTML,
						["span", "div"]);
				lines.push(text.replace("&lt;", "<").replace("&gt;", ">"));
			}
		}

		if (lines.length > 0) {
			if (clipboard) {
				navigator.clipboard.writeText(lines.join("\n"));
				return;
			}
			singletons.getDownloadUtil().buildChatLog(lines.join("\n")).execute();
		}
	}

	/**
	 * Removes HTML tag formatting from a string.
	 *
	 * @param msg {string}
	 *   Message to format.
	 * @param tags {string[]}
	 *   List of tags to filter for removal (default: []).
	 * @return {string}
	 *   Plain text formatted message.
	 */
	private plainText(msg: string, tags?: string[]): string {
		if (!tags) {
			msg = msg.replace(/<.*?>/g, "");
		} else {
			for (const tag of tags) {
				msg = msg.replace(new RegExp("<" + tag + ".*?>", "g"), "")
						.replace(new RegExp("</" + tag + ">", "g"), "");
			}
		}

		return msg;
	}

	/**
	 * Handles creating a custom context menu with options "Clear" & "Copy".
	 *
	 * FIXME: text highlighting interferes with opening context menu with touch
	 *
	 * @param evt {any}
	 *   Mouse or touch event.
	 */
	private onContextMenu(evt: any) {
		if (!evt || stendhal.ui.actionContextMenu.isOpen()) {
			return;
		}
		if (evt.type === "mouseup" && evt.button != 2) {
			return;
		}
		if (evt.type === "touchend" && !stendhal.ui.touch.isLongTouch(evt)) {
			evt.preventDefault();
			return;
		}

		// setting "log" to "this" here doesn't work
		const log = ui.get(UIComponentEnum.ChatLog) as ChatLogComponent;
		const options: MenuItem[] = [
			new MenuItem("Zapisz", function() { log.exportContents(false); }),
			new MenuItem("Wyczyść", function() { log.clear(); })
		];

		if (navigator && navigator.clipboard) {
			options.unshift(new MenuItem("Kopiuj", function() { log.exportContents(); }));
		}

		const pos = stendhal.ui.html.extractPosition(evt);
		stendhal.ui.actionContextMenu.set(ui.createSingletonFloatingWindow("Czynności",
				new LogContextMenu(options), pos.pageX - 50, pos.pageY - 5));

		evt.preventDefault();
		evt.stopPropagation();
	}
}


/**
 * Custom context menu component.
 */
class LogContextMenu extends Component {

	/** Available options. */
	options!: MenuItem[];


	constructor(options: MenuItem[]) {
		super("contextmenu-template", true);
		this.options = options;

		let content = "<div class=\"actionmenu verticalgroup\">";
		for (let i = 0; i < this.options.length; i++) {
			content += "<button class=\"actionbutton\" id=\"actionbutton." + i + "\">" + stendhal.ui.html.esc(this.options[i].title) + "</button>";
		}
		content += "</div>";
		this.componentElement.innerHTML = content;

		this.componentElement.addEventListener("click", (evt) => {
			this.onClick(evt);
		});
	}

	/**
	 * Handles executing commands when an option is clicked/tapped.
	 */
	private onClick(evt: Event) {
		let iStr = (evt.target as HTMLElement).getAttribute("id")?.substring(13);
		if (iStr === undefined || iStr === "") {
			return;
		}
		let i = parseInt(iStr);
		if (i < 0) {
			return;
		}

		this.componentElement.dispatchEvent(new Event("close"));

		if (i >= this.options.length) {
			throw new Error("actions index is larger than number of actions");
		}

		const action = this.options[i].action;
		if (action) {
			action();
		} else {
			console.error("chat log context menu action failed");
		}
	}
}

