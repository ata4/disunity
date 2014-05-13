/*
 ** 2013 August 11
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli;

import info.ata4.unity.cli.classfilter.ClassFilter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * DisUnity configuration class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnityOptions {
    
    private List<Path> files = new ArrayList<>();
    private ClassFilter classFilter;
    private String command = "extract";
    
    public List<Path> getFiles() {
        return files;
    }
    
    public ClassFilter getClassFilter() {
        return classFilter;
    }

    public void setClassFilter(ClassFilter classFilter) {
        this.classFilter = classFilter;
    }
    
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
