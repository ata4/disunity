// LZ.InWindow
package lzma.lz;

import java.io.IOException;
import java.io.InputStream;

public class InWindow {

    private InputStream _stream;
    private int _posLimit;  // offset (from _buffer) of first byte when new block reading must be done
    private boolean _streamEndWasReached; // if (true) then _streamPos shows real end of stream
    private int _pointerToLastSafePosition;
    private int _keepSizeBefore;  // how many BYTEs must be kept in buffer before _pos
    private int _keepSizeAfter;   // how many BYTEs must be kept buffer after _pos
    protected byte[] _bufferBase; // pointer to buffer with data
    protected int _bufferOffset;
    protected int _blockSize;  // Size of Allocated memory block
    protected int _pos;             // offset (from _buffer) of curent byte
    protected int _streamPos;   // offset (from _buffer) of first not read byte from Stream

    public void moveBlock() {
        int offset = _bufferOffset + _pos - _keepSizeBefore;
        // we need one additional byte, since MovePos moves on 1 byte.
        if (offset > 0) {
            offset--;
        }

        int numBytes = _bufferOffset + _streamPos - offset;

        // check negative offset ????
        for (int i = 0; i < numBytes; i++) {
            _bufferBase[i] = _bufferBase[offset + i];
        }
        _bufferOffset -= offset;
    }

    public void readBlock() throws IOException {
        if (_streamEndWasReached) {
            return;
        }
        while (true) {
            int size = (0 - _bufferOffset) + _blockSize - _streamPos;
            if (size == 0) {
                return;
            }
            int numReadBytes = _stream.read(_bufferBase, _bufferOffset + _streamPos, size);
            if (numReadBytes == -1) {
                _posLimit = _streamPos;
                int pointerToPostion = _bufferOffset + _posLimit;
                if (pointerToPostion > _pointerToLastSafePosition) {
                    _posLimit = _pointerToLastSafePosition - _bufferOffset;
                }

                _streamEndWasReached = true;
                return;
            }
            _streamPos += numReadBytes;
            if (_streamPos >= _pos + _keepSizeAfter) {
                _posLimit = _streamPos - _keepSizeAfter;
            }
        }
    }

    public void free() {
        _bufferBase = null;
    }

    public void create(int keepSizeBefore, int keepSizeAfter, int keepSizeReserv) {
        _keepSizeBefore = keepSizeBefore;
        _keepSizeAfter = keepSizeAfter;
        int blockSize = keepSizeBefore + keepSizeAfter + keepSizeReserv;
        if (_bufferBase == null || _blockSize != blockSize) {
            free();
            _blockSize = blockSize;
            _bufferBase = new byte[_blockSize];
        }
        _pointerToLastSafePosition = _blockSize - keepSizeAfter;
    }

    public void setStream(InputStream stream) {
        _stream = stream;
    }

    public void releaseStream() {
        _stream = null;
    }

    public void init() throws IOException {
        _bufferOffset = 0;
        _pos = 0;
        _streamPos = 0;
        _streamEndWasReached = false;
        readBlock();
    }

    public void movePos() throws IOException {
        _pos++;
        if (_pos > _posLimit) {
            int pointerToPostion = _bufferOffset + _pos;
            if (pointerToPostion > _pointerToLastSafePosition) {
                moveBlock();
            }
            readBlock();
        }
    }

    public byte getIndexByte(int index) {
        return _bufferBase[_bufferOffset + _pos + index];
    }

    // index + limit have not to exceed _keepSizeAfter;
    public int getMatchLen(int index, int distance, int limit) {
        if (_streamEndWasReached) {
            if ((_pos + index) + limit > _streamPos) {
                limit = _streamPos - (_pos + index);
            }
        }
        distance++;
        // Byte *pby = _buffer + (size_t)_pos + index;
        int pby = _bufferOffset + _pos + index;

        int i;
        for (i = 0; i < limit && _bufferBase[pby + i] == _bufferBase[pby + i - distance]; i++);
        return i;
    }

    public int getNumAvailableBytes() {
        return _streamPos - _pos;
    }

    public void reduceOffsets(int subValue) {
        _bufferOffset += subValue;
        _posLimit -= subValue;
        _pos -= subValue;
        _streamPos -= subValue;
    }
}
