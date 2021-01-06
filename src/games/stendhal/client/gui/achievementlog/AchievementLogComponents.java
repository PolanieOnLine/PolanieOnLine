package games.stendhal.client.gui.achievementlog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import games.stendhal.client.gui.ScrolledViewport;
import games.stendhal.client.gui.imageviewer.ItemListImageViewerEvent.HeaderRenderer;
import games.stendhal.client.gui.layout.SBoxLayout;

public class AchievementLogComponents {
	JComponent getHeaderText(int width, int pad) {
		final StringBuilder headerText = new StringBuilder();
		List<String> achievementList = AchievementLogController.get().getList();
		final int achievementCount = achievementList.size();

		JLabel header = new JLabel();
		header.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
		headerText.append("Liczba aktywnych osiągnięć: " + achievementCount);

		header.setText("<html><div width=" + (width
				- 10) + ">" + headerText.toString() + "</div></html>");

		return header;
	}

	JComponent getViewTable(JComponent table, int width, int height, int pad) {
		HeaderRenderer hr = new HeaderRenderer();
		ScrolledViewport viewPort = new ScrolledViewport(table);
		viewPort.getComponent().setPreferredSize(new Dimension(width,
				Math.min(height, table.getPreferredSize().height
						+ hr.getPreferredSize().height + 4 * pad)));
		viewPort.getComponent().setBackground(table.getBackground());

		return viewPort.getComponent();
	}

	JComponent getButtons(Window window) {
		JComponent buttonBox = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, SBoxLayout.COMMON_PADDING);
		buttonBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
		buttonBox.setBorder(BorderFactory.createEmptyBorder(SBoxLayout.COMMON_PADDING,
				0, SBoxLayout.COMMON_PADDING, SBoxLayout.COMMON_PADDING));

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
