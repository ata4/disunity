/*
 ** 2014 February 03
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.util;

/**
 * Unity engine version string container.
 * 
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityVersion {
    
    private byte major;
    private byte minor;
    private byte patch;
    private String revision;
    private String raw;
    
    public UnityVersion(String version) {
        try {
            major = partFromString(version.substring(0, 1));
            minor = partFromString(version.substring(2, 3));
            patch = partFromString(version.substring(4, 5));
            revision = version.substring(5);
        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
            // invalid format, save raw string
            raw = version;
        }
    }
    
    private byte partFromString(String part) {
        if (part.equals("x")) {
            return -1;
        } else {
            return Byte.valueOf(part);
        }
    }
    
    private String partToString(byte part) {
        if (part == -1) {
            return "x";
        } else {
            return String.valueOf(part);
        }
    }
    
    public boolean isValid() {
        return raw == null;
    }

    public byte getMajor() {
        return major;
    }

    public void setMajor(byte major) {
        this.major = major;
    }

    public byte getMinor() {
        return minor;
    }

    public void setMinor(byte minor) {
        this.minor = minor;
    }

    public byte getPatch() {
        return patch;
    }

    public void setPatch(byte patch) {
        this.patch = patch;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }
    
    @Override
    public String toString() {
        if (raw != null) {
            return raw;
        } else {
            return String.format("%s.%s.%s%s", partToString(major),
                    partToString(minor), partToString(patch), revision);
        }
    }
}
