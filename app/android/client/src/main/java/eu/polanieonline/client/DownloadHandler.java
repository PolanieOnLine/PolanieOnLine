/* Copyright 2022-2024 Faiumoni e. V., 2026 PolanieOnLine. GPL-2.0-or-later. */
package eu.polanieonline.client;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Saves screenshots and text exports without broad storage permission. */
public class DownloadHandler {
	private static final Logger LOG = LogManager.getLogger(DownloadHandler.class);
	private boolean result;
	private String message;

	public void download(String url, String mimetype, String name) {
		result = false;
		message = null;
		MainActivity activity = MainActivity.get();
		if (activity == null || activity.getActiveClientView() == null || !activity.getActiveClientView().isGameActive()) {
			message = "Pobieranie z tej strony nie jest obsługiwane.";
			return;
		}
		try {
			DecodedData export = decodeDataUrl(url);
			if (name == null || name.trim().isEmpty()) {
				name = "polanieonline_" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss.SSS", Locale.ROOT)
						.format(new Date()) + (export.png ? ".png" : ".txt");
			}
			message = "Zapisano plik: " + saveFile(activity, name, export);
			result = true;
		} catch (IOException | RuntimeException e) {
			// Data URLs may contain private conversations. Never log their contents.
			LOG.warn("Download failed: {}", e.getClass().getSimpleName());
			message = "Nie udało się zapisać pliku. Sprawdź dostępne miejsce w telefonie.";
		}
	}

	static final class DecodedData {
		final String type;
		final byte[] bytes;
		final boolean png;
		DecodedData(String type, byte[] bytes) {
			this.type = type;
			this.bytes = bytes;
			this.png = "image/png".equals(type);
		}
	}

	static DecodedData decodeDataUrl(String url) throws IOException {
		if (url == null || !url.startsWith("data:") || url.length() > 24 * 1024 * 1024) {
			throw new IllegalArgumentException("Unsupported download");
		}
		int comma = url.indexOf(',');
		if (comma < 0) { throw new IllegalArgumentException("Invalid data URL"); }
		String header = url.substring(5, comma).toLowerCase(Locale.ROOT);
		String type = header.split(";", 2)[0];
		if ((!"image/png".equals(type) && !"text/plain".equals(type))
				|| ("image/png".equals(type) && !header.endsWith(";base64"))) {
			throw new IllegalArgumentException("Unsupported media type");
		}
		String payload = url.substring(comma + 1);
		if (header.endsWith(";base64") && !payload.matches("[A-Za-z0-9+/\\r\\n\\t ]*={0,2}")) {
			throw new IllegalArgumentException("Invalid base64 data");
		}
		byte[] bytes = header.endsWith(";base64") ? Base64.decode(payload, Base64.DEFAULT)
				: URLDecoder.decode(payload.replace("+", "%2B"), "UTF-8").getBytes(StandardCharsets.UTF_8);
		return new DecodedData(type, bytes);
	}

	static String saveFile(Context context, String name, DecodedData export) throws IOException {
		if (name == null || name.trim().isEmpty() || name.contains("/") || name.contains("\\")
				|| name.equals(".") || name.equals("..") || name.indexOf('\0') >= 0 || name.length() > 150) {
			throw new IllegalArgumentException("Invalid filename");
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			return saveShared(context, name, export.type, export.bytes, export.png);
		}
		File directory = context.getExternalFilesDir(export.png ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_DOWNLOADS);
		if (directory == null || (!directory.isDirectory() && !directory.mkdirs())) { throw new IOException("Storage unavailable"); }
		File file = new File(directory, name);
		if (!file.createNewFile()) { throw new IOException("File already exists"); }
		try (OutputStream output = new FileOutputStream(file)) { output.write(export.bytes); }
		catch (IOException | RuntimeException e) { file.delete(); throw e; }
		return file.getAbsolutePath();
	}

	@android.annotation.TargetApi(Build.VERSION_CODES.Q)
	private static String saveShared(Context context, String name, String type, byte[] data, boolean png) throws IOException {
		ContentResolver resolver = context.getContentResolver();
		ContentValues values = new ContentValues();
		values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
		values.put(MediaStore.MediaColumns.MIME_TYPE, type);
		String directory = (png ? Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_DOWNLOADS) + "/PolanieOnLine";
		values.put(MediaStore.MediaColumns.RELATIVE_PATH, directory);
		values.put(MediaStore.MediaColumns.IS_PENDING, 1);
		Uri collection = png ? MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
				: MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
		Uri uri = resolver.insert(collection, values);
		if (uri == null) { throw new IOException("Unable to create download"); }
		try {
			try (OutputStream output = resolver.openOutputStream(uri)) {
				if (output == null) { throw new IOException("Unable to open download"); }
				output.write(data);
			}
			ContentValues published = new ContentValues();
			published.put(MediaStore.MediaColumns.IS_PENDING, 0);
			if (resolver.update(uri, published, null, null) != 1) { throw new IOException("Unable to publish download"); }
		} catch (IOException | RuntimeException e) {
			try { resolver.delete(uri, null, null); }
			catch (RuntimeException cleanupError) { LOG.warn("Unable to remove incomplete download."); }
			throw e;
		}
		return directory + "/" + name;
	}

	public void download(String url, String mimetype) { download(url, mimetype, null); }
	public boolean getResult() { return result; }
	public String getMessage() { return message; }
}
