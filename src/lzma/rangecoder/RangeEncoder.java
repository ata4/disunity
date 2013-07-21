package lzma.rangecoder;

import java.io.IOException;
import java.io.OutputStream;

public class RangeEncoder extends RangeCoder {

    public static final int kNumBitPriceShiftBits = 6;
    private static final int kNumMoveReducingBits = 2;
    private static int[] ProbPrices = new int[kBitModelTotal >>> kNumMoveReducingBits];
    
    private OutputStream stream;
    private long low;
    private int range;
    private int _cacheSize;
    private int _cache;
    private long _position;
    
    static {
        int kNumBits = (kNumBitModelTotalBits - kNumMoveReducingBits);
        for (int i = kNumBits - 1; i >= 0; i--) {
            int start = 1 << (kNumBits - i - 1);
            int end = 1 << (kNumBits - i);
            for (int j = start; j < end; j++) {
                ProbPrices[j] = (i << kNumBitPriceShiftBits)
                        + (((end - j) << kNumBitPriceShiftBits) >>> (kNumBits - i - 1));
            }
        }
    }

    public void setStream(OutputStream stream) {
        this.stream = stream;
    }

    public void releaseStream() {
        stream = null;
    }

    public void init() {
        _position = 0;
        low = 0;
        range = -1;
        _cacheSize = 1;
        _cache = 0;
    }

    public void flushData() throws IOException {
        for (int i = 0; i < 5; i++) {
            shiftLow();
        }
    }

    public void flushStream() throws IOException {
        stream.flush();
    }

    public void shiftLow() throws IOException {
        int LowHi = (int) (low >>> 32);
        if (LowHi != 0 || low < 0xFF000000L) {
            _position += _cacheSize;
            int temp = _cache;
            do {
                stream.write(temp + LowHi);
                temp = 0xFF;
            } while (--_cacheSize != 0);
            _cache = (((int) low) >>> 24);
        }
        _cacheSize++;
        low = (low & 0xFFFFFF) << 8;
    }

    public void encodeDirectBits(int v, int numTotalBits) throws IOException {
        for (int i = numTotalBits - 1; i >= 0; i--) {
            range >>>= 1;
            if (((v >>> i) & 1) == 1) {
                low += range;
            }
            if ((range & RangeEncoder.kTopMask) == 0) {
                range <<= 8;
                shiftLow();
            }
        }
    }

    public long getProcessedSizeAdd() {
        return _cacheSize + _position + 4;
    }

    public static void initBitModels(short[] probs) {
        for (int i = 0; i < probs.length; i++) {
            probs[i] = (kBitModelTotal >>> 1);
        }
    }

    public void encode(short[] probs, int index, int symbol) throws IOException {
        int prob = probs[index];
        int newBound = (range >>> kNumBitModelTotalBits) * prob;
        if (symbol == 0) {
            range = newBound;
            probs[index] = (short) (prob + ((kBitModelTotal - prob) >>> kNumMoveBits));
        } else {
            low += (newBound & 0xFFFFFFFFL);
            range -= newBound;
            probs[index] = (short) (prob - ((prob) >>> kNumMoveBits));
        }
        if ((range & kTopMask) == 0) {
            range <<= 8;
            shiftLow();
        }
    }

    public static int getPrice(int Prob, int symbol) {
        return ProbPrices[(((Prob - symbol) ^ ((-symbol))) & (kBitModelTotal - 1)) >>> kNumMoveReducingBits];
    }

    public static int getPrice0(int Prob) {
        return ProbPrices[Prob >>> kNumMoveReducingBits];
    }

    public static int getPrice1(int Prob) {
        return ProbPrices[(kBitModelTotal - Prob) >>> kNumMoveReducingBits];
    }
}
