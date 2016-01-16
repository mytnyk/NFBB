package ga;

/**
 * User: Oleg
 * Date: Jul 7, 2004
 * Time: 1:43:25 PM
 * Description:
 * This header defines the interface for the binary-to-decimal genome and the
 * phenotype objects used for the binary-to-decimal mappings in the
 * binary-to-decimal genomes.
 * The binary-to-decimal phenotype uses reference counting so that you only
 * need one instance of the underlying bit-to-float mapping.  Whenever you create
 * a new phenotype it gets its own core.  You can use the 'link' member to make
 * a phenotype refer to another's core.  This is what the genomes do when they
 * clone themselves so that you don't end up with multiple phenotypes running
 * around.  The core will persist until the last phenotype is destroyed (last one
 * out, please turn off the lights).
 * When you clone a phenotype, you get a competeley new one with its own core.
 * <p/>
 * TO DO:
 * binary to integer phenotype?
 * <p/>
 * TO DO:
 * ** Eventually we may want to cache the values in the phenotype mapping, but
 * for now we'll generate them on the fly.
 * ** Need to write a read method that can interpret binary/decimal input.
 * ----------------------------------------------------------------------------
 */
final class GABin2DecPhenotypeCore {
    private static final int GA_B2D_CHUNKSIZE = 20;
    int cnt;        // how many references to us?
    int csz;        // how big are the chunks we allocate?
    int n, N;       // how many phenotypes do we have? (real,alloc)
    short[] nbits;   // number of bits that max/min get mapped into
    short[] oset;    // offset of the nth gene
    float[] minval, maxval; // min, max value of phenotype elem
    int sz;         // total number of bits required

    GABin2DecPhenotypeCore() {
        csz = GA_B2D_CHUNKSIZE;
        n = 0;
        N = 0;
        sz = 0;
        nbits = oset = null;
        minval = maxval = null;
        cnt = 1;
    }

    GABin2DecPhenotypeCore(final GABin2DecPhenotypeCore p) {
        csz = p.csz;
        n = p.n;
        N = p.N;
        sz = p.sz;
        nbits = new short[N];
        oset = new short[N];
        minval = new float[N];
        maxval = new float[N];
        System.arraycopy(p.nbits, 0, nbits, 0, n);
        System.arraycopy(p.oset, 0, oset, 0, n);
        System.arraycopy(p.minval, 0, minval, 0, n);
        System.arraycopy(p.maxval, 0, maxval, 0, n);
        cnt = 1;
    }

    void delete() {
        if (cnt > 0) {
            GAError.GAErr("GABin2DecPhenotypeCore", "destructor", GAError.gaErrRefsRemain);
        }
        /*delete [] nbits;
        delete [] oset;
        delete [] minval;
        delete [] maxval;*/
    }

    GABin2DecPhenotypeCore assign(final GABin2DecPhenotypeCore p) {
        if (p == this) {
            return this;
        }
        /*delete [] nbits;
        delete [] oset;
        delete [] minval;
        delete [] maxval;*/
        n = p.n;
        sz = p.sz;
        N = p.N;
        csz = p.csz;
        nbits = new short[N];
        oset = new short[N];
        minval = new float[N];
        maxval = new float[N];
        System.arraycopy(p.nbits, 0, nbits, 0, n);
        System.arraycopy(p.oset, 0, oset, 0, n);
        System.arraycopy(p.minval, 0, minval, 0, n);
        System.arraycopy(p.maxval, 0, maxval, 0, n);
        return this;
    }

}

