/** Regroup already-bound controls, preserving their values and event listeners. */
export function organizeSettings(root: HTMLElement): {label: string; element: HTMLElement}[] {
	const definitions = [
		["controls", "Sterowanie"], ["display", "Obraz"], ["appearance", "Wygląd"],
		["text", "Tekst i powiadomienia"], ["combat", "Walka i przedmioty"], ["sound", "Dźwięk"]
	];
	const pages = definitions.map(([id, label]) => {
		const element = document.createElement("div");
		element.className = "settings-category";
		element.dataset.category = id;
		return {label, element};
	});
	const group = (page: number, title: string, ids: string[]) => {
		const fieldset = document.createElement("fieldset");
		const legend = document.createElement("legend"); legend.textContent = title;
		fieldset.append(legend);
		for (const id of ids) {
			const control = root.querySelector<HTMLElement>(`#${id}, [data-toggle-id="${id}"]`);
			if (!control) continue;
			const row = control.closest<HTMLElement>("label.checksetting") || control.closest(".settings-range")?.parentElement ||
				(control.tagName === "BUTTON" ? control : control.parentElement!);
			fieldset.append(row);
		}
		if (fieldset.children.length > 1) pages[page].element.append(fieldset);
	};
	group(0, "Poruszanie się", ["chk_movecont"]);
	group(0, "Klikanie", ["chk_dblclick"]);
	group(1, "Obraz", ["chk_zoom", "rngviewporttilescalemobile"]);
	group(2, "Wygląd", ["selectlanguage", "selecttheme", "selfontbody", "selmenustyle"]);
	group(2, "Panel statystyk", ["chk_charname", "chk_hpbar"]);
	group(2, "Prawy panel", ["chk_rightpanel_personalize", "btn_rightpanel_reset"]);
	group(3, "Tekst i powiadomienia", ["chk_speechcr", "chk_floatchat", "chk_hidechat", "chk_pvtsnd", "chk_nativeemojis", "selfontchat", "selfonttlog", "txtchatopts"]);
	group(4, "Walka i przedmioty", ["chk_chestqp", "chk_itemtooltipcomparison", "chk_attack_button", "chk_loot_button", "chk_attack_players"]);
	// Move the complete controller-owned containers, including dynamically created controls.
	for (const [id, page] of [["settings-input", 0], ["settings-visuals", 1], ["settings-sound", 5]] as const) {
		const source = root.querySelector<HTMLElement>(`#${id}`)!;
		source.className = "settings-category-controls";
		const fieldset = document.createElement("fieldset");
		const legend = document.createElement("legend");
		legend.textContent = page === 1 ? "Efekty" : pages[page].label;
		fieldset.append(legend, source);
		pages[page].element.append(fieldset);
	}
	root.querySelector("#settings-general")!.remove();
	return pages;
}
