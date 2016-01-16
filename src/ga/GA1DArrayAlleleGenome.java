package ga;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 12:00:45 PM
 * Description:
 * ----------------------------------------------------------------------------
 * 1DArrayAlleleGenome
 * -------------------------------------------------------------------------------
 * We don't do any error checking on the assignment to const array of type T, so
 * the array may contain elements that are not in the allele set.
 * When we clone, we link the new allele set to our own so that we don't make
 * unnecessary copies.  If someone sets a new allele set on the genome, then we
 * make a complete new copy of the new one and break any link to a previous one.
 * It is OK to resize these genomes, so we don't have to protect the resize.
 * If this is an order-based genome then resizing should be done when the allele
 * set is changed, but there is nothing implicit in the object that tells us that
 * this is an order-based genome, so that's up to the user to take care of.  If
 * you're really concerned about catching this type of error, derive a class from
 * this class that does order-based protection.
 * I have defined all of the genome functions here to make it easier to
 * do specializations (you can specialize this class instead if its superclass).
 * We define our own resize so that we can set to allele values on resize to a
 * bigger length.
 * ----------------------------------------------------------------------------
 */

public final class GA1DArrayAlleleGenome extends GA1DArrayGenome {
    private int naset;
    private GAAlleleSet[] aset; // the allele set(s) for this genome

    public String className() {
        return "GA1DArrayAlleleGenome";
    }

    public int classID() {
        return ArrayAlleleGenome;
    }


    private final UniformInitializer _UniformInitializer = new UniformInitializer();
    private final FlipMutator _FlipMutator = new FlipMutator();
    private final ElementComparator _ElementComparator = new ElementComparator();

    static final class UniformInitializer implements Initializer {
        public void initializer(final GAGenome c) {
            final GA1DArrayAlleleGenome child =
                    (GA1DArrayAlleleGenome) c;
            child.resize(Size.ANY_SIZE); // let chrom resize if it can
            for (int i = child.length() - 1; i >= 0; i--) {
                child.gene(i, child.alleleset(i).allele());
            }
        }
    }

    static final class OrderedInitializer implements Initializer {
        public void initializer(final GAGenome c) {
            final GA1DArrayAlleleGenome child =
                    (GA1DArrayAlleleGenome) c;
            child.resize(Size.ANY_SIZE); // let chrom resize if it can
            final int length = child.length() - 1;
            int n = 0;
            int i;
            for (i = length; i >= 0; i--) {
                child.gene(i, child.alleleset().allele(n++));
                if (n >= child.alleleset().size()) {
                    n = 0;
                }
            }
            for (i = length; i >= 0; i--) {
                child.swap(i, GARandom.GARandomInt(0, length));
            }
        }
    }

    static final class FlipMutator implements Mutator {
        /**
         * Randomly pick elements in the array then set the element to any of the
         * alleles in the allele set for this genome.  This will work for any number
         * of allele sets for a given array.
         */
        public int mutator(final GAGenome c, final float pmut) {
            final GA1DArrayAlleleGenome child = (GA1DArrayAlleleGenome) c;
            int n, i;
            if (pmut <= 0.0) {
                return 0;
            }

            float nMut = pmut * (float) child.length();
            if (nMut < 1.0) {   // we have to do a flip test on each bit
                nMut = 0;
                for (i = child.length() - 1; i >= 0; i--) {
                    if (GARandom.GAFlipCoin(pmut)) {
                        child.gene(i, child.alleleset(i).allele());
                        nMut++;
                    }
                }
            } else { // only flip the number of bits we need to flip
                for (n = 0; n < nMut; n++) {
                    i = GARandom.GARandomInt(0, child.length() - 1);
                    child.gene(i, child.alleleset(i).allele());
                }
            }
            return (int) nMut;
        }
    }

    public GA1DArrayAlleleGenome(final int length, final GAAlleleSet sa, final Evaluator f) {
        this(length, sa, f, null);
    }

    private GA1DArrayAlleleGenome(final int length, final GAAlleleSet s, final Evaluator f, final Object u) {
        super(length, f, u);
        naset = 1;
        aset = new GAAlleleSet[1];
        aset[0] = s;

        initializer(_UniformInitializer);
        mutator(_FlipMutator);
        comparator(_ElementComparator);
        crossover(_OnePointCrossover);
    }

    GA1DArrayAlleleGenome(final GAAlleleSetArray sa, final Evaluator f, final Object u) {
        super(sa.size(), f, u);
        naset = sa.size();
        aset = new GAAlleleSet[naset];
        for (int i = 0; i < naset; i++) {
            aset[i] = sa.set(i);
        }

        initializer(_UniformInitializer);
        mutator(_FlipMutator);
        comparator(_ElementComparator);
        crossover(_OnePointCrossover);
    }

    private GA1DArrayAlleleGenome(final GA1DArrayAlleleGenome orig) {
        super(orig.array.sz);
        naset = 0;
        aset = null;
        copy(orig);
    }

    GA1DArrayGenome assign(final GAGenome arr) {
        copy(arr);
        return this;
    }

    GA1DArrayGenome assign(final Object[] array) // no err checks!
    {
        super.assign(array);
        return this;
    }

    // Delete the allele set
    public void delete() {
        //delete [] aset;
    }

    public Object clone() {
        return clone(CloneMethod.CONTENTS);
    }

    /**
     * This implementation of clone does not make use of the contents/attributes
     * capability because this whole interface isn't quite right yet...  Just
     * clone the entire thing, contents and all.
     */

    Object clone(final int flag) {
        return new GA1DArrayAlleleGenome(this);
    }

    void copy(final GAGenome orig) {
        if (orig.equals(this)) {
            return;
        }
        final GA1DArrayAlleleGenome c =
                (GA1DArrayAlleleGenome) orig;
        if (c != null) {
            super.copy(c);
            if (naset != c.naset) {
                //delete [] aset;
                naset = c.naset;
                aset = new GAAlleleSet[naset];
            }
            for (int i = 0; i < naset; i++) {
                aset[i] = new GAAlleleSet();
                aset[i].link(c.aset[i]);
            }
        }
    }

    boolean equal(final GAGenome c) {
        return super.equal(c);
    }

    int resize(final int len) {
        final int oldx = nx;
        super.resize(len);
        if (nx > oldx) {
            for (int i = oldx; i < nx; i++) {
                array.a[i] = aset[i % naset].allele();
            }
        }
        return len;
    }

    private GAAlleleSet alleleset() {
        return alleleset(0);
    }

    private GAAlleleSet alleleset(final int i) {
        return aset[i % naset];
    }
}
