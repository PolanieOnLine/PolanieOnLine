package games.stendhal.server.core.rule.glyph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.text.Collator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.item.upgrade.ItemUpgradeResult.Status;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.Slots;

/** All creation and development recipes displayed by the workbench. */
public final class GlyphWorkbenchCatalog {
    private GlyphWorkbenchCatalog() { }
    private static final List<Entry> ENTRIES = create();
    private static List<Entry> create() {
        List<String> names = new ArrayList<String>(GlyphCraftingService.getRecipes().keySet());
        Collator collator = Collator.getInstance(new Locale("pl")); names.sort(collator::compare);
        List<Entry> entries = new ArrayList<Entry>();
        for (String name : names) for (int level = 1; level <= 3; level++) entries.add(new Entry(name, level));
        return Collections.unmodifiableList(entries);
    }
    public static List<Entry> entries() { return ENTRIES; }
    public static Entry find(String name, int level) {
        for (Entry entry : ENTRIES) if (entry.level == level && entry.name.equals(name)) return entry;
        return null;
    }
    public static Entry byId(String id) {
        for (Entry entry : ENTRIES) if (entry.id.equals(id)) return entry;
        return null;
    }
    public static final class Entry {
        public final String name;
        public final int level;
        public final String id;
        private Entry(String name, int level) { this.name = name; this.level = level; id = level + ":" + name; }
        public boolean creates() { return level == 1; }
        public Item template() {
            Item item = SingletonRepository.getEntityManager().getItem(name, ItemCreationContext.restore());
            GlyphProgressionService.applyLevel(item, Math.max(1, level - 1)); return item;
        }
        public Item source(Player player) {
            Item fallback = null;
            for (Item item : player.getAllEquipped(name)) {
                if (!accepts(player, item)) continue;
                if (item.getContainer() == player && Slots.GLYPHS.getNames().contains(item.getContainerSlot().getName())) return item;
                if (fallback == null) fallback = item;
            }
            return fallback;
        }
        public boolean accepts(Player player, Item item) {
            return !creates() && item != null && name.equals(item.getName()) && GlyphProgressionService.isUpgradeable(item)
                    && GlyphProgressionService.getLevel(item) == level - 1
                    && (item.getBoundTo() == null || item.getBoundTo().isEmpty() || item.getBoundTo().equals(player.getName()))
                    && player.getAllEquipped(name).stream().anyMatch(carried -> carried == item);
        }
        public int sourceCount(Player player) {
            return (int)player.getAllEquipped(name).stream().filter(item -> accepts(player, item)).count();
        }
        public Map<String, Integer> materials() {
            return creates() ? GlyphCraftingService.getRecipes().get(name)
                    : GlyphUpgradeService.getInstance().getRequiredMaterials(template(), level);
        }
        public Status status(Player player) {
            if (creates()) return GlyphCraftingService.hasResources(player, name) ? Status.READY : Status.MISSING_RESOURCES;
            Item item = source(player);
            return item == null ? Status.MISSING_RESOURCES : GlyphUpgradeService.getInstance().getUpgradeStatus(player, item);
        }
    }
}
