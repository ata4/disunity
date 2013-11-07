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
    private DisUnityCommand command = DisUnityCommand.EXTRACT;
    private Set<Integer> classFilter;
    
    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public DisUnityCommand getCommand() {
        return command;
    }

    public void setCommand(DisUnityCommand cmd) {
        this.command = cmd;
    }
    
    public Set<Integer> getClassFilter() {
        return classFilter;
    }
    
    public void setClassFilter(Set<Integer> classFilter) {
        this.classFilter = classFilter;
    }
    
    public boolean isClassFiltered(Integer classID) {
        return classFilter != null && !classFilter.contains(classID);
    }
}
