package games.stendhal.server.entity.npc.behaviour.journal;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.RPClassGenerator;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.behaviour.impl.BuyerBehaviour;
import games.stendhal.server.entity.npc.behaviour.impl.SellerBehaviour;

/** Testy rejestracji handlarzy wykorzystywanej przez nowe okno sklepu. */
public class MerchantsRegisterTest {
    @BeforeClass
    public static void createRPClasses() {
        new RPClassGenerator().createRPClassesWithoutBaking();
    }

    @Test
    public void merchantsWithTheSameNameKeepTheirOwnPriceLists() {
        final MerchantsRegister register = MerchantsRegister.get();
        final SpeakerNPC first = new SpeakerNPC("Testowy handlarz");
        final SpeakerNPC second = new SpeakerNPC("Testowy handlarz");
        final SellerBehaviour cheap = new SellerBehaviour(
                Collections.singletonMap("sztylecik", 100));
        final SellerBehaviour expensive = new SellerBehaviour(
                Collections.singletonMap("sztylecik", 300));

        register.add(first, cheap);
        register.add(second, expensive);

        assertSame(cheap, register.getSellerFor(first));
        assertSame(expensive, register.getSellerFor(second));
        assertNull(register.getBuyerFor(first));
    }

    @Test
    public void oneNpcMayBuyAndSellItems() {
        final MerchantsRegister register = MerchantsRegister.get();
        final SpeakerNPC npc = new SpeakerNPC("Testowy sklep dwustronny");
        final BuyerBehaviour buyer = new BuyerBehaviour(
                Collections.singletonMap("deska", 10));
        final SellerBehaviour seller = new SellerBehaviour(
                Collections.singletonMap("deska", 20));

        register.add(npc, buyer);
        register.add(npc, seller);

        assertSame(buyer, register.getBuyerFor(npc));
        assertSame(seller, register.getSellerFor(npc));
    }
}
