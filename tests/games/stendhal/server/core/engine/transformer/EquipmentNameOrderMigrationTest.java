package games.stendhal.server.core.engine.transformer;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import org.junit.Test;

import games.stendhal.common.grammar.ItemParser;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.common.parser.ConversationParser;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.UpdateConverter;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;

/** Compatibility covers saved instances as well as old trade commands. */
public class EquipmentNameOrderMigrationTest {
    private static final String[][] NAMES = {
        {"lazurowy hełm", "hełm lazurowy"},
        {"lazurowy płaszcz smoczy", "płaszcz smoczy lazurowy"},
        {"lazurowy płaszcz elficki", "płaszcz elficki lazurowy"},
        {"prążkowany płaszcz lazurowy", "płaszcz prążkowany lazurowy"},
        {"szmaragdowy płaszcz smoczy", "płaszcz smoczy szmaragdowy"},
        {"szmaragdowa tarcza smocza", "tarcza smocza szmaragdowa"}
    };

    @Test public void oldNamesMigrateIdempotentlyIncludingQuestQuantities() {
        for (String[] pair : NAMES) {
            assertEquals(pair[1], UpdateConverter.updateItemName(pair[0]));
            assertEquals(pair[1], UpdateConverter.updateItemName(pair[0].replace(' ', '_')));
            assertEquals(pair[1] + "=7", UpdateConverter.updateItemName(pair[0] + "=7"));
            assertEquals(pair[1], UpdateConverter.updateItemName(pair[1]));
        }
    }

    @Test public void legacyFactoryAndTradeNamesResolveOnlyToTheSameOfferedItem() {
        EntityManager manager = SingletonRepository.getEntityManager();
        for (String[] pair : NAMES) {
            assertTrue(manager.isItem(pair[1]));
            assertEquals(pair[1], manager.getItem(pair[0]).getName());
            for (String name : pair) {
                ItemParserResult result = new ItemParser(pair[1]).parse(ConversationParser.parse("sell 2 " + name));
                assertTrue(name, result.wasFound());
                assertEquals(pair[1], result.getChosenItemName());
                assertEquals(2, result.getAmount());
            }
        }
        assertFalse(new ItemParser("polano").parse(ConversationParser.parse("sell lazurowy hełm")).wasFound());
    }

    @Test public void savedCountersAndItemQuestProgressAreMergedWithoutBeingReset() {
        Player player = PlayerTestHelper.createPlayer("name-order-progress");
        RPObject record = new RPObject(player);
        for (String[] pair : NAMES) {
            for (String prefix : new String[] {"", "obtained.", "bought.", "sold.", "improved."}) {
                record.put("looted_items", prefix + pair[0], 2);
                record.put("looted_items", prefix + pair[1], 3);
            }
        }
        for (int login = 0; login < 2; login++) {
            UpdateConverter.updatePlayerRPObject(record);
            for (String[] pair : NAMES) {
                for (String prefix : new String[] {"", "obtained.", "bought.", "sold.", "improved."}) {
                    assertEquals(5, record.getInt("looted_items", prefix + pair[1]));
                    assertFalse(record.has("looted_items", prefix + pair[0]));
                }
            }
        }
        for (String[] pair : NAMES) {
            player.setQuest("name_order_item", "start;" + pair[0] + "=2");
            UpdateConverter.updateQuests(player);
            assertEquals("start;" + pair[1] + "=2", player.getQuest("name_order_item"));
        }
    }

    @Test public void persistentRenamedItemsKeepTheirInstanceStateAfterRepeatedLogins() throws Exception {
        int identifier = 17001;
        for (String[] pair : NAMES) {
            Item item = SingletonRepository.getEntityManager().getItem(pair[1], ItemRarity.EPIC);
            assertNotNull(pair[1], item);
            final RPObject.ID identity = new RPObject.ID(identifier++, "name-order-restore");
            item.setID(identity);
            item.setPersistent(true); item.put("name", pair[0]); item.put("improve", 2);
            item.setBoundTo("name-order-owner"); item.setItemData("personal-note");
            final int defense = item.getInt("def");
            final Map<String, String> affixes = ItemAffixState.getValues(item);
            for (int login = 0; login < 2; login++) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                item.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
                RPObject saved = new RPObject();
                saved.readObject(new InputSerializer(new ByteArrayInputStream(bytes.toByteArray())));
                item = new ItemTransformer().transform(saved);
                assertNotNull(item); assertEquals(pair[1], item.getName());
                assertEquals(identity, item.getID());
                assertEquals(defense, item.getInt("def"));
                assertEquals("epic", item.get("rarity_id"));
                assertEquals(2, item.getUpgradeLevel());
                assertEquals("name-order-owner", item.get("bound"));
                assertEquals("personal-note", item.getItemData());
                assertEquals(affixes, ItemAffixState.getValues(item));
            }
        }
    }
}
