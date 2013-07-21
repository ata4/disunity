package lzma;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import lzma.lz.BinTree;
import lzma.rangecoder.BitTreeEncoder;
import lzma.rangecoder.RangeEncoder;

public class LzmaEncoder {

    public static final int EMatchFinderTypeBT2 = 0;
    public static final int EMatchFinderTypeBT4 = 1;
    private static final int kIfinityPrice = 0xFFFFFFF;
    private static byte[] g_FastPos = new byte[1 << 11];

    static {
        int kFastSlots = 22;
        int c = 2;
        g_FastPos[0] = 0;
        g_FastPos[1] = 1;
        for (int slotFast = 2; slotFast < kFastSlots; slotFast++) {
            int k = (1 << ((slotFast >> 1) - 1));
            for (int j = 0; j < k; j++, c++) {
                g_FastPos[c] = (byte) slotFast;
            }
        }
    }

    static int getPosSlot(int pos) {
        if (pos < (1 << 11)) {
            return g_FastPos[pos];
        }
        if (pos < (1 << 21)) {
            return (g_FastPos[pos >> 10] + 20);
        }
        return (g_FastPos[pos >> 20] + 40);
    }

    static int getPosSlot2(int pos) {
        if (pos < (1 << 17)) {
            return (g_FastPos[pos >> 6] + 12);
        }
        if (pos < (1 << 27)) {
            return (g_FastPos[pos >> 16] + 32);
        }
        return (g_FastPos[pos >> 26] + 52);
    }
    private int _state = LzmaState.stateInit();
    private byte _previousByte;
    private int[] _repDistances = new int[LzmaState.kNumRepDistances];

    void baseInit() {
        _state = LzmaState.stateInit();
        _previousByte = 0;
        for (int i = 0; i < LzmaState.kNumRepDistances; i++) {
            _repDistances[i] = 0;
        }
    }
    private static final int kDefaultDictionaryLogSize = 22;
    private static final int kNumFastBytesDefault = 0x20;

    class LiteralEncoder {

        class Encoder2 {

            private short[] m_Encoders = new short[0x300];

            public void init() {
                RangeEncoder.initBitModels(m_Encoders);
            }

            public void encode(RangeEncoder rangeEncoder, byte symbol) throws IOException {
                int context = 1;
                for (int i = 7; i >= 0; i--) {
                    int bit = ((symbol >> i) & 1);
                    rangeEncoder.encode(m_Encoders, context, bit);
                    context = (context << 1) | bit;
                }
            }

            public void encodeMatched(RangeEncoder rangeEncoder, byte matchByte, byte symbol) throws IOException {
                int context = 1;
                boolean same = true;
                for (int i = 7; i >= 0; i--) {
                    int bit = ((symbol >> i) & 1);
                    int state = context;
                    if (same) {
                        int matchBit = ((matchByte >> i) & 1);
                        state += ((1 + matchBit) << 8);
                        same = (matchBit == bit);
                    }
                    rangeEncoder.encode(m_Encoders, state, bit);
                    context = (context << 1) | bit;
                }
            }

            public int getPrice(boolean matchMode, byte matchByte, byte symbol) {
                int price = 0;
                int context = 1;
                int i = 7;
                if (matchMode) {
                    for (; i >= 0; i--) {
                        int matchBit = (matchByte >> i) & 1;
                        int bit = (symbol >> i) & 1;
                        price += RangeEncoder.getPrice(m_Encoders[((1 + matchBit) << 8) + context], bit);
                        context = (context << 1) | bit;
                        if (matchBit != bit) {
                            i--;
                            break;
                        }
                    }
                }
                for (; i >= 0; i--) {
                    int bit = (symbol >> i) & 1;
                    price += RangeEncoder.getPrice(m_Encoders[context], bit);
                    context = (context << 1) | bit;
                }
                return price;
            }
        }
        private Encoder2[] m_Coders;
        private int m_NumPrevBits;
        private int m_NumPosBits;
        private int m_PosMask;

        public void create(int numPosBits, int numPrevBits) {
            if (m_Coders != null && m_NumPrevBits == numPrevBits && m_NumPosBits == numPosBits) {
                return;
            }
            m_NumPosBits = numPosBits;
            m_PosMask = (1 << numPosBits) - 1;
            m_NumPrevBits = numPrevBits;
            int numStates = 1 << (m_NumPrevBits + m_NumPosBits);
            m_Coders = new Encoder2[numStates];
            for (int i = 0; i < numStates; i++) {
                m_Coders[i] = new Encoder2();
            }
        }

        public void init() {
            int numStates = 1 << (m_NumPrevBits + m_NumPosBits);
            for (int i = 0; i < numStates; i++) {
                m_Coders[i].init();
            }
        }

        public Encoder2 getSubCoder(int pos, byte prevByte) {
            return m_Coders[((pos & m_PosMask) << m_NumPrevBits) + ((prevByte & 0xFF) >>> (8 - m_NumPrevBits))];
        }
    }

    class LenEncoder {

        private short[] _choice = new short[2];
        private BitTreeEncoder[] _lowCoder = new BitTreeEncoder[LzmaState.kNumPosStatesEncodingMax];
        private BitTreeEncoder[] _midCoder = new BitTreeEncoder[LzmaState.kNumPosStatesEncodingMax];
        private BitTreeEncoder _highCoder = new BitTreeEncoder(LzmaState.kNumHighLenBits);

        public LenEncoder() {
            for (int posState = 0; posState < LzmaState.kNumPosStatesEncodingMax; posState++) {
                _lowCoder[posState] = new BitTreeEncoder(LzmaState.kNumLowLenBits);
                _midCoder[posState] = new BitTreeEncoder(LzmaState.kNumMidLenBits);
            }
        }

        public void init(int numPosStates) {
            RangeEncoder.initBitModels(_choice);

            for (int posState = 0; posState < numPosStates; posState++) {
                _lowCoder[posState].init();
                _midCoder[posState].init();
            }
            _highCoder.init();
        }

        public void encode(RangeEncoder rangeEncoder, int symbol, int posState) throws IOException {
            if (symbol < LzmaState.kNumLowLenSymbols) {
                rangeEncoder.encode(_choice, 0, 0);
                _lowCoder[posState].encode(rangeEncoder, symbol);
            } else {
                symbol -= LzmaState.kNumLowLenSymbols;
                rangeEncoder.encode(_choice, 0, 1);
                if (symbol < LzmaState.kNumMidLenSymbols) {
                    rangeEncoder.encode(_choice, 1, 0);
                    _midCoder[posState].encode(rangeEncoder, symbol);
                } else {
                    rangeEncoder.encode(_choice, 1, 1);
                    _highCoder.encode(rangeEncoder, symbol - LzmaState.kNumMidLenSymbols);
                }
            }
        }

        public void setPrices(int posState, int numSymbols, int[] prices, int st) {
            int a0 = RangeEncoder.getPrice0(_choice[0]);
            int a1 = RangeEncoder.getPrice1(_choice[0]);
            int b0 = a1 + RangeEncoder.getPrice0(_choice[1]);
            int b1 = a1 + RangeEncoder.getPrice1(_choice[1]);
            int i = 0;
            for (i = 0; i < LzmaState.kNumLowLenSymbols; i++) {
                if (i >= numSymbols) {
                    return;
                }
                prices[st + i] = a0 + _lowCoder[posState].getPrice(i);
            }
            for (; i < LzmaState.kNumLowLenSymbols + LzmaState.kNumMidLenSymbols; i++) {
                if (i >= numSymbols) {
                    return;
                }
                prices[st + i] = b0 + _midCoder[posState].getPrice(i - LzmaState.kNumLowLenSymbols);
            }
            for (; i < numSymbols; i++) {
                prices[st + i] = b1 + _highCoder.getPrice(i - LzmaState.kNumLowLenSymbols - LzmaState.kNumMidLenSymbols);
            }
        }
    };
    public static final int kNumLenSpecSymbols = LzmaState.kNumLowLenSymbols + LzmaState.kNumMidLenSymbols;

    class LenPriceTableEncoder extends LenEncoder {

        int[] _prices = new int[LzmaState.kNumLenSymbols << LzmaState.kNumPosStatesBitsEncodingMax];
        int _tableSize;
        int[] _counters = new int[LzmaState.kNumPosStatesEncodingMax];

        public void setTableSize(int tableSize) {
            _tableSize = tableSize;
        }

        public int getPrice(int symbol, int posState) {
            return _prices[posState * LzmaState.kNumLenSymbols + symbol];
        }

        void updateTable(int posState) {
            setPrices(posState, _tableSize, _prices, posState * LzmaState.kNumLenSymbols);
            _counters[posState] = _tableSize;
        }

        public void updateTables(int numPosStates) {
            for (int posState = 0; posState < numPosStates; posState++) {
                updateTable(posState);
            }
        }

        @Override
        public void encode(RangeEncoder rangeEncoder, int symbol, int posState) throws IOException {
            super.encode(rangeEncoder, symbol, posState);
            if (--_counters[posState] == 0) {
                updateTable(posState);
            }
        }
    }
    static final int kNumOpts = 1 << 12;

    class Optimal {

        public int State;
        public boolean Prev1IsChar;
        public boolean Prev2;
        public int PosPrev2;
        public int BackPrev2;
        public int Price;
        public int PosPrev;
        public int BackPrev;
        public int Backs0;
        public int Backs1;
        public int Backs2;
        public int Backs3;

        public void makeAsChar() {
            BackPrev = -1;
            Prev1IsChar = false;
        }

        public void makeAsShortRep() {
            BackPrev = 0;
            Prev1IsChar = false;
        }

        public boolean isShortRep() {
            return (BackPrev == 0);
        }
    };
    
    private Optimal[] _optimum = new Optimal[kNumOpts];
    private BinTree _matchFinder = null;
    private RangeEncoder _rangeEncoder = new RangeEncoder();
    private short[] _isMatch = new short[LzmaState.kNumStates << LzmaState.kNumPosStatesBitsMax];
    private short[] _isRep = new short[LzmaState.kNumStates];
    private short[] _isRepG0 = new short[LzmaState.kNumStates];
    private short[] _isRepG1 = new short[LzmaState.kNumStates];
    private short[] _isRepG2 = new short[LzmaState.kNumStates];
    private short[] _isRep0Long = new short[LzmaState.kNumStates << LzmaState.kNumPosStatesBitsMax];
    private BitTreeEncoder[] _posSlotEncoder = new BitTreeEncoder[LzmaState.kNumLenToPosStates]; // kNumPosSlotBits
    private short[] _posEncoders = new short[LzmaState.kNumFullDistances - LzmaState.kEndPosModelIndex];
    private BitTreeEncoder _posAlignEncoder = new BitTreeEncoder(LzmaState.kNumAlignBits);
    private LenPriceTableEncoder _lenEncoder = new LenPriceTableEncoder();
    private LenPriceTableEncoder _repMatchLenEncoder = new LenPriceTableEncoder();
    private LiteralEncoder _literalEncoder = new LiteralEncoder();
    private int[] _matchDistances = new int[LzmaState.kMatchMaxLen * 2 + 2];
    private int _numFastBytes = kNumFastBytesDefault;
    private int _longestMatchLength;
    private int _numDistancePairs;
    private int _additionalOffset;
    private int _optimumEndIndex;
    private int _optimumCurrentIndex;
    private boolean _longestMatchWasFound;
    private int[] _posSlotPrices = new int[1 << (LzmaState.kNumPosSlotBits + LzmaState.kNumLenToPosStatesBits)];
    private int[] _distancesPrices = new int[LzmaState.kNumFullDistances << LzmaState.kNumLenToPosStatesBits];
    private int[] _alignPrices = new int[LzmaState.kAlignTableSize];
    private int _alignPriceCount;
    private int _distTableSize = (kDefaultDictionaryLogSize * 2);
    private int _posStateBits = 2;
    private int _posStateMask = (4 - 1);
    private int _numLiteralPosStateBits = 0;
    private int _numLiteralContextBits = 3;
    private int _dictionarySize = (1 << kDefaultDictionaryLogSize);
    private int _dictionarySizePrev = -1;
    private int _numFastBytesPrev = -1;
    private long nowPos64;
    private boolean _finished;
    private InputStream _inStream;
    private int _matchFinderType = EMatchFinderTypeBT4;
    private boolean _writeEndMark = false;
    private boolean _needReleaseMFStream = false;

    void create() {
        if (_matchFinder == null) {
            BinTree bt = new BinTree();
            int numHashBytes = 4;
            if (_matchFinderType == EMatchFinderTypeBT2) {
                numHashBytes = 2;
            }
            bt.setType(numHashBytes);
            _matchFinder = bt;
        }
        _literalEncoder.create(_numLiteralPosStateBits, _numLiteralContextBits);

        if (_dictionarySize == _dictionarySizePrev && _numFastBytesPrev == _numFastBytes) {
            return;
        }
        _matchFinder.create(_dictionarySize, kNumOpts, _numFastBytes, LzmaState.kMatchMaxLen + 1);
        _dictionarySizePrev = _dictionarySize;
        _numFastBytesPrev = _numFastBytes;
    }

    public LzmaEncoder() {
        for (int i = 0; i < kNumOpts; i++) {
            _optimum[i] = new Optimal();
        }
        for (int i = 0; i < LzmaState.kNumLenToPosStates; i++) {
            _posSlotEncoder[i] = new BitTreeEncoder(LzmaState.kNumPosSlotBits);
        }
    }

    void setWriteEndMarkerMode(boolean writeEndMarker) {
        _writeEndMark = writeEndMarker;
    }

    void init() {
        baseInit();
        _rangeEncoder.init();

        RangeEncoder.initBitModels(_isMatch);
        RangeEncoder.initBitModels(_isRep0Long);
        RangeEncoder.initBitModels(_isRep);
        RangeEncoder.initBitModels(_isRepG0);
        RangeEncoder.initBitModels(_isRepG1);
        RangeEncoder.initBitModels(_isRepG2);
        RangeEncoder.initBitModels(_posEncoders);

        _literalEncoder.init();
        for (int i = 0; i < LzmaState.kNumLenToPosStates; i++) {
            _posSlotEncoder[i].init();
        }

        _lenEncoder.init(1 << _posStateBits);
        _repMatchLenEncoder.init(1 << _posStateBits);

        _posAlignEncoder.init();

        _longestMatchWasFound = false;
        _optimumEndIndex = 0;
        _optimumCurrentIndex = 0;
        _additionalOffset = 0;
    }

    int readMatchDistances() throws IOException {
        int lenRes = 0;
        _numDistancePairs = _matchFinder.getMatches(_matchDistances);
        if (_numDistancePairs > 0) {
            lenRes = _matchDistances[_numDistancePairs - 2];
            if (lenRes == _numFastBytes) {
                lenRes += _matchFinder.getMatchLen((int) lenRes - 1, _matchDistances[_numDistancePairs - 1],
                        LzmaState.kMatchMaxLen - lenRes);
            }
        }
        _additionalOffset++;
        return lenRes;
    }

    void movePos(int num) throws IOException {
        if (num > 0) {
            _matchFinder.skip(num);
            _additionalOffset += num;
        }
    }

    int getRepLen1Price(int state, int posState) {
        return RangeEncoder.getPrice0(_isRepG0[state])
                + RangeEncoder.getPrice0(_isRep0Long[(state << LzmaState.kNumPosStatesBitsMax) + posState]);
    }

    int getPureRepPrice(int repIndex, int state, int posState) {
        int price;
        if (repIndex == 0) {
            price = RangeEncoder.getPrice0(_isRepG0[state]);
            price += RangeEncoder.getPrice1(_isRep0Long[(state << LzmaState.kNumPosStatesBitsMax) + posState]);
        } else {
            price = RangeEncoder.getPrice1(_isRepG0[state]);
            if (repIndex == 1) {
                price += RangeEncoder.getPrice0(_isRepG1[state]);
            } else {
                price += RangeEncoder.getPrice1(_isRepG1[state]);
                price += RangeEncoder.getPrice(_isRepG2[state], repIndex - 2);
            }
        }
        return price;
    }

    int getRepPrice(int repIndex, int len, int state, int posState) {
        int price = _repMatchLenEncoder.getPrice(len - LzmaState.kMatchMinLen, posState);
        return price + getPureRepPrice(repIndex, state, posState);
    }

    int getPosLenPrice(int pos, int len, int posState) {
        int price;
        int lenToPosState = LzmaState.getLenToPosState(len);
        if (pos < LzmaState.kNumFullDistances) {
            price = _distancesPrices[(lenToPosState * LzmaState.kNumFullDistances) + pos];
        } else {
            price = _posSlotPrices[(lenToPosState << LzmaState.kNumPosSlotBits) + getPosSlot2(pos)]
                    + _alignPrices[pos & LzmaState.kAlignMask];
        }
        return price + _lenEncoder.getPrice(len - LzmaState.kMatchMinLen, posState);
    }

    int backward(int cur) {
        _optimumEndIndex = cur;
        int posMem = _optimum[cur].PosPrev;
        int backMem = _optimum[cur].BackPrev;
        do {
            if (_optimum[cur].Prev1IsChar) {
                _optimum[posMem].makeAsChar();
                _optimum[posMem].PosPrev = posMem - 1;
                if (_optimum[cur].Prev2) {
                    _optimum[posMem - 1].Prev1IsChar = false;
                    _optimum[posMem - 1].PosPrev = _optimum[cur].PosPrev2;
                    _optimum[posMem - 1].BackPrev = _optimum[cur].BackPrev2;
                }
            }
            int posPrev = posMem;
            int backCur = backMem;

            backMem = _optimum[posPrev].BackPrev;
            posMem = _optimum[posPrev].PosPrev;

            _optimum[posPrev].BackPrev = backCur;
            _optimum[posPrev].PosPrev = cur;
            cur = posPrev;
        } while (cur > 0);
        backRes = _optimum[0].BackPrev;
        _optimumCurrentIndex = _optimum[0].PosPrev;
        return _optimumCurrentIndex;
    }
    int[] reps = new int[LzmaState.kNumRepDistances];
    int[] repLens = new int[LzmaState.kNumRepDistances];
    int backRes;

    int getOptimum(int position) throws IOException {
        if (_optimumEndIndex != _optimumCurrentIndex) {
            int lenRes = _optimum[_optimumCurrentIndex].PosPrev - _optimumCurrentIndex;
            backRes = _optimum[_optimumCurrentIndex].BackPrev;
            _optimumCurrentIndex = _optimum[_optimumCurrentIndex].PosPrev;
            return lenRes;
        }
        _optimumCurrentIndex = _optimumEndIndex = 0;

        int lenMain, numDistancePairs;
        if (!_longestMatchWasFound) {
            lenMain = readMatchDistances();
        } else {
            lenMain = _longestMatchLength;
            _longestMatchWasFound = false;
        }
        numDistancePairs = _numDistancePairs;

        int numAvailableBytes = _matchFinder.getNumAvailableBytes() + 1;
        if (numAvailableBytes < 2) {
            backRes = -1;
            return 1;
        }
        if (numAvailableBytes > LzmaState.kMatchMaxLen) {
            numAvailableBytes = LzmaState.kMatchMaxLen;
        }

        int repMaxIndex = 0;
        int i;
        for (i = 0; i < LzmaState.kNumRepDistances; i++) {
            reps[i] = _repDistances[i];
            repLens[i] = _matchFinder.getMatchLen(0 - 1, reps[i], LzmaState.kMatchMaxLen);
            if (repLens[i] > repLens[repMaxIndex]) {
                repMaxIndex = i;
            }
        }
        if (repLens[repMaxIndex] >= _numFastBytes) {
            backRes = repMaxIndex;
            int lenRes = repLens[repMaxIndex];
            movePos(lenRes - 1);
            return lenRes;
        }

        if (lenMain >= _numFastBytes) {
            backRes = _matchDistances[numDistancePairs - 1] + LzmaState.kNumRepDistances;
            movePos(lenMain - 1);
            return lenMain;
        }

        byte currentByte = _matchFinder.getIndexByte(0 - 1);
        byte matchByte = _matchFinder.getIndexByte(0 - _repDistances[0] - 1 - 1);

        if (lenMain < 2 && currentByte != matchByte && repLens[repMaxIndex] < 2) {
            backRes = -1;
            return 1;
        }

        _optimum[0].State = _state;

        int posState = (position & _posStateMask);

        _optimum[1].Price = RangeEncoder.getPrice0(_isMatch[(_state << LzmaState.kNumPosStatesBitsMax) + posState])
                + _literalEncoder.getSubCoder(position, _previousByte).getPrice(!LzmaState.stateIsCharState(_state), matchByte, currentByte);
        _optimum[1].makeAsChar();

        int matchPrice = RangeEncoder.getPrice1(_isMatch[(_state << LzmaState.kNumPosStatesBitsMax) + posState]);
        int repMatchPrice = matchPrice + RangeEncoder.getPrice1(_isRep[_state]);

        if (matchByte == currentByte) {
            int shortRepPrice = repMatchPrice + getRepLen1Price(_state, posState);
            if (shortRepPrice < _optimum[1].Price) {
                _optimum[1].Price = shortRepPrice;
                _optimum[1].makeAsShortRep();
            }
        }

        int lenEnd = ((lenMain >= repLens[repMaxIndex]) ? lenMain : repLens[repMaxIndex]);

        if (lenEnd < 2) {
            backRes = _optimum[1].BackPrev;
            return 1;
        }

        _optimum[1].PosPrev = 0;

        _optimum[0].Backs0 = reps[0];
        _optimum[0].Backs1 = reps[1];
        _optimum[0].Backs2 = reps[2];
        _optimum[0].Backs3 = reps[3];

        int len = lenEnd;
        do {
            _optimum[len--].Price = kIfinityPrice;
        } while (len >= 2);

        for (i = 0; i < LzmaState.kNumRepDistances; i++) {
            int repLen = repLens[i];
            if (repLen < 2) {
                continue;
            }
            int price = repMatchPrice + getPureRepPrice(i, _state, posState);
            do {
                int curAndLenPrice = price + _repMatchLenEncoder.getPrice(repLen - 2, posState);
                Optimal optimum = _optimum[repLen];
                if (curAndLenPrice < optimum.Price) {
                    optimum.Price = curAndLenPrice;
                    optimum.PosPrev = 0;
                    optimum.BackPrev = i;
                    optimum.Prev1IsChar = false;
                }
            } while (--repLen >= 2);
        }

        int normalMatchPrice = matchPrice + RangeEncoder.getPrice0(_isRep[_state]);

        len = ((repLens[0] >= 2) ? repLens[0] + 1 : 2);
        if (len <= lenMain) {
            int offs = 0;
            while (len > _matchDistances[offs]) {
                offs += 2;
            }
            for (;; len++) {
                int distance = _matchDistances[offs + 1];
                int curAndLenPrice = normalMatchPrice + getPosLenPrice(distance, len, posState);
                Optimal optimum = _optimum[len];
                if (curAndLenPrice < optimum.Price) {
                    optimum.Price = curAndLenPrice;
                    optimum.PosPrev = 0;
                    optimum.BackPrev = distance + LzmaState.kNumRepDistances;
                    optimum.Prev1IsChar = false;
                }
                if (len == _matchDistances[offs]) {
                    offs += 2;
                    if (offs == numDistancePairs) {
                        break;
                    }
                }
            }
        }

        int cur = 0;

        while (true) {
            cur++;
            if (cur == lenEnd) {
                return backward(cur);
            }
            int newLen = readMatchDistances();
            numDistancePairs = _numDistancePairs;
            if (newLen >= _numFastBytes) {

                _longestMatchLength = newLen;
                _longestMatchWasFound = true;
                return backward(cur);
            }
            position++;
            int posPrev = _optimum[cur].PosPrev;
            int state;
            if (_optimum[cur].Prev1IsChar) {
                posPrev--;
                if (_optimum[cur].Prev2) {
                    state = _optimum[_optimum[cur].PosPrev2].State;
                    if (_optimum[cur].BackPrev2 < LzmaState.kNumRepDistances) {
                        state = LzmaState.stateUpdateRep(state);
                    } else {
                        state = LzmaState.stateUpdateMatch(state);
                    }
                } else {
                    state = _optimum[posPrev].State;
                }
                state = LzmaState.stateUpdateChar(state);
            } else {
                state = _optimum[posPrev].State;
            }
            if (posPrev == cur - 1) {
                if (_optimum[cur].isShortRep()) {
                    state = LzmaState.stateUpdateShortRep(state);
                } else {
                    state = LzmaState.stateUpdateChar(state);
                }
            } else {
                int pos;
                if (_optimum[cur].Prev1IsChar && _optimum[cur].Prev2) {
                    posPrev = _optimum[cur].PosPrev2;
                    pos = _optimum[cur].BackPrev2;
                    state = LzmaState.stateUpdateRep(state);
                } else {
                    pos = _optimum[cur].BackPrev;
                    if (pos < LzmaState.kNumRepDistances) {
                        state = LzmaState.stateUpdateRep(state);
                    } else {
                        state = LzmaState.stateUpdateMatch(state);
                    }
                }
                Optimal opt = _optimum[posPrev];
                if (pos < LzmaState.kNumRepDistances) {
                    if (pos == 0) {
                        reps[0] = opt.Backs0;
                        reps[1] = opt.Backs1;
                        reps[2] = opt.Backs2;
                        reps[3] = opt.Backs3;
                    } else if (pos == 1) {
                        reps[0] = opt.Backs1;
                        reps[1] = opt.Backs0;
                        reps[2] = opt.Backs2;
                        reps[3] = opt.Backs3;
                    } else if (pos == 2) {
                        reps[0] = opt.Backs2;
                        reps[1] = opt.Backs0;
                        reps[2] = opt.Backs1;
                        reps[3] = opt.Backs3;
                    } else {
                        reps[0] = opt.Backs3;
                        reps[1] = opt.Backs0;
                        reps[2] = opt.Backs1;
                        reps[3] = opt.Backs2;
                    }
                } else {
                    reps[0] = (pos - LzmaState.kNumRepDistances);
                    reps[1] = opt.Backs0;
                    reps[2] = opt.Backs1;
                    reps[3] = opt.Backs2;
                }
            }
            _optimum[cur].State = state;
            _optimum[cur].Backs0 = reps[0];
            _optimum[cur].Backs1 = reps[1];
            _optimum[cur].Backs2 = reps[2];
            _optimum[cur].Backs3 = reps[3];
            int curPrice = _optimum[cur].Price;

            currentByte = _matchFinder.getIndexByte(0 - 1);
            matchByte = _matchFinder.getIndexByte(0 - reps[0] - 1 - 1);

            posState = (position & _posStateMask);

            int curAnd1Price = curPrice
                    + RangeEncoder.getPrice0(_isMatch[(state << LzmaState.kNumPosStatesBitsMax) + posState])
                    + _literalEncoder.getSubCoder(position, _matchFinder.getIndexByte(0 - 2)).
                    getPrice(!LzmaState.stateIsCharState(state), matchByte, currentByte);

            Optimal nextOptimum = _optimum[cur + 1];

            boolean nextIsChar = false;
            if (curAnd1Price < nextOptimum.Price) {
                nextOptimum.Price = curAnd1Price;
                nextOptimum.PosPrev = cur;
                nextOptimum.makeAsChar();
                nextIsChar = true;
            }

            matchPrice = curPrice + RangeEncoder.getPrice1(_isMatch[(state << LzmaState.kNumPosStatesBitsMax) + posState]);
            repMatchPrice = matchPrice + RangeEncoder.getPrice1(_isRep[state]);

            if (matchByte == currentByte
                    && !(nextOptimum.PosPrev < cur && nextOptimum.BackPrev == 0)) {
                int shortRepPrice = repMatchPrice + getRepLen1Price(state, posState);
                if (shortRepPrice <= nextOptimum.Price) {
                    nextOptimum.Price = shortRepPrice;
                    nextOptimum.PosPrev = cur;
                    nextOptimum.makeAsShortRep();
                    nextIsChar = true;
                }
            }

            int numAvailableBytesFull = _matchFinder.getNumAvailableBytes() + 1;
            numAvailableBytesFull = Math.min(kNumOpts - 1 - cur, numAvailableBytesFull);
            numAvailableBytes = numAvailableBytesFull;

            if (numAvailableBytes < 2) {
                continue;
            }
            if (numAvailableBytes > _numFastBytes) {
                numAvailableBytes = _numFastBytes;
            }
            if (!nextIsChar && matchByte != currentByte) {
                // try Literal + rep0
                int t = Math.min(numAvailableBytesFull - 1, _numFastBytes);
                int lenTest2 = _matchFinder.getMatchLen(0, reps[0], t);
                if (lenTest2 >= 2) {
                    int state2 = LzmaState.stateUpdateChar(state);

                    int posStateNext = (position + 1) & _posStateMask;
                    int nextRepMatchPrice = curAnd1Price
                            + RangeEncoder.getPrice1(_isMatch[(state2 << LzmaState.kNumPosStatesBitsMax) + posStateNext])
                            + RangeEncoder.getPrice1(_isRep[state2]);
                    {
                        int offset = cur + 1 + lenTest2;
                        while (lenEnd < offset) {
                            _optimum[++lenEnd].Price = kIfinityPrice;
                        }
                        int curAndLenPrice = nextRepMatchPrice + getRepPrice(
                                0, lenTest2, state2, posStateNext);
                        Optimal optimum = _optimum[offset];
                        if (curAndLenPrice < optimum.Price) {
                            optimum.Price = curAndLenPrice;
                            optimum.PosPrev = cur + 1;
                            optimum.BackPrev = 0;
                            optimum.Prev1IsChar = true;
                            optimum.Prev2 = false;
                        }
                    }
                }
            }

            int startLen = 2; // speed optimization 

            for (int repIndex = 0; repIndex < LzmaState.kNumRepDistances; repIndex++) {
                int lenTest = _matchFinder.getMatchLen(0 - 1, reps[repIndex], numAvailableBytes);
                if (lenTest < 2) {
                    continue;
                }
                int lenTestTemp = lenTest;
                do {
                    while (lenEnd < cur + lenTest) {
                        _optimum[++lenEnd].Price = kIfinityPrice;
                    }
                    int curAndLenPrice = repMatchPrice + getRepPrice(repIndex, lenTest, state, posState);
                    Optimal optimum = _optimum[cur + lenTest];
                    if (curAndLenPrice < optimum.Price) {
                        optimum.Price = curAndLenPrice;
                        optimum.PosPrev = cur;
                        optimum.BackPrev = repIndex;
                        optimum.Prev1IsChar = false;
                    }
                } while (--lenTest >= 2);
                lenTest = lenTestTemp;

                if (repIndex == 0) {
                    startLen = lenTest + 1;
                }

                // if (_maxMode)
                if (lenTest < numAvailableBytesFull) {
                    int t = Math.min(numAvailableBytesFull - 1 - lenTest, _numFastBytes);
                    int lenTest2 = _matchFinder.getMatchLen(lenTest, reps[repIndex], t);
                    if (lenTest2 >= 2) {
                        int state2 = LzmaState.stateUpdateRep(state);

                        int posStateNext = (position + lenTest) & _posStateMask;
                        int curAndLenCharPrice =
                                repMatchPrice + getRepPrice(repIndex, lenTest, state, posState)
                                + RangeEncoder.getPrice0(_isMatch[(state2 << LzmaState.kNumPosStatesBitsMax) + posStateNext])
                                + _literalEncoder.getSubCoder(position + lenTest,
                                _matchFinder.getIndexByte(lenTest - 1 - 1)).getPrice(true,
                                _matchFinder.getIndexByte(lenTest - 1 - (reps[repIndex] + 1)),
                                _matchFinder.getIndexByte(lenTest - 1));
                        state2 = LzmaState.stateUpdateChar(state2);
                        posStateNext = (position + lenTest + 1) & _posStateMask;
                        int nextMatchPrice = curAndLenCharPrice + RangeEncoder.getPrice1(_isMatch[(state2 << LzmaState.kNumPosStatesBitsMax) + posStateNext]);
                        int nextRepMatchPrice = nextMatchPrice + RangeEncoder.getPrice1(_isRep[state2]);

                        // for(; lenTest2 >= 2; lenTest2--)
                        {
                            int offset = lenTest + 1 + lenTest2;
                            while (lenEnd < cur + offset) {
                                _optimum[++lenEnd].Price = kIfinityPrice;
                            }
                            int curAndLenPrice = nextRepMatchPrice + getRepPrice(0, lenTest2, state2, posStateNext);
                            Optimal optimum = _optimum[cur + offset];
                            if (curAndLenPrice < optimum.Price) {
                                optimum.Price = curAndLenPrice;
                                optimum.PosPrev = cur + lenTest + 1;
                                optimum.BackPrev = 0;
                                optimum.Prev1IsChar = true;
                                optimum.Prev2 = true;
                                optimum.PosPrev2 = cur;
                                optimum.BackPrev2 = repIndex;
                            }
                        }
                    }
                }
            }

            if (newLen > numAvailableBytes) {
                newLen = numAvailableBytes;
                for (numDistancePairs = 0; newLen > _matchDistances[numDistancePairs]; numDistancePairs += 2) ;
                _matchDistances[numDistancePairs] = newLen;
                numDistancePairs += 2;
            }
            if (newLen >= startLen) {
                normalMatchPrice = matchPrice + RangeEncoder.getPrice0(_isRep[state]);
                while (lenEnd < cur + newLen) {
                    _optimum[++lenEnd].Price = kIfinityPrice;
                }

                int offs = 0;
                while (startLen > _matchDistances[offs]) {
                    offs += 2;
                }

                for (int lenTest = startLen;; lenTest++) {
                    int curBack = _matchDistances[offs + 1];
                    int curAndLenPrice = normalMatchPrice + getPosLenPrice(curBack, lenTest, posState);
                    Optimal optimum = _optimum[cur + lenTest];
                    if (curAndLenPrice < optimum.Price) {
                        optimum.Price = curAndLenPrice;
                        optimum.PosPrev = cur;
                        optimum.BackPrev = curBack + LzmaState.kNumRepDistances;
                        optimum.Prev1IsChar = false;
                    }

                    if (lenTest == _matchDistances[offs]) {
                        if (lenTest < numAvailableBytesFull) {
                            int t = Math.min(numAvailableBytesFull - 1 - lenTest, _numFastBytes);
                            int lenTest2 = _matchFinder.getMatchLen(lenTest, curBack, t);
                            if (lenTest2 >= 2) {
                                int state2 = LzmaState.stateUpdateMatch(state);

                                int posStateNext = (position + lenTest) & _posStateMask;
                                int curAndLenCharPrice = curAndLenPrice
                                        + RangeEncoder.getPrice0(_isMatch[(state2 << LzmaState.kNumPosStatesBitsMax) + posStateNext])
                                        + _literalEncoder.getSubCoder(position + lenTest,
                                        _matchFinder.getIndexByte(lenTest - 1 - 1)).
                                        getPrice(true,
                                        _matchFinder.getIndexByte(lenTest - (curBack + 1) - 1),
                                        _matchFinder.getIndexByte(lenTest - 1));
                                state2 = LzmaState.stateUpdateChar(state2);
                                posStateNext = (position + lenTest + 1) & _posStateMask;
                                int nextMatchPrice = curAndLenCharPrice + RangeEncoder.getPrice1(_isMatch[(state2 << LzmaState.kNumPosStatesBitsMax) + posStateNext]);
                                int nextRepMatchPrice = nextMatchPrice + RangeEncoder.getPrice1(_isRep[state2]);

                                int offset = lenTest + 1 + lenTest2;
                                while (lenEnd < cur + offset) {
                                    _optimum[++lenEnd].Price = kIfinityPrice;
                                }
                                curAndLenPrice = nextRepMatchPrice + getRepPrice(0, lenTest2, state2, posStateNext);
                                optimum = _optimum[cur + offset];
                                if (curAndLenPrice < optimum.Price) {
                                    optimum.Price = curAndLenPrice;
                                    optimum.PosPrev = cur + lenTest + 1;
                                    optimum.BackPrev = 0;
                                    optimum.Prev1IsChar = true;
                                    optimum.Prev2 = true;
                                    optimum.PosPrev2 = cur;
                                    optimum.BackPrev2 = curBack + LzmaState.kNumRepDistances;
                                }
                            }
                        }
                        offs += 2;
                        if (offs == numDistancePairs) {
                            break;
                        }
                    }
                }
            }
        }
    }

    boolean changePair(int smallDist, int bigDist) {
        int kDif = 7;
        return (smallDist < (1 << (32 - kDif)) && bigDist >= (smallDist << kDif));
    }

    void writeEndMarker(int posState) throws IOException {
        if (!_writeEndMark) {
            return;
        }

        _rangeEncoder.encode(_isMatch, (_state << LzmaState.kNumPosStatesBitsMax) + posState, 1);
        _rangeEncoder.encode(_isRep, _state, 0);
        _state = LzmaState.stateUpdateMatch(_state);
        int len = LzmaState.kMatchMinLen;
        _lenEncoder.encode(_rangeEncoder, len - LzmaState.kMatchMinLen, posState);
        int posSlot = (1 << LzmaState.kNumPosSlotBits) - 1;
        int lenToPosState = LzmaState.getLenToPosState(len);
        _posSlotEncoder[lenToPosState].encode(_rangeEncoder, posSlot);
        int footerBits = 30;
        int posReduced = (1 << footerBits) - 1;
        _rangeEncoder.encodeDirectBits(posReduced >> LzmaState.kNumAlignBits, footerBits - LzmaState.kNumAlignBits);
        _posAlignEncoder.reverseEncode(_rangeEncoder, posReduced & LzmaState.kAlignMask);
    }

    void flush(int nowPos) throws IOException {
        releaseMFStream();
        writeEndMarker(nowPos & _posStateMask);
        _rangeEncoder.flushData();
        _rangeEncoder.flushStream();
    }

    public void codeOneBlock(long[] inSize, long[] outSize, boolean[] finished) throws IOException {
        inSize[0] = 0;
        outSize[0] = 0;
        finished[0] = true;

        if (_inStream != null) {
            _matchFinder.setStream(_inStream);
            _matchFinder.init();
            _needReleaseMFStream = true;
            _inStream = null;
        }

        if (_finished) {
            return;
        }
        _finished = true;


        long progressPosValuePrev = nowPos64;
        if (nowPos64 == 0) {
            if (_matchFinder.getNumAvailableBytes() == 0) {
                flush((int) nowPos64);
                return;
            }

            readMatchDistances();
            int posState = (int) (nowPos64) & _posStateMask;
            _rangeEncoder.encode(_isMatch, (_state << LzmaState.kNumPosStatesBitsMax) + posState, 0);
            _state = LzmaState.stateUpdateChar(_state);
            byte curByte = _matchFinder.getIndexByte(0 - _additionalOffset);
            _literalEncoder.getSubCoder((int) (nowPos64), _previousByte).encode(_rangeEncoder, curByte);
            _previousByte = curByte;
            _additionalOffset--;
            nowPos64++;
        }
        if (_matchFinder.getNumAvailableBytes() == 0) {
            flush((int) nowPos64);
            return;
        }
        while (true) {

            int len = getOptimum((int) nowPos64);
            int pos = backRes;
            int posState = ((int) nowPos64) & _posStateMask;
            int complexState = (_state << LzmaState.kNumPosStatesBitsMax) + posState;
            if (len == 1 && pos == -1) {
                _rangeEncoder.encode(_isMatch, complexState, 0);
                byte curByte = _matchFinder.getIndexByte((int) (0 - _additionalOffset));
                LiteralEncoder.Encoder2 subCoder = _literalEncoder.getSubCoder((int) nowPos64, _previousByte);
                if (!LzmaState.stateIsCharState(_state)) {
                    byte matchByte = _matchFinder.getIndexByte((int) (0 - _repDistances[0] - 1 - _additionalOffset));
                    subCoder.encodeMatched(_rangeEncoder, matchByte, curByte);
                } else {
                    subCoder.encode(_rangeEncoder, curByte);
                }
                _previousByte = curByte;
                _state = LzmaState.stateUpdateChar(_state);
            } else {
                _rangeEncoder.encode(_isMatch, complexState, 1);
                if (pos < LzmaState.kNumRepDistances) {
                    _rangeEncoder.encode(_isRep, _state, 1);
                    if (pos == 0) {
                        _rangeEncoder.encode(_isRepG0, _state, 0);
                        if (len == 1) {
                            _rangeEncoder.encode(_isRep0Long, complexState, 0);
                        } else {
                            _rangeEncoder.encode(_isRep0Long, complexState, 1);
                        }
                    } else {
                        _rangeEncoder.encode(_isRepG0, _state, 1);
                        if (pos == 1) {
                            _rangeEncoder.encode(_isRepG1, _state, 0);
                        } else {
                            _rangeEncoder.encode(_isRepG1, _state, 1);
                            _rangeEncoder.encode(_isRepG2, _state, pos - 2);
                        }
                    }
                    if (len == 1) {
                        _state = LzmaState.stateUpdateShortRep(_state);
                    } else {
                        _repMatchLenEncoder.encode(_rangeEncoder, len - LzmaState.kMatchMinLen, posState);
                        _state = LzmaState.stateUpdateRep(_state);
                    }
                    int distance = _repDistances[pos];
                    if (pos != 0) {
                        for (int i = pos; i >= 1; i--) {
                            _repDistances[i] = _repDistances[i - 1];
                        }
                        _repDistances[0] = distance;
                    }
                } else {
                    _rangeEncoder.encode(_isRep, _state, 0);
                    _state = LzmaState.stateUpdateMatch(_state);
                    _lenEncoder.encode(_rangeEncoder, len - LzmaState.kMatchMinLen, posState);
                    pos -= LzmaState.kNumRepDistances;
                    int posSlot = getPosSlot(pos);
                    int lenToPosState = LzmaState.getLenToPosState(len);
                    _posSlotEncoder[lenToPosState].encode(_rangeEncoder, posSlot);

                    if (posSlot >= LzmaState.kStartPosModelIndex) {
                        int footerBits = (int) ((posSlot >> 1) - 1);
                        int baseVal = ((2 | (posSlot & 1)) << footerBits);
                        int posReduced = pos - baseVal;

                        if (posSlot < LzmaState.kEndPosModelIndex) {
                            BitTreeEncoder.reverseEncode(_posEncoders,
                                    baseVal - posSlot - 1, _rangeEncoder, footerBits, posReduced);
                        } else {
                            _rangeEncoder.encodeDirectBits(posReduced >> LzmaState.kNumAlignBits, footerBits - LzmaState.kNumAlignBits);
                            _posAlignEncoder.reverseEncode(_rangeEncoder, posReduced & LzmaState.kAlignMask);
                            _alignPriceCount++;
                        }
                    }
                    int distance = pos;
                    for (int i = LzmaState.kNumRepDistances - 1; i >= 1; i--) {
                        _repDistances[i] = _repDistances[i - 1];
                    }
                    _repDistances[0] = distance;
                    _matchPriceCount++;
                }
                _previousByte = _matchFinder.getIndexByte(len - 1 - _additionalOffset);
            }
            _additionalOffset -= len;
            nowPos64 += len;
            if (_additionalOffset == 0) {
                // if (!_fastMode)
                if (_matchPriceCount >= (1 << 7)) {
                    fillDistancesPrices();
                }
                if (_alignPriceCount >= LzmaState.kAlignTableSize) {
                    fillAlignPrices();
                }
                inSize[0] = nowPos64;
                outSize[0] = _rangeEncoder.getProcessedSizeAdd();
                if (_matchFinder.getNumAvailableBytes() == 0) {
                    flush((int) nowPos64);
                    return;
                }

                if (nowPos64 - progressPosValuePrev >= (1 << 12)) {
                    _finished = false;
                    finished[0] = false;
                    return;
                }
            }
        }
    }

    void releaseMFStream() {
        if (_matchFinder != null && _needReleaseMFStream) {
            _matchFinder.releaseStream();
            _needReleaseMFStream = false;
        }
    }

    void setOutStream(OutputStream outStream) {
        _rangeEncoder.setStream(outStream);
    }

    void releaseOutStream() {
        _rangeEncoder.releaseStream();
    }

    void releaseStreams() {
        releaseMFStream();
        releaseOutStream();
    }

    void setStreams(InputStream inStream, OutputStream outStream) {
        _inStream = inStream;
        _finished = false;
        create();
        setOutStream(outStream);
        init();

        // if (!_fastMode)
        {
            fillDistancesPrices();
            fillAlignPrices();
        }

        _lenEncoder.setTableSize(_numFastBytes + 1 - LzmaState.kMatchMinLen);
        _lenEncoder.updateTables(1 << _posStateBits);
        _repMatchLenEncoder.setTableSize(_numFastBytes + 1 - LzmaState.kMatchMinLen);
        _repMatchLenEncoder.updateTables(1 << _posStateBits);

        nowPos64 = 0;
    }
    private long[] processedInSize = new long[1];
    private long[] processedOutSize = new long[1];
    private boolean[] finished = new boolean[1];

    public void code(InputStream inStream, OutputStream outStream) throws IOException {
        _needReleaseMFStream = false;
        try {
            setStreams(inStream, outStream);
            while (true) {
                codeOneBlock(processedInSize, processedOutSize, finished);
                if (finished[0]) {
                    return;
                }
            }
        } finally {
            releaseStreams();
        }
    }
    
    public static final int kPropSize = 5;
    
    public byte[] getCoderProperties() {
        byte[] properties = new byte[kPropSize];
        properties[0] = (byte) ((_posStateBits * 5 + _numLiteralPosStateBits) * 9 + _numLiteralContextBits);
        for (int i = 0; i < 4; i++) {
            properties[1 + i] = (byte) (_dictionarySize >> (8 * i));
        }
        return properties;
    }

    public void writeCoderProperties(OutputStream outStream) throws IOException {
        byte[] properties = getCoderProperties();
        outStream.write(properties, 0, kPropSize);
    }
    private int[] tempPrices = new int[LzmaState.kNumFullDistances];
    private int _matchPriceCount;

    void fillDistancesPrices() {
        for (int i = LzmaState.kStartPosModelIndex; i < LzmaState.kNumFullDistances; i++) {
            int posSlot = getPosSlot(i);
            int footerBits = (int) ((posSlot >> 1) - 1);
            int baseVal = ((2 | (posSlot & 1)) << footerBits);
            tempPrices[i] = BitTreeEncoder.reverseGetPrice(_posEncoders,
                    baseVal - posSlot - 1, footerBits, i - baseVal);
        }

        for (int lenToPosState = 0; lenToPosState < LzmaState.kNumLenToPosStates; lenToPosState++) {
            int posSlot;
            BitTreeEncoder encoder = _posSlotEncoder[lenToPosState];

            int st = (lenToPosState << LzmaState.kNumPosSlotBits);
            for (posSlot = 0; posSlot < _distTableSize; posSlot++) {
                _posSlotPrices[st + posSlot] = encoder.getPrice(posSlot);
            }
            for (posSlot = LzmaState.kEndPosModelIndex; posSlot < _distTableSize; posSlot++) {
                _posSlotPrices[st + posSlot] += ((((posSlot >> 1) - 1) - LzmaState.kNumAlignBits) << RangeEncoder.kNumBitPriceShiftBits);
            }

            int st2 = lenToPosState * LzmaState.kNumFullDistances;
            int i;
            for (i = 0; i < LzmaState.kStartPosModelIndex; i++) {
                _distancesPrices[st2 + i] = _posSlotPrices[st + i];
            }
            for (; i < LzmaState.kNumFullDistances; i++) {
                _distancesPrices[st2 + i] = _posSlotPrices[st + getPosSlot(i)] + tempPrices[i];
            }
        }
        _matchPriceCount = 0;
    }

    void fillAlignPrices() {
        for (int i = 0; i < LzmaState.kAlignTableSize; i++) {
            _alignPrices[i] = _posAlignEncoder.reverseGetPrice(i);
        }
        _alignPriceCount = 0;
    }

    public boolean setAlgorithm(int algorithm) {
        /*
        _fastMode = (algorithm == 0);
        _maxMode = (algorithm >= 2);
         */
        return true;
    }

    public boolean setDictionarySize(int dictionarySize) {
        int kDicLogSizeMaxCompress = 29;
        if (dictionarySize < (1 << LzmaState.kDicLogSizeMin) || dictionarySize > (1 << kDicLogSizeMaxCompress)) {
            return false;
        }
        _dictionarySize = dictionarySize;
        int dicLogSize;
        for (dicLogSize = 0; dictionarySize > (1 << dicLogSize); dicLogSize++) ;
        _distTableSize = dicLogSize * 2;
        return true;
    }

    public boolean setNumFastBytes(int numFastBytes) {
        if (numFastBytes < 5 || numFastBytes > LzmaState.kMatchMaxLen) {
            return false;
        }
        _numFastBytes = numFastBytes;
        return true;
    }

    public boolean setMatchFinder(int matchFinderIndex) {
        if (matchFinderIndex < 0 || matchFinderIndex > 2) {
            return false;
        }
        int matchFinderIndexPrev = _matchFinderType;
        _matchFinderType = matchFinderIndex;
        if (_matchFinder != null && matchFinderIndexPrev != _matchFinderType) {
            _dictionarySizePrev = -1;
            _matchFinder = null;
        }
        return true;
    }

    public boolean setLcLpPb(int lc, int lp, int pb) {
        if (lp < 0 || lp > LzmaState.kNumLitPosStatesBitsEncodingMax
                || lc < 0 || lc > LzmaState.kNumLitContextBitsMax
                || pb < 0 || pb > LzmaState.kNumPosStatesBitsEncodingMax) {
            return false;
        }
        _numLiteralPosStateBits = lp;
        _numLiteralContextBits = lc;
        _posStateBits = pb;
        _posStateMask = ((1) << _posStateBits) - 1;
        return true;
    }

    public void setEndMarkerMode(boolean endMarkerMode) {
        _writeEndMark = endMarkerMode;
    }
}
