import { RPEvent } from "marauroa";
import { ProductionDialog } from "../ui/dialog/ProductionDialog";

export class ProductionEvent extends RPEvent {
    public execute(_entity: any): void { ProductionDialog.show(this as any); }
}
