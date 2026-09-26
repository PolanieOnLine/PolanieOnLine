package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.text.ParseException;
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
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ListSelectionModel;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.constants.Actions;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;

/**
 * Merchant window opened from the NPC context menu.
 * The server provides the catalogue and confirms every transaction.
 */
public final class NpcShopWindow extends InternalManagedWindow {
    private static final long serialVersionUID = 1L;

    private static final Color BACKGROUND = new Color(39, 28, 21);
    private static final Color PANEL = new Color(48, 35, 26);
    private static final Color LIST_BACKGROUND = new Color(34, 26, 21);
    private static final Color ROW_BACKGROUND = new Color(56, 41, 30);
    private static final Color ROW_ALTERNATE = new Color(62, 46, 34);
    private static final Color ROW_HOVER = new Color(75, 54, 36);
    private static final Color ROW_SELECTED = new Color(93, 63, 38);
    private static final Color BORDER = new Color(111, 83, 54);
    private static final Color GOLD = new Color(245, 195, 110);
    private static final Color TEXT = new Color(250, 237, 215);
    private static final Color MUTED = new Color(204, 185, 157);
    private static final Color SUCCESS = new Color(153, 215, 166);
    private static final Color ERROR = new Color(239, 159, 147);

    private static final Map<String, Sprite> SPRITES = new HashMap<String, Sprite>();
    private static NpcShopWindow instance;

    private static final class Entry {
        private final String name;
        private final String price;
        private final String itemClass;
        private final String subclass;
        private final boolean stackable;

        private Entry(final String name, final String price,
                final String itemClass, final String subclass,
                final boolean stackable) {
            this.name = name;
            this.price = price;
            this.itemClass = itemClass;
            this.subclass = subclass;
            this.stackable = stackable;
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
            setBackground(LIST_BACKGROUND);
            setForeground(TEXT);
            setSelectionBackground(ROW_SELECTED);
            setSelectionForeground(TEXT);
            setOpaque(true);
            setFixedCellHeight(59);
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
            final JPanel row = solid(new BorderLayout(9, 0), selected
                    ? ROW_SELECTED : ((ShopList) list).hovered == index
                            ? ROW_HOVER : index % 2 == 0 ? ROW_BACKGROUND : ROW_ALTERNATE);
            row.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, selected ? 3 : 0, 1, 0,
                            selected ? GOLD : LIST_BACKGROUND),
                    BorderFactory.createEmptyBorder(5, selected ? 6 : 9, 5, 8)));
            final JLabel name = label(entry.name, 13, TEXT, true);
            name.setToolTipText(entry.name);
            final JLabel price = label(entry.price, 12, GOLD, false);
            final JPanel description = solid(new java.awt.GridLayout(2, 1, 0, 2),
                    row.getBackground());
            description.add(name);
            description.add(price);
            row.add(new SmallIcon(spriteFor(entry)), BorderLayout.WEST);
            row.add(description, BorderLayout.CENTER);
            return row;
        }
    }

    private static final class SmallIcon extends JComponent {
        private static final long serialVersionUID = 1L;
        private final Sprite sprite;

        private SmallIcon(final Sprite sprite) {
            this.sprite = sprite;
            setPreferredSize(new Dimension(43, 43));
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            if (sprite != null) {
                sprite.draw(graphics, (getWidth() - sprite.getWidth()) / 2,
                        (getHeight() - sprite.getHeight()) / 2);
            }
        }
    }

    private static final class SearchField extends JTextField {
        private static final long serialVersionUID = 1L;

        private SearchField() {
            setBackground(LIST_BACKGROUND);
            setForeground(TEXT);
            setCaretColor(GOLD);
            setSelectionColor(ROW_SELECTED);
            setSelectedTextColor(TEXT);
            setFont(getFont().deriveFont(13f));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(7, 11, 7, 11)));
            setToolTipText("Filtruj nazwy przedmiotów");
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
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
        private final boolean primary;
        private boolean active;

        private ShopButton(final String caption, final boolean primary) {
            super(caption);
            this.primary = primary;
            setUI(new BasicButtonUI());
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
            setForeground(primary ? BACKGROUND : TEXT);
            setFont(getFont().deriveFont(Font.BOLD, 13f));
            setBorder(BorderFactory.createEmptyBorder(9, 12, 9, 12));
            setMargin(new java.awt.Insets(7, 11, 7, 11));
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        }

        private void setActive(final boolean active) {
            this.active = active;
            setForeground(active ? GOLD : primary ? BACKGROUND : TEXT);
            repaint();
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            final Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            final Color fill;
            if (!isEnabled()) {
                fill = new Color(70, 58, 47);
            } else if (active) {
                fill = new Color(78, 54, 34);
            } else if (getModel().isPressed()) {
                fill = primary ? new Color(205, 148, 70) : ROW_SELECTED;
            } else if (getModel().isRollover()) {
                fill = primary ? new Color(255, 207, 123) : ROW_HOVER;
            } else {
                fill = primary ? GOLD : ROW_BACKGROUND;
            }
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 7, 7);
            g.setColor(active ? GOLD : primary ? GOLD : BORDER);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 7, 7);
            if (active) {
                g.fillRoundRect(8, getHeight() - 4, getWidth() - 16, 3, 3, 3);
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static final class SlimScrollBarUI extends BasicScrollBarUI {
        @Override
        protected void paintTrack(final Graphics graphics, final JComponent component,
                final Rectangle bounds) {
            graphics.setColor(LIST_BACKGROUND);
            graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
        }

        @Override
        protected void paintThumb(final Graphics graphics, final JComponent component,
                final Rectangle bounds) {
            if (bounds.isEmpty()) {
                return;
            }
            graphics.setColor(new Color(135, 100, 62));
            graphics.fillRoundRect(bounds.x + 2, bounds.y + 2,
                    Math.max(2, bounds.width - 4), Math.max(2, bounds.height - 4), 6, 6);
        }

        @Override
        protected JButton createDecreaseButton(final int orientation) {
            return invisibleButton();
        }

        @Override
        protected JButton createIncreaseButton(final int orientation) {
            return invisibleButton();
        }

        private JButton invisibleButton() {
            final JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            return button;
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
    private final SpinnerNumberModel amountModel = new SpinnerNumberModel(1, 1, 1000, 1);
    private final JSpinner amount = new JSpinner(amountModel);
    private final ShopButton request = new ShopButton("Zapytaj o cenę", true);
    private final ShopButton confirm = new ShopButton("Potwierdź", true);
    private final ShopButton cancel = new ShopButton("Anuluj", false);
    private final ShopButton refresh = new ShopButton("Odśwież", false);
    private final JPanel actions = solid(new java.awt.GridLayout(0, 1, 0, 7), PANEL);

    private int npcId;
    private String requestToken;
    private String pendingMode;
    private String pendingItem;
    private int pendingAmount;
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
                send("close", null, null, 0, requestToken);
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
        final JPanel result = new JPanel(layout);
        result.setBackground(color);
        result.setOpaque(true);
        return result;
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
        pane.setBorder(BorderFactory.createLineBorder(BORDER));
        pane.getViewport().setBackground(LIST_BACKGROUND);
        pane.getViewport().setOpaque(true);
        pane.setBackground(LIST_BACKGROUND);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        final JScrollBar bar = pane.getVerticalScrollBar();
        bar.setPreferredSize(new Dimension(10, 0));
        bar.setUnitIncrement(28);
        bar.setUI(new SlimScrollBarUI());
        return pane;
    }

    private JComponent createContent() {
        final JPanel content = solid(new BorderLayout(12, 12), BACKGROUND);
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        content.setPreferredSize(new Dimension(710, 486));

        final JPanel header = solid(new BorderLayout(8, 10), BACKGROUND);
        final JPanel heading = solid(new BorderLayout(8, 0), BACKGROUND);
        heading.add(merchant, BorderLayout.WEST);
        wallet.setHorizontalAlignment(SwingConstants.RIGHT);
        wallet.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
        heading.add(wallet, BorderLayout.EAST);
        header.add(heading, BorderLayout.NORTH);
        header.add(search, BorderLayout.SOUTH);
        content.add(header, BorderLayout.NORTH);

        final JPanel tabBar = solid(new FlowLayout(FlowLayout.LEFT, 5, 0), BACKGROUND);
        buyTab.setPreferredSize(new Dimension(100, 34));
        sellTab.setPreferredSize(new Dimension(100, 34));
        tabBar.add(buyTab);
        tabBar.add(sellTab);
        buyTab.addActionListener(event -> switchMode(false));
        sellTab.addActionListener(event -> switchMode(true));

        catalog.add(scroll(sellList), "buy");
        catalog.add(scroll(buyList), "sell");
        final JPanel left = solid(new BorderLayout(0, 7), BACKGROUND);
        final JPanel topLeft = solid(new BorderLayout(5, 5), BACKGROUND);
        topLeft.add(tabBar, BorderLayout.NORTH);
        itemCount.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
        topLeft.add(itemCount, BorderLayout.SOUTH);
        left.add(topLeft, BorderLayout.NORTH);
        left.add(catalog, BorderLayout.CENTER);

        final JPanel detail = solid(new BorderLayout(0, 12), PANEL);
        detail.setPreferredSize(new Dimension(244, 0));
        detail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(14, 12, 12, 12)));

        final JPanel identity = solid(new BorderLayout(0, 8), PANEL);
        final JLabel detailHeading = label("Wybrany przedmiot", 11, MUTED, false);
        detailHeading.setHorizontalAlignment(SwingConstants.CENTER);
        identity.add(detailHeading, BorderLayout.NORTH);
        final JPanel identityBody = solid(new BorderLayout(0, 7), PANEL);
        final JPanel iconWrapper = solid(new FlowLayout(FlowLayout.CENTER, 0, 0), PANEL);
        iconWrapper.add(preview);
        identityBody.add(iconWrapper, BorderLayout.NORTH);
        final JPanel descriptions = solid(new java.awt.GridLayout(0, 1, 0, 5), PANEL);
        selectedName.setHorizontalAlignment(SwingConstants.CENTER);
        selectedName.setPreferredSize(new Dimension(210, 36));
        descriptions.add(selectedName);
        unitPrice.setHorizontalAlignment(SwingConstants.CENTER);
        descriptions.add(unitPrice);
        quote.setHorizontalAlignment(SwingConstants.CENTER);
        quote.setVisible(false);
        descriptions.add(quote);
        identityBody.add(descriptions, BorderLayout.CENTER);
        identity.add(identityBody, BorderLayout.CENTER);
        detail.add(identity, BorderLayout.NORTH);

        final JPanel quantity = solid(new FlowLayout(FlowLayout.CENTER, 10, 10), PANEL);
        quantity.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));
        quantity.add(label("Ilość", 13, TEXT, true));
        amount.setPreferredSize(new Dimension(80, 29));
        if (amount.getEditor() instanceof JSpinner.DefaultEditor) {
            final JFormattedTextField field =
                    ((JSpinner.DefaultEditor) amount.getEditor()).getTextField();
            field.setBackground(LIST_BACKGROUND);
            field.setForeground(TEXT);
            field.setCaretColor(GOLD);
            field.setFont(field.getFont().deriveFont(Font.BOLD, 13f));
            field.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        }
        quantity.add(amount);
        final JPanel center = solid(new BorderLayout(), PANEL);
        center.add(quantity, BorderLayout.NORTH);
        detail.add(center, BorderLayout.CENTER);

        actions.add(request);
        actions.add(confirm);
        actions.add(cancel);
        detail.add(actions, BorderLayout.SOUTH);

        final JPanel body = solid(new BorderLayout(12, 0), BACKGROUND);
        body.add(left, BorderLayout.CENTER);
        body.add(detail, BorderLayout.EAST);
        content.add(body, BorderLayout.CENTER);

        final JPanel bottom = solid(new BorderLayout(9, 0), PANEL);
        bottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 8)));
        information.setLineWrap(true);
        information.setWrapStyleWord(true);
        information.setEditable(false);
        information.setFocusable(false);
        information.setOpaque(false);
        information.setForeground(MUTED);
        information.setFont(information.getFont().deriveFont(12f));
        bottom.add(information, BorderLayout.CENTER);
        refresh.setPreferredSize(new Dimension(92, 36));
        bottom.add(refresh, BorderLayout.EAST);
        content.add(bottom, BorderLayout.SOUTH);

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
                refreshDetails();
            }
        });
        buyList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && !applying && sellingMode) {
                refreshDetails();
            }
        });
        request.addActionListener(event -> ask());
        confirm.addActionListener(event -> answer(true));
        cancel.addActionListener(event -> answer(false));
        refresh.addActionListener(event -> send("refresh", null, null, 0, null));
        amount.addChangeListener(event -> refreshButtons());
        updateTabs();
        refreshButtons();
        return content;
    }

    private final class ItemIcon extends JComponent {
        private static final long serialVersionUID = 1L;
        private Sprite sprite;
        private final Sprite slot =
                SpriteStore.get().getSprite("data/gui/slot.png");

        private ItemIcon() {
            setPreferredSize(new Dimension(84, 84));
            setMinimumSize(new Dimension(84, 84));
        }

        private void setEntry(final Entry entry) {
            sprite = spriteFor(entry);
            repaint();
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            slot.draw(graphics,
                    (getWidth() - slot.getWidth()) / 2,
                    (getHeight() - slot.getHeight()) / 2);
            if (sprite != null) {
                sprite.draw(graphics,
                        (getWidth() - sprite.getWidth()) / 2,
                        (getHeight() - sprite.getHeight()) / 2);
            }
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

    private void switchMode(final boolean toSelling) {
        if (waiting || requestToken != null || sellingMode == toSelling) {
            return;
        }
        sellingMode = toSelling;
        updateTabs();
        refreshDetails();
    }

    private void updateTabs() {
        buyTab.setActive(!sellingMode);
        sellTab.setActive(sellingMode);
        buyTab.setEnabled(!selling.isEmpty() && requestToken == null && !waiting);
        sellTab.setEnabled(!buying.isEmpty() && requestToken == null && !waiting);
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
        requestToken = event.has("request_token") ? event.get("request_token") : null;
        pendingMode = event.has("pending_mode") ? event.get("pending_mode") : null;
        pendingItem = event.has("pending_item") ? event.get("pending_item") : null;
        pendingAmount = event.has("pending_amount") ? event.getInt("pending_amount") : 0;

        if (requestToken != null) {
            sellingMode = "sell".equals(pendingMode);
            if (pendingItem != null && !pendingItem.toLowerCase(Locale.ROOT)
                    .contains(search.getText().trim().toLowerCase(Locale.ROOT))) {
                search.setText("");
            }
        } else if ((sellingMode && buying.isEmpty())
                || (!sellingMode && selling.isEmpty())) {
            sellingMode = selling.isEmpty() && !buying.isEmpty();
        }
        filter();
        final String selectedName = requestToken != null ? pendingItem : previous;
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
        if ("offer".equals(event.get("phase")) && requestToken != null) {
            quote.setText((sellingMode ? "Do otrzymania: " : "Do zapłaty: ")
                    + event.get("pending_price_text"));
            quote.setVisible(true);
            setStatus("Oferta gotowa. Potwierdź lub anuluj.", GOLD);
        } else {
            quote.setText("");
            quote.setVisible(false);
            final String message = event.has("message") ? event.get("message") : "";
            final boolean error = message.startsWith("Nie ")
                    || message.startsWith("Nieprawidł")
                    || message.startsWith("Oferta wygasła")
                    || message.startsWith("Cena uległa")
                    || message.contains("nie powiodła");
            final Color color = error ? ERROR
                    : "result".equals(event.get("phase"))
                            && "Transakcja zakończona.".equals(message) ? SUCCESS : MUTED;
            setStatus(message.length() > 0 ? message
                    : "Wybierz przedmiot i zapytaj handlarza o cenę.", color);
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
        final List<String> stackable = values(event, "sell_stackable");
        for (int i = 0; i < names.size(); i++) {
            target.add(new Entry(names.get(i),
                    get(prices, i), get(classes, i), get(subclasses, i),
                    !"sell".equals(prefix) || "1".equals(get(stackable, i))));
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
        unitPrice.setText(entry == null ? "" : sellingMode
                ? "Cena bazowa: " + entry.price : "Cena: " + entry.price);
        preview.setEntry(entry);
        amountModel.setMaximum(entry != null && !entry.stackable ? 1 : 1000);
        if (entry != null && !entry.stackable) {
            amountModel.setValue(1);
        }
        refreshButtons();
    }

    private void refreshButtons() {
        final boolean offer = requestToken != null;
        request.setVisible(!offer);
        confirm.setVisible(offer);
        cancel.setVisible(offer);
        request.setEnabled(selected() != null && !waiting && !offer);
        confirm.setEnabled(offer && !waiting);
        cancel.setEnabled(offer && !waiting);
        search.setEnabled(!offer && !waiting);
        sellList.setEnabled(!offer && !waiting);
        buyList.setEnabled(!offer && !waiting);
        amount.setEnabled(!offer && !waiting && selected() != null);
        refresh.setEnabled(!waiting);
        updateTabs();
        actions.revalidate();
        actions.repaint();
    }

    private void ask() {
        final Entry entry = selected();
        if (entry == null || waiting || requestToken != null) {
            return;
        }
        final int quantity;
        try {
            amount.commitEdit();
            quantity = ((Number) amount.getValue()).intValue();
        } catch (final ParseException e) {
            setStatus("Podaj poprawną ilość.", ERROR);
            return;
        }
        if (quantity < 1 || quantity > (entry.stackable ? 1000 : 1)) {
            setStatus("Podaj poprawną ilość.", ERROR);
            return;
        }
        waiting = true;
        refreshButtons();
        setStatus("Oczekiwanie na ofertę handlarza...", MUTED);
        send("request", sellingMode ? "sell" : "buy",
                entry.name, quantity, null);
    }

    private void answer(final boolean accept) {
        if (requestToken == null || waiting) {
            return;
        }
        waiting = true;
        refreshButtons();
        setStatus("Oczekiwanie na odpowiedź handlarza...", MUTED);
        send(accept ? "confirm" : "cancel", pendingMode, pendingItem,
                pendingAmount, requestToken);
    }

    private void send(final String command, final String mode,
            final String item, final int quantity, final String token) {
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
        if (token != null) {
            action.put("request_token", token);
        }
        StendhalClient.get().send(action);
    }
}