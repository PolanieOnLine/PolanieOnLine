package games.stendhal.client.gui.skilltree;

/**
 * Builds static descriptions for the mage skill tree.
 */
final class SkillTreeContentBuilder {

    private static final String LINE_SEPARATOR = System.lineSeparator();

    private SkillTreeContentBuilder() {
    }

    static String buildFireMageTree() {
        StringBuilder builder = new StringBuilder();
        append(builder, "[Drzewko umiejętności maga ognia]");
        append(builder, "");
        append(builder, "\tWskazówki:");
        append(builder, "\t\t- Odblokuj podstawowe ścieżki, aby przygotować się na większe zaklęcia.");
        append(builder, "\t\t- Każdy węzeł pokazuje kod umiejętności w nawiasach kwadratowych.");
        append(builder, "\t\t- Wymagania wymienione poniżej opisują dokładne zależności.");
        append(builder, "");
        append(builder, "\tFire Dart – pierwszy pocisk ognia");
        append(builder, "\t\tŚcieżka skupienia:");
        append(builder, "\t\t\tFOC_1\tSkupienie LV1 – wzmacnia moc żywiołu.");
        append(builder, "\t\t\tFOC_2\tSkupienie LV2 – zwiększa moc i odblokowuje Fire Bolt.");
        append(builder, "\t\t\tFOC_3\tSkupienie LV3 – wymagane, aby ruszyć dalej w stronę Fire Bolt i Fire Dart Rain.");
        append(builder, "\t\t\tFOC_4\tSkupienie LV4 – dodatkowa moc, prowadzi do Fire Dart Rain.");
        append(builder, "\t\t\tFOC_5\tSkupienie LV5 – maksymalne skupienie dla Fire Dart Rain.");
        append(builder, "\t\tŚcieżka rozprzestrzenienia:");
        append(builder, "\t\t\tEXT_1\tRozszerzenie LV1 – dwa pociski w jednej salwie.");
        append(builder, "\t\t\tEXT_2\tRozszerzenie LV2 – trzy pociski; krok ku ulepszeniom obszarowym.");
        append(builder, "\t\t\tEXT_3\tRozszerzenie LV3 – warunek wstępny dla Fire Dart Rain.");
        append(builder, "\t\t\tEXT_4\tRozszerzenie LV4 – wzór na Fire Dart Rain.");
        append(builder, "\t\t\tEXT_5\tRozszerzenie LV5 – pełne przygotowanie do Fire Dart Rain.");
        append(builder, "");
        append(builder, "\tFire Bolt – drugi strzał (wymaga FOC_2)");
        append(builder, "\t\tŚcieżka rozprzestrzenienia:");
        append(builder, "\t\t\tEXT_1\tRozszerzenie LV1 – wzmacnia trafienia pojedynczego celu.");
        append(builder, "\t\t\tEXT_2\tRozszerzenie LV2 – zwiększona liczba pocisków w jednym trafieniu.");
        append(builder, "\t\t\tEXT_3\tRozszerzenie LV3 – potężne uderzenia dla bardziej odpornych przeciwników.");
        append(builder, "\t\t\tEXT_4\tRozszerzenie LV4 – maksymalna siła ognia na pojedynczym celu.");
        append(builder, "\t\tŚcieżka skupienia:");
        append(builder, "\t\t\tFOC_1\tSkupienie LV1 – podstawowe wzmocnienie Fire Bolt.");
        append(builder, "\t\t\tFOC_2\tSkupienie LV2 – większa penetracja odporności.");
        append(builder, "\t\t\tFOC_3\tSkupienie LV3 – otwiera drogę do Fire Dart Rain.");
        append(builder, "\t\t\tFOC_4\tSkupienie LV4 – finalny szlif dla Fire Bolt.");
        append(builder, "");
        append(builder, "\tBurn – umiejętność przygotowująca płonące odgałęzienia");
        append(builder, "\t\tFOC_1\tSkupienie żaru LV1 – wzmacnia status podpalenia.");
        append(builder, "\t\tFOC_2\tSkupienie żaru LV2 – dłuższe i silniejsze poparzenia.");
        append(builder, "\t\tFOC_3\tSkupienie żaru LV3 – maksymalny efekt podpalenia.");
        append(builder, "");
        append(builder, "\tFire Dart Rain – salwa obszarowa (wymaga EXT_5 oraz Fire Dart)");
        append(builder, "\t\tŚcieżka skupienia:");
        append(builder, "\t\t\tFOC_1\tSkupienie LV1 – wzmacnia obrażenia obszarowe.");
        append(builder, "\t\t\tFOC_2\tSkupienie LV2 – zwiększa promień rażenia.");
        append(builder, "\t\t\tFOC_3\tSkupienie LV3 – maksymalne obrażenia na obszarze.");
        append(builder, "");
        append(builder, "\tBurning Fire Dart – ognisty rozwój alternatywny (wymaga Burn oraz Fire Dart)");
        append(builder, "\t\tŚcieżka płonącego skupienia:");
        append(builder, "\t\t\tBFOC_1\tPłonące skupienie LV1 – silniejszy status poparzenia.");
        append(builder, "\t\t\tBFOC_2\tPłonące skupienie LV2 – dłuższe efekty przypalenia.");
        append(builder, "\t\t\tBFOC_3\tPłonące skupienie LV3 – maksymalna moc podpaleń.");
        append(builder, "\t\tŚcieżka momentalnego trafienia:");
        append(builder, "\t\t\tDFOC_1\tSkupienie pocisku LV1 – wzmacnia obrażenia w chwili trafienia.");
        append(builder, "\t\t\tDFOC_2\tSkupienie pocisku LV2 – kolejne zwiększenie obrażeń.");
        append(builder, "\t\t\tDFOC_3\tSkupienie pocisku LV3 – maksymalna siła uderzenia.");
        append(builder, "\t\tŚcieżka poszerzająca pociski:");
        append(builder, "\t\t\tDEXT_1\tRozszerzenie pocisku LV1 – zwiększona ilość w momencie trafienia.");
        append(builder, "\t\t\tDEXT_2\tRozszerzenie pocisku LV2 – trzy pociski uderzające jednocześnie.");
        append(builder, "\t\t\tDEXT_3\tRozszerzenie pocisku LV3 – maksymalne natychmiastowe obrażenia.");
        append(builder, "");
        append(builder, "\tPowiązania kluczowe:");
        append(builder, "\t\t- Fire Bolt\t\twymaga zdobycia FOC_2 ze ścieżki Fire Dart.");
        append(builder, "\t\t- Fire Dart Rain\twymaga EXT_5 oraz ścieżki skupienia Fire Dart do FOC_3.");
        append(builder, "\t\t- Burning Fire Dart\twymaga zakończenia ścieżki Burn oraz Fire Dart.");
        append(builder, "");
        append(builder, "\tWpisz w kliencie polecenie /skilltree, aby otworzyć to drzewko.");
        return builder.toString();
    }

    private static void append(StringBuilder builder, String line) {
        builder.append(line).append(LINE_SEPARATOR);
    }
}
