/***************************************************************************
 *                 (C) Copyright 2003 - 2015 Faiumoni e.V.                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.login;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;

import org.apache.log4j.Logger;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.stendhal;
import games.stendhal.client.gui.NumberDocumentFilter;
import games.stendhal.client.gui.ProgressBar;
import games.stendhal.client.gui.StartupConnectionSettings;
import games.stendhal.client.gui.WindowUtils;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.sprite.DataLoader;
import games.stendhal.client.update.ClientGameConfiguration;
import games.stendhal.common.MathHelper;
import marauroa.client.BannedAddressException;
import marauroa.client.LoginFailedException;
import marauroa.client.TimeoutException;
import marauroa.common.io.Persistence;
import marauroa.common.net.InvalidVersionException;
import marauroa.common.net.message.MessageS2CLoginNACK;

/**
 * Server login dialog.
 */
public class LoginDialog extends JDialog {
	private static final String SELECTED_PROFILE_PROPERTY = "ui.window.login.profile";

	private ProfileList profiles;

	private JComboBox<Profile> profilesComboBox;

	private JCheckBox saveLoginBox;

	private JTextField usernameField;

	private JPasswordField passwordField;

	private JTextField serverField;

	private JTextField serverPortField;
	private JLabel connectionLabel;

	private JButton loginButton;

	private JButton steamButton;

	private JLabel steamStatusLabel;

	private volatile boolean steamLoginRunning;

	private JButton removeButton;

	private final StendhalClient client;

	private ProgressBar progressBar;
	/** Object checking that all required fields are filled */
	private DataValidator fieldValidator;

	/**
	 * Create a new LoginDialog.
	 *
	 * @param owner parent window
	 * @param client client
	 */
	public LoginDialog(final Frame owner, final StendhalClient client) {
		super(owner, true);
		this.client = client;
		initializeComponent();
		WindowUtils.closeOnEscape(this);
	}

	/**
	 * Create the dialog contents.
	 */
	private void initializeComponent() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		if (getOwner() != null) {
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					getOwner().setEnabled(true);
				}
			});
		}

		JLabel l;

		this.setTitle("Zaloguj się do serwera");
		this.setResizable(false);

		//
		// contentPane
		//
		JComponent contentPane = (JComponent) getContentPane();
		contentPane.setLayout(new GridBagLayout());
		final int pad = SBoxLayout.COMMON_PADDING;
		contentPane.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));

		final GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.LINE_START;

		/*
		 * Profiles
		 */
		l = new JLabel("Profile kont");

		c.insets = new Insets(4, 4, 15, 4);
		// column
		c.gridx = 0;
		 // row
		c.gridy = 0;
		contentPane.add(l, c);

		profilesComboBox = new JComboBox<Profile>();
		profilesComboBox.addActionListener(new ProfilesCB());

		/*
		 * Remove profile button
		 */
		removeButton = createRemoveButton();

		// Container for the profiles list and the remove button
		JComponent box = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, pad);
		profilesComboBox.setAlignmentY(Component.CENTER_ALIGNMENT);
		box.add(profilesComboBox);
		JButton newLoginButton = new JButton("Nowe");
		newLoginButton.setToolTipText("Zaloguj inne konto, używając domyślnego połączenia");
		newLoginButton.addActionListener(event -> {
			profilesComboBox.setSelectedItem(null);
			usernameField.requestFocusInWindow();
		});
		box.add(newLoginButton);
		box.add(removeButton);

		c.gridx = 1;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		contentPane.add(box, c);

		l = new JLabel("Serwer");
		c.insets = new Insets(4, 4, 4, 4);
		c.gridx = 0;
		c.gridy = 1;
		contentPane.add(l, c);

		connectionLabel = new JLabel();
		JButton changeConnectionButton = new JButton("Zmień");
		JComponent connectionRow = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, pad);
		connectionRow.add(connectionLabel);
		connectionRow.add(changeConnectionButton);
		c.gridx = 1;
		c.gridy = 1;
		c.fill = GridBagConstraints.BOTH;
		contentPane.add(connectionRow, c);

		serverField = new JTextField(StartupConnectionSettings.getHost(), 22);
		serverPortField = new JTextField(String.valueOf(StartupConnectionSettings.getPort()), 7);
		((AbstractDocument) serverPortField.getDocument()).setDocumentFilter(
				new NumberDocumentFilter(serverPortField, false));
		JComponent connectionFields = SBoxLayout.createContainer(SBoxLayout.VERTICAL, pad);
		JComponent hostRow = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, pad);
		hostRow.add(new JLabel("Adres serwera"));
		hostRow.add(serverField);
		connectionFields.add(hostRow, SLayout.EXPAND_X);
		JComponent portRow = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, pad);
		portRow.add(new JLabel("Port"));
		portRow.add(serverPortField);
		connectionFields.add(portRow, SLayout.EXPAND_X);
		connectionFields.setVisible(false);
		c.gridx = 1;
		c.gridy = 2;
		c.insets = new Insets(4, 4, 4, 4);
		c.fill = GridBagConstraints.BOTH;
		contentPane.add(connectionFields, c);
		changeConnectionButton.addActionListener(event -> {
			boolean show = !connectionFields.isVisible();
			connectionFields.setVisible(show);
			changeConnectionButton.setText(show ? "Ukryj" : "Zmień");
			pack();
			setLocationRelativeTo(getOwner());
		});

		/*
		 * Username
		 */
		l = new JLabel("Wprowadź imię wojownika");
		c.insets = new Insets(4, 4, 4, 4);
		c.gridx = 0;
		c.gridy = 3;
		contentPane.add(l, c);

		usernameField = new JTextField();

		c.gridx = 1;
		c.gridy = 3;
		c.fill = GridBagConstraints.BOTH;
		contentPane.add(usernameField, c);

		/*
		 * Password
		 */
		l = new JLabel("Wprowadź hasło");

		c.gridx = 0;
		c.gridy = 4;
		c.fill = GridBagConstraints.NONE;
		contentPane.add(l, c);

		passwordField = new JPasswordField();

		c.gridx = 1;
		c.gridy = 4;
		c.fill = GridBagConstraints.BOTH;
		contentPane.add(passwordField, c);

		/*
		 * Save Profile/Login
		 */
		saveLoginBox = new JCheckBox("Zapamiętaj nazwę konta");
		saveLoginBox.setSelected(false);
		saveLoginBox.setToolTipText("Zapisuje nazwę konta i serwer, ale nigdy hasło");

		c.gridx = 0;
		c.gridy = 5;
		c.fill = GridBagConstraints.NONE;
		contentPane.add(saveLoginBox, c);

		loginButton = new JButton();
		loginButton.setText("Zaloguj do serwera");
		loginButton.setMnemonic(KeyEvent.VK_L);
		this.rootPane.setDefaultButton(loginButton);

		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				loginButtonActionPerformed();
			}
		});

		JComponent buttonBox = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, SBoxLayout.COMMON_PADDING);
		JButton cancelButton = new JButton("Anuluj");
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispatchEvent(new WindowEvent(LoginDialog.this, WindowEvent.WINDOW_CLOSING));
			}
		});
		buttonBox.add(cancelButton);
		buttonBox.add(loginButton);

		c.gridx = 1;
		c.gridy = 5;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.LAST_LINE_END;
		c.insets = new Insets(0, 0, SBoxLayout.COMMON_PADDING, SBoxLayout.COMMON_PADDING);
		contentPane.add(buttonBox, c);

		steamButton = new JButton("Zaloguj przez Steam");
		steamButton.addActionListener(event -> steamButtonActionPerformed());
		c.gridx = 1;
		c.gridy = 6;
		c.gridheight = 1;
		c.insets = new Insets(8, 4, 4, 4);
		c.fill = GridBagConstraints.HORIZONTAL;
		contentPane.add(steamButton, c);

		steamStatusLabel = new JLabel();
		steamStatusLabel.setVisible(false);
		c.gridy = 7;
		c.insets = new Insets(2, 4, 4, 4);
		contentPane.add(steamStatusLabel, c);

		// Before loading profiles so that we can catch the data filled from
		// there
		bindEditListener();
		bindConnectionSummary();

		/*
		 * Load saved profiles
		 */
		profiles = loadProfiles();
		populateProfiles(profiles);

		//
		// Dialog
		//

		this.pack();
		usernameField.requestFocusInWindow();
		if (getOwner() != null) {
			getOwner().setEnabled(false);
			this.setLocationRelativeTo(getOwner());
		}
	}

	/**
	 * Prepare the field validator and bind it to the relevant text fields.
	 */
	private void bindEditListener() {
		fieldValidator = new DataValidator(loginButton,
				serverField.getDocument(), serverPortField.getDocument(),
				usernameField.getDocument(), passwordField.getDocument());
	}

	private void bindConnectionSummary() {
		DocumentListener listener = new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent event) {
				updateConnectionSummary();
			}

			@Override
			public void removeUpdate(DocumentEvent event) {
				updateConnectionSummary();
			}

			@Override
			public void changedUpdate(DocumentEvent event) {
				updateConnectionSummary();
			}
		};
		serverField.getDocument().addDocumentListener(listener);
		serverPortField.getDocument().addDocumentListener(listener);
		updateConnectionSummary();
	}

	private void updateConnectionSummary() {
		connectionLabel.setText(serverField.getText().trim() + ":" + serverPortField.getText().trim());
	}

	/**
	 * Create the remove character button.
	 *
	 * @return JButton
	 */
	private JButton createRemoveButton() {
		final URL url = DataLoader.getResource("data/gui/trash.png");
		ImageIcon icon = new ImageIcon(url);
		JButton button = new JButton(icon);
		// Clear the margins that buttons normally add
		button.setMargin(new Insets(0, 0, 0, 0));
		button.setToolTipText("Usuwa wybrane konto z listy");

		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				removeButtonActionPerformed();
			}
		});

		return button;
	}

	/**
	 * Called when the login button is activated.
	 */
	private void loginButtonActionPerformed() {
		// If this window isn't enabled, we shouldn't act.
		if (!isEnabled() || steamLoginRunning) {
			return;
		}
		setEnabled(false);

		Profile profile;
		profile = new Profile();

		profile.setHost((serverField.getText()).trim());

		try {
			profile.setPort(StartupConnectionSettings.parsePort(serverPortField.getText()));
		} catch (final IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this,
					ex.getMessage(),
					"Nieprawidłowy port", JOptionPane.WARNING_MESSAGE);
			setEnabled(true);
			return;
		}

		profile.setUser(usernameField.getText().trim());
		profile.setPassword(new String(passwordField.getPassword()));

		/*
		 * Save profile?
		 */
		if (saveLoginBox.isSelected()) {
			Profile savedProfile = new Profile();
			savedProfile.setHost(profile.getHost());
			savedProfile.setPort(profile.getPort());
			savedProfile.setUser(profile.getUser());
			profiles.add(savedProfile);
			populateProfiles(profiles);
			profilesComboBox.setSelectedItem(savedProfile);
			passwordField.setText(profile.getPassword());
			saveProfiles(profiles);
		}
		setCurrentProfileAsDefault();

		/*
		 * Run the connection procces in separate thread. added by TheGeneral
		 */
		final Thread t = new Thread(new ConnectRunnable(profile), "Login");
		t.start();
	}

	/**
	 * Called when the remove profile button is activated.
	 */
	private void removeButtonActionPerformed() {
		// If this window isn't enabled, we shouldn't act.
		if (!isEnabled() || profilesComboBox.getSelectedItem() == null) {
			return;
		}
		setEnabled(false);

		Profile profile;

		profile = (Profile) profilesComboBox.getSelectedItem();
		Object[] options = { "Usuń", "Anuluj" };

		Integer confirmRemoveProfile = JOptionPane.showOptionDialog(this,
			"Zostanie usunięty twój profil z lokalnej listy kont.\n"
			+ "Nie usunie to twojego konta z żadnego serwera.\n"
			+ "Czy chcesz usunąć profil \'" + profile.getUser() + "@" + profile.getHost() + "\'?",
			"Usuwa profil użytkownika z lokalnej listy kont",
			JOptionPane.OK_CANCEL_OPTION,
			JOptionPane.QUESTION_MESSAGE,
			null,
			options,
			options[1]);

		if (confirmRemoveProfile == 0) {
			profiles.remove(profile);
			saveProfiles(profiles);
			profiles = loadProfiles();
			populateProfiles(profiles);
			setCurrentProfileAsDefault();
		}

		setEnabled(true);
	}

	private void steamButtonActionPerformed() {
		if (steamLoginRunning) {
			return;
		}
		final String host = serverField.getText().trim();
		final String defaultHost = ClientGameConfiguration.get("DEFAULT_SERVER");
		final int port;
		try {
			port = StartupConnectionSettings.parsePort(serverPortField.getText());
		} catch (IllegalArgumentException exception) {
			JOptionPane.showMessageDialog(this, exception.getMessage(), "Logowanie Steam", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (!host.equalsIgnoreCase(defaultHost)
				|| port != Integer.parseInt(ClientGameConfiguration.get("DEFAULT_PORT"))) {
			JOptionPane.showMessageDialog(this, "Logowanie Steam jest dostępne tylko na oficjalnym serwerze.",
					"Logowanie Steam", JOptionPane.WARNING_MESSAGE);
			return;
		}

		steamLoginRunning = true;
		steamButton.setEnabled(false);
		steamStatusLabel.setVisible(true);
		steamStatusLabel.setText("Łączę się ze stroną...");
		pack();
		final Thread worker = new Thread(() -> {
			try {
				final SteamGameLoginClient website = new SteamGameLoginClient(
						ClientGameConfiguration.get("STEAM_LOGIN_SITE"));
				final SteamGameLoginClient.StartResult request = website.start();
				SwingUtilities.invokeLater(() -> steamStatusLabel.setText("Kod do wpisania na stronie: " + request.code));
				if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
					throw new IOException("Nie można otworzyć systemowej przeglądarki.");
				}
				Desktop.getDesktop().browse(URI.create(request.verificationUrl));
				final long deadline = System.currentTimeMillis() + request.expiresInSeconds * 1000L;
				while (System.currentTimeMillis() < deadline && isDisplayable()) {
					Thread.sleep(3000L);
					final SteamGameLoginClient.Credentials credentials = website.poll(request);
					if (credentials != null) {
						if (!isDisplayable()) {
							return;
						}
						SwingUtilities.invokeLater(() -> steamStatusLabel.setText("Logowanie do gry..."));
						final Profile profile = new Profile();
						profile.setHost(host);
						profile.setPort(port);
						profile.setUser(credentials.username);
						profile.setPassword("");
						profile.setSeed(credentials.seed);
						connect(profile);
						return;
					}
				}
				if (isDisplayable()) {
					throw new IOException("Czas logowania Steam minął. Spróbuj ponownie.");
				}
			} catch (IOException | InterruptedException | RuntimeException exception) {
				if (isDisplayable()) {
					SwingUtilities.invokeLater(() -> {
						steamStatusLabel.setText("Nie udało się zalogować przez Steam.");
						JOptionPane.showMessageDialog(this, exception.getMessage(), "Logowanie Steam", JOptionPane.ERROR_MESSAGE);
					});
				}
			} finally {
				steamLoginRunning = false;
				SwingUtilities.invokeLater(() -> {
					if (isDisplayable()) {
						steamButton.setEnabled(true);
					}
				});
			}
		}, "Steam login");
		worker.setDaemon(true);
		worker.start();
	}

	private void setCurrentProfileAsDefault() {
		final int currentIndex = profilesComboBox.getSelectedIndex();
		if (currentIndex >= 0) {
			WtWindowManager.getInstance().setProperty(SELECTED_PROFILE_PROPERTY, String.valueOf(currentIndex));
		} else {
			WtWindowManager.getInstance().setProperty(SELECTED_PROFILE_PROPERTY, "new");
		}
	}

	@Override
	public void setEnabled(final boolean b) {
		super.setEnabled(b);
		// Enabling login button is conditional
		fieldValidator.revalidate();
		removeButton.setEnabled(b && profilesComboBox.getSelectedItem() != null);
		if (steamButton != null) {
			steamButton.setEnabled(b && !steamLoginRunning);
		}
	}

	/**
	 * Connect to a server using a given profile.
	 *
	 * @param profile profile used for login
	 */
	public void connect(final Profile profile) {
		// We are not in EDT
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progressBar = new ProgressBar(LoginDialog.this);
				progressBar.start();
			}
		});

		try {
			client.connect(profile.getHost(), profile.getPort());

			// for each major connection milestone call step(). progressBar is
			// created in EDT, so it is not guaranteed non null in the main
			// thread.
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					progressBar.step();
				}
			});
		} catch (final Exception ex) {
			// if something goes horribly just cancel the progressbar
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					progressBar.cancel();
					setEnabled(true);
				}
			});
			String message = "Nie można połączyć się z serwerem";
			
			if (profile != null) {
				message = message + " " + profile.getHost() + ":" + profile.getPort();
			} else {
				message = message + ", because profile was null";
			}
			Logger.getLogger(LoginDialog.class).error(message, ex);
			handleError("Nie można połączyć się z serwerem. Źle wpisałeś nazwę serwera?", "Nieprawidłowe połączenie");
			return;
		}

		try {
			client.setAccountUsername(profile.getUser());
			client.setCharacter(profile.getCharacter());
			client.login(profile.getUser(), profile.getPassword(), profile.getSeed());
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					progressBar.finish();
					// workaround near failures in AWT at openjdk (tested on openjdk-1.6.0.0)
					try {
						setVisible(false);
					} catch (NullPointerException npe) {
						Logger.getLogger(LoginDialog.class).error("Error probably related to bug in JRE occured", npe);
						LoginDialog.this.dispose();
					}
				}
			});

		} catch (final InvalidVersionException e) {
			handleError("Uruchomiłeś starszą wersję gry. Proszę zaktualizuj",
					"Starsza wersja");
		} catch (final TimeoutException e) {
			handleError("Serwer jest niedostępny. Możliwe, że padł, a jeżeli używasz innego serwera to sprawdź czy dobrze wpisałeś jego nazwę i numer portu.",
					"Błąd logowania");
		} catch (final LoginFailedException e) {
			handleError(e.getMessage(), "Nie powiodło się logowanie");
			if (e.getReason() == MessageS2CLoginNACK.Reasons.SEED_WRONG
					&& (profile.getSeed() == null || !profile.getSeed().startsWith("S_"))) {
				System.exit(1);
			}
		} catch (final BannedAddressException e) {
			handleError("Twoje IP zostało zablokowane. Jeżeli nie zgadzasz się z tą decyzją to skontaktuj się z nami na https://s1.polanieonline.eu/kontakt-gmgags.html",
					"Zablokowane IP");
		}
	}

	/**
	 * Displays the error message, removes the progress bar and
	 * either enabled the login dialog in interactive mode or exits
	 * the client in non interactive mode.
	 *
	 * @param errorMessage error message
	 * @param errorTitle   title of error dialog box
	 */
	private void handleError(final String errorMessage, final String errorTitle) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progressBar.cancel();
				JOptionPane.showMessageDialog(
						LoginDialog.this, errorMessage, errorTitle, JOptionPane.ERROR_MESSAGE);

				if (isVisible()) {
					setEnabled(true);
				} else {
					// Hack for non interactive login
					System.exit(1);
				}
			}
		});
	}

	/**
	 * Load saves profiles.
	 * @return ProfileList
	 */
	private ProfileList loadProfiles() {
		final ProfileList tmpProfiles = new ProfileList();

		try {
			final InputStream is = Persistence.get().getInputStream(false, stendhal.getGameFolder(),
					"user.dat");
			boolean hadSavedPasswords;

			try {
				hadSavedPasswords = tmpProfiles.load(is);
			} finally {
				is.close();
			}
			if (hadSavedPasswords) {
				// Replace legacy profiles that contained reversibly encoded passwords.
				if (saveProfiles(tmpProfiles)) {
					SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
							"Stare zapamiętane hasła zostały usunięte z profili. Wpisz hasło przy logowaniu.",
							"Bezpieczeństwo konta", JOptionPane.INFORMATION_MESSAGE));
				} else {
					SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this,
							"Nie udało się usunąć starych haseł z pliku user.dat. Usuń ten plik ręcznie.",
							"Bezpieczeństwo konta", JOptionPane.WARNING_MESSAGE));
				}
			}
		} catch (final FileNotFoundException fnfe) {
			// Ignore
		} catch (final IOException ioex) {
			JOptionPane.showMessageDialog(this,
					"Wystąpił błąd podczas ładowania informacji o logowaniu",
					"Błąd Ładowania Informacji Logowania",
					JOptionPane.WARNING_MESSAGE);
		}

		return tmpProfiles;
	}

	/**
	 * Populate the profiles combobox and select the default.
	 *
	 * @param profiles profile data
	 */
	private void populateProfiles(final ProfileList profiles) {
		profilesComboBox.removeAllItems();

		for (Profile p : profiles) {
			profilesComboBox.addItem(p);
		}

		selectDefaultProfile();
	}

	private void selectDefaultProfile() {
		String profileIndexProperty = WtWindowManager.getInstance().getProperty(SELECTED_PROFILE_PROPERTY, "-1");
		if ("new".equals(profileIndexProperty)) {
			profilesComboBox.setSelectedItem(null);
			return;
		}
		int savedProfileIndex = MathHelper.parseIntDefault(profileIndexProperty, -1);
		final int count = profilesComboBox.getItemCount();
		if (savedProfileIndex >= 0 && savedProfileIndex < count) {
			profilesComboBox.setSelectedIndex(savedProfileIndex);
		} else if (count != 0) {
			// The last profile is the default.
			profilesComboBox.setSelectedIndex(count - 1);
		}
	}

	/**
	 * Called when a profile selection is changed.
	 */
	private void profilesCB() {
		Profile profile;
		String host;

		// This *should* be generic in swing, but it is not
		profile = (Profile) profilesComboBox.getSelectedItem();
		if (removeButton != null) {
			removeButton.setEnabled(isEnabled() && profile != null);
		}

		if (profile != null) {
			host = profile.getHost();
			serverField.setText(host);

			serverPortField.setText(String.valueOf(profile.getPort()));

			usernameField.setText(profile.getUser());
			passwordField.setText(profile.getPassword());
		} else {

			serverField.setText(StartupConnectionSettings.getHost());
			serverPortField.setText(String.valueOf(StartupConnectionSettings.getPort()));

			usernameField.setText("");
			passwordField.setText("");
		}
	}

	/**
	 * Checks that a group of Documents (text fields) is not empty, and enables
	 * or disables a JComponent on that condition.
	 */
	private static class DataValidator implements DocumentListener {
		private final Document[] documents;
		private final JComponent component;

		/**
		 * Create a new DataValidator.
		 *
		 * @param component component to be enabled depending on the state of
		 *  documents
		 * @param docs documents
		 */
		DataValidator(JComponent component, Document... docs) {
			this.component = component;
			documents = docs;
			for (Document doc : docs) {
				doc.addDocumentListener(this);
			}
			revalidate();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			revalidate();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if (e.getDocument().getLength() == 0) {
				component.setEnabled(false);
			}
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			// Attribute change - ignore
		}

		/**
		 * Do a full document state check and set the component status according
		 * to the result.
		 */
		final void revalidate() {
			for (Document doc : documents) {
				if (doc.getLength() == 0) {
					component.setEnabled(false);
					return;
				}
			}
			component.setEnabled(true);
		}
	}

	/*
	 * Author: Da_MusH Description: Methods for saving and loading login
	 * information to disk. These should probably make a separate class in the
	 * future, but it will work for now. comment: Thegeneral has added encoding
	 * for password and username. Changed for multiple profiles.
	 */
	private boolean saveProfiles(final ProfileList profiles) {
		try {
			final OutputStream os = Persistence.get().getOutputStream(false,
					stendhal.getGameFolder(), "user.dat");

			try {
				profiles.save(os);
			} finally {
				os.close();
			}
			return true;
		} catch (final IOException ioex) {
			JOptionPane.showMessageDialog(this,
					"Wystąpił błąd podczas zapisywania informacji o logowaniu",
					"Błąd Zapisu Informacji Logowania",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}

	/**
	 * Server connect thread runnable.
	 */
	private final class ConnectRunnable implements Runnable {
		private final Profile profile;

		/**
		 * Create a new ConnectRunnable.
		 *
		 * @param profile profile used for connection
		 */
		private ConnectRunnable(final Profile profile) {
			this.profile = profile;
		}

		@Override
		public void run() {
			connect(profile);
		}
	}

	/**
	 * Profiles combobox selection change listener.
	 */
	private class ProfilesCB implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			profilesCB();
		}
	}

}
