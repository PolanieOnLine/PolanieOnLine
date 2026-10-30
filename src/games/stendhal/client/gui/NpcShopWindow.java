package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GradientPaint;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
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
import javax.swing.border.Border;
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

    private static final Color BACKGROUND = new Color(37, 22, 12);
    private static final Color PANEL = new Color(59, 34, 18);
    private static final Color LIST_BACKGROUND = new Color(43, 24, 13);
    private static final Color ROW_SELECTED = new Color(83, 45, 20);
    private static final Color BORDER = new Color(114, 66, 28);
    private static final Color GOLD = new Color(242, 190, 96);
    private static final Color TEXT = new Color(255, 239, 209);
    private static final Color MUTED = new Color(215, 192, 158);
    private static final Color SUCCESS = new Color(165, 227, 153);
    private static final Color ERROR = new Color(242, 159, 147);

    private static final Map<String, Sprite> SPRITES = new HashMap<String, Sprite>();

    private static void paintWood(final Graphics2D g, final int width,
            final int height, final int shade) {
        final Style style = StyleUtil.getStyle();
        final Sprite wood = style == null
                ? SpriteStore.get().getSprite("data/gui/panel_wood.jpg")
                : style.getBackground();
        if (wood != null && wood.getWidth() > 0 && wood.getHeight() > 0) {
            for (int x = 0; x < width; x += wood.getWidth()) {
                for (int y = 0; y < height; y += wood.getHeight()) {
                    wood.draw(g, x, y);
                }
            }
        } else {
            g.setColor(PANEL);
            g.fillRect(0, 0, width, height);
        }
        g.setColor(new Color(20, 9, 3, shade));
        g.fillRect(0, 0, width, height);
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
            setBackground(new Color(33, 19, 10));
            setForeground(TEXT);
            setSelectionBackground(ROW_SELECTED);
            setSelectionForeground(TEXT);
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

        @Override
        protected void paintComponent(final Graphics graphics) {
            final Graphics2D g = (Graphics2D) graphics.create();
            paintWood(g, getWidth(), getHeight(), 0);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    /**
     * Uses the same wood sprite as the current game skin. Cards have only a
     * shallow engraved edge so that all the parts belong to the same window.
     */
    /** Uses the client's standard StyledPanelUI instead of custom textures. */
    private static final class WoodPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private WoodPanel(final LayoutManager layout, final int shade,
                final boolean framed) {
            super(layout);
            setOpaque(true);
        }
    }

    private static Border nativeInsetBorder(
            final int top, final int left, final int bottom, final int right) {
        final Style style = StyleUtil.getStyle();
        final Border frame = style == null
                ? BorderFactory.createLineBorder(BORDER)
                : style.getBorderDown();
        return BorderFactory.createCompoundBorder(frame,
                BorderFactory.createEmptyBorder(top, left, bottom, right));
    }

    private static final class PlainPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        private PlainPanel(final LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder());
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            // Transparent interior to keep the wood uninterrupted.
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
                protected void paintComponent(final Graphics graphics) {
                    if (selected || hovered) {
                        graphics.setColor(selected
                                ? new Color(105, 65, 31, 205)
                                : new Color(65, 40, 21, 155));
                        graphics.fillRect(0, 0, getWidth(), getHeight());
                    }
                    graphics.setColor(new Color(200, 145, 80, 55));
                    graphics.drawLine(10, getHeight() - 1, getWidth() - 10,
                            getHeight() - 1);
                    if (selected) {
                        graphics.setColor(GOLD);
                        graphics.fillRect(0, 0, 3, getHeight());
                        graphics.drawLine(5, 0, getWidth() - 5, 0);
                    }
                }
            };
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 9));
            final JLabel name = label(entry.name, 14, TEXT, true);
            name.setToolTipText(entry.name);
            final JLabel price = label(entry.price, 12, GOLD, true);
            final PlainPanel priceRow = new PlainPanel(new FlowLayout(
                    FlowLayout.LEFT, 4, 0));
            priceRow.add(new SmallIcon(
                    SpriteStore.get().getSprite("data/gui/goldencoin.png"), 17));
            priceRow.add(price);
            final PlainPanel description = new PlainPanel(
                    new GridLayout(2, 1, 0, 1));
            description.add(name);
            description.add(priceRow);
            if (entry.entity != null) {
                final ItemRarity rarity = entry.entity.getRarity();
                if (rarity != null) {
                    name.setText(entry.name + " · " + ClientLanguage.getText(rarity.getPolishDisplayName())
                            + (entry.entity.getRPObject().has("improve") ? " +" + entry.entity.getRPObject().get("improve") : ""));
                    name.setForeground(rarity == ItemRarity.COMMON ? TEXT : Color.decode(rarity.getColorHex()));
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

    private static final class SearchField extends JTextField {
        private static final long serialVersionUID = 1L;

        private SearchField() {
            setBackground(KTextEdit.getChatBackgroundColor());
            setForeground(TEXT);
            setCaretColor(GOLD);
            setSelectionColor(ROW_SELECTED);
            setSelectedTextColor(TEXT);
            setFont(getFont().deriveFont(13f));
            setBorder(nativeInsetBorder(3, 7, 3, 7));
            setToolTipText("Filtruj nazwy przedmiotów");
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            if (isFocusOwner()) {
                final Graphics2D focus = (Graphics2D) graphics.create();
                focus.setColor(GOLD);
                focus.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
                focus.dispose();
            }
            if (!getText().isEmpty() || isFocusOwner()) {
                return;
            }
            final Graphics2D g = (Graphics2D) graphics.create();
            g.setColor(MUTED);
            g.setFont(getFont());
            final FontMetrics metrics = g.getFontMetrics();
            g.drawString("Szukaj przedmiotu...", 12,
                    (getHeight() + metrics.getAscent() - metrics.getDescent()) / 2);
            g.dispose();
        }
    }

    private static final class ShopButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final boolean tab;

        private ShopButton(final String text, final boolean primary) {
            super(text);
            tab = "Kup".equals(text) || "Sprzedaj".equals(text);
            setFocusPainted(false);
            setMargin(new Insets(3, 8, 3, 8));
            if (primary) {
                setFont(getFont().deriveFont(Font.BOLD));
            }
        }

        private void setActive(final boolean active) {
            if (!tab) {
                return;
            }
            setFont(getFont().deriveFont(active ? Font.BOLD : Font.PLAIN));
            setForeground(active ? GOLD : TEXT);
            final Style style = StyleUtil.getStyle();
            final Border base = style == null
                    ? BorderFactory.createLineBorder(BORDER) : style.getBorder();
            setBorder(base);
        }
    }

    private final List<Entry> selling = new ArrayList<Entry>();
    private final List<Entry> buying = new ArrayList<Entry>();
    private final DefaultListModel<Entry> sellModel = new DefaultListModel<Entry>();
    private final DefaultListModel<Entry> buyModel = new DefaultListModel<Entry>();
    private final ShopList sellList = new ShopList(sellModel);
    private final ShopList buyList = new ShopList(buyModel);
    private final ShopButton buyTab = new ShopButton("Kup", false);
    private final ShopButton sellTab = new ShopButton("Sprzedaj", false);
    private final CardLayout cards = new CardLayout();
    private final JPanel catalog = solid(cards, LIST_BACKGROUND);
    private final SearchField search = new SearchField();
    private final JLabel merchant = label("Sklep", 17, GOLD, true);
    private final JLabel wallet = label("", 12, TEXT, true);
    private final JLabel itemCount = label("", 11, MUTED, false);
    private final JLabel selectedName = label("Wybierz przedmiot", 14, TEXT, true);
    private final JLabel unitPrice = label("", 13, GOLD, true);
    private final JLabel quote = label("", 13, SUCCESS, true);
    private final JTextArea information = new JTextArea(2, 1);
    private final ItemIcon preview = new ItemIcon();
    private final JTextField amount = new JTextField("1", 3);
    private final ShopButton minus = new ShopButton("-", false);
    private final ShopButton plus = new ShopButton("+", false);
    private final ShopButton request = new ShopButton("Kup teraz", true);
    private final JLabel itemDescription = new JLabel();
    private final JLabel stats = new JLabel();
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

    private static JPanel solid(final LayoutManager layout, final Color color) {
        return new PlainPanel(layout);
    }

    private static JLabel label(final String caption, final int size,
            final Color color, final boolean bold) {
        final JLabel result = new JLabel(caption);
        result.setForeground(color);
        result.setFont(result.getFont().deriveFont(bold ? Font.BOLD : Font.PLAIN,
                (float) size));
        return result;
    }

    private static JScrollPane scroll(final ShopList list) {
        final JScrollPane pane = new JScrollPane(list);
        pane.setBorder(StyleUtil.getStyle() == null
                ? BorderFactory.createLineBorder(BORDER)
                : StyleUtil.getStyle().getBorderDown());
        pane.getViewport().setBackground(LIST_BACKGROUND);
        pane.getViewport().setOpaque(false);
        pane.setOpaque(false);
        pane.setBackground(LIST_BACKGROUND);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        final JScrollBar bar = pane.getVerticalScrollBar();
        bar.setUnitIncrement(28);
        return pane;
    }

    private JComponent createContent() {
        final JPanel content = new JPanel(new BorderLayout(7, 7));
        content.setBorder(BorderFactory.createEmptyBorder(9, 10, 9, 10));
        content.setPreferredSize(new Dimension(740, 540));

        // One title bar, one search bar. The wood texture is shared by
        // all parts of the window rather than alternating unrelated panels.
        final PlainPanel north = new PlainPanel(new BorderLayout(0, 7));
        final JPanel heading = new JPanel(new BorderLayout(8, 0));
        heading.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        merchant.setFont(merchant.getFont().deriveFont(Font.BOLD, 14f));
        heading.add(merchant, BorderLayout.WEST);
        wallet.setFont(wallet.getFont().deriveFont(12f));
        heading.add(wallet, BorderLayout.EAST);
        north.add(heading, BorderLayout.NORTH);

        final JPanel searchBar = new JPanel(new BorderLayout(7, 0));
        searchBar.setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));
        searchBar.add(new SmallIcon(
                SpriteStore.get().getSprite("data/gui/loupe.png"), 26),
                BorderLayout.WEST);
        search.setPreferredSize(new Dimension(10, 28));
        searchBar.add(search, BorderLayout.CENTER);
        north.add(searchBar, BorderLayout.SOUTH);
        content.add(north, BorderLayout.NORTH);

        // The catalogue takes roughly 60 percent of the body, as in the
        // supplied shop reference.
        final WoodPanel left = new WoodPanel(
                new BorderLayout(0, 5), 34, true);
        left.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        final PlainPanel tabs = new PlainPanel(
                new FlowLayout(FlowLayout.LEFT, 5, 0));
        buyTab.setPreferredSize(new Dimension(94, 27));
        sellTab.setPreferredSize(new Dimension(94, 27));
        tabs.add(buyTab);
        tabs.add(sellTab);
        buyTab.addActionListener(event -> switchMode(false));
        sellTab.addActionListener(event -> switchMode(true));
        final PlainPanel listHeading = new PlainPanel(new BorderLayout(8, 0));
        itemCount.setFont(itemCount.getFont().deriveFont(Font.BOLD, 12f));
        listHeading.add(itemCount, BorderLayout.WEST);
        final PlainPanel catalogueHeader = new PlainPanel(
                new BorderLayout(0, 3));
        catalogueHeader.add(tabs, BorderLayout.NORTH);
        catalogueHeader.add(listHeading, BorderLayout.SOUTH);
        left.add(catalogueHeader, BorderLayout.NORTH);
        catalog.add(scroll(sellList), "buy");
        catalog.add(scroll(buyList), "sell");
        left.add(catalog, BorderLayout.CENTER);

        // Single engraved panel, with a clear image, description and price.
        final WoodPanel detail = new WoodPanel(
                new BorderLayout(0, 5), 63, true);
        detail.setPreferredSize(new Dimension(302, 0));
        detail.setBorder(nativeInsetBorder(6, 7, 7, 7));
        final JLabel section = label("Wybrany przedmiot", 13, TEXT, true);
        section.setHorizontalAlignment(SwingConstants.CENTER);
        section.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        final PlainPanel detailHeading = new PlainPanel(new BorderLayout(7, 0));
        detailHeading.add(preview, BorderLayout.WEST);
        detailHeading.add(section, BorderLayout.CENTER);
        detail.add(detailHeading, BorderLayout.NORTH);

        final PlainPanel identity = new PlainPanel(new BorderLayout(0, 5));

        final JPanel descriptionContent = new PlainPanel(null);
        descriptionContent.setLayout(new BoxLayout(descriptionContent, BoxLayout.PAGE_AXIS));
        selectedName.setAlignmentX(Component.LEFT_ALIGNMENT);
        stats.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
        itemDescription.setForeground(MUTED);
        descriptionContent.add(selectedName);
        descriptionContent.add(stats);
        descriptionContent.add(itemDescription);
        final JScrollPane descriptionScroll = new JScrollPane(descriptionContent);
        descriptionScroll.setBorder(BorderFactory.createEmptyBorder());
        descriptionScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        descriptionScroll.getViewport().setOpaque(false);
        descriptionScroll.setOpaque(false);
        descriptionScroll.getVerticalScrollBar().setUnitIncrement(18);
        identity.add(descriptionScroll, BorderLayout.CENTER);

        final PlainPanel priceFrame = new PlainPanel(new BorderLayout(0, 3));
        priceFrame.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        unitPrice.setHorizontalAlignment(SwingConstants.CENTER);
        unitPrice.setFont(unitPrice.getFont().deriveFont(Font.BOLD, 14f));
        priceFrame.add(unitPrice, BorderLayout.NORTH);
        quote.setHorizontalAlignment(SwingConstants.CENTER);
        quote.setForeground(SUCCESS);
        quote.setVisible(true);
        priceFrame.add(quote, BorderLayout.SOUTH);
        identity.add(priceFrame, BorderLayout.SOUTH);
        detail.add(identity, BorderLayout.CENTER);

        final PlainPanel controls = new PlainPanel(new BorderLayout(0, 5));
        final PlainPanel quantity = new PlainPanel(new FlowLayout(
                FlowLayout.CENTER, 7, 0));
        quantity.add(label("Ilość", 13, TEXT, true));
        minus.setPreferredSize(new Dimension(32, 30));
        plus.setPreferredSize(new Dimension(32, 30));
        amount.setPreferredSize(new Dimension(64, 30));
        amount.setHorizontalAlignment(SwingConstants.CENTER);
        amount.setBackground(KTextEdit.getChatBackgroundColor());
        amount.setOpaque(true);
        amount.setBorder(nativeInsetBorder(2, 4, 2, 4));
        amount.setForeground(TEXT);
        amount.setCaretColor(GOLD);
        amount.setSelectionColor(ROW_SELECTED);
        amount.setSelectedTextColor(TEXT);
        amount.setFont(amount.getFont().deriveFont(Font.BOLD, 14f));
        amount.setToolTipText("Ilość od 1 do 1000");
        minus.addActionListener(event -> changeAmount(-1));
        plus.addActionListener(event -> changeAmount(1));
        quantity.add(minus);
        quantity.add(amount);
        quantity.add(plus);
        controls.add(quantity, BorderLayout.NORTH);
        final JPanel quick = new PlainPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        for (final String caption : new String[] {"1", "10", "100", "Maks"}) {
            final JButton preset = new JButton(ClientLanguage.getText(caption));
            preset.setMargin(new Insets(2, 5, 2, 5));
            preset.addActionListener(event -> amount.setText(Integer.toString("Maks".equals(caption)
                    ? maximum() : Math.min(maximum(), Integer.parseInt(caption)))));
            presets.add(preset); quick.add(preset);
        }
        controls.add(quick, BorderLayout.CENTER);
        request.setPreferredSize(new Dimension(10, 36));
        controls.add(request, BorderLayout.SOUTH);
        detail.add(controls, BorderLayout.SOUTH);

        final PlainPanel body = new PlainPanel(new BorderLayout(10, 0));
        body.add(left, BorderLayout.CENTER);
        body.add(detail, BorderLayout.EAST);
        content.add(body, BorderLayout.CENTER);

        final JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 9));
        information.setLineWrap(true);
        information.setWrapStyleWord(true);
        information.setEditable(false);
        information.setFocusable(false);
        information.setOpaque(false);
        information.setForeground(MUTED);
        information.setFont(information.getFont().deriveFont(12f));
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
        if (entry == null) { return 0; }
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
        buyTab.setActive(!sellingMode);
        sellTab.setActive(sellingMode);
        buyTab.setEnabled(!selling.isEmpty() && !waiting);
        sellTab.setEnabled(!buying.isEmpty() && !waiting);
        cards.show(catalog, sellingMode ? "sell" : "buy");
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
        merchant.setText(event.get("npc_name"));
        wallet.setText("Posiadasz: " + event.get("owned_money_text"));
        windowId = event.has("window_id") ? event.get("window_id") : "";
        money = event.has("owned_money_value") ? Long.parseLong(event.get("owned_money_value")) : 0;
        priceFactor = event.has("buying_penalty") ? Double.parseDouble(event.get("buying_penalty")) : 1;
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
            setStatus(message.length() > 0 ? message
                    : "Wybierz przedmiot, ustaw ilość i kup lub sprzedaj.",
                    error ? ERROR : success ? SUCCESS : MUTED);
        }
        applying = false;
        updateTabs();
        refreshDetails();
        revalidate();
        repaint();
    }

    private void setStatus(final String message, final Color color) {
        information.setText(message);
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
                : "<html><center>" + entry.name.replace("&", "&amp;")
                        .replace("<", "&lt;").replace(">", "&gt;")
                        + "</center></html>");
        selectedName.setToolTipText(entry == null ? null : entry.name);
        final String html = entry == null || entry.entity == null ? null : ItemRarityPresentation.buildItemToolTip(entry.entity, 250);
        stats.setText(html == null ? "" : html);
        selectedName.setVisible(html == null);
        itemDescription.setText("<html><table width='258'><tr><td>" + escape(entry == null
                ? "Wybierz przedmiot, aby zobaczyć szczegóły." : entry.description) + "</td></tr></table></html>");
        unitPrice.setText(entry == null ? "Brak wybranego przedmiotu"
                : sellingMode ? "Cena za sztukę: " + entry.price
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

    private void refreshButtons() {
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
        amount.setEnabled(!waiting && current != null);
        minus.setEnabled(!waiting && current != null && quantity > 1);
        plus.setEnabled(!waiting && current != null
                && quantity < maximum());
        for (final JButton preset : presets) { preset.setEnabled(!waiting && current != null && current.stackable && maximum() > 0); }
        quote.setVisible(current != null);
        quote.setText(current == null ? "" : "Łącznie: " + formatMoney(total, currencyReformed));
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
        setStatus("Oczekiwanie na odpowiedź handlarza...", MUTED);
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
