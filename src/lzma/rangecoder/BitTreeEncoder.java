package lzma.rangecoder;

import java.io.IOException;

public class BitTreeEncoder extends BitTreeCoder {

    public BitTreeEncoder(int numBitLevels) {
        super(numBitLevels);
    }

    public void encode(RangeEncoder rangeEncoder, int symbol) throws IOException {
        int m = 1;
        for (int bitIndex = numBitLevels; bitIndex != 0;) {
            bitIndex--;
            int bit = (symbol >>> bitIndex) & 1;
            rangeEncoder.encode(models, m, bit);
            m = (m << 1) | bit;
        }
    }

    public void reverseEncode(RangeEncoder rangeEncoder, int symbol) throws IOException {
        int m = 1;
        for (int i = 0; i < numBitLevels; i++) {
            int bit = symbol & 1;
            rangeEncoder.encode(models, m, bit);
            m = (m << 1) | bit;
            symbol >>= 1;
        }
    }

    public int getPrice(int symbol) {
        int price = 0;
        int m = 1;
        for (int bitIndex = numBitLevels; bitIndex != 0;) {
            bitIndex--;
            int bit = (symbol >>> bitIndex) & 1;
            price += RangeEncoder.getPrice(models[m], bit);
            m = (m << 1) + bit;
        }
        return price;
    }

    public int reverseGetPrice(int symbol) {
        int price = 0;
        int m = 1;
        for (int i = numBitLevels; i != 0; i--) {
            int bit = symbol & 1;
            symbol >>>= 1;
            price += RangeEncoder.getPrice(models[m], bit);
            m = (m << 1) | bit;
        }
        return price;
    }

    public static int reverseGetPrice(short[] Models, int startIndex,
            int NumBitLevels, int symbol) {
        int price = 0;
        int m = 1;
        for (int i = NumBitLevels; i != 0; i--) {
            int bit = symbol & 1;
            symbol >>>= 1;
            price += RangeEncoder.getPrice(Models[startIndex + m], bit);
            m = (m << 1) | bit;
        }
        return price;
    }

    public static void reverseEncode(short[] Models, int startIndex,
            RangeEncoder rangeEncoder, int NumBitLevels, int symbol) throws IOException {
        int m = 1;
        for (int i = 0; i < NumBitLevels; i++) {
            int bit = symbol & 1;
            rangeEncoder.encode(Models, startIndex + m, bit);
            m = (m << 1) | bit;
            symbol >>= 1;
        }
    }
}
