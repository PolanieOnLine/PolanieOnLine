package games.stendhal.server.entity.npc;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Test;

import games.stendhal.common.Translation;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.maps.quests.DiceGambling;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.Pair;
import utilities.PlayerTestHelper;
import utilities.SpeakerNPCTestHelper;

public class CroupierTranslationTest {
    @BeforeClass public static void setUpClasses() { MockStendlRPWorld.get(); }

    @After public void clearCroupiers() {
        SingletonRepository.getNPCList().remove("TranslationCroupier");
        SingletonRepository.getNPCList().remove("TranslationBilliardCroupier");
    }

    @Test public void dicePrizeMessageTranslatesAuthoredRewardWithoutTranslatingTheWinnerName() {
        final Player player = PlayerTestHelper.createPlayer("Witaj.");
        final CroupierNPC npc = new CroupierNPC("TranslationCroupier") { };
        final Map<Integer, Pair<String, String>> prizes = new HashMap<Integer, Pair<String, String>>();
        prizes.put(12, new Pair<String, String>("kanapka", "Wygrałeś pyszną kanapkę!"));
        npc.setPrizes(prizes);
        npc.processWin(player, 12);
        assertEquals("Congratulations Witaj., you scored 12 points. You won a delicious sandwich!",
                Translation.getText("en", SpeakerNPCTestHelper.getReply(npc)));
        assertEquals("kanapka", player.getFirstEquipped("kanapka").getName());
    }

    @Test public void billiardPrizeMessageTranslatesAuthoredRewardWithoutTranslatingTheWinnerName() {
        final Player player = PlayerTestHelper.createPlayer("Witaj.");
        final CroupierNPC npc = new CroupierNPC("TranslationBilliardCroupier") { };
        final Map<Integer, Pair<String, String>> prizes = new HashMap<Integer, Pair<String, String>>();
        prizes.put(7, new Pair<String, String>("kanapka", "Wygrałeś pyszną kanapkę!"));
        npc.setPrizes(prizes);
        npc.processWinBill(player, 7);
        assertEquals("Congratulations Witaj., you potted ball number 7. You won a delicious sandwich!",
                Translation.getText("en", SpeakerNPCTestHelper.getReply(npc)));
    }

    @Test public void diceDealerInstructionsTranslateAfterThePlayerPays() {
        final Player player = PlayerTestHelper.createPlayer("Gambler");
        final StackableItem money = (StackableItem) SingletonRepository.getEntityManager().getItem("money");
        money.setQuantity(100);
        player.equipOrPutOnGround(money);
        final StendhalRPZone zone = new StendhalRPZone("dice_dialogue_translation", 40, 20);
        final CroupierNPC npc = new CroupierNPC("Ricardo") { };
        zone.add(npc);
        try {
            new DiceGambling().addToWorld();
            npc.getEngine().setCurrentState(ConversationStates.QUESTION_1);
            npc.getEngine().step(player, "tak");
            assertEquals("All right, here are the dice. Throw them onto the table when you are ready. Good luck!",
                    Translation.getText("en", SpeakerNPCTestHelper.getReply(npc)));
            assertEquals("kości do gry", player.getFirstEquipped("kości do gry").getName());
        } finally { SingletonRepository.getNPCList().remove(npc.getName()); }
    }
}
