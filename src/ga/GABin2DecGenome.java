package ga;

/**
 * User: Oleg
 * Date: Jul 7, 2004
 * Time: 4:09:57 PM
 * Description:
 * ----------------------------------------------------------------------------
 * The phenotype does reference counting, so we can make a copy of it for our
 * own use and we don't have to worry about extra overhead.
 * ----------------------------------------------------------------------------
 */

public final class GABin2DecGenome extends GA1DBinaryStringGenome {
    private GABin2DecPhenotype ptype;
    private BinaryEncoder encode = new GABinCvt();   // function we use to encode the bits
    private BinaryDecoder decode = new GABinCvt();   // function we use to decode the bits

    public String className() {
        return "GABin2DecGenome";
    }

    public int classID() {
        return Bin2DecGenome;
    }

    private GABin2DecGenome(final GABin2DecPhenotype p) {
        this(p, null);
    }

    public GABin2DecGenome(final GABin2DecPhenotype p, final Evaluator f) {
        this(p, f, null);
    }

    public GABin2DecGenome(final GABin2DecPhenotype p, final Evaluator f, final Object u) {
        super(p.size(), f, u);
        ptype = new GABin2DecPhenotype(p);
        comparator(_BitComparator);
        encoder(encode);
        decoder(decode);
    }

    GABin2DecGenome(final GABin2DecGenome orig) {
        super(orig.bs.sz);
        ptype = null;
        copy(orig);
    }

    GA1DBinaryStringGenome assign(final GAGenome arg) {
        copy(arg);
        return this;
    }

    void delete() {
        ptype.delete();
    }

    public Object clone() {
        return clone(CloneMethod.CONTENTS);
    }

    /**
     * We shouldn't have to worry about our superclass's data members for the
     * attributes part here, but there is no 'copy attributes' function, so we
     * end up doing it.  bummer.
     */
    Object clone(final int flag) {
        final GABin2DecGenome cpy = new GABin2DecGenome(ptype);
        if (flag == CloneMethod.CONTENTS) {
            cpy.copy(this);
        } else {
            cpy.copy(this);
            cpy.maxX = maxX;
            cpy.minX = minX;
            cpy.ptype = ptype;
            cpy.encode = encode;
            cpy.decode = decode;
        }
        return cpy;
    }

    void copy(final GAGenome orig) {
        if (orig == this) {
            return;
        }
        final GABin2DecGenome c = (GABin2DecGenome) orig;
        if (c != null) {
            super.copy(c);
            encode = c.encode;
            decode = c.decode;
            if (ptype != null) {
                ptype = c.ptype;
            } else {
                ptype = new GABin2DecPhenotype(c.ptype);
            }
        }
    }

    /**
     * For two bin2dec genomes to be equal they must have the same bits AND the
     * same phenotypes.
     */
    boolean equal(final GAGenome g) {
        final GABin2DecGenome b = (GABin2DecGenome) g;
        return super.equal(b) && ptype == b.ptype;
    }

    boolean notequal(final GAGenome g) {
        final GABin2DecGenome b = (GABin2DecGenome) g;
        return super.notequal(b) || ptype != b.ptype;
    }

    /**
     * The phenotype does reference counting, so its ok to keep our own copy of
     * the phenotype.  So all we have to do here is copy the one that is passed
     * to us, then modify the bit string to accomodate the new mapping.
     */
    GABin2DecPhenotype phenotypes(final GABin2DecPhenotype p) {
        ptype = p;
        super.resize(p.size());
        return ptype;
    }

    GABin2DecPhenotype phenotypes() {
        return ptype;
    }

    public int nPhenotypes() {
        return ptype.nPhenotypes();
    }

    /**
     * Set the bits of the binary string based on the decimal value that is passed
     * to us.  Notice that the number you pass may or may not be set properly.  It
     * depends on the resolution defined in the phenotype.  If you didn't define
     * enough resolution, then there may be no way to represent the number.
     * We round off to the closest representable value, then return the number
     * that we actually entered (the rounded value).
     * ** this is dangerous!  we're accessing the superclass' data representation
     * directly, so if the representation changes to a bit stream, this will break.
     * If someone tries to set the phenotype beyond the bounds, we post an error
     * then set the bits to the closer bound.
     */
    float phenotype(final int n, float val) {
        if (n >= ptype.nPhenotypes()) {
            GAError.GAErr(className(), "phenotype", GAError.gaErrBadPhenotypeID);
            return val;
        }
        if (val < ptype.min(n) || val > ptype.max(n)) {
            GAError.GAErr(className(), "phenotype", GAError.gaErrBadPhenotypeValue);
            val = val < ptype.min(n) ? ptype.min(n) : ptype.max(n);
        }
        final float[] oval = new float[]{val};
        encode.encode(oval, bs.data, ptype.offset(n), ptype.length(n), ptype.min(n), ptype.max(n));
        return oval[0];
    }

    /**
     * We access the data string directly here.  This could be dangerous (if the
     * bitstream ever changes on us it will affect the way this method sees the
     * data string).
     * Eventually we may need to cache the decimal values in an array of floats,
     * but for now we call the converter routine every time each phenotype is
     * requested.
     */
    public float phenotype(final int n) {
        if (n >= ptype.nPhenotypes()) {
            GAError.GAErr(className(), "phenotype", GAError.gaErrBadPhenotypeID);
            return (float) 0.0;
        }
        final float[] val = new float[1];
        decode.decode(val, bs.data, ptype.offset(n), ptype.length(n), ptype.min(n), ptype.max(n));
        return val[0];
    }

    private void encoder(final BinaryEncoder e) {
        encode = e;
        _evaluated = false;
    }

    private void decoder(final BinaryDecoder d) {
        decode = d;
        _evaluated = false;
    }
}
