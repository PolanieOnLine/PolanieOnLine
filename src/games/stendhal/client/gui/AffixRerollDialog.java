/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.TitledBorder;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.constants.Actions;
import games.stendhal.common.constants.ItemRarity;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;

/** Modeless affix transformation service. Only server-authorized items and affixes can be selected. */
public final class AffixRerollDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private static final Set<String> openedWindows = new HashSet<>();
	private static RPObject sessionOwner;
	private static AffixRerollDialog active;

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

	private AffixRerollDialog(final Frame parent, final RPObject owner, final RPEvent event) {
		super(parent, text("Przemir — przemiana afiksów"), false);
		this.owner = owner;
		correlation = new Correlation(value(event, "window_id"), value(event, "npc_id"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		final JPanel content = panel(new BorderLayout(0, 10));
		content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		content.add(new JLabel(text("Wybierz przedmiot i jeden afiks do przemiany.")), BorderLayout.NORTH);

		final JPanel center = panel(new BorderLayout(0, 10));
		final JPanel lists = panel(new GridLayout(1, 2, 10, 0));
		itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		affixList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		itemList.setCellRenderer(new CandidateRenderer());
		affixList.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value,
					final int index, final boolean selected, final boolean focus) {
				final JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, selected, focus);
				styleCell(label, selected);
				label.setText(value instanceof Affix ? AffixRerollPresentation.labelHtml(((Affix) value).label, ((Affix) value).id.equals(rerolledAffix), 250) : "");
				label.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
				return label;
			}
		});
		lists.add(listPanel(text("Przedmioty"), itemList));
		lists.add(listPanel(text("Afiksy przedmiotu"), affixList));
		center.add(lists, BorderLayout.CENTER);

		final JPanel comparison = panel(new BorderLayout(0, 6));
		comparison.setBorder(sectionBorder(text("Porównanie afiksów")));
		final JPanel labels = panel(new GridLayout(2, 1, 0, 6));
		labels.add(current);
		labels.add(replacement);
		comparison.add(labels, BorderLayout.CENTER);
		final JPanel choices = panel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		choices.add(keep);
		choices.add(accept);
		comparison.add(choices, BorderLayout.SOUTH);
		center.add(comparison, BorderLayout.SOUTH);
		content.add(center, BorderLayout.CENTER);

		final JPanel footer = panel(new BorderLayout(0, 7));
		final JPanel payment = panel(new BorderLayout(0, 4));
		cost.setIcon(new ItemIcon("resource", "transformation_stone"));
		payment.add(cost, BorderLayout.NORTH);
		payment.add(new JLabel(html(text("Zwykły afiks kosztuje 1 kamień przemiany, a legendarny 3 kamienie. Zachowanie obecnego wyniku również zużywa kamienie."), 570)), BorderLayout.SOUTH);
		footer.add(payment, BorderLayout.NORTH);
		footer.add(status, BorderLayout.CENTER);
		final JPanel buttons = panel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
		buttons.add(roll);
		buttons.add(refresh);
		final JButton close = new JButton(text("Zamknij"));
		buttons.add(close);
		footer.add(buttons, BorderLayout.SOUTH);
		content.add(footer, BorderLayout.SOUTH);
		setContentPane(content);
		WindowUtils.closeOnEscape(this);

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
		close.addActionListener(change -> dispose());
		timer = new Timer(1000, tick -> {
			if (StendhalClient.get().getPlayer() != this.owner) {
				dispose();
			} else if (waiting && System.currentTimeMillis() - sentAt >= 8000) {
				waiting = false;
				status.setText(html(text("Oczekiwanie na odpowiedź. Odświeżanie stanu przemiany…"), 570));
				send("refresh");
			} else if (!waiting && System.currentTimeMillis() - sentAt >= 30000) {
				send("refresh");
			}
			itemList.repaint();
		});
		apply(event);
		pack();
		setMinimumSize(getSize());
		setLocationRelativeTo(parent);
		timer.start();
	}

	/** Events belong to the current player; later replies never create a window. */
	public static void show(final RPEvent event, final RPObject eventOwner) {
		final RPObject player = StendhalClient.get().getPlayer();
		if (player == null || eventOwner == null || !player.getID().equals(eventOwner.getID())) return;
		SwingUtilities.invokeLater(() -> {
			if (StendhalClient.get().getPlayer() != player) return;
			if (sessionOwner != player) {
				if (active != null) active.dispose();
				sessionOwner = player;
				openedWindows.clear();
			}
			if ("open".equals(value(event, "phase"))) {
				final String window = value(event, "window_id");
				if (window.isEmpty() || value(event, "npc_id").isEmpty() || !openedWindows.add(window)) return;
				if (active != null) active.dispose();
				active = new AffixRerollDialog(j2DClient.get().getMainFrame(), player, event);
				active.setVisible(true);
				active.toFront();
			} else if (active != null && active.isDisplayable() && active.owner == player
					&& active.correlation.accepts(event)) {
				active.apply(event);
			}
		});
	}

	public static void closeOnZoneChange() {
		SwingUtilities.invokeLater(() -> { if (active != null) active.dispose(); });
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
			items.clear();
			for (final Candidate candidate : candidates(event)) {
				items.addElement(candidate);
				if (candidate.path.equals(selectedPath)) itemList.setSelectedValue(candidate, true);
			}
			affixes.clear();
			final List<String> ids = list(event, "affix_ids");
			final List<String> labels = list(event, "affix_labels");
			for (int index = 0; index < Math.min(ids.size(), labels.size()); index++) {
				final Affix affix = new Affix(ids.get(index), labels.get(index));
				affixes.addElement(affix);
				if (affix.id.equals(selectedAffix)) affixList.setSelectedValue(affix, true);
			}
		} finally {
			updating = false;
		}
		current.setText(AffixRerollPresentation.labelHtml(value(event, "current_label"), !rerolledAffix.isEmpty() && rerolledAffix.equals(selectedAffix), 550, text("Obecny afiks:") + " "));
		replacement.setText(html(text("Nowy afiks:") + " " + (offerToken.isEmpty() ? "—" : value(event, "new_label")), 550));
		cost.setText(text("Koszt:") + " " + ("3".equals(value(event, "stone_cost")) ? "3" : "1".equals(value(event, "stone_cost")) ? "1" : "1 / 3") + " × " + text("kamień przemiany") + "   ·   "
				+ text("Posiadasz:") + " " + value(event, "stone_count"));
		final String message = value(event, "message");
		status.setText(html(message.isEmpty() ? text(offerToken.isEmpty()
				? "Wybierz przedmiot i jeden afiks do przemiany." : "Wybierz, który afiks zachować." ) : message, 570));
		updateControls();
	}

	private void updateControls() {
		final boolean offer = !offerToken.isEmpty();
		itemList.setEnabled(!waiting && !offer);
		affixList.setEnabled(!waiting && !offer);
		roll.setEnabled(!waiting && !offer && canRoll && !requestToken.isEmpty()
				&& !selectedPath.isEmpty() && !selectedAffix.isEmpty());
		keep.setEnabled(!waiting && offer);
		accept.setEnabled(!waiting && offer && canAccept);
		refresh.setEnabled(!waiting);
	}

	@Override
	public void dispose() {
		if (!closed) {
			closed = true;
			timer.stop();
			if (StendhalClient.get().getPlayer() == owner) StendhalClient.get().send(action("close"));
			correlation.close();
			items.clear();
			affixes.clear();
			requestToken = "";
			offerToken = "";
			if (active == this) active = null;
		}
		super.dispose();
	}

	private static JPanel panel(final java.awt.LayoutManager layout) {
		final JPanel panel = new JPanel(layout);
		panel.setBorder(BorderFactory.createEmptyBorder());
		return panel;
	}

	private static JPanel listPanel(final String title, final JList<?> list) {
		final JPanel panel = panel(new BorderLayout());
		panel.setBorder(sectionBorder(title));
		list.setOpaque(false);
		final JScrollPane scroll = new JScrollPane(list);
		scroll.setOpaque(false);
		scroll.getViewport().setOpaque(false);
		scroll.setPreferredSize(new Dimension(280, 180));
		panel.add(scroll, BorderLayout.CENTER);
		return panel;
	}

	private static TitledBorder sectionBorder(final String title) {
		final TitledBorder border = BorderFactory.createTitledBorder(title);
		border.setTitleColor(new JLabel().getForeground());
		return border;
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
	}

	private static final class Affix {
		private final String id;
		private final String label;
		Affix(final String id, final String label) { this.id = id; this.label = label; }
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
				label.setText("<html><font color='" + candidate.rarity.getColorHex() + "'>"
						+ escape(candidate.caption()) + "</font><br>"
						+ escape(text(candidate.rarity.getPolishDisplayName())) + " · #" + (index + 1) + "</html>");
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
