package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.constants.Actions;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;

/**
 * Okno sklepu otwierane z menu kontekstowego handlarza.
 * Lista pochodzi z serwera, a transakcje obsluguje istniejacy dialog NPC.
 */
public final class NpcShopWindow extends InternalManagedWindow {
    private static final long serialVersionUID = 1L;
    private static final Color ACCENT = new Color(255, 190, 64);
    private static final Color BORDER = new Color(130, 106, 75);
    private static final Color SELECTED = new Color(90, 67, 43);
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

    private final List<Entry> selling = new ArrayList<Entry>();
    private final List<Entry> buying = new ArrayList<Entry>();
    private final DefaultListModel<Entry> sellModel = new DefaultListModel<Entry>();
    private final DefaultListModel<Entry> buyModel = new DefaultListModel<Entry>();
    private final JList<Entry> sellList = createList(sellModel);
    private final JList<Entry> buyList = createList(buyModel);
    private final JTabbedPane tabs = new JTabbedPane();
    private final JTextField search = new JTextField();
    private final JLabel merchant = title("Sklep");
    private final JLabel wallet = new JLabel("");
    private final JLabel selectedName = title("Wybierz przedmiot");
    private final JLabel unitPrice = new JLabel("");
    private final JLabel information = new JLabel("");
    private final ItemIcon preview = new ItemIcon();
    private final SpinnerNumberModel amountModel = new SpinnerNumberModel(1, 1, 1000, 1);
    private final JSpinner amount = new JSpinner(amountModel);
    private final JButton request = new JButton("Zapytaj o cenę");
    private final JButton confirm = new JButton("Potwierdź");
    private final JButton cancel = new JButton("Anuluj");
    private final JButton refresh = new JButton("Odśwież");
    private final Map<String, Sprite> spriteCache = new HashMap<String, Sprite>();

    private int npcId;
    private String requestToken;
    private String pendingMode;
    private String pendingItem;
    private int pendingAmount;
    private boolean applying;

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

    private JComponent createContent() {
        final JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        content.setPreferredSize(new Dimension(680, 460));

        final JPanel header = new JPanel(new BorderLayout(8, 2));
        header.add(merchant, BorderLayout.WEST);
        header.add(wallet, BorderLayout.EAST);
        final JPanel top = new JPanel(new BorderLayout(0, 8));
        top.add(header, BorderLayout.NORTH);
        search.setToolTipText("Filtruj nazwy przedmiotów");
        top.add(search, BorderLayout.SOUTH);
        content.add(top, BorderLayout.NORTH);

        final JPanel buyPanel = new JPanel(new BorderLayout());
        buyPanel.add(new JScrollPane(sellList), BorderLayout.CENTER);
        final JPanel sellPanel = new JPanel(new BorderLayout());
        sellPanel.add(new JScrollPane(buyList), BorderLayout.CENTER);
        tabs.addTab("Kup", buyPanel);
        tabs.addTab("Sprzedaj", sellPanel);
        tabs.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent event) {
                if (!applying) {
                    refreshDetails();
                }
            }
        });

        final JPanel details = new JPanel(new BorderLayout(5, 8));
        details.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        details.setPreferredSize(new Dimension(260, 100));
        final JPanel item = new JPanel(new BorderLayout(5, 4));
        item.add(preview, BorderLayout.NORTH);
        final JPanel labels = new JPanel(new GridLayout(0, 1, 0, 5));
        selectedName.setHorizontalAlignment(SwingConstants.CENTER);
        unitPrice.setHorizontalAlignment(SwingConstants.CENTER);
        labels.add(selectedName);
        labels.add(unitPrice);
        item.add(labels, BorderLayout.CENTER);
        details.add(item, BorderLayout.NORTH);
        final JPanel count = new JPanel(new FlowLayout(FlowLayout.CENTER));
        count.add(new JLabel("Ilość"));
        amount.setPreferredSize(new Dimension(80, amount.getPreferredSize().height));
        count.add(amount);
        details.add(count, BorderLayout.CENTER);
        final JPanel sideActions = new JPanel(new GridLayout(0, 1, 0, 5));
        sideActions.add(request);
        sideActions.add(confirm);
        sideActions.add(cancel);
        details.add(sideActions, BorderLayout.SOUTH);

        final JPanel main = new JPanel(new BorderLayout(8, 0));
        main.add(tabs, BorderLayout.CENTER);
        main.add(details, BorderLayout.EAST);
        content.add(main, BorderLayout.CENTER);

        final JPanel bottom = new JPanel(new BorderLayout(4, 4));
        information.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        bottom.add(information, BorderLayout.CENTER);
        bottom.add(refresh, BorderLayout.EAST);
        content.add(bottom, BorderLayout.SOUTH);

        final DocumentListener filter = new DocumentListener() {
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
        };
        search.getDocument().addDocumentListener(filter);
        final ListSelectionListener selection = new ListSelectionListener() {
            @Override
            public void valueChanged(final ListSelectionEvent event) {
                if (!event.getValueIsAdjusting() && !applying) {
                    refreshDetails();
                }
            }
        };
        sellList.addListSelectionListener(selection);
        buyList.addListSelectionListener(selection);
        request.addActionListener(event -> ask());
        confirm.addActionListener(event -> answer(true));
        cancel.addActionListener(event -> answer(false));
        refresh.addActionListener(event ->
                send("refresh", null, null, 0, null));
        amount.addChangeListener(event -> refreshButtons());
        confirm.setEnabled(false);
        cancel.setEnabled(false);
        request.setEnabled(false);
        return content;
    }

    private static JLabel title(final String text) {
        final JLabel result = new JLabel(text);
        result.setForeground(ACCENT);
        result.setFont(result.getFont().deriveFont(Font.BOLD));
        return result;
    }

    private static JList<Entry> createList(final DefaultListModel<Entry> model) {
        final JList<Entry> list = new JList<Entry>(model);
        list.setFixedCellHeight(54);
        list.setCellRenderer(new RowRenderer());
        list.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        return list;
    }

    private static final class RowRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(final JList<?> list,
                final Object value, final int index, final boolean selected,
                final boolean focused) {
            final Entry entry = (Entry) value;
            final JPanel row = new JPanel(new BorderLayout(6, 2));
            row.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8));
            row.setBackground(selected ? SELECTED : list.getBackground());
            final JLabel name = new JLabel(entry.name);
            name.setFont(name.getFont().deriveFont(Font.BOLD));
            name.setForeground(selected ? Color.WHITE : list.getForeground());
            final JLabel price = new JLabel(entry.price);
            price.setForeground(selected ? ACCENT : list.getForeground());
            final JPanel text = new JPanel(new GridLayout(0, 1, 0, 2));
            text.setOpaque(false);
            text.add(name);
            text.add(price);
            row.add(new SmallIcon(entry), BorderLayout.WEST);
            row.add(text, BorderLayout.CENTER);
            return row;
        }
    }

    private static final class SmallIcon extends JComponent {
        private static final long serialVersionUID = 1L;
        private final Sprite sprite;

        private SmallIcon(final Entry entry) {
            setPreferredSize(new Dimension(40, 40));
            sprite = entry.itemClass.length() == 0 || entry.subclass.length() == 0
                    ? null : SpriteStore.get().getSprite("data/sprites/items/"
                            + entry.itemClass + "/" + entry.subclass + ".png");
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            if (sprite != null) {
                sprite.draw(graphics, (getWidth() - sprite.getWidth()) / 2,
                        (getHeight() - sprite.getHeight()) / 2);
            }
        }
    }

    private final class ItemIcon extends JComponent {
        private static final long serialVersionUID = 1L;
        private Sprite sprite;
        private final Sprite slot = SpriteStore.get().getSprite("data/gui/slot.png");

        private ItemIcon() {
            setPreferredSize(new Dimension(64, 64));
        }

        private void setEntry(final Entry entry) {
            sprite = null;
            if (entry != null && entry.itemClass.length() > 0
                    && entry.subclass.length() > 0) {
                final String path = "data/sprites/items/" + entry.itemClass
                        + "/" + entry.subclass + ".png";
                sprite = spriteCache.get(path);
                if (sprite == null) {
                    sprite = SpriteStore.get().getSprite(path);
                    spriteCache.put(path, sprite);
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(final Graphics graphics) {
            super.paintComponent(graphics);
            slot.draw(graphics, (getWidth() - slot.getWidth()) / 2,
                    (getHeight() - slot.getHeight()) / 2);
            if (sprite != null) {
                sprite.draw(graphics, (getWidth() - sprite.getWidth()) / 2,
                        (getHeight() - sprite.getHeight()) / 2);
            }
        }
    }

    private void apply(final RPEvent event) {
        applying = true;
        npcId = event.getInt("npc_id");
        merchant.setText(event.get("npc_name"));
        wallet.setText("Posiadasz: " + event.get("owned_money_text"));
        final int previousTab = tabs.getSelectedIndex();
        final String previousSelection = selected() == null ? null : selected().name;
        selling.clear();
        buying.clear();
        readEntries(event, "sell", selling);
        readEntries(event, "buy", buying);
        tabs.setEnabledAt(0, !selling.isEmpty());
        tabs.setEnabledAt(1, !buying.isEmpty());
        if (previousTab == 1 && !buying.isEmpty()) {
            tabs.setSelectedIndex(1);
        } else if (!selling.isEmpty()) {
            tabs.setSelectedIndex(0);
        } else if (!buying.isEmpty()) {
            tabs.setSelectedIndex(1);
        }
        requestToken = event.has("request_token") ? event.get("request_token") : null;
        pendingMode = event.has("pending_mode") ? event.get("pending_mode") : null;
        pendingItem = event.has("pending_item") ? event.get("pending_item") : null;
        pendingAmount = event.has("pending_amount") ? event.getInt("pending_amount") : 0;
        filter();
        if (previousSelection != null) {
            final JList<Entry> current = currentList();
            for (int i = 0; i < current.getModel().getSize(); i++) {
                if (previousSelection.equals(current.getModel().getElementAt(i).name)) {
                    current.setSelectedIndex(i);
                    break;
                }
            }
        }
        final String phase = event.get("phase");
        if ("offer".equals(phase)) {
            information.setText("Oferta: " + pendingAmount + " x " + pendingItem
                    + ", " + event.get("pending_price_text")
                    + ". Potwierdź lub anuluj.");
            information.setForeground(ACCENT);
        } else {
            final String message = event.has("message") ? event.get("message") : "";
            information.setText(message.length() > 0 ? message
                    : "Wybierz przedmiot i zapytaj handlarza o cenę.");
            information.setForeground(ACCENT);
        }
        applying = false;
        refreshDetails();
        revalidate();
        repaint();
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
        final String term = search.getText().trim().toLowerCase(Locale.ROOT);
        fill(selling, sellModel, term);
        fill(buying, buyModel, term);
        refreshDetails();
    }

    private static void fill(final List<Entry> source,
            final DefaultListModel<Entry> model, final String term) {
        model.clear();
        for (final Entry entry : source) {
            if (entry.name.toLowerCase(Locale.ROOT).contains(term)) {
                model.addElement(entry);
            }
        }
    }

    private JList<Entry> currentList() {
        return tabs.getSelectedIndex() == 1 ? buyList : sellList;
    }

    private Entry selected() {
        return currentList().getSelectedValue();
    }

    private void refreshDetails() {
        final Entry entry = selected();
        selectedName.setText(entry == null ? "Wybierz przedmiot" : entry.name);
        unitPrice.setText(entry == null ? "" : tabs.getSelectedIndex() == 1
                ? "Cena bazowa: " + entry.price : "Cena: " + entry.price);
        preview.setEntry(entry);
        amountModel.setMaximum(entry != null && !entry.stackable ? 1 : 1000);
        if (entry != null && !entry.stackable) {
            amountModel.setValue(1);
        }
        refreshButtons();
    }

    private void refreshButtons() {
        request.setEnabled(selected() != null && requestToken == null);
        confirm.setEnabled(requestToken != null);
        cancel.setEnabled(requestToken != null);
    }

    private void ask() {
        final Entry entry = selected();
        if (entry == null || requestToken != null) {
            return;
        }
        request.setEnabled(false);
        information.setText("Oczekiwanie na odpowiedź handlarza...");
        send("request", tabs.getSelectedIndex() == 1 ? "sell" : "buy",
                entry.name, ((Number) amount.getValue()).intValue(), null);
    }

    private void answer(final boolean accept) {
        if (requestToken == null) {
            return;
        }
        confirm.setEnabled(false);
        cancel.setEnabled(false);
        information.setText("Oczekiwanie na odpowiedź handlarza...");
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
