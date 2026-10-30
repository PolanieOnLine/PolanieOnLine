package games.stendhal.server.core.rp.achievement.condition;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

/** Remembers the first completion of repeatable child quests without changing their quest states. */
public final class ChildFriendQuestCondition implements ChatCondition {
    private static final String HISTORY_SLOT = "_child_friend_completed";
    private static final String[] QUESTS = {
        "campfire", "icecream_for_annie", "chocolate_for_elisabeth",
        "fishsoup_for_hughie", "eggs_for_marianne"
    };

    /** Called before achievement checks, including while the player's achievements are loading. */
    public static void rememberCompletion(final Player player, final String quest,
            final String previousState, final String currentState) {
        for (int index = 0; index < QUESTS.length; index++) {
            if (!QUESTS[index].equals(quest)) { continue; }
            if (completed(quest, previousState) || completed(quest, currentState)) {
                final int previous = history(player);
                final int updated = previous | (1 << index);
                if (updated != previous) {
                    // Do not call setQuest here: it recursively invokes the achievement notifier.
                    player.setKeyedSlot("!quests", HISTORY_SLOT, Integer.toString(updated));
                }
            }
            return;
        }
    }

    @Override public boolean fire(final Player player, final Sentence sentence, final Entity npc) {
        final int history = history(player);
        for (int index = 0; index < QUESTS.length; index++) {
            if ((history & (1 << index)) == 0 && !completed(QUESTS[index], player.getQuest(QUESTS[index]))) {
                return false;
            }
        }
        return true;
    }

    private static int history(final Player player) {
        final String value = player.getQuest(HISTORY_SLOT);
        if (value == null) { return 0; }
        try {
            final int history = Integer.parseInt(value);
            return history >= 0 && history < (1 << QUESTS.length) ? history : 0;
        } catch (final NumberFormatException invalid) { return 0; }
    }

    private static boolean completed(final String quest, final String state) {
        if (state == null) { return false; }
        if ("campfire".equals(quest) || "eggs_for_marianne".equals(quest)) {
            final String[] parts = state.split(";", -1);
            return "done".equals(parts[0]) || (parts.length > 2 && positiveNumber(parts[2]));
        }
        if ("fishsoup_for_hughie".equals(quest)) {
            return positiveNumber(state);
        }
        return state.startsWith("eating;");
    }

    private static boolean positiveNumber(final String value) {
        try { return Long.parseLong(value) > 0; }
        catch (final NumberFormatException invalid) { return false; }
    }
}
