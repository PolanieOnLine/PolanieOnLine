package games.stendhal.client.actions;

import javax.swing.SwingUtilities;

import games.stendhal.client.gui.skilltree.SkillTreeWindow;

/**
 * Slash command that opens the mage skill tree window.
 */
class SkillTreeAction implements SlashAction {

    private SkillTreeWindow window;

    @Override
    public boolean execute(String[] params, String remainder) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (window == null || !window.isDisplayable()) {
                    window = new SkillTreeWindow();
                }
                window.showWindow();
            }
        });
        return true;
    }

    @Override
    public int getMaximumParameters() {
        return 0;
    }

    @Override
    public int getMinimumParameters() {
        return 0;
    }
}
