import { marauroa, RPEvent } from "marauroa";
import { AffixRerollDialog } from "../ui/dialog/AffixRerollDialog";

/** Private workbench replies belong only to the currently controlled player. */
export class AffixRerollEvent extends RPEvent {
	public execute(entity: any): void {
		if (entity === marauroa.me && entity) AffixRerollDialog.show(this as any);
	}
}
