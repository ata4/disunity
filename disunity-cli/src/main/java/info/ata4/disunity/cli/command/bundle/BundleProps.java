/*
 ** 2015 December 06
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.disunity.cli.command.bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import info.ata4.io.util.PathUtils;
import info.ata4.junity.UnityVersion;
import info.ata4.junity.bundle.Bundle;
import info.ata4.junity.bundle.BundleExternalEntry;
import info.ata4.junity.bundle.BundleHeader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
class BundleProps {

    private static final Charset CHARSET = Charset.forName("US-ASCII");

    boolean compressed;
    int streamVersion;
    String unityVersion;
    String unityRevision;
    List<String> files;

    static void write(Path propsFile, Bundle bundle) throws IOException {
        BundleProps props = new BundleProps();
        BundleHeader header = bundle.header();
        props.compressed = header.compressed();
        props.streamVersion = header.streamVersion();
        props.unityVersion = header.unityVersion().toString();
        props.unityRevision = header.unityRevision().toString();

        props.files = bundle.entryInfos().stream()
            .map(entry -> entry.name())
            .collect(Collectors.toList());

        try (Writer writer = Files.newBufferedWriter(propsFile,
                CHARSET, WRITE, CREATE, TRUNCATE_EXISTING)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(props, writer);
        }
    }

    static void read(Path propsFile, Bundle bundle) throws IOException {
        BundleProps props;

        try (Reader reader = Files.newBufferedReader(propsFile, CHARSET)) {
            props = new Gson().fromJson(reader, BundleProps.class);
        }

        BundleHeader header = bundle.header();
        header.compressed(props.compressed);
        header.streamVersion(props.streamVersion);
        header.unityVersion(new UnityVersion(props.unityVersion));
        header.unityRevision(new UnityVersion(props.unityRevision));

        String bundleName = PathUtils.getBaseName(propsFile);
        Path bundleDir = propsFile.resolveSibling(bundleName);

        props.files.stream().map(bundleDir::resolve).forEach(file -> {
            bundle.entries().add(new BundleExternalEntry(file));
        });
    }
}
