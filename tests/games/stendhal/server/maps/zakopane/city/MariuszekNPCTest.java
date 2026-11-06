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
        assertTrue(text, text.contains("Bogusia") && text.contains("południu") && text.contains("Staska"));
        text = ask("jedzenie"); assertTrue(text, text.contains("Jagny") && text.contains("północnym wschodzie"));
        text = ask("leczenie"); assertTrue(text, text.contains("Jadźka") && text.contains("za darmo") && text.contains("ulecz"));
        text = ask("mikstury"); assertTrue(text, text.contains("Carmen") && text.contains("Semos") && text.contains("centrum Zakopanego"));
        assertTrue(ask("jedzenia").contains("Jagny"));
        assertTrue(ask("leczenia").contains("Jadźka"));
        text = ask("bank"); assertTrue(text, text.contains("południu") && text.contains("parterze"));
        text = ask("potwory"); assertTrue(text, text.contains("Sabałą") && text.contains("potwór"));
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
        assertTrue(ask("co dalej").contains("Staska"));
        for (String slot : new String[] {"trail_delivery", "trail_guest", "trail_seal", "trail_debt"}) player.setQuest(slot, "done");
        assertTrue(ask("co dalej").contains("Drilenun"));
        for (String slot : new String[] {"trail_semos_letter", "trail_semos_sign", "trail_semos_pickup"}) player.setQuest(slot, "done");
        assertTrue(ask("co dalej").contains("Drilenun"));
        for (String slot : new String[] {"radan_passage", "radan_rescue", "radan_transport", "radan_finale"}) player.setQuest(slot, "done");
        String mineAdvice = ask("co dalej");
        assertTrue(mineAdvice, mineAdvice.contains("Drilenun"));
        assertFalse(mineAdvice, mineAdvice.contains("30"));
        for (String slot : new String[] {"trail_mine_ledger", "trail_mine_tracks", "trail_mine_voices", "trail_mine_dispatch"}) player.setQuest(slot, "done");
        assertTrue(ask("co dalej").contains("Barbarus"));
        for (String slot : new String[] {"trail_missing_miners", "trail_threatened_witnesses", "trail_forest_meeting", "trail_hidden_weapons", "trail_before_departure", "trail_news_for_towns"}) player.setQuest(slot, "done");
        assertTrue(ask("co dalej").contains("Julius"));
        for (String slot : new String[] {"trail_keep_tracks", "trail_keep_witnesses", "trail_keep_inscriptions", "trail_keep_guardian", "trail_keep_road"}) player.setQuest(slot, "done");
        assertTrue(ask("co dalej").contains("Wagabundus"));
        for (String slot:new String[]{"trail_oath_watch","trail_oath_names","trail_oath_grave","trail_oath_truth","trail_oath_banner","trail_oath_burial"}) player.setQuest(slot,"done");
        assertTrue(ask("co dalej").contains("Mark"));
        player.setQuest("trail_warsaw_gate","done");assertTrue(ask("co dalej").contains("księcia"));
        player.setQuest("trail_warsaw_pay","done");assertTrue(ask("co dalej").contains("Mark"));
        player.setQuest("trail_warsaw_witnesses","done");assertTrue(ask("co dalej").contains("Dobrosława"));
        player.setQuest("trail_warsaw_fire","done");assertTrue(ask("co dalej").contains("księcia"));
        player.setQuest("trail_warsaw_steward","done");assertTrue(ask("co dalej").contains("Mark"));
        player.setQuest("trail_warsaw_truce","done");
        assertTrue(ask("co dalej").contains("Staszek"));
        player.setQuest("trail_coast_wreck","done");assertTrue(ask("co dalej").contains("Lubor"));
        player.setQuest("trail_coast_lights","done");assertTrue(ask("co dalej").contains("Malteis"));
        player.setQuest("trail_coast_cargo","done");assertTrue(ask("co dalej").contains("Malteis"));
        player.setQuest("trail_coast_wreckers","done");assertTrue(ask("co dalej").contains("Lubor"));
        player.setQuest("trail_coast_lamps","done");assertTrue(ask("co dalej").contains("Staszek"));
        player.setQuest("trail_coast_return","done");
        assertTrue(ask("co dalej").contains("Zbigniew"));
        player.setQuest("trail_salt_shift","done");assertTrue(ask("co dalej").contains("Radost"));
        player.setQuest("trail_salt_rescue","done");assertTrue(ask("co dalej").contains("Radost"));
        player.setQuest("trail_salt_sample","done");assertTrue(ask("co dalej").contains("Agrypin"));
        player.setQuest("trail_salt_source","done");assertTrue(ask("co dalej").contains("Radost"));
        player.setQuest("trail_salt_creature","done");assertTrue(ask("co dalej").contains("Radost"));
        player.setQuest("trail_salt_return","done");
        assertTrue(ask("co dalej").contains("Gościrad"));
        assertEquals("done", player.getQuest("meet_pietrek"));
        assertEquals(0, player.getTotalNumberOf("puklerz"));
    }
}
