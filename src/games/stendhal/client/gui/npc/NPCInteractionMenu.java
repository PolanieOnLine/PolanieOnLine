/***************************************************************************
*                   (C) Copyright 2024 - Polanie OnLine                   *
***************************************************************************/
package games.stendhal.client.gui.npc;

import java.awt.Component;
import java.awt.Point;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import games.stendhal.client.entity.NPC;
import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.entity.StatusID;
import games.stendhal.client.gui.j2d.entity.EntityView;
import games.stendhal.client.gui.wt.EntityViewCommandList;
import marauroa.common.game.RPObject;

/**
* Context popup dedicated to NPC conversations.
*/
public class NPCInteractionMenu extends JPopupMenu {
	private static final long serialVersionUID = 1L;

	private final NPC npc;
	private final EntityView<?> view;
	private final String[] defaultActions;
	private final Component anchor;
	private final Point popupLocation;
	private final NPCInteractionManager manager;

	public NPCInteractionMenu(final NPC npc, final EntityView<?> view, final String[] defaultActions,
			final Component anchor, final Point popupLocation) {
		this.npc = npc;
		this.view = view;
		this.defaultActions = defaultActions;
		this.anchor = anchor;
		this.popupLocation = popupLocation;
		this.manager = NPCInteractionManager.get();

		buildMenu();
	}

	private void buildMenu() {
		removeAll();

		if (!manager.isInteracting(npc)) {
			add(createGreetingItem());
			addSeparator();
		} else {
			add(createActionMenu());
			addSeparator();
			add(createEndConversationItem());
			addSeparator();
		}

		add(createCancelItem());
		addDefaultActions();
	}

	private JMenuItem createGreetingItem() {
		final JMenuItem item = new JMenuItem("Przywitaj się");
		item.addActionListener(e -> {
			setVisible(false);
			manager.startInteraction(npc);
		});
		return item;
	}

	private JMenu createActionMenu() {
		final JMenu menu = new JMenu("Akcje");

		if (hasJobOption()) {
			menu.add(createConversationItem("Praca", () -> manager.requestJob(npc)));
		}
		if (hasOfferOption()) {
			menu.add(createConversationItem("Oferta", () -> manager.requestOffer(npc)));
		}
		menu.add(createConversationItem("Pomoc", () -> manager.requestHelp(npc)));
		if (hasQuestOption()) {
			menu.add(createConversationItem("Zadanie", () -> manager.requestQuest(npc)));
		}

		return menu;
	}

	private JMenuItem createConversationItem(final String label, final Runnable action) {
		final JMenuItem item = new JMenuItem(label);
		item.addActionListener(e -> {
			setVisible(false);
			action.run();
		});
		return item;
	}

	private JMenuItem createEndConversationItem() {
		final JMenuItem item = new JMenuItem("Zakończ rozmowę");
		item.addActionListener(e -> {
			setVisible(false);
			manager.endInteraction(npc);
		});
		return item;
	}

	private JMenuItem createCancelItem() {
		final JMenuItem item = new JMenuItem("Anuluj");
		item.addActionListener(e -> setVisible(false));
		return item;
	}

	private void addDefaultActions() {
		if ((defaultActions == null) || (defaultActions.length == 0) || (anchor == null) || (popupLocation == null)) {
			return;
		}

		final JMenuItem more = new JMenuItem("Pozostałe akcje…");
		more.addActionListener(e -> showDefaultMenu());
		addSeparator();
		add(more);
	}

	private void showDefaultMenu() {
		final EntityViewCommandList fallback = new EntityViewCommandList(npc.getType(), defaultActions, view);
		fallback.show(anchor, popupLocation.x, popupLocation.y);
	}

	private boolean hasJobOption() {
		if (!(npc instanceof RPEntity)) {
			return false;
		}
		final RPEntity entity = (RPEntity) npc;
		if (entity.hasStatus(StatusID.HEALER) || entity.hasStatus(StatusID.PRODUCER)) {
			return true;
		}
		final RPObject object = npc.getRPObject();
		return (object != null) && (object.has("job") || object.has("jobinfo"));
	}

	private boolean hasOfferOption() {
		if (!(npc instanceof RPEntity)) {
			return false;
		}
		final RPEntity entity = (RPEntity) npc;
		if (entity.hasStatus(StatusID.MERCHANT)) {
			return true;
		}
		final RPObject object = npc.getRPObject();
		return (object != null) && object.has("shop");
	}

	private boolean hasQuestOption() {
		final RPObject object = npc.getRPObject();
		if (object == null) {
			return false;
		}
		if (object.has("quest") || object.has("quest_available")) {
			return true;
		}
		if (object.has("questgiver") || object.has("quest_giver")) {
			return true;
		}
		return false;
	}
}
