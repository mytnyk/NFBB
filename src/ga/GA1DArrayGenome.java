package ga;

/**
 * User: Oleg
 * Date: Jul 2, 2004
 * Time: 4:24:04 PM
 * Description:   Source file for the 1D array genome.
 */

public class GA1DArrayGenome extends GAGenome {
    int nx;               // how long is the data string?
    private int minX;     // what is the lower limit?
    private int maxX;     // what is the upper limit?

    GAArray array;

    final OnePointCrossover _OnePointCrossover = new OnePointCrossover();

    public String className() {
        return "GA1DArrayGenome";
    }

    public int classID() {
        return ArrayGenome;
    }

    protected GA1DArrayGenome(final int length) {
        this(length, null, null);
    }

    /**
     * Set all the initial values to NULL or zero, then allocate the space we'll
     * need (using the resize method).  We do NOT call the initialize method at
     * this point - initialization must be done explicitly by the user of the
     * genome (eg when the population is created or reset).  If we called the
     * initializer routine here then we could end up with multiple initializations
     * and/or calls to dummy initializers (for example when the genome is
     * created with a dummy initializer and the initializer is assigned later on).
     * Besides, we default to the no-initialization initializer by calling the
     * default genome constructor.
     */

    GA1DArrayGenome(final int length, final Evaluator f, final Object u) {
        super(null, null, null);
        array = new GAArray(length);
        evaluator(f);
        userData(u);
        nx = minX = maxX = length;
        crossover(_OnePointCrossover);
    }

    /**
     * This is the copy initializer.  We set everything to the default values, then
     * copy the original.
     */
    GA1DArrayGenome(final GA1DArrayGenome orig) {
        super(null, null, null);
        copy(orig);
    }

    static final class OnePointCrossover implements SexualCrossover {
        /**
         * Single point crossover for 1D array genomes.  Pick a single point then
         * copy genetic material from each parent.  We must allow for resizable genomes
         * so be sure to check the behaviours before we do the crossovers.  If resizing
         * is allowed then the children will change depending on where the site is
         * located.  It is also possible to have a mixture of resize behaviours, but
         * we won't worry about that at this point.  If this happens we just say that
         * we cannot handle that and post an error message.
         */
        public int sexualCrossover(final GAGenome p1, final GAGenome p2, final GAGenome c1, final GAGenome c2) {
            final GA1DArrayGenome mom = (GA1DArrayGenome) p1;
            final GA1DArrayGenome dad = (GA1DArrayGenome) p2;

            int nc = 0;
            final int momsite;
            final int momlen;
            final int dadsite;
            final int dadlen;

            if (c1 != null && c2 != null) {
                final GA1DArrayGenome sis = (GA1DArrayGenome) c1;
                final GA1DArrayGenome bro = (GA1DArrayGenome) c2;

                if (sis.resizeBehaviour() == Size.FIXED_SIZE &&
                    bro.resizeBehaviour() == Size.FIXED_SIZE) {
                    if (mom.length() != dad.length() ||
                        sis.length() != bro.length() ||
                        sis.length() != mom.length()) {
                        GAError.GAErr(mom.className(), "one-point cross", GAError.gaErrSameLengthReqd);
                        return nc;
                    }
                    momsite = dadsite = GARandom.GARandomInt(0, mom.length());
                    momlen = dadlen = mom.length() - momsite;
                } else if (sis.resizeBehaviour() == Size.FIXED_SIZE ||
                           bro.resizeBehaviour() == Size.FIXED_SIZE) {
                    GAError.GAErr(mom.className(), "one-point cross", GAError.gaErrSameBehavReqd);
                    return nc;
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

                nc = 2;
            } else if (c1 != null || c2 != null) {
                final GA1DArrayGenome sis = c1 != null ? (GA1DArrayGenome) c1 : (GA1DArrayGenome) c2;

                if (sis.resizeBehaviour() == Size.FIXED_SIZE) {
                    if (mom.length() != dad.length() || sis.length() != mom.length()) {
                        GAError.GAErr(mom.className(), "one-point cross", GAError.gaErrSameLengthReqd);
                        return nc;
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

                if (GARandom.GARandomBit() == 1) {
                    sis.copy(mom, 0, 0, momsite);
                    sis.copy(dad, momsite, dadsite, dadlen);
                } else {
                    sis.copy(dad, 0, 0, dadsite);
                    sis.copy(mom, dadsite, momsite, momlen);
                }

                nc = 1;
            }
            return nc;
        }
    }

    static final class UniformCrossover implements SexualCrossover {
        /**
         * Randomly take bits from each parent.  For each bit we flip a coin to see if
         * that bit should come from the mother or the father.  If strings are
         * different lengths then we need to use the mask to get things right.
         */
        public int sexualCrossover(final GAGenome p1, final GAGenome p2, final GAGenome c1, final GAGenome c2) {
            final GA1DArrayGenome mom = (GA1DArrayGenome) p1;
            final GA1DArrayGenome dad = (GA1DArrayGenome) p2;

            int n = 0;
            int i;

            if (c1 != null && c2 != null) {
                final GA1DArrayGenome sis = (GA1DArrayGenome) c1;
                final GA1DArrayGenome bro = (GA1DArrayGenome) c2;

                if (sis.length() == bro.length() &&
                    mom.length() == dad.length() &&
                    sis.length() == mom.length()) {
                    for (i = sis.length() - 1; i >= 0; i--) {
                        if (GARandom.GARandomBit() == 1) {
                            sis.gene(i, mom.gene(i));
                            bro.gene(i, dad.gene(i));
                        } else {
                            sis.gene(i, dad.gene(i));
                            bro.gene(i, mom.gene(i));
                        }
                    }
                } else {
                    final GAMask mask = new GAMask();
                    int start;
                    final int max = sis.length() > bro.length() ? sis.length() : bro.length();
                    final int min = mom.length() < dad.length() ? mom.length() : dad.length();
                    mask.size(max);
                    for (i = 0; i < max; i++) {
                        mask.setAt(i, (char) GARandom.GARandomBit());
                    }
                    start = sis.length() < min ? sis.length() - 1 : min - 1;
                    for (i = start; i >= 0; i--) {
                        sis.gene(i, mask.getAt(i) != 0 ? mom.gene(i) : dad.gene(i));
                    }
                    start = bro.length() < min ? bro.length() - 1 : min - 1;
                    for (i = start; i >= 0; i--) {
                        bro.gene(i, mask.getAt(i) != 0 ? dad.gene(i) : mom.gene(i));
                    }
                }
                n = 2;
            } else if (c1 != null || c2 != null) {
                final GA1DArrayGenome sis = c1 != null ? (GA1DArrayGenome) c1 : (GA1DArrayGenome) c2;

                if (mom.length() == dad.length() && sis.length() == mom.length()) {
                    for (i = sis.length() - 1; i >= 0; i--) {
                        sis.gene(i, GARandom.GARandomBit() == 1 ? mom.gene(i) : dad.gene(i));
                    }
                } else {
                    int min = mom.length() < dad.length() ? mom.length() : dad.length();
                    min = sis.length() < min ? sis.length() : min;
                    for (i = min - 1; i >= 0; i--) {
                        sis.gene(i, GARandom.GARandomBit() == 1 ? mom.gene(i) : dad.gene(i));
                    }
                }
                n = 1;
            }
            return n;
        }
    }

    static final class ElementComparator implements Comparator {
        /**
         * The comparator is supposed to return a number that indicates how similar
         * two genomes are, so here we just compare elements and return a number that
         * indicates how many elements match.  If they are different lengths then we
         * return -1 to indicate that we could not calculate the differences.
         * This assumes that there is an operator == defined for the object in the
         * elements of the array.
         */
        public float comparator(final GAGenome a, final GAGenome b) {
            final GA1DArrayGenome sis = (GA1DArrayGenome) a;
            final GA1DArrayGenome bro = (GA1DArrayGenome) b;

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

    public static final class SwapMutator implements Mutator {
        /**
         * Randomly swap elements in the array.
         */
        public int mutator(final GAGenome c, final float pmut) {
            final GA1DArrayGenome child = (GA1DArrayGenome) c;
            int n, i;
            if (pmut <= 0.0) {
                return 0;
            }

            float nMut = pmut * (float) child.length();
            final int length = child.length() - 1;
            if (nMut < 1.0) {   // we have to do a flip test on each bit
                nMut = 0;
                for (i = length; i >= 0; i--) {
                    if (GARandom.GAFlipCoin(pmut)) {
                        child.swap(i, GARandom.GARandomInt(0, length));
                        nMut++;
                    }
                }
            } else {    // only flip the number of bits we need to flip
                for (n = 0; n < nMut; n++) {
                    child.swap(GARandom.GARandomInt(0, length), GARandom.GARandomInt(0, length));
                }
            }
            return (int) nMut;
        }
    }

    static final class TwoPointCrossover implements SexualCrossover {
        /**
         * Two point crossover for the 1D array genome.  Similar to the single point
         * crossover, but here we pick two points then grab the sections based upon
         * those two points.
         * When we pick the points, it doesn't matter where they fall (one is not
         * dependent upon the other).  Make sure we get the lesser one into the first
         * position of our site array.
         */
        public int sexualCrossover(final GAGenome p1, final GAGenome p2, final GAGenome c1, final GAGenome c2) {
            final GA1DArrayGenome mom = (GA1DArrayGenome) p1;
            final GA1DArrayGenome dad = (GA1DArrayGenome) p2;

            int nc = 0;
            final int[] momsite = new int[2];
            final int[] momlen = new int[2];
            final int[] dadsite = new int[2];
            final int[] dadlen = new int[2];

            if (c1 != null && c2 != null) {
                final GA1DArrayGenome sis = (GA1DArrayGenome) c1;
                final GA1DArrayGenome bro = (GA1DArrayGenome) c2;
                if (sis.resizeBehaviour() == Size.FIXED_SIZE &&
                    bro.resizeBehaviour() == Size.FIXED_SIZE) {
                    if (mom.length() != dad.length() ||
                        sis.length() != bro.length() ||
                        sis.length() != mom.length()) {
                        GAError.GAErr(mom.className(), "two-point cross", GAError.gaErrSameLengthReqd);
                        return nc;
                    }
                    momsite[0] = GARandom.GARandomInt(0, mom.length());
                    momsite[1] = GARandom.GARandomInt(0, mom.length());
                    if (momsite[0] > momsite[1]) {
                        final int tmp = momsite[0];
                        momsite[0] = momsite[1];
                        momsite[1] = tmp;
                    }
                    momlen[0] = momsite[1] - momsite[0];
                    momlen[1] = mom.length() - momsite[1];
                    dadsite[0] = momsite[0];
                    dadsite[1] = momsite[1];
                    dadlen[0] = momlen[0];
                    dadlen[1] = momlen[1];
                } else if (sis.resizeBehaviour() == Size.FIXED_SIZE ||
                           bro.resizeBehaviour() == Size.FIXED_SIZE) {
                    return nc;
                } else {
                    momsite[0] = GARandom.GARandomInt(0, mom.length());
                    momsite[1] = GARandom.GARandomInt(0, mom.length());
                    if (momsite[0] > momsite[1]) {
                        final int tmp = momsite[0];
                        momsite[0] = momsite[1];
                        momsite[1] = tmp;
                    }
                    momlen[0] = momsite[1] - momsite[0];
                    momlen[1] = mom.length() - momsite[1];
                    dadsite[0] = GARandom.GARandomInt(0, dad.length());
                    dadsite[1] = GARandom.GARandomInt(0, dad.length());
                    if (dadsite[0] > dadsite[1]) {
                        final int tmp = dadsite[0];
                        dadsite[0] = dadsite[1];
                        dadsite[1] = tmp;
                    }
                    dadlen[0] = dadsite[1] - dadsite[0];
                    dadlen[1] = dad.length() - dadsite[1];
                    sis.resize(momsite[0] + dadlen[0] + momlen[1]);
                    bro.resize(dadsite[0] + momlen[0] + dadlen[1]);
                }
                sis.copy(mom, 0, 0, momsite[0]);
                sis.copy(dad, momsite[0], dadsite[0], dadlen[0]);
                sis.copy(mom, momsite[0] + dadlen[0], momsite[1], momlen[1]);
                bro.copy(dad, 0, 0, dadsite[0]);
                bro.copy(mom, dadsite[0], momsite[0], momlen[0]);
                bro.copy(dad, dadsite[0] + momlen[0], dadsite[1], dadlen[1]);
                nc = 2;
            } else if (c1 != null || c2 != null) {
                final GA1DArrayGenome sis = c1 != null ? (GA1DArrayGenome) c1 : (GA1DArrayGenome) c2;

                if (sis.resizeBehaviour() == Size.FIXED_SIZE) {
                    if (mom.length() != dad.length() || sis.length() != mom.length()) {
                        GAError.GAErr(mom.className(), "two-point cross", GAError.gaErrSameLengthReqd);
                        return nc;
                    }
                    momsite[0] = GARandom.GARandomInt(0, mom.length());
                    momsite[1] = GARandom.GARandomInt(0, mom.length());
                    if (momsite[0] > momsite[1]) {
                        final int tmp = momsite[0];
                        momsite[0] = momsite[1];
                        momsite[1] = tmp;
                    }
                    momlen[0] = momsite[1] - momsite[0];
                    momlen[1] = mom.length() - momsite[1];

                    dadsite[0] = momsite[0];
                    dadsite[1] = momsite[1];
                    dadlen[0] = momlen[0];
                    dadlen[1] = momlen[1];
                } else {
                    momsite[0] = GARandom.GARandomInt(0, mom.length());
                    momsite[1] = GARandom.GARandomInt(0, mom.length());
                    if (momsite[0] > momsite[1]) {
                        final int tmp = momsite[0];
                        momsite[0] = momsite[1];
                        momsite[1] = tmp;
                    }
                    momlen[0] = momsite[1] - momsite[0];
                    momlen[1] = mom.length() - momsite[1];
                    dadsite[0] = GARandom.GARandomInt(0, dad.length());
                    dadsite[1] = GARandom.GARandomInt(0, dad.length());
                    if (dadsite[0] > dadsite[1]) {
                        final int tmp = dadsite[0];
                        dadsite[0] = dadsite[1];
                        dadsite[1] = tmp;
                    }
                    dadlen[0] = dadsite[1] - dadsite[0];
                    dadlen[1] = dad.length() - dadsite[1];
                    sis.resize(momsite[0] + dadlen[0] + momlen[1]);
                }
                if (GARandom.GARandomBit() == 1) {
                    sis.copy(mom, 0, 0, momsite[0]);
                    sis.copy(dad, momsite[0], dadsite[0], dadlen[0]);
                    sis.copy(mom, momsite[0] + dadlen[0], momsite[1], momlen[1]);
                } else {
                    sis.copy(dad, 0, 0, dadsite[0]);
                    sis.copy(mom, dadsite[0], momsite[0], momlen[0]);
                    sis.copy(dad, dadsite[0] + momlen[0], dadsite[1], dadlen[1]);
                }
                nc = 1;
            }
            return nc;
        }
    }

    static final class EvenOddCrossover implements SexualCrossover {
        /**
         * Even and odd crossover for the array works just like it does for the
         * binary strings.  For even crossover we take the 0th element and every other
         * one after that from the mother.  The 1st and every other come from the
         * father.  For odd crossover, we do just the opposite.
         */
        public int sexualCrossover(final GAGenome p1, final GAGenome p2, final GAGenome c1, final GAGenome c2) {
            final GA1DArrayGenome mom = (GA1DArrayGenome) p1;
            final GA1DArrayGenome dad = (GA1DArrayGenome) p2;
            int nc = 0;
            int i;
            if (c1 != null && c2 != null) {
                final GA1DArrayGenome sis = (GA1DArrayGenome) c1;
                final GA1DArrayGenome bro = (GA1DArrayGenome) c2;
                if (sis.length() == bro.length() &&
                    mom.length() == dad.length() &&
                    sis.length() == mom.length()) {
                    for (i = sis.length() - 1; i >= 1; i -= 2) {
                        sis.gene(i, mom.gene(i));
                        bro.gene(i, dad.gene(i));
                        sis.gene(i - 1, dad.gene(i - 1));
                        bro.gene(i - 1, mom.gene(i - 1));
                    }
                    if (i == 0) {
                        sis.gene(0, mom.gene(0));
                        bro.gene(0, dad.gene(0));
                    }
                } else {
                    int start;
                    final int min = mom.length() < dad.length() ? mom.length() : dad.length();
                    start = sis.length() < min ? sis.length() - 1 : min - 1;
                    for (i = start; i >= 0; i--) {
                        sis.gene(i, i % 2 == 0 ? mom.gene(i) : dad.gene(i));
                    }
                    start = bro.length() < min ? bro.length() - 1 : min - 1;
                    for (i = start; i >= 0; i--) {
                        bro.gene(i, i % 2 == 0 ? dad.gene(i) : mom.gene(i));
                    }
                }
                nc = 2;
            } else if (c1 != null || c2 != null) {
                final GA1DArrayGenome sis = c1 != null ? (GA1DArrayGenome) c1 : (GA1DArrayGenome) c2;
                if (mom.length() == dad.length() && sis.length() == mom.length()) {
                    for (i = sis.length() - 1; i >= 1; i -= 2) {
                        sis.gene(i, mom.gene(i));
                        sis.gene(i - 1, dad.gene(i - 1));
                    }
                    if (i == 0) {
                        sis.gene(0, mom.gene(0));
                    }
                } else {
                    int min = mom.length() < dad.length() ? mom.length() : dad.length();
                    min = sis.length() < min ? sis.length() - 1 : min - 1;
                    for (i = min; i >= 0; i--) {
                        sis.gene(i, i % 2 == 0 ? mom.gene(i) : dad.gene(i));
                    }
                }
                nc = 1;
            }
            return nc;
        }
    }


    public static final class PartialMatchCrossover implements SexualCrossover {
        /**
         * Partial match crossover for the 1D array genome.  This uses the partial
         * matching algorithm described in Goldberg's book.
         * Parents and children must be the same size for this crossover to work.  If
         * they are not, we post an error message.
         * We make sure that b will be greater than a.
         */
        public int sexualCrossover(final GAGenome p1, final GAGenome p2, final GAGenome c1, final GAGenome c2) {
            final GA1DArrayGenome mom = (GA1DArrayGenome) p1;
            final GA1DArrayGenome dad = (GA1DArrayGenome) p2;

            int nc = 0;
            int a = GARandom.GARandomInt(0, mom.length());
            int b = GARandom.GARandomInt(0, dad.length());
            if (b < a) {
                final int tmp = a;
                a = b;
                b = tmp;
            }
            int i, j, index;

            if (mom.length() != dad.length()) {
                GAError.GAErr(mom.className(), "parial match cross", GAError.gaErrBadParentLength);
                return nc;
            }

            if (c1 != null && c2 != null) {
                final GA1DArrayGenome sis = (GA1DArrayGenome) c1;
                final GA1DArrayGenome bro = (GA1DArrayGenome) c2;

                sis.array.copy(mom.array);

                for (i = a, index = a; i < b; i++, index++) {
                    for (j = 0; j < sis.length() - 1 && sis.gene(j) != dad.gene(index); j++) {
                    }
                    sis.swap(i, j);
                }
                bro.array.copy(dad.array);
                for (i = a, index = a; i < b; i++, index++) {
                    for (j = 0; j < bro.length() - 1 && bro.gene(j) != mom.gene(index); j++) {
                    }
                    bro.swap(i, j);
                }

                nc = 2;
            } else if (c1 != null || c2 != null) {
                final GA1DArrayGenome sis = c1 != null ? (GA1DArrayGenome) c1 : (GA1DArrayGenome) c2;

                final GA1DArrayGenome parent1, parent2;
                if (GARandom.GARandomBit() != 0) {
                    parent1 = mom;
                    parent2 = dad;
                } else {
                    parent1 = dad;
                    parent2 = mom;
                }

                sis.array.copy(parent1.array);
                for (i = a, index = a; i < b; i++, index++) {
                    for (j = 0; j < sis.length() - 1 && sis.gene(j) != parent2.gene(index); j++) {
                    }
                    sis.swap(i, j);
                }
                nc = 1;
            }
            return nc;
        }
    }
/*
static int OrderCrossover( final GAGenome g1, final GAGenome g2,
                        GAGenome g3, GAGenome g4 ) {

}

static int CycleCrossover( final GAGenome g1, final GAGenome g2,
                        GAGenome g3, GAGenome g4 ) {

}       */

    public final int size() {
        return array.sz;
    }

    GA1DArrayGenome assign(final GAGenome orig) {
        copy(orig);
        return this;
    }

    GA1DArrayGenome assign(final Object[] array) // no err checks!
    {
        for (int i = 0; i < this.array.sz; i++) {
            gene(i, array[i]);
        }
        return this;
    }

    // Delete whatever we own.
    void delete() {
        // empty
    }

    public Object clone() {
        return clone(CloneMethod.CONTENTS);
    }

    Object clone(final int flag) {
        final GA1DArrayGenome cpy = new GA1DArrayGenome(nx);
        if (flag == CloneMethod.CONTENTS) {
            cpy.copy(this);
        } else {
            final GAGenome cpyg = cpy;
            cpyg.copy(this);
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
     * The Array takes care of the resize in its copy method.
     */
    void copy(final GAGenome orig) {
        if (orig.equals(this)) {
            return;
        }
        final GA1DArrayGenome c = (GA1DArrayGenome) orig;
        if (c != null) {
            super.copy(c);
            array.copy(c.array);
            nx = c.nx;
            minX = c.minX;
            maxX = c.maxX;
        }
    }

    boolean equal(final GAGenome c) {
        final GA1DArrayGenome b = (GA1DArrayGenome) c;
        return this == c ? true : nx != b.nx ? false : array.equal(b.array, 0, 0, nx);
    }

    public final Object gene(final int x) {
        return array.a[x];
    }

    public final Object gene(final int x, final Object value) {
        if (array.a[x] != value) {
            array.a[x] = value;
            _evaluated = false;
        }
        return array.a[x];
    }

    final int length() {
        return nx;
    }

    final int length(final int x) {
        resize(x);
        return nx;
    }

    /**
     * Resize the genome.
     * A negative value for the length means that we should randomly set the
     * length of the genome (if the resize behaviour is resizeable).  If
     * someone tries to randomly set the length and the resize behaviour is fixed
     * length, then we don't do anything.
     * We pay attention to the values of minX and maxX - they determine what kind
     * of resizing we are allowed to do.  If a resize is requested with a length
     * less than the min length specified by the behaviour, we set the minimum
     * to the length.  If the length is longer than the max length specified by
     * the behaviour, we set the max value to the length.
     * We return the total size (in bits) of the genome after resize.
     * We don't do anything to the new contents!
     */
    int resize(int len) {
        if (len == nx) {
            return nx;
        }
        if (len == Size.ANY_SIZE) {
            len = GARandom.GARandomInt(minX, maxX);
        } else if (len < 0) {
            return nx;  // do nothing
        } else if (minX == maxX) {
            minX = maxX = len;
        } else {
            if (len < minX) {
                len = minX;
            }
            if (len > maxX) {
                len = maxX;
            }
        }
        nx = array.size(len);
        _evaluated = false;
        return array.sz;
    }

    private int resizeBehaviour() {
        int val = maxX;
        if (maxX == minX) {
            val = Size.FIXED_SIZE;
        }
        return val;
    }

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

    private void copy(final GA1DArrayGenome orig,
                      final int r, final int x, int l) {
        if (l > 0 && x < orig.nx && r < nx) {
            if (x + l > orig.nx) {
                l = orig.nx - x;
            }
            if (r + l > nx) {
                l = nx - r;
            }
            array.copy(orig.array, r, x, l);
        }
        _evaluated = false;
    }

    public final void swap(final int i, final int j) {
        array.swap(i, j);
        _evaluated = false;
    }
}

