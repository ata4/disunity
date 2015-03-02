/*
 ** 2013 August 11
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli;

import com.beust.jcommander.Parameter;

/**
 * DisUnity configuration class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DisUnityOptions {
        
    @Parameter(
        names = {"-h", "--help"},
        description = "Print this help.",
        help = true
    )
    private boolean help;
    
    @Parameter(
        names = { "-v", "--verbose" },
        description = "Show more verbose log output."
    )
    private boolean verbose;

    @Parameter(
        names = { "-f", "--output-format" },
        description = "Set output text format."
    )
    private OutputFormat outputFormat = OutputFormat.PLAINTEXT;
    
    public boolean isHelp() {
        return help;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public OutputFormat getOutputFormat() {
        return outputFormat;
    }
}
