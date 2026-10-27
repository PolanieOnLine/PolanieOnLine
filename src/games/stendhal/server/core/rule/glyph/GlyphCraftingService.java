package games.stendhal.server.core.rule.glyph;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/** Shared original level-I recipes, used by Zoryk and the workbench. */
public final class GlyphCraftingService {
    private static final Map<String, Map<String, Integer>> RECIPES = recipes();
    private static final Map<String, Creation> OFFERS = new ConcurrentHashMap<String, Creation>();
    static { SingletonRepository.getLogoutNotifier().addListener(GlyphCraftingService::clear); }
    private static Map<String, Map<String, Integer>> recipes() {
		Map<String, Map<String, Integer>> glyphConfig = new LinkedHashMap<String, Map<String, Integer>>();
		glyphConfig.put("glif daru Mokoszy", createResourceMap(
			"serce olbrzyma", 100,
			"smocza krew", 40,
			"fragment glifu", 4
		));
		glyphConfig.put("glif siły", createResourceMap(
			"miecz", 1,
			"kieł smoka", 20,
			"kość dla psa", 50,
			"fragment glifu", 2
		));
		glyphConfig.put("glif Peruna", createResourceMap(
			"glif siły", 1,
			"rubin", 18,
			"pióro azazela", 12,
			"pióro lilith", 4,
			"fragment glifu", 5
		));
		glyphConfig.put("glif czaszy", createResourceMap(
			"kieł złotej kostuchy", 1,
			"pióro archanioła", 22,
			"ruda srebra", 40,
			"fragment glifu", 3
		));
		glyphConfig.put("glif tarczy", createResourceMap(
			"bryłka mithrilu", 30,
			"lodowa tarcza", 1,
			"ruda żelaza", 100,
			"fragment glifu", 2
		));
		glyphConfig.put("glif Swaroga", createResourceMap(
			"glif tarczy", 1,
			"szafir", 22,
			"pióro serafina", 16,
			"pióro cherubina", 4,
			"fragment glifu", 5
		));
		glyphConfig.put("glif Tytana", createResourceMap(
			"glif tarczy", 1,
			"glif siły", 1,
			"serce olbrzyma", 50,
			"cudowna krew", 4,
			"kolorowe kulki", 30,
			"fragment glifu", 3
		));
		glyphConfig.put("glif Strzyboga", createResourceMap(
			"piórko", 100,
			"pióro anioła", 40,
			"cudowna krew", 3,
			"bursztyn", 20,
			"ametyst", 32,
			"fragment glifu", 4
		));
		glyphConfig.put("glif Jarowita", createResourceMap(
			"magia ziemi", 1000,
			"magia płomieni", 800,
			"magia deszczu", 800,
			"magia mrozu", 600,
			"magia wiatru", 400,
			"magia mroku", 400,
			"magia światła", 300,
			"kryształ obsydianu", 11,
			"fragment glifu", 3
		));
		glyphConfig.put("glif kryzysu", createResourceMap(
			"kości do gry", 1,
			"diament", 14,
			"ząb potwora", 3,
			"sztabka platyny", 40,
			"sztabka miedzi", 32,
			"fragment glifu", 2
		));
		glyphConfig.put("glif krwi", createResourceMap(
			"wampirza krew", 36,
			"truchło nietoperza", 14,
			"truchło wampira", 22,
			"obsydian", 22,
			"fragment glifu", 3
		));

		glyphConfig.put("glif świętego dębu", createResourceMap(
			"fragment glifu", 3,
			"magia ziemi", 800,
			"bryłka mithrilu", 20,
			"szafir", 12
		));
		glyphConfig.put("glif żywej wody", createResourceMap(
			"fragment glifu", 4,
			"magia deszczu", 800,
			"serce olbrzyma", 40,
			"cudowna krew", 3
		));
		glyphConfig.put("glif Rodzanic", createResourceMap(
			"fragment glifu", 4,
			"serce olbrzyma", 60,
			"smocza krew", 24,
			"bursztyn", 20
		));
		glyphConfig.put("glif wichru", createResourceMap(
			"glif Strzyboga", 1,
			"fragment glifu", 2,
			"magia wiatru", 600,
			"ametyst", 16,
			"pióro anioła", 20
		));
		glyphConfig.put("glif wytrwałości", createResourceMap(
			"glif Strzyboga", 1,
			"fragment glifu", 2,
			"serce olbrzyma", 40,
			"ametyst", 16,
			"cudowna krew", 2
		));
		glyphConfig.put("glif ogniska", createResourceMap(
			"fragment glifu", 3,
			"magia płomieni", 1000,
			"bursztyn", 30,
			"kryształ obsydianu", 8
		));
		glyphConfig.put("glif korzeni", createResourceMap(
			"fragment glifu", 3,
			"magia ziemi", 800,
			"magia deszczu", 800,
			"kryształ obsydianu", 8
		));
		glyphConfig.put("glif zmory", createResourceMap(
			"fragment glifu", 3,
			"magia mroku", 800,
			"wampirza krew", 24,
			"truchło nietoperza", 12,
			"obsydian", 16
		));
		glyphConfig.put("glif Świętowita", createResourceMap(
			"fragment glifu", 5,
			"glif siły", 1,
			"glif tarczy", 1,
			"diament", 8,
			"rubin", 12,
			"szafir", 12
		));
		glyphConfig.put("glif przodków", createResourceMap(
			"fragment glifu", 5,
			"glif tarczy", 1,
			"serce olbrzyma", 50,
			"cudowna krew", 4,
			"magia ziemi", 400,
			"magia światła", 400
		));

        Map<String, Map<String, Integer>> immutable = new LinkedHashMap<String, Map<String, Integer>>();
        for (Map.Entry<String, Map<String, Integer>> recipe : glyphConfig.entrySet()) {
            immutable.put(recipe.getKey(), Collections.unmodifiableMap(recipe.getValue()));
        }
        return Collections.unmodifiableMap(immutable);
    }
	private static Map<String, Integer> createResourceMap(final Object... keyValuePairs) {
		Map<String, Integer> resources = new LinkedHashMap<String, Integer>();
		for (int i = 0; i < keyValuePairs.length; i += 2) {
			String key = (String) keyValuePairs[i];
			Integer value = (Integer) keyValuePairs[i + 1];
			resources.put(key, value);
		}
		return resources;
	}

    public static Map<String, Map<String, Integer>> getRecipes() { return RECIPES; }
    public static boolean hasResources(Player player, String name) {
        Map<String, Integer> required = RECIPES.get(name);
        if (required == null) return false;
        for (Map.Entry<String, Integer> material : required.entrySet()) {
            if (GlyphUpgradeService.getInstance().getResourceCount(player, material.getKey()) < material.getValue()) return false;
        }
        return true;
    }
    public static String prepare(Player player, String name) {
        clear(player);
        if (!hasResources(player, name)) return null;
        Creation offer = new Creation(name); OFFERS.put(player.getName(), offer); return offer.token;
    }
    public static Item craft(Player player, String name, String token) {
        Creation offer = OFFERS.remove(player.getName());
        if (offer == null || !offer.name.equals(name) || !offer.token.equals(token)
                || System.currentTimeMillis() >= offer.expires || !hasResources(player, name)) return null;
        Item output = SingletonRepository.getEntityManager().getItem(name, ItemCreationContext.quest());
        GlyphProgressionService.applyLevel(output, 1);
        boolean success = GlyphUpgradeService.getInstance().consumeResources(player, RECIPES.get(name), () -> {
            boolean equipped = false;
            try { equipped = player.equip("bag", output); return equipped; }
            finally {
                if (!equipped && output.isContained()) output.getContainerSlot().remove(output.getID());
            }
        });
        if (!success) return null;
        player.incProducedForItem(name, 1);
        if (player.getZone() != null) player.notifyWorldAboutChanges();
        return output;
    }
    public static void clear(Player player) { if (player != null) OFFERS.remove(player.getName()); }
    private static final class Creation {
        final String name;
        final String token = UUID.randomUUID().toString();
        final long expires = System.currentTimeMillis() + 300000L;
        Creation(String name) { this.name = name; }
    }
}
