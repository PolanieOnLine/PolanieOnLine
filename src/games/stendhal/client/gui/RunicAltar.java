package games.stendhal.client.gui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import games.stendhal.client.GameObjects;
import games.stendhal.client.entity.ContentChangeListener;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.entity.factory.EntityMap;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.listener.FeatureChangeListener;
import games.stendhal.client.sprite.SpriteStore;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;
import marauroa.common.game.RPSlot;

@SuppressWarnings("serial")
class RunicAltar extends InternalManagedWindow implements ContentChangeListener,
FeatureChangeListener {
	private static final Logger logger = Logger.getLogger(RunicAltar.class);

	private final Map<String, ItemPanel> slotPanels = new HashMap<String, ItemPanel>();
	private User player;

	private static final int PADDING = 1;

	/**
	 * Constructor for the RunicAltar class.
	 * It sets the window to have 7 slots in total, distributed in the shape of the Star of David.
	 * The constructor also configures the window to be closeable by the user.
	 */
	public RunicAltar() {
		super("runicaltar", "RunicAltar");
		// This method configures the window to be closeable by the user (through a close button).
		setCloseable(true);
		createLayout();
	}

	public void setPlayer(final User userEntity) {
		player = userEntity;
		userEntity.addContentChangeListener(this);
		refreshContents();
	}

	private void createLayout() {
		JComponent content = SBoxLayout.createContainer(SBoxLayout.VERTICAL, PADDING);
		JComponent row = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, 20);
		JComponent left = SBoxLayout.createContainer(SBoxLayout.VERTICAL, 15);
		JComponent middle = SBoxLayout.createContainer(SBoxLayout.VERTICAL, 20);
		JComponent right = SBoxLayout.createContainer(SBoxLayout.VERTICAL, 15);

		left.setAlignmentY(CENTER_ALIGNMENT);
		middle.setAlignmentY(CENTER_ALIGNMENT);
		right.setAlignmentY(CENTER_ALIGNMENT);

		row.add(left);
		row.add(middle);
		row.add(right);
		content.add(row);

		Class<? extends IEntity> itemClass = EntityMap.getClass("item", null, null);
		SpriteStore store = SpriteStore.get();

		left.add(createItemPanel(itemClass, store, "control_rune", "data/gui/rune-control.png"));
		left.add(createItemPanel(itemClass, store, "utility_rune", "data/gui/rune-utility.png"));

		middle.add(createItemPanel(itemClass, store, "offensive_rune", "data/gui/rune-offensive.png"));
		middle.add(createItemPanel(itemClass, store, "special_rune", "data/gui/rune-special.png"));
		middle.add(createItemPanel(itemClass, store, "defensive_rune", "data/gui/rune-defensive.png"));

		right.add(createItemPanel(itemClass, store, "healing_rune", "data/gui/rune-healing.png"));
		right.add(createItemPanel(itemClass, store, "resistance_rune", "data/gui/rune-resistance.png"));

		setContent(content);
	}

	private ItemPanel createItemPanel(Class<? extends IEntity> itemClass, SpriteStore store, String id, String image) {
		ItemPanel panel = new ItemPanel(id, store.getSprite(image));
		slotPanels.put(id, panel);
		panel.setAcceptedTypes(itemClass);

		return panel;
	}

	/**
	 * This method will be called when a feature associated with this class is disabled.
	 * It is currently not implemented and serves as a placeholder for future functionality.
	 *
	 * @param name The name of the disabled feature.
	 */
	@Override
	public void featureDisabled(String name) {
		// Placeholder method, not yet implemented.
	}

	/**
	 * This method will be called when a feature associated with this class is enabled.
	 * It is currently not implemented and serves as a placeholder for future functionality.
	 *
	 * @param name  The name of the enabled feature.
	 * @param value The value or additional data associated with the enabled feature.
	 */
	@Override
	public void featureEnabled(String name, String value) {
		// Placeholder method, not yet implemented.
	}

	/**
	 * Updates the player slot panels.
	 */
	private void refreshContents() {
		for (final Entry<String, ItemPanel> entry : slotPanels.entrySet()) {
			final ItemPanel entitySlot = entry.getValue();

			if (entitySlot != null) {
				// Set the parent entity for all slots, even if they are not
				// visible. They may become visible without zone changes
				entitySlot.setParent(player);

				final RPSlot slot = player.getSlot(entry.getKey());
				if (slot == null) {
					continue;
				}

				final Iterator<RPObject> iter = slot.iterator();

				if (iter.hasNext()) {
					final RPObject object = iter.next();

					IEntity entity = GameObjects.getInstance().get(object);

					entitySlot.setEntity(entity);
				} else {
					entitySlot.setEntity(null);
				}
			}
		}

		/*
		 * Refresh gets called from outside the EDT.
		 */
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				setTitle("Ołtarz Runiczny");
			}
		});
	}

	@Override
	public void contentAdded(RPSlot added) {
		ItemPanel panel = slotPanels.get(added.getName());
		if (panel == null) {
			// Not a slot we are interested in
			return;
		}

		for (RPObject obj : added) {
			ID id = obj.getID();
			IEntity entity = panel.getEntity();
			if (entity != null && id.equals(entity.getRPObject().getID())) {
				// Changed rather than added.
				return;
			}
			// Actually added, fetch the corresponding entity
			entity = GameObjects.getInstance().get(obj);
			if (entity == null) {
				logger.error("Unable to find entity for: " + obj,
						new Throwable("here"));
				return;
			}

			panel.setEntity(entity);
		}
	}

	@Override
	public void contentRemoved(RPSlot removed) {
		ItemPanel panel = slotPanels.get(removed.getName());
		if (panel == null) {
			// Not a slot we are interested in
			return;
		}
		for (RPObject obj : removed) {
			ID id = obj.getID();
			IEntity entity = panel.getEntity();
			if (entity != null && id.equals(entity.getRPObject().getID())) {
				if (obj.size() == 1) {
					// The object was removed
					panel.setEntity(null);
					continue;
				}
			} else {
				logger.error("Tried removing wrong object from a panel. "
						+ "removing: " + obj + " , but panel contains: "
						+ panel.getEntity(), new Throwable());
			}
		}
	}
}
