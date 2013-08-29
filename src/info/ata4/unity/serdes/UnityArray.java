/*
 ** 2013 August 30
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.unity.serdes;

import java.nio.ByteBuffer;
import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityArray {
    
    private final String type;
    private ByteBuffer rawData;
    private List<Object> listData;
    
    public UnityArray(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
    
    public boolean isRaw() {
        return rawData != null;
    }

    public ByteBuffer getRaw() {
        return rawData;
    }
    
    public void setRaw(ByteBuffer rawData) {
        this.rawData = rawData;
    }

    public List<Object> getList() {
        return listData;
    }

    public void setList(List<Object> listData) {
        this.listData = listData;
    }
}
