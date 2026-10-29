package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.LayoutManager;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.client.sprite.TextSprite;
import games.stendhal.common.constants.Actions;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;

/** A producer's recipe, batch requirements and persistent order in one window. */
public final class ProductionWindow extends InternalManagedWindow {
    private static final long serialVersionUID = 1L;
    private static final Color AVAILABLE = new Color(92, 190, 105), MISSING = new Color(225, 90, 75);
    private static final Set<String> SEEN = new LinkedHashSet<>();
    private static ProductionWindow instance;
    private static String pendingOpen;
    private final Map<String, Sprite> sprites = new LinkedHashMap<>();
    private final ItemSlot product = new ItemSlot();
    private final JLabel name = new JLabel(), duration = new JLabel(), orderLabel = new JLabel(), remaining = new JLabel();
    private final JComboBox<String> recipes = new JComboBox<>();
    private List<String> recipeIds = Collections.emptyList();
    private String selectedRecipe;
    private final JLabel status = new JLabel(" ");
    private final JPanel materials = panel(new GridLayout(1, 0, 12, 0));
    private final JSpinner amount = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    private final JProgressBar progress = new JProgressBar(0, 1000);
    private final JButton start = new JButton(text("Zleć produkcję")), collect = new JButton(text("Odbierz zamówienie"));
    private final Timer preview = new Timer(180, event -> send("refresh"));
    private final Timer polling = new Timer(2000, event -> { if (!this.busy) send("refresh"); });
    private final Timer animation = new Timer(100, event -> { product.repaint(); materials.repaint(); updateClock(); });
    private RPEvent data;
    private int npcId, sequence;
    private String windowId, lastRequest;
    private boolean applying, busy;
    private long readyAt;

    private ProductionWindow() {
        super("production", text("Produkcja"));
        setCloseable(true); setMinimizable(false);
        preview.setRepeats(false);
        JPanel content = panel(new BorderLayout(0, 8));
        content.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        content.setPreferredSize(new Dimension(360, 420));
        JPanel identity = panel(new BorderLayout(8, 0));
        identity.add(product, BorderLayout.WEST);
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        JPanel selection = panel(new GridBagLayout());
        GridBagConstraints selectionLayout = new GridBagConstraints();
        selectionLayout.gridx = 0; selectionLayout.gridy = 0; selectionLayout.weightx = 1;
        selectionLayout.fill = GridBagConstraints.HORIZONTAL;
        selection.add(name, selectionLayout); selection.add(recipes, selectionLayout); recipes.setVisible(false);
        identity.add(selection, BorderLayout.CENTER);
        content.add(identity, BorderLayout.NORTH);

        JPanel body = panel(null); body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        JPanel controls = panel(new BorderLayout());
        JPanel quantities = panel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        quantities.add(new JLabel(text("Ilość:"))); amount.setPreferredSize(new Dimension(78, 25)); quantities.add(amount);
        for (int count : new int[] {1, 10, 100}) {
            JButton button = new JButton(Integer.toString(count));
            button.addActionListener(e -> {
                int step = data != null && data.has("amount_step") ? data.getInt("amount_step") : 1;
                int maximum = data == null ? 1000 : data.getInt("max_amount");
                amount.setValue(Math.max(step, Math.min(maximum, (count + step - 1) / step * step)));
            });
            quantities.add(button);
        }
        controls.add(quantities, BorderLayout.NORTH);
        controls.add(duration, BorderLayout.SOUTH);
        body.add(section("Zamówienie", controls));
        body.add(section("Składniki (masz / potrzeba)", materials));
        JPanel order = panel(new GridLayout(3, 1, 0, 3));
        order.add(orderLabel); progress.setStringPainted(false); order.add(progress); order.add(remaining);
        body.add(section("Bieżące zamówienie", order));
        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setOpaque(false); bodyScroll.getViewport().setOpaque(false);
        bodyScroll.setBorder(BorderFactory.createEmptyBorder());
        bodyScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(16);
        content.add(bodyScroll, BorderLayout.CENTER);
        JPanel footer = panel(new BorderLayout(0, 6));
        status.setPreferredSize(new Dimension(330, 34)); footer.add(status, BorderLayout.CENTER);
        JPanel buttons = panel(new FlowLayout(FlowLayout.RIGHT, 5, 0)); buttons.add(start); buttons.add(collect);
        footer.add(buttons, BorderLayout.SOUTH); content.add(footer, BorderLayout.SOUTH);
        setContent(content);
        amount.addChangeListener(event -> {
            if (!applying && data != null && !busy) {
                lastRequest = null; start.setEnabled(false); updateRequirements(); preview.restart();
            }
        });
        recipes.addActionListener(event -> {
            if (!applying && !busy && data != null && recipes.getSelectedIndex() >= 0) {
                selectedRecipe = recipeIds.get(recipes.getSelectedIndex());
                lastRequest = null; start.setEnabled(false); collect.setEnabled(false); preview.stop(); send("refresh");
            }
        });
        start.addActionListener(event -> send("start")); collect.addActionListener(event -> send("collect"));
        addCloseListener(window -> {
            pendingOpen = null; preview.stop(); polling.stop(); animation.stop();
            send("close"); instance = null;
        });
    }

    public static RPAction openAction(int npcId) {
        pendingOpen = UUID.randomUUID().toString();
        RPAction action = new RPAction(); action.put("type", Actions.PRODUCTION);
        action.put("command", "open"); action.put("npc_id", npcId); action.put("request_id", pendingOpen);
        return action;
    }

    public static void show(RPEvent event) {
        SwingUtilities.invokeLater(() -> {
            boolean opening = "open".equals(event.get("phase"));
            if (opening) {
                String request = event.get("request_id");
                if (!request.isEmpty() && !request.equals(pendingOpen)) return;
                if (!SEEN.add(event.get("window_id"))) return;
                if (SEEN.size() > 64) SEEN.remove(SEEN.iterator().next());
                pendingOpen = null;
                if (instance != null) instance.close();
                instance = new ProductionWindow(); j2DClient.get().addWindow(instance);
            }
            if (instance == null || (!opening && !event.get("window_id").equals(instance.windowId))) return;
            if (!opening && !event.get("request_id").equals(instance.lastRequest)) return;
            if ("too_far".equals(event.get("status"))) { instance.close(); return; }
            instance.apply(event); instance.setVisible(true);
            if (opening) { instance.raise(); instance.polling.start(); instance.animation.start(); }
        });
    }

    public static void closeOnZoneChange() {
        SwingUtilities.invokeLater(() -> { pendingOpen = null; if (instance != null) instance.close(); });
    }

    private void apply(RPEvent event) {
        data = event; npcId = event.getInt("npc_id"); windowId = event.get("window_id"); busy = false;
        setTitle(text("Produkcja") + " · " + event.get("npc_name"));
        name.setText(text(event.get("name"))); product.setItem(event.get("class"), event.get("subclass"), event.getInt("amount"));
        applying = true;
        List<String> ids = list("recipe_ids"); selectedRecipe = event.has("selected_recipe") ? event.get("selected_recipe") : null;
        if (!recipeIds.equals(ids)) {
            recipeIds = ids; recipes.removeAllItems(); for (String recipe : list("recipe_names")) recipes.addItem(text(recipe));
        }
        int selected = recipeIds.indexOf(selectedRecipe);
        if (recipes.getSelectedIndex() != selected) recipes.setSelectedIndex(selected);
        recipes.setVisible(recipeIds.size() > 1); name.setVisible(recipeIds.size() <= 1); recipes.setEnabled(true);
        SpinnerNumberModel model = (SpinnerNumberModel) amount.getModel();
        int step = event.has("amount_step") ? event.getInt("amount_step") : 1;
        model.setMaximum(event.getInt("max_amount")); model.setMinimum(step); model.setStepSize(step);
        amount.setValue(event.getInt("amount")); applying = false;
        readyAt = System.currentTimeMillis() + event.getInt("remaining") * 1000L;
        updateRequirements(); updateClock();
        String message = event.get("message");
        status.setText(message.isEmpty() ? " " : "<html><div style='width:245px'>" + text(message) + "</div></html>");
        status.setForeground("ok".equals(event.get("status")) ? AVAILABLE : MISSING);
        start.setEnabled(event.getInt("can_start") != 0);
        collect.setEnabled("ready".equals(event.get("order_state")));
    }

    private void updateRequirements() {
        int count = (Integer) amount.getValue();
        int serverCount = data.getInt("amount");
        duration.setText(text("Czas produkcji:") + " " + formatTime((int) ((long) data.getInt("duration") * count / serverCount)));
        product.setItem(data.get("class"), data.get("subclass"), count);
        List<String> names = list("material_names"), classes = list("material_classes"), subclasses = list("material_subclasses");
        List<String> required = list("material_values"), owned = list("owned_material_values");
        materials.setLayout(new GridLayout(0, Math.max(1, Math.min(3, names.size())), 12, 6));
        materials.removeAll();
        for (int i = 0; i < names.size(); i++) {
            int need = (int) ((long) Integer.parseInt(required.get(i)) * count / serverCount), have = Integer.parseInt(owned.get(i));
            JPanel card = panel(new BorderLayout(0, 2));
            ItemSlot slot = new ItemSlot(); slot.setItem(classes.get(i), subclasses.get(i), need);
            JPanel image = panel(new FlowLayout(FlowLayout.CENTER, 0, 0)); image.add(slot); card.add(image, BorderLayout.NORTH);
            JLabel label = new JLabel(text(names.get(i)), SwingConstants.CENTER); card.add(label, BorderLayout.CENTER);
            if (names.size() > 2) label.setText("<html><div style='width:78px;text-align:center'>" + text(names.get(i)) + "</div></html>");
            JLabel values = new JLabel(have + " / " + need, SwingConstants.CENTER);
            values.setForeground(have >= need ? AVAILABLE : MISSING); card.add(values, BorderLayout.SOUTH);
            materials.add(card);
        }
        materials.revalidate(); materials.repaint();
    }

    private List<String> list(String key) { return data.has(key) ? data.getList(key) : Collections.emptyList(); }

    private void updateClock() {
        if (data == null) return;
        String state = data.get("order_state");
        boolean active = !"idle".equals(state);
        orderLabel.setText(active ? text(data.has("order_name") ? data.get("order_name") : data.get("name"))
                + ": " + data.getInt("order_amount") : text("Brak aktywnego zamówienia."));
        int left = (int) Math.max(0, (readyAt - System.currentTimeMillis() + 999) / 1000);
        remaining.setText(!active ? text("Możesz odejść. Produkcja trwa również poza grą.")
                : left == 0 ? text("Gotowe do odbioru.") : text("Pozostało:") + " " + formatTime(left));
        progress.setValue(!active ? 0 : (int) (1000L * Math.max(0, data.getInt("order_duration") - left) / Math.max(1, data.getInt("order_duration"))));
        // The server confirms readiness; a local clock never grants a product.
    }

    private void send(String command) {
        if (npcId == 0 || windowId == null) return;
        RPAction action = new RPAction(); action.put("type", Actions.PRODUCTION); action.put("command", command);
        action.put("npc_id", npcId); action.put("window_id", windowId); action.put("amount", (Integer) amount.getValue());
        if (selectedRecipe != null) action.put("recipe", selectedRecipe);
        lastRequest = Integer.toString(++sequence); action.put("request_id", lastRequest);
        if ("start".equals(command) || "collect".equals(command)) { busy = true; preview.stop(); recipes.setEnabled(false); start.setEnabled(false); collect.setEnabled(false); }
        StendhalClient.get().send(action);
    }

    static String formatTime(int seconds) {
        return seconds >= 3600 ? seconds / 3600 + " h " + seconds % 3600 / 60 + " min"
                : seconds >= 60 ? seconds / 60 + " min " + seconds % 60 + " s" : seconds + " s";
    }
    private static String text(String value) { return ClientLanguage.getText(value); }
    private static JPanel panel(LayoutManager layout) { return new LayoutPanel(layout); }

    private static final class LayoutPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        LayoutPanel(LayoutManager layout) {
            super(layout); setOpaque(false); setBorder(BorderFactory.createEmptyBorder());
        }
        @Override protected void paintComponent(Graphics graphics) {
            // The parent window supplies the themed background.
        }
    }
    private static JPanel section(String title, JComponent body) {
        JPanel panel = panel(new BorderLayout());
        Style style = StyleUtil.getStyle();
        Border frame = style == null ? BorderFactory.createEtchedBorder() : style.getBorderDown();
        TitledBorder border = BorderFactory.createTitledBorder(frame, text(title));
        border.setTitleFont(new JLabel().getFont().deriveFont(Font.BOLD));
        if (style != null) border.setTitleColor(style.getForeground());
        panel.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(5, 6, 5, 6)));
        panel.add(body); return panel;
    }

    /** Display-only slot: the normal texture, an animated sprite and a quantity index. */
    private final class ItemSlot extends JComponent {
        private static final long serialVersionUID = 1L;
        private final Sprite background = SpriteStore.get().getSprite("data/gui/slot.png");
        private Sprite sprite, quantity;
        ItemSlot() { setPreferredSize(new Dimension(42, 42)); }
        void setItem(String itemClass, String subclass, int count) {
            String path = "data/sprites/items/" + itemClass + "/" + subclass + ".png";
            sprite = sprites.get(path);
            if (sprite == null) {
                SpriteStore store = SpriteStore.get(); Sprite sheet = store.getSprite(path);
                sprite = sheet.getWidth() > 32 ? store.getAnimatedSprite(sheet, 0, 0, sheet.getWidth() / 32, 32, 32, 100)
                        : sheet.getHeight() > 32 ? store.getTile(sheet, 0, 0, 32, 32) : sheet;
                sprites.put(path, sprite);
            }
            quantity = count > 1 ? TextSprite.createTextSprite(Integer.toString(count), Color.WHITE) : null;
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int x = (getWidth() - background.getWidth()) / 2, y = (getHeight() - background.getHeight()) / 2;
            background.draw(g, x, y);
            if (sprite != null) sprite.draw(g, (getWidth() - sprite.getWidth()) / 2, (getHeight() - sprite.getHeight()) / 2);
            if (quantity != null) quantity.draw(g, x + background.getWidth() - quantity.getWidth(), y);
        }
    }
}
