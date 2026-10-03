package eu.polanieonline.client;

import static org.junit.Assert.*;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29, manifest = Config.NONE)
public class DownloadHandlerTest {
	private Context context;
	private RecordingProvider provider;
	private static final Uri CREATED = Uri.parse("content://media/external_primary/downloads/1");

	@Before public void setUp() {
		context = RuntimeEnvironment.getApplication();
		provider = new RecordingProvider();
		ShadowContentResolver.registerProviderInternal("media", provider);
	}

	@Test public void textPreservesCommasPlusAndPolishLetters() throws Exception {
		DownloadHandler.DecodedData data = DownloadHandler.decodeDataUrl("data:text/plain;charset=utf-8,raz,dwa+%C5%BC%0A");
		assertEquals("text/plain", data.type);
		assertEquals("raz,dwa+ż\n", new String(data.bytes, StandardCharsets.UTF_8));
		assertFalse(data.png);
	}

	@Test public void base64WorksForBothExports() throws Exception {
		assertArrayEquals(new byte[] {1, 2, 3}, DownloadHandler.decodeDataUrl("data:image/png;base64,AQID").bytes);
		assertEquals("hello", new String(DownloadHandler.decodeDataUrl("data:text/plain;base64,aGVsbG8=").bytes,
				StandardCharsets.UTF_8));
	}

	@Test public void rejectsUnsupportedAndMalformedData() {
		for (String url : new String[] {null, "https://example.com/file", "data:text/plain", "data:text/html,<script>",
				"data:image/png,abc", "data:text/plain,%zz", "data:text/plain;base64,!"}) {
			assertThrows(IllegalArgumentException.class, () -> DownloadHandler.decodeDataUrl(url));
		}
	}

	@Test public void rejectsOversizedDownloads() {
		assertThrows(IllegalArgumentException.class,
				() -> DownloadHandler.decodeDataUrl("data:text/plain," + "x".repeat(24 * 1024 * 1024)));
	}

	@Test public void textIsPublishedOnlyAfterWriting() throws Exception {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		Shadows.shadowOf(context.getContentResolver()).registerOutputStream(CREATED, output);
		String path = DownloadHandler.saveFile(context, "rozmowa.txt", DownloadHandler.decodeDataUrl("data:text/plain,hello"));
		assertEquals("Download/PolanieOnLine/rozmowa.txt", path);
		assertEquals(MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), provider.collection);
		assertEquals("Download/PolanieOnLine", provider.inserted.getAsString(MediaStore.MediaColumns.RELATIVE_PATH));
		assertEquals(Integer.valueOf(1), provider.inserted.getAsInteger(MediaStore.MediaColumns.IS_PENDING));
		assertEquals(Integer.valueOf(0), provider.updated.getAsInteger(MediaStore.MediaColumns.IS_PENDING));
		assertEquals("hello", output.toString("UTF-8"));
		assertEquals(0, provider.deleted);
	}

	@Test public void pngGoesToPicturesCollection() throws Exception {
		Shadows.shadowOf(context.getContentResolver()).registerOutputStream(CREATED, new ByteArrayOutputStream());
		assertEquals("Pictures/PolanieOnLine/zrzut.png", DownloadHandler.saveFile(context, "zrzut.png",
				DownloadHandler.decodeDataUrl("data:image/png;base64,AQID")));
		assertEquals(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), provider.collection);
		assertEquals("image/png", provider.inserted.getAsString(MediaStore.MediaColumns.MIME_TYPE));
	}

	@Test public void failedWriteRemovesPendingRow() throws Exception {
		Shadows.shadowOf(context.getContentResolver()).registerOutputStream(CREATED, new OutputStream() {
			@Override public void write(int value) throws IOException { throw new IOException("Storage full"); }
		});
		assertThrows(IOException.class, () -> DownloadHandler.saveFile(context, "failed.txt",
				DownloadHandler.decodeDataUrl("data:text/plain,hello")));
		assertEquals(1, provider.deleted);
		assertNull(provider.updated);
	}

	@Test public void failedPublishRemovesPendingRow() throws Exception {
		provider.updateResult = 0;
		Shadows.shadowOf(context.getContentResolver()).registerOutputStream(CREATED, new ByteArrayOutputStream());
		assertThrows(IOException.class, () -> DownloadHandler.saveFile(context, "failed.txt",
				DownloadHandler.decodeDataUrl("data:text/plain,hello")));
		assertEquals(1, provider.deleted);
	}

	@Test public void refusesPathsBeforeCreatingAnyFile() throws Exception {
		DownloadHandler.DecodedData data = DownloadHandler.decodeDataUrl("data:text/plain,hello");
		for (String name : new String[] {null, "", "../secret.txt", "a/b.txt", "a\\b.txt", ".", "..", "a\0b"}) {
			assertThrows(IllegalArgumentException.class, () -> DownloadHandler.saveFile(context, name, data));
		}
		assertNull(provider.inserted);
	}

	@Test @Config(sdk = 28) public void olderAndroidUsesAppDirectoryWithoutOverwriting() throws Exception {
		DownloadHandler.DecodedData data = DownloadHandler.decodeDataUrl("data:text/plain,hello");
		File file = new File(DownloadHandler.saveFile(context, "legacy.txt", data));
		assertEquals(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), file.getParentFile());
		assertEquals("hello", new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
		assertThrows(IOException.class, () -> DownloadHandler.saveFile(context, "legacy.txt", data));
		assertEquals("hello", new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
		assertNull(provider.inserted);
	}

	private static class RecordingProvider extends ContentProvider {
		Uri collection;
		ContentValues inserted;
		ContentValues updated;
		int deleted;
		int updateResult = 1;
		@Override public boolean onCreate() { return true; }
		@Override public Uri insert(Uri uri, ContentValues values) {
			collection = uri; inserted = new ContentValues(values); return CREATED;
		}
		@Override public int update(Uri uri, ContentValues values, String selection, String[] args) {
			updated = new ContentValues(values); return updateResult;
		}
		@Override public int delete(Uri uri, String selection, String[] args) { deleted++; return 1; }
		@Override public String getType(Uri uri) { return "text/plain"; }
		@Override public Cursor query(Uri uri, String[] projection, String selection, String[] args, String order) { return null; }
	}
}
