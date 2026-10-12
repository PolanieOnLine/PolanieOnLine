package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.awt.Dimension;
import java.awt.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.Test;
import games.stendhal.client.gui.layout.SLayout;

public class LeftColumnMoneyLayoutTest {
	@Test
	public void narrowingTheDockDoesNotWrapOrHideTheMoneyAmount() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			try {
				final Class<?> moneyClass = Class.forName("games.stendhal.client.gui.stats.StatsPanel$MoneyPanel");
				final Constructor<?> constructor = moneyClass.getDeclaredConstructor();
				constructor.setAccessible(true);
				final JPanel money = (JPanel) constructor.newInstance();
				final Method setMoney = moneyClass.getDeclaredMethod("setMoney", int.class, int.class, int.class, int.class);
				setMoney.setAccessible(true);
				final JComponent dock = SwingClientGUI.createLeftColumn();
				dock.add(money, SLayout.EXPAND_X);
				final int initialMinimum = dock.getMinimumSize().width;
				assertTrue(initialMinimum >= 180);
				for (int quantity : new int[] {0, 1, 92000000, Integer.MAX_VALUE, 10}) {
					setMoney.invoke(money, 0, 0, quantity, quantity);
					assertEquals(initialMinimum, dock.getMinimumSize().width);
					dock.setSize(new Dimension(initialMinimum, 200));
					dock.doLayout();
					money.doLayout();
					boolean amountFound = false;
					for (Component child : money.getComponents()) {
						assertTrue(child.getBounds().x + child.getWidth() <= money.getWidth());
						assertTrue(child.getBounds().y + child.getHeight() <= money.getHeight());
						if (child instanceof JLabel && Integer.toString(quantity).equals(((JLabel) child).getText())) {
							assertTrue(child.getWidth() >= child.getPreferredSize().width);
							amountFound = true;
						}
					}
					assertTrue(amountFound);
				}
			} catch (ReflectiveOperationException e) {
				throw new AssertionError(e);
			}
		});
	}
}
