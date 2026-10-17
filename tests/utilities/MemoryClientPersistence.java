package utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import marauroa.common.io.Persistence;

/** Isolates client preference writes from the user's actual profile. */
public final class MemoryClientPersistence extends Persistence implements AutoCloseable {
	private final Field singleton;
	private final Object previous;
	private final Map<String, byte[]> files = new HashMap<String, byte[]>();
	private byte[] savedPreferences;

	public MemoryClientPersistence() throws Exception {
		singleton = Persistence.class.getDeclaredField("instance");
		singleton.setAccessible(true);
		previous = singleton.get(null);
		singleton.set(null, this);
	}

	@Override public InputStream getInputStream(boolean sandbox, String folder, String filename) throws IOException {
		byte[] bytes = files.get(folder + filename);
		if (bytes == null) throw new FileNotFoundException(filename);
		return new ByteArrayInputStream(bytes);
	}

	@Override public OutputStream getOutputStream(boolean sandbox, final String folder, final String filename) {
		return new ByteArrayOutputStream() {
			@Override public void close() throws IOException {
				super.close();
				files.put(folder + filename, toByteArray());
				if ("windows.properties".equals(filename)) savedPreferences = toByteArray();
			}
		};
	}

	public Properties savedPreferences() throws IOException {
		Properties result = new Properties();
		if (savedPreferences != null) result.load(new ByteArrayInputStream(savedPreferences));
		return result;
	}

	@Override public void close() throws Exception { singleton.set(null, previous); }
}
