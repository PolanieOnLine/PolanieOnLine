/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.zakopane.mountain;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Level;
import games.stendhal.common.Translation;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.fsm.Engine;
import games.stendhal.server.entity.player.RebornSystem;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

public class YerenaRebornNPCTest extends ZonePlayerAndNPCTestImpl {
    private static final String ZONE = "0_zakopane_reborn_test";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        QuestHelper.setUpBeforeClass();
        setupZone(ZONE);
    }

    public YerenaRebornNPCTest() {
        setNpcNames("Yerena");
        setZoneForPlayer(ZONE);
        addZoneConfigurator(new YerenaRebornNPC(), ZONE);
    }

    private String offer(final int completedReborns) {
        player.put(RebornSystem.ATTR_REBORNS, completedReborns);
        player.put(RebornSystem.ATTR_REWARDS, 7);
        player.setLevel(Level.maxLevel());
        final SpeakerNPC npc = getNPC("Yerena");
        final Engine engine = npc.getEngine();
        engine.setCurrentState(ConversationStates.IDLE);
        assertTrue(engine.step(player, "hi"));
        getReply(npc);
        assertTrue(engine.step(player, "odrodzenie"));
        return getReply(npc);
    }

    @Test
    public void firstFiveOffersKeepBaseExperienceAndExistingGifts() {
        for (int completed = 0; completed < 5; completed++) {
            final String text = offer(completed);
            assertTrue(text, text.contains("100 procent wartości bazowej"));
            assertTrue(text, text.contains("premia do każdego z limitów ATK, RATK i DEF wyniesie +"
                    + (completed + 1) * 8 + "."));
            assertFalse(text, text.contains("twoje ciosy"));
            assertFalse(text, text.contains("doświadczenie zdobywane w walce"));
            if (completed == 2) assertTrue(text.contains("dwa szczególne sztylety"));
            if (completed == 3) assertTrue(text.contains("mithrilowy amulet"));
            if (completed == 4) assertTrue(text.contains("ekskalibur"));
            assertTrue(Translation.getText("en", text).contains("100 percent of the base value"));
            assertTrue(Translation.getText("en", text).contains("will be +" + (completed + 1) * 8));
            assertTrue(Translation.getText("es", text).contains("será de +" + (completed + 1) * 8));
        }
    }

    @Test
    public void laterOffersWarnAboutTheNextRebornExperienceBeforeConfirmation() {
        final int[] completed = {5, 6, 7, 8, 9, 10, 50};
        final int[] percentages = {95, 90, 85, 80, 75, 75, 75};
        for (int i = 0; i < completed.length; i++) {
            final String text = offer(completed[i]);
            assertTrue(text, text.contains(percentages[i] + " procent wartości bazowej"));
            if (completed[i] < 10) {
                assertTrue(text, text.contains("premia do każdego z limitów ATK, RATK i DEF wyniesie +"
                        + (completed[i] + 1) * 8 + "."));
            } else {
                assertTrue(text, text.contains("Nie otrzymasz kolejnego zwiększenia limitów umiejętności"));
            }
            final String english = Translation.getText("en", text);
            assertTrue(english, english.contains(percentages[i] + " percent of the base value"));
            final String spanish = Translation.getText("es", text);
            assertTrue(spanish, spanish.contains(percentages[i] + " por ciento del valor base"));
            if (completed[i] < 10) {
                assertTrue(english, english.contains("will be +" + (completed[i] + 1) * 8));
                assertTrue(spanish, spanish.contains("será de +" + (completed[i] + 1) * 8));
            } else {
                assertTrue(english, english.contains("no further skill limit increases"));
                assertTrue(spanish, spanish.contains("No recibirás más aumentos"));
            }
        }
    }
}
