/*
 ** 2014 January 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command;

import info.ata4.disunity.cli.DisUnityOptions;
import java.io.PrintWriter;

/**
 * Abstract class for command actions.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class Command implements Runnable {
    
    private DisUnityOptions options;
    private PrintWriter out;

    public DisUnityOptions getOptions() {
        return options;
    }

    public void setOptions(DisUnityOptions options) {
        this.options = options;
    }

    public PrintWriter getOutputWriter() {
        return out;
    }

    public void setOutputWriter(PrintWriter out) {
        this.out = out;
    }
}
