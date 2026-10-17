/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Dialog;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.JComboBox;
import javax.swing.RootPaneContainer;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.wt.core.WtWindowManager;

/** Translates presentation controls, never editable fields, chat or option values. */
public final class UiTranslation {
	public static final String PRESERVE_TEXT = "translation.preserve";
	public static final String PRESERVE_TOOLTIP = "translation.preserve.tooltip";
	public static final String AFTER_TRANSLATION = "translation.after";
	private static final Set<Component> roots = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<Component, Boolean>()));
	static {
		WtWindowManager.getInstance().registerSettingChangeListener(ClientLanguage.PROPERTY,
				value -> SwingUtilities.invokeLater(() -> {
					for (Component root : roots.toArray(new Component[0])) apply(root);
				}));
	}
	private UiTranslation() { }

	public static void register(Component root) {
		roots.add(root);
		apply(root);
	}

	public static void preserve(JComponent component) {
		component.putClientProperty(PRESERVE_TEXT, Boolean.TRUE);
	}

	public static void apply(Component component) {
		if (component instanceof JComponent && Boolean.TRUE.equals(((JComponent) component).getClientProperty(PRESERVE_TEXT))) return;
		// Their labels are character names supplied by players, not interface strings.
		String type = component.getClass().getName();
		if (type.endsWith("BuddyLabel") || type.endsWith("MemberCellRenderer") || type.endsWith("EventLine")) return;
		if (component instanceof JComponent) {
			JComponent control = (JComponent) component;
			if (Boolean.TRUE.equals(control.getClientProperty("translation.updating"))) return;
			control.putClientProperty("translation.updating", Boolean.TRUE);
			try {
				if (control instanceof JLabel) {
					JLabel label = (JLabel) control;
					label.setText(text(control, "text", label.getText()));
				} else if (control instanceof AbstractButton) {
					AbstractButton button = (AbstractButton) control;
					button.setText(text(control, "text", button.getText()));
				}
				if (!Boolean.TRUE.equals(control.getClientProperty(PRESERVE_TOOLTIP))) control.setToolTipText(text(control, "tooltip", control.getToolTipText()));
				border(control, control.getBorder(), "border");
				if (control instanceof JTabbedPane) {
					JTabbedPane tabs = (JTabbedPane) control;
					for (int i = 0; i < tabs.getTabCount(); i++) tabs.setTitleAt(i, text(control, "tab." + i, tabs.getTitleAt(i)));
				}
				if (control.getClientProperty("translation.listener") == null) {
					PropertyChangeListener listener = event -> {
						if ("text".equals(event.getPropertyName()) || "ToolTipText".equals(event.getPropertyName()) || "border".equals(event.getPropertyName())) apply(control);
					};
					control.addPropertyChangeListener(listener);
					control.putClientProperty("translation.listener", listener);
				}
			} finally { control.putClientProperty("translation.updating", Boolean.FALSE); }
		}
		if (component instanceof Frame && component instanceof RootPaneContainer) {
			Frame frame = (Frame) component;
			frame.setTitle(text(((RootPaneContainer) component).getRootPane(), "window.title", frame.getTitle()));
		}
		if (component instanceof Dialog && component instanceof RootPaneContainer) {
			Dialog dialog = (Dialog) component;
			dialog.setTitle(text(((RootPaneContainer) component).getRootPane(), "window.title", dialog.getTitle()));
		}
		// Selector renderers can contain account names or canonical option values.
		// Settings install their own display-only renderer explicitly.
		if (component instanceof Container && !(component instanceof JComboBox)) {
			Container container = (Container) component;
			for (Component child : container.getComponents()) apply(child);
			boolean installed = false;
			for (java.awt.event.ContainerListener listener : container.getContainerListeners()) if (listener instanceof AddedControls) installed = true;
			if (!installed) container.addContainerListener(new AddedControls());
		}
		if (component instanceof JComponent) {
			JComponent control = (JComponent) component;
			Object callback = control.getClientProperty(AFTER_TRANSLATION);
			if (callback instanceof Runnable) {
				control.putClientProperty("translation.updating", Boolean.TRUE);
				try { ((Runnable) callback).run(); }
				finally { control.putClientProperty("translation.updating", Boolean.FALSE); }
			}
		}
		component.invalidate();
		component.repaint();
	}

	private static class AddedControls extends ContainerAdapter {
		@Override public void componentAdded(ContainerEvent event) { apply(event.getChild()); }
	}

	private static String text(JComponent control, String property, String current) {
		if (current == null) return null;
		String originalKey = "translation.original." + property;
		String renderedKey = "translation.rendered." + property;
		if (!current.equals(control.getClientProperty(renderedKey))) control.putClientProperty(originalKey, current);
		String original = (String) control.getClientProperty(originalKey);
		String translated = ClientLanguage.getText(original);
		control.putClientProperty(renderedKey, translated);
		return translated;
	}

	private static void border(JComponent control, Border border, String key) {
		if (border instanceof TitledBorder) {
			TitledBorder titled = (TitledBorder) border;
			titled.setTitle(text(control, key, titled.getTitle()));
		} else if (border instanceof CompoundBorder) {
			CompoundBorder compound = (CompoundBorder) border;
			border(control, compound.getOutsideBorder(), key + ".outside");
			border(control, compound.getInsideBorder(), key + ".inside");
		}
	}
}
