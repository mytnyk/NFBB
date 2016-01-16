package ga;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 12:01:33 PM
 * Description: the class represents the array of allele sets
 */
final class GAAlleleSetArray {
    private final int csz;
    private int sz;
    private int SZ;
    private GAAlleleSet[] aset;

    private static final int GA_ALLELE_CHUNK = 10;

    GAAlleleSetArray() {
        csz = GA_ALLELE_CHUNK;
        sz = 0;
        SZ = 0;
        aset = null;
    }

    GAAlleleSetArray(final GAAlleleSet s) {
        csz = GA_ALLELE_CHUNK;
        sz = 1;
        SZ = GA_ALLELE_CHUNK;
        aset = new GAAlleleSet[GA_ALLELE_CHUNK];
        aset[0] = new GAAlleleSet(s);
    }

    GAAlleleSetArray(final GAAlleleSetArray orig) {
        csz = orig.csz;
        sz = orig.sz;
        SZ = orig.SZ;
        aset = new GAAlleleSet[orig.SZ];
        for (int i = 0; i < sz; i++) {
            aset[i] = new GAAlleleSet(orig.set(i));
        }
    }


    void delete() {
        for (int i = 0; i < sz; i++) {
            aset[i].delete();
        }
        //delete [] aset;
    }

    GAAlleleSetArray assign(final GAAlleleSetArray orig) {
        if (equals(orig)) {
            return this;
        }
        for (int i = 0; i < sz; i++) {
            aset[i].delete();
        }
        if (SZ < orig.sz) {
            while (SZ < orig.sz) {
                SZ += csz;
            }
            //delete [] aset;
            aset = new GAAlleleSet[SZ];
        }
        for (int i = 0; i < orig.sz; i++) {
            aset[i] = new GAAlleleSet(orig.set(i));
        }
        sz = orig.sz;
        return this;
    }

    int size() {
        return sz;
    }

    GAAlleleSet set(final int i) {
        return aset[i];
    }

    int add(final GAAlleleSet s) {
        if (sz + 1 > SZ) {
            SZ += csz;
            final GAAlleleSet[] tmp = aset;
            aset = new GAAlleleSet[SZ];
            System.arraycopy(tmp, 0, aset, 0, sz);
            //delete [] tmp;
        }
        aset[sz] = new GAAlleleSet(s);
        sz++;
        return 0;
    }

    int add(final int n, final Object[] a) {
        if (sz + 1 > SZ) {
            SZ += csz;
            final GAAlleleSet[] tmp = aset;
            aset = new GAAlleleSet[SZ];
            System.arraycopy(tmp, 0, aset, 0, sz);
            //delete [] tmp;
        }
        aset[sz] = new GAAlleleSet(n, a);
        sz++;
        return 0;
    }

    int add(final Object lower, final Object upper,
            final int lb, final int ub) {
        if (sz + 1 > SZ) {
            SZ += csz;
            final GAAlleleSet[] tmp = aset;
            aset = new GAAlleleSet[SZ];
            System.arraycopy(tmp, 0, aset, 0, sz);
            //delete [] tmp;
        }
        aset[sz] = new GAAlleleSet(lower, upper, lb, ub);
        sz++;
        return 0;
    }

    int add(final Object lower, final Object upper, final Object increment,
            final int lb, final int ub) {
        if (sz + 1 > SZ) {
            SZ += csz;
            final GAAlleleSet[] tmp = aset;
            aset = new GAAlleleSet[SZ];
            System.arraycopy(tmp, 0, aset, 0, sz);
            //delete [] tmp;
        }
        aset[sz] = new GAAlleleSet(lower, upper, increment, lb, ub);
        sz++;
        return 0;
    }

    int remove(final int n) {
        if (n >= sz) {
            return 1;
        }
        aset[n].delete();
        for (int i = n; i < sz - 1; i++) {
            aset[i] = aset[i + 1];
        }
        sz--;
        return 0;
    }
}