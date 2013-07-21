// LZ.BinTree
package lzma.lz;

import java.io.IOException;

public class BinTree extends InWindow {

    private static final int kHash2Size = 1 << 10;
    private static final int kHash3Size = 1 << 16;
    private static final int kBT2HashSize = 1 << 16;
    private static final int kStartMaxLen = 1;
    private static final int kHash3Offset = kHash2Size;
    private static final int kEmptyHashValue = 0;
    private static final int kMaxValForNormalize = (1 << 30) - 1;
    
    private int _cyclicBufferPos;
    private int _cyclicBufferSize = 0;
    private int _matchMaxLen;
    private int[] _son;
    private int[] _hash;
    private int _cutValue = 0xFF;
    private int _hashMask;
    private int _hashSizeSum = 0;
    private boolean HASH_ARRAY = true;
    private int kNumHashDirectBytes = 0;
    private int kMinMatchCheck = 4;
    private int kFixHashSize = kHash2Size + kHash3Size;

    public void setType(int numHashBytes) {
        HASH_ARRAY = (numHashBytes > 2);
        if (HASH_ARRAY) {
            kNumHashDirectBytes = 0;
            kMinMatchCheck = 4;
            kFixHashSize = kHash2Size + kHash3Size;
        } else {
            kNumHashDirectBytes = 2;
            kMinMatchCheck = 2 + 1;
            kFixHashSize = 0;
        }
    }

    @Override
    public void init() throws IOException {
        super.init();
        for (int i = 0; i < _hashSizeSum; i++) {
            _hash[i] = kEmptyHashValue;
        }
        _cyclicBufferPos = 0;
        reduceOffsets(-1);
    }

    @Override
    public void movePos() throws IOException {
        if (++_cyclicBufferPos >= _cyclicBufferSize) {
            _cyclicBufferPos = 0;
        }
        super.movePos();
        if (_pos == kMaxValForNormalize) {
            normalize();
        }
    }

    public boolean create(int historySize, int keepAddBufferBefore,
            int matchMaxLen, int keepAddBufferAfter) {
        if (historySize > kMaxValForNormalize - 256) {
            return false;
        }
        _cutValue = 16 + (matchMaxLen >> 1);

        int windowReservSize = (historySize + keepAddBufferBefore
                + matchMaxLen + keepAddBufferAfter) / 2 + 256;

        super.create(historySize + keepAddBufferBefore, matchMaxLen + keepAddBufferAfter, windowReservSize);

        _matchMaxLen = matchMaxLen;

        int cyclicBufferSize = historySize + 1;
        if (_cyclicBufferSize != cyclicBufferSize) {
            _son = new int[(_cyclicBufferSize = cyclicBufferSize) * 2];
        }

        int hs = kBT2HashSize;

        if (HASH_ARRAY) {
            hs = historySize - 1;
            hs |= (hs >> 1);
            hs |= (hs >> 2);
            hs |= (hs >> 4);
            hs |= (hs >> 8);
            hs >>= 1;
            hs |= 0xFFFF;
            if (hs > (1 << 24)) {
                hs >>= 1;
            }
            _hashMask = hs;
            hs++;
            hs += kFixHashSize;
        }
        if (hs != _hashSizeSum) {
            _hash = new int[_hashSizeSum = hs];
        }
        return true;
    }

    public int getMatches(int[] distances) throws IOException {
        int lenLimit;
        if (_pos + _matchMaxLen <= _streamPos) {
            lenLimit = _matchMaxLen;
        } else {
            lenLimit = _streamPos - _pos;
            if (lenLimit < kMinMatchCheck) {
                movePos();
                return 0;
            }
        }

        int offset = 0;
        int matchMinPos = (_pos > _cyclicBufferSize) ? (_pos - _cyclicBufferSize) : 0;
        int cur = _bufferOffset + _pos;
        int maxLen = kStartMaxLen; // to avoid items for len < hashSize;
        int hashValue, hash2Value = 0, hash3Value = 0;

        if (HASH_ARRAY) {
            int temp = CrcTable[_bufferBase[cur] & 0xFF] ^ (_bufferBase[cur + 1] & 0xFF);
            hash2Value = temp & (kHash2Size - 1);
            temp ^= ((int) (_bufferBase[cur + 2] & 0xFF) << 8);
            hash3Value = temp & (kHash3Size - 1);
            hashValue = (temp ^ (CrcTable[_bufferBase[cur + 3] & 0xFF] << 5)) & _hashMask;
        } else {
            hashValue = ((_bufferBase[cur] & 0xFF) ^ ((int) (_bufferBase[cur + 1] & 0xFF) << 8));
        }

        int curMatch = _hash[kFixHashSize + hashValue];
        if (HASH_ARRAY) {
            int curMatch2 = _hash[hash2Value];
            int curMatch3 = _hash[kHash3Offset + hash3Value];
            _hash[hash2Value] = _pos;
            _hash[kHash3Offset + hash3Value] = _pos;
            if (curMatch2 > matchMinPos) {
                if (_bufferBase[_bufferOffset + curMatch2] == _bufferBase[cur]) {
                    distances[offset++] = maxLen = 2;
                    distances[offset++] = _pos - curMatch2 - 1;
                }
            }
            if (curMatch3 > matchMinPos) {
                if (_bufferBase[_bufferOffset + curMatch3] == _bufferBase[cur]) {
                    if (curMatch3 == curMatch2) {
                        offset -= 2;
                    }
                    distances[offset++] = maxLen = 3;
                    distances[offset++] = _pos - curMatch3 - 1;
                    curMatch2 = curMatch3;
                }
            }
            if (offset != 0 && curMatch2 == curMatch) {
                offset -= 2;
                maxLen = kStartMaxLen;
            }
        }

        _hash[kFixHashSize + hashValue] = _pos;

        int ptr0 = (_cyclicBufferPos << 1) + 1;
        int ptr1 = (_cyclicBufferPos << 1);

        int len0, len1;
        len0 = len1 = kNumHashDirectBytes;

        if (kNumHashDirectBytes != 0) {
            if (curMatch > matchMinPos) {
                if (_bufferBase[_bufferOffset + curMatch + kNumHashDirectBytes]
                        != _bufferBase[cur + kNumHashDirectBytes]) {
                    distances[offset++] = maxLen = kNumHashDirectBytes;
                    distances[offset++] = _pos - curMatch - 1;
                }
            }
        }

        int count = _cutValue;

        while (true) {
            if (curMatch <= matchMinPos || count-- == 0) {
                _son[ptr0] = _son[ptr1] = kEmptyHashValue;
                break;
            }
            int delta = _pos - curMatch;
            int cyclicPos = ((delta <= _cyclicBufferPos)
                    ? (_cyclicBufferPos - delta)
                    : (_cyclicBufferPos - delta + _cyclicBufferSize)) << 1;

            int pby1 = _bufferOffset + curMatch;
            int len = Math.min(len0, len1);
            if (_bufferBase[pby1 + len] == _bufferBase[cur + len]) {
                while (++len != lenLimit) {
                    if (_bufferBase[pby1 + len] != _bufferBase[cur + len]) {
                        break;
                    }
                }
                if (maxLen < len) {
                    distances[offset++] = maxLen = len;
                    distances[offset++] = delta - 1;
                    if (len == lenLimit) {
                        _son[ptr1] = _son[cyclicPos];
                        _son[ptr0] = _son[cyclicPos + 1];
                        break;
                    }
                }
            }
            if ((_bufferBase[pby1 + len] & 0xFF) < (_bufferBase[cur + len] & 0xFF)) {
                _son[ptr1] = curMatch;
                ptr1 = cyclicPos + 1;
                curMatch = _son[ptr1];
                len1 = len;
            } else {
                _son[ptr0] = curMatch;
                ptr0 = cyclicPos;
                curMatch = _son[ptr0];
                len0 = len;
            }
        }
        movePos();
        return offset;
    }

    public void skip(int num) throws IOException {
        do {
            int lenLimit;
            if (_pos + _matchMaxLen <= _streamPos) {
                lenLimit = _matchMaxLen;
            } else {
                lenLimit = _streamPos - _pos;
                if (lenLimit < kMinMatchCheck) {
                    movePos();
                    continue;
                }
            }

            int matchMinPos = (_pos > _cyclicBufferSize) ? (_pos - _cyclicBufferSize) : 0;
            int cur = _bufferOffset + _pos;

            int hashValue;

            if (HASH_ARRAY) {
                int temp = CrcTable[_bufferBase[cur] & 0xFF] ^ (_bufferBase[cur + 1] & 0xFF);
                int hash2Value = temp & (kHash2Size - 1);
                _hash[hash2Value] = _pos;
                temp ^= ((int) (_bufferBase[cur + 2] & 0xFF) << 8);
                int hash3Value = temp & (kHash3Size - 1);
                _hash[kHash3Offset + hash3Value] = _pos;
                hashValue = (temp ^ (CrcTable[_bufferBase[cur + 3] & 0xFF] << 5)) & _hashMask;
            } else {
                hashValue = ((_bufferBase[cur] & 0xFF) ^ ((int) (_bufferBase[cur + 1] & 0xFF) << 8));
            }

            int curMatch = _hash[kFixHashSize + hashValue];
            _hash[kFixHashSize + hashValue] = _pos;

            int ptr0 = (_cyclicBufferPos << 1) + 1;
            int ptr1 = (_cyclicBufferPos << 1);

            int len0, len1;
            len0 = len1 = kNumHashDirectBytes;

            int count = _cutValue;
            while (true) {
                if (curMatch <= matchMinPos || count-- == 0) {
                    _son[ptr0] = _son[ptr1] = kEmptyHashValue;
                    break;
                }

                int delta = _pos - curMatch;
                int cyclicPos = ((delta <= _cyclicBufferPos)
                        ? (_cyclicBufferPos - delta)
                        : (_cyclicBufferPos - delta + _cyclicBufferSize)) << 1;

                int pby1 = _bufferOffset + curMatch;
                int len = Math.min(len0, len1);
                if (_bufferBase[pby1 + len] == _bufferBase[cur + len]) {
                    while (++len != lenLimit) {
                        if (_bufferBase[pby1 + len] != _bufferBase[cur + len]) {
                            break;
                        }
                    }
                    if (len == lenLimit) {
                        _son[ptr1] = _son[cyclicPos];
                        _son[ptr0] = _son[cyclicPos + 1];
                        break;
                    }
                }
                if ((_bufferBase[pby1 + len] & 0xFF) < (_bufferBase[cur + len] & 0xFF)) {
                    _son[ptr1] = curMatch;
                    ptr1 = cyclicPos + 1;
                    curMatch = _son[ptr1];
                    len1 = len;
                } else {
                    _son[ptr0] = curMatch;
                    ptr0 = cyclicPos;
                    curMatch = _son[ptr0];
                    len0 = len;
                }
            }
            movePos();
        } while (--num != 0);
    }

    void normalizeLinks(int[] items, int numItems, int subValue) {
        for (int i = 0; i < numItems; i++) {
            int value = items[i];
            if (value <= subValue) {
                value = kEmptyHashValue;
            } else {
                value -= subValue;
            }
            items[i] = value;
        }
    }

    void normalize() {
        int subValue = _pos - _cyclicBufferSize;
        normalizeLinks(_son, _cyclicBufferSize * 2, subValue);
        normalizeLinks(_hash, _hashSizeSum, subValue);
        reduceOffsets(subValue);
    }

    public void setCutValue(int cutValue) {
        _cutValue = cutValue;
    }
    
    private static final int[] CrcTable = new int[256];

    static {
        for (int i = 0; i < 256; i++) {
            int r = i;
            for (int j = 0; j < 8; j++) {
                if ((r & 1) != 0) {
                    r = (r >>> 1) ^ 0xEDB88320;
                } else {
                    r >>>= 1;
                }
            }
            CrcTable[i] = r;
        }
    }
}
