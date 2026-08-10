/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.common.constants.Events;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradePreview;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradeRequirements;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradeResult;
import games.stendhal.server.entity.item.Item;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/** Wire DTO shared by the desktop and web item-upgrade windows. */
public final class ItemUpgradeEvent extends RPEvent {
	public static final String PHASE_OPEN = "open";
	public static final String PHASE_PREVIEW = "preview";
	public static final String PHASE_RESULT = "result";
	private static final String PATH_SEPARATOR = "/";

	public static void generateRPClass() {
		final RPClass rpclass = new RPClass(Events.ITEM_UPGRADE);
		add(rpclass, "phase", Type.STRING);
		add(rpclass, "status", Type.STRING);
		add(rpclass, "message", Type.LONG_STRING);
		add(rpclass, "npc_id", Type.INT);
		add(rpclass, "request_token", Type.STRING);
		add(rpclass, "candidate_paths", Type.VERY_LONG_STRING);
		add(rpclass, "selected_path", Type.LONG_STRING);
		add(rpclass, "name", Type.STRING);
		add(rpclass, "class", Type.STRING);
		add(rpclass, "subclass", Type.STRING);
		add(rpclass, "rarity_id", Type.STRING);
		add(rpclass, "upgrade_level", Type.INT);
		add(rpclass, "next_upgrade_level", Type.INT);
		add(rpclass, "max_upgrade_level", Type.INT);
		add(rpclass, "success_percent", Type.INT);
		add(rpclass, "fee", Type.INT);
		add(rpclass, "fee_text", Type.STRING);
		add(rpclass, "owned_money", Type.INT);
		add(rpclass, "can_upgrade", Type.BYTE);
		add(rpclass, "stat_names", Type.VERY_LONG_STRING);
		add(rpclass, "current_stat_values", Type.VERY_LONG_STRING);
		add(rpclass, "upgraded_stat_values", Type.VERY_LONG_STRING);
		add(rpclass, "material_names", Type.VERY_LONG_STRING);
		add(rpclass, "material_values", Type.VERY_LONG_STRING);
		add(rpclass, "owned_material_values", Type.VERY_LONG_STRING);
	}

	private static void add(final RPClass rpclass, final String name,
			final Type type) {
		rpclass.addAttribute(name, type, Definition.PRIVATE);
	}

	public ItemUpgradeEvent(final int npcId, final List<Item> candidates,
			final ItemUpgradePreview preview,
			final ItemUpgradeResult result) {
		this(npcId, candidates, preview, result,
				result == null ? PHASE_PREVIEW : PHASE_RESULT);
	}

	/** Creates the only event allowed to open a new client window. */
	public static ItemUpgradeEvent open(final int npcId,
			final List<Item> candidates, final ItemUpgradePreview preview) {
		return new ItemUpgradeEvent(npcId, candidates, preview, null,
				PHASE_OPEN);
	}

	private ItemUpgradeEvent(final int npcId, final List<Item> candidates,
			final ItemUpgradePreview preview, final ItemUpgradeResult result,
			final String phase) {
		super(Events.ITEM_UPGRADE);
		put("npc_id", npcId);
		put("phase", phase);
		putCandidates(candidates);

		if (preview != null) {
			putPreview(preview);
		}
		final ItemUpgradeResult.Status status = result != null
				? result.getStatus() : preview != null
						? preview.getBlockingStatus()
						: candidates != null && !candidates.isEmpty()
								? ItemUpgradeResult.Status.SELECT_ITEM
								: ItemUpgradeResult.Status.NO_UPGRADEABLE_ITEMS;
		put("status", status.name());
		put("message", result == null ? "" : result.getMessage());
	}

	private void putCandidates(final List<Item> candidates) {
		final List<String> paths = new ArrayList<String>();
		if (candidates != null) {
			for (final Item item : candidates) {
				paths.add(encodePath(item));
			}
		}
		if (!paths.isEmpty()) {
			put("candidate_paths", paths);
		}
	}

	private void putPreview(final ItemUpgradePreview preview) {
		final Item item = preview.getItem();
		put("selected_path", encodePath(item));
		put("name", preview.getDisplayName());
		put("class", item.getItemClass());
		put("subclass", item.getItemSubclass());
		put("rarity_id", preview.getRarity().getId());
		put("upgrade_level", preview.getCurrentLevel());
		put("next_upgrade_level", preview.getNextLevel());
		put("max_upgrade_level", preview.getMaximumLevel());
		put("success_percent", preview.getSuccessPercent());
		put("can_upgrade", preview.isUpgradeAllowed() ? 1 : 0);
		if (preview.getRequestToken() != null) {
			put("request_token", preview.getRequestToken());
		}
		putParallelMaps(preview.getCurrentStats().getValues(),
				preview.getUpgradedStats().getValues());

		final ItemUpgradeRequirements requirements = preview.getRequirements();
		put("fee", requirements.getFee());
		put("fee_text", requirements.getFormattedFee());
		put("owned_money", requirements.getOwnedMoney());
		putMaterials(requirements);
	}

	private void putParallelMaps(final Map<String, Integer> current,
			final Map<String, Integer> upgraded) {
		final List<String> names = new ArrayList<String>();
		final List<String> currentValues = new ArrayList<String>();
		final List<String> upgradedValues = new ArrayList<String>();
		for (final Map.Entry<String, Integer> entry : current.entrySet()) {
			names.add(entry.getKey());
			currentValues.add(Integer.toString(entry.getValue()));
			upgradedValues.add(Integer.toString(upgraded.get(entry.getKey())));
		}
		if (!names.isEmpty()) {
			put("stat_names", names);
			put("current_stat_values", currentValues);
			put("upgraded_stat_values", upgradedValues);
		}
	}

	private void putMaterials(final ItemUpgradeRequirements requirements) {
		final List<String> names = new ArrayList<String>();
		final List<String> required = new ArrayList<String>();
		final List<String> owned = new ArrayList<String>();
		for (final Map.Entry<String, Integer> entry
				: requirements.getMaterials().entrySet()) {
			names.add(entry.getKey());
			required.add(Integer.toString(entry.getValue()));
			owned.add(Integer.toString(
					requirements.getOwnedMaterials().get(entry.getKey())));
		}
		if (!names.isEmpty()) {
			put("material_names", names);
			put("material_values", required);
			put("owned_material_values", owned);
		}
	}

	public static String encodePath(final RPObject object) {
		final LinkedList<String> path = new LinkedList<String>();
		RPObject current = object;
		while (current != null) {
			path.addFirst(Integer.toString(current.getID().getObjectID()));
			final RPSlot slot = current.getContainerSlot();
			if (slot != null) {
				path.addFirst(slot.getName());
			}
			current = current.getContainer();
		}
		return String.join(PATH_SEPARATOR, path);
	}

	public static List<String> decodePath(final String encoded) {
		final List<String> path = new ArrayList<String>();
		if (encoded == null || encoded.length() == 0) {
			return path;
		}
		for (final String element : encoded.split(PATH_SEPARATOR)) {
			path.add(element);
		}
		return path;
	}

}
