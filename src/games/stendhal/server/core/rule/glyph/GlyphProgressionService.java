package games.stendhal.server.core.rule.glyph;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import games.stendhal.common.constants.Nature;
import games.stendhal.server.entity.item.Glyph;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.ItemTooltipService;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.Slots;
import marauroa.common.game.RPObject;

/** Exact target values, shared by creation, restore and future paid upgrades. */
public final class GlyphProgressionService {
    public static final String LEVEL_ATTRIBUTE = "glyph_level";
    public static final String REGEN_CAP_ATTRIBUTE = "out_of_combat_regen_cap";
    private static final Logger LOGGER = Logger.getLogger(GlyphProgressionService.class);
    private static final Nature[] ELEMENTS = {Nature.EARTH, Nature.WATER, Nature.FIRE, Nature.ICE, Nature.DARK, Nature.LIGHT};
    private static final Map<String, List<Effect>> DEFINITIONS = definitions();

    private GlyphProgressionService() { }

    private static Map<String, List<Effect>> definitions() {
        Map<String, List<Effect>> values = new LinkedHashMap<String, List<Effect>>();
        add(values, "glif daru Mokoszy", effect("health",100,250,500));
        add(values, "glif siły", effect("atk",2,3,5));
        add(values, "glif Peruna", effect("atk_additional_bonus",4,7,10));
        add(values, "glif czaszy", effect("critical_additional_bonus",10,17.5,25));
        add(values, "glif tarczy", effect("def",8,14,20));
        add(values, "glif Swaroga", effect("def_additional_bonus",8,14,20));
        add(values, "glif Tytana", effect("atk",2,3,5),effect("def",2,3,5),effect("health",80,140,200));
        add(values, "glif Strzyboga", effect(GlyphEffectService.ATTACK_RATE_REDUCTION_ATTRIBUTE,0.4,0.7,1));
        addElements(values, "glif Jarowita",6,10,15);
        add(values, "glif kryzysu", effect("critical_chance",6,10,15));
        add(values, "glif krwi", effect("lifesteal_increase",8,14,20));
        add(values, "glif świętego dębu", effect("critical_bonus_reduction",10,17.5,25));
        add(values, "glif żywej wody", effect("out_of_combat_regen",0.5,0.75,1),effect(REGEN_CAP_ATTRIBUTE,8,14,20));
        add(values, "glif Rodzanic", effect("consumable_healing_bonus",8,14,20));
        add(values, "glif wichru", effect(GlyphEffectService.ATTACK_RATE_REDUCTION_ATTRIBUTE,0.4,0.7,1),effect("physical_dodge_chance",2,3.5,5));
        add(values, "glif wytrwałości", effect(GlyphEffectService.ATTACK_RATE_REDUCTION_ATTRIBUTE,0.4,0.7,1),effect("low_health_damage_reduction",4,7,10));
        add(values, "glif ogniska", new Effect(Nature.FIRE,14,24,35));
        add(values, "glif korzeni", new Effect(Nature.EARTH,10,17.5,25),new Effect(Nature.WATER,10,17.5,25));
        add(values, "glif zmory", effect("nightmare_chance",4,7,10));
        add(values, "glif Świętowita", effect("atk",1,2,3),effect("def",2,3,5),effect("critical_chance",2,3.5,5));
        addElements(values, "glif przodków",2,3.5,5, effect("health",100,175,250),effect("def",3,5,8));
        return Collections.unmodifiableMap(values);
    }

    private static Effect effect(String key, double first, double second, double third) {
        return new Effect(key,first,second,third);
    }
    private static void add(Map<String,List<Effect>> values, String name, Effect... effects) {
        values.put(name,Collections.unmodifiableList(Arrays.asList(effects)));
    }
    private static void addElements(Map<String,List<Effect>> values, String name,
            double first, double second, double third, Effect... extra) {
        List<Effect> effects = new ArrayList<Effect>(Arrays.asList(extra));
        for (Nature nature : ELEMENTS) effects.add(new Effect(nature,first,second,third));
        add(values,name,effects.toArray(new Effect[effects.size()]));
    }
    private static final class Effect {
        private final String attribute;
        private final Nature nature;
        private final double[] values;
        Effect(String attribute, double first, double second, double third) {
            this.attribute=attribute; this.nature=null; this.values=new double[]{first,second,third};
        }
        Effect(Nature nature, double first, double second, double third) {
            this.attribute=null; this.nature=nature; this.values=new double[]{first,second,third};
        }
    }

    public static boolean isUpgradeable(final Item item) {
        return item instanceof Glyph && DEFINITIONS.containsKey(item.getName());
    }

    /** Zero denotes an item outside progression, including the knowledge glyph. */
    public static int getLevel(final Item item) {
        return isUpgradeable(item) ? readLevel(item,item.getName()) : 0;
    }

    private static int readLevel(final RPObject saved, final String name) {
        if (saved != null && saved.has(LEVEL_ATTRIBUTE)) {
            try {
                int level = Integer.parseInt(saved.get(LEVEL_ATTRIBUTE));
                if (level >= 1 && level <= 3) return level;
            } catch (IllegalArgumentException invalid) {
                // Normalise malformed legacy state just like out-of-range values.
            }
            LOGGER.warn("Invalid glyph level for " + name + "; restoring level I");
        }
        return 1;
    }

    /** Rebuild from the table, never multiply already materialised effects. */
    public static void applyLevel(final Item item, final int level) {
        if (!isUpgradeable(item) || level < 1 || level > 3) {
            throw new IllegalArgumentException("Glyph level requires a supported glyph and level 1-3");
        }
        final int oldHealth = item.has("health") ? item.getInt("health") : 0;
        final Map<Nature,Double> susceptibilities = new EnumMap<Nature,Double>(Nature.class);
        for (Effect effect : DEFINITIONS.get(item.getName())) {
            double value = effect.values[level-1];
            if (effect.nature != null) {
                susceptibilities.put(effect.nature,1.0-value/100.0);
            } else if ("atk".equals(effect.attribute) || "def".equals(effect.attribute) || "health".equals(effect.attribute)) {
                item.put(effect.attribute,(int)value);
            } else {
                item.put(effect.attribute,value);
            }
        }
        if (item.has(GlyphEffectService.ATTACK_RATE_REDUCTION_ATTRIBUTE)) {
            item.remove(GlyphEffectService.LEGACY_ATTACK_RATE_REDUCTION_ATTRIBUTE);
        }
        item.setSusceptibilities(susceptibilities);
        item.put(LEVEL_ATTRIBUTE,level);
        ItemTooltipService.update(item);

        if (item.isContained() && item.getContainer() instanceof Player
                && Slots.GLYPHS.getNames().contains(item.getContainerSlot().getName())) {
            Player player = (Player)item.getContainer();
            int health = item.has("health") ? item.getInt("health") : 0;
            if (health != oldHealth) {
                long maximum = (long)player.getUncappedBaseHP()+health-oldHealth;
                player.setBaseHP((int)Math.max(0,Math.min(Integer.MAX_VALUE,maximum)));
                if (player.getHP() > player.getBaseHP()) player.setHP(player.getBaseHP());
            }
            GlyphRecoveryService.reset(player);
            player.updateItemAtkDef();
            if (player.getZone() != null) player.notifyWorldAboutChanges();
        }
    }

    public static void restore(final Item item, final RPObject saved) {
        if (isUpgradeable(item)) {
            applyLevel(item,readLevel(saved,item.getName()));
        } else if (item instanceof Glyph && item.has(LEVEL_ATTRIBUTE)) {
            item.remove(LEVEL_ATTRIBUTE);
        }
    }

    /** Player HP already includes the saved glyph bonus; change only its delta. */
    public static void restoreEquippedHealth(final Player player, final Item item,
            final RPObject saved, final String slot) {
        if (!(item instanceof Glyph) || !Slots.GLYPHS.getNames().contains(slot)) return;
        final int before = saved.has("health") ? saved.getInt("health") : 0;
        final int after = item.has("health") ? item.getInt("health") : 0;
        if (before == after) return;
        final long maximum = (long)player.getUncappedBaseHP()+after-before;
        player.setBaseHP((int)Math.max(0,Math.min(Integer.MAX_VALUE,maximum)));
        if (player.getHP() > player.getBaseHP()) player.setHP(player.getBaseHP());
    }
}
