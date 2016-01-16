package ga;

/**
 * User: Oleg
 * Date: Jul 7, 2004
 * Time: 3:09:18 PM
 * Description:
 * This class defines the interface for the binary string.  This is a crude
 * version of a real bitstring object.  We don't do real bitstring in the
 * interest of speed and ease of coding this mess up.
 * <p/>
 * TO DO:
 * we can get major improvements to speed by inlining functions and getting rid
 * of the error checking...  for example, inlining genome and removing the
 * conditional makes it go from 7.5 seconds to 3.2 seconds (bm bl cs 1024 c 0.9)
 * ----------------------------------------------------------------------------
 */
final class GABinaryString {
    private static final int GA_BINSTR_CHUNKSIZE = 32; // size of the chunks of bits we allocate

    int sz;  // size of chrom
    private int SZ;  // size of the memory allocated
    private int csz; // size of chunks we allocate
    short[] data;     // the data themselves

    GABinaryString(final int s) {
        csz = GA_BINSTR_CHUNKSIZE;
        sz = 0;
        SZ = 0;
        data = null;
        resize(s);
    }

    GABinaryString(final GABinaryString orig) {
        sz = 0;
        SZ = 0;
        data = null;
        copy(orig);
    }

    void delete() {
        //delete [] data;
    }

    /**
     * Copy the contents of the bitstream.  We don't care what format it is in -
     * we resize to make sure we have adequate space then we just copy all of the
     * data.
     * If the original is actually this, then we don't do anything.  If the
     * original is not the same class as this, then we post an error and return.
     */
    void copy(final GABinaryString orig) {
        if (orig == this) {
            return;
        }
        resize(orig.sz);
        System.arraycopy(orig.data, 0, data, 0, SZ);
    }

    /**
     * Resize the bitstream to the specified number of bits.  We return the number
     * of bits actually allocated.  For now there is no error checking or memory
     * management - we assume that we'll always get all of the memory we ask for.
     * If we resize, we copy the previous bits into the new space.  The memory
     * will never overlap (new should see to that) so we use memcpy not memmove.
     * If we're making more space, we set the contents of the new space to zeros.
     */
    int resize(final int x) {  // pass desired size, in bits
        if (sz == x) {
            return sz;
        }
        if (SZ < x) {
            while (SZ < x) {
                SZ += csz;
            }
            if (data != null) {
                data = new short[SZ];
            } else {
                final short[] tmp = data;
                data = new short[SZ];
                if (tmp != null) {
                    System.arraycopy(tmp, 0, data, 0, sz);
                }
                //delete [] tmp;
            }
        }
        return sz = x;
    }

    int size() {
        return sz;
    }

    short bit(final int a) {
        return data[a];
    }

    short bit(final int a, final short val) {    // set/unset the bit
        return data[a] = (short) (val != 0 ? 1 : 0);
    }

    boolean equal(final GABinaryString b, final int r, final int x, final int l) {
        // hope this works:
        boolean equal = true;
        for (int i = 0; i < l; i++) {
            if (data[r + i] != b.data[x + i]) {
                equal = false;
                break;
            }
        }
        return equal;
    }

    void copy(final GABinaryString orig, final int r, final int x, final int l) {
        System.arraycopy(orig.data, x, data, r, l);
    }

    void move(final int r, final int x, final int l) {
        System.arraycopy(data, x, data, r, l);
    }

    void set(final int a, final int l) {
        for (int i = 0; i < l; i++) {
            data[a + i] = 1;
        }
    }

    void unset(final int a, final int l) {
        for (int i = 0; i < l; i++) {
            data[a + i] = 0;
        }
    }

    void randomize(final int a, final int l) {
        for (int i = 0; i < l; i++) {
            data[i + a] = (short) GARandom.GARandomBit();
        }
    }

    void randomize() {
        for (int i = 0; i < sz; i++) {
            data[i] = (short) GARandom.GARandomBit();
        }
    }
}
