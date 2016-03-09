/*
 ** 2015 December 22
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli;

import com.beust.jcommander.Parameter;
import java.util.function.Supplier;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class OutputFormatDelegate implements Supplier<OutputFormat> {

    @Parameter(
        names = { "-f", "--output-format" },
        description = "Set output text format."
    )
    private OutputFormat outputFormat = OutputFormat.TEXT;

    @Override
    public OutputFormat get() {
        return outputFormat;
    }
}
