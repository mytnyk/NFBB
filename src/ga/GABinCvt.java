package ga;

/**
 * User: Oleg
 * Date: Jul 7, 2004
 * Time: 4:39:54 PM
 * Description:
 * Class for the binary to decimal conversion routines.  The binary-to-decimal
 * routines include encoders and decoders.  We define a standard binary encoder/
 * decoder set as well as one Gray encoder/decoder.  You can define your own if
 * you want a different Gray coding.
 * ----------------------------------------------------------------------------
 */

final class GABinCvt implements BinaryEncoder, BinaryDecoder {
// Define the number of bits based on the builtin type
    private static final int BITBASE = 4; // bytes in long
    private static final int BITS_IN_WORD = 8;
    private static final int _GA_MAX_BITS = (BITS_IN_WORD * BITBASE);

    /**
     * ----------------------------------------------------------------------------
     * This uses a threshhold rounding.  First we figure out what our discretization
     * will be based on the bit-resolution we're given (the number of bits we can use
     * to represent the interval).  Then we figure out the integer value that maps to
     * the entire discretized range and use this number to encode the string.  We have
     * to use a precision higher than that of the value that is to be encoded, so the
     * float/double is very important.
     * If we cannot match the value exactly, we'll undershoot rather than overshoot
     * the value.
     * If we could not get the mapping exactly then we set the value that was
     * passed to us to the one that we actually could encode and return a nonzero
     * status to indicate the problem.
     * We are limited in the conversion by two numbers - the nintervals (an int of
     * the maximum size for this system) and the interval (a double for the system).
     * nintervals must be able to count up to the number of intervals that we may
     * represent, ie nintervals must be able to represent 2^nbits.  sum and interval
     * must be able to keep the size of the interval with no loss of data, ie
     * (int)((maxval-minval)/interval)
     * must be equal to
     * ((nintervals=1) << nbits) - 1
     * with absolutely no roundoff error.  Practically, this means that we cannot do
     * anything greater than the number of bits needed to represent the mantissa of
     * the highest floating precision number on the system.
     * ----------------------------------------------------------------------------
     */
    public int encode(final float[] val, final short[] binstr, final int offset, final int nbits, final float minval,
                      final float maxval) {
        if (binstr == null || nbits == 0) {
            return 1;
        }
        if (val[0] < minval || maxval < val[0]) {
            return 1;
        }

        final long nintervals = 1;
        int status = GACheckEncoding(val, nbits, minval, maxval, nintervals);
        for (int i = 0; i < nbits; i++) {
            binstr[i] = 0;
        }
        status = _GAEncodeBase(2, nintervals, binstr, offset, 0, nbits - 1) ? 1 : status;
        return status;
    }

    /**
     * ----------------------------------------------------------------------------
     * Convert the string of bits into a decimal value.  This routine does no
     * scaling - the result that it generates will be a power of two.  You must
     * specify a number of bits so that we know how many bits to consider.  If you
     * specify too many bits, then we reset to the max we can handle and do the
     * conversion using those bits.
     * We return 1 if there was a problem, otherwise 0.
     * ----------------------------------------------------------------------------
     */
    public int decode(final float[] result, final short[] bits, final int offset, final int nbits, final float minval,
                      final float maxval) {

        if (bits == null || nbits == 0) {
            result[0] = (float) 0.0;
            return 1;
        }
        final int status = GACheckDecoding(nbits);

        long maxint = 1;
        float sum = (float) 0.0;
        for (int i = nbits - 1; i > -1; i--) {  // 0th bit is most significant
            if (bits[i + offset] != 0) {
                sum += (float) maxint;
            }
            maxint <<= 1;
        }
        maxint--;
        result[0] = minval + (maxval - minval) * sum / (float) maxint;
        return status;
    }

    /**
     * ----------------------------------------------------------------------------
     * Utilities to check the values for proper sizes.
     * ----------------------------------------------------------------------------
     */
    private static int GACheckEncoding(final float[] val, int nbits, final float minval, final float maxval,
                                       long nintervals) {
        int status = 0;
        if (nbits >= _GA_MAX_BITS) {
            GAError.GAErr("GACheckEncoding", GAError.gaErrBinStrTooLong,
                          "string is " + nbits + " bits, max is " + (_GA_MAX_BITS - 1), "");
            nbits = _GA_MAX_BITS - 1;
            status = 1;
        }

        nintervals = 1; // this type limits the number of bits we can do
        nintervals <<= nbits;
        nintervals--;

        final double interval = (maxval - minval) / (double) nintervals;
        double actual = (val[0] - minval) / interval;  // how many intervals we need
        nintervals = (long) actual;  // make it an integer
        actual = minval + (double) nintervals * interval; // get value we can represent

        if (actual != val[0]) {
            GAError.GAErr("GACheckEncoding", GAError.gaErrDataLost,
                          "desired: " + val + "\tactual: " + actual + "\tdiscretization: " + interval,
                          "  nbits: " + nbits + "\t\tmin: " + minval + "\t\tmax: " + maxval);
            val[0] = (float) actual;
            status = 1;
        }
        return status;
    }

    private static int GACheckDecoding(int nbits) {
        if (nbits >= _GA_MAX_BITS) {
            GAError.GAErr("GACheckDecoding", GAError.gaErrBinStrTooLong,
                          "string is " + nbits + " bits, max is " + (_GA_MAX_BITS - 1), "");
            nbits = _GA_MAX_BITS - 1;
            return 1;
        }
        return 0;
    }

    /**
     * ----------------------------------------------------------------------------
     * Utility routine to encode bits of a decimal number.  This routine recursively
     * loops through the decimal value and grabs the remainder (modulo the base) and
     * sticks the result into each 'bit' (if base is 2 then they're bits, otherwise
     * they're the equivalent for whatever base you're doing).
     * This will only do unsigned integers.
     * ----------------------------------------------------------------------------
     */
    private static boolean _GAEncodeBase(final int base, final long val, final short[] binstr, final int offset,
                                         final int n, final int c) {
        boolean status = false;
        if (c < 0) {
            return true;       // if this happens we should post an error
        }
        // it means we didn't get a perfect encoding
        binstr[c + offset] = (short) ((byte) val % base);
        final long quotient = val / base;
        if (quotient != 0) {
            status = _GAEncodeBase(base, quotient, binstr, offset, n, c - 1);
        }
        return status;
    }


}
