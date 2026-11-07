/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui.progress;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.text.ComponentView;
import javax.swing.text.StyleConstants;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.entity.Item;
import games.stendhal.client.gui.ItemRarityPresentation;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.client.sprite.TextSprite;
import games.stendhal.common.QuestRewardPreview;
import games.stendhal.common.constants.ItemRarity;

/** Native, display-only item slots embedded in the parchment journal. */
final class QuestRewardsPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	static final String PROPERTY = "quest-rewards";
	static final String MARKUP = "<p><object classid='quest-rewards'></object></p>";

	static void install(JEditorPane pane) {
		pane.setEditorKit(new HTMLEditorKit() {
			private static final long serialVersionUID = 1L;

			@Override
			public ViewFactory getViewFactory() {
				ViewFactory standard = super.getViewFactory();
				return element -> {
					if (element.getAttributes().getAttribute(
							StyleConstants.NameAttribute) == HTML.Tag.OBJECT
							&& PROPERTY.equals(
									element.getAttributes().getAttribute(
											HTML.Attribute.CLASSID))) {
						return new ComponentView(element) {
							@Override
							protected Component createComponent() {
								return new QuestRewardsPanel(
										(QuestRewardPreview) pane.getClientProperty(
												PROPERTY));
							}
						};
					}
					return standard.create(element);
				};
			}
		});
	}

	QuestRewardsPanel(QuestRewardPreview rewards) {
		setUI(new BasicPanelUI());
		setBorder(null);
		setOpaque(false);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		if (rewards == null) {
			return;
		}
		JLabel title = label(
				text(rewards.completed ? "Otrzymane nagrody" : "Nagrody"));
		title.setFont(title.getFont().deriveFont(Font.BOLD));
		add(title);
		JPanel amounts = transparent(new FlowLayout(FlowLayout.LEFT, 0, 3));
		if (rewards.xp > 0) {
			amounts.add(label(number(rewards.xp) + " " + text("PD")));
		}
		if (rewards.coins > 0) {
			JLabel coins = label(number(rewards.coins) + " " + text("monet"));
			coins.setBorder(BorderFactory.createEmptyBorder(0,
					rewards.xp > 0 ? 12 : 0, 0, 0));
			amounts.add(coins);
		}
		if (amounts.getComponentCount() > 0) {
			add(amounts);
		}
		for (QuestRewardPreview.Item item : rewards.items) {
			JPanel row = transparent(new BorderLayout(7, 0));
			row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
			row.add(new RewardSlot(item), BorderLayout.WEST);
			String caption = escape(text(item.name));
			ItemRarity rarity = ItemRarity.fromId(item.rarity);
			if (rarity != null) {
				caption += "<br><font color='" + rarity.getColorHex() + "'>"
						+ escape(text(rarity.getPolishDisplayName()))
						+ "</font>";
			}
			JLabel name = new JLabel("<html><div style='width:110px'>" + caption
					+ "</div></html>") {
				private static final long serialVersionUID = 1L;

				@Override
				public String getToolTipText() {
					return tooltip(item);
				}
			};
			name.setForeground(Color.BLACK);
			name.setFont(name.getFont().deriveFont(Font.PLAIN));
			name.setToolTipText("");
			ToolTipManager.sharedInstance().registerComponent(name);
			row.add(name, BorderLayout.CENTER);
			add(row);
		}
		for (String note : rewards.notes) {
			add(label("<html><div style='width:135px'>" + escape(text(note))
					+ "</div></html>"));
		}
		for (Component child : getComponents()) {
			((JComponent) child).setAlignmentX(LEFT_ALIGNMENT);
		}
	}

	private static JLabel label(String text) {
		JLabel label = new JLabel(text);
		label.setForeground(Color.BLACK);
		label.setFont(label.getFont().deriveFont(Font.PLAIN));
		return label;
	}

	private static JPanel transparent(LayoutManager layout) {
		JPanel panel = new JPanel(layout);
		panel.setUI(new BasicPanelUI());
		panel.setBorder(null);
		panel.setOpaque(false);
		return panel;
	}

	private static String text(String value) {
		return ClientLanguage.getText(value);
	}

	private static String number(int value) {
		return NumberFormat.getIntegerInstance(new Locale("pl", "PL")).format(
				value);
	}

	private static String escape(String value) {
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">",
				"&gt;").replace("\"", "&quot;");
	}

	private static String tooltip(QuestRewardPreview.Item reward) {
		Item item = new Item();
		item.initialize(reward.toRPObject());
		return ItemRarityPresentation.buildItemToolTip(item);
	}

	private static final class RewardSlot extends JComponent {
		private static final long serialVersionUID = 1L;
		private final Sprite background = SpriteStore.get().getSprite(
				"data/gui/slot.png");
		private final QuestRewardPreview.Item item;
		private final Sprite sprite;
		private final Sprite quantity;
		private final Timer animation = new Timer(100, event -> repaint());

		RewardSlot(QuestRewardPreview.Item item) {
			this.item = item;
			setPreferredSize(new Dimension(42, 42));
			setToolTipText("");
			// The dynamic getter is already non-null, so setToolTipText alone
			// does not register this component with Swing's tooltip manager.
			ToolTipManager.sharedInstance().registerComponent(this);
			SpriteStore store = SpriteStore.get();
			Sprite sheet = store.getSprite("data/sprites/items/"
					+ item.itemClass + "/" + item.subclass + ".png");
			sprite = sheet.getWidth() > 32
					? store.getAnimatedSprite(sheet, 0, 0,
							sheet.getWidth() / 32, 32, 32, 100)
					: store.getTile(sheet, 0, 0, 32, 32);
			quantity = item.amount > 1
					? TextSprite.createTextSprite(Integer.toString(item.amount),
							Color.WHITE)
					: null;
		}

		@Override
		public String getToolTipText() {
			return tooltip(item);
		}

		@Override
		public void addNotify() {
			super.addNotify();
			animation.start();
		}

		@Override
		public void removeNotify() {
			animation.stop();
			super.removeNotify();
		}

		@Override
		protected void paintComponent(Graphics graphics) {
			super.paintComponent(graphics);
			background.draw(graphics, 2, 2);
			sprite.draw(graphics, 5, 5);
			if (quantity != null) {
				quantity.draw(graphics, 39 - quantity.getWidth(), 2);
			}
		}
	}
}
