package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
		final JPanel content = new JPanel(new BorderLayout(8, 8));
		content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		content.setPreferredSize(new Dimension(360, 125));
		content.add(status, BorderLayout.NORTH);
		content.add(players, BorderLayout.CENTER);
		final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
		final JButton refresh = new JButton("Odśwież");
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
			final String phase = event.get("arena_phase");
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
			final Object selected = instance.players.getSelectedItem();
			final List<String> names = choices(event);
			instance.players.removeAllItems();
			for (final String name : names) {
				instance.players.addItem(name);
			}
			if (selected != null && names.contains(selected)) { instance.players.setSelectedItem(selected); }
			final boolean empty = instance.players.getItemCount() == 0;
			instance.observe.setEnabled(!empty);
			// Server text is plain text, never Swing's implicit HTML renderer.
			instance.status.putClientProperty("html.disable", Boolean.TRUE);
			instance.status.setText(empty ? "Nikt teraz nie walczy na arenie." : event.get("title"));
			instance.leave.setVisible("watch".equals(phase));
			instance.setVisible(true);
			instance.raise();
		});
	}
}
