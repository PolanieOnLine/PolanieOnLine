package games.stendhal.server.core.rule.glyph;

import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import games.stendhal.common.Translation;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/** Readable shared-table values for recipes and the glyph upgrade window. */
public final class GlyphUpgradePresentation {
    private GlyphUpgradePresentation() { }
    public static Map<String, String> effects(Player player, Item item, int level) {
        Map<String, String> result = new LinkedHashMap<String, String>();
        NumberFormat format = NumberFormat.getNumberInstance(new Locale(player.getLanguage()));
        format.setGroupingUsed(false); format.setMaximumFractionDigits(2);
        for (Map.Entry<String, Double> effect : GlyphProgressionService.getEffects(item, level).entrySet()) {
            String key = effect.getKey();
            boolean percent = !("health".equals(key) || "atk".equals(key) || "def".equals(key)
                    || "glyph_attack_rate_reduction".equals(key) || "out_of_combat_regen_cap".equals(key));
            result.put(Translation.getText(player.getLanguage(), effectLabel(key)),
                    format.format(effect.getValue()) + (percent ? "%" : ""));
        }
        return result;
    }
    public static String describe(Player player, Item item, int level) {
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> effect : effects(player, item, level).entrySet()) {
            if (result.length() > 0) result.append(", ");
            result.append(effect.getKey()).append(": ").append(effect.getValue());
        }
        return result.toString();
    }
            private static String effectLabel(String key) {
                switch (key) {
                case "health": return "PZ";
                case "atk": return "ATK";
                case "def": return "DEF";
                case "atk_additional_bonus": return "bonus obrażeń";
                case "def_additional_bonus": return "bonus obrony";
                case "critical_additional_bonus": return "dodatkowe obrażenia krytyczne";
                case "glyph_attack_rate_reduction": return "zmniejszenie wagi broni (minimum 2)";
                case "critical_chance": return "szansa na trafienie krytyczne";
                case "lifesteal_increase": return "kradzież życia";
                case "critical_bonus_reduction": return "redukcja dodatkowych obrażeń krytycznych";
                case "out_of_combat_regen": return "regeneracja co 5 s po 10 s bez zadawania i otrzymywania obrażeń";
                case "out_of_combat_regen_cap": return "maksymalna regeneracja PZ na impuls";
                case "consumable_healing_bonus": return "bonus leczenia z jedzenia, napojów i mikstur";
                case "physical_dodge_chance": return "unik zwykłego niekrytycznego trafienia fizycznego";
                case "low_health_damage_reduction": return "redukcja obrażeń poniżej 35% PZ";
                case "nightmare_chance": return "szansa na osłabienie ataku o 10% na 6 s (odnowienie 12 s, bez kumulacji)";
                case "resistance_fire": return "odporność na ogień";
                case "resistance_earth": return "odporność na ziemię";
                case "resistance_water": return "odporność na wodę";
                case "resistance_ice": return "odporność na lód";
                case "resistance_dark": return "odporność na mrok";
                case "resistance_light": return "odporność na światło";
                default: return key;
                }
            }

}
