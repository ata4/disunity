/*
 ** 2013 June 16
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.extract;

import info.ata4.io.buffer.ByteBufferUtils;
import info.ata4.log.LogUtils;
import info.ata4.unity.engine.SubstanceArchive;
import info.ata4.unity.rtti.ObjectData;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class SubstanceArchiveHandler extends AbstractObjectExtractor {
    
    private static final Logger L = LogUtils.getLogger();
    
    public SubstanceArchiveHandler() {
        super("SubstanceArchive");
    }

    @Override
    public void process(ObjectData object) throws Exception {
        SubstanceArchive subarc = new SubstanceArchive(object.getInstance());
        String name = subarc.getName();
        ByteBuffer packageData = subarc.getPackageData();
        
        if (ByteBufferUtils.isEmpty(packageData)) {
            L.log(Level.WARNING, "Substance archive {0} is empty", name);
            return;
        }
 
        files.add(new MutableFileHandle(name, "sbsar", packageData));
    }
}
