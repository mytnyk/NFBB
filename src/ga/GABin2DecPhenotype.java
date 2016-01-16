package ga;

/**
 * User: Oleg
 * Date: Jul 7, 2004
 * Time: 1:50:47 PM
 * Description:
 */
public final class GABin2DecPhenotype {
    private GABin2DecPhenotypeCore core;

    public GABin2DecPhenotype() {
        core = new GABin2DecPhenotypeCore();
    }

    GABin2DecPhenotype(final GABin2DecPhenotype p) {
        core = new GABin2DecPhenotypeCore(p.core);
    }

    void delete() {
        core.cnt -= 1;
        if (core.cnt == 0) {
            core.delete();
        }
    }

    GABin2DecPhenotype assign(final GABin2DecPhenotype p) {
        if (p != this) {
            core = p.core;
        }
        return this;
    }

    public Object clone() {
        return new GABin2DecPhenotype(this);
    }

    void link(final GABin2DecPhenotype p) {
        core.cnt -= 1;
        if (core.cnt == 0) {
            core.delete();
        }
        core = p.core;
        core.cnt += 1;
    }

    /**
     * Another phenotype to this phenotype object.  If needed, we allocate more
     * space, otherwise just tag the new on then end.  We allocate space in chunks
     * so we don't spend too much time doing memory allocation stuff.
     */
    public void add(final int nb, final float min, final float max) {
        if (core.n + 1 > core.N) {
            core.N += core.csz;

            final short[] nbtmp = core.nbits;
            core.nbits = new short[core.N];
            if (nbtmp != null) {
                System.arraycopy(nbtmp, 0, core.nbits, 0, core.n);
            }
            //delete[] nbtmp;
            final short[] ostmp = core.oset;
            core.oset = new short[core.N];
            if (ostmp != null) {
                System.arraycopy(ostmp, 0, core.oset, 0, core.n);
            }
            //delete[] ostmp;
            final float[] mintmp = core.minval;
            core.minval = new float[core.N];
            if (mintmp != null) {
                System.arraycopy(mintmp, 0, core.minval, 0, core.n);
            }
            //delete[] mintmp;

            final float[] maxtmp = core.maxval;
            core.maxval = new float[core.N];
            if (maxtmp != null) {
                System.arraycopy(maxtmp, 0, core.maxval, 0, core.n);
            }
            //delete[] maxtmp;
        }
        core.nbits[core.n] = (short) nb;
        if (core.n > 0) {
            core.oset[core.n] = (short) (core.oset[core.n - 1] + core.nbits[core.n - 1]);
        } else {
            core.oset[core.n] = 0;
        }
        core.minval[core.n] = min;
        core.maxval[core.n] = max;
        core.n++;
        core.sz += nb;

    }

    void remove(final int x) {
        if (x >= core.n) {
            return;
        }
        System.arraycopy(core.nbits, x + 1, core.nbits, x, core.n - x - 1);
        System.arraycopy(core.oset, x + 1, core.oset, x, core.n - x - 1);
        System.arraycopy(core.minval, x + 1, core.minval, x, core.n - x - 1);
        System.arraycopy(core.maxval, x + 1, core.maxval, x, core.n - x - 1);
        core.n -= 1;
    }

    int size() {
        return core.sz;
    }

    public int nPhenotypes() {
        return core.n;
    }

    float min(final int which) {
        return core.minval[which];
    }

    float max(final int which) {
        return core.maxval[which];
    }

    int length(final int which) {
        return core.nbits[which];
    }

    int offset(final int which) {
        return core.oset[which];
    }

    boolean equal(final GABin2DecPhenotype b) {
        if (core.sz != b.core.sz || core.n != b.core.n) {
            return false;
        }
        // TODO: make sure such comparison works fina!
        if (core.nbits == b.core.nbits ||
            core.oset == b.core.oset ||
            core.minval == b.core.minval ||
            core.maxval == b.core.maxval) {
            return false;
        }
        return true;
    }
}
