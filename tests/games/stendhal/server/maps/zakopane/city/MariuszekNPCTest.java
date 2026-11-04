/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.zakopane.city;

import static org.junit.Assert.*;
import static utilities.SpeakerNPCTestHelper.getReply;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import utilities.QuestHelper;
import utilities.ZonePlayerAndNPCTestImpl;

public class MariuszekNPCTest extends ZonePlayerAndNPCTestImpl {
    private static final String ZONE = "0_zakopane_advice_test";

    @BeforeClass public static void prepareWorld() throws Exception {
        QuestHelper.setUpBeforeClass();
        setupZone(ZONE);
    }

    public MariuszekNPCTest() {
        setNpcNames("Mariuszek");
        setZoneForPlayer(ZONE);
        addZoneConfigurator(new MariuszekNPC(), ZONE);
    }

    private String ask(final String text) {
        final SpeakerNPC npc = getNPC("Mariuszek");
        assertTrue("NPC should understand: " + text, npc.getEngine().step(player, text));
        return getReply(npc);
    }

    @Test public void freshPlayerCanAskForHelpWithoutFirstAnsweringYes() {
        ask("hi");
        final String help = ask("pomoc");
        assertTrue(help, help.contains("co dalej"));
        assertTrue(help, help.contains("leczenie"));
        assertFalse(player.hasQuest("meet_pietrek"));
        assertTrue(ask("co dalej").contains("Pietrkiem"));
    }

    @Test public void unfinishedIntroductionCanBeResumedAndAccepted() {
        player.setQuest("meet_mariuszek", "start");
        ask("hi");
        assertTrue(ask("tak").contains("Pietrkiem"));
        assertEquals(ConversationStates.ATTENDING, getNPC("Mariuszek").getEngine().getCurrentState());
        assertEquals("done", player.getQuest("meet_mariuszek"));
    }

    @Test public void directionsDistinguishImmediateServicesFromLockedShop() {
        player.setQuest("meet_mariuszek", "done");
        ask("hi");
        String text = ask("broń");
        assertTrue(text, text.contains("Bogusia") && text.contains("Zakopanem S") && text.contains("Staska"));
        text = ask("jedzenie"); assertTrue(text, text.contains("Jagny") && text.contains("Zakopanem NE"));
        text = ask("leczenie"); assertTrue(text, text.contains("Jadźka") && text.contains("bezpłatnie") && text.contains("ulecz"));
        text = ask("mikstury"); assertTrue(text, text.contains("Carmen") && text.contains("Semos") && text.contains("Zakopanego C"));
        assertTrue(ask("jedzenia").contains("Jagny"));
        assertTrue(ask("leczenia").contains("Jadźka"));
        text = ask("bank"); assertTrue(text, text.contains("Zakopanem S") && text.contains("parterze"));
        text = ask("potwory"); assertTrue(text, text.contains("Sabała") && text.contains("potwór"));
        assertTrue(ask("pieniądze").contains("skóry"));
    }

    @Test public void nextAdviceTracksProgressWithoutStartingOrRewardingTasks() {
        ask("hi");
        assertTrue(ask("co dalej").contains("Pietrkiem"));
        player.setQuest("meet_pietrek", "done");
        assertTrue(ask("co dalej").contains("Adasiem"));
        player.setQuest("pomoc_adasiowi", "done;0;1");
        assertTrue(ask("co dalej").contains("Fryderyka"));
        player.setQuest("scythe_fryderyk", "start");
        String text = ask("co dalej");
        assertTrue(text, text.contains("kosę") && text.contains("Zakopanem S"));
        player.setQuest("scythe_fryderyk", "done");
        assertTrue(ask("co dalej").contains("Staska"));
        assertFalse(player.hasQuest("wegiel_na_opal"));
        player.setQuest("wegiel_na_opal", "done");
        assertTrue(ask("co dalej").contains("Gościrad"));
        assertEquals("done", player.getQuest("meet_pietrek"));
        assertEquals(0, player.getTotalNumberOf("puklerz"));
    }
}
