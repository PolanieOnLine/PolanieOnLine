/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.constants.Actions;
import games.stendhal.common.constants.ItemRarity;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;

/** In-game affix service. Only server-authorized items and affixes can be selected. */
public final class AffixRerollWindow extends InternalManagedWindow {
	private static final long serialVersionUID = 1L;
	private static final int DETAIL_TEXT_WIDTH = 280;
	private static final Set<String> openedWindows = new HashSet<>();
	private static RPObject sessionOwner;
	private static AffixRerollWindow active;

	private final RPObject owner;
	private final Correlation correlation;
	private final DefaultListModel<Candidate> items = new DefaultListModel<>();
	private final DefaultListModel<Affix> affixes = new DefaultListModel<>();
	private final JList<Candidate> itemList = new JList<>(items);
	private final JList<Affix> affixList = new JList<>(affixes);
	private final JLabel current = new JLabel();
	private final JLabel replacement = new JLabel();
	private final JLabel cost = new JLabel();
	private final JLabel status = new JLabel();
	private final JLabel itemIcon = new JLabel();
	private final JLabel itemName = new JLabel();
	private final JLabel itemRarity = new JLabel();
	private final JPanel comparisonSection;
	private final JButton roll = new JButton(text("Przelosuj afiks"));
	private final JButton keep = new JButton(text("Zachowaj obecny"));
	private final JButton accept = new JButton(text("Wybierz nowy"));
	private final JButton refresh = new JButton(text("Odśwież"));
	private final Timer timer;
	private String selectedPath = "";
	private String selectedAffix = "";
	private String requestToken = "";
	private String offerToken = "";
	private String rerolledAffix = "";
	private boolean canRoll;
	private boolean canAccept;
	private boolean updating;
	private boolean waiting;
	private boolean closed;
	private long sentAt;

	private AffixRerollWindow(final RPObject owner, final RPEvent event) {
		super("affix-reroll", text("Przemiana afiksów u Przemira"));
		this.owner = owner;
		correlation = new Correlation(value(event, "window_id"), value(event, "npc_id"));
		setCloseable(true);
		setMinimizable(false);
		final JPanel content = panel(new BorderLayout(0, 8));
		content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		content.setPreferredSize(new Dimension(660, 530));

		final JPanel center = panel(new BorderLayout(8, 0));
		itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		affixList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		itemList.setCellRenderer(new CandidateRenderer());
		affixList.setCellRenderer(new AffixRenderer());
		final JPanel catalog = listPanel(text("Przedmioty"), itemList);
		catalog.setPreferredSize(new Dimension(210, 1));
		center.add(catalog, BorderLayout.WEST);
		final JPanel details = panel(new BorderLayout(0, 8));
		final JPanel identity = panel(new BorderLayout(8, 0));
		itemIcon.setPreferredSize(new Dimension(48, 48));
		identity.add(itemIcon, BorderLayout.WEST);
		final JPanel names = panel(new GridLayout(0, 1, 0, 3));
		itemName.setFont(itemName.getFont().deriveFont(Font.BOLD));
		names.add(itemName);
		names.add(itemRarity);
		identity.add(names, BorderLayout.CENTER);
		details.add(DialogPanel.section(text("Wybrany przedmiot"), identity), BorderLayout.NORTH);
		final JPanel properties = panel(new BorderLayout(0, 8));
		properties.add(listPanel(text("Afiksy przedmiotu"), affixList), BorderLayout.CENTER);

		final JPanel comparison = panel(new BorderLayout(0, 6));
		final JPanel labels = panel(new BorderLayout(0, 8));
		current.setVerticalAlignment(JLabel.TOP);
		current.setFont(current.getFont().deriveFont(Font.PLAIN));
		replacement.setVerticalAlignment(JLabel.TOP);
		replacement.setFont(replacement.getFont().deriveFont(Font.PLAIN));
		replacement.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(new Color(105, 154, 176)),
				BorderFactory.createEmptyBorder(4, 4, 4, 4)));
		labels.add(current, BorderLayout.NORTH);
		labels.add(replacement, BorderLayout.CENTER);
		comparison.add(scrollPane(labels), BorderLayout.CENTER);
		comparisonSection = DialogPanel.section(text("Porównanie afiksów"), comparison);
		comparisonSection.setPreferredSize(new Dimension(1, 150));
		properties.add(comparisonSection, BorderLayout.SOUTH);
		details.add(properties, BorderLayout.CENTER);
		center.add(details, BorderLayout.CENTER);
		content.add(center, BorderLayout.CENTER);

		final JPanel footer = panel(new BorderLayout(0, 6));
		footer.add(new JSeparator(), BorderLayout.NORTH);
		final JPanel payment = panel(new BorderLayout(0, 3));
		cost.setIcon(new ItemIcon("resource", "transformation_stone"));
		payment.add(cost, BorderLayout.NORTH);
		cost.setToolTipText(html(text("Zwykły afiks kosztuje 1 kamień przemiany, a legendarny 3 kamienie. Zachowanie obecnego wyniku również zużywa kamienie."), 300));
		payment.add(new JLabel(text("Kamienie są zużywane także przy zachowaniu obecnego afiksu.")), BorderLayout.CENTER);
		payment.add(status, BorderLayout.SOUTH);
		footer.add(payment, BorderLayout.CENTER);
		final JPanel buttons = panel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		buttons.add(roll);
		buttons.add(keep);
		buttons.add(accept);
		buttons.add(refresh);
		final JButton close = new JButton(text("Zamknij"));
		buttons.add(close);
		for (final JButton button : Arrays.asList(roll, keep, accept, refresh, close)) {
			button.setFocusable(false);
			button.setMargin(new Insets(4, 12, 4, 12));
		}
		footer.add(buttons, BorderLayout.SOUTH);
		content.add(footer, BorderLayout.SOUTH);
		setContent(content);

		itemList.addListSelectionListener(change -> {
			if (change.getValueIsAdjusting() || updating || waiting || !offerToken.isEmpty()) return;
			final Candidate item = itemList.getSelectedValue();
			if (item == null || item.path.equals(selectedPath)) return;
			selectedPath = item.path;
			selectedAffix = "";
			requestToken = "";
			send("select");
		});
		affixList.addListSelectionListener(change -> {
			if (change.getValueIsAdjusting() || updating || waiting || !offerToken.isEmpty()) return;
			final Affix affix = affixList.getSelectedValue();
			if (affix == null || affix.id.equals(selectedAffix)) return;
			selectedAffix = affix.id;
			requestToken = "";
			send("select");
		});
		roll.addActionListener(change -> { if (roll.isEnabled()) send("roll"); });
		keep.addActionListener(change -> { if (keep.isEnabled()) send("keep"); });
		accept.addActionListener(change -> { if (accept.isEnabled()) send("accept"); });
		refresh.addActionListener(change -> send("refresh"));
		close.addActionListener(change -> close());
		timer = new Timer(100, tick -> {
			final StendhalClient client = StendhalClient.get();
			if (client == null || client.getPlayer() != this.owner) {
				close();
			} else if (waiting && System.currentTimeMillis() - sentAt >= 8000) {
				waiting = false;
				status.setText(html(text("Oczekiwanie na odpowiedź. Odświeżanie stanu przemiany…"), 470));
				send("refresh");
			} else if (!waiting && System.currentTimeMillis() - sentAt >= 30000) {
				send("refresh");
			}
			if (isShowing()) {
				itemList.repaint();
				itemIcon.repaint();
			}
		});
		apply(event);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		if (!closed) timer.start();
	}

	@Override
	public void removeNotify() {
		timer.stop();
		super.removeNotify();
	}

	/** Events belong to the current player; later replies never create a window. */
	public static void show(final RPEvent event, final RPObject eventOwner) {
		final RPObject player = StendhalClient.get().getPlayer();
		if (player == null || eventOwner == null || !player.getID().equals(eventOwner.getID())) return;
		SwingUtilities.invokeLater(() -> {
			if (StendhalClient.get().getPlayer() != player) return;
			if (sessionOwner != player) {
				if (active != null) active.close();
				sessionOwner = player;
				openedWindows.clear();
			}
			if ("open".equals(value(event, "phase"))) {
				final String window = value(event, "window_id");
				if (window.isEmpty() || value(event, "npc_id").isEmpty() || !openedWindows.add(window)) return;
				if (active != null) active.close();
				active = new AffixRerollWindow(player, event);
				j2DClient.get().addWindow(active);
				active.setVisible(true);
				active.raise();
			} else if (active != null && !active.closed && active.owner == player
					&& active.correlation.accepts(event)) {
				active.apply(event);
			}
		});
	}

	public static void closeOnZoneChange() {
		SwingUtilities.invokeLater(() -> { if (active != null) active.close(); });
	}

	private void send(final String command) {
		if (closed || waiting || StendhalClient.get().getPlayer() != owner) return;
		final RPAction action = action(command);
		waiting = true;
		sentAt = System.currentTimeMillis();
		updateControls();
		StendhalClient.get().send(action);
	}

	private RPAction action(final String command) {
		final RPAction action = new RPAction();
		action.put("type", Actions.AFFIX_REROLL);
		action.put("command", command);
		action.put("window_id", correlation.windowId);
		action.put("npc_id", correlation.npcId);
		action.put("request_id", correlation.nextRequest());
		if (!"close".equals(command) && !selectedPath.isEmpty()) {
			action.put("target_path", Arrays.asList(selectedPath.split("/")));
		}
		if (("select".equals(command) || "roll".equals(command) || "refresh".equals(command)) && !selectedAffix.isEmpty()) {
			action.put("affix_id", selectedAffix);
		}
		if ("roll".equals(command)) action.put("request_token", requestToken);
		if ("accept".equals(command) || "keep".equals(command)) action.put("offer_token", offerToken);
		return action;
	}

	private void apply(final RPEvent event) {
		waiting = false;
		sentAt = System.currentTimeMillis();
		selectedPath = value(event, "selected_path");
		selectedAffix = value(event, "selected_affix");
		requestToken = value(event, "request_token");
		offerToken = value(event, "offer_token");
		rerolledAffix = value(event, "rerolled_affix");
		canRoll = "1".equals(value(event, "can_roll"));
		canAccept = !event.has("can_accept") || "1".equals(value(event, "can_accept"));
		updating = true;
		try {
			final List<Candidate> nextItems = candidates(event);
			updateModel(items, nextItems, Candidate::sameAs);
			int selectedItemIndex = -1;
			for (int index = 0; index < nextItems.size(); index++) {
				if (nextItems.get(index).path.equals(selectedPath)) selectedItemIndex = index;
			}
			if (selectedItemIndex < 0) itemList.clearSelection();
			else itemList.setSelectedIndex(selectedItemIndex);
			final List<Affix> nextAffixes = new ArrayList<>();
			final List<String> ids = list(event, "affix_ids");
			final List<String> labels = list(event, "affix_labels");
			for (int index = 0; index < Math.min(ids.size(), labels.size()); index++) {
				if (!ids.get(index).isEmpty()) nextAffixes.add(new Affix(ids.get(index), labels.get(index)));
			}
			updateModel(affixes, nextAffixes, Affix::sameAs);
			int selectedAffixIndex = -1;
			for (int index = 0; index < nextAffixes.size(); index++) {
				if (nextAffixes.get(index).id.equals(selectedAffix)) selectedAffixIndex = index;
			}
			if (selectedAffixIndex < 0) affixList.clearSelection();
			else affixList.setSelectedIndex(selectedAffixIndex);
		} finally {
			updating = false;
		}
		final Candidate selected = itemList.getSelectedValue();
		itemName.setText(selected == null ? text("Wybierz przedmiot z listy.") : html(selected.caption(), 230));
		itemRarity.setText(selected == null ? "" : text(selected.rarity.getPolishDisplayName()));
		itemName.setForeground(selected == null ? new JLabel().getForeground() : Color.decode(selected.rarity.getColorHex()));
		itemIcon.setIcon(selected == null ? null : new ItemIcon(selected.itemClass, selected.subclass));
		// The service replaces affixes within the same ordinary or legendary pool.
		final boolean legendary = selectedAffix.startsWith("legendary_");
		current.setText(AffixRerollPresentation.labelHtml(value(event, "current_label"),
				!rerolledAffix.isEmpty() && rerolledAffix.equals(selectedAffix), DETAIL_TEXT_WIDTH,
				text("Obecny afiks:") + " ", legendary));
		replacement.setText(offerToken.isEmpty() ? ""
				: AffixRerollPresentation.offerLabelHtml(value(event, "new_label"), DETAIL_TEXT_WIDTH - 12, legendary));
		cost.setText(text("Koszt:") + " " + ("3".equals(value(event, "stone_cost")) ? "3" : "1".equals(value(event, "stone_cost")) ? "1" : "1 / 3") + " × " + text("kamień przemiany") + "   ·   "
				+ text("Posiadasz:") + " " + value(event, "stone_count"));
		final String message = value(event, "message");
		status.setText(html(message.isEmpty() ? text(offerToken.isEmpty()
				? "Wybierz przedmiot i jeden afiks do przemiany." : "Wybierz, który afiks zachować." ) : message, 470));
		updateControls();
		setSize(getPreferredSize());
		revalidate();
		repaint();
	}

	/** Preserve list geometry and scrolling when a reply repeats the same catalogue. */
	private static <T> void updateModel(final DefaultListModel<T> model, final List<T> values,
			final BiPredicate<T, T> same) {
		for (int index = 0; index < values.size(); index++) {
			if (index >= model.size()) model.addElement(values.get(index));
			else if (!same.test(model.get(index), values.get(index))) model.set(index, values.get(index));
		}
		while (model.size() > values.size()) model.remove(model.size() - 1);
	}

	private void updateControls() {
		final boolean offer = !offerToken.isEmpty();
		comparisonSection.setVisible(offer);
		roll.setVisible(!offer);
		keep.setVisible(offer);
		accept.setVisible(offer);
		itemList.setEnabled(!waiting && !offer);
		affixList.setEnabled(!waiting && !offer);
		roll.setEnabled(!waiting && !offer && canRoll && !requestToken.isEmpty()
				&& !selectedPath.isEmpty() && !selectedAffix.isEmpty());
		keep.setEnabled(!waiting && offer);
		accept.setEnabled(!waiting && offer && canAccept);
		refresh.setEnabled(!waiting);
	}

	@Override
	public void close() {
		if (closed) return;
		closed = true;
		timer.stop();
		final StendhalClient client = StendhalClient.get();
		if (client != null && client.getPlayer() == owner) client.send(action("close"));
		correlation.close();
		items.clear();
		affixes.clear();
		requestToken = "";
		offerToken = "";
		if (active == this) active = null;
		super.close();
	}

	private static JPanel panel(final java.awt.LayoutManager layout) {
		return new DialogPanel(layout);
	}

	private static JPanel listPanel(final String title, final JList<?> list) {
		list.setOpaque(false);
		return DialogPanel.section(title, scrollPane(list));
	}

	private static JScrollPane scrollPane(final javax.swing.JComponent body) {
		final JScrollPane scroll = new JScrollPane(body);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setPreferredSize(new Dimension(1, 120));
		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		return scroll;
	}

	private static void styleCell(final JLabel label, final boolean selected) {
		// A paid offer disables selection, while its contents remain readable.
		label.setEnabled(true);
		label.setOpaque(selected);
		label.setBackground(new Color(105, 72, 34));
		label.setForeground(new JLabel().getForeground());
	}

	static String html(final String label, final int width) {
		return "<html><div style='width:" + width + "px'>" + escape(label) + "</div></html>";
	}

	private static String escape(final String label) {
		return label.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	private static String text(final String label) {
		return ClientLanguage.getText(label);
	}

	private static String value(final RPEvent event, final String key) {
		return event.has(key) ? event.get(key) : "";
	}

	private static List<String> list(final RPEvent event, final String key) {
		return event.has(key) ? event.getList(key) : Collections.emptyList();
	}

	/** Reject mismatched parallel lists rather than guessing which inventory item they describe. */
	static List<Candidate> candidates(final RPEvent event) {
		final List<String> paths = list(event, "candidate_paths");
		final List<String> names = list(event, "candidate_names");
		final List<String> classes = list(event, "candidate_classes");
		final List<String> subclasses = list(event, "candidate_subclasses");
		final List<String> rarities = list(event, "candidate_rarities");
		final List<String> levels = list(event, "candidate_levels");
		for (final List<String> values : Arrays.asList(names, classes, subclasses, rarities, levels)) {
			if (values.size() != paths.size()) return Collections.emptyList();
		}
		final List<Candidate> result = new java.util.ArrayList<>();
		for (int index = 0; index < paths.size(); index++) {
			if (paths.get(index).isEmpty()) continue;
			result.add(new Candidate(paths.get(index), names.get(index), classes.get(index), subclasses.get(index),
					rarities.get(index), levels.get(index)));
		}
		return result;
	}

	static final class Correlation {
		private final String windowId;
		private final String npcId;
		private String requestId;
		private int sequence;
		private boolean closed;

		Correlation(final String windowId, final String npcId) {
			this.windowId = windowId;
			this.npcId = npcId;
		}

		String nextRequest() {
			requestId = windowId + ":" + ++sequence;
			return requestId;
		}

		boolean accepts(final RPEvent event) {
			return !closed && requestId != null && requestId.equals(value(event, "request_id"))
					&& windowId.equals(value(event, "window_id")) && npcId.equals(value(event, "npc_id"))
					&& Arrays.asList("select", "offer", "result").contains(value(event, "phase"));
		}

		void close() { closed = true; }
	}

	static final class Candidate {
		final String path;
		final String name;
		final String itemClass;
		final String subclass;
		final ItemRarity rarity;
		final String level;

		Candidate(final String path, final String name, final String itemClass, final String subclass,
				final String rarity, final String level) {
			this.path = path;
			this.name = name;
			this.itemClass = itemClass;
			this.subclass = subclass;
			this.rarity = ItemRarity.fromIdOrCommon(rarity);
			this.level = level;
		}

		String caption() {
			return name + (level.isEmpty() || "0".equals(level) ? "" : " +" + level);
		}
		boolean sameAs(final Candidate other) {
			return path.equals(other.path) && name.equals(other.name) && itemClass.equals(other.itemClass)
					&& subclass.equals(other.subclass) && rarity == other.rarity && level.equals(other.level);
		}
	}

	private static final class Affix {
		private final String id;
		private final String label;
		Affix(final String id, final String label) { this.id = id; this.label = label; }
		boolean sameAs(final Affix other) { return id.equals(other.id) && label.equals(other.label); }
	}

	/** Only the content label is highlighted; its separator remains on the window background. */
	private final class AffixRenderer extends JPanel implements ListCellRenderer<Affix> {
		private static final long serialVersionUID = 1L;
		private final JLabel label = new JLabel();
		private final JLabel separator = new JLabel();

		AffixRenderer() {
			super(new BorderLayout());
			setOpaque(false);
			setBorder(BorderFactory.createEmptyBorder());
			label.setFont(label.getFont().deriveFont(Font.PLAIN));
			separator.setFont(label.getFont());
			separator.setHorizontalAlignment(JLabel.CENTER);
			separator.setOpaque(false);
			separator.setBorder(BorderFactory.createEmptyBorder());
			add(label, BorderLayout.CENTER);
			add(separator, BorderLayout.SOUTH);
		}

		@Override protected void paintComponent(final Graphics graphics) { }

		@Override public Component getListCellRendererComponent(final JList<? extends Affix> list,
				final Affix value, final int index, final boolean selected, final boolean focus) {
			styleCell(label, selected);
			label.setText(value == null ? "" : AffixRerollPresentation.listLabelHtml(value.label,
					!rerolledAffix.isEmpty() && value.id.equals(rerolledAffix), DETAIL_TEXT_WIDTH,
					value.id.startsWith("legendary_")));
			final Style style = StyleUtil.getStyle();
			final Color accent = style == null ? new Color(163, 120, 97) : style.getHighLightColor();
			label.setBorder(BorderFactory.createCompoundBorder(selected
					? BorderFactory.createLineBorder(accent) : BorderFactory.createEmptyBorder(1, 1, 1, 1),
					BorderFactory.createEmptyBorder(0, 6, 0, 6)));
			separator.setText("<html>" + ItemRarityPresentation.dividerHtml() + "</html>");
			separator.setVisible(value != null && index >= 0 && index < affixes.size() - 1);
			return this;
		}
	}

	private static final class CandidateRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;
		@Override
		public Component getListCellRendererComponent(final JList<?> list, final Object value,
				final int index, final boolean selected, final boolean focus) {
			final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
			styleCell(label, selected);
			if (value instanceof Candidate) {
				final Candidate candidate = (Candidate) value;
				label.setText("<html><div style='width:90px'><font color='" + candidate.rarity.getColorHex() + "'>"
						+ escape(candidate.caption()) + "</font><br>"
						+ escape(text(candidate.rarity.getPolishDisplayName())) + "</div></html>");
				label.setIcon(new ItemIcon(candidate.itemClass, candidate.subclass));
				label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
			}
			return label;
		}
	}

	private static final class ItemIcon implements Icon {
		private final Sprite sprite;
		ItemIcon(final String itemClass, final String subclass) {
			final SpriteStore store = SpriteStore.get();
			final Sprite sheet = store.getSprite("data/sprites/items/" + itemClass + "/" + subclass + ".png");
			sprite = sheet.getWidth() > 32
					? store.getAnimatedSprite(sheet, 0, 0, sheet.getWidth() / 32, 32, 32, 100)
					: store.getTile(sheet, 0, 0, 32, 32);
		}
		@Override public int getIconWidth() { return 36; }
		@Override public int getIconHeight() { return 36; }
		@Override public void paintIcon(final Component component, final Graphics graphics, final int x, final int y) {
			sprite.draw(graphics, x + 2, y + 2);
		}
	}
}
