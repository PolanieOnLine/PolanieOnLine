package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GradientPaint;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.common.constants.Actions;
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
            this.name = name;
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
            setFixedCellHeight(61);
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

    private static javax.swing.border.Border nativeInsetBorder(
            final int top, final int left, final int bottom, final int right) {
        final Style style = StyleUtil.getStyle();
        final javax.swing.border.Border frame = style == null
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
                    new java.awt.GridLayout(2, 1, 0, 1));
            description.add(name);
            description.add(priceRow);
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

    private static final class ItemBadge extends JComponent {
        private static final long serialVersionUID = 1L;
        private final Sprite sprite;

        private ItemBadge(final Sprite sprite) {
            this.sprite = sprite;
            setPreferredSize(new Dimension(44, 44));
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            final Graphics2D g = (Graphics2D) graphics.create();
            g.setColor(new Color(175, 137, 92, 215));
            g.fillRoundRect(1, 1, 42, 42, 4, 4);
            g.setColor(new Color(92, 59, 30));
            g.drawRoundRect(1, 1, 42, 42, 5, 5);
            if (sprite != null) {
                sprite.draw(g, (getWidth() - sprite.getWidth()) / 2,
                        (getHeight() - sprite.getHeight()) / 2);
            }
            g.dispose();
        }
    }

    private static final class SearchField extends JTextField {
        private static final long serialVersionUID = 1L;

        private SearchField() {
            setBackground(new Color(31, 18, 10));
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
            setMargin(new java.awt.Insets(3, 8, 3, 8));
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
            final javax.swing.border.Border base = style == null
                    ? BorderFactory.createLineBorder(BORDER) : style.getBorder();
            setBorder(active ? BorderFactory.createCompoundBorder(base,
                    BorderFactory.createMatteBorder(0, 0, 2, 0, GOLD)) : base);
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
    private final ShopButton refresh = new ShopButton("Odśwież", false);
    private final JTextArea itemDescription = new JTextArea(3, 2);
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
                instance = null;
            }
        });
    }

    public static void show(final RPEvent event) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
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
                instance.apply(event);
                instance.setVisible(true);
                instance.raise();
            }
        });
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
        content.setPreferredSize(new Dimension(698, 448));

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
        left.setBorder(nativeInsetBorder(5, 6, 6, 6));
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
        detail.setPreferredSize(new Dimension(227, 0));
        detail.setBorder(nativeInsetBorder(6, 7, 7, 7));
        final JLabel section = label("Wybrany przedmiot", 13, TEXT, true);
        section.setHorizontalAlignment(SwingConstants.CENTER);
        section.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, GOLD));
        detail.add(section, BorderLayout.NORTH);

        final PlainPanel identity = new PlainPanel(new BorderLayout(0, 5));
        final PlainPanel imageRow = new PlainPanel(
                new FlowLayout(FlowLayout.CENTER, 0, 0));
        imageRow.add(preview);
        identity.add(imageRow, BorderLayout.NORTH);

        final PlainPanel nameAndDescription = new PlainPanel(
                new BorderLayout(0, 3));
        selectedName.setFont(selectedName.getFont().deriveFont(Font.BOLD, 15f));
        selectedName.setHorizontalAlignment(SwingConstants.CENTER);
        nameAndDescription.add(selectedName, BorderLayout.NORTH);
        itemDescription.setEditable(false);
        itemDescription.setFocusable(false);
        itemDescription.setOpaque(false);
        itemDescription.setLineWrap(true);
        itemDescription.setWrapStyleWord(true);
        itemDescription.setFont(itemDescription.getFont().deriveFont(12f));
        itemDescription.setForeground(MUTED);
        itemDescription.setText("Wybierz przedmiot, aby zobaczyć szczegóły.");
        itemDescription.setBorder(BorderFactory.createEmptyBorder(4, 4, 2, 4));
        nameAndDescription.add(itemDescription, BorderLayout.CENTER);
        identity.add(nameAndDescription, BorderLayout.CENTER);

        final PlainPanel priceFrame = new PlainPanel(new BorderLayout(0, 3));
        priceFrame.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        unitPrice.setHorizontalAlignment(SwingConstants.CENTER);
        unitPrice.setFont(unitPrice.getFont().deriveFont(Font.BOLD, 14f));
        priceFrame.add(unitPrice, BorderLayout.NORTH);
        quote.setHorizontalAlignment(SwingConstants.CENTER);
        quote.setForeground(SUCCESS);
        quote.setVisible(false);
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
        amount.setForeground(TEXT);
        amount.setCaretColor(GOLD);
        amount.setFont(amount.getFont().deriveFont(Font.BOLD, 14f));
        amount.setToolTipText("Ilość od 1 do 1000");
        minus.addActionListener(event -> changeAmount(-1));
        plus.addActionListener(event -> changeAmount(1));
        quantity.add(minus);
        quantity.add(amount);
        quantity.add(plus);
        controls.add(quantity, BorderLayout.NORTH);
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
        refresh.setPreferredSize(new Dimension(98, 32));
        footer.add(refresh, BorderLayout.EAST);
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
        refresh.addActionListener(event -> send("refresh", null, null, 0));
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

    private void changeAmount(final int change) {
        final Entry entry = selected();
        if (entry == null || waiting) {
            return;
        }
        final int oldValue = readAmount();
        final int maximum = entry.stackable ? 1000 : 1;
        amount.setText(Integer.toString(
                Math.max(1, Math.min(maximum, oldValue + change))));
    }

    private final class ItemIcon extends JComponent {
        private static final long serialVersionUID = 1L;
        private Sprite sprite;
        private final Sprite slot =
                SpriteStore.get().getSprite("data/gui/slot.png");

        private ItemIcon() {
            setPreferredSize(new Dimension(83, 84));
            setMinimumSize(new Dimension(83, 84));
        }

        private void setEntry(final Entry entry) {
            sprite = spriteFor(entry);
            repaint();
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            final Graphics2D g = (Graphics2D) graphics.create();
            final double scale = 1.7;
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
            sprite = SpriteStore.get().getSprite(path);
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
        npcId = event.getInt("npc_id");
        merchant.setText(event.get("npc_name"));
        wallet.setText("Posiadasz: " + event.get("owned_money_text"));
        final String previous = selected() == null ? null : selected().name;

        selling.clear();
        buying.clear();
        readEntries(event, "sell", selling);
        readEntries(event, "buy", buying);
        currencyReformed = event.has("currency_reformed")
                && event.getInt("currency_reformed") == 1;
        if ((sellingMode && buying.isEmpty())
                || (!sellingMode && selling.isEmpty())) {
            sellingMode = selling.isEmpty() && !buying.isEmpty();
        }
        filter();
        final String selectedName = previous;
        if (selectedName != null) {
            final ShopList list = currentList();
            for (int i = 0; i < list.getModel().getSize(); i++) {
                if (selectedName.equals(list.getModel().getElementAt(i).name)) {
                    list.setSelectedIndex(i);
                    list.ensureIndexIsVisible(i);
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
        setStatus(message.length() > 0 ? message
                : "Wybierz przedmiot, ustaw ilość i kup lub sprzedaj.",
                error ? ERROR : success ? SUCCESS : MUTED);
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
        final List<String> stackable = values(event, "sell_stackable");
        final List<String> descriptions = values(event, prefix + "_descriptions");
        final List<String> rawPrices = values(event, prefix + "_price_values");
        for (int i = 0; i < names.size(); i++) {
            long rawPrice = 0L;
            try {
                rawPrice = Long.parseLong(get(rawPrices, i));
            } catch (final NumberFormatException ignored) {
                // A legacy event may not provide numerical catalogue prices.
            }
            target.add(new Entry(names.get(i),
                    get(prices, i), get(classes, i), get(subclasses, i),
                    !"sell".equals(prefix) || "1".equals(get(stackable, i)),
                    get(descriptions, i), rawPrice));
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
                ? null : sellList.getSelectedValue().name;
        final String oldSell = buyList.getSelectedValue() == null
                ? null : buyList.getSelectedValue().name;
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
                if (entry.name.equals(previous)) {
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
        itemDescription.setText(entry == null
                ? "Wybierz przedmiot, aby zobaczyć szczegóły."
                : entry.description.isEmpty() ? "Brak dodatkowego opisu."
                        : entry.description);
        itemDescription.setCaretPosition(0);
        unitPrice.setText(entry == null ? "Brak wybranego przedmiotu"
                : sellingMode ? "Cena bazowa: " + entry.price
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
        final long total = current == null ? 0
                : current.priceValue * quantity;
        final boolean valid = current != null && quantity >= 1
                && quantity <= (current.stackable ? 1000 : 1)
                && (current.priceValue == 0 || total <= Integer.MAX_VALUE);
        request.setText(sellingMode ? "Sprzedaj teraz" : "Kup teraz");
        request.setEnabled(valid && !waiting);
        search.setEnabled(!waiting);
        sellList.setEnabled(!waiting);
        buyList.setEnabled(!waiting);
        amount.setEnabled(!waiting && current != null);
        minus.setEnabled(!waiting && current != null && quantity > 1);
        plus.setEnabled(!waiting && current != null
                && quantity < (current.stackable ? 1000 : 1));
        refresh.setEnabled(!waiting);
        if (current != null && quantity > 1 && valid && current.priceValue > 0) {
            quote.setText("<html><center>Razem orientacyjnie: "
                    + formatMoney(total, currencyReformed) + "</center></html>");
            quote.setVisible(true);
        } else {
            quote.setText("");
            quote.setVisible(false);
        }
        updateTabs();
    }

    private void purchase() {
        final Entry entry = selected();
        if (entry == null || waiting) {
            return;
        }
        final int quantity = readAmount();
        if (quantity < 1 || quantity > (entry.stackable ? 1000 : 1)
                || (entry.priceValue > 0
                        && entry.priceValue * quantity > Integer.MAX_VALUE)) {
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
        final RPAction action = new RPAction();
        action.put("type", Actions.NPC_SHOP);
        action.put("command", command);
        action.put("npc_id", npcId);
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