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

import java.util.List;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class UnityList extends UnityType {
    
    private List<Object> listData;
    
    public UnityList(String type) {
        super(type);
    }

    public List<Object> getList() {
        return listData;
    }

    public void setList(List<Object> listData) {
        this.listData = listData;
    }

    @Override
    public String toString() {
        return listData.toString();
    }
}
