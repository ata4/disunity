// LZ.OutWindow
package lzma.lz;

import java.io.IOException;
import java.io.OutputStream;

public class OutWindow {

    private OutputStream _stream;
    private byte[] _buffer;
    private int _pos;
    private int _windowSize = 0;
    private int _streamPos;

    public void create(int windowSize) {
        if (_buffer == null || _windowSize != windowSize) {
            _buffer = new byte[windowSize];
        }
        _windowSize = windowSize;
        _pos = 0;
        _streamPos = 0;
    }

    public void setStream(OutputStream stream) throws IOException {
        releaseStream();
        _stream = stream;
    }

    public void releaseStream() throws IOException {
        flush();
        _stream = null;
    }

    public void init(boolean solid) {
        if (!solid) {
            _streamPos = 0;
            _pos = 0;
        }
    }

    public void flush() throws IOException {
        int size = _pos - _streamPos;
        if (size == 0) {
            return;
        }
        _stream.write(_buffer, _streamPos, size);
        if (_pos >= _windowSize) {
            _pos = 0;
        }
        _streamPos = _pos;
    }

    public void copyBlock(int distance, int len) throws IOException {
        int pos = _pos - distance - 1;
        if (pos < 0) {
            pos += _windowSize;
        }
        for (; len != 0; len--) {
            if (pos >= _windowSize) {
                pos = 0;
            }
            _buffer[_pos++] = _buffer[pos++];
            if (_pos >= _windowSize) {
                flush();
            }
        }
    }

    public void putByte(byte b) throws IOException {
        _buffer[_pos++] = b;
        if (_pos >= _windowSize) {
            flush();
        }
    }

    public byte getByte(int distance) {
        int pos = _pos - distance - 1;
        if (pos < 0) {
            pos += _windowSize;
        }
        return _buffer[pos];
    }
}
