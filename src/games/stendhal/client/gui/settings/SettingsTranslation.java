/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui.settings;

import java.awt.Component;
import java.awt.Container;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.ListCellRenderer;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import games.stendhal.client.ClientLanguage;
import games.stendhal.common.Translation;

/** Translates display labels only, never option values or saved preferences. */
final class SettingsTranslation {
	private SettingsTranslation() { }
	static void apply(Component component) {
		if (component instanceof JComponent) {
			JComponent control = (JComponent) component;
			if (control instanceof JComboBox) translateSelector((JComboBox<?>) control);
			if (control instanceof JLabel) {
				JLabel label = (JLabel) control;
				label.setText(translate(control, "text", label.getText()));
			} else if (control instanceof AbstractButton) {
				AbstractButton button = (AbstractButton) control;
				button.setText(translate(control, "text", button.getText()));
			}
			translateBorder(control, control.getBorder(), "border");
			control.setToolTipText(translate(control, "tooltip", control.getToolTipText()));
		}
		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents()) apply(child);
		}
	}

	private static void translateBorder(JComponent control, Border border, String property) {
		if (border instanceof TitledBorder) {
			TitledBorder titled = (TitledBorder) border;
			titled.setTitle(translate(control, property, titled.getTitle()));
		} else if (border instanceof CompoundBorder) {
			CompoundBorder compound = (CompoundBorder) border;
			translateBorder(control, compound.getOutsideBorder(), property + ".outside");
			translateBorder(control, compound.getInsideBorder(), property + ".inside");
		}
	}

	private static <T> void translateSelector(JComboBox<T> selector) {
		if ("language-selector".equals(selector.getName())) {
			selector.setSelectedIndex(Translation.SUPPORTED_LANGUAGES.indexOf(ClientLanguage.getLanguage()));
		}
		if (selector.getClientProperty("translation.renderer") == null) {
			ListCellRenderer<? super T> renderer = selector.getRenderer();
			selector.setRenderer((list, value, index, selected, focus) -> {
				Component display = renderer.getListCellRendererComponent(list, value, index, selected, focus);
				list.setToolTipText(ClientLanguage.getText(list.getToolTipText()));
				if (display instanceof JLabel) {
					JLabel label = (JLabel) display;
					label.setText(ClientLanguage.getText(label.getText()));
				}
				return display;
			});
			selector.putClientProperty("translation.renderer", Boolean.TRUE);
		}
	}

	private static String translate(JComponent control, String property, String current) {
		if (current == null) return null;
		String originalKey = "translation.original." + property;
		String renderedKey = "translation.rendered." + property;
		if (!current.equals(control.getClientProperty(renderedKey))) control.putClientProperty(originalKey, current);
		String original = (String) control.getClientProperty(originalKey);
		String result = ClientLanguage.getText(original);
		control.putClientProperty(renderedKey, result);
		return result;
	}
}
