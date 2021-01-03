package games.stendhal.client.gui.achievementlog;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

public class DescriptionCellRenderer extends DefaultTableCellRenderer {
	private static final int PAD = AchievementLog.PAD;
	private final Border border = BorderFactory.createEmptyBorder(PAD, PAD, PAD, PAD);

	@Override
	public Component getTableCellRendererComponent(JTable table,
			Object value, boolean isSelected, boolean hasFocus, int row,
			int column) {
		super.getTableCellRendererComponent(table, value, isSelected,
				hasFocus, row, column);
		setBorder(border);
		return this;
	}
}
