/*
 ** 2014 December 18
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command;

import com.beust.jcommander.Parameters;
import info.ata4.disunity.gui.DisUnityGui;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "gui",
    commandDescription = "Opens the experimental graphical user interface."
)
public class GuiCommand extends Command {

    @Override
    public void run() {
        DisUnityGui.main(new String[] {});
    }
}
