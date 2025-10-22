package games.stendhal.client.fx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class that converts emoji shortcodes to Unicode representations.
 */
public final class EmojiManager {

        private static final Map<String, String> SHORTCODES;

        static {
                Map<String, String> map = new HashMap<String, String>();
                map.put(":smile:", "\uD83D\uDE00");
                map.put(":fire:", "\uD83D\uDD25");
                map.put(":heart:", "\u2764\uFE0F");
                SHORTCODES = Collections.unmodifiableMap(map);
        }

        private EmojiManager() {
        }

        /**
         * Replace known shortcodes with Unicode emoji characters.
         *
         * @param msg message to process
         * @return message with shortcodes replaced
         */
        public static String replaceShortcodes(String msg) {
                if ((msg == null) || msg.isEmpty()) {
                        return msg;
                }
                String result = msg;
                for (Map.Entry<String, String> entry : SHORTCODES.entrySet()) {
                        result = result.replace(entry.getKey(), entry.getValue());
                }
                return result;
        }
}
