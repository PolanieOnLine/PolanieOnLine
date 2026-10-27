package games.stendhal.server.actions.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.Translate;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.entity.npc.CroupierNPC;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.quests.BilliardGame;
import games.stendhal.server.maps.quests.DiceGambling;

public class GamblingPrizeSignTest {
    @BeforeClass public static void setUpClasses() { MockStendlRPWorld.get(); }

    @Test public void dicePrizeBoardsTranslateItemNamesAndPreserveScoresAndCanonicalText() {
        final StendhalRPZone zone = new StendhalRPZone("dice_prize_translation", 40, 20);
        final CroupierNPC npc = new CroupierNPC("Ricardo") { };
        zone.add(npc);
        try {
            new DiceGambling().addToWorld();
            final Sign sign = (Sign) zone.getEntityAt(25, 0);
            final String saved = sign.get("text");
            final String display = LookAction.getLookDescription(sign, "en");
            assertTrue(display, display.contains("18: killer helmet"));
            assertTrue(display, display.contains("16: longbow"));
            assertTrue(display, display.contains("PRIZES:"));
            Translate.setThreadLanguage("en");
            try {
                assertTrue(sign.getText().contains("18: killer helmet"));
            } finally { Translate.setThreadLanguage(null); }
            assertEquals(saved, sign.get("text"));
            assertTrue(saved.contains("18: hełm zabójcy"));
        } finally { SingletonRepository.getNPCList().remove(npc.getName()); }
    }

    @Test public void billiardsPrizeBoardsTranslateItemNamesWithoutChangingPrizes() {
        final StendhalRPZone zone = new StendhalRPZone("billiard_prize_translation", 40, 20);
        final CroupierNPC npc = new CroupierNPC("Maris") { };
        zone.add(npc);
        try {
            new BilliardGame().addToWorld();
            final Sign sign = (Sign) zone.getEntityAt(3, 1);
            final String display = LookAction.getLookDescription(sign, "en");
            assertTrue(display, display.contains("7: sandwich"));
            assertTrue(sign.get("text").contains("7: kanapka"));
        } finally { SingletonRepository.getNPCList().remove(npc.getName()); }
    }
}
