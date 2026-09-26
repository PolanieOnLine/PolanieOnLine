import { marauroa } from "marauroa";
import { Paths } from "../../data/Paths";
import { FloatingWindow } from "../toolkit/FloatingWindow";
import { DialogContentComponent } from "../toolkit/DialogContentComponent";

type ShopEventData = {[key: string]: any};

interface ShopEntry {
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

    private readonly heading = document.createElement("h3");
    private readonly wallet = document.createElement("div");
    private readonly search = document.createElement("input");
    private readonly buyTab = document.createElement("button");
    private readonly sellTab = document.createElement("button");
    private readonly listCount = document.createElement("div");
    private readonly listBox = document.createElement("div");
    private readonly detailIcon = document.createElement("img");
    private readonly detailName = document.createElement("h4");
    private readonly detailPrice = document.createElement("div");
    private readonly detailDescription = document.createElement("p");
    private readonly quantityMinus = document.createElement("button");
    private readonly quantityPlus = document.createElement("button");
    private readonly quoteTotal = document.createElement("div");
    private readonly quantity = document.createElement("input");
    private readonly status = document.createElement("div");
    private readonly requestButton: HTMLButtonElement;
    private readonly refreshButton: HTMLButtonElement;

    private npcId = 0;
    private mode: "buy" | "sell" = "buy";
    private selling: ShopEntry[] = [];
    private buying: ShopEntry[] = [];
    private selected?: ShopEntry;
    private currencyReformed = false;
    private waiting = false;

    private constructor() {
        super("empty-div-template");
        this.componentElement.classList.add("npc-shop-dialog");

        const header = document.createElement("div");
        header.className = "npc-shop-header";
        this.wallet.className = "npc-shop-wallet";
        const shopTitle = document.createElement("div");
        shopTitle.className = "npc-shop-title";
        const shopLabel = document.createElement("span");
        shopLabel.textContent = "SKLEP";
        shopTitle.append(shopLabel, this.heading);
        header.append(shopTitle, this.wallet);
        this.componentElement.appendChild(header);

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
        searchFrame.appendChild(this.search);
        this.componentElement.appendChild(searchFrame);

        const tabs = document.createElement("div");
        tabs.className = "npc-shop-tabs";
        this.buyTab.textContent = "Kup";
        this.sellTab.textContent = "Sprzedaj";
        this.buyTab.type = "button";
        this.sellTab.type = "button";
        this.buyTab.onclick = () => this.changeMode("buy");
        this.sellTab.onclick = () => this.changeMode("sell");
        tabs.append(this.buyTab, this.sellTab);
        this.componentElement.appendChild(tabs);

        const layout = document.createElement("div");
        layout.className = "npc-shop-layout";

        const catalogue = document.createElement("div");
        catalogue.className = "npc-shop-catalogue";
        this.listCount.className = "npc-shop-count";
        this.listBox.className = "npc-shop-list";
        this.listBox.setAttribute("role", "listbox");
        this.listBox.setAttribute("aria-label", "Przedmioty handlarza");
        catalogue.append(this.listCount, this.listBox);
        layout.appendChild(catalogue);

        const detail = document.createElement("div");
        detail.className = "npc-shop-detail";

        const caption = document.createElement("div");
        caption.className = "npc-shop-detail-caption";
        caption.textContent = "Wybrany przedmiot";

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
        const quantityRow = document.createElement("div");
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
        detail.append(caption, imageHolder, this.detailName,
                this.detailDescription, this.detailPrice, this.quoteTotal, quantityRow);
        layout.appendChild(detail);
        this.componentElement.appendChild(layout);

        this.status.className = "npc-shop-status";
        this.status.setAttribute("role", "status");
        this.status.setAttribute("aria-live", "polite");
        this.componentElement.appendChild(this.status);

        this.requestButton = this.addButton("Kup teraz", () => this.purchase());
        this.requestButton.classList.add("npc-shop-btn-primary", "npc-shop-btn-request");
        this.refreshButton = this.addButton("Odśwież", () => this.send("refresh"));
        this.refreshButton.classList.add("npc-shop-btn-secondary");
        const tradeActions = document.createElement("div");
        tradeActions.className = "npc-shop-trade-actions";
        tradeActions.append(this.requestButton);
        detail.appendChild(tradeActions);
        this.updateButtons();
    }

    public static show(data: ShopEventData): void {
        if (!NpcShopDialog.active) {
            if (data.phase !== "open") return;
            const dialog = new NpcShopDialog();
            const frame = new FloatingWindow("Sklep", dialog, 32, 32);
            frame.setId("npc-shop");
            dialog.setFrame(frame);
            NpcShopDialog.active = dialog;
        }
        const dialog = NpcShopDialog.active;
        if (dialog.npcId && data.npc_id !== undefined
                && dialog.npcId !== Number(data.npc_id)
                && data.phase !== "open") return;
        dialog.apply(data);
    }

    public override onParentClose(): void {
        this.send("close");
        NpcShopDialog.active = undefined;
    }

    private apply(data: ShopEventData): void {
        const previousName = this.selected?.name;
        this.npcId = Number(data.npc_id || 0);
        this.heading.textContent = String(data.npc_name || "Sklep");
        this.wallet.textContent = "Posiadasz: " + String(data.owned_money_text || "0");
        this.selling = this.entries(data, "sell");
        this.buying = this.entries(data, "buy");
        this.currencyReformed = Number(data.currency_reformed || 0) === 1;
        this.waiting = false;
        if ((this.mode === "buy" && !this.selling.length && this.buying.length)
                || (this.mode === "sell" && !this.buying.length && this.selling.length)) {
            this.mode = this.mode === "buy" ? "sell" : "buy";
        }

        const target = previousName;
        this.selected = this.current().find(item => item.name === target);
        this.drawList();
        const message = String(data.message
                || "Wybierz przedmiot, ustaw ilość i kup lub sprzedaj.");
        const isError = message.startsWith("Nie ")
                || message.startsWith("Nieprawidł")
                || message.startsWith("Oferta wygasła")
                || message.startsWith("Cena uległa")
                || message.includes("nie powiodła")
                || message.startsWith("Podejdź");
        const success = data.phase === "result"
                && (message.startsWith("Zakup udany:")
                        || message.startsWith("Sprzedaż udana:"));
        this.setStatus(message, isError ? "error" : success ? "success" : "neutral");
        this.updateButtons();
    }

    private entries(data: ShopEventData, prefix: string): ShopEntry[] {
        const names = this.asList(data[prefix + "_names"]);
        const prices = this.asList(data[prefix + "_prices"]);
        const classes = this.asList(data[prefix + "_classes"]);
        const subclasses = this.asList(data[prefix + "_subclasses"]);
        const stackable = this.asList(data.sell_stackable);
        const descriptions = this.asList(data[prefix + "_descriptions"]);
        const rawPrices = this.asList(data[prefix + "_price_values"]);
        return names.map((name, index) => ({
            name,
            price: prices[index] || "",
            itemClass: classes[index] || "",
            subclass: subclasses[index] || "",
            stackable: prefix === "buy" || stackable[index] === "1",
            description: descriptions[index] || "",
            priceValue: Number(rawPrices[index] || 0)
        }));
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

            const iconSlot = document.createElement("span");
            iconSlot.className = "npc-shop-row-icon";
            if (entry.itemClass && entry.subclass) {
                const icon = document.createElement("img");
                icon.src = Paths.sprites + "/items/" + entry.itemClass
                        + "/" + entry.subclass + ".png";
                icon.alt = "";
                icon.onerror = () => { icon.style.visibility = "hidden"; };
                iconSlot.appendChild(icon);
            }
            const description = document.createElement("span");
            description.className = "npc-shop-row-text";
            const name = document.createElement("strong");
            name.textContent = entry.name;
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
        this.detailDescription.textContent = !entry
                ? "Wybierz przedmiot, aby zobaczyć szczegóły."
                : entry.description || "Brak dodatkowego opisu.";
        this.detailPrice.textContent = entry
                ? (this.mode === "buy" ? "Cena: " : "Cena bazowa: ") + entry.price
                : "";
        if (entry?.itemClass && entry.subclass) {
            this.detailIcon.src = Paths.sprites + "/items/"
                    + entry.itemClass + "/" + entry.subclass + ".png";
            this.detailIcon.style.visibility = "visible";
        } else {
            this.detailIcon.removeAttribute("src");
            this.detailIcon.style.visibility = "hidden";
        }
        this.quantity.max = entry && !entry.stackable ? "1" : "1000";
        if (entry && !entry.stackable) this.quantity.value = "1";
        this.updateButtons();
    }

    private setStatus(message: string, state: "neutral" | "offer" | "success" | "error"): void {
        this.status.textContent = message;
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

    private updateButtons(): void {
        const amount = Number(this.quantity.value);
        const max = this.selected?.stackable ? 1000 : 1;
        const total = this.selected ? this.selected.priceValue * amount : 0;
        const valid = !!this.selected && Number.isInteger(amount)
                && amount >= 1 && amount <= max
                && (!this.selected.priceValue || total <= 2147483647);
        this.requestButton.textContent = this.mode === "buy"
                ? "Kup teraz" : "Sprzedaj teraz";
        this.requestButton.disabled = !valid || this.waiting;
        this.refreshButton.disabled = this.waiting;
        this.buyTab.disabled = !this.selling.length || this.waiting;
        this.sellTab.disabled = !this.buying.length || this.waiting;
        this.search.disabled = this.waiting;
        this.quantity.disabled = !this.selected || this.waiting;
        this.quantityMinus.disabled = !this.selected || this.waiting || amount <= 1;
        this.quantityPlus.disabled = !this.selected || this.waiting || amount >= max;
        this.listBox.setAttribute("aria-busy", String(this.waiting));
        this.listBox.classList.toggle("is-locked", this.waiting);
        if (valid && amount > 1 && this.selected && this.selected.priceValue > 0) {
            this.quoteTotal.textContent = "Razem orientacyjnie: "
                    + this.formatMoney(total);
            this.quoteTotal.hidden = false;
        } else {
            this.quoteTotal.textContent = "";
            this.quoteTotal.hidden = true;
        }
    }

    private changeAmount(delta: number): void {
        if (!this.selected || this.waiting) return;
        const current = Number(this.quantity.value);
        const maximum = this.selected.stackable ? 1000 : 1;
        const safe = Number.isInteger(current) ? current : 1;
        this.quantity.value = String(Math.max(1, Math.min(maximum, safe + delta)));
        this.updateButtons();
    }

    private purchase(): void {
        if (!this.selected || this.waiting) return;
        const input = Number(this.quantity.value);
        const max = this.selected.stackable ? 1000 : 1;
        if (!Number.isInteger(input) || input < 1 || input > max
                || (this.selected.priceValue > 0
                        && this.selected.priceValue * input > 2147483647)) {
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
        const action: {[key: string]: any} = {
            type: "npc_shop_action",
            command,
            npc_id: String(this.npcId)
        };
        if (mode) action.mode = mode;
        if (item) {
            action.item = item;
            action.quantity = String(quantity || 1);
        }
        marauroa.clientFramework.sendAction(action);
    }
}