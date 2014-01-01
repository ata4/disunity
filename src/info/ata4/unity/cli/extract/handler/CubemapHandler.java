/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract.handler;

import info.ata4.util.io.image.tga.TGAHeader;

import java.io.IOException;
import java.util.logging.Logger;

public class CubemapHandler extends Texture2DHandler {
    
    private static final Logger L = Logger.getLogger(CubemapHandler.class.getName());

	@Override
	protected void extractTGAImages(TGAHeader header, boolean mipMap, int mipMapCount) throws IOException {
		int imageTextureCount = obj.getValue("m_ImageCount");

		for (int cubeTexture = 0; cubeTexture < imageTextureCount; cubeTexture++){
			TGAHeader headerForMipMap = header.clone();
			for (int i = 0; i < mipMapCount; i++) {
				extractTGAImage(headerForMipMap, mipMap, cubeTexture + "_" +i);
			}

		}

	}

}
