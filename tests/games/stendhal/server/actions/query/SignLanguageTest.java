package games.stendhal.server.actions.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.Translate;
import games.stendhal.server.entity.mapstuff.office.RentedSign;
import games.stendhal.server.entity.mapstuff.sign.Sign;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class SignLanguageTest {
    @BeforeClass public static void setUpClasses() {
        MockStendlRPWorld.get();
        PlayerTestHelper.generatePlayerRPClasses();
    }

    @Test public void authoredSignBodyUsesTheRequestedLanguageWithoutAnAmbientAction() {
        final Sign sign = new Sign();
        sign.setText("Witaj.");
        assertEquals("You read: \"Hello.\"", LookAction.getLookDescription(sign, "en"));
        assertEquals("Czytasz: \"Witaj.\"", LookAction.getLookDescription(sign, "pl"));
        assertEquals("Witaj.", sign.get("text"));
    }

    @Test public void multilineAuthoredSignTranslatesEachLineBeforeWrappingIt() {
        final Sign sign = new Sign();
        sign.setText("Witaj.\nWitaj.");
        assertEquals("You read:\n\"Hello.\nHello.\"", LookAction.getLookDescription(sign, "en"));
    }

    @Test public void pizzaDeliveryNoticeTranslatesItsAuthoredBody() {
        final Sign sign = new Sign();
        sign.setText("Potrzebna pomoc do roznoszenia pizzy.");
        Translate.setThreadLanguage("en");
        try {
            assertEquals("You read: \"Help wanted delivering pizzas.\"", LookAction.getLookDescription(sign, "en"));
            assertEquals("Potrzebna pomoc do roznoszenia pizzy.", sign.get("text"));
        } finally {
            Translate.setThreadLanguage(null);
        }
    }

    @Test public void rentedSignTextStaysVerbatimInAnEnglishAction() {
        final RentedSign sign = new RentedSign(PlayerTestHelper.createPlayer("Zamknij"), "Witaj.");
        Translate.setThreadLanguage("en");
        try {
            assertEquals("Witaj.", sign.getText());
            assertEquals("Witaj.", sign.getText("en"));
            assertTrue(LookAction.getLookDescription(sign, "en").contains("\"Witaj.\""));
            assertEquals("Witaj.", sign.get("text"));
            assertEquals("Zamknij", sign.getRenter());
        } finally {
            Translate.setThreadLanguage(null);
        }
    }

    @Test public void rentedMultilineSignDoesNotTranslatePlayerWrittenInnerLines() {
        final String body = "Witaj.\nWitaj.\nWitaj.";
        final RentedSign sign = new RentedSign(PlayerTestHelper.createPlayer("Witaj."), body);
        Translate.setThreadLanguage("en");
        try {
            assertTrue(LookAction.getLookDescription(sign, "en").contains("\"" + body + "\""));
        } finally {
            Translate.setThreadLanguage(null);
        }
    }
}
