package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.GameScreen;
import games.stendhal.common.constants.Actions;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;

/** Select a live arena fight and leave the read-only view at any time. */
public final class ArenaObservationWindow extends InternalManagedWindow {
	private static final long serialVersionUID = 1L;
	private static ArenaObservationWindow instance;
	private final JLabel status = new JLabel();
	private final JComboBox<String> players = new JComboBox<>();
	private final JButton observe = new JButton("Obserwuj");
	private final JButton leave = new JButton("Powrót do lobby");
	private boolean serverClosing;

	private ArenaObservationWindow() {
		super("arena-observation", "Obserwacja areny");
		setCloseable(true);
		setMinimizable(false);
		final JPanel content = new LayoutPanel(new BorderLayout(6, 6)) {
			private static final long serialVersionUID = 1L;
			@Override
			public Dimension getPreferredSize() {
				final Dimension size = super.getPreferredSize();
				return new Dimension(Math.max(300, size.width), size.height);
			}
		};
		content.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
		content.add(status, BorderLayout.NORTH);
		content.add(players, BorderLayout.CENTER);
		final JPanel buttons = new LayoutPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
		final JButton refresh = new JButton("Odśwież");
		for (final JButton button : new JButton[] {observe, refresh, leave}) {
			button.setMargin(new Insets(2, 8, 2, 8));
			button.setFocusable(false);
		}
		buttons.add(observe);
		buttons.add(refresh);
		buttons.add(leave);
		content.add(buttons, BorderLayout.SOUTH);
		setContent(content);
		observe.addActionListener(event -> {
			if (players.getSelectedItem() != null) { send("watch", players.getSelectedItem().toString()); }
		});
		refresh.addActionListener(event -> send("list", null));
		leave.addActionListener(event -> send("return", null));
		addCloseListener(window -> {
			if (!serverClosing) { send("close", null); }
			instance = null;
		});
	}

	/** Layout containers use the enclosing window's background. */
	private static class LayoutPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		LayoutPanel(final LayoutManager layout) {
			super(layout);
			setOpaque(false);
			setBorder(BorderFactory.createEmptyBorder());
		}
		@Override
		protected void paintComponent(final Graphics graphics) {
			// Keep one shared background instead of painting nested panels.
		}
	}

	static RPAction createAction(final String operation, final String target) {
		final RPAction action = new RPAction();
		action.put(Actions.TYPE, Actions.ARENA_OBSERVATION);
		action.put("operation", operation);
		if (target != null) { action.put("target", target); }
		return action;
	}

	private static void send(final String operation, final String target) {
		StendhalClient.get().send(createAction(operation, target));
	}

	static List<String> choices(final RPEvent event) {
		final List<String> names = new ArrayList<>();
		for (final String option : event.get("options").split("\t")) {
			final String[] parts = option.split("\\|~\\|");
			if (parts.length >= 2 && !parts[1].isEmpty()) { names.add(parts[1]); }
		}
		return names;
	}

	static boolean shouldClose(final RPEvent event) {
		return "close".equals(event.get("arena_phase"))
				|| ("open".equals(event.get("arena_phase")) && choices(event).isEmpty());
	}

	public static void show(final RPEvent event) {
		SwingUtilities.invokeLater(() -> {
			if (shouldClose(event)) {
				GameScreen.get().resumePlayerView();
				if (instance != null) {
					instance.serverClosing = true;
					instance.close();
					instance = null;
				}
				return;
			}
			if (instance == null) {
				instance = new ArenaObservationWindow();
				j2DClient.get().addWindow(instance);
			}
			instance.apply(event);
			instance.setVisible(true);
			instance.raise();
		});
	}

	private void apply(final RPEvent event) {
		final Object selected = players.getSelectedItem();
		final List<String> names = choices(event);
		players.removeAllItems();
		for (final String name : names) {
			players.addItem(name);
		}
		if (selected != null && names.contains(selected)) { players.setSelectedItem(selected); }
		final boolean empty = players.getItemCount() == 0;
		observe.setEnabled(!empty);
		// Server text is plain text, never Swing's implicit HTML renderer.
		status.putClientProperty("html.disable", Boolean.TRUE);
		status.setText(empty ? "Nikt teraz nie walczy na arenie." : event.get("title"));
		leave.setVisible("watch".equals(event.get("arena_phase")));
		setSize(getPreferredSize());
		revalidate();
		repaint();
	}
}
