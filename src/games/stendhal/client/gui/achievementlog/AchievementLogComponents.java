package games.stendhal.client.gui.achievementlog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JPanel;

import games.stendhal.client.gui.ScrolledViewport;
import games.stendhal.client.gui.imageviewer.ItemListImageViewerEvent.HeaderRenderer;
import games.stendhal.client.gui.layout.SBoxLayout;

public class AchievementLogComponents {
	SummaryPanel getSummaryPanel(int width, int pad) {
		return new SummaryPanel(width, pad);
	}

	FilterPanel getFilterPanel(List<String> categories, int width, int pad) {
		return new FilterPanel(categories, width, pad);
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

	static class SummaryPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private final JLabel overallLabel = new JLabel();
		private final JLabel visibleLabel = new JLabel();
		private final JProgressBar progressBar = new JProgressBar();

		SummaryPanel(int width, int pad) {
			setLayout(new SBoxLayout(SBoxLayout.VERTICAL, pad));
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(pad, pad, pad, pad),
					BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0xDD, 0xDD, 0xDD))));
			setOpaque(true);
			setBackground(new Color(0xF7, 0xF7, 0xF7));
			setAlignmentX(Component.LEFT_ALIGNMENT);
			setMaximumSize(new Dimension(width, Integer.MAX_VALUE));

			overallLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, pad / 2, 0));
			overallLabel.setText("Liczba aktywnych osiągnięć: 0");

			progressBar.setStringPainted(true);
			progressBar.setBorder(BorderFactory.createEmptyBorder());
			progressBar.setMaximum(1);
			progressBar.setValue(0);
			progressBar.setString("0%");

			visibleLabel.setBorder(BorderFactory.createEmptyBorder(pad / 2, 0, 0, 0));
			visibleLabel.setText("Widoczne osiągnięcia: 0");

			add(overallLabel);
			add(progressBar);
			add(visibleLabel);
		}

		void update(List<AchievementLog.AchievementEntry> all,
				List<AchievementLog.AchievementEntry> visible) {
			int total = all.size();
			int completed = countCompleted(all);
			int visibleTotal = visible.size();
			int visibleCompleted = countCompleted(visible);

			overallLabel.setText(String.format(
					"<html><div style=\"font-size:12px;\">Zdobyto <b>%d</b> z <b>%d</b> dostępnych osiągnięć</div></html>",
					completed, total));

			progressBar.setMaximum(Math.max(total, 1));
			progressBar.setValue(completed);
			int percent = total == 0 ? 0 : (int) Math.round((completed * 100.0) / total);
			if (total == 0) {
				progressBar.setString("0%");
			} else {
				progressBar.setString(String.format("%d%% (%d/%d)", percent, completed, total));
			}

			visibleLabel.setText(String.format(
					"<html><div style=\"font-size:11px; color:#555555;\">Widoczne: %d (w tym %d zdobytych)</div></html>",
					visibleTotal, visibleCompleted));
		}

		private int countCompleted(List<AchievementLog.AchievementEntry> entries) {
			int count = 0;
			for (AchievementLog.AchievementEntry entry : entries) {
				if (entry.reached) {
					count++;
				}
			}
			return count;
		}
	}

	static class FilterPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		interface Listener {
			void onFilterChanged(String category, StatusFilter status);
		}

		private final JComboBox<String> categoryCombo;
		private final JComboBox<StatusFilter> statusCombo;
		private Listener listener;

		FilterPanel(List<String> categories, int width, int pad) {
			setLayout(new SBoxLayout(SBoxLayout.HORIZONTAL, pad));
			setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEmptyBorder(pad, pad, pad, pad),
					BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(0xDD, 0xDD, 0xDD))));
			setOpaque(true);
			setBackground(Color.WHITE);
			setAlignmentX(Component.LEFT_ALIGNMENT);
			setMaximumSize(new Dimension(width, Integer.MAX_VALUE));

			add(new JLabel("Kategoria:"));

			categoryCombo = new JComboBox<>();
			categoryCombo.addItem("Wszystkie kategorie");
			for (String category : categories) {
				categoryCombo.addItem(category);
			}
			categoryCombo.setMaximumSize(categoryCombo.getPreferredSize());
			categoryCombo.setToolTipText("Filtruje osiągnięcia według kategorii");
			add(categoryCombo);

			add(new JLabel("Status:"));

			statusCombo = new JComboBox<>(StatusFilter.values());
			statusCombo.setMaximumSize(statusCombo.getPreferredSize());
			statusCombo.setToolTipText("Wybiera widoczne osiągnięcia na podstawie statusu");
			add(statusCombo);

			ActionListener listener = new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					notifyListener();
				}
			};
			categoryCombo.addActionListener(listener);
			statusCombo.addActionListener(listener);
		}

		void setListener(Listener listener) {
			this.listener = listener;
			notifyListener();
		}

		private void notifyListener() {
			if (listener == null) {
				return;
			}
			String selectedCategory = categoryCombo.getSelectedIndex() <= 0 ? null
					: (String) categoryCombo.getSelectedItem();
			StatusFilter status = (StatusFilter) statusCombo.getSelectedItem();
			listener.onFilterChanged(selectedCategory, status);
		}
	}

	enum StatusFilter {
		ALL("Wszystkie osiągnięcia"),
		COMPLETED("Tylko zdobyte"),
		LOCKED("Tylko do zdobycia");

		private final String label;

		StatusFilter(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return label;
		}
	}
}
