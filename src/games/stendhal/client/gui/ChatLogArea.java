/***************************************************************************
*                (C) Copyright 2003-2018 - Faiumoni e.V.                  *
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

import static games.stendhal.client.gui.settings.SettingsProperties.MSG_BLINK;
import static games.stendhal.client.gui.settings.SettingsProperties.MSG_SOUND;

import java.awt.Color;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.client.gui.wt.core.SettingChangeAdapter;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.sound.facade.InfiniteAudibleArea;
import games.stendhal.client.sound.facade.SoundFileType;
import games.stendhal.client.sound.facade.SoundGroup;
import games.stendhal.common.NotificationType;
import games.stendhal.common.constants.SoundLayer;

class ChatLogArea {
	/** Background color of the private chat tab. Light blue. */
	private static final String PRIVATE_TAB_COLOR = "0x3c1e00";
	private static final Logger logger = Logger.getLogger(ChatLogArea.class);

	private final NotificationChannelManager channelManager;
	private final JTabbedPane tabs = new JTabbedPane(SwingConstants.BOTTOM);
	private final Timer animator = new Timer(100, null);

	ChatLogArea(NotificationChannelManager channelManager) {
		this.channelManager = channelManager;
		createLogArea();
	}

	/**
	 * Create the chat log tabs.
	 *
	 * @return chat log area
	 */
	private JTabbedPane createLogArea() {
		tabs.setFocusable(false);
		List<ChatLogView> logs = createChannelComponents();
		BitSet changedChannels = new BitSet(logs.size());

		// Must be done before adding tabs
		setupTabChangeHandling(changedChannels);

		Iterator<NotificationChannel> it = channelManager.getChannels().iterator();
		for (ChatLogView view : logs) {
			tabs.add(it.next().getName(), view.getComponent());
		}

		setupHiddenChannelMessageHandling(changedChannels);
		setupAnimation(changedChannels);

		return tabs;
	}

	/**
	 * Create chat channels.
	 *
	 * @return Chat log components of the notification channels
	 */
	private List<ChatLogView> createChannelComponents() {
		List<ChatLogView> list = new ArrayList<>();
		ChatLogView view = createChatComponent();
		list.add(view);

		NotificationChannel mainChannel = setupMainChannel(view);
		channelManager.addChannel(mainChannel);

		// ** Private channel **
		view = createChatComponent();
		list.add(view);
		NotificationChannel personal = setupPersonalChannel(view);
		channelManager.addChannel(personal);

		return list;
	}

	private ChatLogView createChatComponent() {
		try {
			return new WebChatLogView();
		} catch (UnsupportedOperationException ex) {
			logger.info(ex.getMessage() + "; using legacy chat log component.");
			return new KTextEdit();
		} catch (Throwable ex) {
			logger.warn("Falling back to legacy chat log component", ex);
			return new KTextEdit();
		}
	}

	private NotificationChannel setupPersonalChannel(ChatLogView view) {
		view.setChannelName("Prywatny");
		/*
		 * Give it a different background color to make it different from the main
		 * chat log.
		 */
		view.setDefaultBackground(Color.decode(PRIVATE_TAB_COLOR));
		/*
		 * Types shown by default in the private/group tab. Admin messages should
		 * occur everywhere, of course, and not be possible to be disabled in
		 * preferences.
		 */
		String personalDefault = NotificationType.PRIVMSG.toString() + ","
				+ NotificationType.CLIENT + "," + NotificationType.GROUP + ","
				+ NotificationType.TUTORIAL + "," + NotificationType.SUPPORT;
		return new NotificationChannel("Prywatny", view, false, personalDefault);
	}

	private NotificationChannel setupMainChannel(ChatLogView view) {
		NotificationChannel channel = new NotificationChannel("Główny", view, true, "");

		// Follow settings changes for the main channel
		WtWindowManager wm = WtWindowManager.getInstance();
		wm.registerSettingChangeListener("ui.healingmessage", new SettingChangeAdapter("ui.healingmessage", "false") {
			@Override
			public void changed(String newValue) {
				channel.setTypeFiltering(NotificationType.HEAL, Boolean.parseBoolean(newValue));
			}
		});
		wm.registerSettingChangeListener("ui.poisonmessage", new SettingChangeAdapter("ui.poisonmessage", "false") {
			@Override
			public void changed(String newValue) {
				channel.setTypeFiltering(NotificationType.POISON, Boolean.parseBoolean(newValue));
			}
		});
		return channel;
	}

	private void setupTabChangeHandling(BitSet changedChannels) {
		tabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int i = tabs.getSelectedIndex();
				NotificationChannel channel = channelManager.getChannels().get(i);
				channelManager.setVisibleChannel(channel);
				if (changedChannels.get(i)) {
					changedChannels.clear(i);
					// Remove modified marker
					tabs.setBackgroundAt(i, null);
					if (changedChannels.isEmpty()) {
						animator.stop();
					}
				}
			}
		});
	}

	private void setupHiddenChannelMessageHandling(BitSet changedChannels) {
		final WtWindowManager wm = WtWindowManager.getInstance();

		channelManager.addHiddenChannelListener(new NotificationChannelManager.HiddenChannelListener() {
			@Override
			public void channelModified(int index) {
				if (index == 1 && wm.getPropertyBoolean(MSG_SOUND, true)) {
					// play notification
					final String sndFile = "ui/notify_up.ogg";
					if (this.getClass().getResource("/data/sounds/" + sndFile) != null) {
						final SoundGroup group = ClientSingletonRepository.getSound()
								.getGroup(SoundLayer.USER_INTERFACE.groupName);
						group.loadSound(MSG_SOUND, sndFile, SoundFileType.OGG, false);
						group.play(MSG_SOUND, 0, new InfiniteAudibleArea(), null, false, true);
					}
				}

				// Mark the tab as modified so that the user can see there's new text
				if (!changedChannels.get(index)) {
					changedChannels.set(index);
					if (!animator.isRunning() && wm.getPropertyBoolean(MSG_BLINK, true)) {
						animator.start();
					}
				}
			}
		});
	}

	private void setupAnimation(final BitSet changedChannels) {
		animator.setInitialDelay(0);
		animator.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = changedChannels.nextSetBit(0); i >= 0; i = changedChannels.nextSetBit(i + 1)) {
					Color c = tabs.getBackgroundAt(i);
					if ((c == null) || (c.getTransparency() == Transparency.TRANSLUCENT)) {
						tabs.setBackgroundAt(i, StyleUtil.getColor(Style.CHANNEL_HILIGHT));
					} else {
						tabs.setBackgroundAt(i, null);
					}
				}
			}
		});
	}

	JComponent getComponent() {
		return tabs;
	}
}
