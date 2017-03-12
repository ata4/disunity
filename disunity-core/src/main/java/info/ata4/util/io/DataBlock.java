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

import info.ata4.io.Positionable;
import java.io.IOException;

/**
 *
 * @author Nico Bergemann <barracuda415 at yahoo.de>
 */
public class DataBlock {

    private long offset;
    private long length;

    public long offset() {
        return offset;
    }

    public void offset(long offset) {
        this.offset = offset;
    }

    public long length() {
        return length;
    }

    public void length(long length) {
        this.length = length;
    }

    public void endOffset(long endOffset) {
        this.length = endOffset - offset;
    }

    public long endOffset() {
        return offset + length;
    }

    public boolean isIntersecting(DataBlock that) {
        return this.endOffset() > that.offset() && that.endOffset() > this.offset();
    }

    public boolean isInside(DataBlock that) {
        return this.offset() >= that.offset() && this.endOffset() <= that.endOffset();
    }

    public void markBegin(Positionable p) throws IOException {
        offset(p.position());
    }

    public void markEnd(Positionable p) throws IOException {
        DataBlock.this.endOffset(p.position());
    }

    @Override
    public String toString() {
        return DataBlock.this.offset() + " - " + endOffset() + " (" + DataBlock.this.length() + ")";
    }
}
