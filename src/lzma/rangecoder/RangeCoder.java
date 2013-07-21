package lzma.rangecoder;

public class RangeCoder {
    protected static final int kTopMask = ~((1 << 24) - 1);
    protected static final int kNumBitModelTotalBits = 11;
    protected static final int kBitModelTotal = (1 << kNumBitModelTotalBits);
    protected static final int kNumMoveBits = 5;
}
