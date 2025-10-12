package games.stendhal.client.gui.achievementlog;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import games.stendhal.client.gui.layout.SBoxLayout;

public class AchievementLogComponents {
	JComponent getHeaderText(int width, int pad) {
		List<String> achievementList = AchievementLogController.get().getList();
		int achievementCount = achievementList.size();

		JLabel header = new JLabel();
		header.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
		StringBuilder text = new StringBuilder();
		text.append("<html><div width=").append(width - 10).append(">Liczba aktywnych osiągnięć: ");
		text.append(achievementCount);
		text.append("<br/><span style="font-size: 11px; color: #6b5842;">");
		text.append("Użyj kafelków filtrów i przycisków nawigacji, aby przeglądać dziennik niczym księgę.");
		text.append("</span></div></html>");
		header.setText(text.toString());

		return header;
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
