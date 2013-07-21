package lzma.rangecoder;

public class BitTreeCoder {
    protected short[] models;
    protected int numBitLevels;

    public BitTreeCoder(int numBitLevels) {
        this.numBitLevels = numBitLevels;
        models = new short[1 << numBitLevels];
    }
    
    public void init() {
        RangeDecoder.initBitModels(models);
    }
}
