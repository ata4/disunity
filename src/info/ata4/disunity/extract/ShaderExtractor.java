/*
 ** 2014 December 31
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.extract;

import info.ata4.unity.util.UnityClass;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class ShaderExtractor extends TextAssetExtractor {

    @Override
    public UnityClass getUnityClass() {
        return new UnityClass("Shader");
    }
}
