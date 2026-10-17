package games.stendhal.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.After;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.mapstuff.game.NineSwitchesGameBoard;
import games.stendhal.server.entity.mapstuff.useable.GoldenCauldronEntity;
import games.stendhal.server.entity.mapstuff.useable.sources.SourceIron;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.ProducerBehaviour;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.SpeakerNPCTestHelper;

public class ReleaseServerTranslationTest {
    @BeforeClass public static void setup() {
        MockStendlRPWorld.get();
        GoldenCauldronEntity.generateRPClass();
    }

    @After public void cleanupNpcs() {
        SingletonRepository.getNPCList().remove("ReleaseProducer");
        SingletonRepository.getNPCList().remove("ReleaseSwitches");
    }

    private Player englishPlayer(final String name) {
        final Player player = PlayerTestHelper.createPlayer(name);
        player.setLanguage("en");
        player.setClientVersion("1.0");
        return player;
    }

    @Test public void cauldronMissingIngredientsUseTranslatedNamesAndLeaveTheRecipeCanonical() {
        final StendhalRPZone zone = new StendhalRPZone("release_cauldron_translation", 20, 20);
        final GoldenCauldronEntity cauldron = new GoldenCauldronEntity();
        final Player player = englishPlayer("Witaj.");
        zone.add(cauldron);
        zone.add(player);
        cauldron.onUsed(player);
        cauldron.mix(player);
        final String text = player.events().get(0).get("text");
        assertTrue(text, text.startsWith("Missing ingredients: "));
        assertTrue(text, text.contains("Azazel"));
        assertTrue(text, text.contains("magical blood"));
        assertFalse(text, text.contains("krwi"));
        assertEquals("Witaj.", cauldron.get("brewer"));
        assertTrue(cauldron.getSlot("content").isEmpty());
    }

    @Test public void outstandingProductionOrderTranslatesTheInsertedProductAndDuration() {
        final Player player = englishPlayer("CraftCustomer");
        final SpeakerNPC npc = SpeakerNPCTestHelper.createSpeakerNPC("ReleaseProducer");
        final ProducerBehaviour behaviour = new ProducerBehaviour("release_order", Arrays.asList("make"),
                "polano", Collections.<String, Integer>emptyMap(), 3600);
        player.setQuest("release_order", "2;polano;" + System.currentTimeMillis());
        final String savedOrder = player.getQuest("release_order");
        behaviour.giveProduct(new EventRaiser(npc), player);
        final String text = Translation.getText("en", SpeakerNPCTestHelper.getReply(npc));
        assertTrue(text, text.startsWith("Welcome back! I am still working on your order for 2 pieces of wood. Come back in "));
        assertFalse(text, text.contains("godzin"));
        assertEquals(savedOrder, player.getQuest("release_order"));
    }

    @Test public void nineSwitchesTimeoutPreservesDictionaryLikePlayerNames() {
        final StendhalRPZone zone = new StendhalRPZone("release_switch_translation", 30, 30);
        final NineSwitchesGameBoard board = new NineSwitchesGameBoard(zone, 5, 5);
        final SpeakerNPC npc = SpeakerNPCTestHelper.createSpeakerNPC("ReleaseSwitches");
        board.setNPC(npc);
        board.setPlayerName("Witaj.");
        board.onTurnReached(0);
        assertEquals("Sorry Witaj., your time has run out.",
                Translation.getText("en", SpeakerNPCTestHelper.getReply(npc)));
        SingletonRepository.getTurnNotifier().dontNotify(board);
        board.remove();
    }

    @Test public void authoredFieldsTranslateWhilePlayerNamesAndUserMessagesStayVerbatim() {
        assertEquals("Witaj. knocks at the door!", Translation.getText("en", "Witaj. puka do drzwi!"));
        assertEquals("You asked the administrators: Witaj.\nIt may take a while for someone to answer your question.",
                Translation.getText("en", "Zapytałeś administratorów: Witaj.\nMoże zająć to trochę czasu nim ktoś odpowie na twoje pytanie."));
        assertEquals("The cauldron is currently reserved by Witaj..",
                Translation.getText("en", "Kocioł jest teraz zarezerwowany przez Witaj.."));
    }

    @Test public void minedRewardUsesThePlayersGenderAndLanguageWithoutRenamingTheItem() {
        final Player player = englishPlayer("Miner");
        player.setGender("F");
        new SourceIron().setMiningXP(player, true, "ruda żelaza", 10);
        final String text = player.events().get(0).get("text");
        assertEquals("You mined iron ore.", text);
        assertEquals("ruda żelaza", player.getFirstEquipped("ruda żelaza").getName());
    }
}
