/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.events;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;
import games.stendhal.common.Translation;
import games.stendhal.server.core.rule.glyph.GlyphProgressionService;
import games.stendhal.server.core.rule.glyph.GlyphUpgradePresentation;
import games.stendhal.server.core.rule.glyph.GlyphUpgradeService;
import games.stendhal.server.core.rule.glyph.GlyphWorkbenchCatalog;
import games.stendhal.server.core.rule.glyph.GlyphWorkbenchCatalog.Entry;
import games.stendhal.server.entity.slot.Slots;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.player.Player;

import games.stendhal.common.constants.Events;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradePreview;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradeRequirements;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradeResult;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
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
	public static final String PHASE_REFRESH = "refresh";
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
		add(rpclass, "material_classes", Type.VERY_LONG_STRING);
		add(rpclass, "material_subclasses", Type.VERY_LONG_STRING);
		add(rpclass, "material_values", Type.VERY_LONG_STRING);
		add(rpclass, "owned_material_values", Type.VERY_LONG_STRING);
		add(rpclass, "service", Type.STRING);
        add(rpclass, "selected_recipe", Type.STRING);
        add(rpclass, "operation", Type.STRING);
        add(rpclass, "source_location", Type.STRING);
        for (String key : new String[] {"recipe_ids", "recipe_names", "recipe_levels", "recipe_ready", "recipe_subclasses", "stat_keys", "stat_short_names", "stat_note_keys", "stat_note_values"}) {
            add(rpclass, key, Type.VERY_LONG_STRING);
        }
        add(rpclass, "window_id", Type.STRING);
        add(rpclass, "window_request", Type.STRING);
        add(rpclass, "material_glyph_levels", Type.VERY_LONG_STRING);
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

	/** Refreshes selectable instances without selecting one client-side. */
	public static ItemUpgradeEvent refresh(final int npcId,
			final List<Item> candidates, final ItemUpgradeResult result) {
		return new ItemUpgradeEvent(npcId, candidates, null, result,
				PHASE_REFRESH);
	}

	private ItemUpgradeEvent(final int npcId, final List<Item> candidates,
			final ItemUpgradePreview preview, final ItemUpgradeResult result,
			final String phase) {
		super(Events.ITEM_UPGRADE);
		put("npc_id", npcId);
		put("phase", phase);
        if (PHASE_OPEN.equals(phase)) put("window_id", UUID.randomUUID().toString());
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

	private void putParallelMaps(final Map<String, ?> current,
			final Map<String, ?> upgraded) {
		final List<String> names = new ArrayList<String>();
		final List<String> currentValues = new ArrayList<String>();
		final List<String> upgradedValues = new ArrayList<String>();
		for (final Map.Entry<String, ?> entry : current.entrySet()) {
			names.add(entry.getKey());
			currentValues.add(entry.getValue().toString());
			upgradedValues.add(upgraded.get(entry.getKey()).toString());
		}
		if (!names.isEmpty()) {
			put("stat_names", names);
			put("current_stat_values", currentValues);
			put("upgraded_stat_values", upgradedValues);
		}
	}

	private void putMaterials(final ItemUpgradeRequirements requirements) {
		putMaterials(requirements.getMaterials(), requirements.getOwnedMaterials());
	}

	private void putMaterials(final Map<String, Integer> materials, final Map<String, Integer> ownedMaterials) {
		final List<String> names = new ArrayList<String>();
		final List<String> classes = new ArrayList<String>();
		final List<String> subclasses = new ArrayList<String>();
		final List<String> required = new ArrayList<String>();
		final List<String> owned = new ArrayList<String>();
		for (final Map.Entry<String, Integer> entry
				: materials.entrySet()) {
			names.add(entry.getKey());
			final Item template = SingletonRepository.getEntityManager().getItem(
					entry.getKey(), ItemCreationContext.restore());
			classes.add(template == null ? "" : template.getItemClass());
			subclasses.add(template == null ? "" : template.getItemSubclass());
			required.add(Integer.toString(entry.getValue()));
			owned.add(Integer.toString(
					ownedMaterials.get(entry.getKey())));
		}
		if (!names.isEmpty()) {
			put("material_names", names);
			put("material_classes", classes);
			put("material_subclasses", subclasses);
			put("material_values", required);
			put("owned_material_values", owned);
		}
	}

    /** Shared workbench catalogue and the selected recipe's server-owned preview. */
    public static ItemUpgradeEvent workbench(Player player, int tableId, List<Item> candidates,
            Entry entry, Item source, String phase, ItemUpgradeResult.Status status, String message, String token) {
        ItemUpgradeEvent event = new ItemUpgradeEvent(tableId, candidates, null, null, phase);
        event.put("service", "glyph"); event.put("status", status.name());
        event.put("message", Translation.getText(player.getLanguage(), message));
        if (status != ItemUpgradeResult.Status.NPC_TOO_FAR) event.putGlyphCatalog(player);
        if (entry == null) return event;
        Item display = entry.template();
        event.put("selected_recipe", entry.id); event.put("operation", entry.creates() ? "create" : "upgrade");
        event.put("name", Translation.getText(player.getLanguage(), entry.name));
        event.put("class", display.getItemClass()); event.put("subclass", display.getItemSubclass());
        if (source != null) {
            event.put("selected_path", encodePath(source));
            event.put("source_location", Translation.getText(player.getLanguage(), source.getContainer() == player
                    && Slots.GLYPHS.getNames().contains(source.getContainerSlot().getName()) ? "Założony glif" : "Glif z ekwipunku"));
        }
        event.put("upgrade_level", entry.creates() ? 1 : entry.level - 1);
        event.put("next_upgrade_level", entry.level); event.put("max_upgrade_level", 3);
        event.put("success_percent", 100); event.put("can_upgrade", token == null ? 0 : 1);
        if (token != null) event.put("request_token", token);
        event.putParallelMaps(GlyphUpgradePresentation.effects(player, display, entry.creates() ? 1 : entry.level - 1),
                GlyphUpgradePresentation.effects(player, display, entry.level));
        List<String> statKeys = new ArrayList<String>(GlyphProgressionService.getEffects(display, entry.level).keySet());
        List<String> shortNames = new ArrayList<String>(), noteKeys = new ArrayList<String>(), notes = new ArrayList<String>();
        for (String key : statKeys) {
            shortNames.add(GlyphUpgradePresentation.compactLabel(player, key));
            String note = GlyphUpgradePresentation.note(player, key);
            if (!note.isEmpty()) { noteKeys.add(key); notes.add(note); }
        }
        event.put("stat_keys", statKeys); event.put("stat_short_names", shortNames);
        if (!noteKeys.isEmpty()) { event.put("stat_note_keys", noteKeys); event.put("stat_note_values", notes); }
        GlyphUpgradeService service = GlyphUpgradeService.getInstance();
        int fee = entry.creates() ? 0 : (int)service.getRequiredCoins(display, entry.level);
        event.put("fee", fee); event.put("fee_text", MoneyUtils.formatPrice(fee));
        event.put("owned_money", (int)Math.min(Integer.MAX_VALUE, MoneyUtils.getTotalMoneyInCopper(player)));
        Map<String, Integer> required = new LinkedHashMap<String, Integer>();
        if (!entry.creates()) required.put("fragment glifu", service.getRequiredFragments(display, entry.level));
        required.putAll(entry.materials());
        Map<String, Integer> owned = new LinkedHashMap<String, Integer>();
        for (String name : required.keySet()) owned.put(name, service.getResourceCount(player, name));
        event.putMaterials(required, owned);
        List<String> names = new ArrayList<String>(event.getList("material_names"));
        List<String> glyphLevels = new ArrayList<String>();
        int index = 0;
        for (String name : required.keySet()) {
            Item ingredient = SingletonRepository.getEntityManager().getItem(name, ItemCreationContext.restore());
            if (GlyphProgressionService.isUpgradeable(ingredient)) names.set(index, names.get(index) + " I");
            glyphLevels.add(GlyphProgressionService.isUpgradeable(ingredient) ? "1" : "0");
            index++;
        }
        event.put("material_names", names);
        event.put("material_glyph_levels", glyphLevels);
        if (!entry.creates()) event.prependSourceIngredient(player, entry, display);
        return event;
    }

    private void putGlyphCatalog(Player player) {
        List<String> ids = new ArrayList<String>(), names = new ArrayList<String>(), levels = new ArrayList<String>();
        List<String> ready = new ArrayList<String>(), subclasses = new ArrayList<String>();
        for (Entry entry : GlyphWorkbenchCatalog.entries()) {
            ids.add(entry.id); names.add(Translation.getText(player.getLanguage(), entry.name));
            levels.add(Integer.toString(entry.level));
            ready.add(entry.status(player) == ItemUpgradeResult.Status.READY ? "1" : "0");
            subclasses.add(entry.template().getItemSubclass());
        }
        put("recipe_ids", ids); put("recipe_names", names); put("recipe_levels", levels);
        put("recipe_ready", ready); put("recipe_subclasses", subclasses);
    }

    private void prependSourceIngredient(Player player, Entry entry, Item template) {
        String[] keys = {"material_names", "material_classes", "material_subclasses", "material_values", "owned_material_values", "material_glyph_levels"};
        String[] values = {Translation.getText(player.getLanguage(), entry.name) + " " + (entry.level == 2 ? "I" : "II"),
                template.getItemClass(), template.getItemSubclass(), "1", Integer.toString(entry.sourceCount(player)), Integer.toString(entry.level - 1)};
        for (int index = 0; index < keys.length; index++) {
            List<String> list = new ArrayList<String>(getList(keys[index])); list.add(0, values[index]); put(keys[index], list);
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
