/***************************************************************************
*                   (C) Copyright 2003-2024 - Stendhal                    *
***************************************************************************/
/***************************************************************************
*                                                                         *
*   This program is free software; you can redistribute it and/or modify  *
*   it under the terms of the GNU General Public License as published by  *
*   the Free Software Foundation; either version 2 of the License, or     *
*   (at your option) any later version.                                   *
*                                                                         *
***************************************************************************/
package games.stendhal.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;

import games.stendhal.client.UserContext;
import games.stendhal.client.gui.chatlog.EventLine;
import games.stendhal.client.gui.chatlog.HeaderLessEventLine;
import games.stendhal.client.sprite.EmojiStore;
import games.stendhal.client.sprite.EmojiStore.EmojiMatch;
import games.stendhal.client.stendhal;
import games.stendhal.common.NotificationType;

/**
* Modern Swing-based chat log view with emoji rendering.
*/
class WebChatLogView extends JComponent implements ChatLogView {
	private static final long serialVersionUID = 7436724181800491158L;

	private static final Logger LOGGER = Logger.getLogger(WebChatLogView.class);

	private static final Color DEFAULT_BACKGROUND = new Color(0x2d, 0x1b, 0x0f);
	private static final Color LIST_BACKGROUND = new Color(0x33, 0x20, 0x14);
	private static final Color BUBBLE_BACKGROUND = new Color(0xf3, 0xe3, 0xc9);
	private static final Color ADMIN_BUBBLE_BACKGROUND = new Color(0xff, 0xf1, 0xce);
	private static final Color AUTHOR_COLOR = new Color(0x2f, 0x1b, 0x0c);
	private static final Color TIMESTAMP_COLOR = new Color(0x6f, 0x52, 0x3b);
	private static final Color BODY_COLOR = new Color(0x32, 0x1c, 0x0c);
	private static final Color ACCENT_DEFAULT = new Color(0xc1, 0x84, 0x54);
	private static final Color ACCENT_ADMIN = new Color(0xff, 0xcc, 0x66);
	private static final Color ACCENT_ERROR = new Color(0xb9, 0x4a, 0x3c);
	private static final Color ACCENT_WARNING = new Color(0xd6, 0xa0, 0x43);
	private static final Color ACCENT_POSITIVE = new Color(0x5d, 0x9c, 0x57);
	private static final Color ACCENT_SUPPORT = new Color(0x4d, 0xa3, 0xc7);

	private final Format dateFormatter = new SimpleDateFormat("[HH:mm:ss] ", Locale.getDefault());
	private final List<MessageEntry> entries = new ArrayList<>();
	private final List<String> plainLines = new ArrayList<>();

	private final JPanel listPanel;
	private final JScrollPane scrollPane;

	private Color defaultBackground = DEFAULT_BACKGROUND;
	private String channelName = "";
	private boolean adminFocus;

	private Font authorFont;
	private Font timestampFont;
	private Font bodyFont;

	WebChatLogView() {
		setLayout(new BorderLayout());
		setOpaque(true);

		listPanel = new JPanel();
		listPanel.setOpaque(false);
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		listPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

		JPanel viewport = new JPanel(new BorderLayout());
		viewport.setOpaque(false);
		viewport.add(listPanel, BorderLayout.NORTH);

		scrollPane = new JScrollPane(viewport, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.getVerticalScrollBar().setUnitIncrement(32);

		add(scrollPane, BorderLayout.CENTER);

		updateFonts(null);
		applyBackground();
		installPopupMenu();
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void addLine(final EventLine line) {
		if (line == null) {
			return;
		}

		final NotificationType type = line.getType();
		final boolean admin = isAdmin(type, line);
		final String timestamp = dateFormatter.format(new Date());
		final JPanel messagePanel = buildMessagePanel(line, timestamp, admin);
		final Component spacer = Box.createVerticalStrut(10);
		final String plain = buildPlainLine(timestamp, line);

MessageEntry entry = new MessageEntry(messagePanel, spacer, admin);
		entries.add(entry);
		plainLines.add(plain);

		listPanel.add(messagePanel);
		listPanel.add(spacer);

		applyAdminFilter();
		listPanel.revalidate();
		listPanel.repaint();
		scrollToBottom();
	}

	@Override
	public void clear() {
		entries.clear();
		plainLines.clear();
		listPanel.removeAll();
		listPanel.revalidate();
		listPanel.repaint();
	}

	@Override
	public void setDefaultBackground(final Color color) {
		defaultBackground = (color != null) ? color : DEFAULT_BACKGROUND;
		applyBackground();
	}

	@Override
	public void setChannelName(final String name) {
		channelName = (name != null) ? name : "";
	}

	@Override
	public void setFont(final Font font) {
		super.setFont(font);
		updateFonts(font);
	}

	private void updateFonts(final Font requested) {
		Font base = (requested != null) ? requested : UIManager.getFont("Label.font");
		if (base == null) {
			base = new Font("SansSerif", Font.PLAIN, 13);
		}
		bodyFont = base.deriveFont(Font.PLAIN, base.getSize2D());
		authorFont = base.deriveFont(Font.BOLD, base.getSize2D());
		timestampFont = base.deriveFont(Font.PLAIN, Math.max(10f, base.getSize2D() - 1f));
	}

	private void installPopupMenu() {
		final JPopupMenu menu = new JPopupMenu();

		JMenuItem saveItem = new JMenuItem("Zapisz czat...");
		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				save();
			}
		});
		menu.add(saveItem);

		JMenuItem clearItem = new JMenuItem("Wyczyść czat");
		clearItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				clear();
			}
		});
		menu.add(clearItem);
		menu.add(new JSeparator());

		final JCheckBoxMenuItem adminToggle = new JCheckBoxMenuItem("Pokaż tylko wiadomości administracji (tryb testowy)");
		adminToggle.setSelected(adminFocus);
		adminToggle.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent event) {
				adminFocus = adminToggle.isSelected();
				applyAdminFilter();
			}
		});
		menu.add(adminToggle);

		MouseAdapter adapter = new MouseAdapter() {
			@Override
			public void mousePressed(final MouseEvent event) {
				maybeShow(event);
			}

			@Override
			public void mouseReleased(final MouseEvent event) {
				maybeShow(event);
			}

			private void maybeShow(final MouseEvent event) {
				if (event.isPopupTrigger()) {
					menu.show(event.getComponent(), event.getX(), event.getY());
				}
			}
		};

		addMouseListener(adapter);
		scrollPane.addMouseListener(adapter);
		scrollPane.getViewport().getView().addMouseListener(adapter);
	}

	private void applyBackground() {
		setBackground(defaultBackground);
		scrollPane.getViewport().setBackground(new Color(defaultBackground.getRed(), defaultBackground.getGreen(), defaultBackground.getBlue(), 180));
		listPanel.setBackground(LIST_BACKGROUND);
	}

	private void scrollToBottom() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JScrollBar bar = scrollPane.getVerticalScrollBar();
				bar.setValue(bar.getMaximum());
			}
		});
	}

	private JPanel buildMessagePanel(final EventLine line, final String timestamp, final boolean admin) {
		final String author = (line.getHeader() != null && !line.getHeader().isEmpty()) ? line.getHeader() : "PolanieOnLine";
		final Color accent = accentColor(line.getType(), admin);

		JPanel wrapper = new JPanel(new BorderLayout());
		wrapper.setOpaque(false);

		JPanel row = new JPanel(new BorderLayout(12, 0));
		row.setOpaque(false);

		AvatarLabel avatar = new AvatarLabel(authorFont);
		avatar.setLabel(author);
		row.add(avatar, BorderLayout.WEST);

		MessageBubble bubble = new MessageBubble(accent, admin);
		bubble.setLayout(new BorderLayout());
		bubble.setBorder(new EmptyBorder(12, 16, 12, 16));

		JPanel headerPanel = new JPanel();
		headerPanel.setOpaque(false);
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));

		if (admin) {
			JLabel badge = new JLabel("ADMIN");
			badge.setOpaque(true);
			badge.setBackground(ACCENT_ADMIN);
			badge.setForeground(AUTHOR_COLOR);
			badge.setFont(authorFont.deriveFont(Font.BOLD, Math.max(10f, authorFont.getSize2D() - 1f)));
			badge.setBorder(new EmptyBorder(2, 6, 2, 6));
			headerPanel.add(badge);
			headerPanel.add(Box.createHorizontalStrut(8));
		}

		JLabel authorLabel = new JLabel(author);
		authorLabel.setFont(authorFont);
		authorLabel.setForeground(AUTHOR_COLOR);
		headerPanel.add(authorLabel);
		headerPanel.add(Box.createHorizontalStrut(10));

		JLabel timestampLabel = new JLabel(timestamp.trim());
		timestampLabel.setFont(timestampFont);
		timestampLabel.setForeground(TIMESTAMP_COLOR);
		headerPanel.add(timestampLabel);
		headerPanel.add(Box.createHorizontalGlue());

		bubble.add(headerPanel, BorderLayout.NORTH);

		JTextPane bodyPane = createBodyPane(line.getText());
		bubble.add(bodyPane, BorderLayout.CENTER);

		row.add(bubble, BorderLayout.CENTER);
		wrapper.add(row, BorderLayout.CENTER);
		return wrapper;
	}

	private JTextPane createBodyPane(final String text) {
		JTextPane pane = new JTextPane();
		pane.setEditable(false);
		pane.setOpaque(false);
		pane.setBorder(new EmptyBorder(4, 0, 0, 0));
		pane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		pane.setFont(bodyFont);

		StyledDocument doc = pane.getStyledDocument();
		SimpleAttributeSet textAttrs = new SimpleAttributeSet();
		StyleConstants.setFontFamily(textAttrs, bodyFont.getFamily());
		StyleConstants.setFontSize(textAttrs, bodyFont.getSize());
		StyleConstants.setForeground(textAttrs, BODY_COLOR);

		SimpleAttributeSet emojiAttrs = new SimpleAttributeSet(textAttrs);
		StyleConstants.setFontFamily(emojiAttrs, EmojiStore.getFontFamily());
		StyleConstants.setBold(emojiAttrs, false);
		StyleConstants.setItalic(emojiAttrs, false);

		appendTextWithEmoji(doc, text, textAttrs, emojiAttrs);
		return pane;
	}

	private void appendTextWithEmoji(final StyledDocument doc, final String text, final SimpleAttributeSet textAttrs, final SimpleAttributeSet emojiAttrs) {
		if ((text == null) || text.isEmpty()) {
			return;
		}

		EmojiStore store = EmojiStore.get();
		StringBuilder buffer = new StringBuilder();
		int index = 0;
		int length = text.length();

		while (index < length) {
			char ch = text.charAt(index);
			if (ch == '\r') {
				index++;
				continue;
			}
			if (ch == '\n') {
				flushBuffer(doc, buffer, textAttrs);
				insertString(doc, "\n", textAttrs);
				index++;
				continue;
			}

			EmojiMatch match = store.matchEmoji(text, index);
			if (match != null) {
				flushBuffer(doc, buffer, textAttrs);
				int consumed = match.getConsumedLength();
				String token = text.substring(index, index + consumed);
				if (!insertEmoji(doc, token, emojiAttrs)) {
					String glyph = match.getGlyph();
					if ((glyph != null) && !glyph.isEmpty()) {
						insertString(doc, glyph, emojiAttrs);
					} else {
						insertString(doc, token, emojiAttrs);
					}
				}
				index += consumed;
				continue;
			}

			buffer.append(ch);
			index++;
		}

		flushBuffer(doc, buffer, textAttrs);
	}

	private void flushBuffer(final StyledDocument doc, final StringBuilder buffer, final SimpleAttributeSet attrs) {
		if (buffer.length() > 0) {
			insertString(doc, buffer.toString(), attrs);
			buffer.setLength(0);
		}
	}

	private void insertString(final StyledDocument doc, final String value, final SimpleAttributeSet attrs) {
		try {
			doc.insertString(doc.getLength(), value, attrs);
		} catch (BadLocationException ex) {
			LOGGER.warn("Failed to insert chat text", ex);
		}
	}

	private boolean insertEmoji(final StyledDocument doc, final String token, final SimpleAttributeSet attrs) {
		try {
			javax.swing.Icon icon = EmojiStore.get().getIcon(token);
			if (icon == null) {
				return false;
			}
			SimpleAttributeSet iconAttrs = new SimpleAttributeSet(attrs);
			StyleConstants.setIcon(iconAttrs, icon);
			doc.insertString(doc.getLength(), "\uFFFC", iconAttrs);
			return true;
		} catch (BadLocationException ex) {
			LOGGER.warn("Failed to insert emoji", ex);
			return false;
		}
	}

	private void save() {
		String fileName = getSaveFileName();
		Writer writer = null;
		try {
			File target = new File(fileName);
			File parent = target.getParentFile();
			if ((parent != null) && !parent.exists()) {
				parent.mkdirs();
			}
			writer = new OutputStreamWriter(new FileOutputStream(target), java.nio.charset.StandardCharsets.UTF_8);
			for (String line : plainLines) {
				writer.write(line);
				writer.write(System.lineSeparator());
			}
		} catch (IOException ex) {
			LOGGER.error("Failed to save chat log", ex);
			return;
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException ex) {
					LOGGER.warn("Failed to close chat log writer", ex);
				}
			}
		}

		addLine(new HeaderLessEventLine("Dziennik rozmowy został zapisany do " + fileName, NotificationType.CLIENT));
	}

	private String getSaveFileName() {
		String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
		StringBuilder builder = new StringBuilder();
		String character = UserContext.get().getName();
		if ((character != null) && !character.isEmpty()) {
			builder.append(character).append('_');
		}
		if ((channelName != null) && !channelName.isEmpty()) {
			builder.append(channelName).append('_');
		}
		builder.append(timestamp).append(".log");
		return stendhal.getGameFolder() + "chat/" + builder.toString();
	}

	private void applyAdminFilter() {
		for (MessageEntry entry : entries) {
			boolean visible = !adminFocus || entry.admin;
			entry.setVisible(visible);
		}
	}

	private boolean isAdmin(final NotificationType type, final EventLine line) {
		if (type == NotificationType.SUPPORT) {
			return true;
		}
		if (line != null) {
			String header = line.getHeader();
			if ((header != null) && header.toLowerCase(Locale.ROOT).contains("admin")) {
				return true;
			}
		}
		return false;
	}

	private Color accentColor(final NotificationType type, final boolean admin) {
		if (admin) {
			return ACCENT_ADMIN;
		}
		if (type == null) {
			return ACCENT_DEFAULT;
		}
		switch (type) {
			case ERROR:
			case NEGATIVE:
			case DAMAGE:
			case POISON:
			case SIGNIFICANT_NEGATIVE:
				return ACCENT_ERROR;
			case WARNING:
				return ACCENT_WARNING;
			case POSITIVE:
			case HEAL:
			case SIGNIFICANT_POSITIVE:
				return ACCENT_POSITIVE;
			case SUPPORT:
				return ACCENT_SUPPORT;
			default:
				return ACCENT_DEFAULT;
		}
	}

	private String buildPlainLine(final String timestamp, final EventLine line) {
		StringBuilder builder = new StringBuilder();
		if (timestamp != null) {
			builder.append(timestamp);
		}
		if (line != null) {
			String header = line.getHeader();
			if ((header != null) && !header.isEmpty()) {
				builder.append('<').append(header).append("> ");
			}
			String text = line.getText();
			if (text != null) {
				builder.append(text);
			}
		}
		return builder.toString();
	}

	private static final class MessageEntry {
		private final JPanel container;
		private final Component spacer;
		private final boolean admin;

		private MessageEntry(final JPanel container, final Component spacer, final boolean admin) {
			this.container = container;
			this.spacer = spacer;
			this.admin = admin;
		}

		private void setVisible(final boolean visible) {
			container.setVisible(visible);
			if (spacer != null) {
				spacer.setVisible(visible);
			}
		}
	}

	private static final class AvatarLabel extends JComponent {
		private static final long serialVersionUID = 1671784509587827657L;
		private static final int SIZE = 44;
		private final Font baseFont;
		private String text = "?";

		private AvatarLabel(final Font baseFont) {
			this.baseFont = baseFont;
			setPreferredSize(new Dimension(SIZE, SIZE));
			setMinimumSize(new Dimension(SIZE, SIZE));
			setMaximumSize(new Dimension(SIZE, SIZE));
		}

		private void setLabel(final String raw) {
			if ((raw == null) || raw.trim().isEmpty()) {
				text = "?";
				return;
			}
			int cp = raw.codePointAt(0);
			String label = new String(java.lang.Character.toChars(cp));
			text = java.lang.Character.isLetter(cp) ? label.toUpperCase(Locale.ROOT) : label;
		}

		@Override
		protected void paintComponent(final Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int diameter = Math.min(getWidth(), getHeight());
			GradientPaint paint = new GradientPaint(0, 0, new Color(0xf6, 0xdf, 0xb4), 0, diameter, new Color(0x8d, 0x65, 0x30));
			g2.setPaint(paint);
			g2.fillOval(0, 0, diameter - 1, diameter - 1);

			g2.setColor(new Color(0x2d, 0x1a, 0x0b));
			g2.drawOval(0, 0, diameter - 1, diameter - 1);

			Font font = baseFont.deriveFont(Font.BOLD, Math.max(14f, baseFont.getSize2D()));
			g2.setFont(font);
			java.awt.FontMetrics fm = g2.getFontMetrics(font);
			int textWidth = fm.stringWidth(text);
			int textHeight = fm.getAscent();
			int x = (diameter - textWidth) / 2;
			int y = ((diameter - fm.getHeight()) / 2) + textHeight;
			g2.drawString(text, x, y);
			g2.dispose();
		}

		@Override
		public boolean isOpaque() {
			return false;
		}
	}

	private static final class MessageBubble extends JPanel {
		private static final long serialVersionUID = -7723521081070025057L;
		private static final int ARC = 18;
		private final Color accent;
		private final boolean admin;

		private MessageBubble(final Color accent, final boolean admin) {
			this.accent = accent;
			this.admin = admin;
			setOpaque(false);
		}

		@Override
		protected void paintComponent(final Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			int width = getWidth();
			int height = getHeight();
			Color background = admin ? ADMIN_BUBBLE_BACKGROUND : BUBBLE_BACKGROUND;

			g2.setColor(background);
			g2.fillRoundRect(0, 0, width - 1, height - 1, ARC, ARC);
			g2.setColor(new Color(0, 0, 0, 40));
			g2.drawRoundRect(0, 0, width - 1, height - 1, ARC, ARC);

			g2.setColor(accent);
			g2.fillRoundRect(0, 0, 8, height - 1, ARC, ARC);
			g2.dispose();

			super.paintComponent(g);
		}

		@Override
		public boolean isOpaque() {
			return false;
		}
	}
}
