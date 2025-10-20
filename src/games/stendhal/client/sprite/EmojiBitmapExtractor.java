/***************************************************************************
 *                    Copyright © 2003-2023 - Arianne                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.sprite;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

final class EmojiBitmapExtractor {
	private static final Logger logger = Logger.getLogger(EmojiBitmapExtractor.class);
	private static final int HEADER_SIZE = 8;
	private static final int BITMAP_SIZE_TABLE_LENGTH = 48;
	private static final int PNG_SIGNATURE = 0x89504E47;

	private final FontRenderContext fontRenderContext;
	private final java.awt.Font font;
	private final Map<Integer, GlyphImage> glyphImages;

	EmojiBitmapExtractor(final java.awt.Font font, final FontRenderContext context, final byte[] cblc, final byte[] cbdt) {
		this.font = font;
		fontRenderContext = context;
		glyphImages = new HashMap<>();
		try {
			parseColorBitmaps(cblc, cbdt);
		} catch (Exception e) {
			logger.warn("Unable to parse color emoji tables", e);
		}
	}

	BufferedImage renderGlyph(final String glyph, final float targetSize, final int padding) {
		if ((glyph == null) || glyph.isEmpty()) {
			return null;
		}

		final GlyphVector vector = font.createGlyphVector(fontRenderContext, glyph);
		if (vector.getNumGlyphs() == 0) {
			return null;
		}

		final int glyphCode = vector.getGlyphCode(0);
		final GlyphImage image = glyphImages.get(glyphCode);
		if (image == null) {
			return null;
		}
		return image.scale(targetSize, padding);
	}

	private void parseColorBitmaps(final byte[] cblc, final byte[] cbdt) {
		if ((cblc == null) || (cbdt == null)) {
			return;
		}

		final ByteBuffer cblcBuffer = ByteBuffer.wrap(cblc).order(ByteOrder.BIG_ENDIAN);
		if (cblcBuffer.remaining() < HEADER_SIZE) {
			return;
		}

		cblcBuffer.position(0);
		/* majorVersion */ cblcBuffer.getShort();
		/* minorVersion */ cblcBuffer.getShort();
		final int numSizes = cblcBuffer.getInt();
		int tableOffset = HEADER_SIZE;
		for (int sizeIndex = 0; sizeIndex < numSizes; sizeIndex++) {
			if ((tableOffset + BITMAP_SIZE_TABLE_LENGTH) > cblcBuffer.capacity()) {
				break;
			}
			final int indexSubTableArrayOffset = getInt32(cblcBuffer, tableOffset);
			/* indexTablesSize */
			getInt32(cblcBuffer, tableOffset + 4);
			final int numberOfIndexSubTables = getInt32(cblcBuffer, tableOffset + 8);
			/* colorRef */
			getInt32(cblcBuffer, tableOffset + 12);
			/* skip hori/vert metrics */
			final int startGlyphIndex = getUInt16(cblcBuffer, tableOffset + 36);
			final int endGlyphIndex = getUInt16(cblcBuffer, tableOffset + 38);
			/* skip ppemX */
			final int ppemY = getUInt8(cblcBuffer, tableOffset + 41);
			/* skip bitDepth + flags */

			parseIndexSubTables(cblcBuffer, cbdt, indexSubTableArrayOffset, numberOfIndexSubTables, startGlyphIndex, endGlyphIndex, ppemY);
			tableOffset += BITMAP_SIZE_TABLE_LENGTH;
		}
	}

	private void parseIndexSubTables(final ByteBuffer cblcBuffer, final byte[] cbdt, final int arrayOffset, final int subTableCount,
		final int startGlyphIndex, final int endGlyphIndex, final int ppemY) {
		for (int index = 0; index < subTableCount; index++) {
			final int entryOffset = arrayOffset + (index * 8);
			if ((entryOffset + 8) > cblcBuffer.capacity()) {
				break;
			}
			final int firstGlyph = getUInt16(cblcBuffer, entryOffset);
			final int lastGlyph = getUInt16(cblcBuffer, entryOffset + 2);
			final int additionalOffset = getInt32(cblcBuffer, entryOffset + 4);
			final int subTableOffset = arrayOffset + additionalOffset;
			parseIndexSubTable(cblcBuffer, cbdt, subTableOffset, firstGlyph, lastGlyph, startGlyphIndex, endGlyphIndex, ppemY);
		}
	}

	private void parseIndexSubTable(final ByteBuffer cblcBuffer, final byte[] cbdt, final int subTableOffset,
		final int firstGlyph, final int lastGlyph, final int sizeStartGlyph, final int sizeEndGlyph, final int ppemY) {
		if ((subTableOffset + 8) > cblcBuffer.capacity()) {
			return;
		}
		final int indexFormat = getUInt16(cblcBuffer, subTableOffset);
		final int imageFormat = getUInt16(cblcBuffer, subTableOffset + 2);
		final int imageDataOffset = getInt32(cblcBuffer, subTableOffset + 4);
		if (imageFormat != 17) {
			return;
		}
		final int start = Math.max(firstGlyph, sizeStartGlyph);
		final int end = Math.min(lastGlyph, sizeEndGlyph);
		final int glyphCount = (end - start) + 1;
		if (glyphCount <= 0) {
			return;
		}

		final int[] offsets;
		switch (indexFormat) {
		case 1:
			offsets = readUInt16Array(cblcBuffer, subTableOffset + 8, glyphCount + 1);
			break;
		case 3:
			offsets = readInt32Array(cblcBuffer, subTableOffset + 8, glyphCount + 1);
			break;
		default:
			return;
		}

		for (int glyphIndex = 0; glyphIndex < glyphCount; glyphIndex++) {
			final int offset = offsets[glyphIndex];
			final int nextOffset = offsets[glyphIndex + 1];
			if (offset == nextOffset) {
				continue;
			}
			final int glyphId = start + glyphIndex;
			final int dataOffset = imageDataOffset + offset;
			final int dataLength = nextOffset - offset;
			if ((dataOffset < 0) || ((dataOffset + dataLength) > cbdt.length)) {
				continue;
			}
			final byte[] entry = Arrays.copyOfRange(cbdt, dataOffset, dataOffset + dataLength);
			registerGlyph(glyphId, entry, ppemY);
		}
	}

	private void registerGlyph(final int glyphId, final byte[] entry, final int ppemY) {
		if ((entry == null) || (entry.length < 9)) {
			return;
		}

		final int pngOffset = locatePng(entry);
		if (pngOffset < 0) {
			return;
		}

		final int dataLength = entry.length - pngOffset;
		if (dataLength <= 0) {
			return;
		}

		try {
			final BufferedImage image = ImageIO.read(new ByteArrayInputStream(entry, pngOffset, dataLength));
			if (image == null) {
				return;
			}
			final GlyphImage existing = glyphImages.get(glyphId);
			if ((existing == null) || (ppemY > existing.ppem)) {
				glyphImages.put(glyphId, new GlyphImage(image, ppemY));
			}
		} catch (IOException e) {
			logger.debug("Unable to decode emoji bitmap", e);
		}
	}

	private int locatePng(final byte[] entry) {
		for (int index = 0; index < (entry.length - 3); index++) {
			final int signature = ((entry[index] & 0xFF) << 24)
				| ((entry[index + 1] & 0xFF) << 16)
				| ((entry[index + 2] & 0xFF) << 8)
				| (entry[index + 3] & 0xFF);
			if (signature == PNG_SIGNATURE) {
			return index;
			}
		}
		return -1;
	}

	private int[] readUInt16Array(final ByteBuffer buffer, final int offset, final int length) {
		final int[] values = new int[length];
		for (int index = 0; index < length; index++) {
			values[index] = getUInt16(buffer, offset + (index * 2));
		}
		return values;
	}

	private int[] readInt32Array(final ByteBuffer buffer, final int offset, final int length) {
		final int[] values = new int[length];
		for (int index = 0; index < length; index++) {
			values[index] = getInt32(buffer, offset + (index * 4));
		}
		return values;
	}

	private int getUInt16(final ByteBuffer buffer, final int offset) {
		if ((offset + 2) > buffer.capacity()) {
			return 0;
		}
		return buffer.getShort(offset) & 0xFFFF;
	}

	private int getInt32(final ByteBuffer buffer, final int offset) {
		if ((offset + 4) > buffer.capacity()) {
			return 0;
		}
		return buffer.getInt(offset);
	}

	private int getUInt8(final ByteBuffer buffer, final int offset) {
		if ((offset + 1) > buffer.capacity()) {
			return 0;
		}
		return buffer.get(offset) & 0xFF;
	}

	private static final class GlyphImage {
		private final BufferedImage baseImage;
		private final int ppem;
		private final Map<Integer, BufferedImage> scaledImages;

		private GlyphImage(final BufferedImage image, final int basePpem) {
			baseImage = image;
			ppem = Math.max(1, basePpem);
			scaledImages = new HashMap<>();
		}

		private BufferedImage scale(final float targetSize, final int padding) {
			final int key = ((int) Math.round(targetSize * 100f)) ^ (padding << 24);
			final BufferedImage cached = scaledImages.get(key);
			if (cached != null) {
				return cached;
			}

			final float scale = Math.max(0.01f, targetSize / (float) ppem);
			final int scaledWidth = Math.max(1, Math.round(baseImage.getWidth() * scale));
			final int scaledHeight = Math.max(1, Math.round(baseImage.getHeight() * scale));
			final BufferedImage result = new BufferedImage(scaledWidth + (padding * 2), scaledHeight + (padding * 2), BufferedImage.TYPE_INT_ARGB);
			final Graphics2D graphics = result.createGraphics();
			try {
			graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
			graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphics.drawImage(baseImage, padding, padding, padding + scaledWidth, padding + scaledHeight, 0, 0, baseImage.getWidth(), baseImage.getHeight(), null);
			} finally {
			graphics.dispose();
			}
			scaledImages.put(key, result);
			return result;
		}
	}
}
