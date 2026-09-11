package games.stendhal.server.actions.skilltree;

import static games.stendhal.common.constants.Actions.MODE;
import static games.stendhal.common.constants.Actions.NODE;
import static games.stendhal.common.constants.Actions.SKILLTREE;

import games.stendhal.common.NotificationType;
import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.skilltree.SkillTreeService;
import games.stendhal.server.skilltree.SkillTreeUnlockResult;
import marauroa.common.game.RPAction;

/**
 * Handles /skilltree interactions from the client.
 */
public class SkillTreeAction implements ActionListener {

    public static void register() {
        CommandCenter.register(SKILLTREE, new SkillTreeAction());
    }

    @Override
    public void onAction(Player player, RPAction action) {
        SkillTreeService.ensureInitialized(player);

        String mode = action.has(MODE) ? action.get(MODE) : "inspect";
        if ("unlock".equalsIgnoreCase(mode)) {
            handleUnlock(player, action.get(NODE));
        } else {
            player.sendPrivateText(NotificationType.INFORMATION,
                    "Punkty umiejętności dostępne: " + SkillTreeService.getAvailablePoints(player));
        }
    }

    private void handleUnlock(Player player, String nodeIdRaw) {
        if ((nodeIdRaw == null) || nodeIdRaw.isEmpty()) {
            player.sendPrivateText(NotificationType.NEGATIVE,
                    "Podaj kod umiejętności do odblokowania, np. /skilltree unlock FOC_1");
            return;
        }

        String nodeId = nodeIdRaw.trim().toUpperCase();
        SkillTreeUnlockResult result = SkillTreeService.unlock(player, nodeId);
        switch (result) {
        case SUCCESS:
            player.sendPrivateText(NotificationType.POSITIVE,
                    "Odblokowano " + nodeId + ". Pozostałe punkty: "
                            + SkillTreeService.getAvailablePoints(player));
            break;
        case UNKNOWN_NODE:
            player.sendPrivateText(NotificationType.NEGATIVE,
                    "Nie znaleziono węzła " + nodeId + " w drzewku maga ognia.");
            break;
        case ALREADY_UNLOCKED:
            player.sendPrivateText(NotificationType.INFORMATION,
                    "Umiejętność " + nodeId + " jest już odblokowana.");
            break;
        case PREREQUISITES_MISSING:
            player.sendPrivateText(NotificationType.NEGATIVE,
                    "Najpierw odblokuj wymagane węzły, aby zdobyć " + nodeId + ".");
            break;
        case NOT_ENOUGH_POINTS:
            player.sendPrivateText(NotificationType.NEGATIVE,
                    "Brakuje punktów umiejętności, aby odblokować " + nodeId + ".");
            break;
        default:
            break;
        }
    }
}

