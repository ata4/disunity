/*
 ** 2014 January 09
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.action;

import java.io.PrintStream;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class PrintAction extends Action {
    
    protected final PrintStream ps;
    
    public PrintAction(PrintStream ps) {
        this.ps = ps;
    }
}
