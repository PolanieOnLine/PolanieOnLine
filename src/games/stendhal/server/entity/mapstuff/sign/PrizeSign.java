package games.stendhal.server.entity.mapstuff.sign;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import games.stendhal.common.Translation;
import games.stendhal.server.core.engine.Translate;

/** An authored prize list whose numbered rows contain canonical item names. */
public class PrizeSign extends Sign {
    private static final Pattern PRIZE_ROW = Pattern.compile("^([0-9]+: )(.+)$");

    @Override public String getText() {
        return localize(Translate::getText);
    }

    @Override public String getText(final String language) {
        return localize(text -> Translation.getText(language, text));
    }

    private String localize(final Function<String, String> translator) {
        if (!has(ATTR_TEXT)) return null;
        final String[] lines = get(ATTR_TEXT).split("\n", -1);
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (i > 0) result.append('\n');
            final Matcher row = PRIZE_ROW.matcher(lines[i]);
            result.append(row.matches() ? row.group(1) + translator.apply(row.group(2))
                    : translator.apply(lines[i]));
        }
        return result.toString();
    }
}
