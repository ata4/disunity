/*
 ** 2013 December 08
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.cli.tools;

import info.ata4.log.LogUtils;
import info.ata4.unity.asset.bundle.AssetBundle;
import info.ata4.unity.cli.DisUnityOptions;
import info.ata4.util.string.StringUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * Semiautomatic bot to download Unity web games and to place them in an organized
 * directory structure.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class WebDownloader {
    
    private static final Logger L = LogUtils.getLogger();
    private static final Pattern UNITY3D_FILE_PATTERN = Pattern.compile("\"([^\"]+.unity3d)\"");
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:26.0) Gecko/20100101 Firefox/26.0";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LogUtils.configure();
        
        if (args.length < 1) {
            L.info("Usage: webdownloader <url list file>");
            return;
        }
        
        Path urlFile = Paths.get(args[0]);
        
        WebDownloader downloader = new WebDownloader();
        downloader.setBaseDirectory(urlFile.getParent());

        List<String> lines;

        try {
            lines = Files.readAllLines(urlFile, Charset.forName("UTF-8"));
        } catch (IOException ex) {
            L.log(Level.SEVERE, "Can't read URL list", ex);
            return;
        }

        for (String line : lines) {
            try {
                downloader.download(line);
            } catch (Exception ex) {
                L.log(Level.WARNING, "Can't download from " + line, ex);
            }
        }
        
        // learn structs
        List<Path> bundles = downloader.getDownloadedFiles();
        boolean learn = true;
        if (learn && !bundles.isEmpty()) {
            DisUnityOptions opts = new DisUnityOptions();
            
//            opts.setCommand("learn");
//            opts.getFiles().addAll(bundles);
            
//            DisUnityProcessor processor = new DisUnityProcessor(opts);
//            processor.run();
        }
    }
    
    private Path baseDir = Paths.get(".");
    private final List<Path> bundles = new ArrayList<>();
    
    public void setBaseDirectory(Path baseDir) {
        this.baseDir = baseDir;
    }
    
    public Path getBaseDirectory() {
        return baseDir;
    }
    
    public List<Path> getDownloadedFiles() {
        return Collections.unmodifiableList(bundles);
    }

    public void download(String urlString) throws MalformedURLException, IOException {
        URL url = new URL(urlString);
        URL urlParent = new URL(FilenameUtils.getPath(urlString));
        
        List<String> lines;
        
        try (InputStream is = getHTTPInputStream(url)) {
            lines = IOUtils.readLines(is);
        }
        
        // search for *.unity3d urls
        Set<String> unityFiles = new HashSet<>();
        for (String line : lines) {
            Matcher m = UNITY3D_FILE_PATTERN.matcher(line);
            if (m.find()) {
                unityFiles.add(m.group(1));
            }
        }
        
        if (unityFiles.isEmpty()) {
            L.info("No downloadable asset bundles found");
        }
        
        // download all found bundles
        for (String unityFile : unityFiles) {
            URL unityUrl;

            if (unityFile.startsWith("http")) {
                unityUrl = new URL(unityFile);
            } else {
                // use whitespace encoding
                String unityFileFixed = unityFile.replace(" ", "%20");
                
                unityUrl = new URL(urlParent, unityFileFixed);
            }
            
            try {
                downloadBundle(url, unityUrl);
            } catch (IOException ex) {
                L.log(Level.WARNING, "Can't download bundle " + unityUrl, ex);
            }
        }
    }

    private void downloadBundle(URL indexUrl, URL unityUrl) throws IOException {
        // create temp dir
        Path tmpDir = baseDir.resolve("tmp");
        if (Files.notExists(tmpDir)) {
            Files.createDirectory(tmpDir);
        }
        
        // download to temp file
        Path tmpFile = tmpDir.resolve(UUID.randomUUID().toString() + ".unity3d");
        
        try (InputStream is = getHTTPInputStream(unityUrl)) {
            Files.copy(is, tmpFile);
        }
        
        // load bundle for validation and to get revision
        AssetBundle ab = new AssetBundle();
        ab.load(tmpFile);
        
        // create version sub-directory
        String version = ab.getEngineVersion().toString();
        String versionDirName = version.substring(0, 3);
        
        L.log(Level.INFO, "Revision: {0}", version);
        
        Path versionDir = baseDir.resolve(versionDirName);
        if (Files.notExists(versionDir)) {
            Files.createDirectory(versionDir);
        }
        
        // create game sub-directory
        String subDirName = unityUrl.getHost();
        
        Path subDir = versionDir.resolve(subDirName);
        if (Files.notExists(subDir)) {
            Files.createDirectory(subDir);
        }
        
        // create file name
        String fileName = FilenameUtils.getName(unityUrl.getFile());
        
        // fix whitespace encoding
        fileName = fileName.replace("%20", " ");
        
        Path finalFile = subDir.resolve(fileName);
        
        // move to final directory
        Files.move(tmpFile, finalFile, StandardCopyOption.REPLACE_EXISTING);
        
        bundles.add(finalFile);
        
        // write source urls to text file
        String bundleName = FilenameUtils.removeExtension(fileName);
        Path sourceUrlFile = subDir.resolve(bundleName + ".txt");
        String sourceString = String.format("Bundle: %s\nIndex: %s",
                unityUrl.toString(), indexUrl.toString());
        FileUtils.writeStringToFile(sourceUrlFile.toFile(), sourceString);
    }
    
    private InputStream getHTTPInputStream(URL url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        
        // use HTTP GET
        con.setRequestMethod("GET");
        
        // give up after 10 seconds
        con.setReadTimeout(10000);
        
        // it's not gentlemanly, but better choose a popular user agent for
        // best compatibility
        con.setRequestProperty("User-Agent", USER_AGENT);
        
        // check response code
        int responseCode = con.getResponseCode();
        String responseMessage = con.getResponseMessage();

        if (responseCode < 200 || responseCode > 299) {
            throw new IOException(String.format("HTTP %d %s", responseCode, responseMessage));
        }
        
        // get content length for logging
        String contentLength = con.getHeaderField("Content-Length");
        if (contentLength != null) {
            try {
                contentLength = StringUtils.humanReadableByteCount(Integer.parseInt(contentLength), true);
            } catch (NumberFormatException ex) {
            }
        }
        
        L.log(Level.INFO, "Downloading {0} ({1})", new Object[]{url, contentLength});
        
        return con.getInputStream();
    }
}
