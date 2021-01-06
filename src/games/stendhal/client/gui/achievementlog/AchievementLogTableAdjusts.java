package games.stendhal.client.gui.achievementlog;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public class AchievementLogTableAdjusts {
	void columnWidths(JTable table) {
		TableColumnModel model = table.getColumnModel();
		for (int column = 0; column < table.getColumnCount(); column++) {
			TableColumn tc = model.getColumn(column);
			int width = tc.getWidth();
			for (int row = 0; row < table.getRowCount(); row++) {
				Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
				width = Math.max(width, comp.getPreferredSize().width);
			}

			tc.setPreferredWidth(width);
		}
	}
	void rowHeights(JTable table) {
		for (int row = 0; row < table.getRowCount(); row++) {
			int rowHeight = table.getRowHeight();

			for (int column = 0; column < table.getColumnCount(); column++) {
				Component comp = table.prepareRenderer(table.getCellRenderer(row, column), row, column);
				rowHeight = Math.max(rowHeight, comp.getPreferredSize().height);
			}

			table.setRowHeight(row, rowHeight);
		}
	}
}
