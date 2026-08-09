package games.stendhal.client.gui.shop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;

public final class ShopWindowManager {
	private static final ShopWindowManager INSTANCE = new ShopWindowManager();

	private final Map<String, ShopWindow> windows;

	private ShopWindowManager() {
		windows = new ConcurrentHashMap<String, ShopWindow>();
	}

	public static ShopWindowManager get() {
		return INSTANCE;
	}

	public void openShop(final ShopOfferData data) {
		if (data == null) {
			return;
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ShopWindow window = windows.get(data.getNpcName());
				if (window == null) {
					window = new ShopWindow(data, ShopWindowManager.this);
					windows.put(data.getNpcName(), window);
				} else {
					window.updateOffer(data);
				}

				window.setVisible(true);
				window.toFront();
			}
		});
	}

	public void close(final String npcName) {
		if (npcName == null) {
			return;
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ShopWindow window = windows.remove(npcName);
				if (window != null) {
					window.dispose();
				}
			}
		});
	}

	void handleWindowClosed(String npcName, ShopWindow window) {
		if ((npcName == null) || (window == null)) {
			return;
		}
		windows.remove(npcName, window);
	}
}
