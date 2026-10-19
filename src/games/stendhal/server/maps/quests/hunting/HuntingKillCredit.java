package games.stendhal.server.maps.quests.hunting;

import org.apache.log4j.Logger;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.ArenaCreature;
import games.stendhal.server.entity.creature.DeathMatchCreature;
import games.stendhal.server.entity.player.Player;

/** Keep arena kills in lifetime statistics without advancing Janislaw's hunt. */
public final class HuntingKillCredit {
    private static final Logger LOGGER = Logger.getLogger(HuntingKillCredit.class);
    private HuntingKillCredit() { }

    /** Called once, immediately after the corresponding lifetime kill increment. */
    public static void excludeArenaKill(Player player, RPEntity victim, boolean solo) {
        if (!player.hasQuest("hunting")) return;
        String zone = victim.getZone() == null ? "" : victim.getZone().getName();
        if (!(victim instanceof ArenaCreature) && !(victim instanceof DeathMatchCreature)
                && !"int_fado_battle_pit".equals(zone) && !"int_fado_battle_arena".equals(zone)) return;
        String[] kills = player.getQuest("hunting", 0).split(",", -1);
        if (kills.length != 5 || !kills[0].equals(victim.getName())) return;
        // The hunt counts lifetime kills minus its starting baseline. Advance
        // only that baseline to cancel this one arena kill, keeping old progress.
        int index = solo ? 3 : 4;
        try {
            kills[index] = Integer.toString(Math.addExact(Integer.parseInt(kills[index]), 1));
            player.setQuest("hunting", 0, String.join(",", kills));
        } catch (NumberFormatException | ArithmeticException invalid) {
            LOGGER.warn("Invalid hunting baseline for " + player.getName(), invalid);
        }
    }
}
