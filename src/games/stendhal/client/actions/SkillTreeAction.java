package games.stendhal.client.actions;

import static games.stendhal.common.constants.Actions.MODE;
import static games.stendhal.common.constants.Actions.NODE;
import static games.stendhal.common.constants.Actions.SKILLTREE;

import javax.swing.SwingUtilities;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.gui.skilltree.SkillTreeWindow;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.common.NotificationType;
import marauroa.common.game.RPAction;

/**
 * Slash command that opens the mage skill tree window.
 */
class SkillTreeAction implements SlashAction {

    private SkillTreeWindow window;

    @Override
    public boolean execute(final String[] params, final String remainder) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ensureWindow().showWindow();
            }
        });

        if ((params.length > 0) && "unlock".equalsIgnoreCase(params[0])) {
            if (params.length < 2) {
                ClientSingletonRepository.getUserInterface()
                        .addEventLine(new HeaderLessEventLine(
                                "Użycie: /skilltree unlock <kod_węzła>", NotificationType.CLIENT));
                return true;
            }
            sendUnlock(params[1]);
        } else {
            sendInspect();
        }
        return true;
    }

    @Override
    public int getMaximumParameters() {
        return 2;
    }

    @Override
    public int getMinimumParameters() {
        return 0;
    }

    private SkillTreeWindow ensureWindow() {
        if (window == null || !window.isDisplayable()) {
            window = new SkillTreeWindow();
        }
        return window;
    }

    private void sendInspect() {
        RPAction action = new RPAction();
        action.put("type", SKILLTREE);
        action.put(MODE, "inspect");
        ClientSingletonRepository.getClientFramework().send(action);
    }

    private void sendUnlock(String node) {
        RPAction action = new RPAction();
        action.put("type", SKILLTREE);
        action.put(MODE, "unlock");
        action.put(NODE, node.toUpperCase());
        ClientSingletonRepository.getClientFramework().send(action);
    }
}
