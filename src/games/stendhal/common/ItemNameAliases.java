package games.stendhal.common;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Historical names retained for saved items and commands after a naming cleanup. */
public final class ItemNameAliases {
    private static final Map<String, String> RENAMED;
    static {
        Map<String, String> names = new LinkedHashMap<>();
        names.put("lazurowy hełm", "hełm lazurowy");
        names.put("lazurowy płaszcz smoczy", "płaszcz smoczy lazurowy");
        names.put("lazurowy płaszcz elficki", "płaszcz elficki lazurowy");
        names.put("prążkowany płaszcz lazurowy", "płaszcz prążkowany lazurowy");
        names.put("szmaragdowy płaszcz smoczy", "płaszcz smoczy szmaragdowy");
        names.put("szmaragdowa tarcza smocza", "tarcza smocza szmaragdowa");
        names.put("hełm ciemnomithrilowy", "hełm z hartowanego mithrilu");
        names.put("zbroja ciemnomithrilowa", "zbroja z hartowanego mithrilu");
        names.put("pas ciemnomithrilowy", "pas z hartowanego mithrilu");
        names.put("spodnie ciemnomithrilowe", "spodnie z hartowanego mithrilu");
        names.put("buty ciemnomithrilowe", "buty z hartowanego mithrilu");
        names.put("tarcza ciemnomithrilowa", "tarcza z hartowanego mithrilu");
        names.put("płaszcz ciemnomithrilowy", "płaszcz z hartowanego mithrilu");
        RENAMED = Collections.unmodifiableMap(names);
    }

    private ItemNameAliases() { }

    public static String canonicalName(String name) { return RENAMED.getOrDefault(name, name); }

    public static Set<String> legacyNames(String canonicalName) {
        Set<String> result = new LinkedHashSet<>();
        for (Map.Entry<String, String> entry : RENAMED.entrySet()) {
            if (entry.getValue().equals(canonicalName)) result.add(entry.getKey());
        }
        return Collections.unmodifiableSet(result);
    }
}
