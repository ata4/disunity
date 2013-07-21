/*
 ** 2013 June 29
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.asset;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * File filter for Unity asset files.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
    
public class AssetFileFilter implements FilenameFilter {

    private static final Pattern ASSET_PATTERN = Pattern.compile("^CAB-[0-9a-f]{32}$|\\.(shared)?asset(s)?$", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean accept(File dir, String name) {
        return ASSET_PATTERN.matcher(name).find();
    }

}