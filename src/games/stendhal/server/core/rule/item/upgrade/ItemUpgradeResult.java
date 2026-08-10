/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.item.upgrade;

/** Result of a server-side item-upgrade request. */
public final class ItemUpgradeResult {
	public enum Status {
		READY,
		SUCCESS,
		FAILURE,
		INVALID_REQUEST,
		INVALID_ITEM,
		STALE_PREVIEW,
		NOT_UPGRADEABLE,
		MAX_LEVEL,
		MISSING_CONFIGURATION,
		NOT_ENOUGH_MONEY,
		MISSING_RESOURCES,
		TRANSACTION_FAILED,
		SELECT_ITEM,
		NO_UPGRADEABLE_ITEMS,
		NPC_NOT_ATTENDING,
		NPC_BUSY,
		NPC_TOO_FAR
	}

	private final Status status;
	private final String message;

	public ItemUpgradeResult(final Status status, final String message) {
		this.status = status;
		this.message = message;
	}

	public Status getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public boolean isSuccess() {
		return status == Status.SUCCESS;
	}
}
