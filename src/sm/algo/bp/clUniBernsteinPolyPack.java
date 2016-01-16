package sm.algo.bp;

import sm.base.util.clMathEx;

/**
 * User: Oleg
 * Date: 20/6/2004
 * Time: 15:58:14
 * Description: class implements univariate polynomials
 * polynomials order - d
 * for univariate polynomial:
 * (total numbers - d+1)
 * expansion of [s+(1-s)]^d or [p+q]^d, p+q = 1
 */
public final class clUniBernsteinPolyPack extends clBernsteinPolyPack {

    private final class clUniBernsteinPoly extends clBernsteinPoly {

        private clUniBernsteinPoly(final int[] vItem) {
            super(vItem); // vItem[0] should be [0.._iOrder]
            /*if (vItem.length != 1 || vItem[0] > m_iOrder) {
                throw new clPolynomialException("Wrong univariate polynomial construction!");
            } */
        }

        public double getValue(final double[] v) throws clPolynomialException {

            if (v.length != 1 || v[0] > 1 || v[0] < 0) {
                throw new clPolynomialException("Wrong univariate polynomial argument!");
            }

            final double sd = v[0];

            if (sd > 1 || sd < 0) {
                throw new clPolynomialException("Wrong univariate polynomial argument!");
            }

            final int m = m_iOrder;
            final int j = m_vInstance[0];

            return (double) (clMathEx.fact(m) / (clMathEx.fact(j) * clMathEx.fact(m - j))) * clMathEx.pow(sd, j) *
                   clMathEx.pow(1.0 - sd, m - j);
        }

        public String toString() {
            return "Univariate bernstein polynomial " + getClass().getName();
        }
    }

    public clUniBernsteinPolyPack(final int iOrder) {
        super(iOrder, iOrder + 1); // only for univariate
        m_cPoly = new clUniBernsteinPoly[m_iDim];
        // initialize here the polynomials:
        final int[][] vInstance = new int[m_iDim][1];
        for (int i = 0; i < m_iDim; i++) {// addData new polynomial to the pack
            vInstance[i][0] = i;
            m_cPoly[i] = new clUniBernsteinPoly(vInstance[i]);
        }
    }

    public String toString() {
        return "Univariate bernstein polynomials pack " + getClass().getName();
    }
}
