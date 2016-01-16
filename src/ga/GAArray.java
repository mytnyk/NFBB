package ga;

/**
 * User: Oleg
 * Date: Jul 1, 2004
 * Time: 4:38:36 PM
 * Description:
 * This defines the array base class.
 * <p/>
 * No error checking on the copy, so don't walk over end of array!
 */
final class GAArray {
    int sz;         // number of elements
    Object[] a;     // the contents of the array

    GAArray(final int s) {
        sz = s;
        a = sz != 0 ? new Object[sz] : null;
        for (int i = 0; i < sz; i++) {
            a[i] = null;
        }
    }

    private GAArray(final GAArray orig) {
        sz = 0;
        a = null;
        copy(orig);
    }

    GAArray assign(final GAArray orig) {
        copy(orig);
        return this;
    }

    GAArray assign(final Object[] array) // no err checks!
    {
        System.arraycopy(array, 0, a, 0, sz);
        return this;
    }

    void delete() {
        //delete [] a;
    }

    public Object clone() {
        return new GAArray(this);
    }

    Object[] get() {
        return a;
    }

    Object get(final int i) {
        return a[i];
    }

    void copy(final GAArray orig) {
        size(orig.sz);
        System.arraycopy(orig.a, 0, a, 0, sz);
    }

    void copy(final GAArray orig, final int dest,
              final int src, final int length) {
        for (int i = 0; i < length; i++) {
            a[dest + i] = orig.a[src + i];
        }
    }

    void move(final int dest, final int src, final int length) {
        if (src > dest) {
            for (int i = 0; i < length; i++) {
                a[dest + i] = a[src + i];
            }
        } else if (src < dest) {
            for (int i = length - 1; i != 0; i--) {
                a[dest + i] = a[src + i];
            }
        }
    }

    void swap(final int i, final int j) {
        final Object tmp = a[j];
        a[j] = a[i];
        a[i] = tmp;
    }

    private int size() {
        return sz;
    }

    int size(final int n) {
        if (n == sz) {
            return sz;
        }
        final Object[] tmp = n != 0 ? new Object[n] : null;
        for (int i = n < sz ? n - 1 : sz - 1; i >= 0; i--) {
            tmp[i] = a[i];
        }
        //delete [] a;
        a = tmp;
        return sz = n;
    }

    boolean equal(final GAArray b,
                  final int dest, final int src, final int length) {
        for (int i = 0; i < length; i++) {
            if (!a[dest + i].equals(b.a[src + i])) {
                return false;
            }
        }
        return true;
    }

    static boolean equal(final GAArray ar, final GAArray br) {
        if (ar.size() != br.size()) {
            return false;
        }
        return ar.equal(br, 0, 0, ar.sz);
    }

    static boolean notequal(final GAArray ar, final GAArray br) {
        if (ar.size() != br.size()) {
            return true;
        }
        return !ar.equal(br, 0, 0, ar.sz);
    }
}
