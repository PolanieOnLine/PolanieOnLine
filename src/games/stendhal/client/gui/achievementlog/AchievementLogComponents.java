package games.stendhal.client.gui.achievementlog;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;

import games.stendhal.client.gui.layout.SBoxLayout;

public class AchievementLogComponents {
	JComponent getHeaderText(int width, int pad) {
		JComponent spacer = SBoxLayout.createContainer(SBoxLayout.VERTICAL, 0);
		spacer.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
		return spacer;
	}

	JComponent getButtons(Window window) {
		JComponent buttonBox = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, SBoxLayout.COMMON_PADDING);
		buttonBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
		buttonBox.setBorder(BorderFactory.createEmptyBorder(SBoxLayout.COMMON_PADDING, 0,
			SBoxLayout.COMMON_PADDING, SBoxLayout.COMMON_PADDING));

		JButton closeButton = new JButton("Zamknij");
		closeButton.setMnemonic(KeyEvent.VK_C);
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				window.dispose();
			}
		});
		buttonBox.add(closeButton);

		return buttonBox;
	}
}
