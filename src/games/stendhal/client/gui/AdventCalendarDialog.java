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
import javax.swing.JComponent;
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
	private final JPanel rewards = new JPanel();
	private final Timer refresh;
	private String requestId;
	private long sentAt;
	private int sequence;
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
		rewards.setBorder(BorderFactory.createEmptyBorder());
		rewards.setPreferredSize(new Dimension(480, 56));
		footer.add(rewards, BorderLayout.NORTH);
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
			rewards.repaint();
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
		date.setText(LocalDate.parse(event.get("date")).format(DateTimeFormatter.ofPattern(
				"d MMMM yyyy", Locale.forLanguageTag(ClientLanguage.getLanguage()))));
		final int today = event.getInt("today");
		for (final Door door : doors.values()) {
			door.state = states.get(door.day - 1);
			door.setEnabled(!"inactive".equals(door.state));
			door.today = today == door.day;
				door.getAccessibleContext().setAccessibleDescription(text("claimed".equals(door.state)
						? "Prezent odebrany" : "available".equals(door.state) ? "Odbierz prezent"
						: "expired".equals(door.state) ? "Termin odbioru minął" : "Prezent jeszcze niedostępny"));
			door.repaint();
		}
		final String message = event.get("message");
		status.setText(text(message.isEmpty() ? "Otwórz dzisiejsze okienko lub odbierz zaległy prezent." : message));
		if ("inactive".equals(event.get("status"))) {
			rewards.removeAll();
			rewards.revalidate();
			rewards.repaint();
		}
		if (event.getInt("revealed_day") > 0 && event.has("reward_names")) {
			rewards.removeAll();
			final List<String> names = event.getList("reward_names");
			final List<String> classes = event.getList("reward_classes");
			final List<String> subclasses = event.getList("reward_subclasses");
			final List<String> quantities = event.getList("reward_quantities");
			for (int i = 0; i < names.size(); i++) {
				final JPanel reward = new JPanel(new BorderLayout(6, 0));
				reward.setBorder(BorderFactory.createEmptyBorder());
				reward.add(new RewardIcon(classes.get(i), subclasses.get(i), Integer.parseInt(quantities.get(i))), BorderLayout.WEST);
				final String name = "money".equals(names.get(i)) ? "monety" : names.get(i);
				reward.add(new JLabel(text(name)), BorderLayout.CENTER);
				rewards.add(reward);
			}
			rewards.revalidate();
			rewards.repaint();
		}
	}

	@Override
	public void dispose() {
		refresh.stop();
		if (active == this) {
			active = null;
		}
		super.dispose();
	}

	private static String text(final String value) {
		return ClientLanguage.getText(value);
	}

	private final class Door extends JButton {
		private static final long serialVersionUID = 1L;
		private final int day;
		private String state = "future";
		private boolean today;
		private final Sprite wood = SpriteStore.get().getSprite("data/gui/panel_wood.jpg");

		private Door(final int day) {
			this.day = day;
			setPreferredSize(new Dimension(76, 84));
			setContentAreaFilled(false);
			setBorderPainted(false);
			getAccessibleContext().setAccessibleName(day + " " + text("grudnia"));
			addActionListener(event -> {
				if (waiting || states.size() != 24) {
					return;
				}
				if ("available".equals(state) || "claimed".equals(state)) {
					send("claimed".equals(state) ? "view" : "claim", day);
				} else {
					status.setText(text("expired".equals(state) ? "Minął już termin odbioru tego prezentu."
							: "Na ten prezent trzeba jeszcze poczekać."));
				}
			});
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
			if (claimed) {
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
	}

	private static final class RewardIcon extends JComponent {
		private static final long serialVersionUID = 1L;
		private final Sprite slot = SpriteStore.get().getSprite("data/gui/slot.png");
		private final Sprite icon;
		private final Sprite quantity;

		private RewardIcon(final String itemClass, final String subclass, final int count) {
			setPreferredSize(new Dimension(42, 42));
			final SpriteStore store = SpriteStore.get();
			final Sprite sheet = store.getSprite("data/sprites/items/" + itemClass + "/" + subclass + ".png");
			icon = sheet.getWidth() > 32
					? store.getAnimatedSprite(sheet, 0, 0, sheet.getWidth() / 32, 32, 32, 100)
					: store.getTile(sheet, 0, 0, 32, 32);
			quantity = count > 1 ? TextSprite.createTextSprite(Integer.toString(count), Color.WHITE) : null;
		}

		@Override
		protected void paintComponent(final Graphics g) {
			slot.draw(g, (getWidth() - slot.getWidth()) / 2, (getHeight() - slot.getHeight()) / 2);
			icon.draw(g, (getWidth() - 32) / 2, (getHeight() - 32) / 2);
			if (quantity != null) {
				quantity.draw(g, (getWidth() + slot.getWidth()) / 2 - quantity.getWidth(),
						(getHeight() - slot.getHeight()) / 2);
			}
		}
	}
}
