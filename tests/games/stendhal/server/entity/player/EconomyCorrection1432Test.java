package games.stendhal.server.entity.player;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.transformer.PlayerTransformer;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.common.game.DetailLevel;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.PlayerTestHelper;

/** Exercise correction through the real saved-character login path. */
public class EconomyCorrection1432Test {
    private static final String RECORD = "economy_correction_1432";

    @BeforeClass public static void setup() {
        MockStendlRPWorld.get();
        SingletonRepository.getEntityManager();
    }

    private Player fixture(final String name) {
        // Do not run the targeted login until its pre-patch inventory is ready.
        Player player = PlayerTestHelper.createPlayer("correction-fixture");
        player.setName(name);
        return player;
    }

    private StackableItem coins(final String name, final int quantity) {
        StackableItem item = (StackableItem) SingletonRepository.getEntityManager().getItem(name);
        item.setQuantity(quantity);
        return item;
    }

    private Player login(final Player player) {
        return (Player) new PlayerTransformer().transform(new RPObject(player));
    }

    private long total(final RPObject owner) {
        long amount = 0;
        for (RPSlot slot : owner.slots()) {
            for (RPObject object : slot) {
                if (object instanceof StackableItem) {
                    StackableItem item = (StackableItem) object;
                    int unit = "money".equals(item.getName()) || "miedziak".equals(item.getName()) ? 1
                            : "talar".equals(item.getName()) ? 100 : "dukat".equals(item.getName()) ? 10000 : 0;
                    amount += (long) unit * item.getQuantity();
                }
                amount += total(object);
            }
        }
        return amount;
    }

    @Test public void leavesOtherPlayersAndTheirItemsUntouched() {
        Player player = fixture("UnlistedTrader");
        player.getSlot("bag").add(coins("money", 900000000));
        Player restored = login(player);
        assertEquals(900000000L, total(restored));
        assertFalse(restored.hasMap(RECORD));
    }

    @Test public void subtractsFrozenPotionAndOnionOverpaymentsForEachListedPlayer() {
        String[] names = {"Mroczny", "Zed Destiny", "Otylka"};
        int[] amounts = {286592902, 625450000, 327732};
        for (int index = 0; index < names.length; index++) {
            Player player = fixture(names[index]);
            player.getSlot("bag").add(coins("money", amounts[index] + 123));
            Player restored = login(player);
            assertEquals(names[index], 123L, total(restored));
            assertTrue(restored.hasMap(RECORD));
            assertEquals(String.valueOf(amounts[index]), restored.get(RECORD, "collected"));
            assertEquals("0", restored.get(RECORD, "forgiven"));
        }
    }

    @Test public void includesBankAndPouchButNeverConfiscatesPurchasedItems() {
        Player player = fixture("Otylka");
        player.getSlot("pouch").add(coins("money", 100000));
        player.getSlot("bank").add(coins("money", 227832));
        Item sword = SingletonRepository.getEntityManager().getItem("miecz");
        player.getSlot("bag").add(sword);
        Player restored = login(player);
        assertEquals(100L, total(restored));
        assertEquals(1, restored.getAllEquipped("miecz").size());
    }

    @Test public void clampsToZeroAndForgivesTheShortfallPermanently() {
        Player player = fixture("Zed Destiny");
        player.getSlot("bag").add(coins("money", 100));
        player.getSlot("bank").add(coins("money", 200));
        Player restored = login(player);
        assertEquals(0L, total(restored));
        assertTrue(restored.hasMap(RECORD));
        assertEquals("300", restored.get(RECORD, "collected"));
        assertEquals("625449700", restored.get(RECORD, "forgiven"));
        restored.getSlot("bank").add(coins("money", 5000));
        Player again = login(restored);
        assertEquals("Future earnings are not repayment of a debt", 5000L, total(again));
        assertEquals("300", again.get(RECORD, "collected"));
    }

    @Test public void emptyCharacterIsMarkedSoLaterEarningsAreNotTaken() {
        Player player = login(fixture("Mroczny"));
        assertTrue(player.hasMap(RECORD));
        assertEquals("0", player.get(RECORD, "collected"));
        player.getSlot("bag").add(coins("money", 500));
        assertEquals(500L, total(login(player)));
    }

    @Test public void persistedCorrectionSurvivesRepeatedLoginsWithoutDoubleDebit() {
        Player player = fixture("Mroczny");
        player.getSlot("bank").add(coins("money", 286593025));
        Player once = login(player);
        assertEquals(123L, total(once));
        assertEquals("286593025", once.get(RECORD, "before"));
        assertEquals("123", once.get(RECORD, "after"));
        Player twice = login(once);
        assertEquals(123L, total(twice));
        assertEquals(once.getMap(RECORD), twice.getMap(RECORD));
    }

    @Test public void sumsMultipleLargeStacksWithoutIntegerOverflow() {
        Player player = fixture("Mroczny");
        player.getSlot("bank").add(coins("money", Integer.MAX_VALUE));
        player.getSlot("bag").add(coins("money", Integer.MAX_VALUE));
        Player restored = login(player);
        assertEquals(4008374392L, total(restored));
    }

    @Test public void countsDifferentDenominationsAndReturnsExactChange() {
        Player player = fixture("Otylka");
        player.getSlot("bank").add(coins("dukat", 33));
        Player restored = login(player);
        assertEquals(2268L, total(restored));
        assertTrue(restored.hasMap(RECORD));
    }

    @Test public void oldVersionCompletionMarkerPreventsAnotherDebit() {
        Player player = fixture("Mroczny");
        // Older releases understand quests even if they discard a new audit map.
        player.setQuest(RECORD, "done;286592902");
        player.getSlot("bank").add(coins("money", 500));
        assertEquals(500L, total(login(player)));
    }

    @Test public void findsMoneyInNestedStorageWithoutRemovingItsContainer() {
        Player player = fixture("Otylka");
        Item container = SingletonRepository.getEntityManager().getItem("miecz");
        player.getSlot("bank").add(container);
        container.addSlot(new RPSlot("content"));
        container.getSlot("content").add(coins("money", 327832));
        EconomyCorrection1432.onLogin(player);
        assertEquals(100L, total(player));
        assertSame(container, player.getSlot("bank").iterator().next());
        assertEquals(100, ((StackableItem) container.getSlot("content").iterator().next()).getQuantity());
    }

    @Test public void unavailableCurrencyChangeLeavesEveryCoinAndItemIntactUntilRetry() {
        Player player = fixture("Otylka");
        player.getSlot("bank").setCapacity(1);
        player.getSlot("bag").setCapacity(1);
        StackableItem gold = coins("dukat", 10000000);
        player.getSlot("bank").add(gold);
        Item sword = SingletonRepository.getEntityManager().getItem("miecz");
        player.getSlot("bag").add(sword);
        while (!player.getSlot("bag").isFull()) {
            player.getSlot("bag").add(SingletonRepository.getEntityManager().getItem("miecz"));
        }
        EconomyCorrection1432.onLogin(player);
        assertEquals(100000000000L, total(player));
        assertSame(sword, player.getSlot("bag").iterator().next());
        assertSame(gold, player.getSlot("bank").iterator().next());
        assertFalse(player.hasMap(RECORD));
        assertFalse(player.hasQuest(RECORD));

        player.getSlot("bag").remove(sword.getID());
        player.getSlot("head").add(sword);
        EconomyCorrection1432.onLogin(player);
        assertEquals(99999672268L, total(player));
        assertTrue(player.hasMap(RECORD));
        assertEquals("327732", player.get(RECORD, "collected"));
    }

    @Test public void binarySavedCharacterRetainsCoinsAndCompletionRecordTogether() throws Exception {
        Player player = fixture("Mroczny");
        player.getSlot("bank").add(coins("money", 286593025));
        Player once = login(player);
        once.setID(new RPObject.ID(1432, "economy_correction_test"));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        once.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
        RPObject saved = new RPObject();
        saved.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
        assertTrue(saved.hasMap(RECORD));
        assertEquals("286592902", saved.get(RECORD, "collected"));
        Player again = (Player) new PlayerTransformer().transform(saved);
        assertEquals(123L, total(again));
        assertEquals(once.getMap(RECORD), again.getMap(RECORD));
    }

    @Test public void unexpectedItemsInsideACoinStackAreNeverDestroyed() {
        Player player = fixture("Otylka");
        StackableItem money = coins("money", 300);
        player.getSlot("bank").add(money);
        money.addSlot(new RPSlot("content"));
        Item sword = SingletonRepository.getEntityManager().getItem("miecz");
        money.getSlot("content").add(sword);
        EconomyCorrection1432.onLogin(player);
        assertEquals(300L, total(player));
        assertSame(money, player.getSlot("bank").iterator().next());
        assertSame(sword, money.getSlot("content").iterator().next());
        assertFalse(player.hasMap(RECORD));
    }

    @Test public void failingRecordWriteRollsBackRemovedAndReplacedStacksThenCanRetry() {
        Player player = new FailingRecordPlayer(fixture("Otylka"));
        StackableItem money = coins("money", 200000);
        StackableItem gold = coins("dukat", 20);
        player.getSlot("bank").add(money);
        player.getSlot("bank").add(gold);
        EconomyCorrection1432.onLogin(player);
        assertEquals(400000L, total(player));
        assertTrue(money.isContained());
        assertTrue(gold.isContained());
        assertEquals(200000, money.getQuantity());
        assertEquals(20, gold.getQuantity());
        assertEquals(2, player.getSlot("bank").size());
        assertFalse(player.has(RECORD, "complete"));
        assertTrue(player.getMap(RECORD).isEmpty());
        assertFalse(player.hasQuest(RECORD));
        EconomyCorrection1432.onLogin(player);
        assertEquals(72268L, total(player));
        assertTrue(player.hasMap(RECORD));
    }

    /** Only the persistence failure is injected; inventory/debit logic stays real. */
    private static final class FailingRecordPlayer extends Player {
        private boolean failOnce = true;
        private FailingRecordPlayer(final RPObject original) { super(original); }
        @Override public void put(final String map, final String key, final String value) {
            if (failOnce && RECORD.equals(map) && "before".equals(key)) {
                failOnce = false;
                throw new IllegalStateException("Injected audit record failure");
            }
            super.put(map, key, value);
        }
    }
}
