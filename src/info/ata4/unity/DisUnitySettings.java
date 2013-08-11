/*
 ** 2013 August 11
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity;

import info.ata4.unity.DisUnity.Command;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnitySettings {
    
    private List<File> files = new ArrayList<>();
    private Command command = Command.EXTRACT;
    private Set<Integer> classFilter;
    
    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command cmd) {
        this.command = cmd;
    }
    
    public Set<Integer> getClassFilter() {
        return classFilter;
    }
    
    public void setClassFilter(Set<Integer> classFilter) {
        this.classFilter = classFilter;
    }
    
    public boolean isClassFiltered(Integer classID) {
        return classFilter != null && classFilter.contains(classID);
    }
}
