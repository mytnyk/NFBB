package ga;

/**
 * User: Oleg
 * Date: Jul 6, 2004
 * Time: 12:04:05 PM
 * Description:
 */
final class GAMask {
    private char[] _mask;
    private int _n;

    GAMask() {
        _n = 0;
        _mask = null;
    }

    GAMask(final GAMask m) {
        _n = 0;
        _mask = null;
        copy(m);
    }

    GAMask assign(final GAMask m) {
        copy(m);
        return this;
    }

    void delete() {
        //delete[] _mask;
    }

    private void copy(final GAMask m) {
        size(m.size());
        System.arraycopy(m, 0, _mask, 0, _n);
    }
/*
    void clear() {
        memset(_mask, 0, _n * sizeof(GA_MASK_TYPE));
    }*/

    private int size() {
        return _n;
    }

    int size(final int s) {
        if (s > _n) {
            _n = s;
            //delete[] _mask;
            _mask = new char[_n];
        }
        return _n;
    }

    void setAt(final int i, final char c) {
        _mask[i] = c;
    }

    char getAt(final int i) {
        return _mask[i];
    }
}
