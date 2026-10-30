import { marauroa } from "marauroa";
import { Item } from "../../entity/Item";
import { buildStructuredItemTooltip, appendStructuredTooltipLine } from "../component/ItemTooltipPresentation";
import { singletons } from "../../SingletonRepo";
import { Paths } from "../../data/Paths";
import { FloatingWindow } from "../toolkit/FloatingWindow";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";

type ShopEventData = {[key: string]: any};

interface ShopEntry {
    key: string;
    limit: number;
    snapshot?: Item;
    name: string;
    price: string;
    itemClass: string;
    subclass: string;
    stackable: boolean;
    description: string;
    priceValue: number;
}

/** Merchant catalogue shown from the NPC context menu. */
export class NpcShopDialog extends DialogContentComponent {
    private static active?: NpcShopDialog;
    private static closed = new Set<string>();
    private windowId = "";
    private sequence = 0;
    private lastRequest = 0;
    private money = 0;
    private priceFactor = 1;
    private polling?: ReturnType<typeof setInterval>;
    private animation?: ReturnType<typeof setInterval>;
    private readonly presets: HTMLButtonElement[] = [];
    private readonly detailStats = document.createElement("div");
    private readonly zone = marauroa.currentZoneName;

    private readonly wallet = document.createElement("div");
    private readonly search = document.createElement("input");
    private readonly buyTab = document.createElement("button");
    private readonly sellTab = document.createElement("button");
    private readonly listCount = document.createElement("div");
    private readonly listBox = document.createElement("div");
    private readonly detailIcon = document.createElement("img");
    private readonly detailName = document.createElement("h4");
    private readonly detailRarity = document.createElement("span");
    private readonly detailPrice = document.createElement("div");
    private readonly detailDescription = document.createElement("p");
    private readonly quantityMinus = document.createElement("button");
    private readonly quantityPlus = document.createElement("button");
    private readonly quoteTotal = document.createElement("div");
    private readonly tradeRequirement = document.createElement("div");
    private purchaseRequirement = "";
    private saleRequirement = "";
    private readonly quantity = document.createElement("input");
    private readonly quantityControls = document.createElement("div");
    private readonly quantityPresets = document.createElement("div");
    private readonly status = document.createElement("div");
    private readonly requestButton: HTMLButtonElement;

    private npcId = 0;
    private mode: "buy" | "sell" = "buy";
    private selling: ShopEntry[] = [];
    private buying: ShopEntry[] = [];
    private selected?: ShopEntry;
    private currencyReformed = false;
    private waiting = false;
    private refreshPending = false;

    private constructor() {
        super("empty-div-template");
        this.componentElement.classList.add("npc-shop-dialog");

        this.wallet.className = "npc-shop-wallet";
        this.componentElement.appendChild(this.wallet);

        this.search.type = "search";
        this.search.className = "npc-shop-search";
        this.search.placeholder = "Szukaj przedmiotu...";
        this.search.setAttribute("aria-label", "Szukaj przedmiotu");
        this.search.addEventListener("input", () => {
            this.selected = undefined;
            this.drawList(true);
        });
        const searchFrame = document.createElement("div");
        searchFrame.className = "npc-shop-search-frame";
        const searchLabel = document.createElement("label");
        searchLabel.textContent = "Szukaj:"; searchLabel.appendChild(this.search);
        searchFrame.appendChild(searchLabel);

        const tabs = document.createElement("div");
        tabs.className = "npc-shop-tabs tabpanel-tabs tabpanel-tabs-top";
        this.buyTab.textContent = "Kup";
        this.sellTab.textContent = "Sprzedaj";
        this.buyTab.type = "button";
        this.sellTab.type = "button";
        this.buyTab.onclick = () => this.changeMode("buy");
        this.sellTab.onclick = () => this.changeMode("sell");
        tabs.append(this.buyTab, this.sellTab);

        const layout = document.createElement("div");
        layout.className = "npc-shop-layout";

        const catalogue = document.createElement("div");
        catalogue.className = "npc-shop-catalogue";
        this.listCount.className = "npc-shop-count";
        this.listBox.className = "npc-shop-list";
        this.listBox.setAttribute("role", "listbox");
        this.listBox.setAttribute("aria-label", "Przedmioty handlarza");
        catalogue.append(searchFrame, tabs, this.listBox, this.listCount);
        this.listBox.classList.add("tabpanel-content");
        layout.appendChild(catalogue);

        const detail = document.createElement("div");
        detail.className = "npc-shop-detail";

        const detailSection = document.createElement("fieldset");
        detailSection.className = "npc-shop-section";
        const caption = document.createElement("legend");
        caption.textContent = "Przedmiot";
        detailSection.append(caption, detail);

        const imageHolder = document.createElement("div");
        imageHolder.className = "npc-shop-icon";
        this.detailIcon.alt = "";
        this.detailIcon.onerror = () => {
            this.detailIcon.style.visibility = "hidden";
        };
        imageHolder.appendChild(this.detailIcon);

        this.detailName.textContent = "Wybierz przedmiot";
        this.detailDescription.className = "npc-shop-description";
        this.detailDescription.textContent = "Wybierz przedmiot, aby zobaczyć szczegóły.";
        this.detailPrice.className = "npc-shop-price";
        this.quoteTotal.className = "npc-shop-quote";
        this.quoteTotal.hidden = true;

        this.quantity.type = "text";
        this.quantity.inputMode = "numeric";
        this.quantity.pattern = "[0-9]*";
        this.quantity.value = "1";
        this.quantity.setAttribute("aria-label", "Ilość");
        this.quantity.addEventListener("input", () => this.updateButtons());
        const quantityRow = this.quantityControls;
        quantityRow.className = "npc-shop-quantity";
        const qtyLabel = document.createElement("span");
        qtyLabel.textContent = "Ilość";
        this.quantityMinus.type = "button";
        this.quantityMinus.textContent = "−";
        this.quantityMinus.setAttribute("aria-label", "Zmniejsz ilość");
        this.quantityMinus.onclick = () => this.changeAmount(-1);
        this.quantityPlus.type = "button";
        this.quantityPlus.textContent = "+";
        this.quantityPlus.setAttribute("aria-label", "Zwiększ ilość");
        this.quantityPlus.onclick = () => this.changeAmount(1);
        quantityRow.append(qtyLabel, this.quantityMinus, this.quantity, this.quantityPlus);
        this.quantityMinus.classList.add("dialogbutton"); this.quantityPlus.classList.add("dialogbutton");
        this.detailIcon.dataset.shopSprite = "true";
        const identity = document.createElement("div"); identity.className = "npc-shop-identity";
        this.detailRarity.className = "npc-shop-rarity";
        identity.append(imageHolder, this.detailName, this.detailRarity);
        const body = document.createElement("div"); body.className = "npc-shop-detail-body";
        body.append(this.detailDescription, this.detailStats);
        const quick = this.quantityPresets; quick.className = "npc-shop-presets";
        for (const value of [1,10,100,"Maks"]) {
            const button = document.createElement("button"); button.type = "button"; button.className = "dialogbutton";
            button.textContent = String(value);
            button.onclick = () => { this.quantity.value = String(value === "Maks" ? this.maximum() : Math.min(this.maximum(), Number(value))); this.updateButtons(); };
            this.presets.push(button); quick.appendChild(button);
        }
        const checkout = document.createElement("div"); checkout.className = "npc-shop-checkout";
        const prices = document.createElement("div"); prices.className = "npc-shop-prices";
        prices.append(this.detailPrice, this.quoteTotal); checkout.appendChild(prices);
        this.tradeRequirement.className = "npc-shop-requirement";
        this.tradeRequirement.hidden = true;
        detail.append(identity, body, this.tradeRequirement, quantityRow, quick, checkout);
        layout.appendChild(detailSection);
        this.componentElement.appendChild(layout);

        this.status.className = "npc-shop-status";
        this.status.setAttribute("role", "status");
        this.status.setAttribute("aria-live", "polite");
        this.componentElement.appendChild(this.status);

        this.requestButton = this.addButton("Kup teraz", () => this.purchase());
        this.requestButton.classList.add("npc-shop-btn-primary", "npc-shop-btn-request");
        const tradeActions = document.createElement("div");
        tradeActions.className = "npc-shop-trade-actions";
        tradeActions.append(this.requestButton);
        checkout.appendChild(tradeActions);
        this.updateButtons();
    }

    public static show(data: ShopEventData): void {
        if (data.window_id && NpcShopDialog.closed.has(String(data.window_id))) return;
        if (NpcShopDialog.active && NpcShopDialog.active.windowId && data.window_id
                && NpcShopDialog.active.windowId !== String(data.window_id) && data.phase !== "open") return;
        if (!NpcShopDialog.active) {
            if (data.phase !== "open") return;
            const dialog = new NpcShopDialog();
            const frame = new FloatingWindow("Sklep · " + String(data.npc_name || ""), dialog, 32, 32);
            frame.setId("npc-shop");
            dialog.setFrame(frame);
            NpcShopDialog.active = dialog;
            dialog.polling = setInterval(() => {
                if (dialog.zone !== marauroa.currentZoneName) { dialog.frame?.close(); return; }
                if (!dialog.waiting && !dialog.refreshPending) dialog.send("refresh");
            }, 2000);
            dialog.animation = setInterval(() => dialog.animate(), 100);
        }
        const dialog = NpcShopDialog.active;
        if (dialog.npcId && data.npc_id !== undefined
                && dialog.npcId !== Number(data.npc_id)
                && data.phase !== "open") return;
        if (data.request_id && Number(data.request_id) < dialog.lastRequest) return;
        dialog.apply(data);
    }

    public override onParentClose(): void {
        this.send("close");
        if (this.polling !== undefined) clearInterval(this.polling);
        if (this.animation !== undefined) clearInterval(this.animation);
        NpcShopDialog.closed.add(this.windowId);
        if (NpcShopDialog.closed.size > 64) NpcShopDialog.closed.delete(NpcShopDialog.closed.values().next().value!);
        NpcShopDialog.active = undefined;
    }

    private apply(data: ShopEventData): void {
        const previousName = this.selected?.key;
        this.windowId = String(data.window_id || "");
        this.money = Number(data.owned_money_value || 0);
        this.priceFactor = Number(data.buying_penalty || 1);
        this.purchaseRequirement = String(data.sell_requirement || "");
        this.saleRequirement = String(data.buy_requirement || "");
        this.npcId = Number(data.npc_id || 0);
        this.frame?.setTitle("Sklep · " + String(data.npc_name || ""));
        this.wallet.textContent = "Posiadasz: " + String(data.owned_money_text || "0");
        this.selling = this.entries(data, "sell");
        this.buying = this.entries(data, "buy");
        this.currencyReformed = Number(data.currency_reformed || 0) === 1;
        this.waiting = false;
        this.refreshPending = false;
        if (data.phase === "open" && ((this.mode === "buy" && !this.selling.length && this.buying.length)
                || (this.mode === "sell" && !this.buying.length && this.selling.length))) {
            this.mode = this.mode === "buy" ? "sell" : "buy";
        }

        const target = previousName;
        this.selected = this.current().find(item => item.key === target);
        this.drawList();
        const message = String(data.message || "");
        const isError = message.startsWith("Nie ")
                || message.startsWith("Nieprawidł")
                || message.startsWith("Oferta wygasła")
                || message.startsWith("Cena uległa")
                || message.includes("nie powiodła")
                || message.startsWith("Podejdź");
        const success = data.phase === "result"
                && (message.startsWith("Zakup udany:")
                        || message.startsWith("Sprzedaż udana:"));
        if (data.phase !== "refresh" || data.message) {
            this.setStatus(message, isError ? "error" : success ? "success" : "neutral");
        }
        this.updateButtons();
    }

    private entries(data: ShopEventData, prefix: string): ShopEntry[] {
        const names = this.asList(data[prefix + "_names"]);
        const prices = this.asList(data[prefix + "_prices"]);
        const classes = this.asList(data[prefix + "_classes"]);
        const subclasses = this.asList(data[prefix + "_subclasses"]);
        const descriptions = this.asList(data[prefix + "_descriptions"]);
        const rawPrices = this.asList(data[prefix + "_price_values"]);
        return names.map((name, index) => ({
            key: this.asList(data[prefix + "_keys"])[index] || name,
            limit: Number(this.asList(data[prefix + "_quantities"])[index] || 1),
            snapshot: this.snapshot(this.asList(data[prefix + "_items"])[index]),
            name,
            price: prices[index] || "",
            itemClass: classes[index] || "",
            subclass: subclasses[index] || "",
            stackable: this.asList(data[prefix + "_stackable"])[index] === "1",
            description: descriptions[index] || "",
            priceValue: Number(rawPrices[index] || 0)
        }));
    }

    private snapshot(raw?: string): Item | undefined {
        if (!raw) return;
        try { const parsed = JSON.parse(raw); const item = new Item(); Object.assign(item, parsed); return item; }
        catch { return; }
    }

    private maximum(): number {
        if (!this.selected || this.requirement()) return 0;
        const unit = this.selected.priceValue * (this.mode === "buy" ? this.priceFactor : 1);
        const budget = this.mode === "buy" ? Math.min(2147483647, this.money) : 2147483647;
        return Math.max(0, Math.min(1000, this.selected.limit,
                unit > 0 ? Math.ceil((budget + 1) / unit) - 1 : 1000));
    }

    private total(count: number): number {
        return this.selected ? Math.floor(this.selected.priceValue * count * (this.mode === "buy" ? this.priceFactor : 1)) : 0;
    }

    private animate(): void {
        this.componentElement.querySelectorAll<HTMLImageElement>("img[data-shop-sprite]").forEach(img => {
            const frames = Math.max(1, Math.floor(img.naturalWidth / 32));
            img.style.objectPosition = `${-(Math.floor(Date.now() / 100) % frames) * 32}px 0px`;
        });
    }

    private asList(value: unknown): string[] {
        if (Array.isArray(value)) return value.map(String);
        if (typeof value !== "string" || value.length < 2) return [];
        return value.substring(1, value.length - 1).split(/\t/);
    }

    private current(): ShopEntry[] {
        return this.mode === "buy" ? this.selling : this.buying;
    }

    private changeMode(mode: "buy" | "sell"): void {
        if (this.waiting || this.mode === mode) return;
        this.mode = mode;
        this.selected = undefined;
        this.quantity.value = "1";
        this.drawList(true);
    }

    private drawList(resetScroll = false): void {
        this.buyTab.classList.toggle("active", this.mode === "buy");
        this.sellTab.classList.toggle("active", this.mode === "sell");
        this.buyTab.setAttribute("aria-pressed", String(this.mode === "buy"));
        this.sellTab.setAttribute("aria-pressed", String(this.mode === "sell"));
        const filter = this.search.value.trim().toLocaleLowerCase();
        const entries = this.current().filter(item =>
                item.name.toLocaleLowerCase().includes(filter));
        if (this.selected && !entries.includes(this.selected)) {
            this.selected = undefined;
        }
        this.listCount.textContent = filter
                ? "Widoczne: " + entries.length + " z " + this.current().length
                : "Przedmioty: " + entries.length;

        const previousScroll = resetScroll ? 0 : this.listBox.scrollTop;
        this.listBox.replaceChildren();
        if (!entries.length) {
            const empty = document.createElement("div");
            empty.className = "npc-shop-empty";
            empty.textContent = filter ? "Nie znaleziono przedmiotów."
                    : "Handlarz nie ma teraz żadnych ofert.";
            this.listBox.appendChild(empty);
        }
        for (const entry of entries) {
            const row = document.createElement("button");
            row.type = "button";
            row.className = "npc-shop-row";
            row.setAttribute("role", "option");
            row.setAttribute("aria-selected", String(entry === this.selected));
            row.title = entry.name + ", " + entry.price;
            row.disabled = this.waiting;
            if (entry === this.selected) row.classList.add("selected");
            if (entry.limit === 0) {
                row.classList.add("is-unavailable");
                row.title += " · Brak przedmiotu do sprzedaży";
            }

            const iconSlot = document.createElement("span");
            iconSlot.className = "npc-shop-row-icon";
            if (entry.itemClass && entry.subclass) {
                const icon = document.createElement("img");
                icon.src = Paths.sprites + "/items/" + entry.itemClass
                        + "/" + entry.subclass + ".png";
                icon.alt = ""; icon.dataset.shopSprite = "true";
                icon.onerror = () => { icon.style.visibility = "hidden"; };
                iconSlot.appendChild(icon);
            }
            const description = document.createElement("span");
            description.className = "npc-shop-row-text";
            const name = document.createElement("strong");
            const rarity = entry.snapshot?.getRarity();
            const upgrade = entry.snapshot?.["improve"];
            name.textContent = entry.name + (rarity ? " · " + rarity.polishDisplayName : "") + (Number(upgrade) > 0 ? " +" + upgrade : "");
            if (rarity && rarity.id !== "common") name.style.color = rarity.colorHex;
            const price = document.createElement("small");
            price.textContent = entry.price;
            description.append(name, price);
            row.append(iconSlot, description);
            row.onclick = () => {
                if (this.waiting) return;
                if (this.selected !== entry) {
                    this.quantity.value = "1";
                }
                this.selected = entry;
                this.drawList();
            };
            this.listBox.appendChild(row);
        }
        this.listBox.scrollTop = previousScroll;
        this.drawDetails();
    }

    private drawDetails(): void {
        const entry = this.selected;
        this.detailName.textContent = entry?.name || "Wybierz przedmiot";
        const titleRarity = entry?.snapshot?.getRarity();
        this.detailName.style.color = titleRarity && titleRarity.id !== "common" ? titleRarity.colorHex : "";
        this.detailRarity.textContent = titleRarity?.polishDisplayName || "";
        this.detailRarity.style.color = titleRarity && titleRarity.id !== "common" ? titleRarity.colorHex : "";
        this.detailStats.replaceChildren();
        if (entry?.snapshot) {
            const structured = buildStructuredItemTooltip(entry.snapshot, singletons.getConfigManager().getBoolean("item-tooltip.comparison"));
            if (structured.upgradeText) {
                const header = document.createElement("div");
                header.textContent = structured.upgradeText;
                this.detailStats.appendChild(header);
            }
            if (structured.comparisonName) {
                const compare = document.createElement("div"); compare.className = "item-rarity-tooltip__comparison";
                compare.textContent = "Porównanie z: " + structured.comparisonName; this.detailStats.appendChild(compare);
            }
            for (const line of structured.lines) appendStructuredTooltipLine(this.detailStats, line);
        }
        this.detailDescription.textContent = !entry
                ? "Wybierz przedmiot, aby zobaczyć szczegóły."
                : entry.description || "Brak dodatkowego opisu.";
        this.detailPrice.textContent = entry
                ? (entry.stackable ? "Cena za sztukę: " : "Cena: ") + entry.price
                : "";
        if (entry?.itemClass && entry.subclass) {
            this.detailIcon.src = Paths.sprites + "/items/"
                    + entry.itemClass + "/" + entry.subclass + ".png";
            this.detailIcon.style.visibility = "visible";
        } else {
            this.detailIcon.removeAttribute("src");
            this.detailIcon.style.visibility = "hidden";
        }
        this.quantity.max = String(this.maximum());
        if (entry && !entry.stackable) this.quantity.value = "1";
        this.updateButtons();
    }

    private setStatus(message: string, state: "neutral" | "offer" | "success" | "error"): void {
        this.status.textContent = message;
        this.status.hidden = !message;
        this.status.dataset.state = state;
    }

    private static noun(count: number, one: string,
            few: string, many: string): string {
        if (count === 1) return one;
        const ending = count % 10;
        const tens = count % 100;
        return ending >= 2 && ending <= 4 && !(tens >= 12 && tens <= 14)
                ? few : many;
    }

    private formatMoney(amount: number): string {
        if (!this.currencyReformed) {
            return amount + " " + NpcShopDialog.noun(
                    amount, "moneta", "monety", "monet");
        }
        const parts: string[] = [];
        const gold = Math.floor(amount / 10000);
        const silver = Math.floor(amount % 10000 / 100);
        const copper = amount % 100;
        if (gold) parts.push(gold + " "
                + NpcShopDialog.noun(gold, "dukat", "dukaty", "dukatów"));
        if (silver) parts.push(silver + " "
                + NpcShopDialog.noun(silver, "talar", "talary", "talarów"));
        if (copper || !parts.length) parts.push(copper + " "
                + NpcShopDialog.noun(copper, "miedziak", "miedziaki", "miedziaków"));
        return parts.length > 1
                ? parts.slice(0, -1).join(", ") + " i " + parts[parts.length - 1]
                : parts[0];
    }

    private requirement(): string {
        return this.mode === "buy" ? this.purchaseRequirement : this.saleRequirement;
    }

    private updateButtons(): void {
        this.tradeRequirement.textContent = this.requirement();
        this.tradeRequirement.hidden = !this.requirement();
        const amount = Number(this.quantity.value);
        const max = this.maximum();
        const total = this.total(amount);
        const valid = !!this.selected && Number.isInteger(amount)
                && amount >= 1 && amount <= max
                && (!this.selected.priceValue || total <= 2147483647);
        this.requestButton.textContent = this.mode === "buy"
                ? "Kup teraz" : "Sprzedaj teraz";
        this.requestButton.disabled = !valid || this.waiting;
        this.buyTab.disabled = !this.selling.length || this.waiting;
        this.sellTab.disabled = !this.buying.length || this.waiting;
        this.search.disabled = this.waiting;
        this.quantity.disabled = !this.selected || this.waiting || max < 1;
        this.quantityMinus.disabled = !this.selected || this.waiting || amount <= 1;
        this.quantityPlus.disabled = !this.selected || this.waiting || amount >= max;
        this.listBox.setAttribute("aria-busy", String(this.waiting));
        this.listBox.classList.toggle("is-locked", this.waiting);
        for (const preset of this.presets) preset.disabled = this.waiting || !this.selected?.stackable || this.maximum() < 1;
        this.quantityControls.hidden = !this.selected?.stackable;
        this.quantityPresets.hidden = !this.selected?.stackable;
        this.quoteTotal.hidden = !this.selected?.stackable || amount <= 1;
        this.quoteTotal.textContent = this.selected ? "Łącznie: " + this.formatMoney(total) : "";
    }

    private changeAmount(delta: number): void {
        if (!this.selected || this.waiting) return;
        const current = Number(this.quantity.value);
        const maximum = this.maximum();
        const safe = Number.isInteger(current) ? current : 1;
        this.quantity.value = String(Math.max(1, Math.min(maximum, safe + delta)));
        this.updateButtons();
    }

    private purchase(): void {
        if (!this.selected || this.waiting) return;
        const input = Number(this.quantity.value);
        const max = this.maximum();
        if (!Number.isInteger(input) || input < 1 || input > max
                || (this.selected.priceValue > 0
                        && this.total(input) > 2147483647)) {
            this.setStatus("Wybierz poprawną ilość.", "error");
            return;
        }
        this.waiting = true;
        this.updateButtons();
        this.setStatus("Oczekiwanie na odpowiedź handlarza...", "neutral");
        this.send("purchase", this.mode, this.selected.name, input);
    }

    private send(command: string, mode?: string, item?: string,
            quantity?: number): void {
        if (!this.npcId) return;
        if (command === "refresh") this.refreshPending = true;
        const action: {[key: string]: any} = {
            type: "npc_shop_action",
            command,
            npc_id: String(this.npcId)
        };
        action.window_id = this.windowId;
        action.request_id = String(++this.sequence); this.lastRequest = this.sequence;
        if (mode) action.mode = mode;
        if (mode === "sell" && this.selected) action.selection = this.selected.key;
        if (item) {
            action.item = item;
            action.quantity = String(quantity || 1);
        }
        marauroa.clientFramework.sendAction(action);
    }
}
