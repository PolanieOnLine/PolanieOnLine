import { ItemInventoryComponent } from "./ItemInventoryComponent";

declare var marauroa: any;


export class MagicBagComponent extends ItemInventoryComponent {

	private slotSize = "6 1";

	constructor(object: any, slot: string, sizeX: number, sizeY: number, quickPickup: boolean, defaultImage?: string) {
		super(object, slot, sizeX, sizeY, quickPickup, defaultImage);
	}

	override update() {
		let features = null;
		if (marauroa.me != null) {
			features = marauroa.me["features"];
		}
		if (features == null) {
			return;
		}

		let size = features["magicbag"];

		if (size) {
			if (!this.isVisible()) {
				this.setVisible(true);
			}
			if (this.slotSize != size) {
				this.slotSize = size;
				let sizeArray = size.split(" ");
				let sizeX = parseInt(sizeArray[0], 10);
				let sizeY = parseInt(sizeArray[1], 10);
				this.resize(sizeX, sizeY);
			}
		} else if (this.isVisible()) {
			this.setVisible(false);
		}

		super.update();
	}


	private resize(sizeX: number, sizeY: number) {
		super.setSize(sizeX, sizeY);
		this.itemContainerImplementation.init(sizeX * sizeY);
	}
}
