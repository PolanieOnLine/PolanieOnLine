/***************************************************************************
 *                  (C) Copyright 2003 - 2015 Faiumoni e.V.                *
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

import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.text.AbstractDocument;

import org.apache.log4j.Logger;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.stendhal;
import games.stendhal.client.gui.NumberDocumentFilter;
import games.stendhal.client.gui.ProgressBar;
import games.stendhal.client.gui.WindowUtils;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.update.ClientGameConfiguration;
import marauroa.client.BannedAddressException;
import marauroa.client.LoginFailedException;
import marauroa.client.TimeoutException;
import marauroa.common.game.AccountResult;
import marauroa.common.net.InvalidVersionException;

/**
 * The account creation dialog. For requesting account name, password, and all
 * other needed data.
 */
public class CreateAccountDialog extends JDialog {
	/** Logger instance. */
	private static final Logger LOGGER = Logger.getLogger(CreateAccountDialog.class);

	/** User name input field. */
	private JTextField usernameField;
	/** Password input field. */
	private JPasswordField passwordField;
	/** Password verification field. */
	private JPasswordField passwordretypeField;
	/** Email input field. */
	private JTextField emailField;
	/** Server name input field. */
	private JTextField serverField;
	/** Server port input field. */
	private JTextField serverPortField;

	/** The client used for login. */
	private StendhalClient client;
	/** Descriptions of error conditions. */
	private String badEmailTitle, badEmailReason, badPasswordReason;

	/**
	 * Create an CreateAccountDialog for a parent window, and specified client.
	 *
	 * @param owner parent frame
	 * @param client client used for login
	 */
	public CreateAccountDialog(final Frame owner, final StendhalClient client) {
		super(owner, true);
		this.client = client;
		initializeComponent(owner);

		WindowUtils.closeOnEscape(this);
		this.setVisible(true);
	}

	/**
	 * A dumb constructor used only for tests.
	 */
	CreateAccountDialog() {
		super();
		initializeComponent(null);
	}

	/**
	 * Create the dialog contents.
	 *
	 * @param owner parent window
	 */
	private void initializeComponent(final Frame owner) {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		if (owner != null) {
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					owner.setEnabled(true);
				}
			});
		}

		JLabel serverLabel = new JLabel("Nazwa serwera");
		serverField = new JTextField(
				ClientGameConfiguration.get("DEFAULT_SERVER"));
		serverField.setEditable(true);
		JLabel serverPortLabel = new JLabel("Port serwera");
		serverPortField = new JTextField(
				ClientGameConfiguration.get("DEFAULT_PORT"));
		((AbstractDocument) serverPortField.getDocument()).setDocumentFilter(new NumberDocumentFilter(serverPortField, false));

		JLabel usernameLabel = new JLabel("Wybierz imię wojownika");
		usernameField = new JTextField();

		JLabel passwordLabel = new JLabel("Hasło (min. 6 znaków)");
		passwordField = new JPasswordField();

		JLabel passwordretypeLabel = new JLabel("Powtórz hasło");
		passwordretypeField = new JPasswordField();

		JLabel emailLabel = new JLabel("Adres e-mail (opcjonalnie)");
		emailField = new JTextField();

		// createAccountButton
		//
		JButton createAccountButton = new JButton();
		createAccountButton.setText("Utwórz Konto");
		createAccountButton.setMnemonic(KeyEvent.VK_A);
		this.rootPane.setDefaultButton(createAccountButton);
		createAccountButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				onCreateAccount();
			}
		});

		//
		// contentPane
		//
		int padding = SBoxLayout.COMMON_PADDING;
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new SBoxLayout(SBoxLayout.VERTICAL, padding));
		contentPane.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));

		JComponent grid = new JComponent() {};
		grid.setLayout(new GridLayout(0, 2, padding, padding));
		contentPane.add(grid, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));

		// row 0
		grid.add(serverLabel);
		grid.add(serverField);

		// row 1
		grid.add(serverPortLabel);
		grid.add(serverPortField);

		// row 2
		grid.add(usernameLabel);
		grid.add(usernameField);

		// row 3
		grid.add(passwordLabel);
		grid.add(passwordField);

		// row 4
		grid.add(passwordretypeLabel);
		grid.add(passwordretypeField);

		// row 5
		grid.add(emailLabel);
		grid.add(emailField);

		// A toggle for showing the contents of the password fields
		grid.add(new JComponent(){});
		JCheckBox showPWToggle = new JCheckBox("Pokaż hasło");
		showPWToggle.setHorizontalAlignment(SwingConstants.RIGHT);
		final char normalEchoChar = passwordField.getEchoChar();
		showPWToggle.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				char echoChar;
				if (e.getStateChange() == ItemEvent.SELECTED) {
					echoChar = (char) 0;
				} else {
					echoChar = normalEchoChar;
				}
				passwordField.setEchoChar(echoChar);
				passwordretypeField.setEchoChar(echoChar);
			}
		});
		grid.add(showPWToggle);

		// Warning label
		JLabel logLabel = new JLabel("<html><body><p><font size=\"-2\">Przy logowaniu będą zapisywane informacje, które będą identyfikować <br>Twój komputer w internecie w celu zapobiegania nadużyciom <br>(np. przy próbie odgadnięcia hasła w celu włamania się na konto <br>lub tworzeniu wielu kont w celu sprawiania problemów). <br>Ponadto wszystkie zdarzenia i akcje, które wydarzą się w grze <br>(np. rozwiązywanie zadań, atakowanie potworów) <br>są umieszczane w dzienniku. Te informacje będą wykorzystywane <br>w celu analizy luk i czasami w sprawach nadużyć.</font></p></body></html>");// Add a bit more empty space around it
		logLabel.setBorder(BorderFactory.createEmptyBorder(padding, padding, padding, padding));
		logLabel.setAlignmentX(CENTER_ALIGNMENT);
		contentPane.add(logLabel, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));

		// Button row
		JComponent buttonRow = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, SBoxLayout.COMMON_PADDING);
		buttonRow.setAlignmentX(RIGHT_ALIGNMENT);
		JButton cancelButton = new JButton("Zamknij");
		cancelButton.setMnemonic(KeyEvent.VK_C);
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispatchEvent(new WindowEvent(CreateAccountDialog.this, WindowEvent.WINDOW_CLOSING));
			}
		});
		buttonRow.add(cancelButton);
		buttonRow.add(createAccountButton);
		contentPane.add(buttonRow);

		// CreateAccountDialog
		this.setTitle("Utwórz Nowe Konto");
		this.setResizable(false);
		// required on Compiz
		this.pack();

		usernameField.requestFocusInWindow();
		if (owner != null) {
			owner.setEnabled(false);
			this.setLocationRelativeTo(owner);
		}
	}

	/**
	 * Run when the "Create account" button is activated.
	 */
	private void onCreateAccount() {
		final String accountUsername = usernameField.getText();
		final String password = new String(passwordField.getPassword());

		// If this window isn't enabled, we shouldn't act.
		if (!this.isEnabled()) {
			return;
		}

		final boolean ok = checkFields();

		if (!ok) {
			return;
		}

		final String email = emailField.getText();
		final String server = serverField.getText();
		int port = 32160;

		// port couldn't be accessed from inner class
		final int finalPort;
		final ProgressBar progressBar = new ProgressBar(this);

		try {
			port = Integer.parseInt(serverPortField.getText());
		} catch (final NumberFormatException ex) {
			JOptionPane.showMessageDialog(getOwner(),
					"Niewłaściwy numer portu. Spróbuj ponownie.",
					"Niewłaściwy Port", JOptionPane.WARNING_MESSAGE);
			return;
		}
		finalPort = port;

		// standalone check
		if (client == null) {
			JOptionPane.showMessageDialog(this,
					"Konto nie zostało utworzone!");
			return;
		}

		/* separate thread for connection process added by TheGeneral */
		// run the connection process in separate thread
		final Thread connectionThread = new Thread() {

			@Override
			public void run() {
				// initialize progress bar
				progressBar.start();
				// disable this screen when attempting to connect
				setEnabled(false);


				try {
					client.connect(server, finalPort);
					// for each major connection milestone call step()
					progressBar.step();
				} catch (final Exception ex) {
					// if something goes horribly just cancel the progress bar
					progressBar.cancel();
					setEnabled(true);
					JOptionPane.showMessageDialog(
							getOwner(),
							"Nie można się połączyć z serwerem w celu utworzenia konta. Serwer może nie działać, a jeśli korzystasz z innego serwera " +
							"to sprawdź czy poprawnie wpisałeś nazwę i numer portu.");

					LOGGER.error(ex, ex);

					return;
				}
				final Window owner = getOwner();
				try {
					final AccountResult result = client.createAccount(
							accountUsername, password, email);
					if (result.failed()) {
						/*
						 * If the account can't be created, show an error
						 * message and don't continue.
						 */
						progressBar.cancel();
						setEnabled(true);
						JOptionPane.showMessageDialog(owner,
								result.getResult().getText(),
								"Nie powiodło się tworzenie konta",
								JOptionPane.ERROR_MESSAGE);
					} else {

						/*
						 * Print username returned by server, as server can
						 * modify it at will to match account names rules.
						 */

						progressBar.step();
						progressBar.finish();

						client.setAccountUsername(accountUsername);
						client.setCharacter(accountUsername);

						/*
						 * Once the account is created, login into server.
						 */
						client.login(accountUsername, password);
						progressBar.step();
						progressBar.finish();

						setEnabled(false);
						if (owner != null) {
							owner.setVisible(false);
							owner.dispose();
						}

						stendhal.setDoLogin();
					}
				} catch (final TimeoutException e) {
					progressBar.cancel();
					setEnabled(true);
					JOptionPane.showMessageDialog(
							owner,
							"Nie można się połączyć z serwerem w celu utworzenia konta. Serwer może nie działać lub mogłeś wprowadzić niewłaściwą nazwę serwera i numer portu.",
							"Błąd Tworzenia Konta", JOptionPane.ERROR_MESSAGE);
				} catch (final InvalidVersionException e) {
					progressBar.cancel();
					setEnabled(true);
					JOptionPane.showMessageDialog(
							owner,
							"Uruchomiłeś starszą wersję gry. Proszę zaktualizuj swoją wersję",
							"Starsza wersja", JOptionPane.ERROR_MESSAGE);
				} catch (final BannedAddressException e) {
					progressBar.cancel();
					setEnabled(true);
					JOptionPane.showMessageDialog(
							owner,
							"Twoje IP zostało zablokowane. Jeżeli nie zgadzasz się z decyzją to skontaktuj się z nami na http://www.polskagra.net/kontakt-gmgags",
							"Zablokowane IP", JOptionPane.ERROR_MESSAGE);
				} catch (final LoginFailedException e) {
					progressBar.cancel();
					setEnabled(true);
					JOptionPane.showMessageDialog(owner, e.getMessage(),
							"Błąd logowania", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		};
		connectionThread.start();
	}

	/**
	 * Runs field checks, to, ex. confirm the passwords correct, etc.
	 * @return if no error found
	 */
	private boolean checkFields() {
		//
		// Check the password
		//
		final String password = new String(passwordField.getPassword());
		final String passwordretype = new String(
				passwordretypeField.getPassword());
		final Window owner = getOwner();
		if (!password.equals(passwordretype)) {
			JOptionPane.showMessageDialog(owner,
					"Hasła nie pasują do siebie. Powtórz oba hasła.",
					"Hasło nie pasuje", JOptionPane.WARNING_MESSAGE);
			return false;
		}

		//
		// Password strength
		//
		final boolean valPass = validatePassword(usernameField.getText(), password);
		if (!valPass) {
			if (badPasswordReason != null) {
				// didn't like the password for some reason, show a dialog and
				// try again
				final int i = JOptionPane.showOptionDialog(owner, badPasswordReason,
						"Niewałaściwe Hasło", JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE, null, null, 1);

				if (i == JOptionPane.NO_OPTION) {
					return false;
				}
			} else {
				return false;
			}
		}

		//
		// Check the email
		//
		final String email = (emailField.getText()).trim();
		if  (!validateEmail(email)) {
			final String warning = badEmailReason + "Adres e-mail jest jedynym sposobem, w jaki administratorzy mogą kontaktować się z prawowitym właścicielem konta.\n" + 
					"Jeśli go nie dostarczysz, nie będziesz mógł uzyskać nowego hasła do tego konta, na przykład:\n" + 
					"- Zapomniałeś hasła.\n" + 
					"- Inny gracz w jakiś sposób dostaje hasło i zmienia je.\n" + 
					"Czy mimo to chcesz kontynuować?";
			final int i = JOptionPane.showOptionDialog(owner, warning, badEmailTitle,
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE,
					null, null, 1);
			if (i != 0) {
				// no, let me type a valid email
				return false;
			}
			// yes, continue anyway
		}
		return true;
	}
	
	/**
	 * Validate email field format.
	 *
	 * @param email address to be validate
	 * @return <code>true</code> if the email looks good enough, otherwise
	 *	<code>false</code>
	 */
	private boolean validateEmail(final String email) {
		if  (email.isEmpty()) {
			badEmailTitle = "Pole adresu e-mail jest puste";
			badEmailReason = "Nie podałeś adresu e-mail.\n";
			return false;
		} else {
			if (!email.contains("@") || !email.contains(".") || (email.length() <= 5)) {
				badEmailTitle =  "Błąd w adresie e-mail?";
				badEmailReason = "Wpisany adres e-mail ma prawdopodobnie błąd.\n";
				return false;
			}
		}
		return true;
	}


	/**
	 * Prints text only when running stand-alone.
	 * @param text text to be printed
	 */
	private void debug(final String text) {
		if (client == null) {
			LOGGER.debug(text);
		}
	}

	/**
	 * Used to preview the CreateAccountDialog.
	 * @param args ignored
	 */
	public static void main(final String[] args) {
		new CreateAccountDialog(null, null);
	}

	/**
	 * Do some sanity checks for the password.
	 *
	 * @param username user name
	 * @param password checked password
	 * @return <code>true</code> if the password seems reasonable,
	 *	<code>false</code> if the password should be rejected
	 */
	boolean validatePassword(final String username, final String password) {
		if (password.length() > 5) {

			// check for all numbers
			boolean allNumbers = true;
			try {
				Integer.parseInt(password);
			} catch (final NumberFormatException e) {
				allNumbers = false;
			}
			if (allNumbers) {
				badPasswordReason = "W swoim haśle użyłeś tylko liczb. To nie jest bezpieczna metoda.\n"
						+ " Czy chcesz użyć tego hasła?";
			}

			// check for username
			boolean hasUsername = false;
			if (password.contains(username)) {
				hasUsername = true;
			}

			if (!hasUsername) {
				// now we'll do some more checks to see if the password
				// contains more than three letters of the username
				debug("Checking if password contains a derivative of the username, trimming from the back...");
				final int minUserLength = 3;
				for (int i = 1; i < username.length(); i++) {
					final String subuser = username.substring(0, username.length()
							- i);
					debug("\tsprawdzam for \"" + subuser + "\"...");
					if (subuser.length() <= minUserLength) {
						break;
					}

					if (password.contains(subuser)) {
						hasUsername = true;
						debug("Hasło zawiera nazwę konta!");
						break;
					}
				}

				if (!hasUsername) {
					// now from the end of the password..
					debug("Checking if password contains a derivative of the username, trimming from the front...");
					for (int i = 0; i < username.length(); i++) {
						final String subuser = username.substring(i);
						debug("\tsprawdzam for \"" + subuser + "\"...");
						if (subuser.length() <= minUserLength) {
							break;
						}
						if (password.contains(subuser)) {
							hasUsername = true;
							debug("Hasło zawiera nazwę konta!");
							break;
						}
					}
				}
			}

			if (hasUsername) {
				badPasswordReason = "W haśle użyłeś nazwę konta lub jest do niej podobna. To jest kiepskie zabezpieczenie konta.\n"
						+ " Jesteś pewien, że chcesz użyć tego hasła?";
				return false;
			}

		} else {
			final String text = "Hasło, które wprowadziłeś jest za krótkie. Musi się składać z minimum 6 znaków.";
			if (isVisible()) {
				JOptionPane.showMessageDialog(getOwner(), text);
			} else {
				LOGGER.warn(text);
			}
			return false;
		}

		return true;
	}
}
