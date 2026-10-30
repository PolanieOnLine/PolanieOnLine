package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.BoxLayout;
import javax.swing.Timer;
import javax.swing.JTabbedPane;
import org.json.simple.JSONValue;
import games.stendhal.client.entity.Item;
import games.stendhal.client.ClientLanguage;
import marauroa.common.game.RPObject;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.common.constants.Actions;
import games.stendhal.common.constants.ItemRarity;
import java.util.Set;
import java.util.LinkedHashSet;
import games.stendhal.common.grammar.Grammar;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;

/**
 * Merchant window opened from the NPC context menu.
 * The server provides the catalogue and confirms every transaction.
 */
public final class NpcShopWindow extends InternalManagedWindow {
    private static final long serialVersionUID = 1L;

    private static final Color SUCCESS = new Color(92, 190, 105);
    private static final Color ERROR = new Color(225, 90, 75);
    private static final Map<String, Sprite> SPRITES = new HashMap<String, Sprite>();

    private static Color textColor() {
        final Style style = StyleUtil.getStyle();
        return style == null ? new JLabel().getForeground() : style.getForeground();
    }

    private static NpcShopWindow instance;

    private static final class Entry {
        private String key;
        private int limit;
        private Item entity;
        private final String name;
        private final String price;
        private final String itemClass;
        private final String subclass;
        private final boolean stackable;
        private final String description;
        private final long priceValue;

        private Entry(final String name, final String price,
                final String itemClass, final String subclass,
                final boolean stackable, final String description,
                final long priceValue) {
            this.name = name; this.key = name; this.limit = stackable ? 1000 : 1;
            this.price = price;
            this.itemClass = itemClass;
            this.subclass = subclass;
            this.stackable = stackable;
            this.description = description;
            this.priceValue = priceValue;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static final class ShopList extends JList<Entry> {
        private static final long serialVersionUID = 1L;
        private int hovered = -1;

        private ShopList(final DefaultListModel<Entry> model) {
            super(model);
            setOpaque(false);
            setFixedCellHeight(46);
            setVisibleRowCount(6);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            setCellRenderer(new RowRenderer());
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseMoved(final MouseEvent event) {
                    final int index = locationToIndex(event.getPoint());
                    final Rectangle bounds = index < 0 ? null : getCellBounds(index, index);
                    final int next = bounds != null && bounds.contains(event.getPoint())
                            ? index : -1;
                    if (next != hovered) {
                        hovered = next;
                        repaint();
                    }
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(final MouseEvent event) {
                    hovered = -1;
                    repaint();
                }
            });
        }

    }

    private static final class RowRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(final JList<?> list,
                final Object value, final int index, final boolean selected,
                final boolean focused) {
            final Entry entry = (Entry) value;
            final boolean hovered = ((ShopList) list).hovered == index;
            final JPanel row = new JPanel(new BorderLayout(10, 0)) {
                private static final long serialVersionUID = 1L;

                @Override
                public void paint(final Graphics graphics) {
                    final Graphics2D copy = (Graphics2D) graphics.create();
                    if (entry.limit == 0) { copy.setComposite(AlphaComposite.SrcOver.derive(0.5f)); }
                    super.paint(copy);
                    copy.dispose();
                }

                @Override
                protected void paintComponent(final Graphics graphics) {
                    if (selected || hovered) {
                        graphics.setColor(selected
                                ? new Color(255, 255, 255, 31)
                                : new Color(255, 255, 255, 15));
                        graphics.fillRect(0, 0, getWidth(), getHeight());
                    }
                }
            };
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 9));
            final JLabel name = new JLabel(entry.name);
            name.setToolTipText(entry.name);
            final JLabel price = new JLabel(entry.price);
            final DialogPanel priceRow = new DialogPanel(new FlowLayout(
                    FlowLayout.LEFT, 4, 0));
            priceRow.add(new SmallIcon(
                    SpriteStore.get().getSprite("data/gui/goldencoin.png"), 17));
            priceRow.add(price);
            final DialogPanel description = new DialogPanel(
                    new GridLayout(2, 1, 0, 1));
            description.add(name);
            description.add(priceRow);
            if (entry.entity != null) {
                final ItemRarity rarity = entry.entity.getRarity();
                if (rarity != null) {
                    name.setText(entry.name + " · " + ClientLanguage.getText(rarity.getPolishDisplayName())
                            + (entry.entity.getRPObject().has("improve") ? " +" + entry.entity.getRPObject().get("improve") : ""));
                    name.setForeground(rarity == ItemRarity.COMMON ? textColor() : Color.decode(rarity.getColorHex()));
                }
            }
            row.add(new ItemBadge(spriteFor(entry)), BorderLayout.WEST);
            row.add(description, BorderLayout.CENTER);
            return row;
        }
    }

    private static final class SmallIcon extends JComponent {
        private static final long serialVersionUID = 1L;
        private final Sprite sprite;

        private SmallIcon(final Sprite sprite, final int size) {
            this.sprite = sprite;
            setPreferredSize(new Dimension(size, size));
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            if (sprite == null) {
                return;
            }
            sprite.draw(graphics, (getWidth() - sprite.getWidth()) / 2,
                    (getHeight() - sprite.getHeight()) / 2);
        }
    }

    /** Draws the original equipment/bag slot instead of a custom tile. */
    private static final class ItemBadge extends JComponent {
        private static final long serialVersionUID = 1L;
        private static final Sprite SLOT =
                SpriteStore.get().getSprite("data/gui/slot.png");
        private final Sprite sprite;

        private ItemBadge(final Sprite sprite) {
            this.sprite = sprite;
            final Dimension size = new Dimension(SLOT.getWidth(), SLOT.getHeight());
            setPreferredSize(size);
            setMinimumSize(size);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            SLOT.draw(graphics, 0, 0);
            if (sprite != null) {
                sprite.draw(graphics, (SLOT.getWidth() - sprite.getWidth()) / 2,
                        (SLOT.getHeight() - sprite.getHeight()) / 2);
            }
        }
    }

    private final List<Entry> selling = new ArrayList<Entry>();
    private final List<Entry> buying = new ArrayList<Entry>();
    private final DefaultListModel<Entry> sellModel = new DefaultListModel<Entry>();
    private final DefaultListModel<Entry> buyModel = new DefaultListModel<Entry>();
    private final ShopList sellList = new ShopList(sellModel);
    private final ShopList buyList = new ShopList(buyModel);
    private final JTabbedPane tabs = new JTabbedPane();
    private final JTextField search = new JTextField();
    private final JLabel wallet = new JLabel();
    private final JLabel itemCount = new JLabel();
    private final JLabel selectedName = new JLabel("Wybierz przedmiot");
    private final JLabel selectedRarity = new JLabel();
    private final JLabel unitPrice = new JLabel();
    private final JLabel quote = new JLabel();
    private final JTextArea information = new JTextArea(2, 1);
    private final ItemIcon preview = new ItemIcon();
    private final JTextField amount = new JTextField("1", 3);
    private JPanel quantityControls;
    private JPanel quantityPresets;
    private final JButton minus = new JButton("-");
    private final JButton plus = new JButton("+");
    private final JButton request = new JButton("Kup teraz");
    private final JLabel itemDescription = new JLabel();
    private final JLabel stats = new JLabel();
    private final JLabel tradeRequirement = new JLabel();
    private String purchaseRequirement = "";
    private String saleRequirement = "";
    private final List<JButton> presets = new ArrayList<>();
    private String windowId = "";
    private long sequence;
    private long lastRequest;
    private long money;
    private double priceFactor = 1;
    private boolean refreshPending;
    private final Timer polling = new Timer(2000, event -> { if (!this.waiting && !this.refreshPending) send("refresh", null, null, 0); });
    private final Timer animation = new Timer(100, event -> { sellList.repaint(); buyList.repaint(); preview.repaint(); });
    private static final Set<String> CLOSED = new LinkedHashSet<>();
    private boolean currencyReformed;

    private int npcId;
    private boolean sellingMode;
    private boolean applying;
    private boolean waiting;

    private NpcShopWindow() {
        super("npc-shop", "Sklep");
        setCloseable(true);
        setMinimizable(false);
        setContent(createContent());
        addCloseListener(new CloseListener() {
            @Override
            public void windowClosed(final InternalWindow window) {
                send("close", null, null, 0);
                polling.stop(); animation.stop(); CLOSED.add(windowId);
                while (CLOSED.size() > 64) { CLOSED.remove(CLOSED.iterator().next()); }
                instance = null;
            }
        });
    }

    public static void show(final RPEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (event.has("window_id") && CLOSED.contains(event.get("window_id"))) { return; }
                if (instance != null && event.has("window_id") && !instance.windowId.isEmpty()
                        && !instance.windowId.equals(event.get("window_id")) && !"open".equals(event.get("phase"))) { return; }
                if (instance == null) {
                    if (!event.has("phase") || !"open".equals(event.get("phase"))) {
                        return;
                    }
                    instance = new NpcShopWindow();
                    j2DClient.get().addWindow(instance);
                }
                if (instance.npcId != 0 && event.has("npc_id")
                        && instance.npcId != event.getInt("npc_id")
                        && !"open".equals(event.get("phase"))) {
                    return;
                }
                if (event.has("request_id") && !event.get("request_id").isEmpty()
                        && Long.parseLong(event.get("request_id")) < instance.lastRequest) { return; }
                instance.apply(event);
                instance.polling.start(); instance.animation.start();
                instance.setVisible(true);
                if ("open".equals(event.get("phase"))) { instance.raise(); }
            }
        });
    }

    public static void closeOnZoneChange() {
        SwingUtilities.invokeLater(() -> { if (instance != null) { instance.close(); } });
    }

    private static JScrollPane scroll(final ShopList list) {
        final JScrollPane pane = new JScrollPane(list);
        pane.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        pane.getViewport().setOpaque(false);
        pane.setOpaque(false);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        pane.getVerticalScrollBar().setUnitIncrement(24);
        return pane;
    }

    private JComponent createContent() {
        final JPanel content = new DialogPanel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        content.setPreferredSize(new Dimension(740, 480));
        wallet.setHorizontalAlignment(SwingConstants.RIGHT);
        content.add(wallet, BorderLayout.NORTH);

        final JPanel left = new DialogPanel(new BorderLayout(0, 6));
        left.setPreferredSize(new Dimension(270, 0));
        final JPanel searchRow = new DialogPanel(new BorderLayout(6, 0));
        searchRow.add(new JLabel(ClientLanguage.getText("Szukaj:")), BorderLayout.WEST);
        search.setToolTipText(ClientLanguage.getText("Szukaj przedmiotu"));
        search.setPreferredSize(new Dimension(10, 25));
        searchRow.add(search, BorderLayout.CENTER);
        left.add(searchRow, BorderLayout.NORTH);
        tabs.addTab(ClientLanguage.getText("Kup"), scroll(sellList));
        tabs.addTab(ClientLanguage.getText("Sprzedaj"), scroll(buyList));
        tabs.addChangeListener(event -> switchMode(tabs.getSelectedIndex() == 1));
        left.add(tabs, BorderLayout.CENTER);
        left.add(itemCount, BorderLayout.SOUTH);

        final JPanel detail = new DialogPanel(new BorderLayout(0, 8));
        final JPanel identity = new DialogPanel(new BorderLayout(8, 0));
        identity.add(preview, BorderLayout.WEST);
        identity.add(selectedName, BorderLayout.CENTER);
        identity.add(selectedRarity, BorderLayout.EAST);
        detail.add(identity, BorderLayout.NORTH);

        final JPanel descriptionContent = new DialogPanel(null);
        descriptionContent.setLayout(new BoxLayout(descriptionContent, BoxLayout.PAGE_AXIS));
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
        descriptionContent.add(itemDescription);
        descriptionContent.add(stats);
        final JScrollPane descriptionScroll = new JScrollPane(descriptionContent);
        descriptionScroll.setBorder(BorderFactory.createEmptyBorder());
        descriptionScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descriptionScroll.getViewport().setOpaque(false);
        descriptionScroll.setOpaque(false);
        descriptionScroll.getVerticalScrollBar().setUnitIncrement(16);
        detail.add(descriptionScroll, BorderLayout.CENTER);

        final JPanel controls = new DialogPanel(new BorderLayout(0, 6));
        quantityControls = new DialogPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        amount.setPreferredSize(new Dimension(42, 25));
        amount.setHorizontalAlignment(SwingConstants.CENTER);
        amount.setToolTipText(ClientLanguage.getText("Ilość od 1 do 1000"));
        minus.addActionListener(event -> changeAmount(-1));
        plus.addActionListener(event -> changeAmount(1));
        quantityControls.add(minus); quantityControls.add(amount); quantityControls.add(plus);
        quantityPresets = new DialogPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        for (final String caption : new String[] {"1", "10", "100", "Maks"}) {
            final JButton preset = new JButton(ClientLanguage.getText(caption));
            preset.addActionListener(event -> amount.setText(Integer.toString("Maks".equals(caption)
                    ? maximum() : Math.min(maximum(), Integer.parseInt(caption)))));
            presets.add(preset); quantityPresets.add(preset);
        }
        final JPanel checkout = new DialogPanel(new BorderLayout(8, 0));
        final JPanel prices = new DialogPanel(null);
        prices.setLayout(new BoxLayout(prices, BoxLayout.PAGE_AXIS));
        prices.add(unitPrice); prices.add(quote);
        final JPanel priceArea = new DialogPanel(new BorderLayout());
        priceArea.add(prices, BorderLayout.SOUTH);
        checkout.add(priceArea, BorderLayout.CENTER);
        final JPanel tradeActions = new DialogPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        quantityControls.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));
        tradeActions.add(quantityControls);
        tradeActions.add(request);
        final JPanel orderControls = new DialogPanel(new BorderLayout(0, 5));
        orderControls.add(quantityPresets, BorderLayout.NORTH);
        orderControls.add(tradeActions, BorderLayout.SOUTH);
        checkout.add(orderControls, BorderLayout.EAST);
        controls.add(checkout, BorderLayout.SOUTH);
        final JPanel footerControls = new DialogPanel(new BorderLayout(0, 8));
        tradeRequirement.setForeground(new Color(255, 190, 85));
        footerControls.add(tradeRequirement, BorderLayout.NORTH);
        footerControls.add(controls, BorderLayout.CENTER);
        detail.add(footerControls, BorderLayout.SOUTH);

        final JPanel body = new DialogPanel(new BorderLayout(8, 0));
        body.add(left, BorderLayout.WEST);
        body.add(DialogPanel.section(ClientLanguage.getText("Przedmiot"), detail), BorderLayout.CENTER);
        content.add(body, BorderLayout.CENTER);
        final JPanel footer = new DialogPanel(new BorderLayout());
        information.setLineWrap(true); information.setWrapStyleWord(true);
        information.setEditable(false); information.setFocusable(false); information.setOpaque(false);
        information.setFont(new JLabel().getFont()); information.setForeground(textColor());
        footer.add(information, BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);

        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent event) {
                filter();
            }
            @Override
            public void removeUpdate(final DocumentEvent event) {
                filter();
            }
            @Override
            public void changedUpdate(final DocumentEvent event) {
                filter();
            }
        });
        sellList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && !applying && !sellingMode) {
                amount.setText("1");
                refreshDetails();
            }
        });
        buyList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && !applying && sellingMode) {
                amount.setText("1");
                refreshDetails();
            }
        });
        request.addActionListener(event -> purchase());
        amount.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(final DocumentEvent event) {
                refreshButtons();
            }
            @Override
            public void removeUpdate(final DocumentEvent event) {
                refreshButtons();
            }
            @Override
            public void changedUpdate(final DocumentEvent event) {
                refreshButtons();
            }
        });
        updateTabs();
        refreshButtons();
        return content;
    }

    private static String escape(final String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static Item parseSnapshot(final String json) {
        final Object parsed = JSONValue.parse(json);
        if (!(parsed instanceof Map)) { return null; }
        final RPObject object = new RPObject();
        object.setRPClass("item");
        for (final Map.Entry<?, ?> field : ((Map<?, ?>) parsed).entrySet()) {
            if (field.getValue() instanceof Map) {
                for (final Map.Entry<?, ?> value : ((Map<?, ?>) field.getValue()).entrySet()) {
                    object.put(String.valueOf(field.getKey()), String.valueOf(value.getKey()), String.valueOf(value.getValue()));
                }

            } else { object.put(String.valueOf(field.getKey()), String.valueOf(field.getValue())); }
        }
        final Item item = new Item(); item.initialize(object); return item;
    }

    private int maximum() {
        final Entry entry = selected();
        if (entry == null || !requirement().isEmpty()) { return 0; }
        final double unit = entry.priceValue * (sellingMode ? 1 : priceFactor);
        int maximum = Math.min(1000, entry.limit);
        if (unit > 0) {
            final long budget = sellingMode ? Integer.MAX_VALUE : Math.min(Integer.MAX_VALUE, money);
            maximum = Math.min(maximum, (int) Math.min(1000, Math.ceil((budget + 1.0) / unit) - 1));
        }
        return Math.max(0, maximum);
    }

    private long total(final Entry entry, final int count) {
        return entry == null ? 0 : (long) (entry.priceValue * count * (sellingMode ? 1 : priceFactor));
    }

    private void changeAmount(final int change) {
        final Entry entry = selected();
        if (entry == null || waiting) {
            return;
        }
        final int oldValue = readAmount();
        final int maximum = maximum();
        amount.setText(Integer.toString(
                Math.max(1, Math.min(maximum, oldValue + change))));
    }

    private final class ItemIcon extends JComponent {
        private static final long serialVersionUID = 1L;
        private Sprite sprite;
        private final Sprite slot =
                SpriteStore.get().getSprite("data/gui/slot.png");

        private ItemIcon() {
            setPreferredSize(new Dimension(42, 44));
            setMinimumSize(new Dimension(42, 44));
        }

        private void setEntry(final Entry entry) {
            sprite = spriteFor(entry);
            repaint();
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            final Graphics2D g = (Graphics2D) graphics.create();
            final double scale = 1.0;
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.translate((getWidth() - slot.getWidth() * scale) / 2,
                    (getHeight() - slot.getHeight() * scale) / 2);
            g.scale(scale, scale);
            slot.draw(g, 0, 0);
            if (sprite != null) {
                sprite.draw(g, (slot.getWidth() - sprite.getWidth()) / 2,
                        (slot.getHeight() - sprite.getHeight()) / 2);
            }
            g.dispose();
        }
    }

    private static Sprite spriteFor(final Entry entry) {
        if (entry == null || entry.itemClass.length() == 0
                || entry.subclass.length() == 0) {
            return null;
        }
        final String path = "data/sprites/items/" + entry.itemClass
                + "/" + entry.subclass + ".png";
        Sprite sprite = SPRITES.get(path);
        if (sprite == null) {
            final SpriteStore store = SpriteStore.get();
            final Sprite sheet = store.getSprite(path);
            sprite = sheet != null && sheet.getWidth() > 32 ? store.getAnimatedSprite(sheet, 0, 0, sheet.getWidth() / 32, 32, 32, 100) : sheet;
            if (sprite != null) {
                SPRITES.put(path, sprite);
            }
        }
        return sprite;
    }

    private int readAmount() {
        try {
            return Integer.parseInt(amount.getText().trim());
        } catch (final NumberFormatException ignored) {
            return 0;
        }
    }

    private void switchMode(final boolean toSelling) {
        if (waiting || sellingMode == toSelling) {
            return;
        }
        sellingMode = toSelling;
        amount.setText("1");
        updateTabs();
        refreshDetails();
    }

    private void updateTabs() {
        tabs.setEnabledAt(0, !selling.isEmpty() && !waiting);
        tabs.setEnabledAt(1, !buying.isEmpty() && !waiting);
        tabs.setSelectedIndex(sellingMode ? 1 : 0);
        final int count = sellingMode ? buying.size() : selling.size();
        final int visible = currentList().getModel().getSize();
        itemCount.setText(search.getText().trim().isEmpty()
                ? "Przedmioty: " + count : "Widoczne: " + visible + " z " + count);
    }

    private void apply(final RPEvent event) {
        applying = true;
        waiting = false;
        refreshPending = false;
        npcId = event.getInt("npc_id");
        setTitle(ClientLanguage.getText("Sklep") + " · " + event.get("npc_name"));
        wallet.setText("Posiadasz: " + event.get("owned_money_text"));
        windowId = event.has("window_id") ? event.get("window_id") : "";
        money = event.has("owned_money_value") ? Long.parseLong(event.get("owned_money_value")) : 0;
        priceFactor = event.has("buying_penalty") ? Double.parseDouble(event.get("buying_penalty")) : 1;
        purchaseRequirement = event.has("sell_requirement") ? event.get("sell_requirement") : "";
        saleRequirement = event.has("buy_requirement") ? event.get("buy_requirement") : "";
        final String previous = selected() == null ? null : selected().key;

        selling.clear();
        buying.clear();
        readEntries(event, "sell", selling);
        readEntries(event, "buy", buying);
        currencyReformed = event.has("currency_reformed")
                && event.getInt("currency_reformed") == 1;
        if ("open".equals(event.get("phase")) && ((sellingMode && buying.isEmpty())
                || (!sellingMode && selling.isEmpty()))) {
            sellingMode = selling.isEmpty() && !buying.isEmpty();
        }
        filter();
        final String selectedName = previous;
        if (selectedName != null) {
            final ShopList list = currentList();
            for (int i = 0; i < list.getModel().getSize(); i++) {
                if (selectedName.equals(list.getModel().getElementAt(i).key)) {
                    list.setSelectedIndex(i);

                    break;
                }
            }
        }
        final String message = event.has("message") ? event.get("message") : "";
        final boolean error = message.startsWith("Nie ")
                || message.startsWith("Nieprawidł")
                || message.startsWith("Oferta wygasła")
                || message.startsWith("Cena uległa")
                || message.contains("nie powiodła")
                || message.startsWith("Podejdź");
        final boolean success = "result".equals(event.get("phase"))
                && (message.startsWith("Zakup udany:")
                        || message.startsWith("Sprzedaż udana:"));
        if (!"refresh".equals(event.get("phase")) || !message.isEmpty()) {
            setStatus(message, error ? ERROR : success ? SUCCESS : textColor());
        }
        applying = false;
        updateTabs();
        refreshDetails();
        revalidate();
        repaint();
    }

    private void setStatus(final String message, final Color color) {
        information.setText(message);
        information.getParent().setVisible(!message.isEmpty());
        information.setForeground(color);
        information.setCaretPosition(0);
    }

    private static void readEntries(final RPEvent event, final String prefix,
            final List<Entry> target) {
        final List<String> names = values(event, prefix + "_names");
        final List<String> prices = values(event, prefix + "_prices");
        final List<String> classes = values(event, prefix + "_classes");
        final List<String> subclasses = values(event, prefix + "_subclasses");
                final List<String> descriptions = values(event, prefix + "_descriptions");
        final List<String> rawPrices = values(event, prefix + "_price_values");
        for (int i = 0; i < names.size(); i++) {
            long rawPrice = 0L;
            try {
                rawPrice = Long.parseLong(get(rawPrices, i));
            } catch (final NumberFormatException ignored) {
                // A legacy event may not provide numerical catalogue prices.
            }
            final Entry entry = new Entry(names.get(i), get(prices, i), get(classes, i), get(subclasses, i),
                    "1".equals(get(values(event, prefix + "_stackable"), i)), get(descriptions, i), rawPrice);
            entry.key = get(values(event, prefix + "_keys"), i);
            if (entry.key.isEmpty()) { entry.key = entry.name; }
            try { entry.limit = Integer.parseInt(get(values(event, prefix + "_quantities"), i)); }
            catch (final NumberFormatException ignored) { entry.limit = entry.stackable ? 1000 : 1; }
            entry.entity = parseSnapshot(get(values(event, prefix + "_items"), i));
            target.add(entry);
        }
    }

    private static List<String> values(final RPEvent event, final String name) {
        return event.has(name) ? event.getList(name) : new ArrayList<String>();
    }

    private static String get(final List<String> values, final int index) {
        return index < values.size() ? values.get(index) : "";
    }

    private void filter() {
        final String oldBuy = sellList.getSelectedValue() == null
                ? null : sellList.getSelectedValue().key;
        final String oldSell = buyList.getSelectedValue() == null
                ? null : buyList.getSelectedValue().key;
        final boolean wasApplying = applying;
        applying = true;
        fill(selling, sellModel, sellList, oldBuy);
        fill(buying, buyModel, buyList, oldSell);
        applying = wasApplying;
        if (!applying) {
            refreshDetails();
        }
    }

    private void fill(final List<Entry> source, final DefaultListModel<Entry> model,
            final ShopList list, final String previous) {
        final String term = search.getText().trim().toLowerCase(Locale.ROOT);
        model.clear();
        for (final Entry entry : source) {
            if (entry.name.toLowerCase(Locale.ROOT).contains(term)) {
                model.addElement(entry);
                if (entry.key.equals(previous)) {
                    list.setSelectedIndex(model.size() - 1);
                }
            }
        }
    }

    private ShopList currentList() {
        return sellingMode ? buyList : sellList;
    }

    private Entry selected() {
        return currentList().getSelectedValue();
    }

    private void refreshDetails() {
        final Entry entry = selected();
        selectedName.setText(entry == null ? "Wybierz przedmiot"
                : "<html>" + entry.name.replace("&", "&amp;")
                        .replace("<", "&lt;").replace(">", "&gt;")
                        + "</html>");
        selectedName.setToolTipText(entry == null ? null : entry.name);
        final ItemRarity rarity = entry == null || entry.entity == null ? null : entry.entity.getRarity();
        selectedRarity.setText(rarity == null ? "" : ClientLanguage.getText(rarity.getPolishDisplayName()));
        selectedRarity.setForeground(rarity == null || rarity == ItemRarity.COMMON
                ? textColor() : Color.decode(rarity.getColorHex()));
        final String html = entry == null || entry.entity == null ? null : ItemRarityPresentation.buildItemDetails(entry.entity, 360);
        stats.setText(html == null ? "" : html);
        selectedName.setVisible(true);
        selectedName.setForeground(entry != null && entry.entity != null && entry.entity.getRarity() != null
                && entry.entity.getRarity() != ItemRarity.COMMON ? Color.decode(entry.entity.getRarity().getColorHex()) : textColor());
        itemDescription.setText("<html><table width='360'><tr><td>" + escape(entry == null
                ? "Wybierz przedmiot, aby zobaczyć szczegóły." : entry.description) + "</td></tr></table></html>");
        unitPrice.setText(entry == null ? "" : entry.stackable
                ? "<html>Za sztukę: " + escape(entry.price) + "</html>"
                : "Cena: " + entry.price);
        preview.setEntry(entry);
        if (entry != null && !entry.stackable && readAmount() != 1) {
            amount.setText("1");
        }
        refreshButtons();
    }

    private static String formatMoney(final long value, final boolean reformed) {
        if (value > Integer.MAX_VALUE) {
            return "Zbyt wysoka kwota";
        }
        final int amount = (int) value;
        if (!reformed) {
            return amount + " " + Grammar.polishQuantity("money", amount);
        }
        final List<String> parts = new ArrayList<String>();
        final int dukaty = amount / 10000;
        final int talary = amount % 10000 / 100;
        final int miedziaki = amount % 100;
        if (dukaty > 0) {
            parts.add(dukaty + " " + Grammar.polishQuantity("dukat", dukaty));
        }
        if (talary > 0) {
            parts.add(talary + " " + Grammar.polishQuantity("talar", talary));
        }
        if (miedziaki > 0 || parts.isEmpty()) {
            parts.add(miedziaki + " "
                    + Grammar.polishQuantity("miedziak", miedziaki));
        }
        if (parts.size() == 1) {
            return parts.get(0);
        }
        if (parts.size() == 2) {
            return parts.get(0) + " i " + parts.get(1);
        }
        return parts.get(0) + ", " + parts.get(1) + " i " + parts.get(2);
    }

    private String requirement() {
        return sellingMode ? saleRequirement : purchaseRequirement;
    }

    private void refreshButtons() {
        final String requirement = requirement();
        tradeRequirement.setText("<html><table width='360'><tr><td>" + escape(requirement) + "</td></tr></table></html>");
        tradeRequirement.setVisible(!requirement.isEmpty());
        final Entry current = selected();
        final int quantity = readAmount();
        final long total = total(current, quantity);
        final boolean valid = current != null && quantity >= 1
                && quantity <= maximum()
                && (current.priceValue == 0 || total <= Integer.MAX_VALUE);
        request.setText(sellingMode ? "Sprzedaj teraz" : "Kup teraz");
        request.setEnabled(valid && !waiting);
        search.setEnabled(!waiting);
        sellList.setEnabled(!waiting);
        buyList.setEnabled(!waiting);
        amount.setEnabled(!waiting && current != null && maximum() > 0);
        minus.setEnabled(!waiting && current != null && quantity > 1);
        plus.setEnabled(!waiting && current != null
                && quantity < maximum());
        for (final JButton preset : presets) { preset.setEnabled(!waiting && current != null && current.stackable && maximum() > 0); }
        quantityControls.setVisible(current != null && current.stackable);
        quantityPresets.setVisible(current != null && current.stackable);
        quote.setVisible(current != null && current.stackable && quantity > 1);
        quote.setText(current == null ? "" : "<html>Łącznie: "
                + escape(formatMoney(total, currencyReformed)) + "</html>");
        updateTabs();
    }

    private void purchase() {
        final Entry entry = selected();
        if (entry == null || waiting) {
            return;
        }
        final int quantity = readAmount();
        if (quantity < 1 || quantity > maximum()
                || (entry.priceValue > 0
                        && total(entry, quantity) > Integer.MAX_VALUE)) {
            setStatus("Podaj poprawną ilość.", ERROR);
            return;
        }
        waiting = true;
        refreshButtons();
        setStatus("Oczekiwanie na odpowiedź handlarza...", textColor());
        send("purchase", sellingMode ? "sell" : "buy",
                entry.name, quantity);
    }

    private void send(final String command, final String mode,
            final String item, final int quantity) {
        if (npcId == 0) {
            return;
        }
        if ("refresh".equals(command)) { refreshPending = true; }
        final RPAction action = new RPAction();
        action.put("type", Actions.NPC_SHOP);
        action.put("command", command);
        action.put("npc_id", npcId);
        action.put("window_id", windowId);
        action.put("request_id", Long.toString(++sequence));
        lastRequest = sequence;
        if ("sell".equals(mode) && selected() != null) { action.put("selection", selected().key); }
        if (mode != null) {
            action.put("mode", mode);
        }
        if (item != null) {
            action.put("item", item);
            action.put("quantity", quantity);
        }
        StendhalClient.get().send(action);
    }
}
