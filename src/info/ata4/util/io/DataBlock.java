/*
 ** 2014 October 28
 **
 ** The author disclaims copyright to this source code.  In place of
 ** a legal notice, here is a blessing:
 **    May you do good and not evil.
 **    May you find forgiveness for yourself and forgive others.
 **    May you share freely, never taking more than you give.
 */
package info.ata4.util.io;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DataBlock {
    
    private long offset;
    private long length;

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }
    
    public void setEndOffset(long endOffset) {
        this.length = endOffset - offset;
    }
    
    public long getEndOffset() {
        return offset + length;
    }
    
    public boolean isIntersecting(DataBlock that) {
        return this.getEndOffset() > that.getOffset() && that.getEndOffset() > this.getOffset();
    }
    
    public boolean isInside(DataBlock that) {
        return this.getOffset() >= that.getOffset() && this.getEndOffset() <= that.getEndOffset();
    }
    
    @Override
    public String toString() {
        return getOffset() + " - " + getEndOffset() + " (" + getLength() + ")";
    }
}
