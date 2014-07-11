/*
 ** 2014 July 11
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serdes;

import java.nio.charset.Charset;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityString implements UnityTag<String> {
    
    private String type;
    private byte[] data;

    public UnityString(byte[] data) {
        this.data = data;
    }
    
    public UnityString() {
    }
    
    public String toString(Charset cs) {
        return new String(data, cs);
    }
    
    public void fromString(String string, Charset cs) {
        data = string.getBytes(cs);
    }

    public byte[] getRaw() {
        return data;
    }

    public void setRaw(byte[] data) {
        this.data = data;
    }
    
    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String get() {
        return toString(Charset.forName("UTF-8"));
    }

    @Override
    public void set(String value) {
        fromString(value, Charset.forName("UTF-8"));
    }
}
