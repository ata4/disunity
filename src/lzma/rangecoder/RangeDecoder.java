package lzma.rangecoder;

import java.io.IOException;
import java.io.InputStream;

public class RangeDecoder extends RangeCoder {
    
    private InputStream stream;
    private int range;
    private int code;

    public void setStream(InputStream stream) {
        this.stream = stream;
    }

    public void releaseStream() {
        stream = null;
    }

    public void init() throws IOException {
        code = 0;
        range = -1;

        for (int i = 0; i < 5; i++) {
            code = (code << 8) | stream.read();
        }
    }

    public int decodeDirectBits(int numTotalBits) throws IOException {
        int result = 0;
        for (int i = numTotalBits; i != 0; i--) {
            range >>>= 1;
            int t = ((code - range) >>> 31);
            code -= range & (t - 1);
            result = (result << 1) | (1 - t);

            if ((range & kTopMask) == 0) {
                code = (code << 8) | stream.read();
                range <<= 8;
            }
        }
        return result;
    }

    public int decodeBit(short[] probs, int index) throws IOException {
        int prob = probs[index];
        int newBound = (range >>> kNumBitModelTotalBits) * prob;
        if ((code ^ 0x80000000) < (newBound ^ 0x80000000)) {
            range = newBound;
            probs[index] = (short) (prob + ((kBitModelTotal - prob) >>> kNumMoveBits));
            if ((range & kTopMask) == 0) {
                code = (code << 8) | stream.read();
                range <<= 8;
            }
            return 0;
        } else {
            range -= newBound;
            code -= newBound;
            probs[index] = (short) (prob - ((prob) >>> kNumMoveBits));
            if ((range & kTopMask) == 0) {
                code = (code << 8) | stream.read();
                range <<= 8;
            }
            return 1;
        }
    }

    public static void initBitModels(short[] probs) {
        for (int i = 0; i < probs.length; i++) {
            probs[i] = (kBitModelTotal >>> 1);
        }
    }
}
