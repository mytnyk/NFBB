package ga;

/**
 * User: Oleg
 * Date: Jul 7, 2004
 * Time: 3:25:49 PM
 * Description:
 * This class defines the interface for the 1D binary string genome, including
 * crossover objects and all the default and built-in operators.
 * ----------------------------------------------------------------------------
 * 1DBinaryStringGenome
 * -------------------------------------------------------------------------------
 * resize
 * These genomes are resizable.  In addition, you can set a resize behaviour
 * to specify the bounds in which the resize can occur.
 * <p/>
 * copy
 * Copy bits from the specified genome using the location and length
 * that are passed to us.  If the current genome is too short for the
 * entire length, copy whatever we can.  If the original genome is too
 * short for the specified length, copy whatever we can.  If either location
 * is out of bounds, return without doing anything.
 * We do NOT check for negative values in the locations!
 * This routine clips if the copy sizes do not match - it does NOT resize the
 * genome to fit the copy.  You'll have to do resizes before you call this
 * routine if you want the copy to fit the original.
 * <p/>
 * ==, !=
 * Are two genomes equal?  Our test for equality is based upon the
 * contents of the genome, NOT the behaviour.  So as long as the bitstreams
 * match, the genomes are 'equal'.  This means that a resizeable genome
 * may be equal to a fixed-length genome.  But a chromsome with 500
 * bits allocated is not equal to a genome with 10 bits allocated unless
 * both are the same size.
 * ----------------------------------------------------------------------------
 */

public class GA1DBinaryStringGenome extends GAGenome {
    private int nx;   // how long is the data string?
    int minX;  // what is the lower limit?
    int maxX;  // what is the upper limit?

    final GABinaryString bs;

    public String className() {
        return "GA1DBinaryStringGenome";
    }

    public int classID() {
        return BinaryStringGenome;
    }

    private final OnePointCrossover _OnePointCrossover = new OnePointCrossover();
    private final UniformInitializer _UniformInitializer = new UniformInitializer();
    private final FlipMutator _FlipMutator = new FlipMutator();
    final BitComparator _BitComparator = new BitComparator();

    static final class OnePointCrossover implements SexualCrossover {
        /**
         * Pick a point in the parents then grab alternating chunks for each child.
         * A word about crossover site mapping.  If a genome has width 10, the
         * cross site can assume a value of 0 to 10, inclusive.  A site of 0 means
         * that all of the material comes from the father.  A site of 10 means that
         * all of the material comes from the mother.  A site of 3 means that bits
         * 0-2 come from the mother and bits 3-9 come from the father.
         */
        public int sexualCrossover(final GAGenome p1, final GAGenome p2, final GAGenome c1, final GAGenome c2) {
            final GA1DBinaryStringGenome mom =
                    (GA1DBinaryStringGenome) p1;
            final GA1DBinaryStringGenome dad =
                    (GA1DBinaryStringGenome) p2;

            int n = 0;
            final int momsite;
            final int momlen;
            final int dadsite;
            final int dadlen;

            if (c1 != null && c2 != null) {
                final GA1DBinaryStringGenome sis = (GA1DBinaryStringGenome) c1;
                final GA1DBinaryStringGenome bro = (GA1DBinaryStringGenome) c2;

                if (sis.resizeBehaviour() == Size.FIXED_SIZE &&
                    bro.resizeBehaviour() == Size.FIXED_SIZE) {
                    if (mom.length() != dad.length() ||
                        sis.length() != bro.length() ||
                        sis.length() != mom.length()) {
                        GAError.GAErr(mom.className(), "one-point cross", GAError.gaErrSameLengthReqd);
                        return n;
                    }
                    momsite = dadsite = GARandom.GARandomInt(0, mom.length());
                    momlen = dadlen = mom.length() - momsite;
                } else if (sis.resizeBehaviour() == Size.FIXED_SIZE ||
                           bro.resizeBehaviour() == Size.FIXED_SIZE) {
                    GAError.GAErr(mom.className(), "one-point cross", GAError.gaErrSameBehavReqd);
                    return n;
                } else {
                    momsite = GARandom.GARandomInt(0, mom.length());
                    dadsite = GARandom.GARandomInt(0, dad.length());
                    momlen = mom.length() - momsite;
                    dadlen = dad.length() - dadsite;
                    sis.resize(momsite + dadlen);
                    bro.resize(dadsite + momlen);
                }

                sis.copy(mom, 0, 0, momsite);
                sis.copy(dad, momsite, dadsite, dadlen);
                bro.copy(dad, 0, 0, dadsite);
                bro.copy(mom, dadsite, momsite, momlen);

                n = 2;
            } else if (c1 != null || c2 != null) {
                final GA1DBinaryStringGenome sis = c1 != null ?
                                                   (GA1DBinaryStringGenome) c1 : (GA1DBinaryStringGenome) c2;

                if (sis.resizeBehaviour() == Size.FIXED_SIZE) {
                    if (mom.length() != dad.length() || sis.length() != mom.length()) {
                        GAError.GAErr(mom.className(), "one-point cross", GAError.gaErrSameLengthReqd);
                        return n;
                    }
                    momsite = dadsite = GARandom.GARandomInt(0, mom.length());
                    momlen = dadlen = mom.length() - momsite;
                } else {
                    momsite = GARandom.GARandomInt(0, mom.length());
                    dadsite = GARandom.GARandomInt(0, dad.length());
                    momlen = mom.length() - momsite;
                    dadlen = dad.length() - dadsite;
                    sis.resize(momsite + dadlen);
                }

                if (GARandom.GARandomBit() != 0) {
                    sis.copy(mom, 0, 0, momsite);
                    sis.copy(dad, momsite, dadsite, dadlen);
                } else {
                    sis.copy(dad, 0, 0, dadsite);
                    sis.copy(mom, dadsite, momsite, momlen);
                }

                n = 1;
            }
            return n;
        }
    }

    static final class UniformInitializer implements Initializer {
        /**
         * Set the bits of the genome to random values.  We use the library's
         * random bit function so we don't have to worry about machine-specific stuff.
         * We also do a resize so the genome can resize itself (randomly) if it
         * is a resizeable genome.
         */
        public void initializer(final GAGenome c) {
            final GA1DBinaryStringGenome child = (GA1DBinaryStringGenome) c;
            child.resize(Size.ANY_SIZE); // let chrom resize if it can
            for (int i = child.length() - 1; i >= 0; i--) {
                child.gene(i, (short) GARandom.GARandomBit()); // initial values are all random
            }
        }
    }

    static final class FlipMutator implements Mutator {
        /**
         * This function gets called a lot (especially for the simple ga) so it must
         * be as streamlined as possible.  If the mutation probability is small, then
         * we must call random on each bit in the string.  Otherwise, can can simply
         * mutate a known number of bits (based on the mutation rate).  We don't check
         * to see how many bits we flip, nor do we keep track of which ones got flipped
         * so this can result in an actual mutation that is lower than that specified,
         * but the bigger the genome and the smaller the mutation probability, the
         * better the chance that it will match the desired mutation rate.
         * If nMut is greater than 1, then we round up, so a mutation of 2.2 would
         * be 3 mutations, and 2.9 would be 3 as well.  nMut of 3 would be 3 mutations.
         */
        public int mutator(final GAGenome c, final float pmut) {
            final GA1DBinaryStringGenome child = (GA1DBinaryStringGenome) c;
            int n, i;
            if (pmut <= 0.0) {
                return 0;
            }

            float nMut = pmut * (float) child.length();
            if (nMut < 1.0) { // we have to do a flip test on each bit
                nMut = 0;
                for (i = child.length() - 1; i >= 0; i--) {
                    if (GARandom.GAFlipCoin(pmut)) {
                        child.gene(i, (short) (child.gene(i) == 0 ? 1 : 0));
                        nMut++;
                    }
                }
            } else {       // only flip the number of bits we need to flip
                for (n = 0; n < nMut; n++) {
                    i = GARandom.GARandomInt(0, child.length() - 1); // the index of the bit to flip
                    child.gene(i, (short) (child.gene(i) == 0 ? 1 : 0));
                }
            }
            return (int) nMut;
        }
    }

    static final class BitComparator implements Comparator {
        /**
         * Return a number from 0 to 1 to indicate how similar two genomes are.  For
         * the binary strings we compare bits.  We count the number of bits that are
         * the same then divide by the number of bits.  If the genomes are different
         * length then we return a -1 to indicate that we cannot calculate the
         * similarity.
         * Normal hamming distance makes use of population information - this is not
         * a hamming measure!  This is a similarity measure of two individuals, not
         * two individuals relative to the rest of the population.  This comparison is
         * independent of the population!  (you can do Hamming measure in the scaling
         * object)
         */
        public float comparator(final GAGenome a, final GAGenome b) {
            final GA1DBinaryStringGenome sis = (GA1DBinaryStringGenome) a;
            final GA1DBinaryStringGenome bro = (GA1DBinaryStringGenome) b;
            if (sis.length() != bro.length()) {
                return -1;
            }
            if (sis.length() == 0) {
                return 0;
            }
            float count = (float) 0.0;
            for (int i = sis.length() - 1; i >= 0; i--) {
                count += sis.gene(i) == bro.gene(i) ? 0 : 1;
            }
            return count / sis.length();
        }
    }

    GA1DBinaryStringGenome(final int x) {
        this(x, null);
    }

    GA1DBinaryStringGenome(final int x, final Evaluator f) {
        this(x, f, null);
    }

    /**
     * Set all the initial values to NULL or zero, then allocate the space we'll
     * need (using the resize method).  We do NOT call the initialize method at
     * this point - initialization must be done explicitly by the user of the
     * genome (eg when the population is created or reset).  If we called the
     * initializer routine here then we could end up with multiple initializations
     * and/or calls to dummy initializers (for example when the genome is
     * created with a dummy initializer and the initializer is assigned later on).
     */
    GA1DBinaryStringGenome(final int len, final Evaluator f, final Object u) {
        super(null, null, null); // little bit ugly:-(
        initializer(_UniformInitializer);
        mutator(_FlipMutator);
        comparator(_BitComparator);
        bs = new GABinaryString(len);
        evaluator(f);
        userData(u);
        crossover(_OnePointCrossover); // assign the default sexual crossover
        nx = minX = maxX = 0;
        resize(len);
    }

    /**
     * This is the copy initializer.  We set everything to the default values, then
     * copy the original.  The BinaryStringGenome creator takes care of zeroing
     * the data and sz members.
     */
    GA1DBinaryStringGenome(final GA1DBinaryStringGenome orig) {
        super(null, null, null);
        initializer(_UniformInitializer);
        mutator(_FlipMutator);
        comparator(_BitComparator);
        bs = new GABinaryString(orig.bs);
        nx = minX = maxX = 0;
        copy(orig);
    }

    GA1DBinaryStringGenome assign(final GAGenome arg) {
        copy(arg);
        return this;
    }

    final GA1DBinaryStringGenome assign(final short[] array) // no err checks!
    {
        for (int i = 0; i < bs.sz; i++) {
            gene(i, array[i]);
        }
        return this;
    }

    final GA1DBinaryStringGenome assign(final int[] array) // no err checks!
    {
        for (int i = 0; i < bs.sz; i++) {
            gene(i, (short) array[i]);
        }
        return this;
    }

    void delete() {

    }

    public Object clone() {
        return clone(CloneMethod.CONTENTS);
    }

    /**
     * The clone member creates a duplicate (exact or just attributes, depending
     * on the flag).  The caller is responsible for freeing the memory that is
     * allocated by this method.
     */
    Object clone(final int flag) {
        final GA1DBinaryStringGenome cpy = new GA1DBinaryStringGenome(nx);
        if (flag == CloneMethod.CONTENTS) {
            cpy.copy(this);
        } else {
            ((GAGenome) cpy).copy(this);
            cpy.maxX = maxX;
            cpy.minX = minX;
        }
        return cpy;
    }

    /**
     * This is the class-specific copy method.  It will get called by the super
     * class since the superclass operator= is set up to call ccopy (and that is
     * what we define here - a virtual function).  We should check to be sure that
     * both genomes are the same class and same dimension.  This function tries
     * to be smart about they way it copies.  If we already have data, then we do
     * a memcpy of the one we're supposed to copy.  If we don't or we're not the
     * same size as the one we're supposed to copy, then we adjust ourselves.
     * The BinaryStringGenome takes care of the resize in its copy method.
     * It also copies the bitstring for us.
     */
    void copy(final GAGenome orig) {
        if (orig == this) {
            return;
        }
        final GA1DBinaryStringGenome c = (GA1DBinaryStringGenome) orig;
        if (c != null) {
            super.copy(c);
            bs.copy(c.bs);
            nx = c.nx;
            minX = c.minX;
            maxX = c.maxX;
        }
    }

    boolean equal(final GAGenome c) {
        if (this == c) {
            return true;
        }
        final GA1DBinaryStringGenome b = (GA1DBinaryStringGenome) c;
        boolean eq = false;
        if (b != null) {
            eq = nx != b.nx ? false : bs.equal(b.bs, 0, 0, nx);
        }
        return eq;
    }

    final short gene() {
        return gene(0);
    }

    private short gene(final int x) {
        return bs.bit(x);
    }

    private short gene(final int x, final short value) {
        _evaluated = false;
        return bs.bit(x) == value ? value : bs.bit(x, value);
    }

    final short getAt(final int x) {
        return gene(x);
    }

    private int length() {
        return nx;
    }

    final int length(final int x) {
        resize(x);
        return nx;
    }

    /**
     * Resize the genome.  If someone specifies ANY_RESIZE then we pick a random
     * size within the behaviour limits that have been set.  If limits have been
     * set and someone passes us something outside the limits, we resize to the
     * closest bound.  If the genome is fixed size (ie min limit equals max limit)
     * then we resize to the specified value and move the min/max to match it.
     */
    final int resize(int l) {
        if (l == nx) {
            return nx;
        }
        if (l == Size.ANY_SIZE) {
            l = GARandom.GARandomInt(minX, maxX);
        } else if (l < 0) {
            return nx;  // do nothing
        } else if (minX == maxX) {
            minX = maxX = l;
        } else {
            if (l < minX) {
                l = minX;
            }
            if (l > maxX) {
                l = maxX;
            }
        }
        bs.resize(l);
        if (l > nx) {
            for (int i = nx; i < l; i++) {
                bs.bit(i, (short) GARandom.GARandomBit());
            }
        }
        nx = l;
        _evaluated = false;
        return bs.sz;
    }

    /**
     * Set the resize behaviour of the genome.  A genome can be fixed
     * length, resizeable with a max and min limit, or resizeable with no limits
     * (other than an implicit one that we use internally).
     * A value of 0 means no resize, a value less than zero mean unlimited
     * resize, and a positive value means resize with that value as the limit.
     * We return the upper limit of the genome's size.
     */
    final int resizeBehaviour(final int lower, final int upper) {
        if (upper < lower) {
            GAError.GAErr(className(), "resizeBehaviour", GAError.gaErrBadResizeBehaviour);
            return resizeBehaviour();
        }
        minX = lower;
        maxX = upper;
        if (nx > upper) {
            resize(upper);
        }
        if (nx < lower) {
            resize(lower);
        }
        return resizeBehaviour();
    }

    private int resizeBehaviour() {
        int val = maxX;
        if (maxX == minX) {
            val = Size.FIXED_SIZE;
        }
        return val;
    }

    private void copy(final GA1DBinaryStringGenome orig, final int r, final int x, int l) {
        if (l > 0 && x < orig.nx && r < nx) {
            if (x + l > orig.nx) {
                l = orig.nx - x;
            }
            if (r + l > nx) {
                l = nx - r;
            }
            bs.copy(orig.bs, r, x, l);
        }
        _evaluated = false;
    }

    final boolean equal(final GA1DBinaryStringGenome c, final int dest, final int src, final int len) {
        return bs.equal(c.bs, dest, src, len);
    }

    final void set(final int x, int l) {
        if (x + l > nx) {
            l = nx - x;
        }
        bs.set(x, l);
        _evaluated = false;
    }

    final void unset(final int x, int l) {
        if (x + l > nx) {
            l = nx - x;
        }
        bs.unset(x, l);
        _evaluated = false;
    }

    final void randomize(final int x, int l) {
        if (x + l > nx) {
            l = nx - x;
        }
        bs.randomize(x, l);
        _evaluated = false;
    }

    final void randomize() {
        bs.randomize();
    }

    final void move(final int x, final int srcx, int l) {
        if (srcx + l > nx) {
            l = nx - srcx;
        }
        if (x + l > nx) {
            l = nx - x;
        }
        bs.move(x, srcx, l);
        _evaluated = false;
    }
}