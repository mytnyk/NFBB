package ga;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 11:53:56 AM
 * Description: the class represents the allele set
 */
public final class GAAlleleSet implements GAAllele, GAErrorIndex {
    private GAAlleleSetCore core;

    public GAAlleleSet() {
        core = null;
    }

    public GAAlleleSet(final int n, final Object[] a) {
        core = new GAAlleleSetCore(n, a);
    }

    GAAlleleSet(final Object lower, final Object upper,
                final int lb, final int ub) {
        core = new GAAlleleSetCore(lower, upper, lb, ub);
    }

    GAAlleleSet(final Object lower, final Object upper, final Object increment,
                final int lb, final int ub) {
        core = new GAAlleleSetCore(lower, upper, increment, lb, ub);
    }

    GAAlleleSet(final GAAlleleSet set) {
        core = new GAAlleleSetCore(set.core);
    }

    public void delete() {
        if (core != null) {
            core.cnt -= 1;
            if (core.cnt == 0) {
                core.delete();
            }
        }
    }

    GAAlleleSet assign(final GAAlleleSet set) {
        if (equals(set)) {
            return this;
        }
        if (core != null) {
            core = set.core;
        } else {
            core = new GAAlleleSetCore(set.core);
        }
        return this;
    }

    public Object clone() {
        return new GAAlleleSet(this);
    }

    void link(final GAAlleleSet set) {
        // When we link to another allele set, we point our core to that one.  Be sure
        // that we have a core.  If not, just point.  If so, trash as needed.
        if (!set.equals(this)) {
            if (core != null) {
                core.cnt -= 1;
                if (core.cnt == 0) {
                    core.delete();
                }
            }
            core = set.core;
            core.cnt += 1;
        }
    }

    void unlink() {
        if (core == null) {
            return; // nothing to unlink
        }
        if (core.cnt > 1) {
            core.cnt -= 1;
            core = new GAAlleleSetCore(core);
        }
    }

    int size() {
        return core.sz;
    }    // only meaninful for enumerated sets

    public int add(final Object alle) {         // only for enumerated sets
        // If everthing goes OK, return 0.  If there's an error, we return -1.  I
        // really wish there were enough compilers doing exceptions to use them...
        if (core == null) {
            core = new GAAlleleSetCore();
        }
        if (core.type != Type.ENUMERATED) {
            return 1;
        }
        if (core.sz >= core.SZ) {
            core.SZ += core.csz;
            final Object[] tmp = core.a;
            core.a = new Object[core.SZ];
            if (tmp != null) {
                System.arraycopy(tmp, 0, core.a, 0, core.sz);
            }
            //delete[] tmp;
        }
        core.a[core.sz] = alle;
        core.sz += 1;
        return 0;
    }


    int remove(final Object allele) { // only for enumerated sets
        if (core == null) {
            core = new GAAlleleSetCore();
        }
        if (core.type != Type.ENUMERATED) {
            return 1;
        }
        for (int i = 0; i < core.sz; i++) {
            if (core.a[i].equals(allele)) {
                for (int j = i; j < core.sz - 1; j++) {
                    core.a[j] = core.a[j + 1];
                }
                core.sz -= 1;
                i = core.sz;// break out of the loop
            }
        }
        return 0;
    }

    int remove(final int x) { // only for enumerated sets
        for (int j = x; j < core.sz - 1; j++) {
            core.a[j] = core.a[j + 1];
        }
        core.sz -= 1;
        return 0;
    }

    /**
     * When returning an allele from the set, we have to know what type we are.
     * The allele that we return depends on the type.  If we're an enumerated set
     * then just pick randomly from the list of alleles.  If we're a bounded set
     * then pick randomly from the bounds, and respect the bound types.  If we're
     * a discretized set then we do much as we would for the bounded set, but we
     * respect the discretization.
     * Be sure to specialize this member function (see the real genome for an
     * example of how to do this)
     */

    Object allele() {         // ok for any type of set
        if (core.type == Type.ENUMERATED) {
            return core.a[GARandom.GARandomInt(0, core.sz - 1)];
        } else if (core.type == Type.DISCRETIZED) {
            GAError.GAErr("GAAlleleSet", "allele(unsigned int)", gaErrOpUndef);
            return core.a[0];
        } else {
            GAError.GAErr("GAAlleleSet", "allele(unsigned int)", gaErrOpUndef);
            return core.a[0];
        }
    }

    /**
     * This works only for enumerated sets.  If someone tries to use this on a
     * non-enumerated set then we post an error message.  No bounds checking on
     * the value that was passed to us, but we do modulo it so that we'll never
     * break.  Also, this means you can wrap an allele set around an array that
     * is significantly larger than the allele set that defines its contents.
     */
    Object allele(final int i) {
        if (core.type == Type.ENUMERATED) {
            return core.a[i % core.sz];
        } else if (core.type == Type.DISCRETIZED) {
            GAError.GAErr("GAAlleleSet", "allele(unsigned int)", gaErrOpUndef);
            return core.a[0];
        } else {
            GAError.GAErr("GAAlleleSet", "allele(unsigned int)", gaErrNoAlleleIndex);
            return core.a[0];
        }
    }

    Object lower() {
        return core.a[0];
    }   // only for bounded sets

    Object upper() {
        return core.a[1];
    }

    Object inc() {
        return core.a[2];
    }

    int lowerBoundType() {
        return core.lowerb;
    }

    int upperBoundType() {
        return core.upperb;
    }

    int type() {
        return core.type;
    }

    static int read() {
        GAError.GAErr("GAAlleleSet", "read", gaErrOpUndef);
        return 1;
    }

    static int write() {
        GAError.GAErr("GAAlleleSet", "write", gaErrOpUndef);
        return 1;
    }

    static int equal(final GAAlleleSet a, final GAAlleleSet b) {
        if (a.core.equals(b.core)) {
            return 1;
        }
        if (a.core == null || b.core == null) {
            return 0;
        }
        if (a.core.sz != b.core.sz) {
            return 0;
        }
        int i;
        for (i = 0; i < a.core.sz && a.core.a[i].equals(b.core.a[i]); i++) {
        }
        return i == a.core.sz ? 1 : 0;
    }

    static int notequal(final GAAlleleSet a, final GAAlleleSet b) {
        if (a.core.equals(b.core)) {
            return 0;
        }
        if (a.core == null || b.core == null) {
            return 1;
        }
        if (a.core.sz != b.core.sz) {
            return 1;
        }
        int i;
        for (i = 0; i < a.core.sz && a.core.a[i].equals(b.core.a[i]); i++) {
        }
        return i == a.core.sz ? 0 : 1;
    }
}

