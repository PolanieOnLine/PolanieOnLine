package games.stendhal.client.gui.login;

import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;

import games.stendhal.client.sprite.DataLoader;

/** Compact, equally sized provider buttons using the normal client theme. */
final class ExternalLoginButtons extends JComponent {
	final JButton google;
	final JButton steam;

	ExternalLoginButtons(final ActionListener googleAction, final ActionListener steamAction) {
		setLayout(new GridLayout(1, 2, 6, 0));
		setOpaque(false);
		google = createButton(ExternalLoginProvider.GOOGLE, googleAction);
		steam = createButton(ExternalLoginProvider.STEAM, steamAction);
		add(google);
		add(steam);
	}

	private JButton createButton(final ExternalLoginProvider provider, final ActionListener action) {
		final JButton button = new JButton(provider.label);
		final URL resource = DataLoader.getResource("data/gui/login/" + provider.id + "-white.png");
		if (resource != null) {
			button.setIcon(new ImageIcon(new ImageIcon(resource).getImage().getScaledInstance(14, 14, Image.SCALE_SMOOTH)));
		}
		button.setIconTextGap(5);
		button.setMargin(new Insets(0, 8, 0, 8));
		button.setToolTipText("Zaloguj do gry przez " + provider.label);
		button.addActionListener(action);
		return button;
	}
}
