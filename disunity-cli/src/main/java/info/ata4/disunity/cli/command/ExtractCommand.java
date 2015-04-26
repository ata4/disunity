/*
 ** 2014 December 26
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command;

import com.beust.jcommander.Parameters;
import info.ata4.disunity.extract.AssetExtractor;
import info.ata4.disunity.extract.AudioClipExtractor;
import info.ata4.disunity.extract.FontExtractor;
import info.ata4.disunity.extract.MovieTextureExtractor;
import info.ata4.disunity.extract.ShaderExtractor;
import info.ata4.disunity.extract.TextAssetExtractor;
import info.ata4.io.util.PathUtils;
import info.ata4.unity.asset.AssetFile;
import info.ata4.unity.rtti.ObjectData;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
@Parameters(
    commandNames = "extract",
    commandDescription = "Test"
)
public class ExtractCommand extends AssetFileCommand {
    
    private final List<AssetExtractor> extractors = new ArrayList<>();
    
    public ExtractCommand() {
        extractors.add(new TextAssetExtractor());
        extractors.add(new ShaderExtractor());
        extractors.add(new AudioClipExtractor());
        extractors.add(new MovieTextureExtractor());
        extractors.add(new FontExtractor());
    }

    @Override
    public void handleAssetFile(AssetFile asset) throws IOException {
        Path outputDir = PathUtils.removeExtension(getCurrentFile());
        
        // if getCurrentFile() has no extension, add a "_" to the path so a new
        // directory with that name won't collide with the file
        if (Files.isRegularFile(outputDir)) {
            outputDir = PathUtils.append(outputDir, "_");
        }
        
        // create directory
        if (Files.notExists(outputDir)) {
            Files.createDirectory(outputDir);
        }
        
        // apply output directory to extractors
        for (AssetExtractor extractor : extractors) {
            extractor.setOutputDirectory(outputDir);
        }
        
        // scan objects and extract files
        for (ObjectData objectData : asset.objects()) {
            for (AssetExtractor extractor : extractors) {
                if (extractor.isEligible(objectData)) {
                    extractor.extract(objectData);
                }
            }
        }
        
        // if no files were extracted, delete the empty directory
        if (PathUtils.isDirectoryEmpty(outputDir)) {
            Files.delete(outputDir);
        }
    }
    
}
