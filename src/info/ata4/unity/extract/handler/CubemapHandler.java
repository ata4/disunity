/*
 ** 2013 July 02
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.extract.handler;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class CubemapHandler extends Texture2DHandler {

    // TODO: configure container header for cubemaps
    
    @Override
    public String getClassName() {
        return "Cubemap";
    }
}
