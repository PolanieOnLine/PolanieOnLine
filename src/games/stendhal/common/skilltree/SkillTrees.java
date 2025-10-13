package games.stendhal.common.skilltree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Registry of predefined skill trees used by the client and the server.
 */
public final class SkillTrees {

    private static final SkillTreeDefinition FIRE_MAGE_TREE = createFireMageTree();

    private SkillTrees() {
    }

    public static SkillTreeDefinition fireMage() {
        return FIRE_MAGE_TREE;
    }

    private static SkillTreeDefinition createFireMageTree() {
        List<SkillNodeDefinition> nodes = new ArrayList<SkillNodeDefinition>();

        // Fire Dart root and progressions
        nodes.add(SkillNodeDefinition.builder()
                .id("FIRE_DART")
                .branch("🔥 FIRE DART")
                .displayName("Fire Dart")
                .description("Podstawowy pocisk ognia – dostępny na starcie.")
                .automaticallyUnlocked(true)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FOC_1")
                .branch("🔥 FIRE DART")
                .displayName("FOC_1")
                .description("Skupienie LV1 – +10% mocy czarów ognia.")
                .addPrerequisite("FIRE_DART")
                .damageModifierBonus(0.10)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FOC_2")
                .branch("🔥 FIRE DART")
                .displayName("FOC_2")
                .description("Skupienie LV2 – +15% obrażeń i odblokowanie Fire Bolt.")
                .addPrerequisite("FOC_1")
                .damageModifierBonus(0.15)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FOC_3")
                .branch("🔥 FIRE DART")
                .displayName("FOC_3")
                .description("Skupienie LV3 – wymagane do Fire Dart Rain.")
                .addPrerequisite("FOC_2")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FOC_4")
                .branch("🔥 FIRE DART")
                .displayName("FOC_4")
                .description("Skupienie LV4 – dalszy wzrost mocy pocisku.")
                .addPrerequisite("FOC_3")
                .damageModifierBonus(0.05)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FOC_5")
                .branch("🔥 FIRE DART")
                .displayName("FOC_5")
                .description("Skupienie LV5 – maksymalna moc pojedynczego pocisku.")
                .addPrerequisite("FOC_4")
                .damageModifierBonus(0.05)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("EXT_1")
                .branch("🔥 FIRE DART")
                .displayName("EXT_1")
                .description("Rozszerzenie LV1 – dwa pociski w salwie.")
                .addPrerequisite("FIRE_DART")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("EXT_2")
                .branch("🔥 FIRE DART")
                .displayName("EXT_2")
                .description("Rozszerzenie LV2 – trzy pociski i fundament pod ulepszenia obszarowe.")
                .addPrerequisite("EXT_1")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("EXT_3")
                .branch("🔥 FIRE DART")
                .displayName("EXT_3")
                .description("Rozszerzenie LV3 – krok w stronę Fire Dart Rain.")
                .addPrerequisite("EXT_2")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("EXT_4")
                .branch("🔥 FIRE DART")
                .displayName("EXT_4")
                .description("Rozszerzenie LV4 – wzorzec salwy obszarowej.")
                .addPrerequisite("EXT_3")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("EXT_5")
                .branch("🔥 FIRE DART")
                .displayName("EXT_5")
                .description("Rozszerzenie LV5 – pełne przygotowanie do Fire Dart Rain.")
                .addPrerequisite("EXT_4")
                .build());

        // Fire Bolt branch
        nodes.add(SkillNodeDefinition.builder()
                .id("FIRE_BOLT")
                .branch("⚡ FIRE BOLT")
                .displayName("Fire Bolt")
                .description("Wymaga FOC_2 – potężniejszy drugi strzał.")
                .addPrerequisite("FOC_2")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FIRE_BOLT_FOC_1")
                .branch("⚡ FIRE BOLT")
                .displayName("FOC_1")
                .description("Skupienie LV1 – podstawowe wzmocnienie Fire Bolt.")
                .addPrerequisite("FIRE_BOLT")
                .damageModifierBonus(0.08)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FIRE_BOLT_FOC_2")
                .branch("⚡ FIRE BOLT")
                .displayName("FOC_2")
                .description("Skupienie LV2 – większa penetracja odporności.")
                .addPrerequisite("FIRE_BOLT_FOC_1")
                .damageModifierBonus(0.1)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FIRE_BOLT_FOC_3")
                .branch("⚡ FIRE BOLT")
                .displayName("FOC_3")
                .description("Skupienie LV3 – otwiera Fire Dart Rain.")
                .addPrerequisite("FIRE_BOLT_FOC_2")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FIRE_BOLT_EXT_1")
                .branch("⚡ FIRE BOLT")
                .displayName("EXT_1")
                .description("Rozszerzenie LV1 – silniejsze trafienie pojedynczego celu.")
                .addPrerequisite("FIRE_BOLT")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FIRE_BOLT_EXT_2")
                .branch("⚡ FIRE BOLT")
                .displayName("EXT_2")
                .description("Rozszerzenie LV2 – zwiększona liczba pocisków w jednym trafieniu.")
                .addPrerequisite("FIRE_BOLT_EXT_1")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FIRE_BOLT_EXT_3")
                .branch("⚡ FIRE BOLT")
                .displayName("EXT_3")
                .description("Rozszerzenie LV3 – przygotowuje do odpornych przeciwników.")
                .addPrerequisite("FIRE_BOLT_EXT_2")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FIRE_BOLT_EXT_4")
                .branch("⚡ FIRE BOLT")
                .displayName("EXT_4")
                .description("Rozszerzenie LV4 – maksymalna siła ognia na pojedynczym celu.")
                .addPrerequisite("FIRE_BOLT_EXT_3")
                .build());

        // Burn branch
        nodes.add(SkillNodeDefinition.builder()
                .id("BURN")
                .branch("🔥 BURN")
                .displayName("Burn")
                .description("Status podpalenia – wzmacnia rozwój płonących odgałęzień.")
                .addPrerequisite("FIRE_DART")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("BURN_FOC_1")
                .branch("🔥 BURN")
                .displayName("FOC_1")
                .description("Skupienie żaru LV1 – wzmacnia status podpalenia.")
                .addPrerequisite("BURN")
                .burnDamageMultiplier(1.25)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("BURN_FOC_2")
                .branch("🔥 BURN")
                .displayName("FOC_2")
                .description("Skupienie żaru LV2 – wydłuża podpalenie o 1s.")
                .addPrerequisite("BURN_FOC_1")
                .burnDurationBonusSeconds(1)
                .burnDamageMultiplier(1.5)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("BURN_FOC_3")
                .branch("🔥 BURN")
                .displayName("FOC_3")
                .description("Skupienie żaru LV3 – maksymalny efekt podpalenia.")
                .addPrerequisite("BURN_FOC_2")
                .burnDamageMultiplier(1.75)
                .build());

        // Fire Dart Rain branch
        nodes.add(SkillNodeDefinition.builder()
                .id("FIRE_DART_RAIN")
                .branch("💥 FIRE DART RAIN")
                .displayName("Fire Dart Rain")
                .description("Wymaga EXT_5 i FOC_3 – odblokowuje salwę obszarową.")
                .prerequisites(Arrays.asList("EXT_5", "FOC_3"))
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FIRE_DART_RAIN_FOC_1")
                .branch("💥 FIRE DART RAIN")
                .displayName("FOC_1")
                .description("Skupienie LV1 – wzmacnia obrażenia obszarowe.")
                .addPrerequisite("FIRE_DART_RAIN")
                .damageModifierBonus(0.10)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FIRE_DART_RAIN_FOC_2")
                .branch("💥 FIRE DART RAIN")
                .displayName("FOC_2")
                .description("Skupienie LV2 – zwiększa promień rażenia.")
                .addPrerequisite("FIRE_DART_RAIN_FOC_1")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("FIRE_DART_RAIN_FOC_3")
                .branch("💥 FIRE DART RAIN")
                .displayName("FOC_3")
                .description("Skupienie LV3 – maksymalne obrażenia na obszarze.")
                .addPrerequisite("FIRE_DART_RAIN_FOC_2")
                .damageModifierBonus(0.15)
                .build());

        // Burning Fire Dart branch
        nodes.add(SkillNodeDefinition.builder()
                .id("BURNING_FIRE_DART")
                .branch("🔥 BURNING FIRE DART")
                .displayName("Burning Fire Dart")
                .description("Wymaga Fire Dart i Burn – łączy status z pociskiem.")
                .prerequisites(Arrays.asList("FIRE_DART", "BURN"))
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("BFOC_1")
                .branch("🔥 BURNING FIRE DART")
                .displayName("BFOC_1")
                .description("Płonące skupienie LV1 – silniejszy efekt podpalenia.")
                .addPrerequisite("BURNING_FIRE_DART")
                .burnDamageMultiplier(1.5)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("BFOC_2")
                .branch("🔥 BURNING FIRE DART")
                .displayName("BFOC_2")
                .description("Płonące skupienie LV2 – dłuższe efekty przypalenia.")
                .addPrerequisite("BFOC_1")
                .burnDurationBonusSeconds(1)
                .burnDamageMultiplier(1.75)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("BFOC_3")
                .branch("🔥 BURNING FIRE DART")
                .displayName("BFOC_3")
                .description("Płonące skupienie LV3 – potrójne obrażenia DOT.")
                .addPrerequisite("BFOC_2")
                .burnDamageMultiplier(3.0)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("DFOC_1")
                .branch("🔥 BURNING FIRE DART")
                .displayName("DFOC_1")
                .description("Skupienie pocisku LV1 – zwiększa obrażenia przy trafieniu.")
                .addPrerequisite("BURNING_FIRE_DART")
                .damageModifierBonus(0.08)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("DFOC_2")
                .branch("🔥 BURNING FIRE DART")
                .displayName("DFOC_2")
                .description("Skupienie pocisku LV2 – kolejne zwiększenie obrażeń.")
                .addPrerequisite("DFOC_1")
                .damageModifierBonus(0.10)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("DFOC_3")
                .branch("🔥 BURNING FIRE DART")
                .displayName("DFOC_3")
                .description("Skupienie pocisku LV3 – maksymalna siła uderzenia.")
                .addPrerequisite("DFOC_2")
                .damageModifierBonus(0.12)
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("DEXT_1")
                .branch("🔥 BURNING FIRE DART")
                .displayName("DEXT_1")
                .description("Rozszerzenie pocisku LV1 – dodatkowe uderzenie przy trafieniu.")
                .addPrerequisite("BURNING_FIRE_DART")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("DEXT_2")
                .branch("🔥 BURNING FIRE DART")
                .displayName("DEXT_2")
                .description("Rozszerzenie pocisku LV2 – trzy pociski jednocześnie.")
                .addPrerequisite("DEXT_1")
                .build());

        nodes.add(SkillNodeDefinition.builder()
                .id("DEXT_3")
                .branch("🔥 BURNING FIRE DART")
                .displayName("DEXT_3")
                .description("Rozszerzenie pocisku LV3 – maksymalne natychmiastowe obrażenia.")
                .addPrerequisite("DEXT_2")
                .build());

        return new SkillTreeDefinition("fire_mage", "🜂 DRZEWKO UMIEJĘTNOŚCI", "Mag Ognia", nodes, 2);
    }
}

