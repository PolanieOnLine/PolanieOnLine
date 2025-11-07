import { ItemInventoryComponent } from "./ItemInventoryComponent";

declare var marauroa: any;


export class MagicBagComponent extends ItemInventoryComponent {
	private readonly defaultSlotSize = "6 1";
	private slotSize = this.defaultSlotSize;

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

		const sizeFeature = features["magicbag"];
		if (sizeFeature !== undefined && sizeFeature !== null) {
			if (!this.isVisible()) {
				this.setVisible(true);
			}
			const normalizedSize = sizeFeature === "" ? this.defaultSlotSize : String(sizeFeature).trim();
			if (this.slotSize != normalizedSize) {
				this.slotSize = normalizedSize;
				const sizeArray = normalizedSize.split(/\s+/);
				const sizeX = parseInt(sizeArray[0], 10);
				const sizeY = parseInt(sizeArray[1], 10);
				if (!Number.isNaN(sizeX) && !Number.isNaN(sizeY)) {
					this.resize(sizeX, sizeY);
				}
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
