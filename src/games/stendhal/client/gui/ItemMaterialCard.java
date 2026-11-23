package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.client.sprite.TextSprite;

/** Ingredient slot shared by the glyph, upgrade and affix services. */
final class ItemMaterialCard extends JPanel {
	private static final long serialVersionUID = 1L;
	static final Color AVAILABLE = new Color(92, 190, 105);
	static final Color UNAVAILABLE = new Color(225, 90, 75);
	private static final Sprite SLOT = SpriteStore.get().getSprite("data/gui/slot.png");

	ItemMaterialCard(final String name, final Sprite sprite, final int owned,
			final int required, final int width, final boolean plainName) {
		super(new BorderLayout(0, 3));
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder());
		final JPanel slot = new DialogPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
		slot.add(new MaterialIcon(sprite, required));
		add(slot, BorderLayout.NORTH);
		final JPanel description = new DialogPanel(new BorderLayout(0, 2));
		final String escaped = name.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
		final JLabel label = new JLabel("<html><table width='" + width
				+ "' cellspacing='0' cellpadding='0'><tr><td align='center'>"
				+ escaped + "</td></tr></table></html>", SwingConstants.CENTER);
		if (plainName) label.setFont(label.getFont().deriveFont(Font.PLAIN));
		description.add(label, BorderLayout.CENTER);
		final JLabel count = new JLabel(owned + " / " + required, SwingConstants.CENTER);
		count.setForeground(owned >= required ? AVAILABLE : UNAVAILABLE);
		description.add(count, BorderLayout.SOUTH);
		add(description, BorderLayout.CENTER);
		setToolTipText(name + ": " + ClientLanguage.getText("Posiadasz:") + " " + owned
				+ ", " + ClientLanguage.getText("Wymagane:") + " " + required);
		final Dimension size = new Dimension(width, getPreferredSize().height);
		setMinimumSize(size);
		setPreferredSize(size);
	}

	@Override
	protected void paintComponent(final Graphics graphics) {
		// The enclosing window paints the shared background.
	}

	private static final class MaterialIcon extends JComponent {
		private static final long serialVersionUID = 1L;
		private final Sprite sprite;
		private final Sprite quantity;

		MaterialIcon(final Sprite sprite, final int required) {
			this.sprite = sprite;
			quantity = required > 1 ? TextSprite.createTextSprite(Integer.toString(required), Color.WHITE) : null;
			setPreferredSize(new Dimension(40, 40));
			setMinimumSize(new Dimension(40, 40));
		}

		@Override
		protected void paintComponent(final Graphics graphics) {
			super.paintComponent(graphics);
			final int x = (getWidth() - SLOT.getWidth()) / 2;
			final int y = (getHeight() - SLOT.getHeight()) / 2;
			SLOT.draw(graphics, x, y);
			if (sprite != null) {
				sprite.draw(graphics, x + (SLOT.getWidth() - sprite.getWidth()) / 2,
						y + (SLOT.getHeight() - sprite.getHeight()) / 2);
			}
			if (quantity != null) quantity.draw(graphics, x + SLOT.getWidth() - quantity.getWidth(), y);
		}
	}
}
