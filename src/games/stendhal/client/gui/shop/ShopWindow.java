package games.stendhal.client.gui.shop;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import games.stendhal.client.sprite.DataLoader;

public class ShopWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	private final ShopWindowManager manager;
	private final BackgroundPanel backgroundPanel;
	private final JLabel titleLabel;
	private final JLabel npcLabel;
	private final JTabbedPane tabbedPane;
	private final ShopTabPanel buyPanel;
	private final ShopTabPanel sellPanel;

	private ShopOfferData data;

	ShopWindow(ShopOfferData data, ShopWindowManager manager) {
		super(data.getTitle());
		this.manager = manager;
		this.data = data;

		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setResizable(false);

		backgroundPanel = new BackgroundPanel();
		backgroundPanel.setLayout(new BorderLayout(12, 12));
		backgroundPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		setContentPane(backgroundPanel);

		JPanel headerPanel = new JPanel(new BorderLayout());
		headerPanel.setOpaque(false);
		titleLabel = new JLabel(data.getTitle(), SwingConstants.CENTER);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		Font baseFont = titleLabel.getFont();
		if (baseFont != null) {
			titleLabel.setFont(baseFont.deriveFont(baseFont.getStyle() | Font.BOLD, baseFont.getSize() + 2.0f));
		}
		headerPanel.add(titleLabel, BorderLayout.CENTER);

		npcLabel = new JLabel(createNpcLabel(data.getNpcName()), SwingConstants.CENTER);
		npcLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
		headerPanel.add(npcLabel, BorderLayout.SOUTH);
		backgroundPanel.add(headerPanel, BorderLayout.NORTH);

		tabbedPane = new JTabbedPane();
		buyPanel = new ShopTabPanel(ShopTabPanel.ShopAction.BUY, data.getSellingItems());
		sellPanel = new ShopTabPanel(ShopTabPanel.ShopAction.SELL, data.getBuyingItems());
		tabbedPane.addTab("Kup", buyPanel);
		tabbedPane.addTab("Sprzedaj", sellPanel);
		backgroundPanel.add(tabbedPane, BorderLayout.CENTER);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				manager.handleWindowClosed(data.getNpcName(), ShopWindow.this);
			}

			@Override
			public void windowClosed(WindowEvent e) {
				manager.handleWindowClosed(data.getNpcName(), ShopWindow.this);
			}
		});

		updateOffer(data);
		setPreferredSize(new Dimension(560, 460));
		pack();
		setLocationRelativeTo(null);
	}

	public void updateOffer(ShopOfferData newData) {
		if (newData == null) {
			return;
		}

		data = newData;
		setTitle(newData.getTitle());
		titleLabel.setText(newData.getTitle());
		npcLabel.setText(createNpcLabel(newData.getNpcName()));
		buyPanel.updateItems(newData.getSellingItems());
		sellPanel.updateItems(newData.getBuyingItems());
		updateTabAvailability();
		backgroundPanel.setBackgroundImage(newData.getBackgroundPath());
		revalidate();
		repaint();
	}

	private void updateTabAvailability() {
		boolean hasBuying = buyPanel.hasItems();
		boolean hasSelling = sellPanel.hasItems();

		tabbedPane.setEnabledAt(0, hasBuying);
		tabbedPane.setEnabledAt(1, hasSelling);

		if (hasBuying) {
			tabbedPane.setSelectedIndex(0);
		} else if (hasSelling) {
			tabbedPane.setSelectedIndex(1);
		}
	}

	private String createNpcLabel(String npcName) {
		if ((npcName == null) || npcName.isEmpty()) {
			return "";
		}
		return "Handlarz: " + npcName;
	}

	private static class BackgroundPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private Image background;

		void setBackgroundImage(String path) {
			if ((path == null) || path.isEmpty()) {
				background = null;
				repaint();
				return;
			}

			try {
				URL url = DataLoader.getResource(path);
				if (url != null) {
					background = new ImageIcon(url).getImage();
				} else {
					background = null;
				}
			} catch (Exception e) {
				background = null;
			}
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (background != null) {
				g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
			}
		}
	}
}
