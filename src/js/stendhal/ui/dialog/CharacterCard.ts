/** Literal account data must never be interpreted as markup or translated. */
export function createCharacterCard(attributes: {[key: string]: any}, preview: HTMLElement,
	onSelect: (name: string) => void, translate: (text: string) => string): HTMLButtonElement {
	const button = document.createElement("button");
	button.type = "button";
	button.className = "character-card";
	button.setAttribute("data-no-translate", "");
	preview.classList.add("character-card__preview");
	preview.setAttribute("aria-hidden", "true");
	button.append(preview);
	const details = document.createElement("span"); details.className = "character-card__details";
	const name = String(attributes.name ?? "");
	const nickname = document.createElement("strong"); nickname.textContent = name;
	details.append(nickname);
	const stat = (key: string) => {
		const number = Number(attributes[key]);
		return Number.isFinite(number) && number >= 0 ? Math.floor(number) : 0;
	};
	for (const text of [translate("Poziom:") + " " + stat("level"),
		translate("Umiejętności:") + " " + stat("atk") + "/" + stat("def")]) {
		const line = document.createElement("span"); line.textContent = text; details.append(line);
	}
	button.append(details);
	button.addEventListener("click", () => onSelect(name));
	return button;
}
