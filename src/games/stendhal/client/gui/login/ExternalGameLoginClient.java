package games.stendhal.client.gui.login;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/** Website bridge for confirmed Google and Steam authentication in the desktop client. */
final class ExternalGameLoginClient {
	private final URI site;
	private final ExternalLoginProvider provider;
	private final ConnectionFactory connections;

	interface ConnectionFactory {
		HttpURLConnection open(URL url) throws IOException;
	}

	ExternalGameLoginClient(final String siteUrl, final ExternalLoginProvider provider) throws IOException {
		this(siteUrl, provider, url -> (HttpURLConnection) url.openConnection());
	}

	ExternalGameLoginClient(final String siteUrl, final ExternalLoginProvider provider,
			final ConnectionFactory connections) throws IOException {
		this.provider = provider;
		this.connections = connections;
		try {
			site = URI.create(siteUrl);
			if (!"https".equalsIgnoreCase(site.getScheme()) || site.getHost() == null
					|| site.getUserInfo() != null || site.getRawQuery() != null || site.getRawFragment() != null) {
				throw new IllegalArgumentException("Invalid HTTPS site URL");
			}
		} catch (IllegalArgumentException exception) {
			throw new IOException("Nieprawidłowy adres strony logowania " + provider.label + ".", exception);
		}
	}

	StartResult start() throws IOException {
		final JSONObject response = post(provider.apiPath() + "/start", "{}", 201);
		final String id = requiredString(response, "id");
		final String secret = requiredString(response, "secret");
		final String code = requiredString(response, "code");
		final Object lifetime = response.get("expires_in");
		final String verificationUrl = requiredString(response, "verification_url");
		if (!id.matches("[0-9a-fA-F-]{36}") || !secret.matches("[0-9a-f]{64}")
				|| !code.matches("[0-9A-F]{10}") || !(lifetime instanceof Number)
				|| ((Number) lifetime).longValue() < 1 || ((Number) lifetime).longValue() > 600) {
			throw new IOException("Strona zwróciła nieprawidłowe żądanie logowania.");
		}
		final URI verification = URI.create(verificationUrl);
		if (!"https".equalsIgnoreCase(verification.getScheme())
				|| !site.getHost().equalsIgnoreCase(verification.getHost())
				|| effectivePort(site) != effectivePort(verification)
				|| verification.getUserInfo() != null
				|| !verification.getPath().equals("/auth/external/" + provider.id + "/redirect")) {
			throw new IOException("Strona zwróciła nieprawidłowy adres logowania.");
		}
		return new StartResult(id, secret, code, verificationUrl, ((Number) lifetime).longValue());
	}

	Credentials poll(final StartResult request) throws IOException {
		final HttpResult result = postRaw(provider.apiPath() + "/" + request.id + "/poll",
				"{\"secret\":\"" + request.secret + "\"}");
		if (result.status == 202) {
			return null;
		}
		if (result.status == 410) {
			throw new IOException("Czas logowania przez " + provider.label + " minął. Spróbuj ponownie.");
		}
		if (result.status == 403) {
			final Object message = result.body.get("message");
			throw new IOException(message instanceof String ? (String) message : "Logowanie przez " + provider.label + " zostało odrzucone.");
		}
		if (result.status != 200) {
			throw new IOException("Nie udało się potwierdzić logowania " + provider.label + " (HTTP " + result.status + ").");
		}
		final String username = requiredString(result.body, "username");
		final String seed = requiredString(result.body, "seed");
		if (seed.getBytes(StandardCharsets.UTF_8).length != 16) {
			throw new IOException("Strona zwróciła nieprawidłowy kod logowania do gry.");
		}
		return new Credentials(username, seed);
	}

	private JSONObject post(final String path, final String body, final int expectedStatus) throws IOException {
		final HttpResult result = postRaw(path, body);
		if (result.status != expectedStatus) {
			final Object message = result.body.get("message");
			throw new IOException(message instanceof String ? (String) message
					: "Usługa logowania " + provider.label + " jest niedostępna (HTTP " + result.status + ").");
		}
		return result.body;
	}

	private HttpResult postRaw(final String path, final String body) throws IOException {
		final URL url = site.resolve(path).toURL();
		final HttpURLConnection connection = connections.open(url);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Accept", "application/json");
		connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		connection.setConnectTimeout(5000);
		connection.setReadTimeout(8000);
		connection.setDoOutput(true);
		connection.setInstanceFollowRedirects(false);
		try {
			final byte[] data = body.getBytes(StandardCharsets.UTF_8);
			try (OutputStream output = connection.getOutputStream()) {
				output.write(data);
			}
			final int status = connection.getResponseCode();
			final InputStream input = status < 400 ? connection.getInputStream() : connection.getErrorStream();
			JSONObject json = new JSONObject();
			if (input != null) {
				try (InputStreamReader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
					final Object parsed = JSONValue.parse(reader);
					if (parsed instanceof JSONObject) {
						json = (JSONObject) parsed;
					}
				}
			}
			return new HttpResult(status, json);
		} finally {
			connection.disconnect();
		}
	}

	private static String requiredString(final JSONObject object, final String key) throws IOException {
		final Object value = object.get(key);
		if (!(value instanceof String) || ((String) value).isEmpty()) {
			throw new IOException("Niepełna odpowiedź strony logowania.");
		}
		return (String) value;
	}

	private static int effectivePort(final URI uri) {
		return uri.getPort() == -1 ? 443 : uri.getPort();
	}

	static final class StartResult {
		final String id;
		final String secret;
		final String code;
		final String verificationUrl;
		final long expiresInSeconds;

		StartResult(final String id, final String secret, final String code,
				final String verificationUrl, final long expiresInSeconds) {
			this.id = id;
			this.secret = secret;
			this.code = code;
			this.verificationUrl = verificationUrl;
			this.expiresInSeconds = expiresInSeconds;
		}
	}

	static final class Credentials {
		final String username;
		final String seed;

		Credentials(final String username, final String seed) {
			this.username = username;
			this.seed = seed;
		}
	}

	private static final class HttpResult {
		final int status;
		final JSONObject body;

		HttpResult(final int status, final JSONObject body) {
			this.status = status;
			this.body = body;
		}
	}
}
