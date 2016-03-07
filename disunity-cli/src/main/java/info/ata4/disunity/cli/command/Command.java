/*
 ** 2015 November 27
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command;

import com.beust.jcommander.JCommander;
import info.ata4.disunity.DisUnity;
import info.ata4.junity.progress.Progress;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * Abstract class for command actions.
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public abstract class Command implements Runnable {

    private JCommander commander;
    private PrintWriter out;

    protected final Progress progress = (s, p) -> {
        if (s.isPresent()) {
            output().println(s.get());
        }
    };

    public void init(JCommander commander, PrintWriter out) {
        this.commander = Objects.requireNonNull(commander);
        this.out = Objects.requireNonNull(out);
    }

    public JCommander commander() {
        return commander;
    }

    @Override
    public void run() {
        String commandName = commander.getParsedCommand();
        if (!commander.getCommands().isEmpty()) {
            JCommander command = commander.getCommands().get(commandName);
            if (command != null && !command.getObjects().isEmpty()) {
                Command commandObj = (Command) command.getObjects().get(0);
                commandObj.run();
            } else {
                // no command selected, show usage
                usage();
            }
        }
    }

    protected JCommander addSubCommand(String commandName, Command commandObj) {
        commander.addCommand(commandName, commandObj);
        JCommander subCommander = commander.getCommands().get(commandName);
        commandObj.init(subCommander, out);
        return subCommander;
    }

    protected void usage() {
        output().println(DisUnity.getSignature());
        commander().usage();
    }

    protected PrintWriter output() {
        return out;
    }
}
