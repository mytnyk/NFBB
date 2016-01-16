package ga;

/**
 * User: Oleg
 * Date: Jul 1, 2004
 * Time: 1:38:28 PM
 * Description:
 * ----------------------------------------------------------------------------
 * This object contains a set of alleles for use with a similarly typed genome.
 * This object can be used with any type that has operator= defined for it.  If
 * you use the remove member then you must have operator== defined for it.
 * This should be implemented as a derivative of the Array class?  But I don't
 * want that overhead at this point.  Also, behaviour is not the same.
 * The allele set uses the envelope/message structure.  The core allele object
 * is a reference-counted structure that contains all of the guts of an allele
 * set.  The outer shell, the allele set itself, is what users actually see.  It
 * defines the interface.  With this setup you can create a single allele set then
 * each genome does not have to make its own copy.  And we don't have to worry
 * about an allele set going out of scope then leaving genomes hanging (a problem
 * if we just used a pointer to a single alleleset with no reference counting).
 * You can link an allele set to another so that they share the same core.  Use
 * the 'link' member function (this is typically used within the GAlib to reduce
 * the number of actual alleleset instances when cloning populations of genomes).
 * There is no way to 'resize' an allele set.  You must add to it or remove
 * elements from it.
 * The base class assumes that the objects in the allele set are
 * complex, i.e. it is not OK to do a bit-copy of each object.  We should do
 * specializations for non-complex objects (or perhaps a separate class that does
 * bit-copies rather than logical-copies?)
 * When you clone an allele set, the new one has its own core.
 * Why didn't I do this as a couple of objects (enumerated set, bounded set,
 * discretized set, etc)?  I wanted to be able to have an array that uses sets of
 * many different types.  I suppose the allele set should be completely
 * polymorphic like the rest of the GAlib objects, but for now we'll do it as
 * a single object with multiple personalities (and a state).
 * There is no error checking.  You should check the type before you try to
 * call any of the member functions.  In particular, if you try to get the
 * bounds on an enumerated set of one element, it will break.
 * <p/>
 * ** should the assignment operator check to be sure that no allele is
 * duplicated, or is it OK to have duplicate alleles in a set?  For now we
 * allow duplicates (via either the add or assignemnt ops).
 * ----------------------------------------------------------------------------
 */
final class GAAlleleSetCore implements GAAllele, GAErrorIndex {
    int type;       // is this an ennumerated or bounded set?
    int lowerb;
    int upperb;     // what kind of limit is the bound?
    int cnt;        // how many objects are using us?
    final int csz;  // how big are the chunks to allocate?
    int sz;         // number we have
    int SZ;         // how many have we allocated?
    Object[] a;

    private static final int GA_ALLELE_CHUNK = 10;

    GAAlleleSetCore() {
        type = Type.ENUMERATED;
        csz = GA_ALLELE_CHUNK;
        sz = 0;
        SZ = 0;
        a = null;
        lowerb = BoundType.NONE;
        upperb = BoundType.NONE;
        cnt = 1;
    }

    GAAlleleSetCore(final int n, final Object[] array) {
        type = Type.ENUMERATED;
        csz = GA_ALLELE_CHUNK;
        sz = n;
        SZ = GA_ALLELE_CHUNK;

        while (SZ < sz) {
            SZ += csz;
        }
        a = new Object[SZ];

        System.arraycopy(array, 0, a, 0, sz);
        lowerb = BoundType.NONE;
        upperb = BoundType.NONE;
        cnt = 1;
    }

    GAAlleleSetCore(final Object lower, final Object upper,
                    final int lb, final int ub) {
        type = Type.BOUNDED;
        csz = GA_ALLELE_CHUNK;
        sz = 2;
        SZ = 2;
        a = new Object[SZ];
        a[0] = lower;
        a[1] = upper;
        lowerb = lb;
        upperb = ub;
        cnt = 1;
    }

    GAAlleleSetCore(final Object lower, final Object upper, final Object increment,
                    final int lb, final int ub) {
        type = Type.DISCRETIZED;
        csz = GA_ALLELE_CHUNK;
        sz = 3;
        SZ = 3;
        a = new Object[SZ];
        a[0] = lower;
        a[1] = upper;
        a[2] = increment;
        lowerb = lb;
        upperb = ub;
        cnt = 1;
    }

    GAAlleleSetCore(final GAAlleleSetCore orig) {
        // We do not copy the original's reference count!
        csz = orig.csz;
        sz = orig.sz;
        SZ = orig.SZ;
        a = new Object[SZ];

        System.arraycopy(orig.a, 0, a, 0, sz);

        lowerb = orig.lowerb;
        upperb = orig.upperb;
        type = orig.type;
        cnt = 1;
    }

    GAAlleleSetCore assign(final GAAlleleSetCore orig) {
        // Copying the contents of another allele set core does NOT change the current
        // count of the allele set core!
        if (equals(orig)) {
            return this;
        }
        if (SZ < orig.sz) {
            while (SZ < orig.sz) {
                SZ += csz;
            }
            a = new Object[SZ];
        }

        System.arraycopy(orig.a, 0, a, 0, orig.sz);

        sz = orig.sz;
        lowerb = orig.lowerb;
        upperb = orig.upperb;
        type = orig.type;

        return this;
    }

    void delete() {
        if (cnt > 0) {
            GAError.GAErr("GAAlleleSetCore", "destructor", gaErrRefsRemain);
        }
    }
}