/*
 ** 2014 September 25
 **
 ** The author disclaims copyright to this source code. In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.assetbundle;

import info.ata4.io.DataReader;
import info.ata4.io.DataReaders;
import info.ata4.io.buffer.ByteBufferChannel;
import info.ata4.io.buffer.ByteBufferOutputStream;
import info.ata4.io.util.PathUtils;
import info.ata4.unity.util.UnityVersion;
import info.ata4.util.progress.DummyProgress;
import info.ata4.util.progress.Progress;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardOpenOption.*;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONObject2;
import org.json.JSONTokener;

/**
 * Asset bundle file utility class.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class AssetBundleUtils {
    
    private static final Charset PROP_CHARSET = Charset.forName("US-ASCII");
    
    private AssetBundleUtils() {
    }
    
    public static boolean isAssetBundle(Path file) {
        if (!Files.isRegularFile(file)) {
            return false;
        }
        
        try (InputStream is = Files.newInputStream(file)) {
            byte[] header = new byte[8];
            is.read(header);
            String headerString = new String(header, PROP_CHARSET);
            return headerString.equals(AssetBundleHeader.SIGNATURE_WEB)
                    || headerString.equals(AssetBundleHeader.SIGNATURE_RAW);
        } catch (IOException ex) {
        }
        
        return false;
    }
    
    public static void extract(Path file, Path outDir, Progress progress) throws IOException {
        try(
            AssetBundleReader assetBundle = new AssetBundleReader(file)
        ) {
            List<AssetBundleEntry> entries = assetBundle.entries();
            progress.setLimit(entries.size());

            for (int i = 0; i < entries.size(); i++) {
                if (progress.isCanceled()) {
                    break;
                }
                
                AssetBundleEntry entry = entries.get(i);
                progress.setLabel(entry.name());
                
                Path entryFile = outDir.resolve(entry.name());
                Files.createDirectories(entryFile.getParent());
                Files.copy(entry.inputStream(), entryFile, REPLACE_EXISTING);
                
                progress.update(i + 1);
            }
            
            String bundleName = outDir.getFileName().toString();
            Path propsFile = outDir.getParent().resolve(bundleName + ".json");
            
            writePropertiesFile(propsFile, assetBundle);
        }
    }
    
    public static void extract(Path file, Path outDir) throws IOException {
        extract(file, outDir, new DummyProgress());
    }
    
    public static void build(Path propsFile, Path bundleFile) throws IOException {
        AssetBundleWriter assetBundle = new AssetBundleWriter();
        readPropertiesFile(propsFile, assetBundle);
        assetBundle.write(bundleFile);
    }
    
    public static SeekableByteChannel byteChannelForEntry(AssetBundleEntry entry) throws IOException {
        SeekableByteChannel chan;
        
        // check if the entry is larger than 128 MiB
        long size = entry.size();
        if (size > 1 << 27) {
            // copy entry to temporary file
            Path tmpFile = Files.createTempFile("disunity", null);
            Files.copy(entry.inputStream(), tmpFile, REPLACE_EXISTING);
            chan = Files.newByteChannel(tmpFile, READ, DELETE_ON_CLOSE);
        } else {
            // copy entry to memory
            ByteBuffer bb = ByteBuffer.allocateDirect((int) size);
            IOUtils.copy(entry.inputStream(), new ByteBufferOutputStream(bb));
            bb.flip();
            chan = new ByteBufferChannel(bb);
        }
        
        return chan;
    }
    
    public static DataReader dataReaderForEntry(AssetBundleEntry entry) throws IOException {
        return DataReaders.forSeekableByteChannel(AssetBundleUtils.byteChannelForEntry(entry));
    }
    
    private static void writePropertiesFile(Path propsFile, AssetBundleReader assetBundle) throws IOException {
        AssetBundleHeader header = assetBundle.header();

        JSONObject props = new JSONObject2();
        props.put("compressed", header.compressed());
        props.put("streamVersion", header.streamVersion());
        props.put("unityVersion", header.unityVersion().toString());
        props.put("unityRevision", header.unityRevision().toString());

        JSONArray files = new JSONArray();
        for (AssetBundleEntry entry : assetBundle) {
            files.put(entry.name());
        }
        props.put("files", files);

        try (Writer out = Files.newBufferedWriter(propsFile,
                PROP_CHARSET, WRITE, CREATE, TRUNCATE_EXISTING)) {
            props.write(out);
        }
    }
    
    private static void readPropertiesFile(Path propsFile, AssetBundleWriter assetBundle) throws IOException {
        JSONObject props;
        
        try (Reader in = Files.newBufferedReader(propsFile, PROP_CHARSET)) {
            props = new JSONObject(new JSONTokener(in));
        }
        
        AssetBundleHeader header = assetBundle.getHeader();
        
        header.compressed(props.getBoolean("compressed"));
        header.streamVersion(props.getInt("streamVersion"));
        header.unityVersion(new UnityVersion(props.getString("unityVersion")));
        header.unityRevision(new UnityVersion(props.getString("unityRevision")));
        
        JSONArray files = props.getJSONArray("files");
        
        String bundleName = PathUtils.getBaseName(propsFile);
        Path bundleDir = propsFile.resolveSibling(bundleName);
        
        for (int i = 0; i < files.length(); i++) {
            String name = files.getString(i);
            Path file = bundleDir.resolve(name);
            assetBundle.addEntry(new AssetBundleExternalEntry(name, file));
        }
    }
}
