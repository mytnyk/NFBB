package sm.base.util;

/**
 * User: Oleg
 * Date: 20/6/2004
 * Time: 10:46:04
 * Description: Advanced math functions.
 */
public final class clMathEx {

    public static double det(final double a11, final double a12, final double a13,
                             final double a21, final double a22, final double a23,
                             final double a31, final double a32, final double a33) {
        return a11 * a22 * a33 + a31 * a12 * a23 + a13 * a21 * a32 - a13 * a22 * a31 - a33 * a12 * a21 -
               a11 * a32 * a23;
    }

    public static int fact(final int n) {
        if (n == 0) {
            return 1;
        }
        return n * fact(n - 1);
    }

    public static double pow(final double x, final int y) {
        if (y == 0) {
            return 1.0;
        }
        return x * pow(x, y - 1);
    }

    public static String formatDouble(final double d) {
        return formatDouble(d, 2);
    }

    public static String formatDouble(final double d, final int iPrecision) {
        int p = (int) Math.pow(10, iPrecision);

        String s = Double.toString(Math.floor(d * p + 0.5d) / p);
        /*
        if (s.length() > 3+iPrecision) {
            s = s.substring(0, 2+iPrecision);
        } */
        return s;
    }
}
