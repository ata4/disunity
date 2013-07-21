package lzma.rangecoder;

import java.io.IOException;

public class BitTreeDecoder extends BitTreeCoder {

    public BitTreeDecoder(int numBitLevels) {
        super(numBitLevels);
    }

    public int decode(RangeDecoder rangeDecoder) throws IOException {
        int m = 1;
        for (int bitIndex = numBitLevels; bitIndex != 0; bitIndex--) {
            m = (m << 1) + rangeDecoder.decodeBit(models, m);
        }
        return m - (1 << numBitLevels);
    }

    public int reverseDecode(RangeDecoder rangeDecoder) throws IOException {
        int m = 1;
        int symbol = 0;
        for (int bitIndex = 0; bitIndex < numBitLevels; bitIndex++) {
            int bit = rangeDecoder.decodeBit(models, m);
            m <<= 1;
            m += bit;
            symbol |= (bit << bitIndex);
        }
        return symbol;
    }

    public static int reverseDecode(short[] Models, int startIndex,
            RangeDecoder rangeDecoder, int NumBitLevels) throws IOException {
        int m = 1;
        int symbol = 0;
        for (int bitIndex = 0; bitIndex < NumBitLevels; bitIndex++) {
            int bit = rangeDecoder.decodeBit(Models, startIndex + m);
            m <<= 1;
            m += bit;
            symbol |= (bit << bitIndex);
        }
        return symbol;
    }
}
