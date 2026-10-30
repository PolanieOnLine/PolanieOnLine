/***************************************************************************
 *					(C) Copyright 2013 - Faiumoni e.V					   *
 ***************************************************************************
 ***************************************************************************
 *																		   *
 *	 This program is free software; you can redistribute it and/or modify  *
 *	 it under the terms of the GNU General Public License as published by  *
 *	 the Free Software Foundation; either version 2 of the License, or	   *
 *	 (at your option) any later version.								   *
 *																		   *
 ***************************************************************************/
package games.stendhal.client.gui.styled;

import java.awt.Font;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.plaf.basic.BasicTableUI;

/**
 * Pixmap style UI delegate for JTables.
 */
public class StyledTableUI extends BasicTableUI {
	/** Used style. */
	private final Style style;

	/**
	 * Required by UIManager.
	 *
	 * @param table component to create UI for
	 * @return UI delegate
	 */
	public static ComponentUI createUI(JComponent table) {
		return new StyledTableUI(StyleUtil.getStyle());
	}

	/**
	 * Create a new StyledTableUI based on a pixmap style.
	 * @param style pixmap style
	 */
	public StyledTableUI(Style style) {
		this.style = style;
	}

	@Override
	public void installUI(JComponent table) {
		super.installUI(table);
		table.setForeground(style.getForeground());
		table.setBackground(style.getPlainColor());
		table.setFont(style.getFont().deriveFont(Font.BOLD));
		this.table.setGridColor(style.getShadowColor());
        final JTableHeader header = this.table.getTableHeader();
        if (header != null) {
            TableCellRenderer original = header.getDefaultRenderer();
            if (original instanceof HeaderRenderer) { original = ((HeaderRenderer) original).delegate; }
            header.setDefaultRenderer(new HeaderRenderer(original, style));
        }
	}

    private static final class HeaderRenderer extends DefaultTableCellRenderer implements UIResource {
        private static final long serialVersionUID = 1L;
        private final TableCellRenderer delegate;
        private final Style style;
        HeaderRenderer(final TableCellRenderer delegate, final Style style) {
            this.delegate = delegate;
            this.style = style;
            putClientProperty("html.disable", Boolean.TRUE);
        }
        @Override public Component getTableCellRendererComponent(final JTable owner, final Object value,
                final boolean selected, final boolean focused, final int row, final int column) {
            final Component original = delegate.getTableCellRendererComponent(owner, value, selected, focused, row, column);
            super.getTableCellRendererComponent(owner, value, selected, focused, row, column);
            if (original instanceof JLabel) {
                final JLabel label = (JLabel) original;
                setText(label.getText()); setIcon(label.getIcon());
                setHorizontalAlignment(label.getHorizontalAlignment());
                setHorizontalTextPosition(label.getHorizontalTextPosition());
            }
            setForeground(style.getForeground());
            setFont(style.getFont().deriveFont(Font.BOLD));
            setBorder(style.getBorder());
            setOpaque(false);
            return this;
        }
        @Override protected void paintComponent(final Graphics graphics) {
            StyleUtil.fillBackground(style, graphics, 0, 0, getWidth(), getHeight());
            super.paintComponent(graphics);
        }
    }

	@Override
	public void paint(Graphics g, JComponent table) {
		StyleUtil.fillBackground(style, g, 0, 0, table.getWidth(), table.getHeight());
		super.paint(g, table);
	}
}
