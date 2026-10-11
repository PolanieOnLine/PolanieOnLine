package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.GameScreen;
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
			if (players.getSelectedItem() != null) { send(players.getSelectedItem().toString()); }
		});
		refresh.addActionListener(event -> send("lista"));
		leave.addActionListener(event -> send("powrot"));
		addCloseListener(window -> {
			if (!serverClosing) { send("zamknij"); }
			instance = null;
		});
	}

	private static void send(final String choice) {
		final RPAction action = new RPAction();
		action.put("type", "chat");
		action.put("text", "obserwuj " + choice);
		StendhalClient.get().send(action);
	}

	public static void show(final RPEvent event) {
		SwingUtilities.invokeLater(() -> {
			final String phase = event.get("arena_phase");
			if ("close".equals(phase)) {
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
			instance.players.removeAllItems();
			for (final String option : event.get("options").split("\t")) {
				final String[] parts = option.split("\\|~\\|");
				if (parts.length >= 2) { instance.players.addItem(parts[1]); }
			}
			if (selected != null) { instance.players.setSelectedItem(selected); }
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
