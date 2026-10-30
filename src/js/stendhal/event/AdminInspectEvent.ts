import { RPEvent } from "marauroa";
import { AdminInspectDialog } from "../ui/dialog/AdminInspectDialog";
export class AdminInspectEvent extends RPEvent {
    public execute(_entity:any):void {AdminInspectDialog.show(this as any);}
}
