import { RPEvent } from "marauroa";
import { NpcShopDialog } from "../ui/dialog/NpcShopDialog";

/** Wyswietla katalog lub wynik rozmowy z handlarzem. */
export class NpcShopEvent extends RPEvent {
    public execute(_entity: any): void {
        NpcShopDialog.show(this as any);
    }
}
