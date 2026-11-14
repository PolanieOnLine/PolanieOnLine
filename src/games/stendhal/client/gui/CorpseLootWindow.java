/* Copyright (C) 2026 PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.plaf.PanelUI;

import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.Sprite;

/** Compact loot tray. Only corpse inspectors use this presentation. */
public final class CorpseLootWindow extends SlotWindow {

	private static final long serialVersionUID = 1L;
	private static final Color GOLD = new Color(177, 131, 73);
	private static final Color SHADOW = new Color(32, 23, 16);

	public CorpseLootWindow(final int columns, final int rows) {
		this(new SlotGrid(columns, rows, createSlotSurface(), 6));
	}

	private CorpseLootWindow(final SlotGrid grid) {
		super("corpse", grid);
		setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(SHADOW),
				BorderFactory.createLineBorder(GOLD)));
		setMinimizable(false);

		JPanel header = (JPanel) getTitlebar();
		header.setUI(new PanelUI() {
			@Override
			public void paint(Graphics graphics, JComponent component) {
				paintWood(graphics, component.getWidth(), component.getHeight(), 85);
				graphics.setColor(GOLD);
				graphics.drawLine(0, component.getHeight() - 2,
						component.getWidth(), component.getHeight() - 2);
				graphics.setColor(SHADOW);
				graphics.drawLine(0, component.getHeight() - 1,
						component.getWidth(), component.getHeight() - 1);
			}
		});
		setTitlebarBorder(BorderFactory.createEmptyBorder(5, 7, 6, 5));
		titleLabel.setForeground(new Color(248, 225, 178));
		titleLabel.setMaximumSize(new Dimension(250, Integer.MAX_VALUE));
		closeButton.setIcon(new ImageIcon(createCloseImage(false)));
		closeButton.setRolloverIcon(new ImageIcon(createCloseImage(true)));
		closeButton.setPressedIcon(new ImageIcon(createCloseImage(true)));
		closeButton.setContentAreaFilled(false);
		closeButton.setToolTipText("Zamknij");

		JPanel tray = new JPanel(new GridBagLayout());
		tray.setUI(new PanelUI() { });
		tray.setOpaque(false);
		tray.setBorder(BorderFactory.createEmptyBorder(9, 10, 10, 10));
		tray.add(grid);
		Dimension size = tray.getPreferredSize();
		tray.setPreferredSize(new Dimension(Math.max(150, size.width), size.height));
		setContent(tray);
	}

	/** Keep long names on one line and expose their complete text on hover. */
	public void setLootTitle(final String title) {
		titleLabel.putClientProperty(UiTranslation.PRESERVE_TEXT, true);
		titleLabel.putClientProperty("html.disable", true);
		titleLabel.setText(title);
		titleLabel.setToolTipText(title);
		getTitlebar().setToolTipText(title);
	}

	@Override
	public void paintComponent(final Graphics graphics) {
		paintWood(graphics, getWidth(), getHeight(), 115);
	}

	private static void paintWood(final Graphics graphics, final int width,
			final int height, final int shade) {
		Style style = StyleUtil.getStyle();
		graphics.setColor(new Color(87, 58, 37));
		graphics.fillRect(0, 0, width, height);
		if (style != null) {
			Sprite wood = style.getBackground();
			for (int x = 0; x < width; x += wood.getWidth()) {
				for (int y = 0; y < height; y += wood.getHeight()) {
					wood.draw(graphics, x, y);
				}
			}
		}
		graphics.setColor(new Color(19, 15, 11, shade));
		graphics.fillRect(0, 0, width, height);
	}

	private static Sprite createSlotSurface() {
		BufferedImage image = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		paintWood(graphics, 40, 40, 160);
		graphics.setColor(SHADOW);
		graphics.drawRect(0, 0, 39, 39);
		graphics.setColor(new Color(110, 85, 53));
		graphics.drawLine(1, 38, 38, 38);
		graphics.drawLine(38, 1, 38, 38);
		graphics.setColor(new Color(15, 13, 10));
		graphics.drawLine(1, 1, 37, 1);
		graphics.drawLine(1, 1, 1, 37);
		graphics.dispose();
		return new ImageSprite(image);
	}

	private static BufferedImage createCloseImage(final boolean highlight) {
		BufferedImage image = new BufferedImage(17, 17, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		paintWood(graphics, 17, 17, highlight ? 30 : 100);
		graphics.setColor(SHADOW);
		graphics.drawRect(0, 0, 16, 16);
		graphics.setColor(GOLD);
		graphics.drawRect(1, 1, 14, 14);
		graphics.setColor(new Color(248, 225, 178));
		graphics.drawLine(5, 5, 11, 11);
		graphics.drawLine(5, 6, 10, 11);
		graphics.drawLine(11, 5, 5, 11);
		graphics.drawLine(10, 5, 5, 10);
		graphics.dispose();
		return image;
	}
}
