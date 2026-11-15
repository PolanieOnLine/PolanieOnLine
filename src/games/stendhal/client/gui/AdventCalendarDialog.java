package games.stendhal.client.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.client.sprite.TextSprite;
import games.stendhal.common.AdventCalendarLayout;
import games.stendhal.common.constants.Actions;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;
import marauroa.common.game.RPObject;

/** Modeless seasonal dialog. Replies never create or reopen a closed window. */
public final class AdventCalendarDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private static AdventCalendarDialog active;
	private final RPObject owner = StendhalClient.get().getPlayer();
	private final String windowId = UUID.randomUUID().toString();
	private final Map<Integer, Door> doors = new HashMap<>();
	private final JLabel date = new JLabel();
	private final JLabel status = new JLabel(text("Wczytywanie kalendarza…"), JLabel.CENTER);
	private final Timer refresh;
	private String requestId;
	private long sentAt;
	private int sequence;
	private int displayedYear;
	private boolean waiting;
	private final List<String> states = new ArrayList<>();

	private AdventCalendarDialog(final Frame parent) {
		super(parent, text("Kalendarz adwentowy"), false);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		final JPanel content = new JPanel(new BorderLayout(0, 10));
		content.setBorder(BorderFactory.createEmptyBorder(12, 12, 10, 12));
		final JPanel heading = new JPanel(new BorderLayout());
		heading.setBorder(BorderFactory.createEmptyBorder());
		heading.add(new JLabel(text("Świąteczne podarunki")), BorderLayout.WEST);
		heading.add(date, BorderLayout.EAST);
		content.add(heading, BorderLayout.NORTH);
		final JPanel board = new JPanel(new GridBagLayout());
		board.setBorder(BorderFactory.createEmptyBorder());
		for (final int[] cell : AdventCalendarLayout.doors()) {
			final Door door = new Door(cell[0]);
			door.addActionListener(event -> {
				if (waiting || states.size() != 24) return;
				if ("available".equals(door.state) || "claimed".equals(door.state)) {
					send("claimed".equals(door.state) ? "view" : "claim", door.day);
				} else {
					status.setText(text("expired".equals(door.state) ? "Minął już termin odbioru tego prezentu."
							: "Na ten prezent trzeba jeszcze poczekać."));
				}
			});
			doors.put(cell[0], door);
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = cell[1];
			constraints.gridy = cell[2];
			constraints.gridwidth = cell[3];
			constraints.gridheight = cell[4];
			constraints.weightx = cell[3];
			constraints.weighty = cell[4];
			constraints.fill = GridBagConstraints.BOTH;
			constraints.insets = new Insets(4, 4, 4, 4);
			board.add(door, constraints);
		}
		board.setPreferredSize(new Dimension(504, 454));
		content.add(board, BorderLayout.CENTER);
		final JPanel footer = new JPanel(new BorderLayout(0, 8));
		footer.setBorder(BorderFactory.createEmptyBorder());
		footer.add(status, BorderLayout.CENTER);
		final JButton close = new JButton(text("Zamknij"));
		close.addActionListener(event -> dispose());
		final JPanel buttons = new JPanel(new BorderLayout());
		buttons.setBorder(BorderFactory.createEmptyBorder());
		buttons.add(close, BorderLayout.EAST);
		footer.add(buttons, BorderLayout.SOUTH);
		content.add(footer, BorderLayout.SOUTH);
		setContentPane(content);
		WindowUtils.closeOnEscape(this);
		pack();
		setMinimumSize(getSize());
		setLocationRelativeTo(parent);
		refresh = new Timer(1000, event -> {
			for (final Door door : doors.values()) {
				if (door.showingRewards()) door.repaint();
			}
			if (StendhalClient.get().getPlayer() != owner) {
				dispose();
			} else if (waiting && System.currentTimeMillis() - sentAt > 8000) {
				waiting = false;
				status.setText(text("Nie udało się odświeżyć kalendarza. Spróbuj ponownie."));
			} else if (!waiting && System.currentTimeMillis() - sentAt > 30000) {
				send("refresh", 0);
			}
		});
		refresh.setDelay(100);
		refresh.start();
	}

	public static void open() {
		SwingUtilities.invokeLater(() -> {
			if (StendhalClient.get().getPlayer() == null) {
				return;
			}
			if (active == null || active.owner != StendhalClient.get().getPlayer()) {
				if (active != null) {
					active.dispose();
				}
				active = new AdventCalendarDialog(j2DClient.get().getMainFrame());
			}
			active.setVisible(true);
			active.toFront();
			if (!active.waiting) {
				active.send("open", 0);
			}
		});
	}

	public static void show(final RPEvent event) {
		SwingUtilities.invokeLater(() -> {
			if (active != null && active.isDisplayable() && event.has("request_id")
					&& event.get("request_id").equals(active.requestId)
					&& StendhalClient.get().getPlayer() == active.owner) {
				active.apply(event);
			}
		});
	}

	private void send(final String command, final int day) {
		if (waiting) {
			return;
		}
		requestId = windowId + ":" + ++sequence;
		final RPAction action = new RPAction();
		action.put("type", Actions.ADVENT_CALENDAR);
		action.put("command", command);
		action.put("request_id", requestId);
		action.put("day", day);
		waiting = true;
		sentAt = System.currentTimeMillis();
		StendhalClient.get().send(action);
	}

	private void apply(final RPEvent event) {
		waiting = false;
		states.clear();
		states.addAll(event.getList("states"));
		final LocalDate calendarDate = LocalDate.parse(event.get("date"));
		if (displayedYear != calendarDate.getYear()) {
			for (final Door door : doors.values()) door.clearRewards();
			displayedYear = calendarDate.getYear();
		}
		date.setText(calendarDate.format(DateTimeFormatter.ofPattern(
				"d MMMM yyyy", Locale.forLanguageTag(ClientLanguage.getLanguage()))));
		final int today = event.getInt("today");
		for (final Door door : doors.values()) {
			door.updateState(states.get(door.day - 1), today == door.day);
			door.setEnabled(!"inactive".equals(door.state));
			door.getAccessibleContext().setAccessibleDescription(text("claimed".equals(door.state)
					? "Prezent odebrany" : "available".equals(door.state) ? "Odbierz prezent"
					: "expired".equals(door.state) ? "Termin odbioru minął" : "Prezent jeszcze niedostępny")
					+ (door.showingRewards() ? ". " + door.rewardDescription() : ""));
			door.repaint();
		}
		final String message = event.get("message");
		status.setText(text(message.isEmpty() ? "Otwórz dzisiejsze okienko lub odbierz zaległy prezent." : message));
		if (event.getInt("revealed_day") > 0 && event.has("reward_names")) {
			final Door door = doors.get(event.getInt("revealed_day"));
			if (door == null || !"claimed".equals(door.state)) return;
			final List<String> names = event.getList("reward_names");
			final List<String> classes = event.getList("reward_classes");
			final List<String> subclasses = event.getList("reward_subclasses");
			final List<String> quantities = event.getList("reward_quantities");
			final List<RewardIcon> icons = new ArrayList<>();
			for (int i = 0; i < names.size(); i++) {
				final String name = "money".equals(names.get(i)) ? "monety" : names.get(i);
				icons.add(new RewardIcon(classes.get(i), subclasses.get(i), text(name), Integer.parseInt(quantities.get(i))));
			}
			door.reveal(icons);
		}
	}

	@Override
	public void dispose() {
		refresh.stop();
		for (final Door door : doors.values()) door.clearRewards();
		if (active == this) {
			active = null;
		}
		super.dispose();
	}

	private static String text(final String value) {
		return ClientLanguage.getText(value);
	}

	static final class Door extends JButton {
		private static final long serialVersionUID = 1L;
		private final int day;
		private String state = "future";
		private boolean today;
		private final List<RewardIcon> rewards = new ArrayList<>();
		private final Sprite wood = SpriteStore.get().getSprite("data/gui/panel_wood.jpg");

		Door(final int day) {
			this.day = day;
			setPreferredSize(new Dimension(76, 84));
			setContentAreaFilled(false);
			setBorderPainted(false);
			getAccessibleContext().setAccessibleName(day + " " + text("grudnia"));
		}

		void updateState(final String nextState, final boolean isToday) {
			state = nextState;
			today = isToday;
			if (!"claimed".equals(state)) clearRewards();
			repaint();
		}

		void reveal(final List<RewardIcon> icons) {
			if (!"claimed".equals(state)) return;
			rewards.clear();
			rewards.addAll(icons);
			final StringBuilder hint = new StringBuilder("<html>");
			for (final RewardIcon icon : icons) {
				hint.append(icon.name).append(" × ").append(icon.count).append("<br>");
			}
			setToolTipText(hint.append("</html>").toString());
			getAccessibleContext().setAccessibleDescription(text("Prezent odebrany") + ". " + rewardDescription());
			repaint();
		}

		private String rewardDescription() {
			final StringBuilder description = new StringBuilder();
			for (final RewardIcon icon : rewards) {
				if (description.length() > 0) description.append(", ");
				description.append(icon.name).append(" × ").append(icon.count);
			}
			return description.toString();
		}

		void clearRewards() {
			rewards.clear();
			setToolTipText(null);
			repaint();
		}

		boolean showingRewards() {
			return "claimed".equals(state) && !rewards.isEmpty();
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			final Graphics2D g = (Graphics2D) graphics.create();
			final int width = getWidth();
			final int height = getHeight();
			for (int y = 0; y < height; y += wood.getHeight()) {
				for (int x = 0; x < width; x += wood.getWidth()) {
					wood.draw(g, x, y);
				}
			}
			final boolean claimed = "claimed".equals(state);
			final boolean expired = "expired".equals(state);
			g.setColor(claimed || expired ? new Color(20, 12, 7, 160)
					: day % 3 == 0 ? new Color(30, 55, 36, 80) : new Color(65, 20, 18, 45));
			g.fillRect(0, 0, width, height);
			g.setColor(new Color(171, 129, 79));
			g.drawRect(1, 1, width - 3, height - 3);
			g.setColor(new Color(45, 24, 12));
			g.drawRect(3, 3, width - 7, height - 7);
			if (claimed) paintOpenDoor(g, width, height);
			if (day == 24 && !claimed) {
				g.drawLine(width / 2, 3, width / 2, height - 4);
				g.setColor(new Color(186, 145, 88));
				g.drawLine(width / 2 + 1, 3, width / 2 + 1, height - 4);
			}
			g.setFont(claimed ? getFont().deriveFont(14f) : new Font("Georgia", Font.BOLD, day == 24 ? 30 : 24));
			g.setColor(expired ? new Color(157, 139, 113) : new Color(255, 223, 157));
			final String number = Integer.toString(day);
			final int x = claimed ? width - g.getFontMetrics().stringWidth(number) - 7
					: (width - g.getFontMetrics().stringWidth(number)) / 2;
			final int y = claimed ? 19 : (height + g.getFontMetrics().getAscent()) / 2 - 3;
			if (day == 24 && !claimed) {
				final int labelWidth = g.getFontMetrics().stringWidth(number) + 18;
				g.setColor(new Color(73, 42, 18));
				g.fillRect((width - labelWidth) / 2, height / 2 - 20, labelWidth, 40);
				g.setColor(new Color(185, 147, 85));
				g.drawRect((width - labelWidth) / 2, height / 2 - 20, labelWidth, 40);
				g.setColor(new Color(255, 223, 157));
			}
			g.drawString(number, x, y);
			if (showingRewards()) {
				paintRewards(g, width, height);
			} else if (claimed) {
				g.setFont(getFont().deriveFont(24f));
				g.drawString("✓", width / 2 - 9, height / 2 + 9);
			}
			if (today) {
				g.setColor(Color.WHITE);
				g.setStroke(new BasicStroke(2));
				g.drawRect(1, 1, width - 3, height - 3);
			}
			if (hasFocus()) {
				g.setColor(Color.WHITE);
				g.drawRect(5, 5, width - 11, height - 11);
			}
			g.dispose();
		}

		private int leafWidth(final int width) {
			return Math.max(10, Math.min(18, width / 6));
		}

		private void paintOpenDoor(final Graphics2D g, final int width, final int height) {
			g.setColor(new Color(17, 12, 9, 220));
			g.fillRect(4, 4, width - 8, height - 8);
			g.setColor(new Color(7, 5, 3));
			g.drawRect(5, 5, width - 11, height - 11);
			g.setColor(new Color(104, 73, 43));
			g.drawLine(6, height - 6, width - 6, height - 6);
			paintLeaf(g, width, height, false);
			if (day == 24) paintLeaf(g, width, height, true);
		}

		private void paintLeaf(final Graphics2D g, final int width, final int height, final boolean right) {
			final int hinge = right ? width - 4 : 4;
			final int edge = right ? width - leafWidth(width) : leafWidth(width);
			final Polygon leaf = new Polygon(new int[] {hinge, edge, edge, hinge},
					new int[] {4, 12, height - 12, height - 4}, 4);
			final Graphics2D panel = (Graphics2D) g.create();
			panel.clip(leaf);
			for (int y = 0; y < height; y += wood.getHeight()) {
				wood.draw(panel, right ? width - wood.getWidth() : 0, y);
			}
			panel.setColor(new Color(30, 16, 7, 80));
			panel.fillRect(0, 0, width, height);
			panel.dispose();
			g.setColor(new Color(182, 135, 76));
			g.drawPolygon(leaf);
			g.setColor(new Color(58, 35, 18));
			g.drawLine(edge + (right ? 2 : -2), 14, edge + (right ? 2 : -2), height - 14);
			g.setColor(new Color(162, 136, 90));
			for (final int y : new int[] {height / 3, height * 2 / 3}) {
				g.fillRect(right ? width - 7 : 2, y, 5, 4);
			}
		}

		Rectangle rewardArea(final int width, final int height) {
			final int left = leafWidth(width) + 5;
			final int right = day == 24 ? leafWidth(width) + 5 : 7;
			return new Rectangle(left, 23, Math.max(0, width - left - right), Math.max(0, height - 31));
		}

		private void paintRewards(final Graphics2D g, final int width, final int height) {
			final Rectangle area = rewardArea(width, height);
			int size = 36;
			int columns;
			int rows;
			do {
				columns = Math.max(1, area.width / size);
				rows = (rewards.size() + columns - 1) / columns;
				if (rows * size <= area.height) break;
				size -= 4;
			} while (size > 12);
			final int gridWidth = Math.min(columns, rewards.size()) * size;
			final int x = area.x + (area.width - gridWidth) / 2;
			final int y = area.y + (area.height - rows * size) / 2;
			final Graphics2D items = (Graphics2D) g.create();
			items.clip(area);
			for (int index = 0; index < rewards.size(); index++) {
				rewards.get(index).draw(items, x + index % columns * size, y + index / columns * size, size);
			}
			items.dispose();
		}
	}

	static final class RewardIcon {
		private final Sprite slot = SpriteStore.get().getSprite("data/gui/slot.png");
		private final Sprite icon;
		private final Sprite quantity;
		private final String name;
		private final int count;

		RewardIcon(final String itemClass, final String subclass, final String name, final int count) {
			this.name = name;
			this.count = count;
			final SpriteStore store = SpriteStore.get();
			final Sprite sheet = store.getSprite("data/sprites/items/" + itemClass + "/" + subclass + ".png");
			icon = sheet.getWidth() > 32
					? store.getAnimatedSprite(sheet, 0, 0, sheet.getWidth() / 32, 32, 32, 100)
					: store.getTile(sheet, 0, 0, 32, 32);
			final String label = count > 9999999 ? count / 1000000 + "M"
					: count > 9999 ? count / 1000 + "K" : Integer.toString(count);
			quantity = count > 1 ? TextSprite.createTextSprite(label, Color.WHITE) : null;
		}

		void draw(final Graphics2D graphics, final int x, final int y, final int size) {
			final Graphics2D g = (Graphics2D) graphics.create();
			g.translate(x, y);
			g.scale(size / 42.0, size / 42.0);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			slot.draw(g, (42 - slot.getWidth()) / 2, (42 - slot.getHeight()) / 2);
			icon.draw(g, 5, 5);
			if (quantity != null) {
				final double scale = Math.min(1.0, 40.0 / quantity.getWidth());
				g.translate(41 - quantity.getWidth() * scale, 1);
				g.scale(scale, scale);
				quantity.draw(g, 0, 0);
			}
			g.dispose();
		}
	}
}
