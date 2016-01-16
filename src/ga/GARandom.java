package ga;

/**
 * User: Oleg
 * Date: Jul 1, 2004
 * Time: 12:15:36 PM
 * Description:
 * ----------------------------------------------------------------------------
 * Initialize the random function by using the current time as the seed.
 * We use the time for the random seed in an attempt to get
 * rid of some of the periodicity from the low bits when using only the time as
 * the random seed.
 * These routines use the random/srandom routines to generate random numbers.
 * The documentation says that rand/srand is about 30% faster than random/srandom,
 * but rand/srand has a significantly smaller period so you'll get less random
 * results.  For the best results, use a RNG with a larger period (such as ran2).
 * This is *not* implemented as a separate RNG class because I wanted to be able
 * to inline everything as much as possible.  Also, we don't need to switch RNGs
 * for most purposes - I assume that compiling in the RNG is ok.  In addition,
 * many methods make use of RNG calls of a global nature - they should not contain
 * a RNG, and they may not have access to a RNG other than the global functions.
 * <p/>
 * GARandomInt, GARandomFloat, GARandomDouble
 * Return a number selected at random within the bounds low and high, inclusive.
 * Don't forget the 'inclusive' part!  If you're using this to get the index of
 * an element in an array, be sure to use the last element of the array, not the
 * number of elements in the array as the arguement for 'high'.
 * If you screw up and pass a value for low that is less than the value for
 * high, this routine does not complain.  It will merrily return a possibly
 * negative value.  We need speed here, so there's no error checking.
 * <p/>
 * GARandomBit
 * This is a faster implementation of GARandomInt(0,1).
 * <p/>
 * GAFlipCoin
 * Simulate a coin toss.  Use specified probability to bias toss.
 * <p/>
 * GAUnitGaussian
 * Returns a number from a Gaussian distribution with mean 0 and stddev of 1
 * <p/>
 * GAGaussianFloat, GAGaussianDouble
 * Scaled versions of the gaussian distribution.  You must specify a stddev,
 * then these functions scale the distribution to that deviation.  Mean is still 0
 * ----------------------------------------------------------------------------
 */

// Here we determine which random number generator will be used.  The critical
// parts here are the name of the random number generator (e.g. rand or random)
// the name of the seed routine (e.g. srand or srandom), the maximum value of
// the number returned by the generator (e.g. RAND_MAX or LONG_MAX)
//   If you want to use your own random function, this is where to declare it.
// Just set the _GA_RND macros to the appropriate values for your random number
// generator.
//   There are *many* ways to speed things up here if you know specifics about
// the machine on which you're going to be running the code.  For example, if
// you have a floating coprocessor then you might want to use ran3 and convert
// it so that it is entirely floating point (as described in NR in C).  Or if
// you're really daring, substitute some assembly code.  For many bit-flipping
// genetic algorithms the random number generator is the bottleneck, so this
// isn't totally useless musing...

public final class GARandom {
    private static final int IB1 = 1;
    private static final int IB2 = 2;
    private static final int IB5 = 16;
    private static final long IB18 = 131072L;
    private static final int MASK = (IB1 + IB2 + IB5);

    private static final long IA = 16807L;
    private static final long IM = 2147483647L;
    private static final double AM = (1.0 / IM);
    private static final long IQ = 127773L;
    private static final long IR = 2836L;
    private static final int NTAB = 32;
    private static final long NDIV = (1 + (IM - 1) / NTAB);
    private static final double EPS = 1.2e-7;
    private static final double RNMX = (1.0 - EPS);

    private static long iy = 0;
    private static final long[] iv = new long[NTAB];
    private static long idum = 0;

    private static boolean cached = false;
    private static double cachevalue = 0;
    private static int seed = 0;
    private static long iseed = 0;

    private static void sran(final int s) {
        int j;
        long k;

        idum = s;
        if (idum == 0) {
            idum = 1;
        }
        if (idum < 0) {
            idum = -idum;
        }
        for (j = NTAB + 7; j >= 0; j--) {
            k = idum / IQ;
            idum = IA * (idum - k * IQ) - IR * k;
            if (idum < 0) {
                idum += IM;
            }
            if (j < NTAB) {
                iv[j] = idum;
            }
        }
        iy = iv[0];
    }

    private static float ran() {
        final int j;
        final long k;
        final float temp;

        k = idum / IQ;
        idum = IA * (idum - k * IQ) - IR * k;
        if (idum < 0) {
            idum += IM;
        }
        j = (int) (iy / NDIV);
        iy = iv[j];
        iv[j] = idum;
        if ((temp = (float) (AM * iy)) > RNMX) {
            return (float) RNMX;
        } else {
            return temp;
        }
    }

    static int GARandomInt() {
        return ran() > 0.5 ? 1 : 0;
    }

    static String GAGetRNG() {
        return "RAND";
    }

    public static int GARandomInt(final int low, final int high) {
        float val = (float) (high - low + 1);
        val *= ran();
        return (int) val + low;
    }

    private static double GARandomDouble() {
        return ran();
    }

    static double GARandomDouble(final double low, final double high) {
        double val = high - low;
        val *= ran();
        return val + low;
    }

    static float GARandomFloat() {
        return ran();
    }

    public static float GARandomFloat(final float low, final float high) {
        float val = high - low;
        val *= ran();
        return val + low;
    }

    static int GAGetRandomSeed() {
        return seed;
    }

    private static void bitseed(final int s) {
        iseed = s;
    }

    /**
     * @param s Seed the random number generator with an appropriate value.  We seed both
     *          the random number generator and the random bit generator.  Set the seed only
     *          if a seed is not specified.  If a seed is specified, then set the seed to
     *          the specified value and use it.  We remember the seed so that multiple calls
     *          to this function with the same seed do not reset the generator.  Subsequent
     *          calls to this function with a different seed will initialize the generator
     *          to the new seed.  Multiple calls with a value of 0 do nothing (we do *not*
     *          re-seed the generator because 0 is the default value and we don't want
     *          people to re-seed the generator inadvertantly).
     *          Some systems return a long as the return value for time, so we need to be
     *          sure to get whatever variation from it that we can since our seed is only an
     *          unsigned int.
     */
    public static void GARandomSeed(final int s) {
        if (s == 0 && seed == 0) {
            long tmp;
            while (seed == 0) {
                tmp = System.currentTimeMillis();
                for (int i = 0; i < 8 * 4; i++) {
                    seed += tmp & 1 << i;
                }
            }
            sran(seed);
            bitseed(seed);
        } else if (s != 0 && seed != s) {
            seed = s;
            sran(seed);
            bitseed(seed);
        }
    }


    /**
     * Similar to setting the random seed, but this one sets it as long as the
     * specified seed is non-zero.
     */
    static void GAResetRNG(final int s) {
        if (s != 0) {
            seed = s;
            sran(seed);
            bitseed(seed);
        }
    }

    public static int GARandomBit() {
        if ((iseed & IB18) != 0) {
            iseed = (iseed ^ MASK) << 1 | IB1;
            return 1;
        } else {
            iseed <<= 1;
            return 0;
        }
    }

    /**
     * @return Return a number from a unit Gaussian distribution.  The mean is 0 and the
     *         standard deviation is 1.0.
     *         First we generate two uniformly random variables inside the complex unit
     *         circle.  Then we transform these into Gaussians using the Box-Muller
     *         transformation.  This method is described in Numerical Recipes in C
     *         ISBN 0-521-43108-5 at http://world.std.com/~nr
     *         When we find a number, we also find its twin, so we cache that here so
     *         that every other call is a lookup rather than a calculation.  (I think GNU
     *         does this in their implementations as well, but I don't remember for
     *         certain.)
     */

    private static double GAUnitGaussian() {
        if (cached) {
            cached = false;
            return cachevalue;
        }

        double rsquare;
        final double factor;
        double var1;
        double var2;
        do {
            var1 = 2.0 * GARandomDouble() - 1.0;
            var2 = 2.0 * GARandomDouble() - 1.0;
            rsquare = var1 * var1 + var2 * var2;
        } while (rsquare >= 1.0 || rsquare == 0.0);

        final double val = -2.0 * Math.log(rsquare) / rsquare;
        if (val > 0.0) {
            factor = Math.sqrt(val);
        } else {
            factor = 0.0; // should not happen, but might due to roundoff
        }

        cachevalue = var1 * factor;
        cached = true;

        return var2 * factor;
    }

    static boolean GAFlipCoin(final float p) {
        return p == 1.0 ? true : p == 0.0 ? false : GARandomFloat() <= p ? true : false;
    }

    static float GAGaussianFloat(final float dev) {
        return (float) GAUnitGaussian() * dev;
    }

    static double GAGaussianDouble(final double dev) {
        return GAUnitGaussian() * dev;
    }
}