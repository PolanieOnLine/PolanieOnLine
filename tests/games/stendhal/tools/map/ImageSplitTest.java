/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.tools.map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ImageSplitTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void startsAtTheFirstSourcePixel() throws IOException {
        File source = temporaryFolder.newFile("world.png");
        File tiles = temporaryFolder.newFolder("map");
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, Color.RED.getRGB());
        image.setRGB(2, 0, Color.GREEN.getRGB());
        image.setRGB(0, 2, Color.BLUE.getRGB());
        ImageIO.write(image, "png", source);

        new ImageSplit(source.getPath(), tiles.getPath(), "5-", 2, 2, 0, 0, 0).split();

        assertEquals(Color.RED.getRGB(), ImageIO.read(new File(tiles, "5-0-0.png")).getRGB(0, 0));
        assertEquals(Color.GREEN.getRGB(), ImageIO.read(new File(tiles, "5-1-0.png")).getRGB(0, 0));
        assertEquals(Color.BLUE.getRGB(), ImageIO.read(new File(tiles, "5-0-1.png")).getRGB(0, 0));
    }

    @Test
    public void doesNotCreateExtraTilesAfterSourceOffset() throws IOException {
        File source = temporaryFolder.newFile("padded.png");
        File tiles = temporaryFolder.newFolder("padded-map");
        BufferedImage image = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        image.setRGB(2, 2, Color.RED.getRGB());
        ImageIO.write(image, "png", source);

        new ImageSplit(source.getPath(), tiles.getPath(), "5-", 2, 2, 0, 0, 2).split();

        assertTrue(new File(tiles, "5-0-0.png").isFile());
        assertEquals(Color.RED.getRGB(), ImageIO.read(new File(tiles, "5-0-0.png")).getRGB(0, 0));
        assertFalse(new File(tiles, "5-1-0.png").exists());
        assertFalse(new File(tiles, "5-0-1.png").exists());
    }
}
