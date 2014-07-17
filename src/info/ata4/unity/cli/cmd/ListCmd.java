/*
 ** 2014 January 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.cmd;

import com.beust.jcommander.Parameters;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.asset.struct.ObjectPath;
import info.ata4.unity.cli.extract.AssetExtractor;
import info.ata4.unity.util.ClassID;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "list",
    commandDescription = "Lists all objects inside asset files."
)
public class ListCmd extends AssetCommand {
    
    private final PrintStream ps;
    
    public ListCmd(PrintStream ps) {
        this.ps = ps;
    }
    
    @Override
    protected void processAsset(AssetFile asset) throws IOException {
        List<ObjectPath> paths = asset.getPaths();

        // dirty hardcoded table printer
        int p1 = 12;
        int p2 = 4;
        int p3 = 24;
        int p4 = 10;
        int p5 = 10;
        int p6 = 11;
        
        ps.print(StringUtils.rightPad("PID", p1));
        ps.print(" | ");
        ps.print(StringUtils.rightPad("CID", p2));
        ps.print(" | ");
        ps.print(StringUtils.rightPad("Class name", p3));
        ps.print(" | ");
        ps.print(StringUtils.rightPad("Offset", p4));
        ps.print(" | ");
        ps.print(StringUtils.leftPad("Size", p5));
        ps.print(" | ");
        ps.print(StringUtils.rightPad("Object name", p6));
        ps.println();
        
        ps.print(StringUtils.repeat("-", p1));
        ps.print(" | ");
        ps.print(StringUtils.repeat("-", p2));
        ps.print(" | ");
        ps.print(StringUtils.repeat("-", p3));
        ps.print(" | ");
        ps.print(StringUtils.repeat("-", p4));
        ps.print(" | ");
        ps.print(StringUtils.repeat("-", p5));
        ps.print(" | ");
        ps.print(StringUtils.repeat("-", p6));
        ps.println();
        
        for (ObjectPath path : paths) {
            if (path.isScript()) {
                continue;
            }
            
            String name = AssetExtractor.getObjectName(asset, path);
            
            if (name == null) {
                name = "";
            }
            
            ps.print(StringUtils.rightPad(String.valueOf(path.getPathID()), p1));
            ps.print(" | ");
            ps.print(StringUtils.rightPad(String.valueOf(path.getClassID()), p2));
            ps.print(" | ");
            ps.print(StringUtils.rightPad(ClassID.getNameForID(path.getClassID(), true), p3));
            ps.print(" | ");
            ps.print(StringUtils.rightPad(String.format("0x%x", path.getOffset()), p4));
            ps.print(" | ");
            ps.print(StringUtils.leftPad(String.valueOf(path.getLength()), p5));
            ps.print(" | ");
            ps.print(name);
            ps.println();
        }
    }
}
