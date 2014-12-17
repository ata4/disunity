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

import info.ata4.io.buffer.ByteBufferOutputStream;
import info.ata4.io.socket.IOSocket;
import info.ata4.io.socket.Sockets;
import info.ata4.io.util.PathUtils;
import info.ata4.unity.util.UnityVersion;
import info.ata4.util.progress.DummyProgress;
import info.ata4.util.progress.Progress;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardOpenOption.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
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
            String headerString = new String(header, "ASCII");
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
            long current = 0;
            long total = 0;
            for (AssetBundleEntry entry : assetBundle) {
                total += entry.getSize();
            }
            
            progress.setLimit(total);

            for (AssetBundleEntry entry : assetBundle) {
                if (progress.isCanceled()) {
                    break;
                }
                
                progress.setLabel(entry.getName());
                
                Path entryFile = outDir.resolve(entry.getName());
                Files.createDirectories(entryFile.getParent());
                Files.copy(entry.getInputStream(), entryFile, REPLACE_EXISTING);
                
                current += entry.getSize();
                progress.update(current);
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
    
    public static IOSocket getSocketForEntry(AssetBundleEntry entry) throws IOException {
        IOSocket socket;
        
        // check if the entry is larger than 128 MiB
        long size = entry.getSize();
        if (size > 1 << 27) {
            // copy entry to temporary file
            Path tmpFile = Files.createTempFile("disunity", null);
            socket = Sockets.forFile(tmpFile, READ, WRITE, DELETE_ON_CLOSE);
            IOUtils.copy(entry.getInputStream(), socket.getOutputStream());
            socket.getPositionable().position(0);
        } else {
            // copy entry to memory
            ByteBuffer bb = ByteBuffer.allocateDirect((int) size);
            IOUtils.copy(entry.getInputStream(), new ByteBufferOutputStream(bb));
            bb.flip();
            socket = Sockets.forByteBuffer(bb);
        }
        
        return socket;
    }
    
    private static void writePropertiesFile(Path propsFile, AssetBundleReader assetBundle) throws IOException {
        AssetBundleHeader header = assetBundle.getHeader();

        JSONObject props = new JSONObject();
        props.put("compressed", header.isCompressed());
        props.put("streamVersion", header.getStreamVersion());
        props.put("unityVersion", header.getUnityVersion().toString());
        props.put("unityRevision", header.getUnityRevision().toString());

        JSONArray files = new JSONArray();
        for (AssetBundleEntry entry : assetBundle) {
            files.put(entry.getName());
        }
        props.put("files", files);

        try (Writer out = Files.newBufferedWriter(propsFile,
                PROP_CHARSET, WRITE, CREATE, TRUNCATE_EXISTING)) {
            props.write(out, 2);
        }
    }
    
    private static void readPropertiesFile(Path propsFile, AssetBundleWriter assetBundle) throws IOException {
        JSONObject props;
        
        try (Reader in = Files.newBufferedReader(propsFile, PROP_CHARSET)) {
            props = new JSONObject(new JSONTokener(in));
        }
        
        AssetBundleHeader header = assetBundle.getHeader();
        
        header.setCompressed(props.getBoolean("compressed"));
        header.setStreamVersion(props.getInt("streamVersion"));
        header.setUnityVersion(new UnityVersion(props.getString("unityVersion")));
        header.setUnityRevision(new UnityVersion(props.getString("unityRevision")));
        
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
